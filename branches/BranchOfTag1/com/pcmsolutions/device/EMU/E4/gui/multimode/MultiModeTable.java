package com.pcmsolutions.device.EMU.E4.gui.multimode;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMidiChannelException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.MultiModeSelection;
import com.pcmsolutions.gui.ZCommandInvocationHelper;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-May-2003
 * Time: 08:51:31
 * To change this template use Options | File Templates.
 */
public class MultiModeTable extends AbstractRowHeaderedAndSectionedTable implements ZDisposable {
    private DeviceContext device;
   // private ParameterModelTableCellEditor pmtce;
   // private MultiModePresetTableCellEditor mmptce;
    private static MultiModeTransferHandler mmth = new MultiModeTransferHandler();
    private MultiModeTableModel mmtm;

    public MultiModeTable(DeviceContext device, boolean just16) throws ZDeviceNotRunningException {
        super(new MultiModeTableModel(device, just16), mmth, null /*new RowHeaderTableCellRenderer(UIColors.getMultimodeRowHeaderBG(), UIColors.getMultimodeRowHeaderFG())*/, "MultiMode >");
        this.mmtm = (MultiModeTableModel) getModel();
        this.device = device;
        setDropChecker(defaultDropGridChecker);
        setMaximumSize(getPreferredSize());

        /*this.setDropChecker(new DragAndDropTable.DropChecker() {
            public boolean isCellDropTarget(DataFlavor[] flavors, int dropRow, int dropCol, int row, int col) {
                if (flavors == null)
                    return false;
                for (int i = 0,j = flavors.length; i < j; i++) {
                    if (flavors[i] instanceof DataFlavorGrid) {
                        if (row >= dropRow)
                            if (MultiModeTable.this.isInPlaceGridDrop())
                                return ((DataFlavorGrid) flavors[i]).isCellPresent(row - dropRow, col);
                            else
                                return ((DataFlavorGrid) flavors[i]).isRowNormalizedCellPresent(row - dropRow, col);

                        break;
                    }
                }
                if (row == dropRow)
                    return true;
                return false;
            }
        });*/
        ParameterModelUtilities.registerTableForEditableParameterModelShortcuts(this);
    }

    public MultiModeSelection getSelection() {
        int[] selRows = getSelectedRows();

        int[] selCols = getSelectedColumns();
        // map columns to TableColumnModel
        for (int i = 0, n = selCols.length; i < n; i++)
            selCols[i] = convertColumnIndexToModel(selCols[i]) - 1; // +1 used here to compensate for row header

        return new MultiModeSelection(device, mmtm.getMultimodeContext(), selCols, selRows);
    }

    public void setSelection(MultiModeSelection mms) {
        mms.render(mmtm.getMultimodeContext(), getSelectedRow() + 1);
    }

    public DeviceContext getDevice() {
        return device;
    }

    protected JMenuItem[] getCustomMenuItems() {
        final int[] selRows = this.getSelectedRows();
        Action sda = new AbstractAction("Disable Channel") {
            public void actionPerformed(ActionEvent e) {
                MultiModeContext mmc = null;
                try {
                    mmc = device.getMultiModeContext();
                    for (int i = 0,j = selRows.length; i < j; i++) {
                        try {
                            mmc.setPreset(IntPool.get(selRows[i] + 1), IntPool.get(-1));
                        } catch (IllegalMidiChannelException e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (ZDeviceNotRunningException e1) {
                    e1.printStackTrace();
                }
            }
        };
        JMenuItem pmi = null;
        try {
            ArrayList selPresets = new ArrayList();
            PresetContext dpc = device.getDefaultPresetContext();
            MultiModeContext mmc = device.getMultiModeContext();
            for (int i = 0; i < selRows.length; i++) {
                Integer preset = mmc.getPreset(IntPool.get(selRows[i] + 1));        // +1 because midi channels indexed from 1
                if (preset.intValue() >= 0)
                    selPresets.add(dpc.getReadablePreset(preset));
            }
            if (selPresets.size() > 0) {

                Object[] sp = ZUtilities.eliminateDuplicates(selPresets.toArray());
                String name = (sp.length > 1 ? "Presets on selected channels" : ((ReadablePreset) sp[0]).getPresetDisplayName());
                pmi = ZCommandInvocationHelper.getMenu(sp, null, null, name);
            }
        } catch (Exception e) {
        }

        if (pmi != null)
            return new JMenuItem[]{new JMenuItem(sda), pmi, ZCommandInvocationHelper.getMenu(new Object[]{mmtm.getMultimodeContext()}, null, null, "MultiMode")};
        else
            return new JMenuItem[]{new JMenuItem(sda), ZCommandInvocationHelper.getMenu(new Object[]{mmtm.getMultimodeContext()}, null, null, "MultiMode")};
    }

    public void zDispose() {
        super.zDispose();
        device = null;
        mmtm = null;
        mmth = null;
    }

    public String getTableTitle() {
        return " ";
    }
}
