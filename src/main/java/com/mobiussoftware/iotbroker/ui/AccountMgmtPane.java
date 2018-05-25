package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AccountMgmtPane extends JPanel {
    AccountMgmtPane() {
        setBackground(UIConstants.APP_BG_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(UIHelper.createAppColorLabel("Please select account", 30));

        JPanel accounts = new JPanel();
        accounts.setBackground(UIConstants.APP_BG_COLOR);

        this.add(UIHelper.wrapInScrollAndBorderLayout(accounts, BorderLayout.CENTER));
        addAccounts();
//        JScrollPane scrollPane = new JScrollPane(accounts);
//        JPanel wrapper = new JPanel(new BorderLayout());
//        wrapper.add(scrollPane, BorderLayout.CENTER);
//        this.add(wrapper);


        MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                addAccountBtnClicked(arg0);
            }
        };
        this.add(UIHelper.createButton("Add new account", 40, listener));
    }

    private void addAccounts() {

    }

    private void addAccountBtnClicked(MouseEvent event) {
        Main.createAndShowLogInPane();

    }
}
