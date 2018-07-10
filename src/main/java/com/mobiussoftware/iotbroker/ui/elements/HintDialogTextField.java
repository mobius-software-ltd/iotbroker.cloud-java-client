package com.mobiussoftware.iotbroker.ui.elements;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;

public class HintDialogTextField extends HintTextField {

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
