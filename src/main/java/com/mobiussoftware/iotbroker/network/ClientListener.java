package com.mobiussoftware.iotbroker.network;

import com.mobiussoftware.iotbroker.db.Message;

public interface ClientListener<T>
{
	void messageSent(Message messageObj);

	void messageReceived(T message);

	void stateChanged(ConnectionState state);
}
