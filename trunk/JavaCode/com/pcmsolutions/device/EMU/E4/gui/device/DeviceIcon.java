/*
 * SampleIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.device;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author  pmeehan
 */
public class DeviceIcon implements Icon {
    private int w, h;
    private Color c1, c2;
    private boolean empty;

    /** Creates a new instance of SampleIcon */
    public DeviceIcon(int w, int h) {
        this(w, h, Color.white, Color.darkGray, false);
    }

    public DeviceIcon(int w, int h, Color c1, Color c2) {
        this(w, h, c1, c2, false);
    }

    public DeviceIcon(int w, int h, Color c1, Color c2, boolean empty) {
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
        //Shape c = new Ellipse2D.Double(x, y, w, h);
        //Shape c = new Rectangle2D.Double(x,y, w, h);
        GeneralPath c = new GeneralPath();

        if (empty) {
            int eh = h - 2;
            int ew = w - 2;
            int ex = x + 1;
            int ey = y + 1;
            c.moveTo(ex, ey + eh);
            c.lineTo(ex + ew, ey + eh);
            c.lineTo(ex + ew, ey);
            c.lineTo(ex, ey);
            c.closePath();
            g2d.setColor(c1);
            g2d.draw(c);
            return;
        }
        c.moveTo(x, y + h);
        c.lineTo(x + w, y + h);
        c.lineTo(x + w, y);
        c.lineTo(x, y);
        c.closePath();

        GradientPaint gp = new GradientPaint(x, y, c1, x + w, y, c2, false);
        g2d.setPaint(gp);
        g2d.fill(c);
        // Shape s = new Ellipse2D.Double(x + (w * 5) / 8, y + h / 4, (w * 3) / 8, h / 2);
        g2d.setColor(UIColors.applyAlpha(c2, 75));

        Shape s;
        s = new Rectangle2D.Double(x + w / 8, y + h / 8, w / 2, h / 4);
        g2d.fill(s);
        s = new Rectangle2D.Double(x + w / 8, y + (h * 5) / 8, w / 2, h / 4);
        g2d.fill(s);
    }
}
