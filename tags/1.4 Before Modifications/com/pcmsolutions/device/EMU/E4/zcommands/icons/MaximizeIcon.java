package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * User: paulmeehan
 * Date: 03-May-2004
 * Time: 18:39:11
 */
public class MaximizeIcon extends ParameterZCommandIcon {
 public final static MaximizeIcon INSTANCE = new MaximizeIcon();

    MaximizeIcon(){
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        GeneralPath p = new GeneralPath();
        final float rx = x + inset;
        final float ry = y + inset;
        int barHeight = 1;
        p.moveTo(rx, ry + height);
        p.lineTo(rx + width / 2, ry+(barHeight+1));
        p.lineTo(rx + width, ry + height);
        p.closePath();
        g2d.setPaint(c2);
        g2d.fill(p);
        g2d.draw(p);
        Rectangle2D rect = new Rectangle2D.Double(rx,ry,width, barHeight);
        g2d.fill(rect);
        g2d.draw(rect);
        g2d.setRenderingHints(hints);
    }
}
