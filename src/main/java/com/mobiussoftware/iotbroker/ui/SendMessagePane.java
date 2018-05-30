package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SendMessagePane extends JPanel {

    private JPanel settingsPane;
    private JTextField contentTF;
    private JTextField topicTF;
    private JComboBox<Integer> qosCB;
    private JCheckBox retainCB;
    private JCheckBox duplicateCB;

    private JPanel progressBarSpace;
    private JProgressBar progressBar;

    SendMessagePane() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        progressBarSpace = UIHelper.createProgressBarSpace(5);
        this.add(progressBarSpace);

        settingsPane = new JPanel();
        settingsPane.setBackground(UIConstants.APP_BG_COLOR);
        MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                sendMessageAction();
            }
        };

        this.add(UIHelper.createSmallBoldLabel("send message:"));
        this.add(UIHelper.wrapInBorderLayout(settingsPane, BorderLayout.PAGE_START));
        this.add(UIHelper.createButton("Send", listener));

        addSettingsPaneElements();
    }

    private void addSettingsPaneElements() {
        UIManager.put("ComboBox.background", new ColorUIResource(Color.white));
        UIManager.put("ComboBox.selectionBackground", UIConstants.SELECTION_COLOR);
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.gray));

        Image tmp = UIConstants.IC_SETTINGS.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        ImageIcon settingsIcon = new ImageIcon(tmp);

        final int rows = 5;
        final int columns = 2;
        final int parameterAlignment = SwingConstants.LEFT;
        final Color evenColor = UIConstants.ROW_EVEN_COLOR;
        final Color oddColor = UIConstants.ROW_ODD_COLOR;

        settingsPane.setLayout(new GridLayout(rows, columns));

        JPanel panel;
        settingsPane.add( UIHelper.createParameterLabel("Content", settingsIcon, parameterAlignment, evenColor));
        panel = UIHelper.createHintTextField("content", new Dimension(150, 28), evenColor );
        contentTF = (JTextField)panel.getComponent(0);
        settingsPane.add(panel);
        settingsPane.add(UIHelper.createParameterLabel("Topic", settingsIcon, parameterAlignment, oddColor));
        panel = UIHelper.createHintTextField("topic", new Dimension(150, 28), oddColor );
        topicTF = (JTextField)panel.getComponent(0);
        settingsPane.add(panel);
        settingsPane.add(UIHelper.createParameterLabel("QoS", settingsIcon, parameterAlignment, evenColor));
        panel = UIHelper.createJComboBox(AppConstants.QOS_VALUES, new Dimension(70, 22), evenColor);
        qosCB = (JComboBox)((JPanel)panel.getComponent(0)).getComponent(0);
        settingsPane.add(panel);
        settingsPane.add(UIHelper.createParameterLabel("Retain", settingsIcon, parameterAlignment, oddColor));
        panel = UIHelper.createJCheckBox(oddColor);
        retainCB = (JCheckBox) panel.getComponent(0);
        settingsPane.add(panel);
        settingsPane.add(UIHelper.createParameterLabel("Duplicate", settingsIcon, parameterAlignment, evenColor));
        panel = UIHelper.createJCheckBox(evenColor);
        duplicateCB = (JCheckBox) panel.getComponent(0);
        settingsPane.add(panel);
    }

    private void sendMessageAction() {
        if (validateTextFieldsFilled()) {

            addProgressBar();

            SendTask task = new SendTask(contentTF.getText(), topicTF.getText(), qosCB.getSelectedIndex(), retainCB.isSelected(), duplicateCB.isSelected());
            task.addPropertyChangeListener(propertyChangeListener());
            task.execute();
        }
    }

    private boolean validateTextFieldsFilled() {

        String content = contentTF.getText();
        if (content == null || content.equals("")) {
            contentTF.setBorder(BorderFactory.createLineBorder(Color.red));
            contentTF.requestFocusInWindow();
            contentTF.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {
                    contentTF.setBorder(BorderFactory.createLineBorder(Color.lightGray));
                    contentTF.removeKeyListener(this);
                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {
                }

                @Override
                public void keyReleased(KeyEvent keyEvent) {
                }
            });
            return false;
        }

        String topic = topicTF.getText();
        if (topic == null || topic.equals("")) {
            topicTF.setBorder(BorderFactory.createLineBorder(Color.red));
            topicTF.requestFocusInWindow();
            topicTF.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {
                    topicTF.setBorder(BorderFactory.createLineBorder(Color.lightGray));
                    topicTF.removeKeyListener(this);
                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {
                }

                @Override
                public void keyReleased(KeyEvent keyEvent) {
                }
            });
            return false;
        }
        return true;
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

            contentTF.setText("");
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
