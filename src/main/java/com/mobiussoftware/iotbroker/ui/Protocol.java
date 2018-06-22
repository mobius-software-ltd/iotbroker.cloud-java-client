package com.mobiussoftware.iotbroker.ui;

public enum Protocol {
	MQTT("MQTT"), MQTTSN("MQTT-SN"), CoAP("CoAP"), AMQP("AMQP");

	private final String value;

	Protocol(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
