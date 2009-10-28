package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnAndSectionDataProvider;
import com.pcmsolutions.device.EMU.database.Context;
import com.pcmsolutions.device.EMU.database.ContextElement;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * User: paulmeehan
 * Date: 14-Dec-2004
 * Time: 02:18:32
 */
abstract public class AbstractContextTable <C extends Context, CE extends ContextElement> extends AbstractRowHeaderedAndSectionedTable {
    private C context;

    public AbstractContextTable(C context, TableModel model, TransferHandler t, ColumnAndSectionDataProvider csdp, String popupName) {
        super(model, t, csdp, popupName);
        this.context = context;
    }

    public AbstractContextTable(C context, TableModel model, ColumnAndSectionDataProvider csdp, String popupName) {
        super(model, csdp, popupName);
        this.context = context;
    }

    public boolean editCellAt(int row, int column, EventObject e) {
        if (e instanceof MouseEvent && ((MouseEvent) e).getClickCount() >= 2) {
            //if (getValueAt(row, column) instanceof CE) {
            final CE elem = (CE) getValueAt(row, column);
            elem.performDefaultAction();
            //}
        }
        return false;
    }

    public C getContext() {
        return context;
    }

    // inclusive (will ignore indexes that are not available)
    public void addIndexToSelection(Integer index) {
        AbstractContextTableModel tm = (AbstractContextTableModel) getModel();
        int row = tm.getRowForIndex(index);
        if (row != -1) {
            addRowSelectionInterval(row, row);
            addColumnSelectionInterval(0, 0);
            //this.getSelectionModel().addSelectionInterval(row, row);
        }
    }

    public void addIndexesToSelection(Integer[] indexes) {
        getSelectionModel().setValueIsAdjusting(true);
        try {
            for (int i = 0; i < indexes.length; i++)
                addIndexToSelection(indexes[i]);
        } finally {
            getSelectionModel().setValueIsAdjusting(false);
        }
    }

    public void invertSelection() {
        getSelectionModel().setValueIsAdjusting(true);
        try {
            int[] selRows = this.getSelectedRows();
            this.selectAll();
            for (int i = 0; i < selRows.length; i++)
                this.removeRowSelectionInterval(selRows[i], selRows[i]);
        } finally {
            getSelectionModel().setValueIsAdjusting(false);
        }
    }

    public int getRowForIndex(Integer index) {
        AbstractContextTableModel pctm = (AbstractContextTableModel) getModel();
        return pctm.getRowForIndex(index);
    }

    // will do nothing if index does not available
    public void scrollToIndex(Integer index) {
        AbstractContextTableModel tm = (AbstractContextTableModel) getModel();
        int row = tm.getRowForIndex(index);
        Rectangle cellRect = this.getCellRect(row, 0, true);
        this.scrollRectToVisible(cellRect);
    }

    public List<CE> getFilteredElements() {
        ArrayList<CE> filteredPresets = new ArrayList<CE>();
        for (int i = 0, j = getRowCount(); i < j; i++) {
            Object o = getValueAt(i, 0);
            //if (o instanceof ReadablePreset)
            filteredPresets.add((CE) o);
        }
        return filteredPresets;
    }

    public boolean showingAllIndexes(Integer[] indexes) {
        AbstractContextTableModel tm = (AbstractContextTableModel) getModel();
        for (int i = 0; i < indexes.length; i++)
            if (tm.getRowForIndex(indexes[i]) == -1)
                return false;
        return true;
    }

    public void zDispose() {
        context = null;
    }

}
