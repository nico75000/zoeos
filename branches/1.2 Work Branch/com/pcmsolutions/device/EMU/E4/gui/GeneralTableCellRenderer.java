package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:25:13
 * To change this template use Options | File Templates.
 */
public abstract class GeneralTableCellRenderer extends AbstractTableCellRenderer implements TableCellRenderer {
    protected boolean drawFG = true;

    protected void paintComponent(Graphics g) {
        Graphics2D g2d = ((Graphics2D) g);
        int w = getWidth();
        int h = getHeight();
        Color e_bg = getBackground();
        Color bg = UIColors.applyAlpha(e_bg, UIColors.tableAlpha);
        Color sbg = e_bg;
        Color e_fg = getForeground();
        if (isDropCell) {
            GradientPaint gp;
            gp = new GradientPaint(0, h * UIColors.tableDropTargetGradientFactor, UIColors.getDropColor(), 0, 0, Color.white, false);
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, w, h, 10, 10);
            setBorder(bdrNormal);
            setOpaque(true);
        } else if (selected) {
            GradientPaint gp;
            gp = new GradientPaint(0, h * UIColors.tableSelectionGradientFactor, bg, 0, 0, sbg, false);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            setBorder(bdrSel);
            setOpaque(false);
        } else {
            setBorder(bdrNormal);
            setOpaque(true);
        }

        if (!drawFG) setForeground(bg);
        setBackground(bg);

        super.paintComponent(g);

        if (!drawFG)
            setForeground(e_fg);
        setBackground(e_bg);
    }
}
