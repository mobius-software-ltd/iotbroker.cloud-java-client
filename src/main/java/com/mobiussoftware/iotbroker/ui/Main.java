package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import java.awt.*;

public class Main {

    static final Dimension SCREEN_DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();

    static JFrame logInPane;
    static JFrame mainPane;
    static JFrame accountMgmt;

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
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

        JFrame frame = new JFrame("Accounts");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add(new AccountMgmtPane());

        frame.pack();
        frame.setVisible(true);
        frame.setSize(new Dimension(UIConstants.ACCNT_MGMT_FRAME_WIDTH, UIConstants.ACCNT_MGMT_FRAME_HEIGHT));

        setLocation(frame);

        accountMgmt = frame;
    }

    static void hideAccountMgmtPane() {
        if (accountMgmt != null) {
            accountMgmt.setVisible(false);
        }
    }

    static void showAccountMgmtPane() {
        if (accountMgmt != null) {
            accountMgmt.setVisible(true);
        }
    }

    static void disposeAccountMgmtPane() {
        if (accountMgmt != null) {
            accountMgmt.dispose();
        }
    }
}
