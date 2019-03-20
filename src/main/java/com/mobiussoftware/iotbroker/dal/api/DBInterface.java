package com.mobiussoftware.iotbroker.dal.api;

import java.sql.SQLException;
import java.util.List;

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
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.DBTopic;
import com.mobiussoftware.iotbroker.db.Message;

public interface DBInterface
{

	CloseableWrappedIterable<Account> accountIterator();

	void storeAccount(Account account)
			throws SQLException;

	void deleteAccount(String id)
			throws SQLException;

	List<DBTopic> getTopics(Account account)
			throws SQLException;

	DBTopic getTopic(String id)
			throws SQLException;

	DBTopic getTopicByName(String name)
		throws SQLException;
	
	void createTopic(DBTopic topic)
			throws SQLException;

	void updateTopic(DBTopic topic)
			throws SQLException;
	
	void deleteTopic(String id)
			throws SQLException;

	List<Message> getMessages(Account account)
			throws SQLException;

	void saveMessage(Message message)
			throws SQLException;

	void deleteAllTopics(Account account) throws SQLException;

	Account getDefaultAccount()
			throws SQLException;

	Account getAccountByCertificate(String certificate) throws SQLException;
	
	void markAsDefault(Account account)
			throws SQLException;

	void unmarkAsDefault(Account account)
			throws SQLException;
}
