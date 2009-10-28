package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.ZDisposable;

import java.awt.*;

public class VoiceParameterTable extends AbstractVoiceParameterTable implements ZDisposable {
    private String title;

    public VoiceParameterTable(ReadablePreset.ReadableVoice voice, String category, ReadableParameterModel[] parameterModels, String title)  {
        this(voice, category, new VoiceParameterTableModel(parameterModels), title);
    }

    public VoiceParameterTable(ReadablePreset.ReadableVoice voice, String category, VoiceParameterTableModel pgtm, String title)  {
        super(voice, category, pgtm, VoiceParameterTableTransferHandler.getInstance(), null, title + " >");
        this.title = title;
        //setDragEnabled(true);
    }

    protected void setupDropOverExtent() {
        dropOverExtent = -1;
    }

    protected Component[] getCustomMenuItems() {
        return null;
    }

    public String getTableTitle() {
        return title;
    }
}
