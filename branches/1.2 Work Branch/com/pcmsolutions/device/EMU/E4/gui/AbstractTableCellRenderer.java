package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.gui.table.DropCellCheckerTable;
import com.pcmsolutions.gui.IconAndTipCarrier;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:25:13
 * To change this template use Options | File Templates.
 */
public abstract class AbstractTableCellRenderer extends JLabel implements TableCellRenderer {
    protected boolean selected;
    protected boolean isDropCell;
    protected Border bdrSel;
    protected Border bdrNormal;

    public AbstractTableCellRenderer() {
        setHorizontalAlignment(JLabel.LEFT);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        setupLook(table, value, isSelected, row, column);
        buildLabel(value);

        isDropCell = false;

        if (table instanceof DropCellCheckerTable)
            if (((DropCellCheckerTable) table).isCellDropTarget(row, column, value)) {
                isDropCell = true;
                return this;
            }

        if (isSelected && !(table instanceof DragAndDropTable && ((DragAndDropTable) table).isDropFeedbackActive()))
            selected = true;
        else
            selected = false;

        return this;
    }

    protected void buildLabel(Object value) {
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
        if (value instanceof IconAndTipCarrier)
            try {
                String tip = ((IconAndTipCarrier) value).getToolTipText();
                Icon icon = ((IconAndTipCarrier) value).getIcon();
                this.setToolTipText(tip);
                setIcon(icon);
                return;
            } catch (Exception e) {
            }

        setIcon(null);
        if (value != null)
            setToolTipText(value.toString());
    }

    protected void setupLook(JTable table, Object value, boolean isSelected, int row, int column) {
        if (isSelected)
            bdrSel = new BevelBorder(BevelBorder.LOWERED, getBackground(), getForeground());
        else
            bdrNormal = null;
    }
}
