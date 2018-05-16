package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class TopicListPane extends JPanel {

//    Image bgImage = Toolkit.getDefaultToolkit().createImage(MainPane.IMAGE_RES_PATH + MainPane.BG_IMG);
    private Image bgImage = new ImageIcon(MainPane.IMAGE_RES_PATH + MainPane.BG_IMG).getImage();

    TopicListPane() {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        JPanel txtLbl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtLbl1.setBackground(new Color(0,0,0,0));

        JLabel topicListLbl = new JLabel("topics list:", SwingConstants.LEFT);
        topicListLbl.setFont(MainPane.TEXT_LABEL_FONT);

        txtLbl1.add(topicListLbl);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.gridwidth = 1;
//        c.ipadx = 50;
        this.add(txtLbl1, c);

        JPanel topics = new JPanel();
        topics.setBackground(Color.white);
        topics.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        topics.setUI(new PanelUI() {
            @Override
            public Dimension getPreferredSize(JComponent jComponent) {
                return new Dimension(410, 290);
            }
        });

//        JScrollPane wrapper = new JScrollPane(topics);
//        wrapper.setPreferredSize(new Dimension(450, 1000));
//        wrapper.setMinimumSize(new Dimension(450, 290));
//        wrapper.setMaximumSize(new Dimension(450, 1000));

//        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
//        this.add(wrapper, c);
        this.add(topics, c);

        JPanel txtLbl2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtLbl2.setBackground(new Color(0,0,0,0));

        JLabel addTopicLbl = new JLabel("add new topic:", SwingConstants.LEFT);
        addTopicLbl.setFont(MainPane.TEXT_LABEL_FONT);

        txtLbl2.add(addTopicLbl);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        this.add(txtLbl2, c);

        JPanel addTopic = new JPanel();
        addTopic.setLayout(new BoxLayout(addTopic, BoxLayout.Y_AXIS));
        addTopic.setBackground(Color.white);
        addTopic.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        addTopic.setUI(new PanelUI() {
            @Override
            public Dimension getPreferredSize(JComponent jComponent) {
                return new Dimension(438, 70);
            }
        });

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;

        this.add(addTopic, c);

        JLabel addLbl = new JLabel("Add");
        addLbl.setBackground(MainPane.APP_COLOR);

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

        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 4;
        c.anchor = GridBagConstraints.PAGE_END;

        this.add(addLbl, c);

        addTopicListElements(topics);
        addAddTopicElements(addTopic);

    }

    private void addTopicListElements(JPanel parent) {

        parent.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 0;

        for (int i = 0; i < 20; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setBackground(new Color(255,255,255));

            JLabel topic = new JLabel("TOPIC " + i, SwingConstants.LEFT);
            topic.setFont(MainPane.TEXT_LABEL_FONT);

            row.add(topic);

            c.gridy = i;

            parent.add(row, c);
        }
    }

    private void addAddTopicElements(JPanel parent) {
        //subelements of addtopic
        UIManager.put("ComboBox.background", new ColorUIResource(Color.white));
        UIManager.put("ComboBox.selectionBackground", MainPane.SELECTION_COLOR);
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.gray));

        Font subElementsFont = new Font("SansSerif", Font.PLAIN, 12);

        JPanel el1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        el1.setBackground(Color.white);
        ImageIcon tmp = new ImageIcon(MainPane.IMAGE_RES_PATH + MainPane.IC_SETTINGS);
        Image tmp2 = tmp.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        JLabel icon = new JLabel(new ImageIcon(tmp2));
        icon.setBorder(new EmptyBorder(0, 10, 0, 10));
        JLabel text = new JLabel("Topic:");
        text.setFont(subElementsFont);

        el1.add(icon);
        el1.add(text);

        JPanel el2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        el2.setBackground(Color.white);

        JTextField input = new HintTextField("topic", BorderFactory.createLineBorder(Color.lightGray));
        input.setHorizontalAlignment(JTextField.RIGHT);
        input.setFont(subElementsFont);
        input.setMinimumSize(new Dimension(150, 28));
        input.setPreferredSize(input.getMinimumSize());

        el2.add(input);

        JPanel el3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        el3.setBackground(Color.white);

        text = new JLabel("QoS:");
        text.setFont(subElementsFont);
        JLabel icon2 = new JLabel(new ImageIcon(tmp2));
        icon2.setBorder(new EmptyBorder(0, 10, 0, 10));
        JComboBox dropDown = new JComboBox(new Integer[] {0, 1, 2});

        JPanel el4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        el4.setBackground(Color.white);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        wrapper.setBackground(Color.yellow);
        wrapper.setMinimumSize(new Dimension(72, 24));
        wrapper.setPreferredSize(wrapper.getMinimumSize());
        wrapper.setBorder(BorderFactory.createLineBorder(Color.lightGray));


        dropDown.setFont(subElementsFont);
        dropDown.setMinimumSize(new Dimension(70, 22));
        dropDown.setPreferredSize(dropDown.getMinimumSize());
        dropDown.setUI(CustomComboBoxUI.createUI(dropDown));

        BasicComboBoxRenderer renderer = (BasicComboBoxRenderer)dropDown.getRenderer();
//        Border blackLineBorder = new BorderUIResource(new LineBorder(Color.gray));
//        BorderUIResource border = new BorderUIResource(new CompoundBorder(blackLineBorder,new EmptyBorder(0,7,0,0)));
        renderer.setBorder(new EmptyBorder(0,7,0,0));

        el4.add(wrapper);
        wrapper.add(dropDown);

        el3.add(icon2);
        el3.add(text);

        parent.setLayout(new GridLayout(2, 2));
        parent.add(el1);
        parent.add(el2);
        parent.add(el3);
        parent.add(el4);
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
