package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 04-May-2004
 * Time: 10:27:01
 */
public class JMenuSeparatorComponent extends JComponent {
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Insets ins = getInsets();
        float w = getWidth() - ins.right - ins.left;
        float h = getHeight() - ins.bottom - ins.top;
        int x = ins.left;
        int y = ins.top;
        //g2d.setPaint(UIColors.applyAlpha(Color.white, UIColors.tableAlpha));
        g2d.setPaint(UIColors.getTableFirstSectionHeaderBG());
        //g.fillRoundRect(x+5, y, (int)w-10, (int)h, 3, 3);
        g.fillRect(x + 5, y, (int) w - 10, (int) h);
        g.drawRect(x + 5, y, (int) w - 10, (int) h);
    }

    public Dimension getPreferredSize() {
        return new Dimension(12, 40);
    }

    public Dimension getMaximumSize() {
        return new Dimension(12, Integer.MAX_VALUE);
    }
}
