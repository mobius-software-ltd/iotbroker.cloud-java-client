package com.mobiussoftware.iotbroker.ui;

import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.mqtt.MqttClient;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

public class LogoPane extends JPanel implements PropertyChangeListener, ClientListener {

    JProgressBar progressBar;
    private Account account;
    private ConnectingTask connectingTask;

    public LogoPane(Account account) {
        this.account = account;
        drawUI();
    }

    private void drawUI() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(Box.createRigidArea(new Dimension(1, 15)));

		ImageIcon icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.LOGO_FILE_PATH);
		Image tmp = icon.getImage().getScaledInstance(180, 180,  java.awt.Image.SCALE_SMOOTH);
		final ImageIcon logoIcn = new ImageIcon(tmp);

		icon = new ImageIcon(UIConstants.IMAGE_RES_PATH + UIConstants.IC_LOADING_FILE_PATH);
		tmp = icon.getImage().getScaledInstance(96, 18,  java.awt.Image.SCALE_SMOOTH);
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
		progressBar.setMinimumSize(new Dimension(250,5));
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
						MqttClient client = new MqttClient(account, LogoPane.this);
						client.createChannel();
						System.out.println("LogoPane createChannel OK");
						Main.setClient(client);
						break;
					default:
						break;
				}
	      	} catch(Exception e) {
	      		//handle exeption
	      	}

        	 
            Random random = new Random();
            int progress = 0;
            //Initialize progress property.
            setProgress(0);
            while (progress < 1000) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException ignore) {}
                //Make random progress.
                progress += random.nextInt(2)+1;
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
	public void messageSent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageReceived(MessageType type) {
		// TODO Auto-generated method stub
		System.out.println("LogoPanel messageReceived");
	}

	@Override
	public void stateChanged(ConnectionState state) {
		System.out.println("LogoPane state changed state=" + state.toString());
		switch (state) {
		case CHANNEL_ESTABLISHED:
			Main.disposeLogoPane();
			
			//System.out.println("LogoPane after dispose "+ Main.getClient().getConnectionState());
			
			try {
				Main.createAndShowMainPane(account);
			}catch(Exception e) 
			{
				System.out.println("Error occured while createAndShowMainPane from LogoPanel");
				e.printStackTrace();
			}
			
			connectingTask.cancel(true);
			break;
		case CHANNEL_FAILED:
			Main.disposeLogoPane();
			Main.showAccountMgmtPane();
			connectingTask.cancel(true);
			break;
		default:
			break;
		}
		
	}
}
