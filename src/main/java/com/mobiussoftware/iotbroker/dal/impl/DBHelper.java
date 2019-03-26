package com.mobiussoftware.iotbroker.dal.impl;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

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
import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.DBTopic;
import com.mobiussoftware.iotbroker.db.Message;

public class DBHelper
		implements DBInterface
{

	private final static String DATABASE_URL = "jdbc:sqlite:iotbroker.db";
	private static DBInterface instance = null;
	private final Logger logger = Logger.getLogger(getClass());
	ConnectionSource connectionSource;
	Dao<Account, String> accountDao;
	Dao<DBTopic, String> topicDao;
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
			topicDao = DaoManager.createDao(connectionSource, DBTopic.class);
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
		TableUtils.createTableIfNotExists(connectionSource, DBTopic.class);
		TableUtils.createTableIfNotExists(connectionSource, Message.class);
	}

	@Override public CloseableWrappedIterable<Account> accountIterator()
	{
		return accountDao.getWrappedIterable();
	}

	@Override public Account getAccountByCertificate(String certificate) throws SQLException 
	{
		List<Account> list = accountDao.queryForEq("certificate", certificate);
		return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
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

	@Override public List<DBTopic> getTopics(Account account)
			throws SQLException
	{
		List<DBTopic> topics = topicDao.queryBuilder().where().eq("account_id", account).query();
		return topics;
	}

	@Override public DBTopic getTopic(String id)
			throws SQLException
	{
		return topicDao.queryForId(id);
	}

	@Override public DBTopic getTopicByName(String name, Account account) throws SQLException
	{
		List<DBTopic> topics = topicDao.queryBuilder().where().eq("name", name).and().eq("account_id", account).query();
		return CollectionUtils.isEmpty(topics) ? null : topics.get(0);
	}
	
	@Override public void createTopic(DBTopic topic)
			throws SQLException
	{
		topicDao.create(topic);
	}

	@Override public void updateTopic(DBTopic topic) throws SQLException
	{
		topicDao.update(topic);
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

	@Override public void deleteAllTopics(Account account) throws SQLException
	{
		List<DBTopic> topics = getTopics(account);
		Set<String> ids = new HashSet<>();
		for(DBTopic topic : topics)
			ids.add(String.valueOf(topic.getId()));
		topicDao.deleteIds(ids);
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
