package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * User: paulmeehan
 * Date: 03-May-2004
 * Time: 18:39:11
 */
public class RepeatIcon extends ParameterZCommandIcon {
    public final static RepeatIcon INSTANCE = new RepeatIcon();

    RepeatIcon(){
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        GeneralPath p = new GeneralPath();
        final float rx = x + inset;
        final float ry = y + inset;
        g2d.setPaint(c2);
        p.moveTo(rx, ry);
        p.lineTo(rx + width/3.0F, ry + height/2.0F);
        p.lineTo(rx, ry + height);
        p.closePath();
        g2d.draw(p);
        g2d.fill(p);

        p.reset();
        p.moveTo(rx + width/3.0F, ry);
        p.lineTo(rx + width/3.0F + width/3.0F, ry + height/2.0F);
        p.lineTo(rx + width/3.0F, ry + height);
        p.closePath();
        g2d.draw(p);

        p.reset();
        p.moveTo(rx + (width*2.0F)/3, ry);
        p.lineTo(rx + width, ry + height/2.0F);
        p.lineTo(rx + (width*2.0F)/3, ry + height);
        p.closePath();
        g2d.draw(p);
        g2d.setRenderingHints(hints);
    }
}
