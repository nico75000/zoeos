package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.LinkTable;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.LinkTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.DataFlavorGrid;
import com.pcmsolutions.device.EMU.E4.selections.LinkParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.LinkParameterSelectionCollection;
import com.pcmsolutions.device.EMU.E4.selections.LinkSelection;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class EditableLinkTable extends LinkTable implements ZDisposable {
    //protected ContextEditablePreset preset;

    public static final EditableLinkTableTransferHandler editableLinkTableTransferHandler = new EditableLinkTableTransferHandler();

    public EditableLinkTable(ContextEditablePreset p, int mode) throws DeviceException {
        super(new EditableLinkTableModel(p, p.getDeviceContext().getDeviceParameterContext(), mode));
        this.setTransferHandler(editableLinkTableTransferHandler);
        setDropChecker(new DropChecker() {
            public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {
                if (chosenDropFlavor instanceof DataFlavorGrid)
                    return defaultDropGridChecker.isCellDropTarget(dropRow, dropCol, row, col, value);
                else if (chosenDropFlavor instanceof LinkTableTransferHandler.LinkParameterCollectionDataFlavor) {
                    Integer cellId = ((ReadableParameterModel) getValueAt(row, col)).getParameterDescriptor().getId();
                    int count = ((LinkTableTransferHandler.LinkParameterCollectionDataFlavor) chosenDropFlavor).getCount();
                    if ((row >= dropRow) && (row < dropRow + count)
                            && ((LinkTableTransferHandler.LinkParameterCollectionDataFlavor) chosenDropFlavor).containsId(cellId))
                        return true;
                }
                return false;
            }
        });
        ParameterModelUtilities.registerTableForEditableParameterModelShortcuts(this);
        this.preset = p;
    }

    public boolean editCellAt(int row, int column, EventObject e) {
        if (ParameterModelTableCellEditor.testCellEditable(e) && ParameterModelTableCellEditor.tryToggleCellAt(this, row, column))
            return false;
        return super.editCellAt(row, column, e);
    }

    public void setSelection(LinkParameterSelectionCollection lpsc) {
        final int selRow = getSelectedRow();
        final int rc = getRowCount();
        final LinkParameterSelection[] data = lpsc.getSelections();
        final ContextEditablePreset.EditableLink[] links = new ContextEditablePreset.EditableLink[data.length];
        for (int i = 0, j = data.length; i < j; i++) {
            if (data[i] != null && (selRow + i < rc))
                links[i] = (ContextEditablePreset.EditableLink) getModel().getValueAt(selRow + i, 0);
        }
        //   Impl_ZThread.ddTQ.postTask(new Impl_ZThread.Task(){
        //     public void doTask() {
        for (int i = 0, j = data.length; i < j; i++) {
            if (data[i] != null && (selRow + i < rc))
                try {
                    data[i].render(links[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        //      }
        //  });
    }

    public interface LinkSelectionAcceptor {
        public void setSelection(LinkSelection ils);
    }

    protected void setupDropOverExtent() {
        if (chosenDropFlavor instanceof DataFlavorGrid)
            super.setupDropOverExtent();
        else if (chosenDropFlavor instanceof LinkTableTransferHandler.LinkParameterCollectionDataFlavor)
            this.dropOverExtent = ((LinkTableTransferHandler.LinkParameterCollectionDataFlavor) chosenDropFlavor).getCount();
        else
            this.dropOverExtent = -1;
    }

    protected class EditableLinkTableRowHeader extends LinkTableRowHeader implements EditableLinkTable.LinkSelectionAcceptor {
        {
            setDropChecker(new DropChecker() {
                public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {
                    return true;
                }
            });
        }

        protected void setupDropOverExtent() {
            this.dropOverExtent = -1;
        }

        public EditableLinkTableRowHeader(String popupName, Color popupBG, Color popupFG) {
            super(popupName, popupBG, popupFG);
        }

        public EditableLinkTableRowHeader(TransferHandler t, String popupName, Color popupBG, Color popupFG) {
            super(t, popupName, popupBG, popupFG);
        }

        public void zDispose() {
        }

        public void setSelection(final LinkSelection ils) {
            for (int i = 0, j = ils.linkCount(); i < j; i++)
                try {
                    ((ContextEditablePreset) EditableLinkTable.this.preset).newLink(ils.getIsolatedLink(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    };

    protected DragAndDropTable generateRowHeaderTable() {
        EditableLinkTable.EditableLinkTableRowHeader t = new EditableLinkTable.EditableLinkTableRowHeader(editableLinkTableTransferHandler, popupName, null, null) {
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
        return t;
    }

    protected ReadablePreset convertPassThroughPreset(ReadablePreset preset) {
        return preset;
    }
    /*protected JMenuItem[] getCustomMenuItems() {
        try {
            JMenuItem[] cjmi;
            if (customAction != null) {
                cjmi = new JMenuItem[2];
                cjmi[1] = new JMenuItem(customAction);
            } else
                cjmi = new JMenuItem[1];

            cjmi[0] = ZCommandFactory.getMenu(new Object[]{preset}, null, null, preset.getPresetDisplayName());
            return cjmi;
        } catch (DeviceException e) {
            e.printStackTrace();
        }
        return null;
    }
*/
}
