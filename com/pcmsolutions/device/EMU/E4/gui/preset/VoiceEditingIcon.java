/*
 * PresetIcon.java
 *
 * Created on February 5, 2003, 4:14 AM
 */

package com.pcmsolutions.device.EMU.E4.gui.preset;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.PresetIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 *
 * @author  pmeehan
 */
public class VoiceEditingIcon implements Icon {
    private int w, h;
    private Color c1, c2;
    private Icon pi = null;

    public VoiceEditingIcon(PresetIcon p) {
        this(p.getIconWidth(), p.getIconHeight(), p.getColor1(), p.getColor2());
        pi = p;
    }

    public VoiceEditingIcon(int w, int h, Color c1, Color c2) {
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

    public Color getColor1() {
        return c1;
    }

    public Color getColor2() {
        return c2;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g2d = ((Graphics2D) graphics);
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        Shape c;
        if (pi != null) {
            pi.paintIcon(component, graphics, x, y);
        } else {
            c = new Ellipse2D.Double(x, y, w, h);
            GradientPaint gp = new GradientPaint(x, y, c1, x + w, y + h, c2, false);
            g2d.setPaint(gp);
            g2d.fill(c);
        }
        //c = new RoundRectangle2D.Double(x + Math.round(w / 3.0), y, Math.round(w / 3.0), h, 2, 2);
        g2d.setColor(UIColors.applyAlpha(c1, 175));
        Ellipse2D.Double e = new Ellipse2D.Double();
        e.setFrame(x + w / 2.0 , y + h / 2.0 , w / 2.0, h / 2.0);
        g2d.fill(e);
        e.setFrame(x , y , w / 2.0, h / 2.0);
        g2d.fill(e);
        g2d.setRenderingHints(hints);
    }
}
