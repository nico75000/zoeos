/*
 * PresetIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.multimode;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

/**
 *
 * @author  pmeehan
 */
public class MultimodeIcon implements Icon {
    private int w, h;
    private static Color c = UIColors.applyAlpha(Color.blue, UIColors.iconAlpha);

    /** Creates a new instance of PresetIcon */
    public MultimodeIcon(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public int getIconHeight() {
        return h;
    }

    public int getIconWidth() {
        return w;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g2d = ((Graphics2D) graphics);

        GradientPaint gp = new GradientPaint(x, y, Color.white, x + w, y, c, false);
        g2d.setPaint(gp);
        g2d.fillRect(x,y,w,h);

        Shape line;
        g2d.setColor(UIColors.applyAlpha(c, 75));
        for (int i = 2, n = h; i < n - 2; i += 2) {
            line = new Line2D.Double(x, y + i, x + w, y + i);
            g2d.draw(line);
        }
       /* line = new Line2D.Double(x, y + 2, x, y + h - 4);
        g2d.draw(line);
        line = new Line2D.Double(x + w, y + 2, x + w, y + h - 4);
        g2d.draw(line);
        */
    }
}
