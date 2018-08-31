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
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

@SuppressWarnings("unused") 
public class CustomProgressBar extends JProgressBar
{

	private static final long serialVersionUID = 6295930331759745866L;

	private Color color;
	private int cornerRadius;

	CustomProgressBar(Color color, int cornerRadius)
	{
		this.color = color;
		this.cornerRadius = cornerRadius;
	}

	@Override protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int width = getWidth();
		int height = getHeight();

		// Draws the rounded panel with borders.
		g2d.setColor(Color.yellow);
		g2d.fillRoundRect(5, 5, width - 20, height - 10, 10, 10);// paint
		// background
		super.paintComponent(g);
	}

	@Override public void paint(Graphics graphics)
	{
		super.paint(graphics);
	}

	class CustomUI
			extends BasicProgressBarUI
	{
		@Override protected void paintDeterminate(Graphics graphics, JComponent jComponent)
		{
			super.paintDeterminate(graphics, jComponent);
		}

		@Override public void paint(Graphics g, JComponent c)
		{
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int width = 210;
			int height = 30;
			int cornerRadius = 5;
			// int height = getHeight();
			g.setColor(progressBar.getForeground());
			g.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
		}
	}
}