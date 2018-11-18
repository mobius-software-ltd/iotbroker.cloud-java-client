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

@SuppressWarnings("unused") 
public class HintDialogTextField extends HintTextField
{

	/**
	 *
	 */
	private static final long serialVersionUID = -7413423837551286778L;
	private boolean textAreaIsShown = false;
	private String text = "";
	private String hint;
	
	public HintDialogTextField(String hint, Border border)
	{		
		super(hint, border);
		this.hint=hint;
	}

	@Override public void focusGained(FocusEvent e)
	{
		super.focusGained(e);
		transferFocus();

		JTextArea textArea = new JTextArea(text, 20, 30);
		textArea.setSize(textArea.getPreferredSize().width, 1);
		textAreaIsShown = true;

		// remove this line if you want to keep "<...> even when text area is
		// shown"
		super.setText("");

		JOptionPane.showMessageDialog(null, new JScrollPane(textArea), hint, JOptionPane.PLAIN_MESSAGE);

		text = textArea.getText().trim();

		// network of showing text/hint in will field
		if (!text.isEmpty())
		{
			super.setText("<...>");
			super.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		}
		else
			super.setText("");

		super.focusLost(e);

		textAreaIsShown = false;
	}

	@Override public void focusLost(FocusEvent e)
	{
	}

	public String getText()
	{
		return text;
	}

	@Override public void clearText()
	{
		text = "";
		super.clearText();
	}
}
