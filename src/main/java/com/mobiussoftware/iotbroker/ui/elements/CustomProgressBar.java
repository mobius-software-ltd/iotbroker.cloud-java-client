package com.mobiussoftware.iotbroker.ui.elements;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

public class CustomProgressBar extends JProgressBar {
    private Color color;
    private int cornerRadius;

    CustomProgressBar(Color color, int cornerRadius) {
        this.color = color;
        this.cornerRadius = cornerRadius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();

        //Draws the rounded panel with borders.
        g2d.setColor(Color.yellow);
        g2d.fillRoundRect(5, 5, width-20, height-10, 10, 10);//paint background
        super.paintComponent(g);
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
    }
}

class CustomUI extends BasicProgressBarUI {
    private Rectangle r = new Rectangle();

    @Override
    protected void paintDeterminate(Graphics graphics, JComponent jComponent) {
        super.paintDeterminate(graphics, jComponent);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int width = 210;
        int height = 30;
        int cornerRadius = 5;
//        int height = getHeight();
        g.setColor(progressBar.getForeground());
        g.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
    }
}