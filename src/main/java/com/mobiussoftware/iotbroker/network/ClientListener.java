package com.mobiussoftware.iotbroker.network;

import com.mobius.software.mqtt.parser.header.api.MQMessage;

public interface ClientListener {
	public void messageSent();

	public void messageReceived(MQMessage message);

	public void stateChanged(ConnectionState state);
}
