package com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTable;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.ContextPresetSelection;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.gui.DisabledTransferHandler;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 21-May-2003
 * Time: 01:19:26
 * To change this template use Options | File Templates.
 */
public class PresetContextTable extends AbstractContextTable<PresetContext, ReadablePreset> {
    private static PresetContextTransferHandler pcth = new PresetContextTransferHandler();

    public PresetContextTable(PresetContext pc) {
        super(pc, new PresetContextTableModel(pc), pcth, null/*, new RowHeaderTableCellRenderer(UIColors.getMultimodeRowHeaderBG(), UIColors.getMultimodeRowHeaderFG())*/, "Preset>");
        //setDropChecker(defaultDropGridChecker);
        setDropChecker(new DropChecker() {
            public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {
                if (chosenDropFlavor.equals(SampleContextTransferHandler.sampleContextFlavor))
                    return col == dropCol && row == dropRow;
                else
                    return defaultDropGridChecker.isCellDropTarget(dropRow, dropCol, row, col, value);
            }
        });
        setMaximumSize(getPreferredSize());
        this.getRowHeader().setSelectionModel(this.getSelectionModel());
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
                    final Object o = getModel().getValueAt(this.rowAtPoint(e.getPoint()), 1);
                    if (o instanceof ReadablePreset)
                        try {
                            ((ReadablePreset) o).audition().post(new Callback() {
                                public void result(Exception e, boolean wasCancelled) {
                                    if ( e!=null && !wasCancelled)
                                    UserMessaging.flashWarning(null, e.getMessage());
                                }
                            });
                        } catch (ResourceUnavailableException e1) {
                            UserMessaging.flashWarning(null, e1.getMessage());
                        }
                } else
                    super.mouseClicked(e);
            }
        };
        t.setTransferHandler(DisabledTransferHandler.getInstance());
        return t;
    }

    public ContextPresetSelection getSelection() {
        Object[] sobjs = this.getSelObjects();
        ArrayList readablePresets = new ArrayList();

        for (int i = 0, j = sobjs.length; i < j; i++)
            if (sobjs[i] instanceof ReadablePreset)
                readablePresets.add(sobjs[i]);

        return new ContextPresetSelection(getContext().getDeviceContext(), getContext(), (ReadablePreset[]) readablePresets.toArray(new ReadablePreset[readablePresets.size()]));
    }

    public Integer selectPresetsByRegex(String regexStr, boolean fullMatch, boolean useDisplayName, boolean newSelection) {
        if (regexStr == null)
            return null;

        getSelectionModel().setValueIsAdjusting(true);
        try {
            Pattern p = Pattern.compile(regexStr);
            PresetContextTableModel pctm = (PresetContextTableModel) getModel();
            Matcher m;
            if (newSelection)
                this.clearSelection();
            String name;
            Integer firstSelectedPreset = null;
            for (int i = 0, j = pctm.getRowCount(); i < j; i++) {
                ReadablePreset preset = (ReadablePreset) pctm.getValueAt(i, 1);
                try {
                    if (useDisplayName)
                        name = preset.getDisplayName();
                    else
                        name = preset.getName();
                } catch (EmptyException e) {
                    name = DeviceContext.EMPTY_PRESET;
                } catch (PresetException e) {
                    continue;
                }
                m = p.matcher(name);

                boolean res = false;
                if (fullMatch)
                    res = m.matches();
                else
                    res = m.find();

                if (res) {
                    if (firstSelectedPreset == null)
                        firstSelectedPreset = preset.getIndex();
                    this.addRowSelectionInterval(i, i);
                    this.addColumnSelectionInterval(0, 0);
                }
            }
            return firstSelectedPreset;
        } finally {
            getSelectionModel().setValueIsAdjusting(false);
        }
    }

    public String getTableTitle() {
        return " ";
    }

    public void zDispose() {
       super.zDispose();
    }
}
