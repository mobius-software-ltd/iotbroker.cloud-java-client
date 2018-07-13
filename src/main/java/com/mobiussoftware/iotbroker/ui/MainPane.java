package com.mobiussoftware.iotbroker.ui;

import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;
import com.mobiussoftware.iotbroker.network.NetworkClient;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class MainPane extends JFrame implements ClientListener{

    private Account account;

    private JPanel imagesPanel;
    private JFrame mainFrame;
    private DBInterface dbInterface;
    private NetworkClient client;
    
    MainPane(Account account) throws Exception{
    	this.account = account;
    	this.dbInterface = DBHelper.getInstance();
    	Main.getClient().setListener(MainPane.this);
    	this.client = Main.getClient();
    	drawUI();
    }

    private void drawUI() {
		UIDefaults def = UIManager.getLookAndFeelDefaults();

		Insets tabInsets = new Insets(20,0,20,0);
		Insets zeroInsets = new Insets(0,0,0,0);
		Color opaqueColor = new Color(0, 0,0,0);

		def.put( "TabbedPane.tabInsets", tabInsets);
//        def.put("TabbedPane.borderHightlightColor", new Insets(0,0,0,0));
//        def.put("TabbedPane.tabsOpaque", false);
		def.put("TabbedPane.focus", opaqueColor);
		def.put("TabbedPane.selected", opaqueColor);
		def.put("TabbedPane.darkShadow", new Color( 255,255,255));
		def.put("TabbedPane.shadow", Color.lightGray);
//        def.put("TabbedPane.light", new Color( 180,180,180));
		def.put("TabbedPane.tabAreaInsets", zeroInsets);
		def.put("TabbedPane.contentBorderInsets", zeroInsets);
		def.put("TabbedPane.selectedTabPadInsets", zeroInsets);
//
//        def.put("TabbedPane.selectHighlight", new Color(0, 0,0,0));
//        def.put("TabbedPane.unselectedBackground", new Color(0, 0,0,0));
//        def.put("TabbedPane.contentAreaColor", new Color( 122,122,0));

		final JTabbedPane jtp = new JTabbedPane();
		jtp.setBackground(UIConstants.APP_BG_COLOR);
		jtp.setUI(new BasicTabbedPaneUI() {
			private final Insets borderInsets = new Insets(0, 0, 0, 0);

			@Override
			protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
			}

			@Override
			protected Insets getContentBorderInsets(int tabPlacement) {
				return borderInsets;
			}

		});
		jtp.addChangeListener(new ChangeListener() {
			int tabIndex = 0;
			public void stateChanged(ChangeEvent e) {
				ImageIcon icon = null;
				switch (tabIndex) {
					case 0:
						icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.TOPIC_LIST_IMG);
						break;
					case 1:
						icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.SEND_MSG_IMG);
						break;
					case 2:
						icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.MSG_LIST_IMG);
						break;
					case 3:
						icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.LOGOUT_IMG);
						break;
				}
				jtp.setIconAt(tabIndex, icon);

				tabIndex = jtp.getSelectedIndex();
				switch (tabIndex) {
					case 0:
						icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.TOPIC_LIST_SELECTED_IMG);
						break;
					case 1:
						icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.SEND_MSG_SELECTED_IMG);
						break;
					case 2:
						icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.MSG_LIST_SELECTED_IMG);
						break;
					case 3:
						icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.LOGOUT_SELECTED_IMG);
						Main.showAccountMgmtPane();
						Main.disposeMainPane();
						break;
				}
				jtp.setIconAt(tabIndex, icon);
			}
		});
		jtp.setTabPlacement(JTabbedPane.BOTTOM);

		JPanel topicListJP = new TopicListPane(account);
		ImageIcon topicListIcon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.TOPIC_LIST_SELECTED_IMG);
		jtp.addTab("", topicListIcon, topicListJP);

		SendMessagePane sendMsgJP = new SendMessagePane(account);
		ImageIcon sendMsgIcon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.SEND_MSG_IMG);
		jtp.addTab("", sendMsgIcon, sendMsgJP);

		MessagesListPane msgListJP = new MessagesListPane(account);
		ImageIcon msgListIcon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.MSG_LIST_IMG);
		jtp.addTab("", msgListIcon, msgListJP);
		sendMsgJP.setListener(MainPane.this);

		JPanel logoutJP = new JPanel();
		ImageIcon logoutIcon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.LOGOUT_IMG);
		jtp.addTab("", logoutIcon, logoutJP);

		setContentPane(jtp);
		setBackground(Color.white);
	}

	@Override
	public void messageSent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageReceived(MessageType type) {
		System.out.println("MainPane messageReceived=" + type);
		switch (type) {
		case SUBACK:
		case UNSUBACK:
		case PUBLISH:
			System.out.println("MainPane messageReceived " + type);
			
			try {
				Main.createAndShowMainPane(account);
			}catch(Exception e) 
			{
				System.out.println("Error occured while createAndShowMainPane from MainPanel");
				e.printStackTrace();
			}
			break;
		}
	}

	@Override
	public void stateChanged(ConnectionState state) {
		System.out.println("MainPane state changed state=" + state.toString());
		switch (state) {
		case CONNECTION_LOST:
			try{
				//dbInterface.unmarkAsDefault(account);
			}catch(Exception e)
			{}
			Main.getClient().setListener(null);
			Main.showAccountMgmtPane();
			break;
		default:
			break;
			
		}
	}


}