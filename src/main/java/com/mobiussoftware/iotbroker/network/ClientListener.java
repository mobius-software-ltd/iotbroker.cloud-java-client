package com.mobiussoftware.iotbroker.network;

import com.mobius.software.mqtt.parser.avps.MessageType;

public interface ClientListener {
	public void messageSent();
	public void messageReceived(MessageType type);
	public void stateChanged(ConnectionState state);
}
