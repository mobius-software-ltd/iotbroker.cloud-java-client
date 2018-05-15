package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.plaf.PanelUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class TopicListPane extends JPanel {

    Image bgImage = Toolkit.getDefaultToolkit().createImage(MainPane.IMAGE_RES_PATH + MainPane.BG_IMG);

    public TopicListPane() {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        JLabel topicListLbl = new JLabel("topics list:", SwingConstants.LEFT);
        topicListLbl.setFont(new Font("SansSerif", Font.BOLD, 10));

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.gridwidth = 1;
        c.ipadx = 50;

        this.add(topicListLbl, c);

        JPanel topics = new JPanel();
        topics.setBackground(Color.white);
        topics.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        topics.setUI(new PanelUI() {
            @Override
            public Dimension getPreferredSize(JComponent jComponent) {
                return new Dimension(438, 300);
            }
        });

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;

        this.add(topics, c);

        JLabel addTopicLbl = new JLabel("add new topic:", SwingConstants.LEFT);
        addTopicLbl.setFont(new Font("SansSerif", Font.BOLD, 10));

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;

        this.add(addTopicLbl, c);

        JPanel addTopic = new JPanel();
        addTopic.setBackground(Color.white);
        addTopic.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        addTopic.setUI(new PanelUI() {
            @Override
            public Dimension getPreferredSize(JComponent jComponent) {
                return new Dimension(438, 60);
            }
        });

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;

        this.add(addTopic, c);

        JLabel addLbl = new JLabel("Add");
        addLbl.setBackground(new Color(25, 163, 219) );

        addLbl.setOpaque(true);
        addLbl.setForeground(Color.white);
        addLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        addLbl.setHorizontalAlignment(SwingConstants.CENTER);
        addLbl.setPreferredSize(new Dimension(438, 40));
        addLbl.setMinimumSize(new Dimension(438, 40));
        addLbl.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        addLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                System.out.println("Add button clicked!");
            }
        });

        c.fill = GridBagConstraints.HORIZONTAL;
//        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 4;
        c.anchor = GridBagConstraints.PAGE_END;

        this.add(addLbl, c);

        //subelements of addtopic
        JPanel row1 = new JPanel();
        row1.setBackground(Color.white);
        ImageIcon tmp = new ImageIcon(MainPane.IMAGE_RES_PATH + MainPane.IC_SETTINGS);
        Image tmp2 = tmp.getImage().getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);
        JLabel lbl1 = new JLabel(new ImageIcon(tmp2));
        JLabel lbl2 = new JLabel("Topic:");
        JTextField txtField = new HintTextField("topic");

        row1.add(lbl1);
        row1.add(lbl2);
        row1.add(txtField);

        addTopic.add(row1);

    }

    private Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }

    @Override
    protected void paintComponent(Graphics g) {
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
