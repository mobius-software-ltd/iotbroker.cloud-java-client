package com.mobiussoftware.iotbroker.ui;

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

import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;
import com.mobiussoftware.iotbroker.network.TopicListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class MainPane extends JFrame implements ClientListener
{

	private static final long serialVersionUID = -4038896950583862834L;

	private Account account;

	private MessagesListPane msgListJP;
	private TopicListPane topicListJP;

	private TopicListener topicListener;

	MainPane(Account account)
			throws Exception
	{
		this.account = account;
		drawUI();
	}

	public TopicListener getTopicListener()
	{
		return topicListener;
	}

	private void drawUI()
	{
		UIDefaults def = UIManager.getLookAndFeelDefaults();

		Insets tabInsets = new Insets(20, 0, 20, 0);
		Insets zeroInsets = new Insets(0, 0, 0, 0);
		Color opaqueColor = new Color(0, 0, 0, 0);

		def.put("TabbedPane.tabInsets", tabInsets);
		// def.put("TabbedPane.borderHightlightColor", new Insets(0,0,0,0));
		// def.put("TabbedPane.tabsOpaque", false);
		def.put("TabbedPane.focus", opaqueColor);
		def.put("TabbedPane.selected", opaqueColor);
		def.put("TabbedPane.darkShadow", new Color(255, 255, 255));
		def.put("TabbedPane.shadow", Color.lightGray);
		// def.put("TabbedPane.light", new Color( 180,180,180));
		def.put("TabbedPane.tabAreaInsets", zeroInsets);
		def.put("TabbedPane.contentBorderInsets", zeroInsets);
		def.put("TabbedPane.selectedTabPadInsets", zeroInsets);
		//
		// def.put("TabbedPane.selectHighlight", new Color(0, 0,0,0));
		// def.put("TabbedPane.unselectedBackground", new Color(0, 0,0,0));
		// def.put("TabbedPane.contentAreaColor", new Color( 122,122,0));

		final JTabbedPane jtp = new JTabbedPane();
		jtp.setBackground(UIConstants.APP_BG_COLOR);
		jtp.setUI(new BasicTabbedPaneUI()
		{
			private final Insets borderInsets = new Insets(0, 0, 0, 0);

			@Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex)
			{
			}

			@Override protected Insets getContentBorderInsets(int tabPlacement)
			{
				return borderInsets;
			}

		});
		jtp.addChangeListener(new ChangeListener()
		{
			int tabIndex = 0;

			public void stateChanged(ChangeEvent e)
			{
				ImageIcon icon = null;
				switch (tabIndex)
				{
				case 0:
					icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.TOPIC_LIST_IMG);
					break;
				case 1:
					icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.SEND_MSG_IMG);
					break;
				case 2:
					icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.MSG_LIST_IMG);
					break;
				case 3:
					icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.LOGOUT_IMG);
					break;
				}
				jtp.setIconAt(tabIndex, icon);

				tabIndex = jtp.getSelectedIndex();
				switch (tabIndex)
				{
				case 0:
					icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.TOPIC_LIST_SELECTED_IMG);
					break;
				case 1:
					icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.SEND_MSG_SELECTED_IMG);
					break;
				case 2:
					icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.MSG_LIST_SELECTED_IMG);
					break;
				case 3:
					icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.LOGOUT_SELECTED_IMG);
					Main.getCurrentClient().disconnect();
					try
					{
						final DBInterface dbInterface = DBHelper.getInstance();
						dbInterface.unmarkAsDefault(account);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
					Main.showAccountMgmtPane();
					Main.disposeMainPane();
					break;
				}
				jtp.setIconAt(tabIndex, icon);
			}
		});
		jtp.setTabPlacement(JTabbedPane.BOTTOM);

		this.topicListJP = new TopicListPane(account);
		ImageIcon topicListIcon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.TOPIC_LIST_SELECTED_IMG);
		jtp.addTab("", topicListIcon, topicListJP);
		topicListener = topicListJP;

		SendMessagePane sendMsgJP = new SendMessagePane(account);

		ImageIcon sendMsgIcon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.SEND_MSG_IMG);
		jtp.addTab("", sendMsgIcon, sendMsgJP);

		this.msgListJP = new MessagesListPane(account);
		ImageIcon msgListIcon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.MSG_LIST_IMG);
		jtp.addTab("", msgListIcon, this.msgListJP);
		sendMsgJP.setListener(msgListJP);

		JPanel logoutJP = new JPanel();
		ImageIcon logoutIcon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.LOGOUT_IMG);
		jtp.addTab("", logoutIcon, logoutJP);

		setContentPane(jtp);
		setBackground(Color.white);
	}

	@Override public void messageSent(Message messageObj)
	{
	}

	@Override public void messageReceived(Message message)
	{

		msgListJP.messageReceived(message);

	}

	@Override public void stateChanged(ConnectionState state)
	{
		System.out.println("MainPane state changed state=" + state.toString());
		switch (state)
		{
		case CONNECTION_LOST:
			try
			{
				// dbInterface.unmarkAsDefault(account);
			}
			catch (Exception e)
			{
			}
			Main.showAccountMgmtPane();
			break;
		default:
			break;

		}
	}

}