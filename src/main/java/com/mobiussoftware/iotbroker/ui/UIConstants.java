package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import java.awt.*;

class UIConstants {
    static final int ACCNT_MGMT_FRAME_WIDTH = 347;
    static final int ACCNT_MGMT_FRAME_HEIGHT = 458;
    static final int MAIN_FRAME_WIDTH = 428;
    static final int MAIN_FRAME_HEIGHT = 533;
    static final int LOGIN_FRAME_WIDTH = 418;
    static final int LOGIN_FRAME_HEIGHT = 548;
    static final int LOGO_FRAME_WIDTH = 299;
    static final int LOGO_FRAME_HEIGHT = 287;

    static final Color APP_BG_COLOR = Color.white;
    static final Color APP_CONTRAST_COLOR = new Color(25, 163, 219);
    static final Color SELECTION_COLOR = new Color(25, 163, 219, 50);
    static final Color ROW_EVEN_COLOR = Color.white;
    static final Color ROW_ODD_COLOR = new Color(245, 245, 245);
    static final Color BLUE_COLOR = new Color(0, 161, 217);
    static final Color YELLOW_COLOR = new Color(252, 227, 79);

    static final String IMAGE_RES_PATH = "src/main/resources/images/";

    static final String TOPIC_LIST_SELECTED_IMG = "ic_tab_topic_list_blue.png";
    static final String SEND_MSG_SELECTED_IMG = "ic_tab_send_msg_blue.png";
    static final String MSG_LIST_SELECTED_IMG = "ic_tab_msg_list_blue.png";
    static final String LOGOUT_SELECTED_IMG = "ic_tab_logout_blue.png";

    static final String TOPIC_LIST_IMG = "ic_tab_topic_list_gray.png";
    static final String SEND_MSG_IMG = "ic_tab_send_msg_gray.png";
    static final String MSG_LIST_IMG = "ic_tab_msg_list_gray.png";
    static final String LOGOUT_IMG = "ic_tab_logout_gray.png";

    private static final String BG_IMG = "img_background.jpg";

    private static final String IC_SETTINGS_FILE_PATH = "ic_settings.png";
    private static final String IC_USERNAME_FILE_PATH = "ic_username.png";
    private static final String IC_PASSWORD_FILE_PATH = "ic_password.png";
    private static final String IC_CLIENT_ID_FILE_PATH = "ic_client_id.png";
    private static final String IC_HOST_PORT_FILE_PATH = "ic_host_port.png";
    private static final String IC_CLEAN_SESSION_FILE_PATH = "ic_clean_session.png";
    private static final String IC_KEEP_ALIVE_FILE_PATH = "ic_keep_alive.png";
    private static final String IC_TRASH_FILE_PATH = "ic_trash.png";

    static final String LOGO_FILE_PATH = "logo.png";
    static final String IC_LOADING_FILE_PATH = "ic_loading_text.png";

    static final Font TEXT_LABEL_FONT = new Font("SansSerif", Font.BOLD, 10);
    static final Font REGULAR_FONT = new Font("SansSerif", Font.PLAIN, 12);
    static final Font REGULAR_BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);
    static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);

    static final Image BG_IMAGE = new ImageIcon(IMAGE_RES_PATH + BG_IMG).getImage();

    static final ImageIcon IC_TRASH = new ImageIcon(IMAGE_RES_PATH + IC_TRASH_FILE_PATH);

    static final ImageIcon IC_SETTINGS = new ImageIcon(IMAGE_RES_PATH + IC_SETTINGS_FILE_PATH);
    static final ImageIcon IC_USERNAME = new ImageIcon(IMAGE_RES_PATH + IC_USERNAME_FILE_PATH);
    static final ImageIcon IC_PASSWORD = new ImageIcon(IMAGE_RES_PATH + IC_PASSWORD_FILE_PATH);
    static final ImageIcon IC_CLIENT_ID = new ImageIcon(IMAGE_RES_PATH + IC_CLIENT_ID_FILE_PATH);
    static final ImageIcon IC_HOST_PORT = new ImageIcon(IMAGE_RES_PATH + IC_HOST_PORT_FILE_PATH);
    static final ImageIcon IC_CLEAN_SESSION = new ImageIcon(IMAGE_RES_PATH + IC_CLEAN_SESSION_FILE_PATH);
    static final ImageIcon IC_KEEP_ALIVE = new ImageIcon(IMAGE_RES_PATH + IC_KEEP_ALIVE_FILE_PATH);
}
