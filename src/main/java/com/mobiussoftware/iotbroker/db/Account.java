package com.mobiussoftware.iotbroker.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.mobiussoftware.iotbroker.ui.Protocol;

@DatabaseTable(tableName = "accounts") public class Account {

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

    public Account() {
    }

    public Account(Protocol protocol, String username, String password, String clientId, String serverHost, int serverPort,
            boolean cleanSession, int keepAlive, String will, String willTopic, boolean retain, int qos, boolean isDefault) {
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
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getWill() {
        return will;
    }

    public void setWill(String will) {
        this.will = will;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
