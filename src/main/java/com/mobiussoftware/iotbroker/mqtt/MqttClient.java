package com.mobiussoftware.iotbroker.mqtt;

import com.mobius.software.mqtt.parser.avps.*;
import com.mobius.software.mqtt.parser.header.api.MQDevice;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.*;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.mqtt.net.TCPClient;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;
import com.mobiussoftware.iotbroker.network.MessageResendTimer;
import com.mobiussoftware.iotbroker.network.NetworkClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

public class MqttClient implements ConnectionListener<MQMessage>,MQDevice,NetworkClient {
	
	private static Logger logger = Logger.getLogger(MqttClient.class);
	public static String MESSAGETYPE_PARAM = "MESSAGETYPE";
	private int RESEND_PERIOND = 3000;
	private int WORKER_THREADS = 4;

	private InetSocketAddress address;
	private ConnectionState connectionState;

	private TimersMap timers;
	private TCPClient client;
	private String username;
	private String password;
	private String clientID;
	private Boolean isClean;
	private int keepalive;
	private Will will;
	private ClientListener listener;
	private DBInterface dbInterface;

	public MqttClient(Account account, ClientListener listener) throws Exception {
		
		System.out.println("MqttClient constructor");
		this.dbInterface = DBHelper.getInstance();
		this.address = new InetSocketAddress(account.getServerHost(), account.getServerPort());
		this.username = account.getUsername();
		this.password = account.getPassword();
		this.clientID = account.getClientId();
		this.isClean = account.isCleanSession();
		this.keepalive = account.getKeepAlive();

		Text topicName = new Text(account.getWillTopic());
		QoS qos = QoS.valueOf(account.getQos());
		Topic topic = new Topic(topicName, qos);
		this.will = new Will(topic, account.getWill().getBytes(), account.isRetain());
		this.listener = listener;
		client = new TCPClient(address, WORKER_THREADS);
	}
	
	@Override
	public void setListener(ClientListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void setState(ConnectionState state) {
		System.out.println("setState listener= " + listener);
		connectionState = state;
		if(listener != null)
			listener.stateChanged(state);
	}
	
	public Boolean createChannel() {
		setState(ConnectionState.CHANNEL_CREATING);
		Boolean isSuccess = client.init(this);
		System.out.println("client.init ok isSuccess=" + isSuccess);
		if (!isSuccess)
			setState(ConnectionState.CHANNEL_FAILED);
		return isSuccess;
	}

	public Boolean isConnected() {
		return connectionState == ConnectionState.CONNECTION_ESTABLISHED;
	}

	public ConnectionState getConnectionState() {
		return connectionState;
	}

	public InetSocketAddress getInetSocketAddress() {
		return address;
	}

	public void CloseChannel() {
		if (client != null)
			client.shutdown();
	}

	public void Connect() {
		
		setState(ConnectionState.CONNECTING);
		Connect connect = new Connect(username, password, clientID, isClean, keepalive, will);
		
		if (timers != null)
			timers.stopAllTimers();

		timers = new TimersMap(this, client, RESEND_PERIOND, keepalive * 1000);
		
		timers.storeConnectTimer(connect);
			
		if (client.isConnected())
			client.send(connect);
	}

	public void Disconnect() {
		if (client.isConnected()) {
			client.send(new Disconnect());
			client.Close();
		}

		setState(ConnectionState.NONE);
		return;
	}

	public void Subscribe(Topic[] topics) {
		Subscribe subscribe = new Subscribe(null, topics);
		timers.store(subscribe);
		client.send(subscribe);
	}

	public void Unsubscribe(String[] topics) {
		Text[] texts = new Text[topics.length];
		for (int i = 0; i < topics.length; i++) {
			texts[i] = new Text(topics[i]);
		}
		Unsubscribe unsubscribe = new Unsubscribe(null, texts);
		timers.store(unsubscribe);
		client.send(unsubscribe);
	}

	public void Publish(Topic topic, byte[] content, Boolean retain, Boolean dup) {
		Publish publish = new Publish(null, topic, Unpooled.wrappedBuffer(content), retain, dup);
		if (topic.getQos() != QoS.AT_MOST_ONCE)
			timers.store(publish);

		client.send(publish);
	}

	public void Reinit() {
		setState(ConnectionState.CHANNEL_CREATING);

		if (client != null)
			client.shutdown();

		client = new TCPClient(address, WORKER_THREADS);
	}

	public void CloseConnection() {
		if (timers != null)
			timers.stopAllTimers();

		if (client != null) {
			TCPClient currClient = client;
			client = null;
			currClient.shutdown();
		}
	}

	public void packetReceived(MQMessage message) {
		try
		{
			message.processBy(this);
		}
		catch (Exception ex)
		{
			client.shutdown();
		}
	}

	@Override
	public void CancelConnection() {
		client.shutdown();
	}

	public void connectionLost() {
		if (isClean)
			clearAccountTopics();

		if (timers != null)
			timers.stopAllTimers();

		if (client != null) {
			client.shutdown();
			setState(ConnectionState.CONNECTION_LOST);
		}
	}

	public void processConnack(ConnackCode code, boolean sessionPresent) {
		// CANCEL CONNECT TIMER
		MessageResendTimer<MQMessage> timer = timers.getConnectTimer();
		timers.stopConnectTimer();

		// CHECK CODE , IF OK THEN MOVE TO CONNECTED AND NOTIFY NETWORK SESSION
		if (code == ConnackCode.ACCEPTED) {
			setState(ConnectionState.CONNECTION_ESTABLISHED);

			if (timer != null) {
				Connect connect = (Connect) timer.getMessage();
				if (connect.isClean())
					clearAccountTopics();
			}

			timers.startPingTimer();
		} else {
			timers.stopAllTimers();
			client.shutdown();
			setState(ConnectionState.CONNECTION_FAILED);
		}
	}

	private void clearAccountTopics() {
		dbInterface.deleteAllTopics();
	}

	public void processSuback(Integer packetID, List<SubackCode> codes) {

		MQMessage message = timers.remove(packetID);
		for (SubackCode code : codes) {
			if (code != SubackCode.FAILURE)
				//throw new CoreLogicException("received invalid message suback");
				logger.error("received invalid message suback");
			else
			{
				Subscribe subscribe = (Subscribe) message;
				Topic topic = subscribe.getTopics()[0];
				QoS expectedQos = topic.getQos();
				QoS actualQos = QoS.valueOf(code.getNum());
				try {
					if (expectedQos == actualQos)
						dbInterface.saveTopic(new com.mobiussoftware.iotbroker.db.Topic(topic.getName().toString(), (byte) expectedQos.getValue()));
					else
						dbInterface.saveTopic(new com.mobiussoftware.iotbroker.db.Topic(topic.getName().toString(), (byte) actualQos.getValue()));
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (listener != null)
					listener.messageReceived(MessageType.SUBACK);
			}
		}
	}

	public void processUnsuback(Integer packetID) {
		MQMessage message = timers.remove(packetID);
		if (message != null) {
			Unsubscribe unsubscribe = (Unsubscribe) message;
			Text[] topics = unsubscribe.getTopics();
			try {
				for (Text topic : topics)
					dbInterface.deleteTopic(topic.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (listener != null)
			listener.messageReceived(MessageType.UNSUBACK);
	}

	public void processPublish(Integer packetID, Topic topic, ByteBuf content, boolean retain, boolean isDup) {
		QoS publisherQos = topic.getQos();
		switch (publisherQos) {
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
		try {
			if (!dbInterface.topicExists(topicName.toString()))
				return;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!(isDup && publisherQos == QoS.EXACTLY_ONCE)) {
			byte[] bytes = new byte[content.readableBytes()];
			content.readBytes(bytes);
			Message message = new Message(topicName.toString(), new String(bytes), false, (byte) publisherQos.getValue(), null, null);
			try {
				dbInterface.saveMessage(message);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (listener != null)
			listener.messageReceived(MessageType.PUBLISH);
	}

	public void processPuback(Integer packetID) {
		timers.remove(packetID);
		if (listener != null)
			listener.messageReceived(MessageType.PUBACK);
	}

	public void processPubrec(Integer packetID) {
		timers.remove(packetID);
		if (listener != null)
			listener.messageReceived(MessageType.PUBREC);
		MQMessage message = new Pubrel(packetID);
		timers.store(message);
		client.send(message);
	}

	public void processPubrel(Integer packetID) {
		client.send(new Pubcomp(packetID));
	}

	public void processPubcomp(Integer packetID) {
		timers.remove(packetID);
		if (listener != null)
			listener.messageReceived(MessageType.PUBCOMP);
	}

	public void processPingresp() {
		//DO NOTHING
	}

	public void processSubscribe(Integer packetID, Topic[] topics) {
		//throw new CoreLogicException("received invalid message subscribe");
		logger.error("received invalid message subscribe");
	}

	public void processConnect(boolean cleanSession, int keepalive, Will will) {
//		throw new CoreLogicException("received invalid message connect");
		logger.error("received invalid message connect");
	}

	@Override
	public void processPingreq() {
//		throw new CoreLogicException("received invalid message pingreq");
		logger.error("received invalid message pingreq");
	}

	@Override
	public void processDisconnect() {
//		throw new CoreLogicException("received invalid message disconnect");
		logger.error("received invalid message disconnect");
	}

	public void processUnsubscribe(Integer packetID, Text[] topics) {
//		throw new CoreLogicException("received invalid message unsubscribe");
		logger.error("received invalid message unsubscribe");
	}

	public void connected() {
			
		// 	public Connect reInit(String username, String password, String clientID, boolean isClean, int keepalive, Will will)

		System.out.println("start--");

		Connect connect = new Connect("user","pass", "342re3r2", true, 12, null);
		this.client.send(connect);
		System.out.println("---end");
		setState(ConnectionState.CHANNEL_ESTABLISHED);
	}

	public void connectFailed() {
		setState(ConnectionState.CHANNEL_FAILED);
	}
}
