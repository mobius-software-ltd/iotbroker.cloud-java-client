package com.mobiussoftware.iotbroker.ui;

import com.mobiussoftware.iotbroker.ui.elements.HintDialogTextField;
import com.mobiussoftware.iotbroker.ui.elements.HintTextField;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SendMessagePane extends JPanel {

    private JPanel settingsPane;
    private HintDialogTextField contentTF;
    private HintTextField topicTF;
    private JComboBox<Integer> qosCB;
    private JCheckBox retainCB;
    private JCheckBox duplicateCB;

    private JPanel progressBarSpace;
    private JProgressBar progressBar;

	private MouseListener sendMsgBtnListener;
	private final JPanel sendMsgBtn;

    SendMessagePane() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        progressBarSpace = UIHelper.createProgressBarSpace(5);
        this.add(progressBarSpace);

        settingsPane = new JPanel();
        settingsPane.setBackground(UIConstants.APP_BG_COLOR);

		sendMsgBtnListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				arg0.getComponent().removeMouseListener(this);
				System.out.println("mouse listener removed");
				sendMessageAction();
			}
		};
		sendMsgBtn = UIHelper.createButton("Send", sendMsgBtnListener);

        this.add(UIHelper.createSmallBoldLabel("send message:"));
        this.add(UIHelper.wrapInBorderLayout(settingsPane, BorderLayout.PAGE_START));
        this.add(sendMsgBtn);

        addSettingsPaneElements();
    }
    private Color rowColor(int rowNumber) {
        return rowNumber % 2 == 0 ? UIConstants.ROW_EVEN_COLOR : UIConstants.ROW_ODD_COLOR;
    }

    private void addSettingsPaneElements() {
        UIManager.put("ComboBox.background", new ColorUIResource(Color.white));
        UIManager.put("ComboBox.selectionBackground", UIConstants.SELECTION_COLOR);
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.gray));

        Image tmp = UIConstants.IC_SETTINGS.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        ImageIcon settingsIcon = new ImageIcon(tmp);

        final int rows = 5;
        final int columns = 2;
        settingsPane.setLayout(new GridLayout(rows, columns));

        final int parameterAlignment = SwingConstants.LEFT;
        int i = 0;

        settingsPane.add(UIHelper.createParameterLabel("Topic", settingsIcon, parameterAlignment, rowColor(i)));
        topicTF = UIHelper.createHintTextField("topic", new Dimension(150, 28));
        settingsPane.add(UIHelper.wrapInJPanel(topicTF, rowColor(i++)));

        settingsPane.add( UIHelper.createParameterLabel("Content", settingsIcon, parameterAlignment, rowColor(i)));
        contentTF = UIHelper.createTextArea("content", new Dimension(150, 28));
        settingsPane.add(UIHelper.wrapInJPanel(contentTF, rowColor(i++)));

        settingsPane.add(UIHelper.createParameterLabel("QoS", settingsIcon, parameterAlignment, rowColor(i)));
        JPanel panel = UIHelper.createJComboBox(AppConstants.QOS_VALUES, new Dimension(70, 22));
        qosCB = (JComboBox)(panel.getComponent(0));
        settingsPane.add(UIHelper.wrapInJPanel(panel, rowColor(i++)));

        settingsPane.add(UIHelper.createParameterLabel("Retain", settingsIcon, parameterAlignment, rowColor(i)));
        retainCB = UIHelper.createJCheckBox(rowColor(i));
        settingsPane.add(UIHelper.wrapInJPanel(retainCB, rowColor(i++)));

        settingsPane.add(UIHelper.createParameterLabel("Duplicate", settingsIcon, parameterAlignment, rowColor(i)));
        duplicateCB = UIHelper.createJCheckBox(rowColor(i));
//        duplicateCB.requestFocusInWindow();
        settingsPane.add(UIHelper.wrapInJPanel(duplicateCB, rowColor(i++)));
    }

    private void sendMessageAction() {
        if (UIHelper.validateTF(topicTF) && UIHelper.validateDialogTF(contentTF)) {

            addProgressBar();

            SendTask task = new SendTask(contentTF.getText(), topicTF.getText(), qosCB.getSelectedIndex(), retainCB.isSelected(), duplicateCB.isSelected());
            task.addPropertyChangeListener(propertyChangeListener());
            task.execute();
        } else {
        	sendMsgBtn.addMouseListener(sendMsgBtnListener);
		}
    }

    private PropertyChangeListener propertyChangeListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == "progress") {
                    int progress = (Integer) evt.getNewValue();
                    progressBar.setValue(progress);
                }
            }
        };
    }

    private void addProgressBar() {
        progressBar = UIHelper.createProgressBar();
        if (progressBarSpace.getComponents().length > 0) {
            for (Component c:progressBarSpace.getComponents()) {
                progressBarSpace.remove(c);
            }
        }
        progressBarSpace.add(progressBar);
        progressBar.revalidate();
    }

    private void removeProgressBar() {
        progressBarSpace.remove(progressBar);
        SendMessagePane.this.revalidate();
        SendMessagePane.this.repaint();
    }

    class SendTask extends NetworkTask<Void, Void> {
        private String content;
        private String topic;
        private int qos;
        private boolean retain;
        private boolean duplicate;

        public SendTask(String content, String topic, int qos, boolean retain, boolean duplicate) {
            this.content = content;
            this.topic = topic;
            this.qos = qos;
            this.retain = retain;
            this.duplicate = duplicate;
        }

        @Override
        public Void doInBackground() {
            super.doInBackground();
            //sent to server logic!!

            return null;
        }

        @Override
        protected void done() {
            System.out.println("Sent!");
            removeProgressBar();

            sendMsgBtn.addMouseListener(sendMsgBtnListener);

            contentTF.clearText();
            topicTF.setText("");
            qosCB.setSelectedIndex(0);
            retainCB.setSelected(false);
            duplicateCB.setSelected(false);
        }
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
