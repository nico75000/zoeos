package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * User: paulmeehan
 * Date: 05-May-2004
 * Time: 14:53:07
 */
public abstract class PackageIcon implements Icon {
    private boolean open;

    protected static final int width = 18;
    protected static final int height = 18;
    protected static final int inset = 3;

    protected final Color c1;
    protected final Color c2;
    protected final Color c3;

    protected static final float part = 6;
    protected static final float interval = height / part;

    protected PackageIcon(boolean open, Color c1, Color c2, Color c3) {
        this.open = open;
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        GeneralPath p = new GeneralPath();
        int rx = x + inset;
        int ry = y + inset;

        g2d.setPaint(c2);
        p.reset();
        p.moveTo(rx, ry + interval);
        p.lineTo(rx + width / 2, ry + interval * 2);
        p.lineTo(rx + width / 2, ry + height);
        p.lineTo(rx, ry + height - interval);
        p.closePath();
        g2d.fill(p);
        //g2d.draw(p);

        g2d.setPaint(c3);
        p.reset();
        p.moveTo(rx + width / 2, ry + interval * 2);
        p.lineTo(rx + width, ry + interval);
        p.lineTo(rx + width, ry + height - interval);
        p.lineTo(rx + width / 2, ry + height);
        p.closePath();
        g2d.fill(p);
        //g2d.draw(p);

        g2d.setPaint(c1);
        p.reset();
        p.moveTo(rx, ry + interval);
        p.lineTo(rx + width / 2, ry);
        p.lineTo(rx + width, ry + interval);
        p.lineTo(rx + width / 2, ry + interval * 2);
        p.closePath();
        if (!open) {
            g2d.fill(p);
            g2d.draw(p);
            g2d.setPaint(Color.yellow);
            g2d.drawLine(rx, (int) (ry + interval), rx + width / 2, (int) (ry + interval * 2));
            g2d.drawLine(rx + width / 2, (int) (ry + interval * 2), rx + width, (int) (ry + interval));
        } else {
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(p);
            /*p.reset();
            p.moveTo(rx, ry + interval);
            p.lineTo(rx+width, ry + interval );
            p.lineTo(rx + width / 2, ry);
            p.closePath();
            g2d.fill(p);
            */
            g2d.setPaint(Color.yellow);
            p.reset();
            p.moveTo(rx, ry + interval);
            p.lineTo(rx + width, ry + interval);
            p.lineTo(rx + width / 2, ry + interval * 2);
            p.closePath();
            g2d.fill(p);
        }

    }

    public int getIconWidth() {
        return width + inset * 2;
    }

    public int getIconHeight() {
        return height + inset * 2;
    }
}
