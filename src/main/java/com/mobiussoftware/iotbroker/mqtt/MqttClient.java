package com.mobiussoftware.iotbroker.mqtt;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;

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
import com.mobius.software.mqtt.parser.avps.*;
import com.mobius.software.mqtt.parser.header.api.MQDevice;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.*;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.DBTopic;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.mqtt.net.MqttTlsClient;
import com.mobiussoftware.iotbroker.mqtt.net.TCPClient;
import com.mobiussoftware.iotbroker.mqtt.net.WSClient;
import com.mobiussoftware.iotbroker.mqtt.net.WSSClient;
import com.mobiussoftware.iotbroker.network.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MqttClient implements ConnectionListener<MQMessage>, MQDevice, NetworkClient
{

	public static String MESSAGETYPE_PARAM = "MESSAGETYPE";
	private final Logger logger = Logger.getLogger(getClass());
	private int RESEND_PERIOND = 3000;
	private int WORKER_THREADS = 4;

	private InetSocketAddress address;
	private ConnectionState connectionState;

	private TimersMap timers;
	private NetworkChannel<MQMessage> client;

	private Account account;

	private ClientListener clientListener;
	private TopicListener topicListener;
	private DBInterface dbInterface;

	public MqttClient(Account account) throws Exception
	{
		this.dbInterface = DBHelper.getInstance();
		this.account = account;
		this.address = new InetSocketAddress(account.getServerHost(), account.getServerPort());

		switch (account.getProtocol())
		{
		case WEBSOCKETS:
			if (!account.isSecure())
				this.client = new WSClient(address, WORKER_THREADS);
			else
				this.client = new WSSClient(address, WORKER_THREADS, account.getCertificate(), account.getCertificatePassword());
			break;
		default:
			if (!account.isSecure())
				this.client = new TCPClient(address, WORKER_THREADS);
			else
				this.client = new MqttTlsClient(address, WORKER_THREADS, account.getCertificate(), account.getCertificatePassword());
			break;
		}
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
		if (account.isCleanSession())
			clearAccountTopics();
		
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

		Will will = null;
		if (account.getWillTopic() != null && account.getWillTopic().length() > 0)
		{
			Text topicName = new Text(account.getWillTopic());
			QoS qos = QoS.valueOf(account.getQos());
			Topic topic = new Topic(topicName, qos);

			will = new Will(topic, account.getWill().getBytes(), account.isRetain());
		}

		Connect connect = new Connect(account.getUsername(), account.getPassword(), account.getClientId(), account.isCleanSession(), account.getKeepAlive(), will);

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
			client.send(new Disconnect());
			client.close();
		}

		setState(ConnectionState.NONE);
		return;
	}

	@Override
	public void subscribe(Topic[] topics)
	{
		Subscribe subscribe = new Subscribe(null, topics);
		timers.store(subscribe);
		client.send(subscribe);
	}

	@Override
	public void unsubscribe(String[] topics)
	{
		Text[] texts = new Text[topics.length];
		for (int i = 0; i < topics.length; i++)
		{
			texts[i] = new Text(topics[i]);
		}
		Unsubscribe unsubscribe = new Unsubscribe(null, texts);
		timers.store(unsubscribe);
		client.send(unsubscribe);
	}

	@Override
	public void publish(Topic topic, byte[] content, Boolean retain, Boolean dup)
	{
		Publish publish = new Publish(null, topic, Unpooled.wrappedBuffer(content), retain, dup);
		if (topic.getQos() != QoS.AT_MOST_ONCE)
			timers.store(publish);

		client.send(publish);
	}

	public void reinit()
	{
		setState(ConnectionState.CHANNEL_CREATING);
		if (client != null)
			client.shutdown();

		client = new TCPClient(address, WORKER_THREADS);
	}

	@Override
	public void closeConnection()
	{
		if (timers != null)
			timers.stopAllTimers();

		if (client != null)
		{
			NetworkChannel<MQMessage> currClient = client;
			client = null;
			currClient.shutdown();
		}
	}

	@Override
	public void packetReceived(MQMessage message)
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
	public void processConnack(ConnackCode code, boolean sessionPresent)
	{
		// CANCEL CONNECT TIMER
		MessageResendTimer<MQMessage> timer = timers.getConnectTimer();
		timers.cancelConnectTimer();

		// CHECK CODE , IF OK THEN MOVE TO CONNECTED AND NOTIFY NETWORK SESSION
		if (code == ConnackCode.ACCEPTED)
		{
			setState(ConnectionState.CONNECTION_ESTABLISHED);

			if (timer != null)
			{
				Connect connect = (Connect) timer.getMessage();

				if (connect.getKeepalive() > 0)
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
	public void processSuback(Integer packetID, List<SubackCode> codes)
	{

		logger.info("processing incoming suback...");

		MQMessage message = timers.remove(packetID);
		if (message == null || message.getType() != MessageType.SUBSCRIBE)
		{
			logger.warn("received unexpected suback, was expecting " + message.getType());
			return;
		}

		Subscribe subscribe = (Subscribe) message;
		for (int i = 0; i < codes.size(); i++)
		{
			SubackCode code = codes.get(i);
			if (code != SubackCode.FAILURE)
			{
				Topic messageTopic = subscribe.getTopics()[i];
				String name = messageTopic.getName().toString();
				QoS expectedQos = messageTopic.getQos();
				QoS actualQos = QoS.valueOf(code.getNum());
				byte qos = (byte) QoS.calculate(expectedQos, actualQos).getValue();
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
			else
			{
				logger.warn("received suback failure");
				if (topicListener != null)
					topicListener.finishAddingTopicFailed();
			}
		}
	}

	@Override
	public void processUnsuback(Integer packetID)
	{

		logger.info("processing incoming unsuback...");

		MQMessage message = timers.remove(packetID);
		if (message == null || message.getType() != MessageType.UNSUBSCRIBE)
		{
			logger.warn("received unexpected unsuback");
			return;
		}

		Unsubscribe unsubscribe = (Unsubscribe) message;
		Text[] topics = unsubscribe.getTopics();
		try
		{
			for (Text topic : topics)
			{
				List<com.mobiussoftware.iotbroker.db.DBTopic> dbTopics = dbInterface.getTopics(account);
				for (com.mobiussoftware.iotbroker.db.DBTopic dbTopic : dbTopics)
				{
					if (dbTopic.getName().equals(topic.toString()))
					{
						dbInterface.deleteTopic(String.valueOf(dbTopic.getId()));
						if (topicListener != null)
							topicListener.finishDeletingTopic(dbTopic.getName());
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void processPublish(Integer packetID, Topic topic, ByteBuf content, boolean retain, boolean isDup)
	{

		logger.info("processing incoming publish...");

		QoS publisherQos = topic.getQos();
		switch (publisherQos)
		{
		case AT_LEAST_ONCE:
			Puback puback = new Puback(packetID.intValue());
			client.send(puback);
			break;
		case EXACTLY_ONCE:
			Pubrec pubrec = new Pubrec(packetID.intValue());
			client.send(pubrec);
			break;
		default:
			break;
		}

		Text topicName = topic.getName();
		if (!(isDup && publisherQos == QoS.EXACTLY_ONCE))
		{
			byte[] bytes = new byte[content.readableBytes()];
			content.readBytes(bytes);
			Message message = new Message(account, topicName.toString(), new String(bytes), true, (byte) publisherQos.getValue(), retain, isDup);

			try
			{
				logger.info("storing publish to DB");
				dbInterface.saveMessage(message);
			}
			catch (SQLException e)
			{
				logger.error(e.getMessage(), e);
			}

			if (clientListener != null)
			{
				logger.info("notifying clientListener on publish received");
				clientListener.messageReceived(message);
			}
		}
	}

	@Override
	public void processPuback(Integer packetID)
	{
		timers.remove(packetID);
	}

	@Override
	public void processPubrec(Integer packetID)
	{
		timers.remove(packetID);
		Pubrel message = new Pubrel(packetID);
		timers.store(message);
		client.send(message);
	}

	@Override
	public void processPubrel(Integer packetID)
	{
		client.send(new Pubcomp(packetID));
	}

	@Override
	public void processPubcomp(Integer packetID)
	{
		timers.remove(packetID);
	}

	@Override
	public void processPingresp()
	{
	}

	@Override
	public void processSubscribe(Integer packetID, Topic[] topics)
	{
		logger.error("received invalid message subscribe");
	}

	@Override
	public void processConnect(boolean cleanSession, int keepalive, Will will)
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
	public void processUnsubscribe(Integer packetID, Text[] topics)
	{
		logger.error("received invalid message unsubscribe");
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
}
