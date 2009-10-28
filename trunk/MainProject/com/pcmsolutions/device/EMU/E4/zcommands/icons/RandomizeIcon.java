package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * User: paulmeehan
 * Date: 03-May-2004
 * Time: 18:39:11
 */
public class RandomizeIcon extends ParameterZCommandIcon {
    public final static RandomizeIcon INSTANCE = new RandomizeIcon();

    private static final double[] randoms = new double[]{0.15, 0.27, 0.32, 0.1, 0.45, 0.6, 0.87, 0.38, 0.97, 0.05, 0.77};

    RandomizeIcon() {
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints hints = g2d.getRenderingHints();
        g2d.setRenderingHints(UIColors.iconRH);
        final float rx = x + inset;
        final float ry = y + inset;
        g2d.setPaint(c2);
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                if (h % 3 != 0 && w % 5 != 0 && w%3!=0) {
                    Rectangle2D rect = new Rectangle2D.Double(rx + (h%2==0?w:w-1), ry + (w%2!=0?h:h+1), 1/*(w%2==0?1:2)*/, 1/*(h%2!=0?1:2)*/);
                    g2d.fill(rect);
                    //g2d.draw(rect);
                }
            }
        }
        g2d.setRenderingHints(hints);
    }
}
