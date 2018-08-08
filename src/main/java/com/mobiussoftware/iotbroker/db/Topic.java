package com.mobiussoftware.iotbroker.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "topics") public class Topic
{

	@DatabaseField(generatedId = true) private int id;

	@DatabaseField(canBeNull = false, foreign = true) private Account account;

	@DatabaseField private String name;

	@DatabaseField private byte qos;

	public Topic()
	{
	}

	public Topic(Account account, String name, byte qos)
	{
		this.account = account;
		this.name = name;
		this.qos = qos;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public Account getAccount()
	{
		return account;
	}

	public void setAccount(Account account)
	{
		this.account = account;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public byte getQos()
	{
		return qos;
	}

	public void setQos(byte qos)
	{
		this.qos = qos;
	}
}
