package com.mobiussoftware.iotbroker.ui.elements;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

import com.mobiussoftware.iotbroker.ui.UIConstants;

public class CustomComboBoxUI extends BasicComboBoxUI {

	public static ComboBoxUI createUI(JComponent c) {
		return new CustomComboBoxUI();
	}

	@Override
	protected Insets getInsets() {
		return new Insets(0, 0, 0, 0);
	}

	@Override
	protected JButton createArrowButton() {
		return new BasicArrowButton(BasicArrowButton.SOUTH, UIConstants.APP_CONTRAST_COLOR,
				UIConstants.APP_CONTRAST_COLOR, Color.white, UIConstants.APP_CONTRAST_COLOR);
	}
}