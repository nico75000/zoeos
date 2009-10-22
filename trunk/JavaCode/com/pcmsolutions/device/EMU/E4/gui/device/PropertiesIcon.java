/*
 * SampleIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.device;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author  pmeehan
 */
public class PropertiesIcon implements Icon {
    private int w, h;
    private Color c1, c2;

    /** Creates a new instance of SampleIcon */
    public PropertiesIcon(int w, int h) {
        this(w, h, Color.white, Color.blue, false);
    }

    public PropertiesIcon(int w, int h, Color c1, Color c2) {
        this(w, h, c1, c2, false);
    }

    public PropertiesIcon(int w, int h, Color c1, Color c2, boolean empty) {
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

        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);

        Rectangle2D rect1 = new Rectangle2D.Double(x, y + h / 8, w / 2, (h * 3) / 8);
        Rectangle2D rect2 = new Rectangle2D.Double(x, y + (h * 5) / 8, w / 2, (h * 3) / 8);
        Line2D line1 = new Line2D.Double(rect1.getCenterX(), rect1.getCenterY(), x + w * 0.8, rect1.getCenterY());
        Line2D line2 = new Line2D.Double(rect2.getCenterX(), rect2.getCenterY(), x + w * 0.8, rect2.getCenterY());
        GradientPaint gp = new GradientPaint(x, y, c1, x + w, y, c2, false);
        g2d.setPaint(gp);
        g2d.fill(rect1);
        g2d.fill(rect2);
        g2d.draw(line1);
        g2d.draw(line2);
        g2d.setRenderingHints(hints);
    }
}
