package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.CordParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;

public class CordTable extends AbstractVoiceParameterTable implements ZDisposable {
    private String title;

    public CordTable(ReadablePreset.ReadableVoice voice, ReadableParameterModel[] parameterModels, String title) throws ZDeviceNotRunningException {
        this(voice, new CordTableModel(parameterModels), title);
    }

    public CordTable(ReadablePreset.ReadableVoice voice, CordTableModel pgtm, String title) throws ZDeviceNotRunningException {
        super(voice, ParameterCategories.VOICE_CORDS, pgtm, VoiceParameterTableTransferHandler.getInstance(), null, title + " >");
        this.title = title;
    }

    protected JMenuItem[] getCustomMenuItems() {
        return null;
    }

    public String getTableTitle() {
        return title;
    }

    public VoiceParameterSelection getSelection() {
        VoiceParameterSelection vps = super.getSelection();
        if (vps != null)
            return new CordParameterSelection(vps);
        return null;
    }

    public void zDispose() {
        super.zDispose();
    }
}
