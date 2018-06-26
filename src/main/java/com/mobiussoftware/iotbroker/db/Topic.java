package com.mobiussoftware.iotbroker.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "topics")
public class Topic {

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField
	private String name;

	@DatabaseField
	private byte qos;

	public Topic() {
	}

	public Topic(String name, byte qos) {
		this.name = name;
		this.qos = qos;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte getQos() {
		return qos;
	}

	public void setQos(byte qos) {
		this.qos = qos;
	}
}
