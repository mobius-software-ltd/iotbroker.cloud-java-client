package com.mobiussoftware.iotbroker.amqp;

/**
* Mobius Software LTD
* Copyright 2015-2018, Mobius Software LTD
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.mobius.software.amqp.parser.avps.OutcomeCode;
import com.mobius.software.amqp.parser.avps.ReceiveCode;
import com.mobius.software.amqp.parser.avps.RoleCode;
import com.mobius.software.amqp.parser.avps.SectionCode;
import com.mobius.software.amqp.parser.avps.SendCode;
import com.mobius.software.amqp.parser.avps.TerminusDurability;
import com.mobius.software.amqp.parser.header.api.AMQPHeader;
import com.mobius.software.amqp.parser.header.impl.*;
import com.mobius.software.amqp.parser.sections.AMQPData;
import com.mobius.software.amqp.parser.sections.AMQPSection;
import com.mobius.software.amqp.parser.sections.MessageHeader;
import com.mobius.software.amqp.parser.terminus.AMQPSource;
import com.mobius.software.amqp.parser.terminus.AMQPTarget;
import com.mobius.software.amqp.parser.tlv.impl.AMQPAccepted;
import com.mobius.software.amqp.parser.wrappers.AMQPMessageFormat;
import com.mobius.software.amqp.parser.wrappers.AMQPSymbol;
import com.mobius.software.mqtt.parser.avps.QoS;
import com.mobius.software.mqtt.parser.avps.Text;
import com.mobius.software.mqtt.parser.avps.Topic;
import com.mobiussoftware.iotbroker.amqp.net.AmqpTlsClient;
import com.mobiussoftware.iotbroker.amqp.net.TCPClient;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.DBTopic;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;
import com.mobiussoftware.iotbroker.network.NetworkClient;
import com.mobiussoftware.iotbroker.network.TopicListener;

public class AmqpClient implements ConnectionListener<AMQPHeader>, AMQPDevice, NetworkClient
{
	private final Logger logger = Logger.getLogger(getClass());

	public static String MESSAGETYPE_PARAM = "MESSAGETYPE";
	private Integer RESEND_PERIOND = 3000;
	private Integer WORKER_THREADS = 4;

	private InetSocketAddress address;
	private ConnectionState connectionState;

	private TimersMap timers;
	private TCPClient client;

	private Account account;

	private ClientListener listener;
	private TopicListener topicListener;

	private DBInterface dbInterface;

	private Boolean isSaslConfirm = false;

	private int channel;
	private AtomicLong nextHandle = new AtomicLong();
	private ConcurrentHashMap<String, Long> usedIncomingMappings = new ConcurrentHashMap<String, Long>();
	private ConcurrentHashMap<String, Long> usedOutgoingMappings = new ConcurrentHashMap<String, Long>();
	private ConcurrentHashMap<Long, String> usedIncomingHandles = new ConcurrentHashMap<Long, String>();
	private ConcurrentHashMap<Long, String> usedOutgoingHandles = new ConcurrentHashMap<Long, String>();
	private List<AMQPTransfer> pendingMessages = new ArrayList<AMQPTransfer>();

	private Long idleTimeout;

	private ConcurrentHashMap<String, Long> pendingSubscribes = new ConcurrentHashMap<>();

	public AmqpClient(Account account) throws Exception
	{
		this.dbInterface = DBHelper.getInstance();
		this.address = new InetSocketAddress(account.getServerHost(), account.getServerPort());
		this.account = account;
		if (!account.isSecure())
			client = new TCPClient(address, WORKER_THREADS);
		else
			client = new AmqpTlsClient(address, WORKER_THREADS, account.getCertificate(), account.getCertificatePassword());
	}

	public Long getKeepalivePeriod()
	{
		if (idleTimeout == null)
			return account.getKeepAlive() * 1000L;

		return idleTimeout;
	}

	@Override
	public void setClientListener(ClientListener clientListener)
	{
		this.listener = clientListener;
	}

	@Override
	public void setTopicListener(TopicListener topicListener)
	{
		this.topicListener = topicListener;
	}

	public void setListener(ClientListener listener)
	{
		this.listener = listener;
	}

	public void setState(ConnectionState state)
	{
		this.connectionState = state;
		if (this.listener != null)
			listener.stateChanged(state);
	}

	public Boolean createChannel()
	{
		setState(ConnectionState.CHANNEL_CREATING);
		Boolean isSuccess = client.init(this);
		if (!isSuccess)
			setState(ConnectionState.CHANNEL_FAILED);

		return isSuccess;
	}

	public Boolean isConnected()
	{
		return connectionState == ConnectionState.CONNECTION_ESTABLISHED;
	}

	public ConnectionState getConnectionState()
	{
		return connectionState;
	}

	public InetSocketAddress getAddress()
	{
		return address;
	}

	public void closeChannel()
	{
		if (client != null)
			client.shutdown();
	}

	public void connect()
	{
		setState(ConnectionState.CONNECTING);

		timers = new TimersMap(this, client, RESEND_PERIOND);
		timers.stopAllTimers();
		timers.storeConnectTimer();

		AMQPProtoHeader header = new AMQPProtoHeader(3);
		client.send(header);
	}

	public void disconnect()
	{
		if (client.isConnected())
		{
			AMQPEnd end = new AMQPEnd();
			end.setChannel(channel);
			client.send(end);

			if (timers != null)
				timers.stopAllTimers();

			timers = null;
		}

		return;
	}

	public void subscribe(Topic[] topics)
	{
		for (Topic topic : topics)
		{
			Long currHandler = usedIncomingMappings.get(topic.getName().toString());
			if (currHandler == null)
				currHandler = nextHandle.incrementAndGet();

			pendingSubscribes.put(topic.getName().toString(), currHandler);

			AMQPAttach attach = new AMQPAttach();
			attach.setChannel(channel);
			attach.setName(topic.getName().toString());
			attach.setHandle(currHandler);
			attach.setRole(RoleCode.RECEIVER);
			attach.setSndSettleMode(SendCode.MIXED);
			AMQPTarget target = new AMQPTarget();
			target.setAddress(topic.getName().toString());
			target.setDurable(TerminusDurability.NONE);
			target.setTimeout(0L);
			target.setDynamic(false);
			attach.setTarget(target);
			client.send(attach);
		}
	}

	public void unsubscribe(String[] topics)
	{
		for (String topic : topics)
		{
			Long incomingHandle = usedIncomingMappings.remove(topic);
			if (incomingHandle != null)
			{
				AMQPDetach detach = new AMQPDetach();
				detach.setChannel(channel);
				detach.setClosed(true);
				detach.setHandle(incomingHandle);
				client.send(detach);
			}
		}
	}

	public void publish(Topic topic, byte[] content, Boolean retain, Boolean dup)
	{
		AMQPTransfer transfer = new AMQPTransfer();
		transfer.setChannel(channel);
		if (topic.getQos() == QoS.AT_MOST_ONCE)
			transfer.setSettled(true);
		else
			transfer.setSettled(false);

		transfer.setMore(false);
		transfer.setMessageFormat(new AMQPMessageFormat(0));

		MessageHeader messageHeader = new MessageHeader();
		messageHeader.setDurable(true);
		messageHeader.setPriority((short) 3);
		messageHeader.setMilliseconds(1000L);

		AMQPData data = new AMQPData();
		data.setValue(content);

		ConcurrentHashMap<SectionCode, AMQPSection> sections = new ConcurrentHashMap<>();
		sections.put(SectionCode.DATA, data);
		transfer.setSections(sections);

		if (usedOutgoingMappings.containsKey(topic.getName().toString()))
		{
			Long handle = usedOutgoingMappings.get(topic.getName().toString());
			transfer.setHandle(handle);
			timers.store(transfer);
			if (transfer.getSettled())
				timers.remove(transfer.getDeliveryId().intValue());

			client.send(transfer);
		}
		else
		{
			Long currentHandler = nextHandle.incrementAndGet();
			usedOutgoingMappings.put(topic.getName().toString(), currentHandler);
			usedOutgoingHandles.put(currentHandler, topic.getName().toString());

			transfer.setHandle(currentHandler);
			pendingMessages.add(transfer);

			AMQPAttach attach = new AMQPAttach();
			attach.setChannel(channel);
			attach.setName(topic.getName().toString());
			attach.setHandle(currentHandler);
			attach.setRole(RoleCode.SENDER);
			attach.setRcvSettleMode(ReceiveCode.FIRST);
			attach.setInitialDeliveryCount(0L);
			AMQPSource source = new AMQPSource();
			source.setAddress(topic.getName().toString());
			source.setDurable(TerminusDurability.NONE);
			source.setTimeout(0L);
			source.setDynamic(false);
			attach.setSource(source);
			client.send(attach);
		}
	}

	public void reinit()
	{
		setState(ConnectionState.CHANNEL_CREATING);

		if (client != null)
			client.shutdown();

		client = new TCPClient(address, WORKER_THREADS);
	}

	public void closeConnection()
	{
		if (timers != null)
			timers.stopAllTimers();

		if (client != null)
		{
			TCPClient currClient = client;
			client = null;
			currClient.shutdown();
		}
	}

	public void packetReceived(AMQPHeader message)
	{
		logger.info("incoming: " + message);
		try
		{
			switch (message.getCode())
			{
			case ATTACH:
				AMQPAttach attach = (AMQPAttach) message;
				processAttach(attach.getName(), attach.getRole(), attach.getHandle());
				break;
			case BEGIN:
				processBegin();
				break;
			case CHALLENGE:
				processSASLChallenge(((SASLChallenge) message).getChallenge());
				break;
			case CLOSE:
				processClose();
				break;
			case DETACH:
				AMQPDetach detach = (AMQPDetach) message;
				processDetach(detach.getChannel(), detach.getHandle());
				break;
			case DISPOSITION:
				AMQPDisposition disposition = (AMQPDisposition) message;
				processDisposition(disposition.getFirst(), disposition.getLast());
				break;
			case END:
				processEnd(((AMQPEnd) message).getChannel());
				break;
			case FLOW:
				processFlow(((AMQPFlow) message).getChannel());
				break;
			case INIT:
				break;
			case MECHANISMS:
				SASLMechanisms mechanisms = (SASLMechanisms) message;
				processSASLMechanism(mechanisms.getMechanisms(), mechanisms.getChannel(), mechanisms.getType());
				break;
			case OPEN:
				processOpen(((AMQPOpen) message).getIdleTimeout());
				break;
			case OUTCOME:
				SASLOutcome outcome = ((SASLOutcome) message);
				processSASLOutcome(outcome.getAdditionalData(), outcome.getOutcomeCode());
				break;
			case PING:
				processPing();
				break;
			case PROTO:
				AMQPProtoHeader header = (AMQPProtoHeader) message;
				processProto(header.getChannel(), header.getProtocolId());
				break;
			case RESPONSE:
				processSASLResponse(((SASLResponse) message).getResponse());
				break;
			case TRANSFER:
				AMQPTransfer transfer = (AMQPTransfer) message;
				processTransfer(((AMQPData) transfer.getData()), transfer.getHandle(), transfer.getRcvSettleMode(), transfer.getDeliveryId());
				break;
			default:
				break;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			client.shutdown();
		}
	}

	public void cancelConnection()
	{
		client.shutdown();
	}

	public void connectionLost()
	{
		if (account.isCleanSession())
			clearAccountTopics();

		if (timers != null)
			timers.stopAllTimers();

		if (client != null)
		{
			client.shutdown();
			if (getConnectionState() != ConnectionState.NONE)
				setState(ConnectionState.CONNECTION_LOST);
		}
	}

	private void clearAccountTopics()
	{
		try
		{
			dbInterface.deleteAllTopics(account);
		}
		catch (SQLException e)
		{
			logger.error("error deleting topics " + e.getMessage(), e);
		}
	}

	public void connected()
	{
		setState(ConnectionState.CHANNEL_ESTABLISHED);
	}

	public void connectFailed()
	{
		setState(ConnectionState.CHANNEL_FAILED);
	}

	public void processProto(Integer channel, Integer protocolId)
	{
		if (isSaslConfirm && protocolId == 0)
		{
			this.channel = channel;
			AMQPOpen open = AMQPOpen.builder().channel(channel).containerId(account.getClientId()).idleTimeout(account.getKeepAlive() * 1000).build();
			client.send(open);
		}
	}

	public void processOpen(Long idleTimeout)
	{
		if (idleTimeout != null)
			this.idleTimeout = idleTimeout;

		timers.startPingTimer();
		timers.cancelConnectTimer();

		AMQPBegin begin = AMQPBegin.builder().channel(channel).nextOutgoingId(0L).incomingWindow(2147483647L).outgoingWindow(0L).build();
		client.send(begin);
	}

	public void processBegin()
	{
		setState(ConnectionState.CONNECTION_ESTABLISHED);

		if (account.isCleanSession())
			clearAccountTopics();
		else
		{
			try
			{
				List<DBTopic> dbTopics = dbInterface.getTopics(account);
				for (DBTopic dbTopic : dbTopics)
				{
					subscribe(new Topic[]
					{ new Topic(new Text(dbTopic.getName()), QoS.valueOf((int) dbTopic.getQos())) });
				}
			}
			catch (SQLException ex)
			{
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	public void processAttach(String name, RoleCode role, Long handle)
	{
		if (role != null)
		{
			// its opposite here
			if (role == RoleCode.RECEIVER)
			{
				Long realHandle = usedOutgoingMappings.get(name);

				// publish
				if (realHandle != null)
				{
					for (int i = 0; i < pendingMessages.size(); i++)
					{
						AMQPTransfer currMessage = pendingMessages.get(i);
						if (currMessage.getHandle().equals(realHandle))
						{
							pendingMessages.remove(i);
							i--;

							timers.store(currMessage);
							if (currMessage.getSettled())
								timers.remove(currMessage.getDeliveryId().intValue());

							client.send(currMessage);
						}
					}
				}
			}
			else
			{
				usedIncomingMappings.put(name, handle);
				usedIncomingHandles.put(handle, name);

				Long currHandle = pendingSubscribes.remove(name);
				if (currHandle != null)
				{
					byte qos = (byte) QoS.AT_LEAST_ONCE.getValue();
					try
					{
						DBTopic topic = dbInterface.getTopicByName(name, account);
						if (topic != null)
						{
							topic.setQos(qos);
							dbInterface.updateTopic(topic);
						}
						else
						{
							topic = new DBTopic(account, name, qos);
							dbInterface.createTopic(topic);
						}

						if (topicListener != null)
							topicListener.finishAddingTopic(topic.getName(), topic.getQos());
					}
					catch (Exception ex)
					{
						logger.error("An error occured while saving topic," + ex.getMessage(), ex);
					}
				}
			}
		}
	}

	public void processFlow(Integer channel)
	{
		// not implemented for now
	}

	public void processTransfer(AMQPData data, Long handle, ReceiveCode receiveCode, Long deliveryId)
	{
		QoS qos = QoS.AT_LEAST_ONCE;
		if (receiveCode == ReceiveCode.FIRST)
			qos = QoS.AT_MOST_ONCE;
		else
		{
			AMQPDisposition disposition = new AMQPDisposition();
			disposition.setChannel(channel);
			disposition.setRole(RoleCode.RECEIVER);
			disposition.setFirst(deliveryId);
			disposition.setLast(deliveryId);
			disposition.setSettled(true);
			disposition.setState(new AMQPAccepted());
			client.send(disposition);
		}

		if (handle == null || !usedIncomingHandles.containsKey(handle))
			return;

		String topicName = usedIncomingHandles.get(handle);
		Message message = new Message(account, topicName, new String(data.getData()), true, (byte) qos.getValue(), false, false);
		try
		{
			logger.info("storing publish to DB");
			dbInterface.saveMessage(message);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		if (listener != null)
			listener.messageReceived(message);
	}

	public void processDisposition(Long first, Long last)
	{
		if (first != null)
		{
			if (last != null)
			{
				for (Long i = first; i < last; i++)
					timers.remove(i.intValue());
			}
			else
				timers.remove(first.intValue());
		}
	}

	public void processDetach(Integer channel, Long handle)
	{
		String topicName = usedIncomingHandles.remove(handle);
		if (topicName == null)
		{
			logger.warn("received unrecognized detach handle=" + handle);
			return;
		}

		try
		{
			logger.info("deleting  topic " + topicName + " from DB");
			DBTopic dbTopic = dbInterface.getTopicByName(topicName, account);
			dbInterface.deleteTopic(String.valueOf(dbTopic.getId()));
			if (topicListener != null)
				topicListener.finishDeletingTopic(dbTopic.getName());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void processEnd(Integer channel)
	{
		AMQPClose close = new AMQPClose();
		close.setChannel(channel);
		client.send(close);
	}

	public void processClose()
	{
		if (client.isConnected())
			client.close();
		setState(ConnectionState.NONE);
		return;
	}

	public void processSASLInit(String mechanism, byte[] initialResponse, String hostName) throws CoreLogicException
	{
		throw new CoreLogicException("received invalid message init");
	}

	public void processSASLChallenge(byte[] challenge) throws CoreLogicException
	{
		throw new CoreLogicException("received invalid message challenge");
	}

	public void processSASLMechanism(List<AMQPSymbol> mechanisms, Integer channel, Integer headerType)
	{
		AMQPSymbol plainMechanism = null;
		for (AMQPSymbol mechanism : mechanisms)
		{
			if (mechanism.getValue().toLowerCase().equals("plain"))
			{
				plainMechanism = mechanism;
				break;
			}
		}

		// currently supporting only plain
		if (plainMechanism == null)
		{
			timers.stopAllTimers();
			client.shutdown();
			setState(ConnectionState.CONNECTION_FAILED);
			return;
		}

		SASLInit saslInit = new SASLInit();
		saslInit.setType(headerType);
		saslInit.setChannel(channel);
		saslInit.setMechanism(plainMechanism.getValue());

		byte[] userBytes = account.getUsername().getBytes();
		byte[] passwordBytes = account.getPassword().getBytes();
		byte[] challenge = new byte[userBytes.length + 1 + userBytes.length + 1 + passwordBytes.length];
		System.arraycopy(userBytes, 0, challenge, 0, userBytes.length);
		challenge[userBytes.length] = 0x00;
		System.arraycopy(userBytes, 0, challenge, userBytes.length + 1, userBytes.length);
		challenge[userBytes.length + 1 + userBytes.length] = 0x00;
		System.arraycopy(passwordBytes, 0, challenge, userBytes.length + 1 + userBytes.length + 1, passwordBytes.length);

		saslInit.setInitialResponse(challenge);
		client.send(saslInit);
	}

	public void processSASLOutcome(byte[] additionalData, OutcomeCode outcomeCode)
	{
		if (outcomeCode != null)
			if (outcomeCode == OutcomeCode.OK)
			{
				isSaslConfirm = true;
				AMQPProtoHeader header = new AMQPProtoHeader(0);
				client.send(header);
			}
	}

	public void processSASLResponse(byte[] response) throws CoreLogicException
	{
		throw new CoreLogicException("received invalid message response");
	}

	public void processPing()
	{
		// nothing to be done here
	}
}
