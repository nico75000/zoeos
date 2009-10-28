/*
 * SampleIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.gui.piano;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * @author pmeehan
 */
public class PianoIcon implements Icon {
    private int w, h;
    private Color c1, c2;

    /**
     * Creates a new instance of SampleIcon
     */
    public PianoIcon(int w, int h) {
        this(w, h, Color.white, Color.blue);
    }

    public PianoIcon(int w, int h, Color c1, Color c2) {
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
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        GeneralPath c = new GeneralPath();

        c.moveTo(x, y + h);
        c.lineTo(x + w, y + h);
        c.lineTo(x + w, y);
        c.lineTo(x, y);
        c.closePath();

        GradientPaint gp = new GradientPaint(x, y, c1, x, y + h, c2, false);
        g2d.setPaint(gp);
        g2d.fill(c);
        g2d.setColor(UIColors.applyAlpha(c2, 75));
       
        Shape s;
        s = new Rectangle2D.Double(x + w / 10, y + h / 8, w / 5, h / 2);
        g2d.fill(s);
        s = new Rectangle2D.Double(x + (w * 4) / 10, y + h / 8, w / 5, h / 2);
        g2d.fill(s);
        s = new Rectangle2D.Double(x + (w * 7) / 10, y + h / 8, w / 5, h / 2);
        g2d.fill(s);
    }
}
