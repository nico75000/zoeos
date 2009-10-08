package com.pcmsolutions.gui.smdi;

import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Sep-2003
 * Time: 15:00:11
 * To change this template use Options | File Templates.
 */
public class SmdiManagerTable extends AbstractRowHeaderedAndSectionedTable {
    public SmdiManagerTable() {
        super(new SmdiManagerTableModel().init(), null, "SMDI Manager");
        this.setColumnSelectionAllowed(false);
        this.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.hidingSelectionOnFocusLost = false;
    }

    public String getTableTitle() {
        return "";
    }

    protected JMenuItem[] getCustomMenuItems() {
        /*JMenuItem[] items = new JMenuItem[2];
        items[0] = new JMenuItem(new AbstractAction("Ignore port") {
            public void actionPerformed(ActionEvent e) {
                int[] selRows = getSelectedRows();
                for (int i = 0,j = selRows.length; i < j; i++) {
                    Object tok = getModel().getValueAt(selRows[i], 0);
                    if (tok != null && !tok.equals(""))
                        MidiSystemFacade.getInstance().addIgnoreToken(tok);
                }
            }
        });
        items[1] = new JMenuItem(new AbstractAction("Permit port") {
            public void actionPerformed(ActionEvent e) {
                int[] selRows = getSelectedRows();
                for (int i = 0,j = selRows.length; i < j; i++) {
                    Object tok = getModel().getValueAt(selRows[i], 0);
                    if (tok != null && !tok.equals(""))
                        MidiSystemFacade.getInstance().removeIgnoreToken(tok);
                }
            }
        });

        return items;
        */
        return new JMenuItem[0];
    }

    protected DragAndDropTable generateRowHeaderTable() {
        DragAndDropTable t = new DragAndDropTable(popupName, null, null) {
            {
                this.hidingSelectionOnFocusLost = false;
            }

            public void zDispose() {
            }

            /*protected JMenuItem[] getCustomMenuItems() {
                JMenuItem[] items = new JMenuItem[2];
                items[0] = new JMenuItem(new AbstractAction("Ignore port") {
                    public void actionPerformed(ActionEvent e) {
                        int[] selRows = getSelectedRows();
                        for (int i = 0,j = selRows.length; i < j; i++) {
                            Object tok = getModel().getValueAt(selRows[i], 0);
                            if (tok != null && !tok.equals(""))
                                MidiSystemFacade.getInstance().addIgnoreToken(tok);
                        }
                    }
                });
                items[1] = new JMenuItem(new AbstractAction("Permit port") {
                    public void actionPerformed(ActionEvent e) {
                        int[] selRows = getSelectedRows();
                        for (int i = 0,j = selRows.length; i < j; i++) {
                            Object tok = getModel().getValueAt(selRows[i], 0);
                            if (tok != null && !tok.equals(""))
                                MidiSystemFacade.getInstance().removeIgnoreToken(tok);
                        }
                    }
                });
                return items;
            }
              */
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (!e.isConsumed() && e.getClickCount() == 2)
                    this.clearSelection();
            }
        };
        //t.setTransferHandler(null);
        return t;
    }

}
