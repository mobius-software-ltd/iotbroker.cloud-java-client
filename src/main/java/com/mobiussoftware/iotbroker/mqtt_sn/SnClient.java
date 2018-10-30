package com.mobiussoftware.iotbroker.mqtt_sn;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

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
import com.mobius.software.mqtt.parser.avps.QoS;
import com.mobius.software.mqtt.parser.avps.Topic;
import com.mobius.software.mqttsn.parser.avps.*;
import com.mobius.software.mqttsn.parser.packet.api.SNDevice;
import com.mobius.software.mqttsn.parser.packet.api.SNMessage;
import com.mobius.software.mqttsn.parser.packet.impl.*;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.mqtt_sn.net.SnDtlsClient;
import com.mobiussoftware.iotbroker.mqtt_sn.net.UDPClient;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;
import com.mobiussoftware.iotbroker.network.MessageResendTimer;
import com.mobiussoftware.iotbroker.network.NetworkClient;
import com.mobiussoftware.iotbroker.network.TopicListener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SnClient implements ConnectionListener<SNMessage>, SNDevice, NetworkClient
{
	public static String MESSAGETYPE_PARAM = "MESSAGETYPE";
	private final Logger logger = Logger.getLogger(getClass());
	private int RESEND_PERIOND = 3000;
	private int WORKER_THREADS = 4;
	private InetSocketAddress address;
	private ConnectionState connectionState;

	private TimersMap timers;
	private UDPClient client;

	private Account account;

	private ClientListener clientListener;
	private TopicListener topicListener;
	private DBInterface dbInterface;

	private ConcurrentHashMap<Integer, String> mappedTopics = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Integer> reverseMappedTopics = new ConcurrentHashMap<>();
	private List<SNPublish> pendingMessages = new ArrayList<>();

	public SnClient(Account account) throws Exception
	{
		this.dbInterface = DBHelper.getInstance();
		this.account = account;
		this.address = new InetSocketAddress(account.getServerHost(), account.getServerPort());
		if (!account.isSecure())
			this.client = new UDPClient(address, WORKER_THREADS);
		else
			this.client = new SnDtlsClient(address, WORKER_THREADS, account.getCertificatePath(), account.getCertificatePassword());
	}

	@Override
	public void setClientListener(ClientListener clientListener)
	{
		this.clientListener = clientListener;
	}

	@Override
	public void setTopicListener(TopicListener topicListener)
	{
		this.topicListener = topicListener;
	}

	@Override
	public void setState(ConnectionState state)
	{
		connectionState = state;
		if (clientListener != null)
			clientListener.stateChanged(state);
	}

	@Override
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

	public InetSocketAddress getInetSocketAddress()
	{
		return address;
	}

	@Override
	public void closeChannel()
	{
		if (client != null)
			client.shutdown();
	}

	@Override
	public void connect()
	{
		setState(ConnectionState.CONNECTING);
		Boolean willPresent = false;

		if (account.getWillTopic() != null && account.getWillTopic().length() > 0)
			willPresent = true;

		SNConnect connect = new SNConnect(account.isCleanSession(), account.getKeepAlive(), account.getClientId(), willPresent);

		if (timers != null)
			timers.stopAllTimers();

		timers = new TimersMap(client, RESEND_PERIOND, account.getKeepAlive() * 1000);
		timers.storeConnectTimer(connect);

		if (client.isConnected())
			client.send(connect);
	}

	@Override
	public void disconnect()
	{
		if (client.isConnected())
		{
			client.send(new SNDisconnect());
			client.close();
		}

		setState(ConnectionState.NONE);
	}

	@Override
	public void subscribe(Topic[] topics)
	{
		for (Topic topic1 : topics)
		{
			SNQoS realQos = SNQoS.AT_LEAST_ONCE;
			switch (topic1.getQos())
			{
			case AT_LEAST_ONCE:
				realQos = SNQoS.AT_LEAST_ONCE;
				break;
			case AT_MOST_ONCE:
				realQos = SNQoS.AT_MOST_ONCE;
				break;
			case EXACTLY_ONCE:
				realQos = SNQoS.EXACTLY_ONCE;
				break;
			}

			SNTopic topic;
			if (reverseMappedTopics.containsKey(topic1.getName().toString()))
				topic = new IdentifierTopic(reverseMappedTopics.get(topic1.getName().toString()), realQos);
			else
				topic = new FullTopic(topic1.getName().toString(), realQos);

			SNSubscribe subscribe = new SNSubscribe(0, topic, false);
			timers.store(subscribe);
			client.send(subscribe);
		}
	}

	@Override
	public void unsubscribe(String[] topics)
	{
		for (String topic1 : topics)
		{
			SNQoS realQos = SNQoS.AT_LEAST_ONCE;
			SNTopic topic;
			if (reverseMappedTopics.containsKey(topic1))
				topic = new IdentifierTopic(reverseMappedTopics.get(topic1), realQos);
			else
				topic = new FullTopic(topic1, realQos);

			SNUnsubscribe subscribe = new SNUnsubscribe(0, topic);
			timers.store(subscribe);
			client.send(subscribe);
		}
	}

	@Override
	public void publish(Topic topic, byte[] content, Boolean retain, Boolean dup)
	{
		SNQoS realQos = SNQoS.AT_LEAST_ONCE;
		switch (topic.getQos())
		{
		case AT_LEAST_ONCE:
			realQos = SNQoS.AT_LEAST_ONCE;
			break;
		case AT_MOST_ONCE:
			realQos = SNQoS.AT_MOST_ONCE;
			break;
		case EXACTLY_ONCE:
			realQos = SNQoS.EXACTLY_ONCE;
			break;
		}

		String topicName = topic.getName().toString();
		if (reverseMappedTopics.containsKey(topic.getName().toString()))
		{
			IdentifierTopic idTopic = new IdentifierTopic(reverseMappedTopics.get(topic.getName().toString()), realQos);
			SNPublish publish = new SNPublish(0, idTopic, Unpooled.wrappedBuffer(content), dup, retain);
			if (topic.getQos() != QoS.AT_MOST_ONCE)
				timers.store(publish);

			client.send(publish);
		}
		else
		{
			FullTopic fullTopic = new FullTopic(topicName, realQos);
			pendingMessages.add(new SNPublish(0, fullTopic, Unpooled.wrappedBuffer(content), dup, retain));
			Register register = new Register(0, 0, topicName);
			timers.store(register);
			client.send(register);
		}
	}

	public void reinit()
	{
		setState(ConnectionState.CHANNEL_CREATING);
		if (client != null)
			client.shutdown();

		client = new UDPClient(address, WORKER_THREADS);
	}

	@Override
	public void closeConnection()
	{
		if (timers != null)
			timers.stopAllTimers();

		if (client != null)
		{
			UDPClient currClient = client;
			client = null;
			currClient.shutdown();
		}
	}

	@Override
	public void packetReceived(SNMessage message)
	{
		try
		{
			message.processBy(this);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			client.shutdown();
		}
	}

	@Override
	public void cancelConnection()
	{
		client.shutdown();
	}

	@Override
	public void connectionLost()
	{
		if (timers != null)
			timers.stopAllTimers();

		if (client != null)
		{
			client.shutdown();
			setState(ConnectionState.CONNECTION_LOST);
		}
	}

	@Override
	public void processWillTopicRequest()
	{
		Boolean retain = false;
		SNQoS topicQos = SNQoS.EXACTLY_ONCE;

		if (account.getWillTopic() != null)
		{
			retain = account.isRetain();
			if (account.getWillTopic() == null)
				return;

			switch (SNQoS.valueOf(account.getQos()))
			{
			case AT_LEAST_ONCE:
				topicQos = SNQoS.AT_LEAST_ONCE;
				break;
			case AT_MOST_ONCE:
				topicQos = SNQoS.AT_MOST_ONCE;
				break;
			default:
				break;
			}
		}

		FullTopic topic = new FullTopic(account.getWillTopic(), topicQos);
		WillTopic willTopic = new WillTopic(retain, topic);
		client.send(willTopic);
	}

	@Override
	public void processWillMessageRequest()
	{
		byte[] content;
		if (account.getWill() != null)
		{
			content = account.getWill().getBytes();

			WillMsg willMessage = new WillMsg(Unpooled.wrappedBuffer(content));
			client.send(willMessage);
		}
	}

	@Override
	public void processConnack(ReturnCode code)
	{
		// CANCEL CONNECT TIMER
		MessageResendTimer<SNMessage> timer = timers.getConnectTimer();
		timers.cancelConnectTimer();

		// CHECK CODE , IF OK THEN MOVE TO CONNECTED AND NOTIFY NETWORK SESSION
		if (code == ReturnCode.ACCEPTED)
		{
			setState(ConnectionState.CONNECTION_ESTABLISHED);

			if (timer != null)
			{
				SNConnect connect = (SNConnect) timer.getMessage();
				if (connect.getDuration() > 0)
					timers.startPingTimer();
			}
		}
		else
		{
			timers.stopAllTimers();
			client.shutdown();
			setState(ConnectionState.CONNECTION_FAILED);
		}
	}

	@Override
	public void processSuback(int packetID, int topicID, ReturnCode returnCode, SNQoS allowedQos)
	{

		SNMessage message = timers.remove(packetID);
		if (returnCode != ReturnCode.ACCEPTED)
		{
			logger.warn("received suback failure");
			if (topicListener != null)
				topicListener.finishAddingTopicFailed();
		}
		else
		{
			SNSubscribe subscribe = (SNSubscribe) message;
			String topicName;

			if (subscribe.getTopic() instanceof IdentifierTopic)
			{
				topicName = mappedTopics.get(((IdentifierTopic) subscribe.getTopic()).getValue());
			}
			else
			{
				topicName = ((FullTopic) subscribe.getTopic()).getValue();
				mappedTopics.put(topicID, topicName);
				reverseMappedTopics.put(topicName, topicID);
			}

			//			SNQoS actualQos = allowedQos;
			QoS realQos = QoS.AT_LEAST_ONCE;

			switch (allowedQos)
			{
			case AT_MOST_ONCE:
				realQos = QoS.AT_MOST_ONCE;
				break;
			case EXACTLY_ONCE:
				realQos = QoS.EXACTLY_ONCE;
				break;
			case LEVEL_ONE:
				realQos = QoS.AT_MOST_ONCE;
				break;
			case AT_LEAST_ONCE:
				realQos = QoS.AT_LEAST_ONCE;
				break;
			}

			com.mobiussoftware.iotbroker.db.Topic topic = new com.mobiussoftware.iotbroker.db.Topic(account, topicName, (byte) realQos.getValue());

			if (!account.isCleanSession())
			{
				try
				{
					logger.info("storing topic " + topicName + " to DB");
					dbInterface.saveTopic(topic);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

			if (topicListener != null)
				topicListener.finishAddingTopic(String.valueOf(topic.getId()), topic.getName().toString(), topic.getQos());
		}
	}

	@Override
	public void processUnsuback(int packetID)
	{
		SNMessage message = timers.remove(packetID);
		if (message != null)
		{
			SNUnsubscribe unsubscribe = (SNUnsubscribe) message;
			String topicName;
			if (unsubscribe.getTopic() instanceof IdentifierTopic)
				topicName = mappedTopics.get(((IdentifierTopic) unsubscribe.getTopic()).getValue());
			else
				topicName = ((FullTopic) unsubscribe.getTopic()).getValue();

			if (!account.isCleanSession())
			{
				try
				{
					logger.info("deleting  topic" + topicName + " from DB");
					List<com.mobiussoftware.iotbroker.db.Topic> topics = dbInterface.getTopics(account);
					for (com.mobiussoftware.iotbroker.db.Topic topic : topics)
					{
						if (topic.getName().equals(topicName))
						{
							dbInterface.deleteTopic(String.valueOf(topic.getId()));
							if (topicListener != null)
								topicListener.finishDeletingTopic(String.valueOf(topic.getId()));
						}
					}
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void processRegister(int packetID, int topicID, String topicName)
	{
		mappedTopics.put(topicID, topicName);

		reverseMappedTopics.put(topicName, topicID);

		SNMessage message = new Regack(topicID, packetID, ReturnCode.ACCEPTED);
		client.send(message);
	}

	@Override
	public void processRegack(int packetID, int topicID, ReturnCode returnCode)
	{
		SNMessage message = timers.remove(packetID);
		if (message != null)
		{
			Register register = (Register) message;
			if (returnCode == ReturnCode.ACCEPTED)
			{
				mappedTopics.put(topicID, register.getTopicName());
				reverseMappedTopics.put(register.getTopicName(), topicID);
			}

			for (int i = 0; i < pendingMessages.size(); i++)
			{
				SNPublish currMessage = pendingMessages.get(i);
				if (register.getTopicName().equals((((FullTopic) currMessage.getTopic()).getValue())))
				{
					pendingMessages.remove(i--);
					currMessage.setTopic(new IdentifierTopic(topicID, (currMessage.getTopic()).getQos()));
					timers.store(currMessage);
					client.send(currMessage);
				}
			}
		}
	}

	@Override
	public void processPublish(int packetID, SNTopic topic, ByteBuf content, Boolean retain, Boolean isDup)
	{
		SNQoS publisherQos = topic.getQos();
		QoS realQos = QoS.AT_MOST_ONCE;

		switch (publisherQos)
		{
		case AT_LEAST_ONCE:
			SNPuback puback = new SNPuback();
			puback.setMessageID(packetID);

			int topicID = 0;
			if (topic instanceof IdentifierTopic)
				topicID = ((IdentifierTopic) topic).getValue();
			else if (topic instanceof ShortTopic)
				topicID = Integer.valueOf(((ShortTopic) topic).getValue());

			puback.setTopicID(topicID);
			puback.setCode(ReturnCode.ACCEPTED);
			client.send(puback);
			realQos = QoS.AT_LEAST_ONCE;
			break;
		case EXACTLY_ONCE:
			realQos = QoS.EXACTLY_ONCE;
			SNPubrec pubrec = new SNPubrec(packetID);
			client.send(pubrec);
			break;
		default:
			break;
		}

		String topicName = null;
		if (topic instanceof IdentifierTopic && mappedTopics.containsKey(((IdentifierTopic) topic).getValue()))
			topicName = mappedTopics.get(((IdentifierTopic) topic).getValue());

		if (topicName == null)
			return;

		if (!(isDup && publisherQos.equals(SNQoS.EXACTLY_ONCE)))
		{
			byte[] bytes = new byte[content.readableBytes()];
			content.readBytes(bytes);
			Message message = new Message(account, topicName, new String(bytes), true, (byte) realQos.getValue(), retain, isDup);

			if (!account.isCleanSession())
			{
				try
				{
					logger.info("storing publish to DB");
					dbInterface.saveMessage(message);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}

			if (clientListener != null)
			{
				logger.info("notifying clientListener on publish received");
				clientListener.messageReceived(message);
			}
		}
	}

	@Override
	public void processPuback(int packetID)
	{
		timers.remove(packetID);
	}

	@Override
	public void processPubrec(int packetID)
	{
		timers.remove(packetID);
		SNMessage message = new SNPubrel(packetID);
		timers.store(message);
		client.send(message);
	}

	@Override
	public void processPubrel(int packetID)
	{
		client.send(new SNPubcomp(packetID));
	}

	@Override
	public void processPubcomp(int packetID)
	{
		timers.remove(packetID);
	}

	@Override
	public void processPingresp()
	{
	}

	@Override
	public void processSubscribe(int packetID, SNTopic topic)
	{
		logger.error("received invalid message subscribe");
	}

	@Override
	public void processConnect(boolean cleanSession, int keepalive)
	{
		logger.error("received invalid message connect");
	}

	@Override
	public void processPingreq()
	{
		logger.error("received invalid message pingreq");
	}

	@Override
	public void processDisconnect()
	{
		closeConnection();
		setState(ConnectionState.CONNECTION_LOST);
	}

	@Override
	public void processUnsubscribe(int packetID, SNTopic topic)
	{
		logger.error("received invalid message unsubscribe");
	}

	@Override
	public void processWillTopicUpdate(FullTopic willTopic)
	{
		logger.warn("received invalid message will topic update");
	}

	@Override
	public void processWillMessageUpdate(ByteBuf content)
	{
		logger.warn("received invalid message will message update");
	}

	@Override
	public void processWillTopic(FullTopic topic)
	{
		logger.warn("received invalid message will topic");
	}

	@Override
	public void processWillMessage(ByteBuf content)
	{
		logger.warn("received invalid message will message");
	}

	@Override
	public void processAdvertise(int gatewayID, int duration)
	{
		logger.warn("received invalid message advertise");
	}

	@Override
	public void processGwInfo(int gatewayID, String gatewayAddress)
	{
		logger.warn("received invalid message gw info");
	}

	@Override
	public void processSearchGw(Radius radius)
	{
		logger.warn("received invalid message search gw");
	}

	@Override
	public void processWillTopicResponse()
	{
		logger.warn("received invalid message will topic response");
	}

	@Override
	public void processWillMessageResponse()
	{
		logger.warn("received invalid message will message response");
	}

	@Override
	public void connected()
	{
		setState(ConnectionState.CHANNEL_ESTABLISHED);
	}

	@Override
	public void connectFailed()
	{
		setState(ConnectionState.CHANNEL_FAILED);
	}
}