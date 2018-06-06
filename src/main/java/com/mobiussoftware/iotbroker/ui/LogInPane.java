package com.mobiussoftware.iotbroker.ui;

import com.mobiussoftware.iotbroker.ui.elements.HintTextField;
import com.mobiussoftware.iotbroker.ui.elements.HintDialogTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class LogInPane extends JPanel {

    private final int columns = 2;

    private JPanel settingsPane;
    private JPanel regInfoPane;

    private JComboBox<String> protocolCB;
    private HintTextField usernameTF;
    private HintTextField passwordTF;
    private HintTextField clientIdTF;
    private HintTextField hostNameTF;
    private HintTextField portTF;
    private JCheckBox cleanSessionCB;
    private HintTextField keepAliveTF;
    private HintDialogTextField willTF;
    private HintTextField willTopicTF;
    private JCheckBox retainCB;
    private JComboBox<Integer> qosCB;

    LogInPane() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(UIHelper.createSmallBoldLabel("registration info:"));

        regInfoPane = new JPanel();
        regInfoPane.setBackground(UIConstants.APP_BG_COLOR);
        this.add(UIHelper.wrapInBorderLayout(regInfoPane, BorderLayout.PAGE_START));
        addRegInfoPaneElements();

        //settings:
        this.add(UIHelper.createSmallBoldLabel("settings:"));

        settingsPane = new JPanel();
        settingsPane.setBackground(UIConstants.APP_BG_COLOR);
        this.add(UIHelper.wrapInBorderLayout(settingsPane, BorderLayout.PAGE_START));
        addSettingsPaneElements();

        MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                loginBtnClicked(arg0);
            }
        };
        this.add(UIHelper.createButton("Log In", listener));

    }

    private void loginBtnClicked(MouseEvent event) {
        System.out.println("LogIn button clicked!");
        Main.createAndShowMainPane();
        Main.disposeLogInPane();
    }

    private Color rowColor(int rowNumber) {
        return rowNumber % 2 == 0 ? UIConstants.ROW_EVEN_COLOR : UIConstants.ROW_ODD_COLOR;
    }

    private void addRegInfoPaneElements() {
        Image tmp = UIConstants.IC_SETTINGS.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        final ImageIcon settingsIcn = new ImageIcon(tmp);
        tmp = UIConstants.IC_USERNAME.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        final ImageIcon usernameIcn = new ImageIcon(tmp);
        tmp = UIConstants.IC_PASSWORD.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        final ImageIcon passwordIcn = new ImageIcon(tmp);
        tmp = UIConstants.IC_CLIENT_ID.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        final ImageIcon clientIdIcn = new ImageIcon(tmp);
        tmp = UIConstants.IC_HOST_PORT.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        final ImageIcon hostPortIcn = new ImageIcon(tmp);

//        final Row[] rows = new Row[] {
//                UIHelper.createRow("Protocol:", settingsIcn, UIHelper.InputType.combobox, AppConstants.PROTOCOL_VALUES),
//                UIHelper.createRow("Username:", usernameIcn, UIHelper.InputType.textfield, "username"),
//                UIHelper.createRow("Password:", passwordIcn, UIHelper.InputType.textfield, "password"),
//                UIHelper.createRow("Client ID:", clientIdIcn, UIHelper.InputType.textfield, "client id"),
//                UIHelper.createRow("Server Host:", hostPortIcn, UIHelper.InputType.textfield, "server host"),
//                UIHelper.createRow("Port:", hostPortIcn, UIHelper.InputType.textfield, "port")};
//
//        regInfoPane.setLayout(new GridLayout(rows.length, columns));
//        final Dimension tfDimension = new Dimension(150, 28);

        final int rows = 6;
        regInfoPane.setLayout(new GridLayout(rows, columns));

        final Dimension tfDimension = new Dimension(150, 28);
        final int parameterLabelAlignment = SwingConstants.LEFT;
        int i = 0;

        regInfoPane.add(UIHelper.createParameterLabel("Protocol:", settingsIcn, parameterLabelAlignment, rowColor(i)));
        JPanel panel = UIHelper.createJComboBox(AppConstants.PROTOCOL_VALUES, new Dimension(72, 24));
        protocolCB = (JComboBox<String>)(panel.getComponent(0));
        regInfoPane.add(UIHelper.wrapInJPanel(panel, rowColor(i++)));

        regInfoPane.add(UIHelper.createParameterLabel("Username:", usernameIcn, parameterLabelAlignment, rowColor(i)));
        usernameTF = UIHelper.createHintTextField("username", tfDimension);
        regInfoPane.add(UIHelper.wrapInJPanel(usernameTF, rowColor(i++)));

        regInfoPane.add(UIHelper.createParameterLabel("Password:", passwordIcn, parameterLabelAlignment, rowColor(i)));
        passwordTF = UIHelper.createHintTextField("password", tfDimension);
        regInfoPane.add(UIHelper.wrapInJPanel(passwordTF, rowColor(i++)));

        regInfoPane.add(UIHelper.createParameterLabel("Client ID:", clientIdIcn, parameterLabelAlignment, rowColor(i)));
        clientIdTF = UIHelper.createHintTextField("client id", tfDimension);
        regInfoPane.add(UIHelper.wrapInJPanel(clientIdTF, rowColor(i++)));

        regInfoPane.add(UIHelper.createParameterLabel("Server Host:", hostPortIcn, parameterLabelAlignment, rowColor(i)));
        hostNameTF = UIHelper.createHintTextField("server host", tfDimension);
        regInfoPane.add(UIHelper.wrapInJPanel(hostNameTF, rowColor(i++)));

        regInfoPane.add(UIHelper.createParameterLabel("Port:", hostPortIcn, parameterLabelAlignment, rowColor(i)));
        portTF = UIHelper.createHintTextField("port", tfDimension);
        regInfoPane.add(UIHelper.wrapInJPanel(portTF, rowColor(i++)));


//        for (int i = 0; i < rows.length; i++) {
//            Row row = rows[i];
//            String text = row.getLabel();
//            Icon icon = row.getIcon();
//            Color color = i%2 == 0 ? UIConstants.ROW_EVEN_COLOR : UIConstants.ROW_ODD_COLOR;
//            int alignment = SwingConstants.LEFT;
//
//            regInfoPane.add(UIHelper.createParameterLabel(text, icon, alignment, color));
//
//            UIHelper.InputType inputType = row.getInputType();
//            JPanel jp = null;
//            switch (inputType) {
//                case textfield:
//                    jp = UIHelper.createHintTextField((String)row.getData(), tfDimension, color);
//                    break;
//                case checkbox:
//                    jp = UIHelper.createJCheckBox(color);
//                    break;
//                case combobox:
//                    jp = UIHelper.createJComboBox((Object[])row.getData(), new Dimension(95, 24), color);
//                    break;
//            }
//
//            regInfoPane.add(jp);
//        }
    }

    private void addSettingsPaneElements() {
        Image tmp = UIConstants.IC_SETTINGS.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
        final ImageIcon settingsIcn = new ImageIcon(tmp);
        tmp = UIConstants.IC_CLEAN_SESSION.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
        final ImageIcon cleanSessionIcn = new ImageIcon(tmp);
        tmp = UIConstants.IC_KEEP_ALIVE.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
        final ImageIcon keepAliveIcn = new ImageIcon(tmp);

//        final Row[] rows = new Row[]{
//                UIHelper.createRow("Clean Session:", cleanSessionIcn, UIHelper.InputType.checkbox, false),
//                UIHelper.createRow("Keepalive:", keepAliveIcn, UIHelper.InputType.textfield, "keepalive"),
//                UIHelper.createRow("Will:", settingsIcn, UIHelper.InputType.textfield, "will"),
//                UIHelper.createRow("Will topic:", settingsIcn, UIHelper.InputType.textfield, "will topic"),
//                UIHelper.createRow("Retain:", settingsIcn, UIHelper.InputType.checkbox, false),
//                UIHelper.createRow("QoS:", settingsIcn, UIHelper.InputType.combobox, AppConstants.QOS_VALUES)};
//        settingsPane.setLayout(new GridLayout(rows.length, columns));
//        final Dimension tfDimension = new Dimension(150, 28);

        final int rows = 6;
        settingsPane.setLayout(new GridLayout(rows, columns));

        final Dimension tfDimension = new Dimension(150, 28);
        final int parameterAlignment = SwingConstants.LEFT;
        int i = 0;

        settingsPane.add(UIHelper.createParameterLabel("Clean Session:", cleanSessionIcn, parameterAlignment, rowColor(i)));
        cleanSessionCB = UIHelper.createJCheckBox(rowColor(i));
        settingsPane.add(UIHelper.wrapInJPanel(cleanSessionCB, rowColor(i++)));

        settingsPane.add(UIHelper.createParameterLabel("Keepalive:", keepAliveIcn, parameterAlignment, rowColor(i)));
        keepAliveTF = UIHelper.createHintTextField("keepalive", tfDimension);
        settingsPane.add(UIHelper.wrapInJPanel(keepAliveTF, rowColor(i++)));

        settingsPane.add(UIHelper.createParameterLabel("Will:", settingsIcn, parameterAlignment, rowColor(i)));
        willTF = UIHelper.createTextArea("will", tfDimension);
        settingsPane.add(UIHelper.wrapInJPanel(willTF, rowColor(i++)));

        settingsPane.add(UIHelper.createParameterLabel("Will topic:", settingsIcn, parameterAlignment, rowColor(i)));
        willTopicTF = UIHelper.createHintTextField("will topic", tfDimension);
        settingsPane.add(UIHelper.wrapInJPanel(willTopicTF, rowColor(i++)));

        settingsPane.add(UIHelper.createParameterLabel("Retain:", settingsIcn, parameterAlignment, rowColor(i)));
        retainCB = UIHelper.createJCheckBox(rowColor(i));
        settingsPane.add(UIHelper.wrapInJPanel(retainCB, rowColor(i++)));

        settingsPane.add(UIHelper.createParameterLabel("QoS:", settingsIcn, parameterAlignment, rowColor(i)));
        JPanel panel = UIHelper.createJComboBox(AppConstants.QOS_VALUES, new Dimension(72, 24));
        qosCB = (JComboBox<Integer>)(panel.getComponent(0));
        settingsPane.add(UIHelper.wrapInJPanel(panel, rowColor(i++)));

//        for (int i = 0; i < rows.length; i++) {
//            Row row = rows[i];
//            String text = row.getLabel();
//            Icon icon = row.getIcon();
//            Color color = i % 2 == 0 ? UIConstants.ROW_EVEN_COLOR : UIConstants.ROW_ODD_COLOR;
//
//
//            settingsPane.add(UIHelper.createParameterLabel(text, icon, alignment, color));
//
//            UIHelper.InputType inputType = row.getInputType();
//            JPanel jp = null;
//            switch (inputType) {
//                case textfield:
//                    jp = UIHelper.createHintTextField((String) row.getData(), tfDimension, color);
//                    break;
//                case checkbox:
//                    jp = UIHelper.createJCheckBox(color);
//                    break;
//                case combobox:
//                    jp = UIHelper.createJComboBox((Object[]) row.getData(), new Dimension(72, 24), color);
//                    break;
//            }

//            settingsPane.add(jp);
//        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Image bgImage = UIConstants.BG_IMAGE;
        g.drawImage(bgImage, 0, 0, null);

        BufferedImage bimage = new BufferedImage(bgImage.getWidth(null), bgImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(bgImage, 0, 0, null);
        bGr.dispose();

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        TexturePaint paint = new TexturePaint(bimage, new Rectangle(0, 0, bgImage.getWidth(null), bgImage.getHeight(null)));
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
