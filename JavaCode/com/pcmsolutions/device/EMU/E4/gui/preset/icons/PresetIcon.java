/*
 * PresetIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.preset.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 *
 * @author  pmeehan
 */
public class PresetIcon implements Icon {
    private int w, h;
    private Color c1, c2, core;
    private boolean empty;

    /** Creates a new instance of PresetIcon */
    public PresetIcon(int w, int h, Color c1, Color c2) {
        this(w, h, c1, c2, false);
    }

    public PresetIcon(int w, int h, Color c1, Color c2, boolean empty) {
        this.w = w;
        this.h = h;
        this.c1 = UIColors.applyAlpha(c1, UIColors.iconAlpha);
        this.c2 = UIColors.applyAlpha(c2, UIColors.iconAlpha);
        this.empty = empty;
    }

    public PresetIcon(int w, int h, Color c1, Color c2, Color core) {
        this.w = w;
        this.h = h;
        this.c1 = UIColors.applyAlpha(c1, UIColors.iconAlpha);
        this.c2 = UIColors.applyAlpha(c2, UIColors.iconAlpha);
        this.core = core;
    }

    public int getIconHeight() {
        return h;
    }

    public int getIconWidth() {
        return w;
    }

    public Color getColor1() {
        return c1;
    }

    public Color getColor2() {
        return c2;
    }

    private static int fact(int val, double fact) {
        return (int) Math.round(val * fact);
    }

    public void paintIcon(java.awt.Component component, java.awt.Graphics graphics, int x, int y) {
        Graphics2D g2d = ((Graphics2D) graphics);
        Shape c;
        if (empty) {
            c = new Ellipse2D.Double(x + 1, y + 1, w - 2, h - 2);
            g2d.setColor(c1);
            g2d.draw(c);
            return;
        }
        c = new Ellipse2D.Double(x, y, w, h);
        GradientPaint gp = new GradientPaint(x, y, c1, x + w, y + h, c2, false);
        g2d.setPaint(gp);
        g2d.fill(c);
        g2d.draw(c);
        if (core != null) {
            c = new Ellipse2D.Double(x + fact(w, 0.25), y + fact(h, 0.25), w / 2, h / 2);
            //gp = new GradientPaint(x, y, c1, x + w / 2, y + h / 2, c2, false);
            gp = new GradientPaint(x, y, c1, x + w, y + h, core, false);
            g2d.setPaint(gp);
            g2d.fill(c);
            g2d.setColor(core);
            g2d.draw(c);
        }
    }
}
