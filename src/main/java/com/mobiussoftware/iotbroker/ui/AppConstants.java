package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import java.awt.*;

public class AppConstants {
    static final int WIDTH = 428;
    static final int HEIGHT = 533;

    static final Color APP_COLOR = new Color(25, 163, 219);
    static final Color SELECTION_COLOR = new Color(25, 163, 219, 50);
    static final Color CONTRAST_LIST_COLOR = new Color(245, 245, 245);
    static final Color BLUE_COLOR = new Color(0, 161, 217);
    static final Color YELLOW_COLOR = new Color(252, 227, 79);

    static final String IMAGE_RES_PATH = "src/main/resources/images/";

    static final String TOPIC_LIST_SELECTED_IMG = "ic_topic_list_blue.png";
    static final String SEND_MSG_SELECTED_IMG = "ic_send_msg_blue.png";
    static final String MSG_LIST_SELECTED_IMG = "ic_msg_list_blue.png";
    static final String LOGOUT_SELECTED_IMG = "ic_logout_blue.png";

    static final String TOPIC_LIST_IMG = "ic_topic_list_gray.png";
    static final String SEND_MSG_IMG = "ic_send_msg_gray.png";
    static final String MSG_LIST_IMG = "ic_msg_list_gray.png";
    static final String LOGOUT_IMG = "ic_logout_gray.png";

    static final String BG_IMG = "img_background.jpg";

    static final String IC_SETTINGS_FILE_PATH = "ic_settings.png";
    static final String IC_TRASH_FILE_PATH = "ic_trash.png";

    static final Font TEXT_LABEL_FONT = new Font("SansSerif", Font.BOLD, 10);
    static final Font REGULAR_FONT = new Font("SansSerif", Font.PLAIN, 12);
    static final Font REGULAR_BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);

    static final Image BG_IMAGE = new ImageIcon(IMAGE_RES_PATH + BG_IMG).getImage();
    static final ImageIcon IC_TRASH = new ImageIcon(IMAGE_RES_PATH + IC_TRASH_FILE_PATH);
    static final ImageIcon IC_SETTINGS = new ImageIcon(IMAGE_RES_PATH + IC_SETTINGS_FILE_PATH);
}
