package com.mobiussoftware.iotbroker.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "messages") public class Message
{

	@DatabaseField(generatedId = true) private int id;

	@DatabaseField(canBeNull = false, foreign = true) private Account account;

	@DatabaseField private String topic;

	@DatabaseField private String contents;

	@DatabaseField private boolean incoming;

	@DatabaseField private byte qos;

	@DatabaseField private Boolean retain;

	@DatabaseField private Boolean duplicate;

	public Message()
	{
	}

	public Message(Account account, String topic, String contents, boolean incoming, byte qos, Boolean retain, Boolean duplicate)
	{
		this.account = account;
		this.topic = topic;
		this.contents = contents;
		this.incoming = incoming;
		this.qos = qos;
		this.retain = retain;
		this.duplicate = duplicate;
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

	public String getTopic()
	{
		return topic;
	}

	public void setTopic(String topic)
	{
		this.topic = topic;
	}

	public String getContents()
	{
		return contents;
	}

	public void setContents(String contents)
	{
		this.contents = contents;
	}

	public boolean isIncoming()
	{
		return incoming;
	}

	public void setIncoming(boolean incoming)
	{
		this.incoming = incoming;
	}

	public byte getQos()
	{
		return qos;
	}

	public void setQos(byte qos)
	{
		this.qos = qos;
	}

	public Boolean getRetain()
	{
		return retain;
	}

	public void setRetain(Boolean retain)
	{
		this.retain = retain;
	}

	public Boolean getDuplicate()
	{
		return duplicate;
	}

	public void setDuplicate(Boolean duplicate)
	{
		this.duplicate = duplicate;
	}
}
