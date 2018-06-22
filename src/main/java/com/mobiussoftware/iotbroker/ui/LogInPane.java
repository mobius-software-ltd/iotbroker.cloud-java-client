package com.mobiussoftware.iotbroker.ui;

import com.mobiussoftware.iotbroker.ui.elements.HintTextField;
import com.mobiussoftware.iotbroker.ui.elements.HintDialogTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

//import static com.mobiussoftware.iotbroker.ui.AppConstants.PROTOCOL_VALUES;

public class LogInPane extends JPanel {

    private final int columns = 2;

    private JPanel settingsPane;
    private JPanel regInfoPane;
	private JPanel settingsPaneWrapper;
	private JPanel regInfoPaneWrapper;

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

    private JPanel settingsBlockLabel;

    private JPanel usernameLabel;
    private JPanel passwordLabel;
    private JPanel clientIdLabel;

    private Protocol previousProtocolChoice;

    LogInPane() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(UIHelper.createSmallBoldLabel("registration info:"));

        regInfoPane = new JPanel();
        regInfoPane.setBackground(UIConstants.APP_BG_COLOR);
        this.add(UIHelper.wrapInBorderLayout(regInfoPane, BorderLayout.PAGE_START));
        addRegInfoBlock();

        //settings:
		settingsPane = new JPanel();
		settingsPane.setBackground(UIConstants.APP_BG_COLOR);

		settingsBlockLabel = UIHelper.createSmallBoldLabel("settings:");
		this.add(settingsBlockLabel);

		settingsPaneWrapper = UIHelper.wrapInBorderLayout(settingsPane, BorderLayout.PAGE_START);
		this.add(settingsPaneWrapper);
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

	final Dimension tfDimension = new Dimension(150, 28);
	final int parameterLabelAlignment = SwingConstants.LEFT;
	int regInfoColorCount = 0;

    private void addRegInfoBlock() {
        Image tmp = UIConstants.IC_SETTINGS.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        final ImageIcon settingsIcn = new ImageIcon(tmp);

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

        regInfoPane.add(UIHelper.createParameterLabel("Protocol:", settingsIcn, parameterLabelAlignment, rowColor(0)));
        JPanel panel = UIHelper.createJComboBox(Protocol.values(), new Dimension(90, 24));
        protocolCB = (JComboBox<String>)(panel.getComponent(0));
        protocolCB.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent itemEvent) {

				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					System.out.println("item " + itemEvent.getItem() + " changed from " + previousProtocolChoice + ", class " + itemEvent.getItem().getClass());

					Protocol currentProtocol = (Protocol) itemEvent.getItem();
					int  i = 1;
					switch (currentProtocol) {
						case MQTT:
//							switch (previousProtocolChoice) {
//								case CoAP:
//									regInfoPane.add(clientIdLabel, 2);
//									regInfoPane.add(UIHelper.wrapInJPanel(clientIdTF, rowColor(i++)), 3);
//								case MQTTSN:
//								case AMQP:
//									regInfoPane.add(usernameLabel, 2);
//									regInfoPane.add(UIHelper.wrapInJPanel(usernameTF, rowColor(i++)), 3);
//									regInfoPane.add(passwordLabel, 4);
//									regInfoPane.add(UIHelper.wrapInJPanel(passwordTF, rowColor(i++)), 5);
//									break;
//							}
							removeRegInfoPaneElements();
							addMqRegInfo();
							regInfoPane.revalidate();
							addSettingsBlock();
							break;
						case MQTTSN:
							removeRegInfoPaneElements();
							addSnRegInfo();
							regInfoPane.revalidate();
							addSettingsBlock();
							break;
						case CoAP:
							removeRegInfoPaneElements();
							addCoapRegInfo();
							regInfoPane.revalidate();
							removeSettingsBlock();
							break;
						case AMQP:
							removeRegInfoPaneElements();
							addAmqpRegInfo();
							regInfoPane.revalidate();
							removeSettingsBlock();
							break;
					}
				} else if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
					previousProtocolChoice = (Protocol) itemEvent.getItem();
			}
		});
        regInfoPane.add(UIHelper.wrapInJPanel(panel, rowColor(regInfoColorCount++)));

        addMqRegInfo();
    }

    private void addMqRegInfo() {
		regInfoPane.setLayout(new GridLayout(6, columns));
		addUserPasswordFields();
		addClientIdField();
		addServerPortFileds();
	}

    private void addSnRegInfo() {
		regInfoPane.setLayout(new GridLayout(4, columns));
		addClientIdField();
		addServerPortFileds();
	}

    private void addCoapRegInfo() {
		regInfoPane.setLayout(new GridLayout(3, columns));
		addServerPortFileds();
	}

    private void addAmqpRegInfo() {
		addSnRegInfo();
	}

	private void removeRegInfoPaneElements() {
//		System.out.println("regInfo componentCount is " + regInfoPane.getComponentCount());
		for (int j = regInfoPane.getComponentCount() - 1; j >= 2; j--) {
//			System.out.println("removed " + regInfoPane.getComponent(j));
			regInfoPane.remove(2);
		}
		regInfoColorCount = 1;
	}

	private void addSettingsBlock() {
		if (this.getComponent(2) != settingsBlockLabel) {
			this.add(settingsBlockLabel, 2);
			this.add(settingsPaneWrapper, 3);
			this.revalidate();
		}
	}

	private void removeSettingsBlock() {
		if (this.getComponentCount() > 3 && this.getComponent(3) == settingsPaneWrapper) {
			this.remove(settingsPaneWrapper);
			this.remove(settingsBlockLabel);
			this.revalidate();
		}
	}

    private void addUserPasswordFields() {
		Image tmp = UIConstants.IC_USERNAME.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
		final ImageIcon usernameIcn = new ImageIcon(tmp);
		tmp = UIConstants.IC_PASSWORD.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
		final ImageIcon passwordIcn = new ImageIcon(tmp);

		usernameLabel = UIHelper.createParameterLabel("Username:", usernameIcn, parameterLabelAlignment, rowColor(regInfoColorCount));
		regInfoPane.add(usernameLabel);
		usernameTF = UIHelper.createHintTextField("username", tfDimension);
		regInfoPane.add(UIHelper.wrapInJPanel(usernameTF, rowColor(regInfoColorCount++)));

		passwordLabel = UIHelper.createParameterLabel("Password:", passwordIcn, parameterLabelAlignment, rowColor(regInfoColorCount));
		regInfoPane.add(passwordLabel);
		passwordTF = UIHelper.createHintTextField("password", tfDimension);
		regInfoPane.add(UIHelper.wrapInJPanel(passwordTF, rowColor(regInfoColorCount++)));
	}

	private void addClientIdField() {
		Image tmp = UIConstants.IC_CLIENT_ID.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
		final ImageIcon clientIdIcn = new ImageIcon(tmp);

		clientIdLabel = UIHelper.createParameterLabel("Client ID:", clientIdIcn, parameterLabelAlignment, rowColor(regInfoColorCount));
		regInfoPane.add(clientIdLabel);
		clientIdTF = UIHelper.createHintTextField("client id", tfDimension);
		regInfoPane.add(UIHelper.wrapInJPanel(clientIdTF, rowColor(regInfoColorCount++)));
	}

	private void addServerPortFileds() {
		Image tmp = UIConstants.IC_HOST_PORT.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
		final ImageIcon hostPortIcn = new ImageIcon(tmp);

		regInfoPane.add(UIHelper.createParameterLabel("Server Host:", hostPortIcn, parameterLabelAlignment, rowColor(regInfoColorCount)));
		hostNameTF = UIHelper.createHintTextField("server host", tfDimension);
		regInfoPane.add(UIHelper.wrapInJPanel(hostNameTF, rowColor(regInfoColorCount++)));

		regInfoPane.add(UIHelper.createParameterLabel("Port:", hostPortIcn, parameterLabelAlignment, rowColor(regInfoColorCount)));
		portTF = UIHelper.createHintTextField("port", tfDimension);
		regInfoPane.add(UIHelper.wrapInJPanel(portTF, rowColor(regInfoColorCount++)));
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
