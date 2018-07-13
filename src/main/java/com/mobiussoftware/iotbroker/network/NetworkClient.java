package com.mobiussoftware.iotbroker.network;

import com.mobius.software.mqtt.parser.avps.*;

public interface NetworkClient {
	void setListener(ClientListener listener);

	Boolean createChannel();

	void closeChannel();

	void connect();

	void disconnect();

	void subscribe(Topic[] topics);

	void unsubscribe(String[] topics);

	void publish(Topic topic, byte[] content, Boolean retain, Boolean dup);

	void closeConnection();

	void cancelConnection();

	void setState(ConnectionState state);
}
