package com.pcmsolutions.device.EMU.E4.gui.multimode;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.MultiModeSelection;
import com.pcmsolutions.gui.DisabledTransferHandler;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZCommandFactory;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

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

    public MultiModeTable(DeviceContext device, boolean just16) throws DeviceException {
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
        this.getRowHeader().setSelectionModel(this.getSelectionModel());
    }

    public MultiModeSelection getSelection() throws DeviceException {
        int[] selRows = getSelectedRows();

        int[] selCols = getSelectedColumns();
        // map columns to TableColumnModel
        for (int i = 0, n = selCols.length; i < n; i++)
            selCols[i] = convertColumnIndexToModel(selCols[i]) - 1; // +1 used here to compensate for row header

        return new MultiModeSelection(device, mmtm.getMultimodeContext(), selCols, selRows);
    }

    public void setSelection(final MultiModeSelection mms) {
        //  Impl_ZThread.ddTQ.postTask(new Impl_ZThread.Task(){
        //      public void doTask() {
        mms.render(mmtm.getMultimodeContext(), getSelectedRow() + 1);
        //      }
        //  });
    }

    protected DragAndDropTable generateRowHeaderTable() {
        DragAndDropTable t = new DragAndDropTable(popupName, null, null) {
            public void zDispose() {
            }

            protected Component[] getCustomMenuItems() {
                return customRowHeaderMenuItems;
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final int row = this.rowAtPoint(e.getPoint());
                    try {
                        device.getMultiModeContext().getMultiModeChannel(IntPool.get(row + 1)).audition().post(new Callback() {
                            public void result(Exception e, boolean wasCancelled) {
                                if (e != null && !wasCancelled)
                                    UserMessaging.flashWarning(MultiModeTable.this, e.getMessage());
                            }
                        });
                    } catch (Exception e1) {
                        UserMessaging.flashWarning(MultiModeTable.this, e1.getMessage());
                    }
                } else
                    super.mouseClicked(e);
            }
        };
        t.setTransferHandler(DisabledTransferHandler.getInstance());
        return t;
    }


    public DeviceContext getDevice() {
        return device;
    }

    protected Component[] getCustomMenuItems() {
        final int[] selRows = this.getSelectedRows();
        Action dc = new AbstractAction("Disable channel") {
            public void actionPerformed(ActionEvent e) {
                MultiModeContext mmc = null;
                try {
                    mmc = device.getMultiModeContext();
                    for (int i = 0, j = selRows.length; i < j; i++) {
                        try {
                            mmc.setPreset(IntPool.get(selRows[i] + 1), IntPool.get(-1)).post();
                        } catch (ResourceUnavailableException e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (DeviceException e1) {
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
                String name = (sp.length > 1 ? "Presets on selected channels" : ((ReadablePreset) sp[0]).getDisplayName());
                pmi = ZCommandFactory.getMenu(sp, name);
            }
        } catch (Exception e) {
        }

        Action stm = null;
        if (this.getSelectedRows().length == 1 && this.getSelectedRow() < 16)
            stm = new AbstractAction("Set Ch " + (getSelectedRow() + 1) + " as effects channel") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        device.getMasterContext().setMasterParam(IntPool.get(245), IntPool.get(getSelectedRow())).post();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            };
        ArrayList comps = new ArrayList();
        //comps.add(new PopupCategoryLabel("Multimode"));
        JMenuItem[] mi;
        Component[] mmi;
        if (pmi != null)
            mi = new JMenuItem[]{pmi, new JMenuItem(dc)};
        else
            mi = new JMenuItem[]{new JMenuItem(dc)};

        mmi = ZCommandFactory.getMenu(new Object[]{mmtm.getMultimodeContext()}, "Multimode").getMenuComponents();
        comps.addAll(Arrays.asList(mmi));
        comps.addAll(Arrays.asList(mi));
        if (stm != null)
            comps.add(new JMenuItem(stm));
        return (Component[]) comps.toArray(new Component[comps.size()]);
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
