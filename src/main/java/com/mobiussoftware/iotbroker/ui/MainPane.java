package com.mobiussoftware.iotbroker.ui;

import com.mobiussoftware.iotbroker.db.Account;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class MainPane extends JFrame {

    private Account account;

    private JPanel imagesPanel;
    private JFrame mainFrame;


    MainPane(Account account) {
    	this.account = account;
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
		sendMsgJP.setListener(msgListJP);

		JPanel logoutJP = new JPanel();
		ImageIcon logoutIcon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.LOGOUT_IMG);
		jtp.addTab("", logoutIcon, logoutJP);

		setContentPane(jtp);
		setBackground(Color.white);
	}


}