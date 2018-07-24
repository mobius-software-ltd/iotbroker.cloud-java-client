package com.mobiussoftware.iotbroker.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicProgressBarUI;

import org.apache.log4j.Logger;

import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.mqtt.MqttClient;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;
import com.mobiussoftware.iotbroker.network.NetworkClient;

public class LoadingPane extends JPanel implements PropertyChangeListener, ClientListener {

	private static final long serialVersionUID = 3454494855396228813L;

	private static final Logger logger = Logger.getLogger(LoadingPane.class);

	JProgressBar progressBar;
	private Account account;
	private ConnectingTask connectingTask;
	private NetworkClient client;

	public LoadingPane(Account account) {
		this.account = account;
		drawUI();
	}

	private void drawUI() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(Box.createRigidArea(new Dimension(1, 15)));

		ImageIcon icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.LOGO_FILE_PATH);
		Image tmp = icon.getImage().getScaledInstance(180, 180, java.awt.Image.SCALE_SMOOTH);
		final ImageIcon logoIcn = new ImageIcon(tmp);

		icon = UIConstants.initImageIcon(UIConstants.IMAGES_PATH + UIConstants.IC_LOADING_FILE_PATH);
		tmp = icon.getImage().getScaledInstance(96, 18, java.awt.Image.SCALE_SMOOTH);
		final ImageIcon textIcn = new ImageIcon(tmp);

		JLabel logoLbl = new JLabel(logoIcn, SwingConstants.CENTER);
		logoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(logoLbl);

		this.add(Box.createRigidArea(new Dimension(1, 15)));

		JLabel textLbl = new JLabel(textIcn, SwingConstants.CENTER);
		textLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(textLbl);

		this.add(Box.createRigidArea(new Dimension(1, 25)));

		connectingTask = new ConnectingTask();
		connectingTask.addPropertyChangeListener(this);
		connectingTask.execute();

		progressBar = new JProgressBar();
		progressBar.setUI(new BasicProgressBarUI());
		progressBar.setString("");
		progressBar.setBackground(new Color(190, 200, 200, 50));
		progressBar.setForeground(UIConstants.APP_CONTRAST_COLOR);
		progressBar.setBorder(BorderFactory.createLineBorder(new Color(170, 180, 180, 200)));
		progressBar.setStringPainted(true);
		progressBar.setMinimumSize(new Dimension(250, 5));
		progressBar.setPreferredSize(progressBar.getMinimumSize());
		progressBar.setOpaque(true);
		progressBar.setMaximumSize(progressBar.getMinimumSize());

		this.add(progressBar);
	}

	class ConnectingTask extends SwingWorker<Void, Void> {
		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public Void doInBackground() {

			try {
				switch (account.getProtocol()) {
				case MQTT:
					initConnectMqtt();
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// handle exeption
			}

			Random random = new Random();
			int progress = 0;
			// Initialize progress property.
			setProgress(0);
			while (progress < 1000) {
				// Sleep for up to one second.
				try {
					Thread.sleep(random.nextInt(1000));
				} catch (InterruptedException ignore) {
				}
				// Make random progress.
				progress += random.nextInt(2) + 1;
				setProgress(Math.min(progress, 1000));
			}
			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			if (!isCancelled()) {
				System.out.println("done");
				Main.disposeLogoPane();
				Main.showAccountMgmtPane();
			}
		}
	}

	private void initConnectMqtt() throws Exception {
		client = new MqttClient(account);
		client.setListener(this);
		Main.updateCurrentClient(client);
		boolean channelCreated = client.createChannel();
		if (!channelCreated) {
			// TODO: dialog that error occurred
			System.out.println("mqtt connection failed");
			progressBar.setValue(0);
			return;
		}
	}

	private void closeConnection() {
		if (client != null) {
			System.out.println("closing connection...");
			client.closeChannel();
			client = null;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		}
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		Image bgImage = UIConstants.BG_IMAGE;
		graphics.drawImage(bgImage, 0, 0, null);
	}

	@Override
	public void messageSent(Message messageObj) {
	}

	@Override
	public void messageReceived(MQMessage message) {
		Main.mainPane.messageReceived(message);
	}

	@Override
	public void stateChanged(ConnectionState state) {
		System.out.println("LoadingPane state changed state=" + state.toString());
		switch (state) {
		case CHANNEL_ESTABLISHED:
			client.connect();
			break;
		case CHANNEL_FAILED:
			Main.disposeLogoPane();
			Main.showAccountMgmtPane();
			connectingTask.cancel(true);
			break;
		case CONNECTION_ESTABLISHED:
			Main.disposeLogoPane();
			try {
				Main.createAndShowMainPane(account);
			} catch (Exception e) {
				logger.error("Error occured while createAndShowMainPane from LoadingPanel");
				System.out.println("Error occured while createAndShowMainPane from LoadingPanel");
				e.printStackTrace();
			}
			connectingTask.cancel(true);
			break;
		case CONNECTION_LOST:
			// TODO: show "Connection closed by server" dialog
			Main.disposeMainPane();
			Main.showAccountMgmtPane();
			closeConnection();
			break;
		case CONNECTION_FAILED:
			// TODO: show "Connection failed" dialog
			closeConnection();
			break;

		case CHANNEL_CREATING:
		case CONNECTING:
		case NONE:
		default:
			break;
		}

	}
}
