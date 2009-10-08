package com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.ContextPresetSelection;
import com.pcmsolutions.system.threads.ZDBModifyThread;

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
public class PresetContextTable extends AbstractRowHeaderedAndSectionedTable {
    private PresetContext presetContext;
    private static PresetContextTransferHandler pcth = new PresetContextTransferHandler();

    public PresetContextTable(PresetContext pc) {
        super(new PresetContextTableModel(pc), pcth, null/*, new RowHeaderTableCellRenderer(UIColors.getMultimodeRowHeaderBG(), UIColors.getMultimodeRowHeaderFG())*/, "Preset>");
        this.presetContext = pc;
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
    }

    public PresetContext getPresetContext() {
        return presetContext;
    }

    public boolean editCellAt(int row, int column, EventObject e) {
        if (e instanceof MouseEvent && ((MouseEvent) e).getClickCount() >= 2) {
            if (getValueAt(row, column) instanceof ReadablePreset) {
                final ReadablePreset p = (ReadablePreset) getValueAt(row, column);
                new ZDBModifyThread("Preset Default Action") {
                    public void run() {
                        p.performDefaultAction();
                    }
                }.start();
            }
        }
        return false;
    }

    public ContextPresetSelection getSelection() {
        Object[] sobjs = this.getSelObjects();
        ArrayList readablePresets = new ArrayList();

        for (int i = 0,j = sobjs.length; i < j; i++)
            if (sobjs[i] instanceof ReadablePreset)
                readablePresets.add(sobjs[i]);

        return new ContextPresetSelection(presetContext.getDeviceContext(), presetContext, (ReadablePreset[]) readablePresets.toArray(new ReadablePreset[readablePresets.size()]));
    }

    public Integer selectPresetsByRegex(String regexStr, boolean fullMatch, boolean useDisplayName, boolean newSelection) {
        if (regexStr == null)
            return null;
        Pattern p = Pattern.compile(regexStr);
        PresetContextTableModel pctm = (PresetContextTableModel) getModel();
        Matcher m;
        if (newSelection)
            this.clearSelection();
        String name;
        Integer firstSelectedPreset = null;
        for (int i = 0,j = pctm.getRowCount(); i < j; i++) {
            ReadablePreset preset = (ReadablePreset) pctm.getValueAt(i, 1);
            try {
                if (useDisplayName)
                    name = preset.getPresetDisplayName();
                else
                    name = preset.getPresetName();
            } catch (NoSuchPresetException e) {
                continue;
            } catch (PresetEmptyException e) {
                name = DeviceContext.EMPTY_PRESET;
            }
            m = p.matcher(name);

            boolean res = false;
            if (fullMatch)
                res = m.matches();
            else
                res = m.find();

            if (res) {
                if (firstSelectedPreset == null)
                    firstSelectedPreset = preset.getPresetNumber();
                this.addRowSelectionInterval(i, i);
                this.addColumnSelectionInterval(0, 0);
            }
        }
        return firstSelectedPreset;
    }

    // inclusive (will ignore indexes that are not available)
    public void addPresetToSelection(Integer preset) {
        PresetContextTableModel pctm = (PresetContextTableModel) getModel();
        int row = pctm.getRowForPreset(preset);
        if (row != -1) {
            addRowSelectionInterval(row, row);
            addColumnSelectionInterval(0, 0);
            //this.getSelectionModel().addSelectionInterval(row, row);
        }
    }

    public void addPresetsToSelection(Integer[] presets) {
        for (int i = 0; i < presets.length; i++)
            addPresetToSelection(presets[i]);
    }

    public void invertSelection() {
        int[] selRows = this.getSelectedRows();
        this.selectAll();
        for (int i = 0; i < selRows.length; i++)
            this.removeRowSelectionInterval(selRows[i], selRows[i]);
    }

    public int getRowForPreset(Integer preset) {
        PresetContextTableModel pctm = (PresetContextTableModel) getModel();
        return pctm.getRowForPreset(preset);
    }

    // will do nothing if index does not available
    public void scrollToPreset(Integer preset) {
        PresetContextTableModel pctm = (PresetContextTableModel) getModel();
        int row = pctm.getRowForPreset(preset);
        Rectangle cellRect = this.getCellRect(row, 0, true);
        this.scrollRectToVisible(cellRect);
    }

    public boolean showingAllPresets(Integer[] presets) {
        PresetContextTableModel pctm = (PresetContextTableModel) getModel();
        for (int i = 0; i < presets.length; i++)
            if (pctm.getRowForPreset(presets[i]) == -1)
                return false;
        return true;
    }

    public String getTableTitle() {
        return " ";
    }

    public void zDispose() {
        presetContext = null;
    }
}
