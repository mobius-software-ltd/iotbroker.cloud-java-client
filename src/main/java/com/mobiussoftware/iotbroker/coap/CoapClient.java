package com.mobiussoftware.iotbroker.coap;

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

import org.apache.log4j.Logger;

import com.mobius.software.coap.parser.message.options.CoapOption;
import com.mobius.software.coap.parser.message.options.CoapOptionType;
import com.mobius.software.coap.parser.tlv.CoapCode;
import com.mobius.software.coap.parser.tlv.CoapMessage;
import com.mobius.software.coap.parser.tlv.CoapType;
import com.mobius.software.mqtt.parser.avps.Topic;
import com.mobiussoftware.iotbroker.coap.net.CoapDtlsClient;
import com.mobiussoftware.iotbroker.coap.net.UDPClient;
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

public class CoapClient implements ConnectionListener<CoapMessage>, NetworkClient
{
	public static Integer VERSION = 1;
	private Integer WORKER_THREADS = 4;
	private Long RESEND_PERIOND = 3000L;
	private final Logger logger = Logger.getLogger(getClass());

	private InetSocketAddress address;
	private ConnectionState connectionState;

	private TimersMap timers;
	private UDPClient client;

	private Account account;

	private ClientListener listener;
	private TopicListener topicListener;

	private DBInterface dbInterface;

	public CoapClient(Account account) throws Exception
	{

		this.dbInterface = DBHelper.getInstance();
		this.account = account;
		this.address = new InetSocketAddress(account.getServerHost(), account.getServerPort());
		if (!account.isSecure())
			this.client = new UDPClient(address, WORKER_THREADS);
		else
			this.client = new CoapDtlsClient(address, WORKER_THREADS, account.getCertificate(), account.getCertificatePassword());
	}

	@Override
	public void setClientListener(ClientListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void setTopicListener(TopicListener topicListener)
	{
		this.topicListener = topicListener;
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

	public InetSocketAddress getInetSocketAddress()
	{
		return address;
	}

	public void closeChannel()
	{
		if (client != null && client.isConnected())
			client.shutdown();
	}

	public void cancelConnection()
	{
		client.shutdown();
	}

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

	public void connect()
	{
		if (account.isCleanSession())
			clearAccountTopics();

		if (timers != null)
			timers.stopAllTimers();

		timers = new TimersMap(this, client, RESEND_PERIOND, account.getKeepAlive() * 1000L);
		setState(ConnectionState.CONNECTING);
		timers.storeConnectTimer(timers.getPingreqMessage());
	}

	public void disconnect()
	{
		if (client.isConnected())
			client.close();

		setState(ConnectionState.NONE);
		return;
	}

	public void publish(Topic topic, byte[] content, Boolean retain, Boolean dup)
	{
		byte[] nameBytes = topic.getName().toString().getBytes();
		byte[] nodeIdBytes = account.getClientId().getBytes();

		byte[] qosValue = new byte[2];
		qosValue[0] = 0x00;
		switch (topic.getQos())
		{
		case AT_LEAST_ONCE:
			qosValue[1] = 0x00;
			break;
		case AT_MOST_ONCE:
			qosValue[1] = 0x01;
			break;
		case EXACTLY_ONCE:
			qosValue[1] = 0x02;
			break;
		}

		CoapMessage coapMessage = CoapMessage.builder().version(VERSION).type(CoapType.CONFIRMABLE).code(CoapCode.PUT).messageID(0).payload(content).option(new CoapOption(CoapOptionType.URI_PATH.getValue(), nameBytes.length, nameBytes)).option(new CoapOption((int) CoapOptionType.NODE_ID.getValue(), nodeIdBytes.length, nodeIdBytes)).option(new CoapOption(CoapOptionType.ACCEPT.getValue(), 2, qosValue)).build();
		timers.store(coapMessage);
		// set message id = token id
		int messageID = Integer.parseInt(new String(coapMessage.getToken()));
		coapMessage.setMessageID(messageID);
		client.send(coapMessage);
	}

	public void subscribe(Topic[] topics)
	{
		for (int i = 0; i < topics.length; i++)
		{
			byte[] nameBytes = topics[i].getName().toString().getBytes();
			byte[] qosValue = new byte[2];
			qosValue[0] = 0x00;

			byte[] nodeIdBytes = account.getClientId().getBytes();
			switch (topics[i].getQos())
			{
			case AT_MOST_ONCE:
				qosValue[1] = 0x00;
				break;
			case AT_LEAST_ONCE:
				qosValue[1] = 0x01;
				break;
			case EXACTLY_ONCE:
				qosValue[1] = 0x02;
				break;
			}

			CoapMessage coapMessage = CoapMessage.builder().version(VERSION).type(CoapType.CONFIRMABLE).code(CoapCode.GET).messageID(0).payload(new byte[0]).option(new CoapOption(CoapOptionType.OBSERVE.getValue(), 4, new byte[]
			{ 0x00, 0x00, 0x00, 0x00 })).option(new CoapOption(CoapOptionType.URI_PATH.getValue(), nameBytes.length, nameBytes)).option(new CoapOption(CoapOptionType.ACCEPT.getValue(), 2, qosValue)).option(new CoapOption(CoapOptionType.NODE_ID.getValue(), nodeIdBytes.length, nodeIdBytes)).build();
			timers.store(coapMessage);
			// set message id = token id
			int messageID = Integer.parseInt(new String(coapMessage.getToken()));
			coapMessage.setMessageID(messageID);
			client.send(coapMessage);
		}
	}

	public void unsubscribe(String[] topics)
	{
		byte[] nodeIdBytes = account.getClientId().getBytes();
		CoapMessage.Builder builder = CoapMessage.builder().version(VERSION).type(CoapType.CONFIRMABLE).code(CoapCode.GET).messageID(0).payload(new byte[0]).option(new CoapOption(CoapOptionType.OBSERVE.getValue(), 4, new byte[]
		{ 0x00, 0x00, 0x00, 0x01 })).option(new CoapOption(CoapOptionType.NODE_ID.getValue(), nodeIdBytes.length, nodeIdBytes));
		for (int i = 0; i < topics.length; i++)
		{
			byte[] nameBytes = topics[i].getBytes();
			builder = builder.option(new CoapOption(CoapOptionType.URI_PATH.getValue(), nameBytes.length, nameBytes));
		}

		CoapMessage coapMessage = builder.build();
		timers.store(coapMessage);
		// set message id = token id
		int messageID = Integer.parseInt(new String(coapMessage.getToken()));
		coapMessage.setMessageID(messageID);
		client.send(coapMessage);
	}

	public void packetReceived(CoapMessage message)
	{
		System.out.println("IN:" + message);

		CoapType type = message.getType();
		if ((message.getCode() == CoapCode.POST || message.getCode() == CoapCode.PUT) && type != CoapType.ACKNOWLEDGEMENT)
		{
			String topic = message.options().fetchUriPath();
			byte qos = message.options().fetchAccept().byteValue();
			byte[] content = message.getPayload();
			if (topic == null)
			{
				byte[] textBytes = "text/plain".getBytes();
				byte[] nodeIdBytes = account.getClientId().getBytes();

				CoapMessage ack = CoapMessage.builder().version(VERSION).type(CoapType.ACKNOWLEDGEMENT).code(CoapCode.BAD_OPTION).messageID(message.getMessageID()).token(message.getToken()).payload(new byte[0]).option(new CoapOption(CoapOptionType.CONTENT_FORMAT.getValue(), textBytes.length, textBytes)).option(new CoapOption(CoapOptionType.NODE_ID.getValue(), nodeIdBytes.length, nodeIdBytes)).build();
				client.send(ack);
				return;
			}

			Message dbMessage = new Message(account, topic, new String(content), true, qos, false, false);
			try
			{
				dbInterface.saveMessage(dbMessage);
			}
			catch (Exception ex)
			{
				logger.error("An error occured while deleting topic," + ex.getMessage(), ex);
			}

			if (listener != null)
				listener.messageReceived(dbMessage);
		}

		switch (type)
		{
		case CONFIRMABLE:
			byte[] nodeIdBytes = account.getClientId().getBytes();
			CoapMessage response = CoapMessage.builder().version(message.getVersion()).type(CoapType.ACKNOWLEDGEMENT).code(message.getCode()).messageID(message.getMessageID()).token(message.getToken()).payload(new byte[0]).option(new CoapOption(CoapOptionType.NODE_ID.getValue(), nodeIdBytes.length, nodeIdBytes)).build();
			client.send(response);
			break;
		case NON_CONFIRMABLE:
			timers.remove(message.getToken());
			break;
		case ACKNOWLEDGEMENT:
			if (message.getCode() == CoapCode.GET)
			{
				Integer observe = message.options().fetchObserve();
				if (observe == 0)
				{
					CoapMessage originalMessage = timers.remove(message.getToken());
					if (originalMessage != null)
					{
						String name = originalMessage.options().fetchUriPath();
						byte qos = originalMessage.options().fetchAccept().byteValue();
						try
						{
							DBTopic topic = dbInterface.getTopicByName(name);
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
				else if (observe == 1)
				{
					CoapMessage originalMessage = timers.remove(message.getToken());
					if (originalMessage != null)
					{
						String name = originalMessage.options().fetchUriPath();
						try
						{
							com.mobiussoftware.iotbroker.db.DBTopic topic = dbInterface.getTopicByName(name);
							if (topic != null)
							{
								dbInterface.deleteTopic(String.valueOf(topic.getId()));
								if (topicListener != null)
									topicListener.finishDeletingTopic(name);
							}
						}
						catch (Exception ex)
						{
							logger.error("An error occured while deleting topic," + ex.getMessage(), ex);
						}
					}
				}
			}
			else if (message.getCode() == CoapCode.PUT && message.getMessageID() == 0)
			{
				if (connectionState == ConnectionState.CONNECTING)
				{
					setState(ConnectionState.CONNECTION_ESTABLISHED);

					timers.cancelConnectTimer();

					if (account.getKeepAlive() > 0)
						timers.startPingTimer();
				}
			}
			else if (message.getToken() != null)
				timers.remove(message.getToken());
			break;
		case RESET:
			timers.remove(message.getToken());
			break;
		}
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
			setState(ConnectionState.CONNECTION_LOST);
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

	public String getClientID()
	{
		return account.getClientId();
	}
}