package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnAndSectionDataProvider;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 08:51:55
 * To change this template use Options | File Templates.
 */
abstract public class AbstractPresetTableModel extends AbstractRowHeaderedAndSectionedTableModel implements PresetListener, ColumnAndSectionDataProvider, ZDisposable {
    protected ArrayList parameterObjects = new ArrayList();
    protected ReadablePreset preset;
    protected ParameterContext pc;
    protected boolean ignorePresetInitialize = false;

    public AbstractPresetTableModel(ReadablePreset p, ParameterContext pc) {
        this.preset = p;
        this.pc = pc;
        preset.addListener(this);
    }

    public AbstractRowHeaderedAndSectionedTableModel init() {
        buildDefaultParameterData(pc);
        return super.init();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {

        if (columnIndex == 0)
            return false;
        if (tableRowObjects.get(rowIndex) instanceof AbstractPresetTableModel.ColumnEditableTester)
            return ((ColumnEditableTester) tableRowObjects.get(rowIndex)).isColumnEditable(columnIndex);
        return false;
    }


    final Runnable nsp = new Runnable() {
        public void run() {
            clearRows();
            fireTableDataChanged();
        }
    };

    final Runnable ep = new Runnable() {
        public void run() {
            clearRows();
            fireTableDataChanged();
        }
    };

    public void handleEmptyException() {
        //SwingUtilities.invokeLater(ep);
        ep.run();
    }

    public void handlePresetException() {
        //SwingUtilities.invokeLater(nsp);
        ep.run();
    }

    // req_count = number of tokens required from end of string
    protected String getColNameFromRefString(String rn, int req_count) {
        StringTokenizer tok;

        tok = new StringTokenizer(rn, "_");

        int count = tok.countTokens();
        String outStr = "";
        String nt;
        for (int i = 0; tok.hasMoreTokens(); i++) {
            nt = tok.nextToken();
            if (count - i <= req_count)
                if (!nt.equals("voice"))
                    if (outStr.equals(""))
                        outStr += nt;
                    else
                        outStr += "_" + nt;
        }
        return outStr;
    }

    public ReadablePreset getPreset() {
        return preset;
    }

    abstract protected void buildDefaultParameterData(ParameterContext vc);

    protected interface ColumnEditableTester {
        public boolean isColumnEditable(int column);
    }

    public void presetInitializationStatusChanged(final PresetInitializationStatusChangedEvent ev) {
    }

    public void presetRefreshed(PresetInitializeEvent ev) {
        refresh(false);
    }

    public void presetChanged(PresetChangeEvent ev) {
    }

    public void presetNameChanged(PresetNameChangeEvent ev) {
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

    public void zDispose() {
        super.zDispose();
        preset.removeListener(this);
        preset = null;
        pc = null;
        parameterObjects = null;
    }
}
