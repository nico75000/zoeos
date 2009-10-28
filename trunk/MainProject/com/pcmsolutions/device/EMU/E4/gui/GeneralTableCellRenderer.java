package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.system.Zoeos;

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
    {
        setOpaque(false);
    }
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = ((Graphics2D) g);
        final int w = getWidth();
        final int h = getHeight();
        final Color bg = getBackground();
        if (isDropCell) {
            setBorder(bdrNormal);
            GradientPaint gp;
            gp = new GradientPaint(0, h * UIColors.tableDropTargetGradientFactor, UIColors.getDropColor(), 0, 0, Color.white, false);
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, w, h, 10, 10);
            //Color e_bg = getBackground();
            //setBackground(transparent);
            //super.paintComponent(g); // should only draw text
            //setBackground(e_bg);
        } else if (selected) {
            setBorder(bdrSel);
            Color sbg = UIColors.applyAlpha(getBackground(), UIColors.tableAlpha);
            GradientPaint gp;
            gp = new GradientPaint(0, h * UIColors.tableSelectionGradientFactor, bg, 0, 0, sbg, false);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            //Color e_bg = getBackground();
            //setBackground(transparent);
            //super.paintComponent(g); // should only draw text
            //setBackground(e_bg);
        } else {
            setBorder(bdrNormal);
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, w, h);
            //g.setColor(getForeground());
        }
        super.paintComponent(g);
    }
}
