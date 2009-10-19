/*
 * SampleIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author  pmeehan
 */
public class EmptyIcon implements Icon {
    private static EmptyIcon INSTANCE = new EmptyIcon();

    public static EmptyIcon getInstance() {
        return INSTANCE;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y) {
    }

    public int getIconWidth() {
        return 0;
    }

    public int getIconHeight() {
        return 0;
    }
}
