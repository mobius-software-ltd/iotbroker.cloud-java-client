package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class MessagesListPane extends JPanel {

    private final int msgCount = 3;

    MessagesListPane() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel txtLbl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtLbl1.setBackground(new Color(0,0,0,0));
        JLabel topicListLbl = new JLabel("messages list:", SwingConstants.LEFT);
        topicListLbl.setFont(MainPane.TEXT_LABEL_FONT);
        txtLbl1.add(topicListLbl);

        this.add(txtLbl1);

        JPanel messagesPane = new JPanel();
        messagesPane.setBackground(Color.white);
        messagesPane.setMinimumSize(new Dimension(410, 280));
        messagesPane.setPreferredSize(new Dimension(410, msgCount * 85));
        messagesPane.setBorder(BorderFactory.createLineBorder(Color.lightGray));

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

        c.fill = GridBagConstraints.BOTH;

        Random r = new Random();

        for (int i = 0; i < msgCount; i++) {
            Color bgColor = i%2 == 0 ? Color.white : MainPane.CONTRAST_LIST_COLOR;

            JPanel messageData = new JPanel();
            messageData.setLayout(new BoxLayout(messageData, BoxLayout.Y_AXIS));
            messageData.setBackground(bgColor);
//            messageData.setBorder(BorderFactory.createLineBorder(Color.blue));

            JLabel topic = new JLabel("topic " + i, SwingConstants.LEFT);
            topic.setFont(MainPane.REGULAR_BOLD_FONT);
            topic.setBorder(BorderFactory.createEmptyBorder(5,5,2,5));

            JLabel text = new JLabel("<html>message payload " + randomAlphaNumeric(r.nextInt(128) + 64) + i + "</html>", SwingConstants.LEFT);
            text.setFont(MainPane.REGULAR_FONT);
            text.setBorder(BorderFactory.createEmptyBorder(1,5,3,5));

            messageData.add(topic);
            messageData.add(text);

            c.gridx = 0;
            c.gridy = i;
            c.weightx = 5;
            c.anchor = GridBagConstraints.NORTHWEST;

            parent.add(messageData, c);

            JPanel extraData = new TwoColorRoundedRect(MainPane.BLUE_COLOR, MainPane.YELLOW_COLOR, 20, 0, 4);
            extraData.setBackground(bgColor);
            extraData.setLayout(new BoxLayout(extraData, BoxLayout.Y_AXIS));


            JLabel direction = new JLabel(r.nextInt(2) == 0 ? "in" : "out", SwingConstants.CENTER);
            direction.setFont(MainPane.REGULAR_FONT);
            direction.setForeground(Color.white);
            direction.setAlignmentX(Component.CENTER_ALIGNMENT);
//            direction.setMaximumSize(new Dimension((int)extraData.getMinimumSize().getWidth(), (int)extraData.getMinimumSize().getHeight()/2));
//            direction.setMaximumSize(new Dimension(extraData.getWidth(), extraData.getHeight()/2));
            direction.setBorder(BorderFactory.createEmptyBorder(extraData.getHeight()/2, 0,0,0));

            JLabel qos = new JLabel("QoS:"+ r.nextInt(3), SwingConstants.CENTER);
            qos.setBorder(BorderFactory.createEmptyBorder(3, 0,0,0));
            qos.setFont(MainPane.REGULAR_FONT);
            qos.setAlignmentX(Component.CENTER_ALIGNMENT);

            extraData.add(direction);
//            extraData.add(Box.createRigidArea(new Dimension(1,6)));
            extraData.add(qos);

            c.gridx = 1;
            c.weightx = 1;
            c.anchor = GridBagConstraints.NORTHEAST;

            parent.add(extraData, c);

            c.gridx = 2;
            c.weightx = 0.1;
            c.anchor = GridBagConstraints.NORTHEAST;

            parent.add(Box.createRigidArea(new Dimension(1,5)), c);

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

    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz123456789       ";

    private static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}

class TwoColorRoundedRect extends JPanel {
    private Color color1;
    private Color color2;
    private int cornerRadius;
    private int verticalOffset;
    private int horizontalOffset;

    TwoColorRoundedRect(Color color1, Color color2, int cornerRadius, int horizontalOffset, int verticalOffset) {
        this.color1 = color1;
        this.color2 = color2;
        this.cornerRadius = cornerRadius;
        this.verticalOffset = verticalOffset;
        this.horizontalOffset = horizontalOffset;
    }

    @Override
    protected void paintComponent(Graphics g) {

        int width = getWidth();
        int height = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        super.paintComponent(g);
        //Draws the rounded panel with borders.
        g2d.setColor(color1);
        g2d.fillRoundRect(horizontalOffset, verticalOffset, width-horizontalOffset, height-verticalOffset-3-1, cornerRadius, cornerRadius);//paint background
        g2d.setColor(color2);
        g2d.fillRoundRect(horizontalOffset, height/2, width-horizontalOffset, 10, 0, 0);//paint background
        g2d.fillRoundRect(horizontalOffset, height/2 + 1, width-horizontalOffset, (height-verticalOffset-3)/2, cornerRadius, cornerRadius);//paint background

        ((JLabel)this.getComponents()[0]).setBorder(BorderFactory.createEmptyBorder(height/4 - MainPane.REGULAR_FONT.getSize()/2, 0,height/4 - MainPane.REGULAR_FONT.getSize()/2,0));
        ((JLabel)this.getComponents()[1]).setBorder(BorderFactory.createEmptyBorder(height/4 - MainPane.REGULAR_FONT.getSize(), 0,0,0));
    }
}