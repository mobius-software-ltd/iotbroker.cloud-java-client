package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public class CustomComboBoxUI extends BasicComboBoxUI {

    public static ComboBoxUI createUI(JComponent c) {
        return new CustomComboBoxUI();
    }

    @Override
    protected Insets getInsets() {
        return new Insets(0,0,0,0);
    }

    @Override protected JButton createArrowButton() {
        return new BasicArrowButton(
                BasicArrowButton.SOUTH,
                MainPane.APP_COLOR, MainPane.APP_COLOR,
                Color.white, MainPane.APP_COLOR);
    }
}