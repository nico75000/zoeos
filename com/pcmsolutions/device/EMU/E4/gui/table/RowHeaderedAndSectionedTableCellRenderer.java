package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:25:13
 * To change this template use Options | File Templates.
 */
public class RowHeaderedAndSectionedTableCellRenderer extends GeneralTableCellRenderer {
    public static final RowHeaderedAndSectionedTableCellRenderer INSTANCE = new RowHeaderedAndSectionedTableCellRenderer();

    protected RowHeaderedAndSectionedTableCellRenderer() {
    }

    protected void setupLook(JTable table, Object value, boolean isSelected, int row, int column) {
       super.setupLook(table, value, isSelected, row, column);
        RowHeaderedAndSectionedTable t = (RowHeaderedAndSectionedTable) table;
        ColumnData[] cd = t.getColumnData();
        SectionData[] sd = t.getSectionData();
        setBackground(sd[cd[column].sectionIndex].sectionBG);
        setForeground(sd[cd[column].sectionIndex].sectionFG);
    }
}
