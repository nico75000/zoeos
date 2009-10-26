/*
 * PresetIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.preset.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 *
 * @author  pmeehan
 */
public class PresetContextIcon implements Icon {
    private int w, h;
    private static Color c1 = Color.red;
    private static Color c2 = Color.BLUE;
    private static Color gradient = Color.white;

    /** Creates a new instance of PresetIcon */
    public PresetContextIcon(int w, int h) {
        this.w = w;
        this.h = h;
        this.c1 = UIColors.applyAlpha(c1, UIColors.iconAlpha);
        this.c2 = UIColors.applyAlpha(c2, UIColors.iconAlpha);
    }

    public int getIconHeight() {
        return h;
    }

    public int getIconWidth() {
        return w;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g2d = ((Graphics2D) graphics);

        Arc2D arc = new Arc2D.Double(x, y, w, h, 270, 180, Arc2D.CHORD);
        GradientPaint gp = new GradientPaint(x, y, gradient, x + w, y + h, c1, false);
        g2d.setPaint(gp);
        g2d.fill(arc);

        arc = new Arc2D.Double(x, y, w, h, 90, 180, Arc2D.CHORD);
        gp = new GradientPaint(x, y, gradient, x + w, y + h, c2, false);
        g2d.setPaint(gp);
        g2d.fill(arc);
    }
}
