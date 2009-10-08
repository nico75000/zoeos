package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;

public class VoiceParameterTable extends AbstractVoiceParameterTable implements ZDisposable {
    private String title;

    public VoiceParameterTable(ReadablePreset.ReadableVoice voice, String category, ReadableParameterModel[] parameterModels, String title) throws ZDeviceNotRunningException {
        this(voice, category, new VoiceParameterTableModel(parameterModels), title);
    }

    public VoiceParameterTable(ReadablePreset.ReadableVoice voice, String category, VoiceParameterTableModel pgtm, String title) throws ZDeviceNotRunningException {
        super(voice, category, pgtm, VoiceParameterTableTransferHandler.getInstance(), null, title + " >");
        this.title = title;
        //setDragEnabled(true);
    }

    protected void setupDropOverExtent() {
        dropOverExtent = -1;
    }

    protected JMenuItem[] getCustomMenuItems() {
        return null;
    }

    public String getTableTitle() {
        return title;
    }
}
