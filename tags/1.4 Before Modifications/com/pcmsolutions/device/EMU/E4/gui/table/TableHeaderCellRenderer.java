package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:25:13
 * To change this template use Options | File Templates.
 */
public class TableHeaderCellRenderer extends GeneralTableCellRenderer {
    public static final TableHeaderCellRenderer INSTANCE = new TableHeaderCellRenderer();

    private TableHeaderCellRenderer() {
        setForeground(UIColors.getTableHeaderFG());
        setBackground(UIColors.getTableHeaderBG());
    }

    protected void setupLook(JTable table, Object value, boolean isSelected, int row, int column) {
    }
}
