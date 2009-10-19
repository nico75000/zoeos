package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope;

import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.VoiceEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.IntPool;

import javax.swing.*;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.                   abstrac
 * User: pmeehan
 * Date: 01-Jul-2003
 * Time: 21:18:52
 * To change this template use Options | File Templates.
 */
public class EditableVoiceEnvelopePanel extends VoiceEnvelopePanel {
    protected ContextEditablePreset.EditableVoice[] voices;
    public EditableVoiceEnvelopePanel init(ContextEditablePreset.EditableVoice[] voices, String category, EditableParameterModel[] models, String title, Action toggleAction) {
        this.voices = voices;
        super.init(voices[0], category, models, title, toggleAction);
        return this;
    }

    public EditableVoiceEnvelopePanel init(ContextEditablePreset.EditableVoice[] voices, String category, Integer startId, String title, Action toggleAction) throws IllegalParameterIdException {
        this.voices = voices;
        super.init(voices[0], category, startId, title, toggleAction);
        return this;
    }

    protected void makeModelAndTable(String category, ReadableParameterModel[] models, String title) {
        envTableModel = new EditableVoiceEnvelopeTableModel((EditableParameterModel[]) (Arrays.asList(models).toArray(new EditableParameterModel[models.length])));
        envTable = new EditableVoiceEnvelopeTable(voices, category, (EditableVoiceEnvelopeTableModel) envTableModel, title);
    }

    protected void generateParameterModels(ReadablePreset.ReadableVoice voice, Integer startId, ReadableParameterModel[] models) throws IllegalParameterIdException {
        for (int i = 0; i < 12; i++)
            try {
                models[i] = ((ContextEditablePreset.EditableVoice) voice).getEditableParameterModel(IntPool.get(startId.intValue() + i));
            } catch (IllegalParameterIdException e) {
                for (int j = 0; j < i; j++)
                    models[i].zDispose();
                throw e;
            }
    }

    public void zDispose() {
        super.zDispose();
        voices = null;
    }
}
