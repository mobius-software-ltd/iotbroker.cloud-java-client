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

import com.mobiussoftware.iotbroker.ui.UIConstants;

import javax.swing.*;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public class CustomComboBoxUI extends BasicComboBoxUI
{

	public static ComboBoxUI createUI(JComponent c)
	{
		return new CustomComboBoxUI();
	}

	@Override protected Insets getInsets()
	{
		return new Insets(0, 0, 0, 0);
	}

	@Override protected JButton createArrowButton()
	{
		return new BasicArrowButton(BasicArrowButton.SOUTH, UIConstants.APP_CONTRAST_COLOR, UIConstants.APP_CONTRAST_COLOR, Color.white, UIConstants.APP_CONTRAST_COLOR);
	}
}