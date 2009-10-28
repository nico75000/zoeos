package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * User: paulmeehan
 * Date: 03-May-2004
 * Time: 18:39:11
 */
public class DecrementIcon extends ParameterZCommandIcon {
 public final static DecrementIcon INSTANCE = new DecrementIcon();

    DecrementIcon(){
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        GeneralPath p = new GeneralPath();
        float rx = x + inset;
        float ry = y + inset;
        p.moveTo(rx, ry);
        p.lineTo(rx + width, ry);
        p.lineTo(rx + width / 2, ry + height);
        p.closePath();
        //GradientPaint gp = new GradientPaint(rx, ry, c1, rx, ry + height, c2, false);
        g2d.setPaint(c2);
        g2d.fill(p);
        g2d.draw(p);
        g2d.setRenderingHints(hints);
    }
}
