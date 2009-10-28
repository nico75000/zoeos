package com.pcmsolutions.device.EMU.E4.gui.preset.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * User: paulmeehan
 * Date: 21-May-2004
 * Time: 15:25:20
 */
public class PresetUserIcon implements Icon {
    final PresetIcon p;
    static final Color c = UIColors.applyAlpha(Color.gray, 125);

    public PresetUserIcon(PresetIcon p) {
        if (p == null)
            throw new IllegalArgumentException("null icon");
        this.p = p;
    }

    public void paintIcon(java.awt.Component component, java.awt.Graphics graphics, int x, int y) {
        p.paintIcon(component, graphics, x, y);
        int w = p.getIconWidth();
        int h = p.getIconHeight();

        Graphics2D g2d = ((Graphics2D) graphics);
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        Shape s;
        g2d.setPaint(c);
        s = new Ellipse2D.Double(x + w / 4.0, y + h / 4.0 + 1, 1, 1);
        g2d.fill(s);
        g2d.draw(s);
        s = new Ellipse2D.Double(x + (w * 3 / 4.0), y + h / 4.0 + 1, 1, 1);
        g2d.fill(s);
        g2d.draw(s);

        s = new Ellipse2D.Double(x + w / 2.0, y + h / 2.0 + 2, 1, 1);
        g2d.fill(s);
        g2d.draw(s);
        g2d.setRenderingHints(hints);
    }

    public int getIconWidth() {
        return p.getIconWidth();
    }

    public int getIconHeight() {
        return p.getIconHeight();
    }
}
