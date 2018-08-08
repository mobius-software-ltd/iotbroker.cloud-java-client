package com.mobiussoftware.iotbroker.ui;

import com.mobius.software.mqtt.parser.avps.SubackCode;
import com.mobius.software.mqtt.parser.avps.Text;
import com.mobius.software.mqtt.parser.avps.Topic;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.Suback;
import com.mobius.software.mqtt.parser.header.impl.Subscribe;
import com.mobius.software.mqtt.parser.header.impl.Unsubscribe;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class MainPane<T>
		extends JFrame
		implements ClientListener<T>
{

	private static final long serialVersionUID = -4038896950583862834L;

	private Account account;

	private MessagesListPane msgListJP;
	private TopicListPane topicListJP;

	MainPane(Account account)
			throws Exception
	{
		this.account = account;
		drawUI();
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

	@Override public void messageReceived(T msg)
	{

		if (msg instanceof MQMessage)
		{
			MQMessage message = (MQMessage) msg;
			switch (message.getType())
			{
			case SUBACK:
				Suback suback = (Suback) message;
				if (suback.getReturnCodes().contains(SubackCode.FAILURE))
					topicListJP.finishAddingTopicFailed();
				break;
			case SUBSCRIBE:
				Subscribe subscribe = (Subscribe) message;
				for (Topic subscribeTopic : subscribe.getTopics())
					topicListJP.finishAddingTopic(subscribeTopic.getName().toString(), subscribeTopic.getQos().getValue());
				break;
			case UNSUBSCRIBE:
				/*
				 * try { Main.createAndShowMainPane(account); } catch (Exception e)
				 * { System.out.
				 * println("Error occured while createAndShowMainPane from MainPanel"
				 * ); e.printStackTrace(); }
				 */
				Unsubscribe unsubscribe = (Unsubscribe) message;
				for (Text unsubscribeTopic : unsubscribe.getTopics())
					topicListJP.finishDeletingTopic(unsubscribeTopic.toString());
				break;
			case PUBLISH:
				msgListJP.messageReceived(message);
				break;
			default:
				break;
			}
		}
		else if (msg instanceof MQMessage)
		{
		}
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