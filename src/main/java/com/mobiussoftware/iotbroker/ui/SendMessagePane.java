package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class SendMessagePane extends JPanel {

    private JPanel settingsPane;
    private HintTextField contentTF;
    private HintTextField topicTF;
    private JComboBox<Integer> qosCB;
    private JCheckBox retainCB;
    private JCheckBox duplicateCB;

    SendMessagePane() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel txtLbl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtLbl1.setBackground(new Color(0,0,0,0));
        JLabel topicListLbl = new JLabel("send message:", SwingConstants.LEFT);
        topicListLbl.setFont(MainPane.TEXT_LABEL_FONT);
        txtLbl1.add(topicListLbl);

        this.add(txtLbl1);

//        JPanel pane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
//        pane.setBackground(Color.yellow);
//        pane.setMinimumSize(new Dimension(500, 373));
//        pane.setPreferredSize(pane.getMinimumSize());
//        pane.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        settingsPane = new JPanel();
        settingsPane.setBackground(Color.white);
//        settingsPane.setMinimumSize(new Dimension(410, 175));
//        settingsPane.setPreferredSize(new Dimension(426, 175));
        settingsPane.setMinimumSize(new Dimension(500, 373));
        settingsPane.setPreferredSize(settingsPane.getMinimumSize());
        settingsPane.setMaximumSize(new Dimension(5000, 1000));
        settingsPane.setBorder(BorderFactory.createLineBorder(Color.lightGray));

//        pane.add(settingsPane);
//        this.add(pane);
        this.add(settingsPane);

        JPanel sendLbl = new JPanel();
        JLabel sendBtn = new JLabel("Send");
        sendBtn.setBackground(MainPane.APP_COLOR);
        sendBtn.setOpaque(true);
        sendBtn.setForeground(Color.white);
        sendBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendBtn.setHorizontalAlignment(SwingConstants.CENTER);
        sendBtn.setPreferredSize(new Dimension(1000, 35));
        sendBtn.setMinimumSize(new Dimension(450, 35));
        sendBtn.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        sendBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                System.out.println("Send button clicked!");
            }
        });
        sendLbl.add(sendBtn);

        this.add(sendLbl);

        addMainPanelElements();
    }

    private void addMainPanelElements() {
        UIManager.put("ComboBox.background", new ColorUIResource(Color.white));
        UIManager.put("ComboBox.selectionBackground", MainPane.SELECTION_COLOR);
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.gray));

        Image tmp = MainPane.IC_SETTINGS.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        ImageIcon gearIcn = new ImageIcon(tmp);

        settingsPane.setLayout(new GridLayout(11, 2));

        JPanel lbl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lbl1.setBackground(Color.white);

        JLabel label = new JLabel("Content", gearIcn, SwingConstants.LEFT);
        label.setBorder(new EmptyBorder(0, 10, 0, 0));
        label.setFont(MainPane.REGULAR_FONT);
        label.setIconTextGap(10);

        lbl1.add(label);
        settingsPane.add(lbl1);

        JPanel val1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        val1.setBackground(Color.white);

        contentTF = new HintTextField("content", BorderFactory.createLineBorder(Color.lightGray));
        contentTF.setHorizontalAlignment(JTextField.RIGHT);
        contentTF.setFont(MainPane.REGULAR_FONT);
        contentTF.setMinimumSize(new Dimension(150, 28));
        contentTF.setPreferredSize(contentTF.getMinimumSize());

        val1.add(contentTF);
        settingsPane.add(val1);

        JPanel lbl2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lbl2.setBackground(MainPane.CONTRAST_LIST_COLOR);

        label = new JLabel("Topic", gearIcn, SwingConstants.LEFT);
        label.setBorder(new EmptyBorder(0, 10, 0, 0));
        label.setFont(MainPane.REGULAR_FONT);
        label.setIconTextGap(10);

        lbl2.add(label);
        settingsPane.add(lbl2);

        JPanel val2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        val2.setBackground(MainPane.CONTRAST_LIST_COLOR);

        topicTF = new HintTextField("topic", BorderFactory.createLineBorder(Color.lightGray));
        topicTF.setHorizontalAlignment(JTextField.RIGHT);
        topicTF.setFont(MainPane.REGULAR_FONT);
        topicTF.setMinimumSize(new Dimension(150, 28));
        topicTF.setPreferredSize(topicTF.getMinimumSize());

        val2.add(topicTF);
        settingsPane.add(val2);

        JPanel lbl3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lbl3.setBackground(Color.white);

        label = new JLabel("QoS", gearIcn, SwingConstants.LEFT);
        label.setBorder(new EmptyBorder(0, 10, 0, 0));
        label.setIconTextGap(10);
        label.setFont(MainPane.REGULAR_FONT);

        lbl3.add(label);
        settingsPane.add(lbl3);

        qosCB = new JComboBox<>(new Integer[] {0, 1, 2});

        JPanel val3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        val3.setBackground(Color.white);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        wrapper.setBackground(Color.yellow);
        wrapper.setMinimumSize(new Dimension(72, 24));
        wrapper.setPreferredSize(wrapper.getMinimumSize());
        wrapper.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        qosCB.setFont(MainPane.REGULAR_FONT);
        qosCB.setMinimumSize(new Dimension(70, 22));
        qosCB.setPreferredSize(qosCB.getMinimumSize());
        qosCB.setUI(CustomComboBoxUI.createUI(qosCB));

        BasicComboBoxRenderer renderer = (BasicComboBoxRenderer)qosCB.getRenderer();
        renderer.setBorder(new EmptyBorder(0,7,0,0));

        val3.add(wrapper);
        wrapper.add(qosCB);

        settingsPane.add(val3);

        JPanel lbl4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lbl4.setBackground(MainPane.CONTRAST_LIST_COLOR);

        label = new JLabel("Retain", gearIcn, SwingConstants.LEFT);
        label.setBorder(new EmptyBorder(0, 10, 0, 0));
        label.setFont(MainPane.REGULAR_FONT);
        label.setIconTextGap(10);

        lbl4.add(label);
        settingsPane.add(lbl4);

        JPanel val4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        val4.setBackground(MainPane.CONTRAST_LIST_COLOR);
        retainCB = new JCheckBox();
        retainCB.setBackground(MainPane.CONTRAST_LIST_COLOR);
        retainCB.setUI(new BasicCheckBoxUI() {
        });
        val4.add(retainCB);

        settingsPane.add(val4);

        JPanel lbl5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lbl5.setBackground(Color.white);

        label = new JLabel("Duplicate", gearIcn, SwingConstants.LEFT);
        label.setBorder(new EmptyBorder(0, 10, 0, 0));
        label.setFont(MainPane.REGULAR_FONT);
        label.setIconTextGap(10);

        lbl5.add(label);
        settingsPane.add(lbl5);

        JPanel val5 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        val5.setBackground(Color.white);
        duplicateCB = new JCheckBox();
        duplicateCB.setBackground(Color.white);
        val5.add(duplicateCB);

        settingsPane.add(val5);

        for (int i = 0; i < 12; i++) {
            JPanel emptyCell = new JPanel();
            emptyCell.setBackground(Color.white);
            settingsPane.add(emptyCell);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Image bgImage = MainPane.BG_IMAGE;
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
