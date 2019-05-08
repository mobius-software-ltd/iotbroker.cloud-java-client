package com.mobiussoftware.iotbroker.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.*;

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

import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.Message;
import com.mobiussoftware.iotbroker.network.ClientListener;
import com.mobiussoftware.iotbroker.network.ConnectionState;

public class MessagesListPane extends JPanel implements ClientListener
{

	static final int COLORED_LABEL_WIDTH = 80;
	static final int COLORED_LABEL_HEIGHT = 40;
	private static final long serialVersionUID = -3181230353114636429L;
	private final int msgCount = 50;
	private Account account;
	private JPanel messagesPane;

	MessagesListPane(Account account)
	{
		super();

		this.account = account;
		drawUI();
	}

	private void drawUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel txtLbl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		txtLbl1.setBackground(new Color(0, 0, 0, 0));
		JLabel topicListLbl = new JLabel("messages list:", SwingConstants.LEFT);
		topicListLbl.setFont(UIConstants.TEXT_LABEL_FONT);
		txtLbl1.add(topicListLbl);

		this.add(txtLbl1);

		messagesPane = new JPanel();
		messagesPane.setBackground(Color.white);
		messagesPane.setMinimumSize(new Dimension(410, 280));
		messagesPane.setLayout(new GridBagLayout());

		JScrollPane scrollPane = new JScrollPane(messagesPane);
		scrollPane.setPreferredSize(new Dimension(450, 0));
		scrollPane.setMinimumSize(new Dimension(450, 0));
		scrollPane.setMaximumSize(new Dimension(450, 1000));
		scrollPane.getVerticalScrollBar().setUnitIncrement(26);
		scrollPane.getHorizontalScrollBar().setEnabled(false);
		
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(scrollPane, BorderLayout.CENTER);

		this.add(wrapper);

		addMessagesPaneElements();
	}

	private void addMessagesPaneElements()
	{
		messagesPane.removeAll();
		try
		{
			final DBInterface dbInterface = DBHelper.getInstance();

			java.util.List<Message> messages = dbInterface.getMessages(account);
			Collections.sort(messages, new Comparator<Message>()
			{
				@Override
				public int compare(Message o1, Message o2)
				{
					return Integer.compare(o2.getId(), o1.getId());
				}
			});

			for (Message msg : messages)
				addMessagesPaneElement(msg);

			addEmptySpace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void addEmptySpace()
	{
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 1;
		c.gridy = msgCount;
		c.gridx = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;

		JPanel emptySpace = new JPanel();
		emptySpace.setLayout(new BoxLayout(emptySpace, BoxLayout.Y_AXIS));
		emptySpace.setBackground(Color.white);
		// emptySpace.setBorder(BorderFactory.createLineBorder(Color.blue));
		emptySpace.add(Box.createRigidArea(new Dimension(50, 5)));
		messagesPane.add(emptySpace, c);
	}

	public void addMessagesPaneElement(Message msg)
	{
		Color bgColor = messagesPane.getComponentCount() % 2 == 0 ? Color.white : UIConstants.ROW_ODD_COLOR;

		JPanel messageData = new JPanel();
		messageData.setLayout(new BoxLayout(messageData, BoxLayout.Y_AXIS));
		messageData.setBackground(bgColor);
		// messageData.setBorder(BorderFactory.createLineBorder(Color.blue));

		JLabel topic = new JLabel(msg.getTopic(), SwingConstants.LEFT);
		topic.setFont(UIConstants.REGULAR_BOLD_FONT);
		topic.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));

		JTextArea area = new JTextArea(msg.getContents());
		area.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));
		area.setEditable(false);
		area.setLineWrap(true); 
		area.setWrapStyleWord(true);
		area.setAlignmentX(SwingConstants.CENTER);
		
		messageData.add(topic);
		messageData.add(area);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = messagesPane.getComponentCount() + 1;
		c.weightx = 10;
		c.anchor = GridBagConstraints.NORTHWEST;

		messagesPane.add(messageData, c);

		JPanel extraData = new TwoColorRoundedRect();
		extraData.setBackground(bgColor);
		extraData.setMinimumSize(new Dimension(COLORED_LABEL_WIDTH, 1));
		extraData.setPreferredSize(extraData.getMinimumSize());
		extraData.setLayout(new BoxLayout(extraData, BoxLayout.Y_AXIS));

		JLabel direction = new JLabel(msg.isIncoming() ? "in" : "out", SwingConstants.RIGHT);
		direction.setFont(UIConstants.REGULAR_FONT);
		direction.setForeground(Color.white);
		direction.setAlignmentX(Component.CENTER_ALIGNMENT);
		// direction.setMaximumSize(new
		// Dimension((int)extraData.getMinimumSize().getWidth(),
		// (int)extraData.getMinimumSize().getHeight()/2));
		// direction.setMaximumSize(new Dimension(extraData.getWidth(),
		// extraData.getHeight()/2));
		direction.setBorder(BorderFactory.createEmptyBorder(extraData.getHeight() / 2, 0, 0, 0));

		JLabel qos = new JLabel("QoS:" + msg.getQos(), SwingConstants.CENTER);
		qos.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		qos.setFont(UIConstants.REGULAR_FONT);
		qos.setAlignmentX(Component.CENTER_ALIGNMENT);

		extraData.add(direction);
		// extraData.add(Box.createRigidArea(new Dimension(1,6)));
		extraData.add(qos);

		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 1;
		c.weightx = 0;
		c.anchor = GridBagConstraints.NORTHEAST;

		messagesPane.add(extraData, c);

		c.gridx = 2;
		c.weightx = 0;
		c.anchor = GridBagConstraints.NORTHEAST;

		messagesPane.add(Box.createRigidArea(new Dimension(5, 5)), c);
	}

	@Override
	public void messageSent(Message messageObj)
	{
		addMessagesPaneElements();
	}

	@Override
	public void messageReceived(Message message)
	{
		addMessagesPaneElements();
	}

	@Override
	public void stateChanged(ConnectionState state)
	{
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Image bgImage = UIConstants.BG_IMAGE;
		g.drawImage(bgImage, 0, 0, null);

		BufferedImage bimage = new BufferedImage(bgImage.getWidth(null), bgImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(bgImage, 0, 0, null);
		bGr.dispose();

		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		TexturePaint paint = new TexturePaint(bimage, new Rectangle(0, 0, bgImage.getWidth(null), bgImage.getHeight(null)));
		g2d.setPaint(paint);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}

	private class TwoColorRoundedRect extends JPanel
	{

		private static final long serialVersionUID = 5629986014016100634L;

		private final Color color1 = UIConstants.BLUE_COLOR;
		private final Color color2 = UIConstants.YELLOW_COLOR;
		private final int cornerRadius = 25;
		private final int width = MessagesListPane.COLORED_LABEL_WIDTH;
		private final int height = MessagesListPane.COLORED_LABEL_HEIGHT;

		public TwoColorRoundedRect()
		{
			super.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			int areaWidth = getWidth();
			// System.out.println("area width is " + areaWidth);
			int areaHeight = getHeight();
			int horizontalOffset = (areaWidth - width) / 2;
			// System.out.println("horizontal offset is " + horizontalOffset);
			int verticalOffset = (areaHeight - height) / 2;

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			super.paintComponent(g);
			// Draws the rounded panel with borders.
			g2d.setColor(color1);
			g2d.fillRoundRect(horizontalOffset, verticalOffset, width, height - 1, cornerRadius, cornerRadius);

			g2d.setColor(color2);
			int rectangularZoneHeight = height / 4;
			g2d.fillRoundRect(horizontalOffset, areaHeight / 2, width, rectangularZoneHeight, 0, 0);
			g2d.fillRoundRect(horizontalOffset, areaHeight / 2, width, height / 2, cornerRadius, cornerRadius);

			((JLabel) this.getComponents()[0]).setBorder(BorderFactory.createEmptyBorder(areaHeight / 2 - height / 2, 0, 0, 0));
			((JLabel) this.getComponents()[1]).setBorder(BorderFactory.createEmptyBorder(height / 4 - UIConstants.REGULAR_FONT.getSize() / 2, 0, 0, 0));
		}
	}
}