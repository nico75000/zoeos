package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * User: paulmeehan
 * Date: 03-May-2004
 * Time: 18:39:11
 */
public class MinimizeIcon extends ParameterZCommandIcon {
 public final static MinimizeIcon INSTANCE = new MinimizeIcon();

    MinimizeIcon(){
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        GeneralPath p = new GeneralPath();
        float rx = x + inset;
        float ry = y + inset;
        p.moveTo(rx, ry);
        int barHeight =1;
        p.lineTo(rx + width, ry);
        p.lineTo(rx + width / 2, ry + height-(barHeight+1));
        p.closePath();
        g2d.setPaint(c2);
        g2d.fill(p);
        g2d.draw(p);
        Rectangle2D rect = new Rectangle2D.Double(rx,ry+height-barHeight,width, barHeight);
        g2d.fill(rect);
        g2d.draw(rect);
        g2d.setRenderingHints(hints);
    }
}
