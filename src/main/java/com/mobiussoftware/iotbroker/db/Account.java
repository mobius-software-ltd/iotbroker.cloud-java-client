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
import com.mobiussoftware.iotbroker.ui.Protocol;

@DatabaseTable(tableName = "accounts") public class Account
{

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField private Protocol protocol;
	@DatabaseField(canBeNull = true) private String username;
	@DatabaseField(canBeNull = true) private String password;
	@DatabaseField(columnName = "client_id", canBeNull = true) private String clientId;
	@DatabaseField(columnName = "server_host") private String serverHost;
	@DatabaseField private int serverPort;

	@DatabaseField(columnName = "clean_session", canBeNull = true) private boolean cleanSession;
	@DatabaseField(columnName = "keep_alive", canBeNull = true) private int keepAlive;
	@DatabaseField(canBeNull = true) private String will;
	@DatabaseField(columnName = "will_topic", canBeNull = true) private String willTopic;
	@DatabaseField(canBeNull = true) private boolean retain;
	@DatabaseField(canBeNull = true) private int qos;
	@DatabaseField(columnName = "is_default", canBeNull = true) private boolean isDefault;

	@DatabaseField(columnName = "is_secure", canBeNull = true) private boolean isSecure;
	@DatabaseField(columnName = "certificate_path", canBeNull = true) private String certificatePath;
	@DatabaseField(columnName = "certificate_password", canBeNull = true) private String certificatePassword;
	
	public Account()
	{
	}

	public Account(Protocol protocol, String username, String password, String clientId, String serverHost, int serverPort, boolean cleanSession, int keepAlive, String will, String willTopic, boolean retain, int qos, boolean isDefault, boolean isSecure, String certificatePath, String certificatePassword)
	{
		this.protocol = protocol;
		this.username = username;
		this.password = password;
		this.clientId = clientId;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.cleanSession = cleanSession;
		this.keepAlive = keepAlive;
		this.will = will;
		this.willTopic = willTopic;
		this.retain = retain;
		this.qos = qos;
		this.isDefault = isDefault;
		this.isSecure = isSecure;
		this.certificatePath = certificatePath;
		this.certificatePassword = certificatePassword;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public Protocol getProtocol()
	{
		return protocol;
	}

	public void setProtocol(Protocol protocol)
	{
		this.protocol = protocol;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public String getServerHost()
	{
		return serverHost;
	}

	public void setServerHost(String serverHost)
	{
		this.serverHost = serverHost;
	}

	public int getServerPort()
	{
		return serverPort;
	}

	public void setServerPort(int serverPort)
	{
		this.serverPort = serverPort;
	}

	public boolean isCleanSession()
	{
		return cleanSession;
	}

	public void setCleanSession(boolean cleanSession)
	{
		this.cleanSession = cleanSession;
	}

	public int getKeepAlive()
	{
		return keepAlive;
	}

	public void setKeepAlive(int keepAlive)
	{
		this.keepAlive = keepAlive;
	}

	public String getWill()
	{
		return will;
	}

	public void setWill(String will)
	{
		this.will = will;
	}

	public String getWillTopic()
	{
		return willTopic;
	}

	public void setWillTopic(String willTopic)
	{
		this.willTopic = willTopic;
	}

	public boolean isRetain()
	{
		return retain;
	}

	public void setRetain(boolean retain)
	{
		this.retain = retain;
	}

	public int getQos()
	{
		return qos;
	}

	public void setQos(int qos)
	{
		this.qos = qos;
	}

	public boolean isDefault()
	{
		return isDefault;
	}

	public void setDefault(boolean isDefault)
	{
		this.isDefault = isDefault;
	}

	public boolean isSecure()
	{
		return isSecure;
	}

	public void setSecure(boolean isSecure)
	{
		this.isSecure = isSecure;
	}

	public String getCertificatePath()
	{
		return certificatePath;
	}

	public void setCertificatePath(String certificatePath)
	{
		this.certificatePath = certificatePath;
	}

	public String getCertificatePassword()
	{
		return certificatePassword;
	}

	public void setCertificatePassword(String certificatePassword)
	{
		this.certificatePassword = certificatePassword;
	}
}
