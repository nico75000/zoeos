package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope;

import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.AbstractVoiceParameterTable;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceParameterTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-Jul-2003
 * Time: 02:12:39
 * To change this template use Options | File Templates.
 */
public class VoiceEnvelopeTable extends AbstractVoiceParameterTable {
    protected VoiceEnvelopeTableModel model;
    protected String title;

    public VoiceEnvelopeTable(ReadablePreset.ReadableVoice voice, String category, VoiceEnvelopeTableModel model, String title) {
        super(voice, category, model, VoiceParameterTableTransferHandler.getInstance(), null, title);
        this.model = model;
        this.title = title;
    }

    public String getTableTitle() {
        return title;
    }
}
