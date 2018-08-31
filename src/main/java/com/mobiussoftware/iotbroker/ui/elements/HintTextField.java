package com.mobiussoftware.iotbroker.ui.elements;

/**
* Mobius Software LTD
* Copyright 2015-2018, Mobius Software LTD
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HintTextField extends JTextField implements FocusListener
{

	private static final long serialVersionUID = -1534516902639919627L;

	private String hint;
	private boolean showingHint;

	public HintTextField(String hint, Border border)
	{
		super(hint);
		super.setForeground(Color.gray);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
		this.setBorder(border);
	}

	@Override public void focusGained(FocusEvent e)
	{
		if (this.getText().isEmpty())
		{
			super.setText("");
			super.setForeground(Color.black);
			this.showingHint = false;
		}
	}

	@Override public void focusLost(FocusEvent e)
	{
		if (this.getText().isEmpty())
		{
			super.setText(hint);
			super.setForeground(Color.gray);
			super.setBorder(BorderFactory.createLineBorder(Color.lightGray));
			this.showingHint = true;
		}
	}

	@Override public String getText()
	{
		return showingHint ? "" : super.getText();
	}

	public void clearText()
	{
		super.setText(hint);
		super.setForeground(Color.gray);
		this.showingHint = true;
	}
}