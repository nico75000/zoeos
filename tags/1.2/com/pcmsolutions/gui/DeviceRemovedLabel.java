package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-Apr-2003
 * Time: 20:33:54
 * To change this template use Options | File Templates.
 */
public class DeviceRemovedLabel extends JLabel {
    public DeviceRemovedLabel() {
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.setAlignmentY(Component.TOP_ALIGNMENT);
        this.setText("DEVICE REMOVED");
        this.setFont(new Font("MONOSPACED", Font.BOLD, 16));
        this.setIcon(new ImageIcon("toolbarButtonGraphics/general/delete16.gif"));
        this.setIcon(new ImageIcon("toolbarButtonGraphics/general/delete16.gif"));
    }
        public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }
}
