package com.mobiussoftware.iotbroker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

public class AccountMgmtPane extends JPanel
{

	private static final long serialVersionUID = -2902157175553624396L;

	JPanel accountsPane;
	private boolean accountChosen = false;
	private Map<Integer, Component[]> componentList = new HashMap<>();

	AccountMgmtPane(JFrame frame)
	{
		setBackground(UIConstants.APP_BG_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(UIHelper.createAppColorLabel("Please select account", 30));

		accountsPane = new JPanel();
		accountsPane.setBackground(UIConstants.APP_BG_COLOR);

		this.add(UIHelper.wrapInScrollAndBorderLayout(accountsPane, BorderLayout.CENTER));
		// JScrollPane scrollPane = new JScrollPane(accountsPane);
		// JPanel wrapper = new JPanel(new BorderLayout());
		// wrapper.add(scrollPane, BorderLayout.CENTER);
		// this.add(wrapper);

		MouseListener listener = new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				addAccountBtnClicked(arg0);
			}
		};

		this.add(UIHelper.createButton("Add new account", 40, listener));
		final JFrame parent = frame;
		frame.addComponentListener(new ComponentAdapter()
		{
			public void componentHidden(ComponentEvent e)
			{
			}

			public void componentShown(ComponentEvent e)
			{
				accountsPane.removeAll();
				addAccounts();
				parent.revalidate();
				parent.repaint();
			}
		});
	}

	private void addAccounts()
	{
		componentList.clear();

		// final int accountCount = 3;
		final int parameterAlignment = SwingConstants.LEFT;

		accountsPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;

		int count = 0;
		try
		{
			final DBInterface dbInterface = DBHelper.getInstance();

			for (final Account account : dbInterface.accountIterator())
			{
				// for (int i = 0; i < accountCount; i++) {
				int id = account.getId();
				String protocolStr = account.getProtocol().toString();
				String clientIdStr = account.getClientId();
				String hostPortStr = account.getServerHost() + ":" + account.getServerPort();

				final JPanel accountData = new JPanel();
				accountData.setLayout(new BoxLayout(accountData, BoxLayout.Y_AXIS));
				accountData.setBackground(UIConstants.APP_BG_COLOR);

				JLabel protocol = new JLabel(protocolStr, parameterAlignment);
				protocol.setFont(UIConstants.REGULAR_BOLD_FONT);
				protocol.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));
				// protocol.setBorder(new
				// CompoundBorder(BorderFactory.createLineBorder(Color.blue),(BorderFactory.createEmptyBorder(5,5,2,5))));
				accountData.add(protocol);

				JLabel clientId = new JLabel("<html>" + clientIdStr + "</html>", parameterAlignment);
				clientId.setFont(UIConstants.REGULAR_FONT);
				clientId.setBorder(BorderFactory.createEmptyBorder(1, 5, 3, 5));
				// clientId.setBorder(new
				// CompoundBorder(BorderFactory.createLineBorder(Color.blue),BorderFactory.createEmptyBorder(1,
				// 5, 3, 5)));
				accountData.add(clientId);

				JLabel hostPort = new JLabel("<html>" + hostPortStr + "</html>", parameterAlignment);
				hostPort.setFont(UIConstants.REGULAR_FONT);
				hostPort.setBorder(BorderFactory.createEmptyBorder(1, 5, 3, 5));
				// hostPort.setBorder(new
				// CompoundBorder(BorderFactory.createLineBorder(Color.blue),BorderFactory.createEmptyBorder(1,5,3,5)));
				accountData.add(hostPort);

				if (account.getProtocol().equals(Protocol.CoAP))
				{
					accountData.add(new JLabel(" "));
				}

				accountData.setName(String.valueOf(id));

				accountData.addMouseListener(new MouseAdapter()
				{

					@Override
					public void mouseClicked(MouseEvent mouseEvent)
					{
						if (accountChosen)
							return;
						accountChosen = true;

						try
						{
							final DBInterface dbInterface = DBHelper.getInstance();
							dbInterface.markAsDefault(account);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}

						String tag = ((Component) mouseEvent.getSource()).getName();
						final Component[] row = componentList.get(Integer.valueOf(tag));
						row[0].setBackground(UIConstants.SELECTION_COLOR);
						row[1].setBackground(UIConstants.SELECTION_COLOR);

						int delay = 200;
						Timer timer = new Timer(delay, new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent e)
							{
								Main.createAndShowLoadingPane(account);
								row[0].setBackground(UIConstants.APP_BG_COLOR);
								row[1].setBackground(UIConstants.APP_BG_COLOR);
								accountChosen = false;
								Main.hideAccountMgmtPane();
							}
						});
						timer.setRepeats(false);
						timer.start();
					}

					@Override
					public void mouseEntered(MouseEvent mouseEvent)
					{
						super.mouseEntered(mouseEvent);
						String tag = ((Component) mouseEvent.getSource()).getName();
						final Component[] row = componentList.get(Integer.valueOf(tag));
						row[0].setBackground(UIConstants.HOVER_COLOR);
						row[1].setBackground(UIConstants.HOVER_COLOR);
						accountsPane.revalidate();
						accountsPane.repaint();
					}

					@Override
					public void mouseExited(MouseEvent mouseEvent)
					{
						super.mouseExited(mouseEvent);
						String tag = ((Component) mouseEvent.getSource()).getName();
						final Component[] row = componentList.get(Integer.valueOf(tag));
						row[0].setBackground(UIConstants.APP_BG_COLOR);
						row[1].setBackground(UIConstants.APP_BG_COLOR);
						accountsPane.revalidate();
						accountsPane.repaint();
					}
				});

				c.gridx = 0;
				c.gridy = count++;
				c.weightx = 5;
				c.anchor = GridBagConstraints.NORTHWEST;

				accountsPane.add(accountData, c);

				JLabel deleteBtn = new JLabel(UIConstants.IC_TRASH, SwingConstants.CENTER);
				deleteBtn.setBackground(UIConstants.APP_BG_COLOR);
				deleteBtn.setOpaque(true);
				deleteBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				// deleteBtn.setBorder(new
				// CompoundBorder(BorderFactory.createLineBorder(Color.cyan),BorderFactory.createEmptyBorder(5,5,5,5)));
				deleteBtn.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent arg0)
					{

						JLabel btnClicked = (JLabel) arg0.getSource();
						String index = btnClicked.getName();
						int id = Integer.valueOf(index);

						Component[] row = componentList.get(id);
						accountsPane.remove(row[0]);
						accountsPane.remove(row[1]);

						componentList.remove(id);
						accountsPane.revalidate();
						accountsPane.repaint();

						try
						{
							dbInterface.deleteAccount(String.valueOf(id));
						}
						catch (SQLException ex)
						{
							ex.printStackTrace();
						}
					}
				});
				deleteBtn.setName(String.valueOf(id));

				c.gridx = 1;
				c.weightx = 0.1;
				c.anchor = GridBagConstraints.NORTHEAST;

				accountsPane.add(deleteBtn, c);

				Component[] row = new Component[2];
				row[0] = accountData;
				row[1] = deleteBtn;

				componentList.put(id, row);
			}

		}
		catch (Exception e)
		{
			// should not happen
			e.printStackTrace();
		}

		JPanel emptySpace = new JPanel();
		emptySpace.setLayout(new BoxLayout(emptySpace, BoxLayout.Y_AXIS));
		emptySpace.setBackground(Color.white);
		// emptySpace.setBorder(BorderFactory.createLineBorder(Color.blue));
		emptySpace.add(Box.createRigidArea(new Dimension(50, 5)));

		c.weighty = 1;
		c.gridy = count;
		c.gridx = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;

		accountsPane.add(emptySpace, c);
	}

	private void addAccountBtnClicked(MouseEvent event)
	{
		Main.createAndShowLogInPane();
		Main.disposeAccountMgmtPane();
	}
}
