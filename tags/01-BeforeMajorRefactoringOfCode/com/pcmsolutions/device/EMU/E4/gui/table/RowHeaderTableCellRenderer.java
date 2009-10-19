package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import javax.swing.border.BevelBorder;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:25:13
 * To change this template use Options | File Templates.
 */
public class RowHeaderTableCellRenderer extends GeneralTableCellRenderer {
    public static final RowHeaderTableCellRenderer INSTANCE = new RowHeaderTableCellRenderer();

    private RowHeaderTableCellRenderer() {
    }

    protected void setupLook(JTable table, Object value, boolean isSelected, int row, int column) {
        setForeground(UIColors.getTableRowHeaderFG());
        setBackground(UIColors.getTableRowHeaderBG());
        if (selected)
            bdrSel = new BevelBorder(BevelBorder.LOWERED, UIColors.getTableRowHeaderBG(), UIColors.getTableRowHeaderFG());
        else
            bdrNormal = null;
    }
}
