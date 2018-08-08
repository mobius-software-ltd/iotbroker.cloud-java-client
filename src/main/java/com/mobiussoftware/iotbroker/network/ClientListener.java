package com.mobiussoftware.iotbroker.network;

import com.mobiussoftware.iotbroker.db.Message;

public interface ClientListener
{
	void messageSent(Message messageObj);

	void messageReceived(Message message);

	void stateChanged(ConnectionState state);
}
