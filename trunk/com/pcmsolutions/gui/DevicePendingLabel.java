package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-Apr-2003
 * Time: 20:33:54
 * To change this template use Options | File Templates.
 */
public class DevicePendingLabel extends JLabel {
    public DevicePendingLabel() {
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.setAlignmentY(Component.TOP_ALIGNMENT);
        this.setText("DEVICE PENDING");
        this.setFont(new Font("MONOSPACED", Font.BOLD, 16));
        URL url = DevicePendingLabel.class.getResource("/pause16.gif");
        if (url != null)
            this.setIcon(new ImageIcon(url));
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }
}
