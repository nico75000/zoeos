package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 25-Jan-2004
 * Time: 09:56:32
 */
public class PopupCategoryLabel extends JPanel {
    public PopupCategoryLabel(String text) {
        super(new BorderLayout());
        JLabel j = new JLabel(text);
        add(j, BorderLayout.CENTER);
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }
}
