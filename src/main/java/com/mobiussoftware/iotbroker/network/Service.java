package com.mobiussoftware.iotbroker.network;

import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.ui.Main;

public class Service {

	public static void login(Account account) {
		Main.createAndShowLogoPane(account);
	}
}
