package com.mobiussoftware.iotbroker.network;

import java.util.Timer;

//Needs to be rewritten for JAVA!!!
public class MessageResendTimer<T> {
	private static int MAX_CONNECT_RESEND = 5;

	private T message;
	private NetworkChannel<T> client;
	private TimersMapInterface<T> timersMap;
	private Timer timer;

	private Integer retriesLeft = null;

	public MessageResendTimer(T message, NetworkChannel<T> client, TimersMapInterface<T> timersMap, Boolean isConnect) {
		if (isConnect)
			retriesLeft = MAX_CONNECT_RESEND;

		this.message = message;
		this.client = client;
//			timer.stop();
		this.timersMap = timersMap;
	}


	public void execute(long period) {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		timer = new Timer();
//		timer = System.Timers.Timer();
//		timer.autoReset = false;
//		timer.Elapsed += new ElapsedEventHandler(OnTimedEvent);
//		timer. = period;
//		timer.Enabled = true;
	}

	public void onTimedEvent(Object sender, String args) {
		timer = null;
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

	public void stop() {
		if (timer != null) {
			timer.cancel();
//			timer.stop();
			timer = null;
		}
	}

	public T getMessage() {
		return message;
	}

}
