package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AccountMgmtPane extends JPanel {

    JPanel accountsPane;

    AccountMgmtPane() {
        setBackground(UIConstants.APP_BG_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(UIHelper.createAppColorLabel("Please select account", 30));

        accountsPane = new JPanel();
        accountsPane.setBackground(UIConstants.APP_BG_COLOR);

        this.add(UIHelper.wrapInScrollAndBorderLayout(accountsPane, BorderLayout.CENTER));
        addAccounts();
//        JScrollPane scrollPane = new JScrollPane(accountsPane);
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


    private boolean accountChosen = false;

    private Map<Integer, Component[]> componentList = new HashMap<>();
    private void addAccounts() {
        final int accountCount = 3;
        final int parameterAlignment = SwingConstants.LEFT;

        accountsPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;

        Random r = new Random();

        for (int i = 0; i < accountCount; i++) {
            JPanel accountData = new JPanel();
            accountData.setLayout(new BoxLayout(accountData, BoxLayout.Y_AXIS));
            accountData.setBackground(UIConstants.APP_BG_COLOR);

            JLabel username = new JLabel("username " + i, parameterAlignment);
            username.setFont(UIConstants.REGULAR_BOLD_FONT);
            username.setBorder(BorderFactory.createEmptyBorder(5,5,2,5));

            JLabel clientId = new JLabel("<html>" + UIHelper.randomAlphaNumeric(r.nextInt(5) + 5) + i + "</html>", parameterAlignment);
            clientId.setFont(UIConstants.REGULAR_FONT);
            clientId.setBorder(BorderFactory.createEmptyBorder(1,5,3,5));

            JLabel hostPort = new JLabel("<html>" + UIHelper.randomAlphaNumeric(r.nextInt(10) + 12) + i + "</html>", parameterAlignment);
            hostPort.setFont(UIConstants.REGULAR_FONT);
            hostPort.setBorder(BorderFactory.createEmptyBorder(1,5,3,5));

            accountData.add(username);
            accountData.add(clientId);
            accountData.add(hostPort);

            accountData.setName(String.valueOf(i));

            accountData.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if (accountChosen)
                        return;
                    accountChosen = true;
                    String tag = ((Component)mouseEvent.getSource()).getName();
                    int t = Integer.valueOf(tag);
                    final Component[] row = componentList.get(Integer.valueOf(tag));
                    row[0].setBackground(UIConstants.SELECTION_COLOR);
                    row[1].setBackground(UIConstants.SELECTION_COLOR);

                    int delay = 200;
                    Timer timer = new Timer( delay, new ActionListener(){
                        @Override
                        public void actionPerformed( ActionEvent e ){
                            Main.createAndShowLogoPane();
                            row[0].setBackground(UIConstants.APP_BG_COLOR);
                            row[1].setBackground(UIConstants.APP_BG_COLOR);
                            accountChosen = false;
                            Main.hideAccountMgmtPane();
                        }
                    } );
                    timer.setRepeats( false );
                    timer.start();
                }
            });

            c.gridx = 0;
            c.gridy = i;
            c.weightx = 5;
            c.anchor = GridBagConstraints.NORTHWEST;

            accountsPane.add(accountData, c);

            JLabel deleteBtn = new JLabel(UIConstants.IC_TRASH, SwingConstants.CENTER);
            deleteBtn.setBackground(UIConstants.APP_BG_COLOR);
            deleteBtn.setOpaque(true);
            deleteBtn.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
//            deleteBtn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.cyan),BorderFactory.createEmptyBorder(5,5,5,5)));
            deleteBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {

                    JLabel btnClicked = (JLabel)arg0.getSource();
                    String index = btnClicked.getName();
                    System.out.println("Delete account clicked! " + index);
                    int i = Integer.valueOf(index);

                    Component[] row = componentList.get(i);
                    accountsPane.remove(row[0]);
                    accountsPane.remove(row[1]);

                    componentList.remove(i);
                    accountsPane.revalidate();
                    accountsPane.repaint();
                }
            });
            deleteBtn.setName(String.valueOf(i));

            c.gridx = 2;
            c.weightx = 0.1;
            c.anchor = GridBagConstraints.NORTHEAST;

            accountsPane.add(deleteBtn, c);

            Component[] row = new Component[2];
            row[0] = accountData;
            row[1] = deleteBtn;

            componentList.put(i, row);
        }

        JPanel emptySpace = new JPanel();
        emptySpace.setLayout(new BoxLayout(emptySpace, BoxLayout.Y_AXIS));
        emptySpace.setBackground(Color.white);
//        emptySpace.setBorder(BorderFactory.createLineBorder(Color.blue));
        emptySpace.add(Box.createRigidArea(new Dimension(50,5)));

        c.weighty = 1;
        c.gridy = accountCount;
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        accountsPane.add(emptySpace, c);
    }

    private void addAccountBtnClicked(MouseEvent event) {
        Main.createAndShowLogInPane();
        Main.hideAccountMgmtPane();
    }
}