package com.mobiussoftware.iotbroker.mqtt;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.mobius.software.mqtt.parser.avps.ConnackCode;
import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.parser.avps.QoS;
import com.mobius.software.mqtt.parser.avps.SubackCode;
import com.mobius.software.mqtt.parser.avps.Text;
import com.mobius.software.mqtt.parser.avps.Topic;
import com.mobius.software.mqtt.parser.avps.Will;
import com.mobius.software.mqtt.parser.header.api.MQDevice;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.Connect;
import com.mobius.software.mqtt.parser.header.impl.Disconnect;
import com.mobius.software.mqtt.parser.header.impl.Puback;
import com.mobius.software.mqtt.parser.header.impl.Pubcomp;
import com.mobius.software.mqtt.parser.header.impl.Publish;
import com.mobius.software.mqtt.parser.header.impl.Pubrec;
import com.mobius.software.mqtt.parser.header.impl.Pubrel;
import com.mobius.software.mqtt.parser.header.impl.Subscribe;
import com.mobius.software.mqtt.parser.header.impl.Unsubscribe;
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

public class MqttClient implements ConnectionListener<MQMessage>, MQDevice, NetworkClient {

	private final Logger logger = Logger.getLogger(getClass());

	public static String MESSAGETYPE_PARAM = "MESSAGETYPE";
	private int RESEND_PERIOND = 3000;
	private int WORKER_THREADS = 4;

	private InetSocketAddress address;
	private ConnectionState connectionState;

	private TimersMap timers;
	private TCPClient client;

	private Account account;

	private Will will;
	private ClientListener listener;
	private DBInterface dbInterface;

	public MqttClient(Account account) throws Exception {
		this.dbInterface = DBHelper.getInstance();
		this.account = account;
		this.address = new InetSocketAddress(account.getServerHost(), account.getServerPort());

		Text topicName = new Text(account.getWillTopic());
		QoS qos = QoS.valueOf(account.getQos());
		Topic topic = new Topic(topicName, qos);
		this.will = new Will(topic, account.getWill().getBytes(), account.isRetain());
		client = new TCPClient(address, WORKER_THREADS);
	}

	@Override
	public void setListener(ClientListener listener) {
		this.listener = listener;
	}

	@Override
	public void setState(ConnectionState state) {
		connectionState = state;
		if (listener != null)
			listener.stateChanged(state);
	}

	@Override
	public Boolean createChannel() {
		setState(ConnectionState.CHANNEL_CREATING);
		Boolean isSuccess = client.init(this);
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

	@Override
	public void closeChannel() {
		if (client != null)
			client.shutdown();
	}

	@Override
	public void connect() {

		setState(ConnectionState.CONNECTING);
		Connect connect = new Connect(account.getUsername(), account.getPassword(), account.getClientId(),
				account.isCleanSession(), account.getKeepAlive(), will);

		if (timers != null)
			timers.stopAllTimers();

		timers = new TimersMap(client, RESEND_PERIOND, account.getKeepAlive() * 1000);

		timers.storeConnectTimer(connect);

		if (client.isConnected())
			client.send(connect);
	}

	@Override
	public void disconnect() {
		if (client.isConnected()) {
			client.send(new Disconnect());
			client.close();
		}

		setState(ConnectionState.NONE);
		return;
	}

	@Override
	public void subscribe(Topic[] topics) {
		Subscribe subscribe = new Subscribe(null, topics);
		timers.store(subscribe);
		client.send(subscribe);
	}

	@Override
	public void unsubscribe(String[] topics) {
		Text[] texts = new Text[topics.length];
		for (int i = 0; i < topics.length; i++) {
			texts[i] = new Text(topics[i]);
		}
		Unsubscribe unsubscribe = new Unsubscribe(null, texts);
		timers.store(unsubscribe);
		client.send(unsubscribe);
	}

	@Override
	public void publish(Topic topic, byte[] content, Boolean retain, Boolean dup) {
		Publish publish = new Publish(null, topic, Unpooled.wrappedBuffer(content), retain, dup);
		if (topic.getQos() != QoS.AT_MOST_ONCE)
			timers.store(publish);

		client.send(publish);
	}

	public void reinit() {
		setState(ConnectionState.CHANNEL_CREATING);
		if (client != null)
			client.shutdown();

		client = new TCPClient(address, WORKER_THREADS);
	}

	@Override
	public void closeConnection() {
		if (timers != null)
			timers.stopAllTimers();

		if (client != null) {
			TCPClient currClient = client;
			client = null;
			currClient.shutdown();
		}
	}

	@Override
	public void packetReceived(MQMessage message) {
		try {
			message.processBy(this);
		} catch (Exception ex) {
			ex.printStackTrace();
			client.shutdown();
		}
	}

	@Override
	public void cancelConnection() {
		client.shutdown();
	}

	@Override
	public void connectionLost() {
		if (account.isCleanSession())
			clearAccountTopics();

		if (timers != null)
			timers.stopAllTimers();

		if (client != null) {
			client.shutdown();
			setState(ConnectionState.CONNECTION_LOST);
		}
	}

	@Override
	public void processConnack(ConnackCode code, boolean sessionPresent) {
		// CANCEL CONNECT TIMER
		MessageResendTimer<MQMessage> timer = timers.getConnectTimer();
		timers.stopConnectTimer();

		// CHECK CODE , IF OK THEN MOVE TO CONNECTED AND NOTIFY NETWORK SESSION
		if (code == ConnackCode.ACCEPTED) {
			setState(ConnectionState.CONNECTION_ESTABLISHED);

			if (timer != null) {
				Connect connect = (Connect) timer.getMessage();
				if (connect.isCleanSession())
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

	@Override
	public void processSuback(Integer packetID, List<SubackCode> codes) {

		logger.info("processing suback...");

		MQMessage message = timers.remove(packetID);
		if (message == null || message.getType() != MessageType.SUBSCRIBE) {
			logger.warn("received unexpected suback, was expecting " + message.getType());
			return;
		}

		Subscribe subscribe = (Subscribe) message;
		for (int i = 0; i < codes.size(); i++) {
			SubackCode code = codes.get(i);
			if (code != SubackCode.FAILURE) {
				Topic topic = subscribe.getTopics()[i];
				QoS expectedQos = topic.getQos();
				QoS actualQos = QoS.valueOf(code.getNum());
				if (!account.isCleanSession()) {
					try {
						if (expectedQos == actualQos)
							dbInterface.saveTopic(new com.mobiussoftware.iotbroker.db.Topic(topic.getName().toString(),
									(byte) expectedQos.getValue()));
						else
							dbInterface.saveTopic(new com.mobiussoftware.iotbroker.db.Topic(topic.getName().toString(),
									(byte) actualQos.getValue()));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (listener != null)
					listener.messageReceived(new Subscribe(packetID, new Topic[] { topic }));
			} else
				logger.warn("received suback failure");
		}
	}

	@Override
	public void processUnsuback(Integer packetID) {

		logger.info("processing unsuback...");

		MQMessage message = timers.remove(packetID);
		if (message == null || message.getType() != MessageType.UNSUBSCRIBE) {
			logger.warn("received unexpected unsuback");
			return;
		}

		Unsubscribe unsubscribe = (Unsubscribe) message;
		Text[] topics = unsubscribe.getTopics();
		if (!account.isCleanSession()) {
			try {
				for (Text topic : topics)
					dbInterface.deleteTopic(topic.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (listener != null)
			listener.messageReceived(unsubscribe);
	}

	@Override
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!(isDup && publisherQos == QoS.EXACTLY_ONCE)) {
			byte[] bytes = new byte[content.readableBytes()];
			content.readBytes(bytes);
			Message message = new Message(account, topicName.toString(), new String(bytes), true,
					(byte) publisherQos.getValue(), retain, isDup);
			try {
				dbInterface.saveMessage(message);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (listener != null)
				listener.messageReceived(new Publish(topic, content, retain, isDup));
		}
	}

	@Override
	public void processPuback(Integer packetID) {
		timers.remove(packetID);
	}

	@Override
	public void processPubrec(Integer packetID) {
		timers.remove(packetID);
		MQMessage message = new Pubrel(packetID);
		timers.store(message);
		client.send(message);
	}

	@Override
	public void processPubrel(Integer packetID) {
		client.send(new Pubcomp(packetID));
	}

	@Override
	public void processPubcomp(Integer packetID) {
		timers.remove(packetID);
	}

	@Override
	public void processPingresp() {
	}

	@Override
	public void processSubscribe(Integer packetID, Topic[] topics) {
		logger.error("received invalid message subscribe");
	}

	@Override
	public void processConnect(boolean cleanSession, int keepalive, Will will) {
		logger.error("received invalid message connect");
	}

	@Override
	public void processPingreq() {
		logger.error("received invalid message pingreq");
	}

	@Override
	public void processDisconnect() {
		logger.error("received invalid message disconnect");
	}

	@Override
	public void processUnsubscribe(Integer packetID, Text[] topics) {
		logger.error("received invalid message unsubscribe");
	}

	@Override
	public void connected() {
		setState(ConnectionState.CHANNEL_ESTABLISHED);
	}

	@Override
	public void connectFailed() {
		setState(ConnectionState.CHANNEL_FAILED);
	}
}
