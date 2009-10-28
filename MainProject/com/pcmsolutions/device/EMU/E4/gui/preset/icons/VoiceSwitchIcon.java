/*
 * PresetIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.preset.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 *
 * @author  pmeehan
 */
public class VoiceSwitchIcon implements Icon {
    public final static int CONTRACTED = 0;
    public final static int EXPANDED = 1;
    public final static int DISABLED = 2;

    private int w, h;
    private Color c1, c2;
    private int state;

    /** Creates a new instance of PresetIcon */
    public VoiceSwitchIcon(int w, int h, Color c1, Color c2, int state) {
        this.w = w;
        this.h = h;
        this.c1 = UIColors.applyAlpha(c1, UIColors.iconAlpha);
        this.c2 = UIColors.applyAlpha(c2, UIColors.iconAlpha);
        this.state = state;
    }

    public int getIconHeight() {
        return h;
    }

    public int getIconWidth() {
        return w;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g2d = ((Graphics2D) graphics);
        //Color halfRed = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 127);
        //g2d.setColor(halfRed);
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        int x1, x2, x3;
        int y1, y2, y3;
        switch (state) {
            case EXPANDED:
                x1 = x;
                y1 = y;
                x2 = x + w;
                y2 = y;
                x3 = x + w / 2;
                y3 = y + h;
                break;
            case CONTRACTED:
                x1 = x;
                y1 = y;
                x2 = x;
                y2 = y + h;
                x3 = x = w;
                y3 = y + h / 2;
                break;
            case DISABLED:
            default:
                return;
        }
        p.moveTo(x1, y1);
        p.lineTo(x2, y2);
        p.lineTo(x3, y3);
        p.lineTo(x1, y1);
        p.closePath();
        GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2, false);
        g2d.setPaint(gp);
        g2d.fill(p);
        g2d.setRenderingHints(hints);
    }
}
