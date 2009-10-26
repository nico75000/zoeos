package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.RatesEnvelope;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.RatesEnvelopeMouseListener;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.DevicePreferences;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;


public class EditableFilterEnvelopePanel extends EditableVoiceEnvelopePanel {
    Action toggleAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            String s = DevicePreferences.ZPREF_filterEnvelopeMode.getValue();
            if (s.equals(DevicePreferences.ENVELOPE_MODE_FIXED))
                DevicePreferences.ZPREF_filterEnvelopeMode.putValue(DevicePreferences.ENVELOPE_MODE_SCALED);
            else if (s.equals(DevicePreferences.ENVELOPE_MODE_SCALED))
                DevicePreferences.ZPREF_filterEnvelopeMode.putValue(DevicePreferences.ENVELOPE_MODE_FIXED);
        }
    };
    ChangeListener cl = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            updateEnvelope();
        }
    };

    public EditableFilterEnvelopePanel init(ContextEditablePreset.EditableVoice[] voices) throws IllegalParameterIdException {
        init(voices, ParameterModelUtilities.getEditableParameterModelGroups(voices, 93, 12));
        return this;
    }

    private EditableFilterEnvelopePanel init(ContextEditablePreset.EditableVoice[] voices, EditableParameterModel[] models) {
        super.init(voices, ParameterCategories.VOICE_FILTER_ENVELOPE, models, "Filter Envelope", toggleAction);
        getEnvModel().setMinLevel(-100);
        DevicePreferences.ZPREF_fillFilterEnvelopes.addChangeListener(cl);
        DevicePreferences.ZPREF_filterEnvelopeMode.addChangeListener(cl);
        getEnvelope().addMouseListener(new RatesEnvelopeMouseListener(getEnvelope(), DevicePreferences.ZPREF_fillFilterEnvelopes, DevicePreferences.ZPREF_filterEnvelopeMode));
        return this;
    }

    protected void updateEnvelope() {
        this.getEnvelope().setFill(DevicePreferences.ZPREF_fillFilterEnvelopes.getValue());
        String s = DevicePreferences.ZPREF_filterEnvelopeMode.getValue();
        if (s.equals(DevicePreferences.ENVELOPE_MODE_FIXED))
            getEnvelope().setMode(RatesEnvelope.MODE_FIXED);
        else if (s.equals(DevicePreferences.ENVELOPE_MODE_SCALED))
            getEnvelope().setMode(RatesEnvelope.MODE_SCALED);
    }

    public void zDispose() {
        super.zDispose();
        DevicePreferences.ZPREF_fillFilterEnvelopes.removeChangeListener(cl);
        DevicePreferences.ZPREF_filterEnvelopeMode.removeChangeListener(cl);
    }
    /* public EditableFilterEnvelopePanel stateInitial(ContextEditablePreset.EditableVoice[] voices) throws IllegalParameterIdException {
         super.stateInitial(voice, ParameterCategories.VOICE_FILTER_ENVELOPE, IntPool.get(93), "Filter Envelope");
         getEnvModel().setMinLevel(-100);
         return this;
     }*/
}
