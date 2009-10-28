package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.PopupCategoryLabel;
import com.pcmsolutions.gui.ZCommandFactory;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import javax.swing.table.JTableHeader;
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
    private boolean hidingSelectionOnFocusLost = false;

    protected int[] lastHiddenRows;
    protected int[] lastHiddenCols;

    public PopupTable(TableModel model, String popupName, Color popupBG, Color popupFG) {
        this(popupName, popupBG, popupFG);
        setModel(model);
    }

    protected JTableHeader createDefaultTableHeader() {
        //return super.createDefaultTableHeader();    //To change body of overridden methods use File | Settings | File Templates.
        return new JTableHeader(columnModel) {
            {
                enableEvents(AWTEvent.MOUSE_EVENT_MASK);
            }

            protected void processMouseEvent(MouseEvent e) {
                super.processMouseEvent(e);    //To change body of overridden methods use File | Settings | File Templates.
                if (e.getClickCount() == 2) {
                    final int col = this.columnAtPoint(e.getPoint());
                    if (col != -1){
                        final JTable table = this.getTable();
                        table.requestFocusInWindow();
                        SwingUtilities.invokeLater(new Runnable(){
                            public void run() {
                                table.setColumnSelectionInterval(col, col);
                                table.setRowSelectionInterval(0, table.getRowCount()-1);
                            }
                        });
                    }
                }
            }
        };
    }

    public JTableHeader getTableHeader() {
        return super.getTableHeader();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public PopupTable(String popupName, Color popupBG, Color popupFG) {
        this.popupFG = popupFG;
        this.popupBG = popupBG;
        this.popupName = popupName;
        this.addMouseListener(this);
        this.addFocusListener(this);
        //  this.setDoubleBuffered(false);
      //  setShowGrid(false);
    }

    public Color getBackground() {
        return UIColors.getTableBG();
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

    public void mouseDragged(MouseEvent mouseEvent) {
    }

    public void mouseClicked(MouseEvent e) {
        checkPopup(e);
        /* if (e.getClickCount() == 2)
         {
             int col = this.getTableHeader().columnAtPoint(e.getPoint());
             if (col != -1 )
                 this.addColumnSelectionInterval(col, col);
         }*/
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
                JPopupMenu p = ZCommandFactory.getPopup(sels, popupName);
                Component[] cmi = getCustomMenuItems();
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

                p.add(new AbstractAction("Select all") {
                    public void actionPerformed(ActionEvent e) {
                        PopupTable.this.selectAll();
                    }
                });
                p.add(new AbstractAction("Select row") {
                    public void actionPerformed(ActionEvent e) {
                        PopupTable.this.addColumnSelectionInterval(0, PopupTable.this.getColumnCount() - 1);
                        //PopupTable.this.getSelectionModel().addSelectionInterval(0, PopupTable.this.getRowCount() - 1);
                    }
                });
                p.add(new AbstractAction("Select column") {
                    public void actionPerformed(ActionEvent e) {
                        PopupTable.this.addRowSelectionInterval(0, PopupTable.this.getRowCount() - 1);
                        //PopupTable.this.getSelectionModel().addSelectionInterval(0, PopupTable.this.getRowCount() - 1);
                    }
                });
                p.add(new AbstractAction("Clear selection") {
                    public void actionPerformed(ActionEvent e) {
                        PopupTable.this.clearSelection();
                        //PopupTable.this.getSelectionModel().addSelectionInterval(0, PopupTable.this.getRowCount() - 1);
                    }
                });
                ZCommandFactory.showPopup(p, this, e);
                return true;
            }
        }
        return false;
    }

    protected Component[] getCustomMenuItems() {
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