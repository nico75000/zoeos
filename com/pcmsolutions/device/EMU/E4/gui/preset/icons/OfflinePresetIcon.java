/*
 * PresetIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.preset.icons;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 *
 * @author  pmeehan
 */
public class OfflinePresetIcon implements Icon {
    /** Creates a new instance of PresetIcon */
    public OfflinePresetIcon(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public int getIconHeight() {
        return h;
    }

    public int getIconWidth() {
        return w;
    }

    public void paintIcon(java.awt.Component component, java.awt.Graphics graphics, int x, int y) {
        Graphics2D g2d = ((Graphics2D) graphics);
        Color halfRed = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 127);
        //g2d.setColor(halfRed);
        Shape mainCircle = new Ellipse2D.Double(x, y, w, h);
        GradientPaint gp = new GradientPaint(75, 75, Color.blue, 95, 95, Color.white, true);
        g2d.setPaint(gp);
        g2d.fill(mainCircle);

        g2d.setColor(halfRed);
        //Shape onlineCircle  = new Ellipse2D.Float( x + w/4, y + h/4, w/2, h/2 );
        Shape line1 = new Line2D.Double(x, y, x + w / 2.0, y + h / 2.0);
        Shape line2 = new Line2D.Double(x, y + h / 2.0, x + w / 2.0, y);
        g2d.draw(line1);
        g2d.draw(line2);
    }

    private int w, h;
}
