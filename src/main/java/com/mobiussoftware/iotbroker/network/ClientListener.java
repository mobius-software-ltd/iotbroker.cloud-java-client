package com.mobiussoftware.iotbroker.network;

import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.db.Message;

public interface ClientListener {
	public void messageSent(Message messageObj);

	public void messageReceived(MQMessage message);

	public void stateChanged(ConnectionState state);
}
