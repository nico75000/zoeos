package com.pcmsolutions.device.EMU.E4.gui.table;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Jun-2003
 * Time: 01:23:56
 * To change this template use Options | File Templates.
 */
abstract public class EditingTable extends PopupTable {
    int selCol;
    int selRow;

   // protected final Object mouseWheelMonitor = new Object();

    public EditingTable(TableModel model, String popupName, Color popupBG, Color popupFG) {
        super(model, popupName, popupBG, popupFG);
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setBorder(null);
    }

    public EditingTable(String popupName, Color popupBG, Color popupFG) {
        super(popupName, popupBG, popupFG);
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setBorder(null);
    }

    public boolean editCellAt(int row, int column, EventObject e) {
        boolean rv = super.editCellAt(row, column, e);
        if (rv == true) {
            Component c = getEditorComponent();
            if (c instanceof JComboBox) {
                // D&D upsets JComboBox double click behaviour
                //if (getDragEnabled() && getTransferHandler() != null) {
                //if (!((JComboBox) c).isPopupVisible())
                ((JComboBox) c).updateUI();
                ((JComboBox) c).showPopup();// setPopupVisible(true);
                ((JComboBox) c).requestFocusInWindow();
                //((JComboBox) c).showPopup();
                //((JComboBox) c).showPopup();// setPopupVisible(true);
                //((JComboBox) c).showPopup();

                //}
            } else if (c instanceof JSpinner) {
                ((JSpinner) c).updateUI();
                ((JSpinner) c).requestFocusInWindow();
            }
        }
        return rv;
    }

    public boolean editCellAt(int row, int column) {
        boolean rv = super.editCellAt(row, column);
        if (rv == true) {
            Component c = getEditorComponent();
            if (c instanceof JComboBox) {
                // D&D upsets JComboBox double click behaviour
                //if (getDragEnabled() && getTransferHandler() != null) {
                //if (!((JComboBox) c).isPopupVisible())
                ((JComboBox) c).updateUI();
                ((JComboBox) c).showPopup();// setPopupVisible(true);
                //((JComboBox) c).showPopup();
                //((JComboBox) c).showPopup();// setPopupVisible(true);
                //((JComboBox) c).showPopup();

                //}
            } else if (c instanceof JSpinner) {
                ((JSpinner) c).updateUI();
            }
        }
        return rv;
    }

    /*   public void mouseDragged(MouseEvent mouseEvent) {
           //super.mouseDragged(mouseEvent);
           int oldSelRow = selRow;
           int oldSelCol = selCol;
           selRow = this.rowAtPoint(mouseEvent.getPoint());
           selCol = this.columnAtPoint(mouseEvent.getPoint());

           if ( oldSelRow != selRow && oldSelCol != selCol)
           if ( this.isCellEditable(selRow, selCol) && !this.isEditing()){
               this.editCellAt(selRow, selCol);
           }

       }*/
}
