package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.IconAndTipCarrier;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:25:13
 * To change this template use Options | File Templates.
 */
public class GeneralListCellRenderer extends JLabel implements ListCellRenderer {
    private boolean selected;
    private BevelBorder bdrSel;

    private Color sbg;
    private Color bg;
    private Color fg;
    private Color sfg;

    public GeneralListCellRenderer(Color sbg, Color fg) {
        setHorizontalAlignment(JLabel.LEFT);
        this.sbg = sbg;
        this.bg = UIColors.applyAlpha(this.sbg, UIColors.listAlpha);
        this.fg = fg;
        this.sfg = fg;
        bdrSel = new BevelBorder(BevelBorder.RAISED, this.sbg, this.sfg);
    }

    public GeneralListCellRenderer(Color fg, Color bg, Color sfg, Color sbg) {
        setHorizontalAlignment(JLabel.LEFT);
        this.sbg = sbg;
        this.bg = bg;
        this.fg = fg;
        this.sfg = sfg;
        bdrSel = new BevelBorder(BevelBorder.RAISED, this.sbg, this.sfg);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2d = ((Graphics2D) g);
        int w = getWidth();
        int h = getHeight();
        Icon i = getIcon();
        int iw = 0;
        if (i != null)
            iw = i.getIconWidth();
        if (selected) {
            GradientPaint gp;
            gp = new GradientPaint(0, h * 2, bg, 0, 0, sbg, false);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            setBorder(bdrSel);
        } else
            setBorder(null);

        super.paintComponent(g);
    }

    private void buildLabel(Object value) {
        String valueStr;
        if (value != null) {
            valueStr = value.toString();
            if (valueStr != null)
                valueStr.trim();
            else
                valueStr = "";
        } else
            valueStr = "";

        setText(valueStr);
        if (value instanceof IconAndTipCarrier) {
            String tip = ((IconAndTipCarrier) value).getToolTipText();
            Icon icon = ((IconAndTipCarrier) value).getIcon();
            this.setToolTipText(tip);
            setIcon(icon);
        } else {
            setIcon(null);
            setToolTipText("");
        }
    }

    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        buildLabel(value);

        if (isSelected) {
            setForeground(sfg);
            setBackground(sbg);
            selected = true;
            setOpaque(false);
        } else {
            setForeground(fg);
            setBackground(bg);
            selected = false;
            setOpaque(true);
        }

        return this;
    }
}
