package com.pcmsolutions.gui.midi;

import com.pcmsolutions.comms.ZMidiSystem;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Sep-2003
 * Time: 15:00:11
 * To change this template use Options | File Templates.
 */
public class MidiManagerTable extends AbstractRowHeaderedAndSectionedTable {
    public MidiManagerTable() {
        super(new MidiManagerTableModel().init(), null, "Midi Manager");
        this.setColumnSelectionAllowed(false);
        this.getRowHeader().setSelectionModel(this.getSelectionModel());
    }


    public String getTableTitle() {
        return "";
    }

    final Action togglePermission = new AbstractAction("Toggle permission") {
        public void actionPerformed(ActionEvent e) {
            int[] selRows = getSelectedRows();
            for (int i = 0, j = selRows.length; i < j; i++) {
                MidiDevice.Info dev = (MidiDevice.Info) getModel().getValueAt(selRows[i], 0);
                if (dev != null)
                    try {
                        ZMidiSystem.getInstance().setPortPermitted(dev, false);
                    } catch (MidiUnavailableException e1) {
                        e1.printStackTrace();
                    }
            }
        }
    };

    protected Component[] getCustomMenuItems() {
        JMenuItem[] items = new JMenuItem[1];
        items[0] = new JMenuItem(togglePermission);
        return items;
    }

    protected DragAndDropTable generateRowHeaderTable() {
        DragAndDropTable t = new DragAndDropTable(popupName, null, null) {

            public void zDispose() {
            }

            protected Component[] getCustomMenuItems() {
                JMenuItem[] items = new JMenuItem[1];
                items[0] = new JMenuItem(togglePermission);
                return items;
            }

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
