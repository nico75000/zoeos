package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * User: paulmeehan
 * Date: 03-May-2004
 * Time: 18:39:11
 */
public class RotateIcon extends ParameterZCommandIcon {
    private boolean left;

    RotateIcon(boolean left) {
        this.left = left;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        final float rx = x + inset;
        final float ry = y + inset;        
        Arc2D.Float arc;
        arc = new Arc2D.Float(rx, ry, width, height * 2, 0, 180, Arc2D.OPEN);

        g2d.setPaint(c2);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(arc);
        Point2D point;
        if (left)
            point = arc.getEndPoint();
        else
            point = arc.getStartPoint();

        int aw = 3;
        g2d.drawLine((int) point.getX() - aw, (int) point.getY() - aw, (int) point.getX(), (int) point.getY());
        g2d.drawLine((int) point.getX(), (int) point.getY(), (int) point.getX() + aw, (int) point.getY() - aw);
        g2d.setRenderingHints(hints);
    }
}
