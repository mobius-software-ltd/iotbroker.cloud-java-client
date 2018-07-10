package com.mobiussoftware.iotbroker.mqtt;

import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.*;
import com.mobiussoftware.iotbroker.mqtt.net.TCPClient;
import com.mobiussoftware.iotbroker.network.MessageResendTimer;
import com.mobiussoftware.iotbroker.network.TimersMapInterface;

import java.util.HashMap;
import java.util.Map;

public class TimersMap implements TimersMapInterface<MQMessage> {
	private static int MAX_VALUE = 65535;
	private static int MIN_VALUE = 1;

	private TCPClient _listener;
	private long resendPeriod;
	private long keepalivePeriod;
	private MqttClient client;

	private Map<Integer,MessageResendTimer<MQMessage>> _timersMap = new HashMap<>();
	private int packetIDCounter = MIN_VALUE;

	private MessageResendTimer<MQMessage> pingTimer;
	private MessageResendTimer<MQMessage> connectTimer;


	public TimersMap(MqttClient client, TCPClient listener, long resendPeriod, long keepalivePeriod) {
	}

	@Override
	public void cancelConnectTimer() {
	}

	@Override
	public void refreshTimer(MessageResendTimer<MQMessage> timer) {
	}

	public void stopAllTimers() {
	}

	public void store(MQMessage message) {
	}

	public void storeConnectTimer(Connect connect) {
	}

	public MessageResendTimer<MQMessage> getConnectTimer() {
		return connectTimer;
	}

	public MQMessage remove(int packetID) {
		return null;
	}

	public void startPingTimer() {
	}

	public void stopConnectTimer() {
	}
}
