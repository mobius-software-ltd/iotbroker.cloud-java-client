package com.mobiussoftware.iotbroker.network;

import com.mobius.software.mqtt.parser.avps.Topic;

public interface NetworkClient<T>
{
	void setListener(ClientListener<T> listener);

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
