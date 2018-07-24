package com.mobiussoftware.iotbroker.network;

import java.util.concurrent.ScheduledFuture;

public class MessageResendTimer<T> implements Runnable {
	private static int MAX_CONNECT_RESEND = 5;

	private T message;
	private NetworkChannel<T> client;
	private TimersMapInterface<T> timersMap;
	private Integer retriesLeft = null;
	private ScheduledFuture<?> future;

	public MessageResendTimer(T message, NetworkChannel<T> client, TimersMapInterface<T> timersMap, Boolean isConnect) {
		if (isConnect)
			retriesLeft = MAX_CONNECT_RESEND;

		this.message = message;
		this.client = client;
		this.timersMap = timersMap;
	}

	@Override
	public void run() {
		onTimedEvent();
	}

	public void onTimedEvent() {
		if (retriesLeft != null) {
			retriesLeft--;
			if (retriesLeft == 0) {
				timersMap.cancelConnectTimer();
				return;
			}
		}

		client.send(message);
		timersMap.refreshTimer(this);
	}

	public T getMessage() {
		return message;
	}

	public ScheduledFuture<?> getFuture() {
		return future;
	}

	public void setFuture(ScheduledFuture<?> future) {
		this.future = future;
	}

}
