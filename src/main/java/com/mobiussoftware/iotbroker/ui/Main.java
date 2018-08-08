package com.mobiussoftware.iotbroker.ui;

import com.mobiussoftware.iotbroker.dal.api.DBInterface;
import com.mobiussoftware.iotbroker.dal.impl.DBHelper;
import com.mobiussoftware.iotbroker.db.Account;
import com.mobiussoftware.iotbroker.network.NetworkClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main
{

	static final Dimension SCREEN_DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();

	static JFrame logInPane;
	static MainPane mainPane;
	static JFrame accountMgmtPane;
	static JFrame logoPane;
	static NetworkClient client;

	public static void main(String[] args)
	{
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.

		UIDefaults def = UIManager.getLookAndFeelDefaults();
		def.put("ProgressBar.repaintInterval", 10);

		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				//				for (Object key : UIManager.getLookAndFeelDefaults().keySet()) {
				//					boolean tbp = key.toString().startsWith("ProgressBar");
				//					if (tbp)
				//						System.out.println(key + " = " + UIManager.get(key));
				//				}
				try
				{
					final DBInterface dbInterface = DBHelper.getInstance();
					final Account defAccount = dbInterface.getDefaultAccount();
					if (defAccount != null)
					{
						int delay = 200;
						Timer timer = new Timer(delay, new ActionListener()
						{
							@Override public void actionPerformed(ActionEvent e)
							{
								Main.createAndShowLoadingPane(defAccount);
							}
						});
						timer.setRepeats(false);
						timer.start();
					}
					else
					{
						createAndShowAccountMgmtPane();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

			}
		});
	}

	static void setLocation(JFrame frame)
	{
		frame.setLocation(SCREEN_DIMENSION.width / 2 - frame.getSize().width / 2, SCREEN_DIMENSION.height / 2 - frame.getSize().height / 2);
	}

	static void createAndShowLogInPane()
	{

		JFrame frame = new JFrame("Log In");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(new LogInPane());

		frame.pack();
		frame.setVisible(true);
		frame.setSize(new Dimension(UIConstants.LOGIN_FRAME_WIDTH, UIConstants.LOGIN_FRAME_HEIGHT));

		// frame.setLocation(SCREEN_DIMENSION.width/2-frame.getSize().width/2,
		// SCREEN_DIMENSION.height/2-frame.getSize().height/2);
		setLocation(frame);

		logInPane = frame;
	}

	static void disposeLogInPane()
	{
		if (logInPane != null)
		{
			logInPane.dispose();
		}
	}

	static MainPane createAndShowMainPane(Account account)
			throws Exception
	{
		MainPane frame = new MainPane(account);
		frame.setTitle(account.getProtocol().toString());
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(UIConstants.MAIN_FRAME_WIDTH, UIConstants.MAIN_FRAME_HEIGHT));

		setLocation(frame);

		mainPane = frame;

		return mainPane;
	}

	static void disposeMainPane()
	{
		if (mainPane != null)
		{
			mainPane.dispose();
		}
	}

	static void createAndShowAccountMgmtPane()
	{

		// UIDefaults def = UIManager.getLookAndFeelDefaults();

		JFrame frame = new JFrame("Accounts");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(new AccountMgmtPane());

		frame.pack();
		frame.setVisible(true);
		frame.setSize(new Dimension(UIConstants.ACCNT_MGMT_FRAME_WIDTH, UIConstants.ACCNT_MGMT_FRAME_HEIGHT));

		setLocation(frame);

		accountMgmtPane = frame;
	}

	static void hideAccountMgmtPane()
	{
		if (accountMgmtPane != null)
		{
			accountMgmtPane.setVisible(false);
		}
	}

	static void showAccountMgmtPane()
	{
		if (accountMgmtPane == null)
		{
			createAndShowAccountMgmtPane();
		}
		else if (!accountMgmtPane.isVisible())
		{
			accountMgmtPane.setVisible(true);
			System.out.println("showing hidden mgmt pane");
			//		} else {
			//			System.out.println("creating account mgmt pane again");
			//			createAndShowAccountMgmtPane();
		}
	}

	static void disposeAccountMgmtPane()
	{
		System.out.print("disposing accnt mgmt pane: ");
		if (accountMgmtPane != null)
		{
			accountMgmtPane.dispose();
		}
		System.out.println(accountMgmtPane);
	}

	public static void createAndShowLoadingPane(Account account)
	{

		JFrame frame = new JFrame(account.getProtocol().toString());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.getContentPane().add(new LoadingPane(account));

		frame.pack();
		frame.setVisible(true);
		frame.setSize(new Dimension(UIConstants.LOGO_FRAME_WIDTH, UIConstants.LOGO_FRAME_HEIGHT));
		frame.setResizable(false);

		setLocation(frame);

		logoPane = frame;
	}

	static void disposeLogoPane()
	{
		if (logoPane != null)
		{
			logoPane.dispose();
		}
	}

	public static NetworkClient getCurrentClient()
	{
		return client;
	}

	public static void updateCurrentClient(NetworkClient client)
	{
		Main.client = client;
	}
}
