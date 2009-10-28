package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 25-Jan-2004
 * Time: 09:56:32
 */
public class PopupCategoryLabel extends JPanel {
    public PopupCategoryLabel(String text) {
        super(new BorderLayout());
        JLabel j = new JLabel(text);
        j.setHorizontalAlignment(JLabel.CENTER);
        add(j, BorderLayout.CENTER);
        //this.setOpaque(false);
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        int alpha = 200;
        Color c1 =UIColors.applyAlpha(Color.white, alpha);
        Color c2 =UIColors.applyAlpha(getBackground(), alpha);
        GradientPaint gp = new GradientPaint(0, 0, c1, w/2, 0, c2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }
}
