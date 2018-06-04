package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class MessagesListPane extends JPanel {

    static final int COLORED_LABEL_WIDTH = 80;
    static final int COLORED_LABEL_HEIGHT = 40;
    private final int msgCount = 50;

    MessagesListPane() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel txtLbl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtLbl1.setBackground(new Color(0,0,0,0));
        JLabel topicListLbl = new JLabel("messages list:", SwingConstants.LEFT);
        topicListLbl.setFont(UIConstants.TEXT_LABEL_FONT);
        txtLbl1.add(topicListLbl);

        this.add(txtLbl1);

        JPanel messagesPane = new JPanel();
        messagesPane.setBackground(Color.white);
        messagesPane.setMinimumSize(new Dimension(410, 280));
        messagesPane.setPreferredSize(new Dimension(410, msgCount * 200));
//        messagesPane.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        JScrollPane scrollPane = new JScrollPane(messagesPane);
        scrollPane.setPreferredSize(new Dimension(450, 1000));
        scrollPane.setMinimumSize(new Dimension(450, 280));
        scrollPane.setMaximumSize(new Dimension(450, 1000));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);

        this.add(wrapper);

        addMessagesPaneElements(messagesPane);
    }

    private void addMessagesPaneElements(final JPanel parent) {
        parent.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();



        Random r = new Random();

        for (int i = 0; i < msgCount; i++) {
            Color bgColor = i%2 == 0 ? Color.white : UIConstants.ROW_ODD_COLOR;

            JPanel messageData = new JPanel();
            messageData.setLayout(new BoxLayout(messageData, BoxLayout.Y_AXIS));
            messageData.setBackground(bgColor);
//            messageData.setBorder(BorderFactory.createLineBorder(Color.blue));

            JLabel topic = new JLabel("topic " + i, SwingConstants.LEFT);
            topic.setFont(UIConstants.REGULAR_BOLD_FONT);
            topic.setBorder(BorderFactory.createEmptyBorder(5,5,2,5));

            JLabel text = new JLabel("<html>message payload " + UIHelper.randomAlphaNumeric(r.nextInt(512) + 64) + i + "</html>", SwingConstants.LEFT);
            text.setFont(UIConstants.REGULAR_FONT);
            text.setBorder(BorderFactory.createEmptyBorder(1,5,3,5));

            messageData.add(topic);
            messageData.add(text);

            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 10;
            c.anchor = GridBagConstraints.NORTHWEST;

            parent.add(messageData, c);

            JPanel extraData = new TwoColorRoundedRect();
            extraData.setBackground(bgColor);
            extraData.setMinimumSize(new Dimension(COLORED_LABEL_WIDTH, 1));
            extraData.setPreferredSize(extraData.getMinimumSize());
            extraData.setLayout(new BoxLayout(extraData, BoxLayout.Y_AXIS));

            JLabel direction = new JLabel(r.nextInt(2) == 0 ? "in" : "out", SwingConstants.CENTER);
            direction.setFont(UIConstants.REGULAR_FONT);
            direction.setForeground(Color.white);
            direction.setAlignmentX(Component.CENTER_ALIGNMENT);
//            direction.setMaximumSize(new Dimension((int)extraData.getMinimumSize().getWidth(), (int)extraData.getMinimumSize().getHeight()/2));
//            direction.setMaximumSize(new Dimension(extraData.getWidth(), extraData.getHeight()/2));
            direction.setBorder(BorderFactory.createEmptyBorder(extraData.getHeight()/2, 0,0,0));

            JLabel qos = new JLabel("QoS:"+ r.nextInt(3), SwingConstants.CENTER);
            qos.setBorder(BorderFactory.createEmptyBorder(3, 0,0,0));
            qos.setFont(UIConstants.REGULAR_FONT);
            qos.setAlignmentX(Component.CENTER_ALIGNMENT);

            extraData.add(direction);
//            extraData.add(Box.createRigidArea(new Dimension(1,6)));
            extraData.add(qos);

            c.fill = GridBagConstraints.VERTICAL;
            c.gridx = 1;
            c.weightx = 0;
            c.anchor = GridBagConstraints.NORTHEAST;

            parent.add(extraData, c);

            c.gridx = 2;
            c.weightx = 0;
            c.anchor = GridBagConstraints.NORTHEAST;

            parent.add(Box.createRigidArea(new Dimension(5,5)), c);

        }

        c.weighty = 1;
        c.gridy = msgCount;
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        JPanel emptySpace = new JPanel();
        emptySpace.setLayout(new BoxLayout(emptySpace, BoxLayout.Y_AXIS));
        emptySpace.setBackground(Color.white);
//        emptySpace.setBorder(BorderFactory.createLineBorder(Color.blue));
        emptySpace.add(Box.createRigidArea(new Dimension(50,5)));
        parent.add(emptySpace, c);
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

class TwoColorRoundedRect extends JPanel {
    private final Color color1 = UIConstants.BLUE_COLOR;
    private final Color color2 = UIConstants.YELLOW_COLOR;
    private final int cornerRadius = 25;
    private final int width = MessagesListPane.COLORED_LABEL_WIDTH;
    private final int height = MessagesListPane.COLORED_LABEL_HEIGHT;

    public TwoColorRoundedRect() {
        super.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        int areaWidth = getWidth();
//        System.out.println("area width is " + areaWidth);
        int areaHeight = getHeight();
        int horizontalOffset = (areaWidth - width)/2;
//        System.out.println("horizontal offset is " + horizontalOffset);
        int verticalOffset = (areaHeight - height)/2;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        super.paintComponent(g);
        //Draws the rounded panel with borders.
        g2d.setColor(color1);
        g2d.fillRoundRect(horizontalOffset, verticalOffset, width, height-1, cornerRadius, cornerRadius);

        g2d.setColor(color2);
        int rectangularZoneHeight = height/4;
        g2d.fillRoundRect(horizontalOffset, areaHeight/2, width, rectangularZoneHeight, 0, 0);
        g2d.fillRoundRect(horizontalOffset, areaHeight/2, width, height/2, cornerRadius, cornerRadius);

        ((JLabel)this.getComponents()[0]).setBorder(BorderFactory.createEmptyBorder(areaHeight/2 - height/2, 0, 0,0));
        ((JLabel)this.getComponents()[1]).setBorder(BorderFactory.createEmptyBorder(height/4 - UIConstants.REGULAR_FONT.getSize()/2, 0,0,0));
    }
}