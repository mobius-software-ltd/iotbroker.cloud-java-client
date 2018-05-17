package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class TopicListPane extends JPanel {

    private JPanel emptySpace;
    private JPanel topics;

    TopicListPane() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        emptySpace = new JPanel();
        emptySpace.setLayout(new BoxLayout(emptySpace, BoxLayout.Y_AXIS));
        emptySpace.setBackground(Color.white);
//        emptySpace.setBorder(BorderFactory.createLineBorder(Color.blue));
        emptySpace.add(Box.createRigidArea(new Dimension(50,5)));

        JPanel txtLbl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtLbl1.setBackground(new Color(0,0,0,0));

        JLabel topicListLbl = new JLabel("topics list:", SwingConstants.LEFT);
        topicListLbl.setFont(MainPane.TEXT_LABEL_FONT);

        txtLbl1.add(topicListLbl);

        this.add(txtLbl1);

        topics = new JPanel();
        topics.setBackground(Color.white);
        topics.setMinimumSize(new Dimension(410, 280));
        topics.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        JScrollPane scrollPane = new JScrollPane(topics);
        scrollPane.setPreferredSize(new Dimension(450, 1000));
        scrollPane.setMinimumSize(new Dimension(450, 280));
        scrollPane.setMaximumSize(new Dimension(450, 1000));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);

        this.add(wrapper);

        JPanel txtLbl2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtLbl2.setBackground(new Color(0,0,0,0));

        JLabel addTopicLbl = new JLabel("add new topic:");
        addTopicLbl.setFont(MainPane.TEXT_LABEL_FONT);

        txtLbl2.add(addTopicLbl);

        this.add(txtLbl2);

        final JPanel addTopic = new JPanel();
        addTopic.setBackground(Color.white);
        addTopic.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        addTopic.setMinimumSize(new Dimension(410, 70));
        addTopic.setPreferredSize(addTopic.getMinimumSize());

        this.add(addTopic);

        JPanel addLbl = new JPanel();

        JLabel addBtn = new JLabel("Add");
        addBtn.setBackground(MainPane.APP_COLOR);

        addBtn.setOpaque(true);
        addBtn.setForeground(Color.white);
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        addBtn.setHorizontalAlignment(SwingConstants.CENTER);
        addBtn.setPreferredSize(new Dimension(1000, 35));
        addBtn.setMinimumSize(new Dimension(450, 35));
        addBtn.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
//                System.out.println("Add button clicked!");
                addTopicAction();
                topicInput.setText("");
                dropDown.setSelectedIndex(0);
            }
        });

        addLbl.add(addBtn);
        this.add(addLbl);

        addTopicListElements(topics);
        addAddTopicElements(addTopic);
    }

    private ArrayList<Component[]> componentList = new ArrayList<>();

    //adding subelements to topicList panel
    private void addTopicListElements(final JPanel parent) {

        parent.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.VERTICAL;

        Random r = new Random();

        int lblCount = 5;

        for (int i = 0; i < lblCount; i++) {
            JLabel topic = new JLabel("topic " + i, SwingConstants.LEFT);
            topic.setFont(MainPane.REGULAR_FONT);
            topic.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

            c.gridx = 0;
            c.gridy = i;
            c.weightx = 10;
            c.anchor = GridBagConstraints.NORTHWEST;

            parent.add(topic, c);

            JLabel qos = new RoundedFilledLabel(new Color(252, 227, 79), 20, 0, 4);
            qos.setText("QoS:"+ r.nextInt(3));
            qos.setHorizontalAlignment(SwingConstants.LEFT);
            qos.setFont(MainPane.REGULAR_FONT);
            qos.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

            c.gridx = 1;
            c.weightx = 0.1;
            c.anchor = GridBagConstraints.NORTHEAST;

            parent.add(qos, c);

            JLabel deleteBtn = new JLabel(MainPane.IC_TRASH, SwingConstants.CENTER);
            deleteBtn.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            deleteBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {

                    JLabel btnClicked = (JLabel)arg0.getSource();
                    String index = btnClicked.getName();
//                    System.out.println("Delete topic clicked! " + index);
                    int i = Integer.valueOf(index);

                    Component[] row = componentList.get(i);
                    parent.remove(row[0]);
                    parent.remove(row[1]);
                    parent.remove(btnClicked);

                    componentList.remove(i);
                    parent.revalidate();
                    parent.repaint();
                }
            });
            deleteBtn.setName(String.valueOf(i));

            c.gridx = 2;
            c.weightx = 0.1;
            c.anchor = GridBagConstraints.NORTHEAST;

            parent.add(deleteBtn, c);


            Component[] row = new Component[3];
            row[0] = topic;
            row[1] = qos;
            row[2] = deleteBtn;

            componentList.add(row);
        }

        c.weighty = 1;
        c.gridy = lblCount;
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        parent.add(emptySpace, c);
    }

    private void addTopicListRow(String topicText, int qosValue) {
        int rowNumber = ((GridBagLayout)topics.getLayout()).getLayoutDimensions()[1].length - 1;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;

        JLabel topic = new JLabel(topicText, SwingConstants.LEFT);
        topic.setFont(MainPane.REGULAR_FONT);
        topic.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        c.gridx = 0;
        c.gridy = rowNumber;
        c.weightx = 10;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTHWEST;

        topics.add(topic, c);

        JLabel qos = new RoundedFilledLabel(new Color(252, 227, 79), 20, 0, 4);
        qos.setText("QoS:" + qosValue);
        qos.setHorizontalAlignment(SwingConstants.LEFT);
        qos.setFont(MainPane.REGULAR_FONT);
        qos.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        c.gridx = 1;
        c.weightx = 0.1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTHEAST;

        topics.add(qos, c);
//
        JLabel deleteBtn = new JLabel(MainPane.IC_TRASH, SwingConstants.CENTER);
        deleteBtn.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {

                JLabel btnClicked = (JLabel)arg0.getSource();
                String index = btnClicked.getName();
                System.out.println("Delete topic clicked! " + index);
                int i = Integer.valueOf(index);

                Component[] row = componentList.get(i);
                topics.remove(row[0]);
                topics.remove(row[1]);
                topics.remove(btnClicked);

                componentList.remove(i);
                topics.revalidate();
                topics.repaint();
            }
        });

        deleteBtn.setName(String.valueOf(rowNumber));

        c.gridx = 2;
        c.weightx = 0.1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTHEAST;

        topics.add(deleteBtn, c);
        topics.revalidate();
        topics.repaint();

        Component[] row = new Component[3];
        row[0] = topic;
        row[1] = qos;
        row[2] = deleteBtn;

        componentList.add(row);

        c.weighty = 1;
        c.gridy = rowNumber + 1;
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        topics.add(emptySpace, c);
    }

    private void addTopicAction() {
        String topic = topicInput.getText();
        if (topic == null || topic.equals("")) {
            topicInput.setBorder(BorderFactory.createLineBorder(Color.red));
            topicInput.requestFocusInWindow();
            topicInput.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {
                    topicInput.setBorder(BorderFactory.createLineBorder(Color.gray));
                    topicInput.removeKeyListener(this);
                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {
                }

                @Override
                public void keyReleased(KeyEvent keyEvent) {
                }
            });
            return;
        }
        int qos = dropDown.getSelectedIndex();
        addTopicListRow(topic, qos);
    }

    private HintTextField topicInput;
    private JComboBox<Integer> dropDown;

    //adding subelements to addtopic panel
    private void addAddTopicElements(JPanel parent) {
        UIManager.put("ComboBox.background", new ColorUIResource(Color.white));
        UIManager.put("ComboBox.selectionBackground", MainPane.SELECTION_COLOR);
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.gray));

        JPanel el1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        el1.setBackground(Color.white);
        Image tmp2 = MainPane.IC_SETTINGS.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        JLabel icon = new JLabel(new ImageIcon(tmp2));
        icon.setBorder(new EmptyBorder(0, 10, 0, 10));
        JLabel text = new JLabel("Topic:");
        text.setFont(MainPane.REGULAR_FONT);

        el1.add(icon);
        el1.add(text);

        JPanel el2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        el2.setBackground(Color.white);

        topicInput = new HintTextField("topic", BorderFactory.createLineBorder(Color.lightGray));
        topicInput.setHorizontalAlignment(JTextField.RIGHT);
        topicInput.setFont(MainPane.REGULAR_FONT);
        topicInput.setMinimumSize(new Dimension(150, 28));
        topicInput.setPreferredSize(topicInput.getMinimumSize());

        el2.add(topicInput);

        JPanel el3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        el3.setBackground(MainPane.CONTRAST_LIST_COLOR);

        text = new JLabel("QoS:");
        text.setFont(MainPane.REGULAR_FONT);
        JLabel icon2 = new JLabel(new ImageIcon(tmp2));
        icon2.setBorder(new EmptyBorder(0, 10, 0, 10));
        dropDown = new JComboBox<>(new Integer[] {0, 1, 2});

        JPanel el4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        el4.setBackground(MainPane.CONTRAST_LIST_COLOR);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        wrapper.setBackground(Color.yellow);
        wrapper.setMinimumSize(new Dimension(72, 24));
        wrapper.setPreferredSize(wrapper.getMinimumSize());
        wrapper.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        dropDown.setFont(MainPane.REGULAR_FONT);
        dropDown.setMinimumSize(new Dimension(70, 22));
        dropDown.setPreferredSize(dropDown.getMinimumSize());
        dropDown.setUI(CustomComboBoxUI.createUI(dropDown));

        BasicComboBoxRenderer renderer = (BasicComboBoxRenderer)dropDown.getRenderer();
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

class RoundedFilledLabel extends JLabel {
    private Color color;
    private int cornerRadius;
    private int verticalOffset;
    private int horizontalOffset;

    RoundedFilledLabel(Color color, int cornerRadius, int horizontalOffset, int verticalOffset) {
        this.color = color;
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


        //Draws the rounded panel with borders.
        g2d.setColor(color);
        g2d.fillRoundRect(horizontalOffset, verticalOffset, width-horizontalOffset-1, height-verticalOffset-3, cornerRadius, cornerRadius);//paint background
        super.paintComponent(g);
    }
}
