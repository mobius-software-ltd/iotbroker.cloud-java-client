package com.mobiussoftware.iotbroker.network;

public interface NetworkChannel<T>
{
	void send(T message);
}
