package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TopicListPane extends JPanel {

    private JPanel emptySpace;
    private JPanel topics;

    private JPanel progressBarSpace;
    private JProgressBar progressBar;

    private Map<Integer, Component[]> componentList = new HashMap<>();

    private HintTextField topicInput;
    private JComboBox<Integer> dropDown;

    TopicListPane() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        emptySpace = new JPanel();
        emptySpace.setLayout(new BoxLayout(emptySpace, BoxLayout.Y_AXIS));
        emptySpace.setBackground(Color.white);
//        emptySpace.setBorder(BorderFactory.createLineBorder(Color.blue));
        emptySpace.add(Box.createRigidArea(new Dimension(50,5)));

        progressBarSpace = UIHelper.createProgressBarSpace(5);
        this.add(progressBarSpace);

        JPanel txtLbl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtLbl1.setBackground(new Color(0,0,0,0));

        JLabel topicListLbl = new JLabel("topics list:", SwingConstants.LEFT);
        topicListLbl.setFont(UIConstants.TEXT_LABEL_FONT);

        txtLbl1.add(topicListLbl);

        this.add(txtLbl1);

        topics = new JPanel();
        topics.setBackground(Color.white);
//        topics.setMinimumSize(new Dimension(410, 280));
//        topics.setBorder(BorderFactory.createLineBorder(Color.lightGray));

//        JScrollPane scrollPane = new JScrollPane(topics);
//        scrollPane.setPreferredSize(new Dimension(450, 1000));
//        scrollPane.setMinimumSize(new Dimension(450, 280));
//        scrollPane.setMaximumSize(new Dimension(450, 1000));
//
//        JPanel wrapper = new JPanel(new BorderLayout());
//        wrapper.add(scrollPane, BorderLayout.CENTER);
//
//        this.add(wrapper);

        this.add(UIHelper.wrapInScrollAndBorderLayout(topics, BorderLayout.CENTER));

        JPanel txtLbl2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtLbl2.setBackground(new Color(0,0,0,0));

        JLabel addTopicLbl = new JLabel("add new topic:");
        addTopicLbl.setFont(UIConstants.TEXT_LABEL_FONT);

        txtLbl2.add(addTopicLbl);

        this.add(txtLbl2);

        final JPanel addTopic = new JPanel();
        addTopic.setBackground(Color.white);
        addTopic.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        addTopic.setMinimumSize(new Dimension(410, 70));
        addTopic.setPreferredSize(addTopic.getMinimumSize());

        this.add(addTopic);

        MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
//                System.out.println("Add button clicked!");
                addTopicAction();
                topicInput.setText("");
                dropDown.setSelectedIndex(0);
            }
        };
        this.add(UIHelper.createButton("Add", listener));

        addTopicListElements(topics);
        addAddTopicElements(addTopic);
    }

    //adding subelements to topicList panel
    private void addTopicListElements(final JPanel parent) {

        parent.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.VERTICAL;

        Random r = new Random();

        int lblCount = 15;

        for (int i = 0; i < lblCount; i++) {
            JLabel topic = new JLabel("topic " + i, SwingConstants.LEFT);
            topic.setFont(UIConstants.REGULAR_FONT);
            topic.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

            c.gridx = 0;
            c.gridy = i;
            c.weightx = 10;
            c.anchor = GridBagConstraints.NORTHWEST;

            parent.add(topic, c);

            JLabel qos = new RoundedFilledLabel(new Color(252, 227, 79), 20, 0, 4);
            qos.setText("QoS:"+ r.nextInt(3));
            qos.setHorizontalAlignment(SwingConstants.LEFT);
            qos.setFont(UIConstants.REGULAR_FONT);
            qos.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

            c.gridx = 1;
            c.weightx = 0.1;
            c.anchor = GridBagConstraints.NORTHEAST;

            parent.add(qos, c);

            JLabel deleteBtn = new JLabel(UIConstants.IC_TRASH, SwingConstants.CENTER);
            deleteBtn.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            deleteBtn.addMouseListener(deleteTopicAction());
            deleteBtn.setName(String.valueOf(i));

            c.gridx = 2;
            c.weightx = 0.1;
            c.anchor = GridBagConstraints.NORTHEAST;

            parent.add(deleteBtn, c);


            Component[] row = new Component[3];
            row[0] = topic;
            row[1] = qos;
            row[2] = deleteBtn;

            componentList.put(i, row);
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
        topic.setFont(UIConstants.REGULAR_FONT);
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
        qos.setFont(UIConstants.REGULAR_FONT);
        qos.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        c.gridx = 1;
        c.weightx = 0.1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTHEAST;

        topics.add(qos, c);
//
        JLabel deleteBtn = new JLabel(UIConstants.IC_TRASH, SwingConstants.CENTER);
        deleteBtn.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        deleteBtn.addMouseListener(deleteTopicAction());

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

        componentList.put(rowNumber, row);

        c.weighty = 1;
        c.gridy = rowNumber + 1;
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        topics.add(emptySpace, c);
    }

    private void deleteListRow(String index) {
        System.out.println("Delete topic clicked! " + index);
        int i = Integer.valueOf(index);

        Component[] row = componentList.get(i);
        topics.remove(row[0]);
        topics.remove(row[1]);
        topics.remove(row[2]);

        componentList.remove(i);
        topics.revalidate();
        topics.repaint();
    }

    //adding subelements to addtopic panel
    private void addAddTopicElements(JPanel parent) {
        UIManager.put("ComboBox.background", new ColorUIResource(Color.white));
        UIManager.put("ComboBox.selectionBackground", UIConstants.SELECTION_COLOR);
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.gray));

        JPanel el1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        el1.setBackground(Color.white);
        Image tmp2 = UIConstants.IC_SETTINGS.getImage().getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
        JLabel icon = new JLabel(new ImageIcon(tmp2));
        icon.setBorder(new EmptyBorder(0, 10, 0, 10));
        JLabel text = new JLabel("Topic:");
        text.setFont(UIConstants.REGULAR_FONT);

        el1.add(icon);
        el1.add(text);

        JPanel el2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        el2.setBackground(Color.white);

        topicInput = new HintTextField("topic", BorderFactory.createLineBorder(Color.lightGray));
        topicInput.setHorizontalAlignment(JTextField.RIGHT);
        topicInput.setFont(UIConstants.REGULAR_FONT);
        topicInput.setMinimumSize(new Dimension(150, 28));
        topicInput.setPreferredSize(topicInput.getMinimumSize());

        el2.add(topicInput);

        JPanel el3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        el3.setBackground(UIConstants.ROW_ODD_COLOR);

        text = new JLabel("QoS:");
        text.setFont(UIConstants.REGULAR_FONT);
        JLabel icon2 = new JLabel(new ImageIcon(tmp2));
        icon2.setBorder(new EmptyBorder(0, 10, 0, 10));
        dropDown = new JComboBox<>(AppConstants.QOS_VALUES);

        JPanel el4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        el4.setBackground(UIConstants.ROW_ODD_COLOR);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        wrapper.setBackground(Color.yellow);
        wrapper.setMinimumSize(new Dimension(72, 24));
        wrapper.setPreferredSize(wrapper.getMinimumSize());
        wrapper.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        dropDown.setFont(UIConstants.REGULAR_FONT);
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

    private void addTopicAction() {
        //validation
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

        addProgressBar();

        AddTopicTask task = new AddTopicTask(topic, qos);
        task.addPropertyChangeListener(propertyChangeListener());
        task.execute();
    }

    private MouseListener deleteTopicAction() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                JLabel btnClicked = (JLabel)arg0.getSource();
                btnClicked.removeMouseListener(this);

                addProgressBar();

                DeleteTopicTask task = new DeleteTopicTask(btnClicked.getName());
                task.addPropertyChangeListener(propertyChangeListener());
                task.execute();
            }
        };
    }

    class AddTopicTask extends NetworkTask<Void, Void> {
        private String topic;
        private int qos;

        public AddTopicTask(String topic, int qos) {
            this.topic = topic;
            this.qos = qos;
        }

        @Override
        protected void done() {
            addTopicListRow(topic, qos);
            removeProgressBar();
        }
    }

    class DeleteTopicTask extends NetworkTask<Void, Void> {
        private String index;

        public DeleteTopicTask(String index) {
            this.index = index;
        }

        @Override
        public void done() {
            deleteListRow(index);
            removeProgressBar();
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
        TopicListPane.this.revalidate();
        TopicListPane.this.repaint();
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