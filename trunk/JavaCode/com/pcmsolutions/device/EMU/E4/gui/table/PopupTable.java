package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.PopupCategoryLabel;
import com.pcmsolutions.gui.ZCommandInvocationHelper;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-May-2003
 * Time: 08:51:31
 * To change this template use Options | File Templates.
 */
abstract public class PopupTable extends JTable implements MouseListener, ZDisposable, FocusListener {
    protected Color popupBG;
    protected Color popupFG;
    protected String popupName;
    protected boolean hidingSelectionOnFocusLost = false;

    protected int[] lastHiddenRows;
    protected int[] lastHiddenCols;

    public PopupTable(TableModel model, String popupName, Color popupBG, Color popupFG) {
        this(popupName, popupBG, popupFG);
        setModel(model);
        // this.setDoubleBuffered(true);
    }

    public PopupTable(String popupName, Color popupBG, Color popupFG) {
        this.popupFG = popupFG;
        this.popupBG = popupBG;
        this.popupName = popupName;
        this.addMouseListener(this);
        this.addFocusListener(this);
        //this.setDoubleBuffered(true);
        setShowGrid(true);
    }

    public Color getBackground() {
        return UIColors.getTableBG();
    }

    public void mouseDragged(MouseEvent mouseEvent) {
    }

    public void mouseClicked(MouseEvent e) {
        checkPopup(e);
    }

    public int getRowHeight() {
        return UIColors.getTableRowHeight();
    }

    public boolean isHidingSelectionOnFocusLost() {
        return hidingSelectionOnFocusLost;
    }

    public void setHidingSelectionOnFocusLost(boolean hidingSelectionOnFocusLost) {
        this.hidingSelectionOnFocusLost = hidingSelectionOnFocusLost;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        checkPopup(e);
    }

    public Object[] getSelObjects() {
        int[] selRows = this.getSelectedRows();
        int[] selCols = this.getSelectedColumns();

        if (selRows != null && selCols != null) {
            int selRowCount = selRows.length;
            int selColCount = selCols.length;
            ArrayList selObjects = new ArrayList();
            for (int n = 0; n < selRowCount; n++)
                for (int i = 0; i < selColCount; i++)
                    selObjects.add(this.getValueAt(selRows[n], selCols[i]));
            return selObjects.toArray();
        }
        return new Object[0];
    }

    protected boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Object[] sels = getSelObjects();
            if (sels != null) {
                JPopupMenu p = ZCommandInvocationHelper.getPopup(sels, popupFG, popupBG, popupName);
                JMenuItem[] cmi = getCustomMenuItems();
                if (cmi != null) {
                    p.addSeparator();
                    for (int i = 0, n = cmi.length; i < n; i++)
                        if (cmi[i] != null)
                            p.add(cmi[i]);
                        else
                            p.addSeparator();
                }
                //p.addSeparator();
                // p.addDesktopElement(new PopupCategoryMenuItem("Table"));
                p.add(new PopupCategoryLabel("Table"));

                p.add(new AbstractAction("Select All") {
                    public void actionPerformed(ActionEvent e) {
                        PopupTable.this.selectAll();
                    }
                });
                p.add(new AbstractAction("Select Row") {
                    public void actionPerformed(ActionEvent e) {
                        PopupTable.this.addColumnSelectionInterval(0, PopupTable.this.getColumnCount() - 1);
                        //PopupTable.this.getSelectionModel().addSelectionInterval(0, PopupTable.this.getRowCount() - 1);
                    }
                });
                p.add(new AbstractAction("Select Column") {
                    public void actionPerformed(ActionEvent e) {
                        PopupTable.this.addRowSelectionInterval(0, PopupTable.this.getRowCount() - 1);
                        //PopupTable.this.getSelectionModel().addSelectionInterval(0, PopupTable.this.getRowCount() - 1);
                    }
                });
                p.add(new AbstractAction("Clear Selection") {
                    public void actionPerformed(ActionEvent e) {
                        PopupTable.this.clearSelection();
                        //PopupTable.this.getSelectionModel().addSelectionInterval(0, PopupTable.this.getRowCount() - 1);
                    }
                });
                ZCommandInvocationHelper.showPopup(p, this, e);
                return true;
            }
        }
        return false;
    }

    protected JMenuItem[] getCustomMenuItems() {
        return null;
    }

    private void restoreSelection() {
        if (lastHiddenRows != null) {
            for (int i = 0; i < lastHiddenRows.length; i++)
                this.addRowSelectionInterval(lastHiddenRows[i], lastHiddenRows[i]);
        }
        if (lastHiddenCols != null) {
            for (int i = 0; i < lastHiddenCols.length; i++)
                this.addColumnSelectionInterval(lastHiddenCols[i], lastHiddenCols[i]);
        }
    }

    public void focusGained(FocusEvent e) {
        /* if (hidingSelectionOnFocusLost && !e.isTemporary()) {
             clearSelection();
             restoreSelection();
         }*/
    }

    public void focusLost(FocusEvent e) {
        if (hidingSelectionOnFocusLost && !e.isTemporary()) {
            lastHiddenRows = this.getSelectedRows();
            lastHiddenCols = this.getSelectedColumns();
            clearSelection();
        } else {
            lastHiddenRows = null;
            lastHiddenCols = null;
        }
    }

    public void zDispose() {
        this.removeMouseListener(this);
        this.removeFocusListener(this);
        setTransferHandler(null);
        if (getModel() instanceof ZDisposable)
            ((ZDisposable) getModel()).zDispose();
    }
}