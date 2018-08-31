package com.mobiussoftware.iotbroker.db;

/**
* Mobius Software LTD
* Copyright 2015-2018, Mobius Software LTD
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
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
