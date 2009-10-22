/*
 * SampleIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.sample;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.system.Zoeos;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * @author pmeehan
 */
public class SampleIcon implements Icon {
    private int w, h;
    private Color c1, c2;
    private boolean empty;

    /**
     * Creates a new instance of SampleIcon
     */
    public SampleIcon(int w, int h, Color c1, Color c2) {
        this(w, h, c1, c2, false);
    }

    public SampleIcon(int w, int h, Color c1, Color c2, boolean empty) {
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
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        GeneralPath c = new GeneralPath();
        if (empty) {
            int eh = h - 2;
            int ew = w - 2;
            int ex = x + 1;
            int ey = y + 1;
            c.moveTo(ex, ey + eh);
            c.lineTo(ex + ew, ey + eh);
            c.lineTo(ex + ew / 2, ey);
            c.closePath();
            g2d.setColor(c1);
            g2d.draw(c);
        } else {
            c.moveTo(x, y + h);
            c.lineTo(x + w, y + h);
            c.lineTo(x + w / 2, y);
            c.closePath();

            GradientPaint gp = new GradientPaint(x, y, c1, w, h, c2, false);
            g2d.setPaint(gp);
            g2d.fill(c);
            g2d.setRenderingHints(hints);
        }
        g2d.setRenderingHints(hints);
    }
}
