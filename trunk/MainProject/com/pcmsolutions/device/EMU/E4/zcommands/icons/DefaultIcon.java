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
public class DefaultIcon extends ParameterZCommandIcon {
     public final static DefaultIcon INSTANCE = new DefaultIcon();

    DefaultIcon(){
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        GeneralPath p = new GeneralPath();
        float rx = x + inset;
        float ry = y + inset;

        g2d.setPaint(c2);

        // bar
        float topOfBar =ry + height/2-1;
        int barHeight = 1;
        Rectangle2D rect = new Rectangle2D.Double(rx,topOfBar,width, barHeight);
        g2d.fill(rect);
        g2d.draw(rect);

        // up
        p.reset();
        p.moveTo(rx, ry + height);
        p.lineTo(rx + width / 2, topOfBar+barHeight+1);
        p.lineTo(rx + width, ry + height);
        p.closePath();
        g2d.fill(p);
        g2d.draw(p);

        // down
        p.reset();
        p.moveTo(rx, ry);
        p.lineTo(rx + width / 2, topOfBar-1);
        p.lineTo(rx + width, ry);
        p.closePath();
        //g2d.setPaint(gp);
        g2d.fill(p);
        g2d.draw(p);
        g2d.setRenderingHints(hints);
    }
}
