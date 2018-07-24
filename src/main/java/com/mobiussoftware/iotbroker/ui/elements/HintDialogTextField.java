package com.mobiussoftware.iotbroker.ui.elements;

import java.awt.Color;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

@SuppressWarnings("unused")
public class HintDialogTextField extends HintTextField {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7413423837551286778L;
	private boolean textAreaIsShown = false;
    private String text = "";

    public HintDialogTextField(String hint, Border border) {
        super(hint, border);
    }

    @Override
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        transferFocus();

        JTextArea textArea = new JTextArea(text, 20, 30);
        textArea.setSize(textArea.getPreferredSize().width, 1);
        textAreaIsShown = true;

        //remove this line if you want to keep "<...> even when text area is shown"
        super.setText("");

        JOptionPane.showMessageDialog(null, new JScrollPane( textArea), "Will", JOptionPane.PLAIN_MESSAGE);

        text = textArea.getText().trim();

        //network of showing text/hint in will field
        if (!text.isEmpty()) {
            super.setText("<...>");
            super.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        }
        else
            super.setText("");

        super.focusLost(e);

        textAreaIsShown = false;
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    public String getText() {
        return text;
    }

    @Override
    public void clearText() {
        text = "";
        super.clearText();
    }
}
