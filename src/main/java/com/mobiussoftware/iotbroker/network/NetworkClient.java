package com.mobiussoftware.iotbroker.network;

import com.mobius.software.mqtt.parser.avps.*;

public interface NetworkClient {
	void SetListener(ClientListener listener);
    Boolean createChannel();
    void CloseChannel();
    void Connect();
    void Disconnect();
    void Subscribe(Topic[] topics);
    void Unsubscribe(String[] topics);
    void Publish(Topic topic, byte[] content, Boolean retain, Boolean dup);
    void CloseConnection();
    void CancelConnection();
    void SetState(ConnectionState state);
}
