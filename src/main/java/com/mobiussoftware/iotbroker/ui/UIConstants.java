package com.mobiussoftware.iotbroker.ui;

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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class UIConstants
{
	public static final Color APP_CONTRAST_COLOR = new Color(25, 163, 219);
	static final int ACCNT_MGMT_FRAME_WIDTH = 347;
	static final int ACCNT_MGMT_FRAME_HEIGHT = 458;
	static final int MAIN_FRAME_WIDTH = 428;
	static final int MAIN_FRAME_HEIGHT = 533;
	static final int LOGIN_FRAME_WIDTH = 418;
	static final int LOGIN_FRAME_HEIGHT = 548;
	static final int LOGO_FRAME_WIDTH = 299;
	static final int LOGO_FRAME_HEIGHT = 287;
	static final Color APP_BG_COLOR = Color.white;
	static final Color SELECTION_COLOR = new Color(25, 186, 248, 21);
	static final Color HOVER_COLOR = new Color(252, 227, 79, 50);
	static final Color ROW_EVEN_COLOR = Color.white;
	static final Color ROW_ODD_COLOR = new Color(245, 245, 245);
	static final Color BLUE_COLOR = new Color(0, 161, 217);
	static final Color YELLOW_COLOR = new Color(252, 227, 79);

	static final String IMAGES_PATH = "/images/";

	static final String TOPIC_LIST_SELECTED_IMG = "ic_tab_topic_list_blue.png";
	static final String SEND_MSG_SELECTED_IMG = "ic_tab_send_msg_blue.png";
	static final String MSG_LIST_SELECTED_IMG = "ic_tab_msg_list_blue.png";
	static final String LOGOUT_SELECTED_IMG = "ic_tab_logout_blue.png";

	static final String TOPIC_LIST_IMG = "ic_tab_topic_list_gray.png";
	static final String SEND_MSG_IMG = "ic_tab_send_msg_gray.png";
	static final String MSG_LIST_IMG = "ic_tab_msg_list_gray.png";
	static final String LOGOUT_IMG = "ic_tab_logout_gray.png";
	static final String LOGO_FILE_PATH = "logo.png";
	static final String IC_LOADING_FILE_PATH = "ic_loading_text.png";
	static final Font TEXT_LABEL_FONT = new Font("SansSerif", Font.BOLD, 10);
	static final Font REGULAR_FONT = new Font("SansSerif", Font.PLAIN, 12);
	static final Font REGULAR_BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);
	static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);
	private static final String BG_IMG = "img_background.jpg";
	private static final String IC_SETTINGS_FILE_PATH = "ic_settings.png";
	private static final String IC_USERNAME_FILE_PATH = "ic_username.png";
	private static final String IC_PASSWORD_FILE_PATH = "ic_password.png";
	private static final String IC_CLIENT_ID_FILE_PATH = "ic_client_id.png";
	private static final String IC_HOST_PORT_FILE_PATH = "ic_host_port.png";
	private static final String IC_CLEAN_SESSION_FILE_PATH = "ic_clean_session.png";
	private static final String IC_KEEP_ALIVE_FILE_PATH = "ic_keep_alive.png";
	private static final String IC_TRASH_FILE_PATH = "ic_trash.png";
	static Image BG_IMAGE;
	static ImageIcon IC_TRASH;
	static ImageIcon IC_SETTINGS;
	static ImageIcon IC_USERNAME;
	static ImageIcon IC_PASSWORD;
	static ImageIcon IC_CLIENT_ID;
	static ImageIcon IC_HOST_PORT;
	static ImageIcon IC_CLEAN_SESSION;
	static ImageIcon IC_KEEP_ALIVE;

	static
	{
		BG_IMAGE = initImage(IMAGES_PATH + BG_IMG);
		IC_TRASH = initImageIcon(IMAGES_PATH + IC_TRASH_FILE_PATH);
		IC_SETTINGS = initImageIcon(IMAGES_PATH + IC_SETTINGS_FILE_PATH);
		IC_USERNAME = initImageIcon(IMAGES_PATH + IC_USERNAME_FILE_PATH);
		IC_PASSWORD = initImageIcon(IMAGES_PATH + IC_PASSWORD_FILE_PATH);
		IC_CLIENT_ID = initImageIcon(IMAGES_PATH + IC_CLIENT_ID_FILE_PATH);
		IC_HOST_PORT = initImageIcon(IMAGES_PATH + IC_HOST_PORT_FILE_PATH);
		IC_CLEAN_SESSION = initImageIcon(IMAGES_PATH + IC_CLEAN_SESSION_FILE_PATH);
		IC_KEEP_ALIVE = initImageIcon(IMAGES_PATH + IC_KEEP_ALIVE_FILE_PATH);
	}

	public static Image initImage(String path)
	{
		try
		{
			InputStream in = UIConstants.class.getResourceAsStream(path);
			return ImageIO.read(in);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static ImageIcon initImageIcon(String path)
	{
		return new ImageIcon(initImage(path));
	}
}
