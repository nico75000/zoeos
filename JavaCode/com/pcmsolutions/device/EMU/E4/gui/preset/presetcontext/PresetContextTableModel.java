package com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTableModel;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.IntPool;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 25-Jun-2003
 * Time: 01:40:38
 * To change this template use Options | File Templates.
 */
public class PresetContextTableModel extends AbstractContextTableModel<PresetContext, ReadablePreset> implements PresetListener {

    public PresetContextTableModel(PresetContext pc) {
        init(pc);
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 45, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[1];
        // columnData[0] = new ColumnData("", 155, JLabel.LEFT, 0, ReadablePreset.class, new PresetContextTableCellRenderer(new Color(255, 255, 255, 200), new Color(242, 81, 103, 250), 0.6), null);
        columnData[0] = new ColumnData("", 155, JLabel.LEFT, 0, ReadablePreset.class, new PresetContextTableCellRenderer(), null);
        sectionData = new SectionData[]{new SectionData(UIColors.getTableFirstSectionBG(),UIColors.getTableFirstSectionHeaderBG(), UIColors.getTableFirstSectionFG(), 155, "")};
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1)
            return true;
        return false;
    }

    protected void doPreRefresh() {
        removePresetListeners();
    }

    protected void doPostRefresh() {
    }


    protected void xdoRefresh() {
        try {
            final List pic = getContext().getContextPresets();
            final DecimalFormat df = new DecimalFormat("0000");
            int count = 0;
            for (int i = 0, n = pic.size(); i < n; i++) {
                final ReadablePreset p = (ReadablePreset) pic.get(i);
                try {
                    if (!contextFilter.filter(p.getIndex(), p.getString(), prevIndexes.contains(p.getIndex())))
                        continue;
                } catch (PresetException e) {
                    continue;
                } /*catch (EmptyException e) {
                    if (!contextFilter.filter(p.getPresetNumber(), DeviceContext.EMPTY_PRESET, prevPresetIndexes.containsKey(p.getPresetNumber())))
                        continue;
                } */
                p.setToStringFormatExtended(false);
                indexes.put(p.getIndex(), IntPool.get(count++));
                tableRowObjects.add(new ColumnValueProvider() {
                    private ReadablePreset preset = p;

                    public Object getValueAt(int col) {
                        if (col == 0)
                            return "P " + df.format(preset.getIndex());
                        else if (col == 1)
                            return preset;
                        return "";
                    }

                    public void zDispose() {
                        preset = null;
                    }

                    public boolean equals(Object obj) {
                        if (obj instanceof Integer && obj.equals(preset.getIndex()))
                            return true;
                        return false;
                    }
                });
                p.addListener(this);
            }
        } catch (DeviceException e) {
        }
    }

    public boolean acceptElement(ReadablePreset readablePreset) {
        return true;
    }

    protected String getContextPrefix() {
        return "P ";
    }

    protected void finalizeRefreshedElement(ReadablePreset p) {
        p.addListener(this);
    }

    private void removePresetListeners() {
        Integer[] presets = new Integer[tableRowObjects.size()];
        for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
            Object o = ((ColumnValueProvider) tableRowObjects.get(i)).getValueAt(1);
            presets[i] = ((ReadablePreset) o).getIndex();
        }
        getContext().removeContentListener(this, presets);
    }

    public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev) {
        updateIndex(ev.getIndex());
    }

    public void presetRefreshed(PresetInitializeEvent ev) {
        updateIndex(ev.getIndex());
    }

    public void presetChanged(PresetChangeEvent ev) {
    }

    public void presetNameChanged(PresetNameChangeEvent ev) {
        updateIndex(ev.getIndex());
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
