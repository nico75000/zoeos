package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * User: paulmeehan
 * Date: 03-May-2004
 * Time: 18:39:11
 */
public class JumpUpIcon extends ParameterZCommandIcon {
    public final static JumpUpIcon INSTANCE = new JumpUpIcon();

    JumpUpIcon(){
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        GeneralPath p = new GeneralPath();
        final float rx = x + inset;
        final float ry = y + inset;
        g2d.setPaint(c2);

        // large
        p.moveTo(rx, ry + height);
        p.lineTo(rx + width / 2, ry);
        p.lineTo(rx + width, ry + height);
        p.closePath();
        //GradientPaint gp = new GradientPaint(rx, ry, c2, rx, ry + height, c1, false);
        g2d.fill(p);
        g2d.draw(p);

        // small
        p.reset();
        p.moveTo(rx, ry + height/3);
        p.lineTo(rx + width / 2, ry);
        p.lineTo(rx + width, ry + height/3);
        p.closePath();
        //GradientPaint gp = new GradientPaint(rx, ry, c2, rx, ry + height, c1, false);
        g2d.fill(p);
        g2d.draw(p);
        g2d.setRenderingHints(hints);
    }
}
