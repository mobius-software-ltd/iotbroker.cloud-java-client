package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

public class Main {

    static final Dimension SCREEN_DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();

    static JFrame logInPane;
    static JFrame mainPane;
    static JFrame accountMgmtPane;
    static JFrame logoPane;

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.

        UIDefaults def = UIManager.getLookAndFeelDefaults();
        def.put("ProgressBar.repaintInterval", 10);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for(Object key : UIManager.getLookAndFeelDefaults().keySet()){
                    boolean tbp = key.toString().startsWith("ProgressBar");
                    if (tbp)
                        System.out.println(key + " = " + UIManager.get(key));
                }
                createAndShowAccountMgmtPane();
            }
        });


    }

    static void setLocation(JFrame frame) {
        frame.setLocation(SCREEN_DIMENSION.width/2-frame.getSize().width/2, SCREEN_DIMENSION.height/2-frame.getSize().height/2);
    }

    static void createAndShowLogInPane() {

        JFrame frame = new JFrame("Log In");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add(new LogInPane());

        frame.pack();
        frame.setVisible(true);
        frame.setSize(new Dimension(UIConstants.LOGIN_FRAME_WIDTH, UIConstants.LOGIN_FRAME_HEIGHT));

//        frame.setLocation(SCREEN_DIMENSION.width/2-frame.getSize().width/2, SCREEN_DIMENSION.height/2-frame.getSize().height/2);
        setLocation(frame);

        logInPane = frame;
    }

    static void disposeLogInPane() {
        if (logInPane != null) {
            logInPane.dispose();
        }
    }

    static void createAndShowMainPane() {

        MainPane frame = new MainPane();
        frame.setTitle("MQTT");
        frame.pack();
        frame.setVisible(true);
        ;
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(UIConstants.MAIN_FRAME_WIDTH, UIConstants.MAIN_FRAME_HEIGHT));

        setLocation(frame);

        mainPane = frame;
    }

    static void disposeMainPane() {
        if (mainPane != null) {
            mainPane.dispose();
        }
    }

    static void createAndShowAccountMgmtPane() {

//        UIDefaults def = UIManager.getLookAndFeelDefaults();

        JFrame frame = new JFrame("Accounts");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add(new AccountMgmtPane());

        frame.pack();
        frame.setVisible(true);
        frame.setSize(new Dimension(UIConstants.ACCNT_MGMT_FRAME_WIDTH, UIConstants.ACCNT_MGMT_FRAME_HEIGHT));

        setLocation(frame);

        accountMgmtPane = frame;
    }

    static void hideAccountMgmtPane() {
        if (accountMgmtPane != null) {
            accountMgmtPane.setVisible(false);
        }
    }

    static void showAccountMgmtPane() {
        if (accountMgmtPane.isDisplayable()) {
			accountMgmtPane.setVisible(true);
			System.out.println("showing hidden mgmt pane");
		} else {
			System.out.println("creating account mgmt pane again");
        	createAndShowAccountMgmtPane();
		}
    }

    static void disposeAccountMgmtPane() {
		System.out.print("disposing accnt mgmt pane: ");
        if (accountMgmtPane != null) {
            accountMgmtPane.dispose();
        }
		System.out.println(accountMgmtPane);
    }

    static void createAndShowLogoPane() {

        JFrame frame = new JFrame("IotBroker");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.getContentPane().add(new LogoPane());

        frame.pack();
        frame.setVisible(true);
        frame.setSize(new Dimension(UIConstants.LOGO_FRAME_WIDTH, UIConstants.LOGO_FRAME_HEIGHT));
        frame.setResizable(false);

        setLocation(frame);

        logoPane = frame;
    }

    static void disposeLogoPane() {
        if (logoPane != null) {
            logoPane.dispose();
        }
    }
}
