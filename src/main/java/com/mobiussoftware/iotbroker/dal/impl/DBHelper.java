package com.mobiussoftware.iotbroker.dal.impl;

import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.db.Topic;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class DBHelper
		implements DBInterface
{

	private final static String DATABASE_URL = "jdbc:sqlite:iotbroker.db";
	private static DBInterface instance = null;
	private final Logger logger = Logger.getLogger(getClass());
	ConnectionSource connectionSource;
	Dao<Account, String> accountDao;
	Dao<Topic, String> topicDao;
	Dao<Message, String> messageDao;

	protected DBHelper()
			throws Exception
	{
		init();
	}

	public static DBInterface getInstance()
			throws Exception
	{
		if (instance == null)
		{
			instance = new DBHelper();
		}
		return instance;
	}

	private void init()
			throws Exception
	{

		try
		{
			// create our data-source for the database
			connectionSource = new JdbcConnectionSource(DATABASE_URL);

			// setup our database and DAOs
			setupDatabase(connectionSource);
			logger.info("database is set up");

			accountDao = DaoManager.createDao(connectionSource, Account.class);
			topicDao = DaoManager.createDao(connectionSource, Topic.class);
			messageDao = DaoManager.createDao(connectionSource, Message.class);

		}
		finally
		{
			// destroy the data source which should close underlying connections
			if (connectionSource != null)
			{
				connectionSource.close();
			}
		}
	}

	private void setupDatabase(ConnectionSource connectionSource)
			throws Exception
	{
		TableUtils.createTableIfNotExists(connectionSource, Account.class);
		TableUtils.createTableIfNotExists(connectionSource, Topic.class);
		TableUtils.createTableIfNotExists(connectionSource, Message.class);
	}

	@Override public CloseableWrappedIterable<Account> accountIterator()
	{
		return accountDao.getWrappedIterable();
	}

	@Override public void storeAccount(Account account)
			throws SQLException
	{
		accountDao.create(account);
	}

	@Override public void deleteAccount(String id)
			throws SQLException
	{
		accountDao.deleteById(id);
	}

	@Override public List<Topic> getTopics(Account account)
			throws SQLException
	{
		List<Topic> topics = topicDao.queryBuilder().where().eq("account_id", account).query();
		return topics;
	}

	@Override public Topic getTopic(String id)
			throws SQLException
	{
		return topicDao.queryForId(id);
	}

	@Override public void saveTopic(Topic topic)
			throws SQLException
	{
		topicDao.create(topic);
	}

	@Override public void deleteTopic(String id)
			throws SQLException
	{
		topicDao.deleteById(id);
	}

	@Override public List<Message> getMessages(Account account)
			throws SQLException
	{
		List<Message> messages = messageDao.queryBuilder().where().eq("account_id", account).query();
		return messages;
	}

	@Override public void saveMessage(Message message)
			throws SQLException
	{
		messageDao.create(message);
	}

	@Override public void deleteAllTopics()
	{

	}

	@Override public Account getDefaultAccount()
			throws SQLException
	{
		Account account = accountDao.queryBuilder().where().eq("is_default", true).queryForFirst();
		return account;
	}

	/*@Override
	public Boolean topicExists(String topicName) throws SQLException {
		List<Topic> list = topicDao.queryBuilder().where().eq("name", topicName).query();
		if (!list.isEmpty())
			return true;
		return false;
	}*/

	@Override public void markAsDefault(Account account)
			throws SQLException
	{
		account.setDefault(true);
		accountDao.update(account);
	}

	@Override public void unmarkAsDefault(Account account)
			throws SQLException
	{
		account.setDefault(false);
		accountDao.update(account);
	}
}
