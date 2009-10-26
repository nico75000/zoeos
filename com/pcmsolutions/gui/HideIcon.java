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

/**
 *
 * @author  pmeehan
 */
public class HideIcon implements Icon {
    private int w, h;
    private Color c1, c2;
    private boolean empty;
    private static HideIcon INSTANCE;

    public static HideIcon getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HideIcon(40, 8, Color.LIGHT_GRAY, Color.DARK_GRAY);
        return INSTANCE;
    }

    /** Creates a new instance of SampleIcon */
    public HideIcon(int w, int h, Color c1, Color c2) {
        this(w, h, c1, c2, false);
    }

    public HideIcon(int w, int h, Color c1, Color c2, boolean empty) {
        this.w = w;
        this.h = h;
        this.c1 = UIColors.applyAlpha(c1, UIColors.iconAlpha);
        this.c2 = UIColors.applyAlpha(c2, UIColors.iconAlpha);
        this.empty = empty;
    }

    public int getIconHeight() {
        return h;
    }

    public int getIconWidth() {
        return w;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g2d = ((Graphics2D) graphics);
        GeneralPath c = new GeneralPath();

        if (empty) {
            int eh = h - 2;
            int ew = w - 2;
            int ex = x + 1;
            int ey = y + 1;
            c.moveTo(ex, ey);
            c.lineTo(ex + ew, ey);
            c.lineTo(ex + ew / 2, ey = eh);
            c.closePath();
            g2d.setColor(c1);
            g2d.draw(c);
            return;
        }
        c.moveTo(x, y);
        c.lineTo(x + w, y);
        c.lineTo(x + w / 2, y + h);
        c.closePath();

        GradientPaint gp = new GradientPaint(0, 0, c2, 0, h, c1, false);
        g2d.setPaint(gp);
        g2d.fill(c);
    }
}
