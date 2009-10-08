/*
 * PresetIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 *
 * @author  pmeehan
 */
public class SampleContextIcon implements Icon {
    private int w, h;
    private static Color c1 = Color.RED;
    private static Color c2 = Color.BLUE;
    private static Color gradient = Color.white;

    public SampleContextIcon(int w, int h) {
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

        GeneralPath s = new GeneralPath();

        s.moveTo(x, y + h);
        s.lineTo(x + w / 2, y + h);
        s.lineTo(x + w / 2, y);
        s.closePath();

        GradientPaint gp = new GradientPaint(x, y, gradient, x + w, y + h, c1, false);
        g2d.setPaint(gp);
        g2d.fill(s);

        s.reset();
        s.moveTo(x + w / 2, y);
        s.lineTo(x + w, y+h);
        s.lineTo(x + w/2, y+h);
        s.closePath();

        gp = new GradientPaint(x, y, gradient, x + w, y + h, c2, false);
        g2d.setPaint(gp);
        g2d.fill(s);
    }
}
