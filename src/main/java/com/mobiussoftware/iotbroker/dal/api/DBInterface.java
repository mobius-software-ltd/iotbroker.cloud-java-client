package com.mobiussoftware.iotbroker.dal.api;

import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.db.Topic;

import java.sql.SQLException;
import java.util.List;

public interface DBInterface {
	CloseableWrappedIterable<Account> accountIterator();

	void storeAccount(Account account) throws SQLException;

	void deleteAccount(String id) throws SQLException;

	List<Topic> getTopics(Account account) throws SQLException;

	void saveTopic(Topic topic) throws SQLException;

	void deleteTopic(String id) throws SQLException;

	List<Message> getMessages(Account account) throws SQLException;

	void saveMessage(Message message) throws SQLException;

	void deleteAllTopics();

	Boolean topicExists(String topicName) throws SQLException;

	//void unmarkAsDefault(Account account) throws SQLException;
}
