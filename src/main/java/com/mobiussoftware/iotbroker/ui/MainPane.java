package com.mobiussoftware.iotbroker.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.plaf.multi.MultiTabbedPaneUI;

import org.kordamp.ikonli.dashicons.Dashicons;
import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.openiconic.Openiconic;
import org.kordamp.ikonli.swing.FontIcon;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.net.URL;

public class MainPane extends JFrame {

    public static final String IMAGE_RES_PATH = "src/main/resources/images/";

    private static final String TOPIC_LIST_SELECTED_IMG = "ic_topic_list_blue.png";
    private static final String SEND_MSG_SELECTED_IMG = "ic_send_msg_blue.png";
    private static final String MSG_LIST_SELECTED_IMG = "ic_msg_list_blue.png";
    private static final String LOGOUT_SELECTED_IMG = "ic_logout_blue.png";

    private static final String TOPIC_LIST_IMG = "ic_topic_list_gray.png";
    private static final String SEND_MSG_IMG = "ic_send_msg_gray.png";
    private static final String MSG_LIST_IMG = "ic_msg_list_gray.png";
    private static final String LOGOUT_IMG = "ic_logout_gray.png";

    public static final String BG_IMG = "img_background.jpg";

    public static final String IC_SETTINGS = "ic_settings.png";

    private JPanel imagesPanel;
    private JFrame mainFrame;


    public MainPane() {

        UIDefaults def = UIManager.getLookAndFeelDefaults();
////        UIDefaults def = new BasicTabbedPaneUI();

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

        for(Object key : UIManager.getLookAndFeelDefaults().keySet()){
            boolean tbp = key.toString().startsWith("TabbedPane");
            if (tbp)
                System.out.println(key + " = " + UIManager.get(key));
        }

        final JTabbedPane jtp = new JTabbedPane();
//        jtp.setPreferredSize(new Dimension(392, 533));
        jtp.setBackground(Color.white);
        setContentPane(jtp);
        setBackground(Color.white);

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

        setTitle("MQTT");
        pack();
        setVisible(true);;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(428, 533));

        JPanel topicListJP = new TopicListPane();
//        topicListJP.setBackground(Color.white);
        JPanel sendMsgJP = new MyPanel();
//        sendMsgJP.setBackground(Color.white);
        JPanel msgListJP = new MyPanel();
//        msgListJP.setBackground(Color.white);
        JPanel logoutJP = new MyPanel();
//        logoutJP.setBackground(Color.white);

//        JLabel topicListLbl = new JLabel();
//        topicListLbl.setText("You are in area of topicListLbl");
        JLabel sendMsgLbl = new JLabel();
        sendMsgLbl.setText("You are in area of sendMsgLbl");
        JLabel msgListLbl = new JLabel();
        msgListLbl.setText("You are in area of msgListLbl");
        JLabel logoutLbl = new JLabel();
        logoutLbl.setText("You are in area of logoutLbl");

//        topicListJP.add(topicListLbl);
        sendMsgJP.add(sendMsgLbl);
        msgListJP.add(msgListLbl);
        logoutJP.add(logoutLbl);

        jtp.addChangeListener(new ChangeListener() {
            int tabIndex = 0;
            public void stateChanged(ChangeEvent e) {
                ImageIcon icon = null;
                switch (tabIndex) {
                    case 0:
                        icon = new ImageIcon(IMAGE_RES_PATH + TOPIC_LIST_IMG);
                        break;
                    case 1:
                        icon = new ImageIcon(IMAGE_RES_PATH + SEND_MSG_IMG);
                        break;
                    case 2:
                        icon = new ImageIcon(IMAGE_RES_PATH + MSG_LIST_IMG);
                        break;
                    case 3:
                        icon = new ImageIcon(IMAGE_RES_PATH + LOGOUT_IMG);
                        break;
                }
                jtp.setIconAt(tabIndex, icon);

                tabIndex = jtp.getSelectedIndex();
                switch (tabIndex) {
                    case 0:
                        icon = new ImageIcon(IMAGE_RES_PATH + TOPIC_LIST_SELECTED_IMG);
                        break;
                    case 1:
                        icon = new ImageIcon(IMAGE_RES_PATH + SEND_MSG_SELECTED_IMG);
                        break;
                    case 2:
                        icon = new ImageIcon(IMAGE_RES_PATH + MSG_LIST_SELECTED_IMG);
                        break;
                    case 3:
                        icon = new ImageIcon(IMAGE_RES_PATH + LOGOUT_SELECTED_IMG);
                        break;
                }
                jtp.setIconAt(tabIndex, icon);
            }
        });

//        FontIcon topicListIcon = new FontIcon();
//        topicListIcon.setIkon(Dashicons.LIST_VIEW);
//        FontIcon sendMsgIcon = new FontIcon();
//        sendMsgIcon.setIkon(Material.SEND);
//        FontIcon msgListIcon = new FontIcon();
//        msgListIcon.setIkon(Openiconic.CHAT);
//        FontIcon logoutIcon = new FontIcon();
//        logoutIcon.setIkon(Openiconic.ACCOUNT_LOGOUT);

        ImageIcon topicListIcon = new ImageIcon(IMAGE_RES_PATH + TOPIC_LIST_SELECTED_IMG);
        ImageIcon sendMsgIcon = new ImageIcon(IMAGE_RES_PATH + SEND_MSG_IMG);
        ImageIcon msgListIcon = new ImageIcon(IMAGE_RES_PATH + MSG_LIST_IMG);
        ImageIcon logoutIcon = new ImageIcon(IMAGE_RES_PATH + LOGOUT_IMG);

        jtp.setTabPlacement(JTabbedPane.BOTTOM);

        jtp.addTab("", topicListIcon, topicListJP);
        jtp.addTab("", sendMsgIcon, sendMsgJP);
        jtp.addTab("", msgListIcon, msgListJP);
        jtp.addTab("", logoutIcon, logoutJP);
//        jtp.addTab("Topics List", topicListIcon, topicListJP);
//        jtp.addTab("Send Message", sendMsgIcon, sendMsgJP);
//        jtp.addTab("Messages List", msgListIcon, msgListJP);
//        jtp.addTab("Logout", logoutIcon, logoutJP);

    }

    public static void main(String[] args) {

        MainPane tp = new MainPane();

    }

    class MyPanel extends JPanel {

        Image bgImage = Toolkit.getDefaultToolkit().createImage(IMAGE_RES_PATH + BG_IMG);

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            g.drawImage(bgImage, 0, 0, null);
        }
    }
}