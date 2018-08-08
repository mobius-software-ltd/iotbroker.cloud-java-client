package com.mobiussoftware.iotbroker.network;

public interface ConnectionListener<T>
{
	void packetReceived(T message);

	void connectionLost();

	void connected();

	void connectFailed();
}
