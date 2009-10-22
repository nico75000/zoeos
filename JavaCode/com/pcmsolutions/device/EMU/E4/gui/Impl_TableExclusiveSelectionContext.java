package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.gui.table.PopupTable;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedTable;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * User: paulmeehan
 * Date: 02-May-2004
 * Time: 09:45:09
 */
public class Impl_TableExclusiveSelectionContext implements TableExclusiveSelectionContext, ListSelectionListener, ZDisposable {
    private final ArrayList<PopupTable> tables = new ArrayList<PopupTable>();
    private SelectionAction selectionAction;

    public interface SelectionAction {
        void newSelection(PopupTable t);

        void clearedSelection(PopupTable t);
    }

    public Impl_TableExclusiveSelectionContext() {
        this.selectionAction = null;
    }

    public Impl_TableExclusiveSelectionContext(SelectionAction selectionAction) {
        this.selectionAction = selectionAction;
    }

    public SelectionAction getSelectionAction() {
        return selectionAction;
    }

    public void setSelectionAction(SelectionAction selectionAction) {
        this.selectionAction = selectionAction;
    }

    public void addTableToContext(PopupTable t) {
        if (t instanceof RowHeaderedTable) {
            PopupTable rh = ((RowHeaderedTable) t).getRowHeader();
            tables.add(rh);
            rh.getSelectionModel().addListSelectionListener(this);
            rh.getColumnModel().getSelectionModel().addListSelectionListener(this);
        }
        tables.add(t);
        t.getSelectionModel().addListSelectionListener(this);
        t.getColumnModel().getSelectionModel().addListSelectionListener(this);
    }

    public ArrayList getTables() {
        return (ArrayList) tables.clone();
    }

    private boolean isHandling = false;

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && !isHandling) {
            PopupTable t;
            PopupTable selectionTable = null;
            for (Iterator<PopupTable> i = tables.iterator(); i.hasNext();) {
                t = i.next();
                if (e.getSource().equals(t.getSelectionModel()) || e.getSource().equals(t.getColumnModel().getSelectionModel())) {
                    if (t.getSelectedRowCount() > 0 /*&& t.getSelectedColumnCount() > 0*/)
                        selectionTable = t;
                    break;
                }
            }
            if (selectionTable != null) {
                isHandling = true;
                try {
                    for (Iterator<PopupTable> i = tables.iterator(); i.hasNext();) {
                        t = i.next();
                        if (selectionTable != t && t.getSelectionModel() != selectionTable.getSelectionModel())
                            if (t.getSelectedRowCount() > 0 /* t.getSelectedColumnCount() > 0*/)
                                t.clearSelection();
                    }
                } finally {
                    isHandling = false;
                }
            }
            handleNewSelection(selectionTable);
        }
    }

    void handleNewSelection(final PopupTable selectionTable) {
        if (selectionTable instanceof DragAndDropTable && ((DragAndDropTable) selectionTable).isDropFeedbackActive())
            return;
        if (selectionAction != null)
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    selectionAction.newSelection(selectionTable);
                }
            });
    }

    public void zDispose() {
        JTable t;
        for (Iterator<PopupTable> i = tables.iterator(); i.hasNext();) {
            t = i.next();
            t.getSelectionModel().removeListSelectionListener(this);
            t.getColumnModel().getSelectionModel().removeListSelectionListener(this);
        }
        tables.clear();
    }
}
