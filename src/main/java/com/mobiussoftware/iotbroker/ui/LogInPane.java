package com.mobiussoftware.iotbroker.ui;

/**
* Mobius Software LTD
* Copyright 2015-2018, Mobius Software LTD
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.ui.elements.HintDialogTextField;
import com.mobiussoftware.iotbroker.ui.elements.HintTextField;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class LogInPane extends JPanel
{

	private static final long serialVersionUID = 8294913343212905727L;
	final Dimension tfDimension = new Dimension(150, 28);
	final int parameterLabelAlignment = SwingConstants.LEFT;
	private final Logger logger = Logger.getLogger(getClass());
	private final int columns = 2;
	int regInfoColorCount = 0;
	private JPanel settingsPane;
	private JPanel regInfoPane;
	private JPanel settingsPaneWrapper;
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
	@SuppressWarnings("unused") private Protocol previousProtocolChoice;

	LogInPane()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(UIHelper.createSmallBoldLabel("registration info:"));

		regInfoPane = new JPanel();
		regInfoPane.setBackground(UIConstants.APP_BG_COLOR);
		this.add(UIHelper.wrapInBorderLayout(regInfoPane, BorderLayout.PAGE_START));
		
		// settings:
		settingsBlockLabel = UIHelper.createSmallBoldLabel("settings:");
		settingsBlockLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		this.add(settingsBlockLabel);

		settingsPane = new JPanel();
		settingsPane.setBackground(UIConstants.APP_BG_COLOR);
		
		settingsPaneWrapper = UIHelper.wrapInBorderLayout(settingsPane, BorderLayout.PAGE_START);
		this.add(settingsPaneWrapper);
		
		addRegInfoBlock();

		addSettingsPaneElements(true);

		MouseListener listener = new MouseAdapter()
		{
			@Override public void mouseClicked(MouseEvent arg0)
			{
				loginBtnClicked(arg0);
			}
		};
		this.add(UIHelper.createButton("Log In", listener));

	}

	private void loginBtnClicked(MouseEvent event)
	{
		logger.info("LogIn button clicked!");

		if (!UIHelper.validateTF(usernameTF, passwordTF, clientIdTF, hostNameTF) || !UIHelper.validateNumTF(portTF) || !UIHelper.validateNumTF(keepAliveTF))
			return;

		Account account = getAccountObject();
		try
		{
			DBInterface dbInterface = DBHelper.getInstance();
			dbInterface.storeAccount(account);
		}
		catch (Exception e)
		{
			// should not happen
			e.printStackTrace();
		}
		try
		{
			Main.createAndShowLoadingPane(account);
		}
		catch (Exception e)
		{
			System.out.println("Error occured while createAndShowMainPane from LoginPanel");
			e.printStackTrace();
		}
		Main.disposeLogInPane();
	}

	private Color rowColor(int rowNumber)
	{
		return rowNumber % 2 == 0 ? UIConstants.ROW_EVEN_COLOR : UIConstants.ROW_ODD_COLOR;
	}

	@SuppressWarnings("unchecked") private void addRegInfoBlock()
	{
		Image tmp = UIConstants.IC_SETTINGS.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		final ImageIcon settingsIcn = new ImageIcon(tmp);

		final int rows = 6;
		regInfoPane.setLayout(new GridLayout(rows, columns));

		regInfoPane.add(UIHelper.createParameterLabel("Protocol:", settingsIcn, parameterLabelAlignment, rowColor(0)));
		JPanel panel = UIHelper.createJComboBox(Protocol.values(), new Dimension(90, 24));
		protocolCB = (JComboBox<String>) (panel.getComponent(0));
		protocolCB.addItemListener(new ItemListener()
		{
			@Override public void itemStateChanged(ItemEvent itemEvent)
			{

				if (itemEvent.getStateChange() == ItemEvent.SELECTED)
				{
					// System.out.println("item " + itemEvent.getItem() + "
					// changed from " + previousProtocolChoice + ", class " +
					// itemEvent.getItem().getClass());

					Protocol currentProtocol = (Protocol) itemEvent.getItem();
					switch (currentProtocol)
					{
					case MQTT:
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
						addSettingsBlock();
						break;
					case AMQP:
						removeRegInfoPaneElements();
						addAmqpRegInfo();
						regInfoPane.revalidate();
						addSettingsBlock();
						break;
					case WEBSOCKETS:
						removeRegInfoPaneElements();
						addMqRegInfo();
						regInfoPane.revalidate();
						addSettingsBlock();
						break;
					default:
						break;
					}
				}
				else if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
					previousProtocolChoice = (Protocol) itemEvent.getItem();
			}
		});
		regInfoPane.add(UIHelper.wrapInJPanel(panel, rowColor(regInfoColorCount++)));

		addMqRegInfo();
	}

	private void addMqRegInfo()
	{
		addUserPasswordFields();
		addClientIdField();
		addServerPortFields();
		addSettingsPaneElements(true);
	}

	private void addSnRegInfo()
	{
		addClientIdField();
		addServerPortFields();
		addSettingsPaneElements(true);
	}

	private void addCoapRegInfo()
	{
		addSnRegInfo();
		addSettingsPaneElements(false);
	}

	private void addAmqpRegInfo()
	{
		addMqRegInfo();
		addSettingsPaneElements(false);
	}

	private void removeRegInfoPaneElements()
	{
		// System.out.println("regInfo componentCount is " +
		// regInfoPane.getComponentCount());
		for (int j = regInfoPane.getComponentCount() - 1; j >= 2; j--)
		{
			// System.out.println("removed " + regInfoPane.getComponent(j));
			regInfoPane.remove(2);
		}
		regInfoColorCount = 1;
	}

	private void addSettingsBlock()
	{
		if (this.getComponent(2) != settingsBlockLabel)
		{
			this.add(settingsBlockLabel, 2);
			this.add(settingsPaneWrapper, 3);			
		}
		
		this.revalidate();
		this.repaint();
	}

	private void addUserPasswordFields()
	{
		Image tmp = UIConstants.IC_USERNAME.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		final ImageIcon usernameIcn = new ImageIcon(tmp);
		tmp = UIConstants.IC_PASSWORD.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
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

	private void addClientIdField()
	{
		Image tmp = UIConstants.IC_CLIENT_ID.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		final ImageIcon clientIdIcn = new ImageIcon(tmp);

		clientIdLabel = UIHelper.createParameterLabel("Client ID:", clientIdIcn, parameterLabelAlignment, rowColor(regInfoColorCount));
		regInfoPane.add(clientIdLabel);
		clientIdTF = UIHelper.createHintTextField("client id", tfDimension);
		regInfoPane.add(UIHelper.wrapInJPanel(clientIdTF, rowColor(regInfoColorCount++)));
	}

	private void addServerPortFields()
	{
		Image tmp = UIConstants.IC_HOST_PORT.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		final ImageIcon hostPortIcn = new ImageIcon(tmp);

		regInfoPane.add(UIHelper.createParameterLabel("Server Host:", hostPortIcn, parameterLabelAlignment, rowColor(regInfoColorCount)));
		hostNameTF = UIHelper.createHintTextField("server host", tfDimension);
		regInfoPane.add(UIHelper.wrapInJPanel(hostNameTF, rowColor(regInfoColorCount++)));

		regInfoPane.add(UIHelper.createParameterLabel("Port:", hostPortIcn, parameterLabelAlignment, rowColor(regInfoColorCount)));
		portTF = UIHelper.createHintTextField("port", tfDimension);
		regInfoPane.add(UIHelper.wrapInJPanel(portTF, rowColor(regInfoColorCount++)));
	}

	@SuppressWarnings("unchecked") private void addSettingsPaneElements(Boolean showWill)
	{
		Image tmp = UIConstants.IC_SETTINGS.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		final ImageIcon settingsIcn = new ImageIcon(tmp);
		tmp = UIConstants.IC_CLEAN_SESSION.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		final ImageIcon cleanSessionIcn = new ImageIcon(tmp);
		tmp = UIConstants.IC_KEEP_ALIVE.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		final ImageIcon keepAliveIcn = new ImageIcon(tmp);

		// final Row[] rows = new Row[]{
		// UIHelper.createRow("Clean Session:", cleanSessionIcn,
		// UIHelper.InputType.checkbox, false),
		// UIHelper.createRow("Keepalive:", keepAliveIcn,
		// UIHelper.InputType.textfield, "keepalive"),
		// UIHelper.createRow("Will:", settingsIcn,
		// UIHelper.InputType.textfield, "will"),
		// UIHelper.createRow("Will topic:", settingsIcn,
		// UIHelper.InputType.textfield, "will topic"),
		// UIHelper.createRow("Retain:", settingsIcn,
		// UIHelper.InputType.checkbox, false),
		// UIHelper.createRow("QoS:", settingsIcn, UIHelper.InputType.combobox,
		// AppConstants.QOS_VALUES)};
		// settingsPane.setLayout(new GridLayout(rows.length, columns));
		// final Dimension tfDimension = new Dimension(150, 28);

		int rows = 6;
		//if(!showWill)
		//	rows=1;
			
		settingsPane.removeAll();
		settingsPane.setLayout(new GridLayout(rows, columns));

		final Dimension tfDimension = new Dimension(150, 28);
		final int parameterAlignment = SwingConstants.LEFT;
		int i = 0;

		if(showWill)
		{
			settingsPane.add(UIHelper.createParameterLabel("Clean Session:", cleanSessionIcn, parameterAlignment, rowColor(i)));
			cleanSessionCB = UIHelper.createJCheckBox(rowColor(i));
			settingsPane.add(UIHelper.wrapInJPanel(cleanSessionCB, rowColor(i++)));
		}		
		
		settingsPane.add(UIHelper.createParameterLabel("Keepalive:", keepAliveIcn, parameterAlignment, rowColor(i)));
		keepAliveTF = UIHelper.createHintTextField("keepalive", tfDimension);
		settingsPane.add(UIHelper.wrapInJPanel(keepAliveTF, rowColor(i++)));

		if(showWill)
		{
			settingsPane.add(UIHelper.createParameterLabel("Will topic:", settingsIcn, parameterAlignment, rowColor(i)));
			willTopicTF = UIHelper.createHintTextField("will topic", tfDimension);
			settingsPane.add(UIHelper.wrapInJPanel(willTopicTF, rowColor(i++)));
		
			settingsPane.add(UIHelper.createParameterLabel("Will:", settingsIcn, parameterAlignment, rowColor(i)));
			willTF = UIHelper.createTextArea("will", tfDimension);
			settingsPane.add(UIHelper.wrapInJPanel(willTF, rowColor(i++)));

			settingsPane.add(UIHelper.createParameterLabel("Retain:", settingsIcn, parameterAlignment, rowColor(i)));
			retainCB = UIHelper.createJCheckBox(rowColor(i));
			settingsPane.add(UIHelper.wrapInJPanel(retainCB, rowColor(i++)));

			settingsPane.add(UIHelper.createParameterLabel("QoS:", settingsIcn, parameterAlignment, rowColor(i)));
			JPanel panel = UIHelper.createJComboBox(AppConstants.QOS_VALUES, new Dimension(72, 24));
			qosCB = (JComboBox<Integer>) (panel.getComponent(0));
			settingsPane.add(UIHelper.wrapInJPanel(panel, rowColor(i++)));
		}
		else
		{
			for(int j=0;j<10;j++)
			{
				settingsPane.add(UIHelper.createParameterLabel("", null, parameterAlignment, rowColor(0)));				
			}
		}
		// for (int i = 0; i < rows.length; i++) {
		// Row row = rows[i];
		// String text = row.getLabel();
		// Icon icon = row.getIcon();
		// Color color = i % 2 == 0 ? UIConstants.ROW_EVEN_COLOR :
		// UIConstants.ROW_ODD_COLOR;
		//
		//
		// settingsPane.add(UIHelper.createParameterLabel(text, icon, alignment,
		// color));
		//
		// UIHelper.InputType inputType = row.getInputType();
		// JPanel jp = null;
		// switch (inputType) {
		// case textfield:
		// jp = UIHelper.createHintTextField((String) row.getData(),
		// tfDimension, color);
		// break;
		// case checkbox:
		// jp = UIHelper.createJCheckBox(color);
		// break;
		// case combobox:
		// jp = UIHelper.createJComboBox((Object[]) row.getData(), new
		// Dimension(72, 24), color);
		// break;
		// }

		// settingsPane.add(jp);
		// }
	}

	private Account getAccountObject()
			throws NumberFormatException
	{
		Protocol protocol = (Protocol) protocolCB.getSelectedItem();
		String username = usernameTF.getText();
		String password = passwordTF.getText();
		String clientId = clientIdTF.getText();
		String hostName = hostNameTF.getText();
		int port = Integer.valueOf(portTF.getText());
		boolean cleanSesssion = cleanSessionCB.isSelected();
		int keepAlive = keepAliveTF.getText().equals("") ? 0 : Integer.valueOf(keepAliveTF.getText());
		String will = willTF.getText();
		String willTopic = willTopicTF.getText();
		boolean retain = retainCB.isSelected();
		int qos = qosCB.getSelectedIndex();

		Account account = new Account(protocol, username, password, clientId, hostName, port, cleanSesssion, keepAlive, will, willTopic, retain, qos, true);

		return account;
	}

	@Override protected void paintComponent(Graphics g)
	{
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
