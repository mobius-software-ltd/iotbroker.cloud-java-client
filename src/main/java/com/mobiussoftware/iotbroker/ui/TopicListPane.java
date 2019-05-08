package com.mobiussoftware.iotbroker.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

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

import com.mobius.software.mqtt.parser.avps.QoS;
import com.mobius.software.mqtt.parser.avps.Text;
import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.db.DBTopic;
import com.mobiussoftware.iotbroker.network.TopicListener;
import com.mobiussoftware.iotbroker.ui.elements.CustomComboBoxUI;
import com.mobiussoftware.iotbroker.ui.elements.HintTextField;
import com.mobiussoftware.iotbroker.ui.elements.TopicComponent;

public class TopicListPane extends JPanel implements TopicListener
{

	private static final long serialVersionUID = -615880735007928743L;

	private Account account;

	private JPanel emptySpace;
	private JPanel topics;

	private JPanel progressBarSpace;
	private JProgressBar progressBar;

	private Map<String, TopicComponent> componentsMap = new TreeMap<>();

	private HintTextField topicInput;
	private JComboBox<Integer> dropDown;

	private JPanel addTopicBtn;

	TopicListPane(Account account)
	{
		super();
		this.account = account;
		drawUI();
	}

	private void drawUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		emptySpace = new JPanel();
		emptySpace.setLayout(new BoxLayout(emptySpace, BoxLayout.Y_AXIS));
		emptySpace.setBackground(Color.white);
		// emptySpace.setBorder(BorderFactory.createLineBorder(Color.blue));
		emptySpace.add(Box.createRigidArea(new Dimension(50, 5)));

		progressBarSpace = UIHelper.createProgressBarSpace(5);
		this.add(progressBarSpace);

		JPanel txtLbl1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		txtLbl1.setBackground(new Color(0, 0, 0, 0));

		JLabel topicListLbl = new JLabel("topics list:", SwingConstants.LEFT);
		topicListLbl.setFont(UIConstants.TEXT_LABEL_FONT);

		txtLbl1.add(topicListLbl);

		this.add(txtLbl1);

		topics = new JPanel();
		topics.setBackground(Color.white);

		this.add(UIHelper.wrapInScrollAndBorderLayout(topics, BorderLayout.CENTER));

		JPanel txtLbl2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		txtLbl2.setBackground(new Color(0, 0, 0, 0));

		JLabel addTopicLbl = new JLabel("add new topic:");
		addTopicLbl.setFont(UIConstants.TEXT_LABEL_FONT);

		txtLbl2.add(addTopicLbl);

		this.add(txtLbl2);

		final JPanel addTopic = new JPanel();
		addTopic.setBackground(Color.white);
		addTopic.setBorder(BorderFactory.createLineBorder(Color.lightGray));

		addTopic.setMinimumSize(new Dimension(410, 70));
		addTopic.setPreferredSize(addTopic.getMinimumSize());

		this.add(addTopic);

		addTopicBtn = UIHelper.createButton("Add", getMouseListener());
		this.add(addTopicBtn);

		addTopicListElements(topics);
		addAddTopicElements(addTopic);
	}

	private MouseListener getMouseListener()
	{
		return new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				// System.out.println("Add button clicked!");
				//arg0.getComponent().removeMouseListener(this);
				addTopicAction();
				dropDown.setSelectedIndex(0);
			}
		};
	}

	// adding subelements to topicList panel
	private void addTopicListElements(final JPanel parent)
	{

		parent.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.VERTICAL;

		int count = 0;
		try
		{
			final DBInterface dbInterface = DBHelper.getInstance();

			for (DBTopic tp : dbInterface.getTopics(account))
			{
				String topicName = tp.getName();
				String qosValue = String.valueOf(tp.getQos());

				JLabel topic = new JLabel(topicName, SwingConstants.LEFT);
				topic.setFont(UIConstants.REGULAR_FONT);
				topic.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

				c.gridx = 0;
				c.gridy = count++;
				c.weightx = 10;
				c.anchor = GridBagConstraints.NORTHWEST;

				parent.add(topic, c);

				JLabel qos = new RoundedFilledLabel(new Color(252, 227, 79), 20, 0, 4);
				qos.setText("QoS:" + qosValue);
				qos.setHorizontalAlignment(SwingConstants.LEFT);
				qos.setFont(UIConstants.REGULAR_FONT);
				qos.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

				c.gridx = 1;
				c.weightx = 0.1;
				c.anchor = GridBagConstraints.NORTHEAST;

				parent.add(qos, c);

				JLabel deleteBtn = new JLabel(UIConstants.IC_TRASH, SwingConstants.CENTER);
				deleteBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				deleteBtn.addMouseListener(deleteTopicAction());
				deleteBtn.setName(topicName);

				c.gridx = 2;
				c.weightx = 0.1;
				c.anchor = GridBagConstraints.NORTHEAST;

				parent.add(deleteBtn, c);

				Component[] row = new Component[3];
				row[0] = topic;
				row[1] = qos;
				row[2] = deleteBtn;

				TopicComponent topicComponent = new TopicComponent(topicName, row);
				componentsMap.put(topicName, topicComponent);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		c.weighty = 1;
		c.gridy = count;
		c.gridx = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;

		parent.add(emptySpace, c);
	}

	private void addTopicListRow(String topicName, int qosValue)
	{
		int rowNumber = ((GridBagLayout) topics.getLayout()).getLayoutDimensions()[1].length - 1;

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;

		JLabel topic = new JLabel(topicName, SwingConstants.LEFT);
		topic.setFont(UIConstants.REGULAR_FONT);
		topic.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		c.gridx = 0;
		c.gridy = rowNumber;
		c.weightx = 10;
		c.weighty = 0;
		c.anchor = GridBagConstraints.NORTHWEST;

		topics.add(topic, c);

		JLabel qos = new RoundedFilledLabel(new Color(252, 227, 79), 20, 0, 4);
		qos.setText("QoS:" + qosValue);
		qos.setHorizontalAlignment(SwingConstants.LEFT);
		qos.setFont(UIConstants.REGULAR_FONT);
		qos.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		c.gridx = 1;
		c.weightx = 0.1;
		c.weighty = 0;
		c.anchor = GridBagConstraints.NORTHEAST;

		topics.add(qos, c);
		//
		JLabel deleteBtn = new JLabel(UIConstants.IC_TRASH, SwingConstants.CENTER);
		deleteBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		deleteBtn.addMouseListener(deleteTopicAction());

		deleteBtn.setName(topicName);

		c.gridx = 2;
		c.weightx = 0.1;
		c.weighty = 0;
		c.anchor = GridBagConstraints.NORTHEAST;

		topics.add(deleteBtn, c);
		topics.revalidate();
		topics.repaint();

		Component[] row = new Component[3];
		row[0] = topic;
		row[1] = qos;
		row[2] = deleteBtn;

		TopicComponent topicComponent = new TopicComponent(topicName, row);
		componentsMap.put(topicName, topicComponent);

		c.weighty = 1;
		c.gridy = rowNumber + 1;
		c.gridx = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;

		topics.add(emptySpace, c);
	}

	private void deleteListRow(String name)
	{
		TopicComponent topicComponent = componentsMap.remove(name);
		if (topicComponent != null)
		{
			for (Component component : topicComponent.getComponents())
				topics.remove(component);
		}

		topics.revalidate();
		topics.repaint();
	}

	// adding subelements to addtopic panel
	private void addAddTopicElements(JPanel parent)
	{
		UIManager.put("ComboBox.background", new ColorUIResource(Color.white));
		UIManager.put("ComboBox.selectionBackground", UIConstants.SELECTION_COLOR);
		UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.gray));

		JPanel el1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		el1.setBackground(Color.white);
		Image tmp2 = UIConstants.IC_SETTINGS.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		JLabel icon = new JLabel(new ImageIcon(tmp2));
		icon.setBorder(new EmptyBorder(0, 10, 0, 10));
		JLabel text = new JLabel("Topic:");
		text.setFont(UIConstants.REGULAR_FONT);

		el1.add(icon);
		el1.add(text);

		JPanel el2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		el2.setBackground(Color.white);

		topicInput = new HintTextField("topic", BorderFactory.createLineBorder(Color.lightGray));
		topicInput.setHorizontalAlignment(JTextField.RIGHT);
		topicInput.setFont(UIConstants.REGULAR_FONT);
		topicInput.setMinimumSize(new Dimension(150, 28));
		topicInput.setPreferredSize(topicInput.getMinimumSize());

		el2.add(topicInput);

		JPanel el3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		el3.setBackground(UIConstants.ROW_ODD_COLOR);

		text = new JLabel("QoS:");
		text.setFont(UIConstants.REGULAR_FONT);
		JLabel icon2 = new JLabel(new ImageIcon(tmp2));
		icon2.setBorder(new EmptyBorder(0, 10, 0, 10));
		dropDown = new JComboBox<>(AppConstants.qosValues(account.getProtocol()));

		JPanel el4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		el4.setBackground(UIConstants.ROW_ODD_COLOR);

		JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		wrapper.setBackground(Color.yellow);
		wrapper.setMinimumSize(new Dimension(72, 24));
		wrapper.setPreferredSize(wrapper.getMinimumSize());
		wrapper.setBorder(BorderFactory.createLineBorder(Color.lightGray));

		dropDown.setFont(UIConstants.REGULAR_FONT);
		dropDown.setMinimumSize(new Dimension(70, 22));
		dropDown.setPreferredSize(dropDown.getMinimumSize());
		dropDown.setUI(CustomComboBoxUI.createUI(dropDown));
		dropDown.setEnabled(true);
		dropDown.setSelectedIndex(0);

		BasicComboBoxRenderer renderer = (BasicComboBoxRenderer) dropDown.getRenderer();
		renderer.setBorder(new EmptyBorder(0, 7, 0, 0));

		el4.add(wrapper);
		wrapper.add(dropDown);

		el3.add(icon2);
		el3.add(text);

		parent.setLayout(new GridLayout(2, 2));
		parent.add(el1);
		parent.add(el2);
		parent.add(el3);
		parent.add(el4);
	}

	private void addTopicAction()
	{
		if (UIHelper.validateTF(topicInput))
		{
			int qos = (int) dropDown.getSelectedItem();

			addProgressBar();

			AddTopicTask task = new AddTopicTask(topicInput.getText(), qos);
			task.addPropertyChangeListener(propertyChangeListener());
			task.execute();
		}
	}

	private MouseListener deleteTopicAction()
	{
		return new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				JLabel btnClicked = (JLabel) arg0.getSource();
				btnClicked.removeMouseListener(this);

				addProgressBar();

				DeleteTopicTask task = new DeleteTopicTask(btnClicked.getName());
				task.addPropertyChangeListener(propertyChangeListener());
				task.execute();
			}
		};
	}

	@Override
	public void finishAddingTopic(String topicName, int qosVal)
	{
		TopicComponent curr = componentsMap.get(topicName);
		if (curr != null)
		{
			JLabel qos = (JLabel) curr.getComponents()[1];
			qos.setText("QoS:" + qosVal);
			topics.revalidate();
			topics.repaint();
		}
		else
			addTopicListRow(topicName, qosVal);
	}

	@Override
	public void finishAddingTopicFailed()
	{
		JOptionPane.showMessageDialog(this.getParent(), "Failed to add topic.");
	}

	@Override
	public void finishDeletingTopic(String name)
	{
		deleteListRow(name);
	}

	private PropertyChangeListener propertyChangeListener()
	{
		return new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getPropertyName() == "progress")
				{
					int progress = (Integer) evt.getNewValue();
					progressBar.setValue(progress);
				}
			}
		};
	}

	private void addProgressBar()
	{
		progressBar = UIHelper.createProgressBar();
		if (progressBarSpace.getComponents().length > 0)
		{
			for (Component c : progressBarSpace.getComponents())
			{
				progressBarSpace.remove(c);
			}
		}
		progressBarSpace.add(progressBar);
		progressBar.revalidate();
	}

	private void removeProgressBar()
	{
		progressBarSpace.remove(progressBar);
		TopicListPane.this.revalidate();
		TopicListPane.this.repaint();
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

	class AddTopicTask extends NetworkTask<Void, Void>
	{
		private String topicName;
		private int qosVal;

		public AddTopicTask(String topicName, int qosVal)
		{
			this.topicName = topicName;
			this.qosVal = qosVal;
		}

		@Override
		public Void doInBackground()
		{
			try
			{
				if (!topicExists())
				{
					Main.getCurrentClient().subscribe(new com.mobius.software.mqtt.parser.avps.Topic[]
					{ new com.mobius.software.mqtt.parser.avps.Topic(new Text(topicName), QoS.valueOf(qosVal)) });
				}
				else
					finishAddingTopic(topicName, qosVal);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return super.doInBackground();
		}

		private boolean topicExists()
		{
			try
			{
				final DBInterface dbInterface = DBHelper.getInstance();
				for (DBTopic tp : dbInterface.getTopics(account))
				{
					if (Objects.equals(tp.getName(), topicName) && Objects.equals(tp.getQos(), (byte) qosVal))
						return true;
				}
			}
			catch (Exception e)
			{
			}
			return false;
		}

		@Override
		protected void done()
		{
			topicInput.setText("");
			removeProgressBar();
		}
	}

	class DeleteTopicTask extends NetworkTask<Void, Void>
	{

		private String id;

		public DeleteTopicTask(String id)
		{
			this.id = id;
		}

		@Override
		public Void doInBackground()
		{
			try
			{
				Main.getCurrentClient().unsubscribe(new String[]
				{ id });
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return super.doInBackground();
		}

		@Override
		public void done()
		{
			removeProgressBar();
		}
	}

	class RoundedFilledLabel extends JLabel
	{

		private static final long serialVersionUID = -8353580452631276508L;

		private Color color;
		private int cornerRadius;
		private int verticalOffset;
		private int horizontalOffset;

		RoundedFilledLabel(Color color, int cornerRadius, int horizontalOffset, int verticalOffset)
		{
			this.color = color;
			this.cornerRadius = cornerRadius;
			this.verticalOffset = verticalOffset;
			this.horizontalOffset = horizontalOffset;
		}

		@Override
		protected void paintComponent(Graphics g)
		{

			int width = getWidth();
			int height = getHeight();
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Draws the rounded panel with borders.
			g2d.setColor(color);
			g2d.fillRoundRect(horizontalOffset, verticalOffset, width - horizontalOffset - 1, height - verticalOffset - 3, cornerRadius, cornerRadius);// paint
			// background
			super.paintComponent(g);
		}
	}
}