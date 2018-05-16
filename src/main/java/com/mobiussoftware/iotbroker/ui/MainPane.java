package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class MainPane extends JFrame {

    static final int WIDTH = 428;
    static final int HEIGHT = 533;

    static final Color APP_COLOR = new Color(25, 163, 219);
    static final Color SELECTION_COLOR = new Color(25, 163, 219, 50);

    static final String IMAGE_RES_PATH = "src/main/resources/images/";

    private static final String TOPIC_LIST_SELECTED_IMG = "ic_topic_list_blue.png";
    private static final String SEND_MSG_SELECTED_IMG = "ic_send_msg_blue.png";
    private static final String MSG_LIST_SELECTED_IMG = "ic_msg_list_blue.png";
    private static final String LOGOUT_SELECTED_IMG = "ic_logout_blue.png";

    private static final String TOPIC_LIST_IMG = "ic_topic_list_gray.png";
    private static final String SEND_MSG_IMG = "ic_send_msg_gray.png";
    private static final String MSG_LIST_IMG = "ic_msg_list_gray.png";
    private static final String LOGOUT_IMG = "ic_logout_gray.png";

    static final String BG_IMG = "img_background.jpg";

    static final String IC_SETTINGS_FILE_PATH = "ic_settings.png";
    static final String IC_TRASH_FILE_PATH = "ic_trash.png";

    static final Font TEXT_LABEL_FONT = new Font("SansSerif", Font.BOLD, 10);
    static final Font REGULAR_FONT = new Font("SansSerif", Font.PLAIN, 12);

    static final Image BG_IMAGE = new ImageIcon(MainPane.IMAGE_RES_PATH + MainPane.BG_IMG).getImage();
    static final ImageIcon IC_TRASH = new ImageIcon(MainPane.IMAGE_RES_PATH + MainPane.IC_TRASH_FILE_PATH);
    static final ImageIcon IC_SETTINGS = new ImageIcon(MainPane.IMAGE_RES_PATH + MainPane.IC_SETTINGS_FILE_PATH);

    private JPanel imagesPanel;
    private JFrame mainFrame;


    private MainPane() {

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
            boolean tbp = key.toString().startsWith("ComboBox");
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

        JPanel msgListJP = new MyPanel();
//        msgListJP.setBackground(Color.white);
        JPanel logoutJP = new MyPanel();
//        logoutJP.setBackground(Color.white);


        JLabel msgListLbl = new JLabel();
        msgListLbl.setText("You are in area of msgListLbl");
        JLabel logoutLbl = new JLabel();
        logoutLbl.setText("You are in area of logoutLbl");


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
        jtp.setTabPlacement(JTabbedPane.BOTTOM);

        JPanel topicListJP = new TopicListPane();
//        topicListJP.setBackground(Color.white);
        ImageIcon topicListIcon = new ImageIcon(IMAGE_RES_PATH + TOPIC_LIST_SELECTED_IMG);
        jtp.addTab("", topicListIcon, topicListJP);

        JPanel sendMsgJP = new SendMessagePane();
//        sendMsgJP.setBackground(Color.white);
        JLabel sendMsgLbl = new JLabel();
        sendMsgLbl.setText("You are in area of sendMsgLbl");
        sendMsgJP.add(sendMsgLbl);
        ImageIcon sendMsgIcon = new ImageIcon(IMAGE_RES_PATH + SEND_MSG_IMG);
        jtp.addTab("", sendMsgIcon, sendMsgJP);

        ImageIcon msgListIcon = new ImageIcon(IMAGE_RES_PATH + MSG_LIST_IMG);
        ImageIcon logoutIcon = new ImageIcon(IMAGE_RES_PATH + LOGOUT_IMG);
        jtp.addTab("", msgListIcon, msgListJP);
        jtp.addTab("", logoutIcon, logoutJP);

//        FontIcon topicListIcon = new FontIcon();
//        topicListIcon.setIkon(Dashicons.LIST_VIEW);
//        FontIcon sendMsgIcon = new FontIcon();
//        sendMsgIcon.setIkon(Material.SEND);
//        FontIcon msgListIcon = new FontIcon();
//        msgListIcon.setIkon(Openiconic.CHAT);
//        FontIcon logoutIcon = new FontIcon();
//        logoutIcon.setIkon(Openiconic.ACCOUNT_LOGOUT);
//        jtp.addTab("Topics List", topicListIcon, topicListJP);
//        jtp.addTab("Send Message", sendMsgIcon, sendMsgJP);
//        jtp.addTab("Messages List", msgListIcon, msgListJP);
//        jtp.addTab("Logout", logoutIcon, logoutJP);

    }

    private static void createAndShowGUI() {
        MainPane tp = new MainPane();
        tp.setTitle("MQTT");
        tp.pack();
        tp.setVisible(true);;
        tp.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        tp.setSize(new Dimension(WIDTH, HEIGHT));
    }

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    class MyPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            g.drawImage(BG_IMAGE, 0, 0, null);
        }
    }
}