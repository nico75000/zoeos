package com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTableModel;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.IntPool;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 25-Jun-2003
 * Time: 01:40:38
 * To change this template use Options | File Templates.
 */
public class PresetContextTableModel extends AbstractContextTableModel implements PresetContextListener, PresetListener {
    private PresetContext pc;
    private HashMap presetIndexes = new HashMap();
    private HashMap prevPresetIndexes;

    public PresetContextTableModel(PresetContext pc) {
        this.pc = pc;
        init();
        pc.addPresetContextListener(this);
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 45, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[1];
        // columnData[0] = new ColumnData("", 155, JLabel.LEFT, 0, ReadablePreset.class, new PresetContextTableCellRenderer(new Color(255, 255, 255, 200), new Color(242, 81, 103, 250), 0.6), null);
        columnData[0] = new ColumnData("", 155, JLabel.LEFT, 0, ReadablePreset.class, new PresetContextTableCellRenderer(), null);
        sectionData = new SectionData[]{new SectionData(UIColors.getTableFirstSectionBG(), UIColors.getTableFirstSectionFG(), 155, "")};
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1)
            return true;
        return false;
    }

    protected void doPreRefresh() {
        removePresetListeners();
        prevPresetIndexes = (HashMap) presetIndexes.clone();
        presetIndexes.clear();
    }

    protected void doPostRefresh() {
    }

    protected void doRefresh() {
        try {
            final List pic = pc.getContextPresets();
            final DecimalFormat df = new DecimalFormat("0000");
            int count = 0;
            for (int i = 0, n = pic.size(); i < n; i++) {
                final ReadablePreset p = (ReadablePreset) pic.get(i);
                try {
                    if (!contextFilter.filter(p.getPresetNumber(), p.getPresetName(), prevPresetIndexes.containsKey(p.getPresetNumber())))
                        continue;
                } catch (NoSuchPresetException e) {
                    continue;
                } catch (PresetEmptyException e) {
                    if (!contextFilter.filter(p.getPresetNumber(), DeviceContext.EMPTY_PRESET, prevPresetIndexes.containsKey(p.getPresetNumber())))
                        continue;
                }
                p.setToStringFormatExtended(false);
                presetIndexes.put(p.getPresetNumber(), IntPool.get(count++));
                tableRowObjects.add(new ColumnValueProvider() {
                    private ReadablePreset preset = p;

                    public Object getValueAt(int col) {
                        if (col == 0)
                            return "P " + df.format(preset.getPresetNumber());
                        else if (col == 1)
                            return preset;
                        return "";
                    }

                    public void zDispose() {
                        preset = null;
                    }

                    public boolean equals(Object obj) {
                        if (obj instanceof Integer && obj.equals(preset.getPresetNumber()))
                            return true;
                        return false;
                    }
                });
                p.addPresetListener(this);
            }
        } catch (NoSuchContextException e) {
        }
    }

    public int getRowForPreset(Integer preset) {
        Integer row = (Integer) presetIndexes.get(preset);
        if (row != null)
            return row.intValue();
        return -1;
    }

    private void removePresetListeners() {
        Integer[] presets = new Integer[tableRowObjects.size()];
        for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
            Object o = ((ColumnValueProvider) tableRowObjects.get(i)).getValueAt(1);
            presets[i] = ((ReadablePreset) o).getPresetNumber();
        }
        pc.removePresetListener(this, presets);
    }


    public void presetsRemovedFromContext(PresetContext pc, Integer[] presets) {
        refresh(false);
    }

    public void presetsAddedToContext(PresetContext pc, Integer[] presets) {
        refresh(false);
    }

    public void contextReleased(PresetContext pc) {
        refresh(false);
    }

    private void updatePreset(final Integer preset) {
        Integer index = (Integer) presetIndexes.get(preset);
        if (index != null)
            this.fireTableCellUpdated(index.intValue(), 1);
    }

    public void presetInitialized(final PresetInitializeEvent ev) {
        updatePreset(ev.getPreset());
    }

    public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev) {
        updatePreset(ev.getPreset());
    }

    public void presetRefreshed(PresetRefreshEvent ev) {
        updatePreset(ev.getPreset());
    }

    public void presetChanged(PresetChangeEvent ev) {
    }

    public void presetNameChanged(PresetNameChangeEvent ev) {
        updatePreset(ev.getPreset());
    }

    public void voiceAdded(VoiceAddEvent ev) {
    }

    public void voiceRemoved(VoiceRemoveEvent ev) {
    }

    public void voiceChanged(VoiceChangeEvent ev) {
    }

    public void linkAdded(LinkAddEvent ev) {
    }

    public void linkRemoved(LinkRemoveEvent ev) {
    }

    public void linkChanged(LinkChangeEvent ev) {
    }

    public void zoneAdded(ZoneAddEvent ev) {
    }

    public void zoneRemoved(ZoneRemoveEvent ev) {
    }

    public void zoneChanged(ZoneChangeEvent ev) {
    }
}
