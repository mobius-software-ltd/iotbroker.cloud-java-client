package com.mobiussoftware.iotbroker.dal.api;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.db.Topic;

public interface DBInterface {
	
	CloseableWrappedIterable<Account> accountIterator();

	void storeAccount(Account account) throws SQLException;

	void deleteAccount(String id) throws SQLException;

	List<Topic> getTopics(Account account) throws SQLException;

	Topic getTopic(String id) throws SQLException;

	void saveTopic(Topic topic) throws SQLException;

	void deleteTopic(String id) throws SQLException;

	List<Message> getMessages(Account account) throws SQLException;

	void saveMessage(Message message) throws SQLException;

	void deleteAllTopics();
}
