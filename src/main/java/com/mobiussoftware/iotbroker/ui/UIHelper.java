package com.mobiussoftware.iotbroker.ui;

import com.sun.rowset.internal.Row;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.MouseListener;

public class UIHelper {

    static JPanel createProgressBarSpace(int height) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
//        panel.setBackground(new Color(255,255,0,100));
        panel.setBackground(new Color(0,0,0,0));
        panel.setMinimumSize(new Dimension(400,height));
        panel.setPreferredSize(panel.getMinimumSize());

        return  panel;
    }

    static JProgressBar createProgressBar() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setUI(new BasicProgressBarUI());
        progressBar.setString("");
        progressBar.setBackground(new Color(190, 200, 200, 50));
        progressBar.setForeground(UIConstants.APP_CONTRAST_COLOR);
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(170, 180, 180, 200)));
        progressBar.setStringPainted(true);
        progressBar.setMinimumSize(new Dimension(450,7));
        progressBar.setPreferredSize(progressBar.getMinimumSize());
        progressBar.setOpaque(true);


        return progressBar;
    }

    static JPanel createSmallBoldLabel(String text) {
        JPanel lbl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lbl.setBackground(new Color(0,0,0,0));
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setFont(UIConstants.TEXT_LABEL_FONT);
        lbl.add(label);

        return  lbl;
    }

    static JPanel wrapInBorderLayout(JPanel panel, String borderLayoutAlignment) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.white);
        wrapper.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        wrapper.add(panel, borderLayoutAlignment);
        return wrapper;
    }

    static JPanel wrapInScrollAndBorderLayout(JPanel panel, String borderLayoutAlignment) {
        JScrollPane scrollPane = new JScrollPane(panel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, borderLayoutAlignment);

        return wrapper;
    }

//    static JPanel wrapInScrollAndBorderLayout(JPanel panel, int width, int minHeight, int prefHeight, String borderLayoutAlignment) {
//        JScrollPane scrollPane = new JScrollPane(panel);
//        scrollPane.setPreferredSize(new Dimension(width, prefHeight));
//        scrollPane.setMinimumSize(new Dimension(width, minHeight));
//        scrollPane.setMaximumSize(new Dimension(width, prefHeight));
//
//        JPanel wrapper = new JPanel(new BorderLayout());
//        wrapper.add(scrollPane, borderLayoutAlignment);
//
//        return wrapper;
//    }

    static JPanel createParameterLabel(String text, Icon icon, int horizontalAlignment, Color color) {
        JPanel lbl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lbl.setBackground(color);

        JLabel label = new JLabel(text, icon, horizontalAlignment);
        label.setBorder(new EmptyBorder(0, 10, 0, 0));
        label.setFont(UIConstants.REGULAR_FONT);
        label.setIconTextGap(10);

        lbl.add(label);

        return lbl;
    }

    static <T extends Object> JPanel createJComboBox(T[] values, Dimension dimension) {
        UIManager.put("ComboBox.background", new ColorUIResource(Color.white));
        UIManager.put("ComboBox.selectionBackground", UIConstants.SELECTION_COLOR);
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.gray));

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        Dimension wrapperDimension = new Dimension(dimension.width + 2, dimension.height + 2);
        wrapper.setMinimumSize(wrapperDimension);
        wrapper.setPreferredSize(wrapper.getMinimumSize());
        wrapper.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        JComboBox<T> cb = new JComboBox<>(values);
        cb.setFont(UIConstants.REGULAR_FONT);
        cb.setMinimumSize(dimension);
        cb.setPreferredSize(cb.getMinimumSize());
        cb.setUI(CustomComboBoxUI.createUI(cb));

        BasicComboBoxRenderer renderer = (BasicComboBoxRenderer)cb.getRenderer();
        renderer.setBorder(new EmptyBorder(0,7,0,0));

        wrapper.add(cb);

        return wrapper;
    }

    static HintTextField createHintTextField(String hint, Dimension dimension) {

        HintTextField tf = new HintTextField(hint, BorderFactory.createLineBorder(Color.lightGray));
        tf.setHorizontalAlignment(JTextField.RIGHT);
        tf.setFont(UIConstants.REGULAR_FONT);
        tf.setMinimumSize(dimension);
        tf.setPreferredSize(tf.getMinimumSize());

        return tf;
    }

    static JCheckBox createJCheckBox(Color color) {

        JCheckBox cb = new JCheckBox();
        cb.setBackground(color);
        cb.setUI(new BasicCheckBoxUI() {
        });

        return cb;
    }

    static JPanel wrapInJPanel(Component component, Color color) {
        JPanel jp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jp.setBackground(color);
        jp.add(component);

        return jp;
    }

    private static JPanel createButton(String text, int height, Font font, MouseListener listener) {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setBackground(Color.cyan);
        btnPanel.setBackground(UIConstants.APP_CONTRAST_COLOR);

        JLabel btnLabel = new JLabel(text);
        btnLabel.setBackground(UIConstants.APP_CONTRAST_COLOR);
        btnLabel.setOpaque(true);
        btnLabel.setForeground(Color.white);
        btnLabel.setFont(font);
        btnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        btnLabel.setMinimumSize(new Dimension(450, height));
//        btnLabel.setPreferredSize(new Dimension(1000, height));
        btnLabel.setPreferredSize(btnLabel.getMinimumSize());
//        btnLabel.setBorder(BorderFactory.createLineBorder(UIConstants.APP_CONTRAST_COLOR));

        if (listener != null)
            btnLabel.addMouseListener(listener);
        btnPanel.add(btnLabel);

        return btnPanel;
    }

    static JPanel createButton(String text, int height, MouseListener listener) {
        Font font = UIConstants.BUTTON_FONT;
        return createButton(text, height, font, listener);
    }

    static JPanel createButton(String text, MouseListener listener) {
        int height = 35;
        return createButton(text, height, listener);
    }

    static JPanel createAppColorLabel(String text, int height) {
        Font font = UIConstants.REGULAR_FONT;
        return createButton(text, height, font, null);
    }

    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz123456789       ";
    static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

//    static <T> Row createRow(String label, Icon icon, InputType inputType, T data) {
//        return new Row(label, icon, inputType, data);
//    }
//
//    enum InputType{
//        textarea, textfield, combobox, checkbox
//    }
}

//class Row<T> {
//    private String label;
//    private Icon icon;
//    private UIHelper.InputType inputType;
//    private T data;
//
//    Row(String label, Icon icon, UIHelper.InputType inputType, T data) {
//        this.label = label;
//        this.icon = icon;
//        this.inputType = inputType;
//        this.data = data;
//
//        try {
//            init();
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void init() throws Exception {
//        switch (inputType) {
//            case textarea:
//            case textfield:
//                if (!(data instanceof String))
//                    throw new Exception("For " + inputType + " input type String parameter should be passed as 4th argument of a row");
//                break;
//            case checkbox:
//                if (!(data instanceof Boolean))
//                    throw new Exception("For checkbox input type Boolean parameter should be passed as 4th argument of a row");
//                break;
//            case combobox:
//                if (!(data instanceof Object[]))
//                    throw new Exception("For textfield input type Object[] parameter should be passed as 4th argument of a row");
//                break;
//        }
//    }
//
//    public String getLabel() {
//        return label;
//    }
//
//    public Icon getIcon() {
//        return icon;
//    }
//
//    public UIHelper.InputType getInputType() {
//        return inputType;
//    }
//
//    public T getData() {
//        return data;
//    }
//}
