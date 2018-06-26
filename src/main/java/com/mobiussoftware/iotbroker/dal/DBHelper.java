package com.mobiussoftware.iotbroker.dal;

import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.db.Topic;

import java.sql.SQLException;

public class DBHelper {
	private final static String DATABASE_URL = "jdbc:sqlite:iotbroker.db";

	ConnectionSource connectionSource;
	Dao<Account, String> accountDao;

	public DBHelper() throws Exception {
		init();
	}

	private void init() throws Exception {

		try {
			// create our data-source for the database
			connectionSource = new JdbcConnectionSource(DATABASE_URL);

			// setup our database and DAOs
			setupDatabase(connectionSource);
			System.out.println("database is set up");

			accountDao = DaoManager.createDao(connectionSource, Account.class);

		} finally {
			// destroy the data source which should close underlying connections
//			if (connectionSource != null) {
//				connectionSource.close();
//			}
		}
	}

	private void setupDatabase(ConnectionSource connectionSource) throws Exception {
		TableUtils.createTableIfNotExists(connectionSource, Account.class);
		TableUtils.createTableIfNotExists(connectionSource, Topic.class);
		TableUtils.createTableIfNotExists(connectionSource, Message.class);
	}

	public CloseableWrappedIterable<Account> accountIterator() {
		return accountDao.getWrappedIterable();
	}

	public void storeAccount(Account account) throws SQLException {
		accountDao.create(account);
	}

	public void deleteAccount(String id) throws SQLException {
		accountDao.deleteById(id);
	}
}
