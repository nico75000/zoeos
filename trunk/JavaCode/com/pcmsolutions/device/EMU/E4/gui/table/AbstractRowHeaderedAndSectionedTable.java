package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.gui.DisabledTransferHandler;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

abstract public class AbstractRowHeaderedAndSectionedTable extends DragAndDropTable implements RowHeaderedAndSectionedTable, ZDisposable, DropTargetListener {
    //protected DeviceContext device;
    protected DragAndDropTable rowHeader;
    protected ColumnAndSectionDataProvider columnAndSectionDataProvider;
    //protected static int rowHeight = 17;
    protected JMenuItem[] customRowHeaderMenuItems;

    public AbstractRowHeaderedAndSectionedTable(TableModel model, TransferHandler t, ColumnAndSectionDataProvider csdp/*, TableCellRenderer rowHeaderRenderer*/, String popupName) {
        super(t, popupName, null, null);
        init(model, csdp, popupName);
    }

    public AbstractRowHeaderedAndSectionedTable(TableModel model, ColumnAndSectionDataProvider csdp/*, TableCellRenderer rowHeaderRenderer*/, String popupName) {
        super(popupName, null, null);
        init(model, csdp, popupName);
    }

    private void init(TableModel model, ColumnAndSectionDataProvider csdp, String popupName) {
        if (columnAndSectionDataProvider == null)
            if (model instanceof ColumnAndSectionDataProvider)
                this.columnAndSectionDataProvider = (ColumnAndSectionDataProvider) model;
            else
                throw new IllegalArgumentException("Model is not a ColumnAndSectionDataProvider");
        else
            this.columnAndSectionDataProvider = csdp;
        setShowGrid(false);
        setColumnSelectionAllowed(true);
        setRowSelectionAllowed(true);
        //setRowHeight(rowHeight);
        this.setAutoCreateColumnsFromModel(false);
        setModel(model);
        //this.setBorder(null);
        makeColumns();

        this.getTableHeader().setResizingAllowed(false);
        this.getTableHeader().setReorderingAllowed(false);
        this.getTableHeader().setDefaultRenderer(TableHeaderCellRenderer.INSTANCE);

        setupRowHeader(popupName);
        //setMaximumSize(getPreferredSize());
    }

    protected void makeColumns() {
        Enumeration e = this.getColumnModel().getColumns();
        for (; e.hasMoreElements();)
            this.removeColumn((TableColumn) e.nextElement());

        TableColumn tc;
        ColumnData[] columnData = this.columnAndSectionDataProvider.getColumnData();
        RowHeaderedAndSectionedTableCellRenderer defRenderer = RowHeaderedAndSectionedTableCellRenderer.INSTANCE;
        TableCellRenderer currRenderer;
        TableCellEditor currEditor;
        for (int i = 0, n = columnData.length; i < n; i++) {
            currRenderer = columnData[i].renderer;
            if (currRenderer == null)
                currRenderer = defRenderer;
            currEditor = columnData[i].editor;
            tc = new TableColumn(i + 1, columnData[i].width, currRenderer, currEditor); // +1 to compensate for row header column
            tc.setResizable(false);
            addColumn(tc);
        }
    }

    public void setCustomRowHeaderMenuItems(JMenuItem[] jmi) {
        customRowHeaderMenuItems = jmi;

    }

    public ColumnAndSectionDataProvider getColumnAndSectionDataProvider() {
        return columnAndSectionDataProvider;
    }

    public SectionData[] getSectionData() {
        return columnAndSectionDataProvider.getSectionData();
    }

    public ColumnData[] getColumnData() {
        return columnAndSectionDataProvider.getColumnData();
    }

    public ColumnData getRowHeaderColumnData() {
        return columnAndSectionDataProvider.getRowHeaderColumnData();
    }

    public void addColumnAndSectionDataListener(ColumnAndSectionDataListener cdsl) {
        columnAndSectionDataProvider.addColumnAndSectionDataListener(cdsl);
    }

    public void removeColumnAndSectionDataListener(ColumnAndSectionDataListener cdsl) {
        columnAndSectionDataProvider.removeColumnAndSectionDataListener(cdsl);
    }

    //  protected void fireColumnAndSectionDataChanged(Object source) {
    //    for (int i = 0,j = csdListeners.size(); i < j; i++)
    //      ((ColumnAndSectionDataListener) csdListeners.get(i)).columnAndSectionDataChanged(source);
    //}
    protected DragAndDropTable generateRowHeaderTable() {
        DragAndDropTable t = new DragAndDropTable(popupName, null, null) {
            public void zDispose() {
            }

            protected Component[] getCustomMenuItems() {
                return customRowHeaderMenuItems;
            }      

            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (!e.isConsumed() && e.getClickCount() == 2)
                    this.clearSelection();
            }
        };
        t.setTransferHandler(DisabledTransferHandler.getInstance());
        return t;
    }

    private void setupRowHeader(String popupName) {
        TableColumn tc;
        // Set up the row header column and get it hooked up to everything
        rowHeader = generateRowHeaderTable();
        rowHeader.setShowGrid(false);
        rowHeader.setAutoCreateColumnsFromModel(false);
        rowHeader.setModel(this.getModel());
        rowHeader.setRowHeight(rowHeight);
        rowHeader.setDragEnabled(false);
        rowHeader.setFocusable(false);

        ColumnData ca = columnAndSectionDataProvider.getRowHeaderColumnData();
        TableCellRenderer tcr = ca.renderer;
        if (tcr == null)
            tcr = RowHeaderTableCellRenderer.INSTANCE;
        tc = new TableColumn(0, ca.width, tcr, null);
        tc.setResizable(false);
        rowHeader.addColumn(tc);

        // Make sure that selections between the main table and the header stay
        // in sync (by sharing the same model)            
        //setSelectionModel(rowHeader.getSelectionModel());

        // With out shutting off autoResizeMode, our tables won't scroll
        // correctly (horizontally, anyway)
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        rowHeader.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        rowHeader.setRowHeight(rowHeight);
        rowHeader.setDragEnabled(true);
    }

    public PopupTable getRowHeader() {
        return rowHeader;
    }

    public PopupTable getTable() {
        return this;
    }

    /*protected boolean checkPopup(MouseEvent e) {
        if (!this.contains(e.getPoint()))
            return rowHeader.checkPopup(e);
        else
            return super.checkPopup(e);
    } */

    public void zDispose() {
        super.zDispose();
        if (rowHeader instanceof ZDisposable)
            ((ZDisposable) rowHeader).zDispose();
    }
}
