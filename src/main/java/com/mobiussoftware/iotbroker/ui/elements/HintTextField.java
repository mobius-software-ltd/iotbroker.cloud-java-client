package com.mobiussoftware.iotbroker.ui.elements;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HintTextField extends JTextField implements FocusListener {

	private static final long serialVersionUID = -1534516902639919627L;

	private final String hint;
	private boolean showingHint;

	public HintTextField(final String hint, Border border) {
		super(hint);
		super.setForeground(Color.gray);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
		this.setBorder(border);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (this.getText().isEmpty()) {
			super.setText("");
			super.setForeground(Color.black);
			showingHint = false;
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (this.getText().isEmpty()) {
			super.setText(hint);
			super.setForeground(Color.gray);
			super.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			showingHint = true;
		}
	}

	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}

	public void clearText() {
		super.setText(hint);
		super.setForeground(Color.gray);
		showingHint = true;
	}
}