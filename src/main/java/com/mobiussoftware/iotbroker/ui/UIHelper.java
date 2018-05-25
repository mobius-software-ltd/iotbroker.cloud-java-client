package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.MouseListener;

public class UIHelper {

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

    static <T extends Object> JPanel createJComboBox(T[] values, Dimension dimension, Color color) {
        UIManager.put("ComboBox.background", new ColorUIResource(Color.white));
        UIManager.put("ComboBox.selectionBackground", UIConstants.SELECTION_COLOR);
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.gray));

        JPanel val = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        val.setBackground(color);

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

        val.add(wrapper);
        wrapper.add(cb);

        return val;
    }

    static JPanel createHintTextField(String hint, Dimension dimension, Color color) {
        JPanel jp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jp.setBackground(color);

        HintTextField tf = new HintTextField(hint, BorderFactory.createLineBorder(Color.lightGray));
        tf.setHorizontalAlignment(JTextField.RIGHT);
        tf.setFont(UIConstants.REGULAR_FONT);
        tf.setMinimumSize(dimension);
        tf.setPreferredSize(tf.getMinimumSize());

        jp.add(tf);

        return jp;
    }

    static JPanel createButton(String text, int height, Font font, MouseListener listener) {
        JPanel loginLbl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        loginLbl.setBackground(Color.cyan);
        loginLbl.setBackground(UIConstants.APP_CONTRAST_COLOR);
        JLabel loginBtn = new JLabel(text);
        loginBtn.setBackground(UIConstants.APP_CONTRAST_COLOR);
        loginBtn.setOpaque(true);
        loginBtn.setForeground(Color.white);
        loginBtn.setFont(font);
        loginBtn.setHorizontalAlignment(SwingConstants.CENTER);
        loginBtn.setMinimumSize(new Dimension(450, height));
//        loginBtn.setPreferredSize(new Dimension(1000, height));
        loginBtn.setPreferredSize(loginBtn.getMinimumSize());
//        loginBtn.setBorder(BorderFactory.createLineBorder(UIConstants.APP_CONTRAST_COLOR));

        if (listener != null)
            loginBtn.addMouseListener(listener);
        loginLbl.add(loginBtn);

        return loginLbl;
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

    static JPanel createJCheckBox(Color color) {

        JPanel jp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jp.setBackground(color);

        JCheckBox cb = new JCheckBox();
        cb.setBackground(color);
        cb.setUI(new BasicCheckBoxUI() {
        });
        jp.add(cb);

        return jp;
    }

    static <T> Row createRow(String label, Icon icon, InputType inputType, T data) {
        return new Row(label, icon, inputType, data);
    }

    enum InputType{
        textfield, combobox, checkbox
    }
}

class Row<T> {
    private String label;
    private Icon icon;
    private UIHelper.InputType inputType;
    private T data;

    public Row(String label, Icon icon, UIHelper.InputType inputType, T data) {
        this.label = label;
        this.icon = icon;
        this.inputType = inputType;
        this.data = data;

        try {
            init();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        switch (inputType) {
            case textfield:
                if (!(data instanceof String))
                    throw new Exception("For textfield input type String parameter should be passed as 4th argument of a row");
                break;
            case checkbox:
                if (!(data instanceof Boolean))
                    throw new Exception("For checkbox input type Boolean parameter should be passed as 4th argument of a row");
                break;
            case combobox:
                if (!(data instanceof Object[]))
                    throw new Exception("For textfield input type Object[] parameter should be passed as 4th argument of a row");
                break;
        }
    }

    public String getLabel() {
        return label;
    }

    public Icon getIcon() {
        return icon;
    }

    public UIHelper.InputType getInputType() {
        return inputType;
    }

    public T getData() {
        return data;
    }
}
