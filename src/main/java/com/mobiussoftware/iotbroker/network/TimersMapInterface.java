package com.mobiussoftware.iotbroker.network;

public interface TimersMapInterface<T> {
	void cancelConnectTimer();

	void refreshTimer(MessageResendTimer<T> timer);
}
