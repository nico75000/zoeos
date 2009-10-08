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


public class EditableAuxEnvelopePanel extends EditableVoiceEnvelopePanel {
    Action toggleAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            String s = DevicePreferences.ZPREF_auxEnvelopeMode.getValue();
            if (s.equals(DevicePreferences.ENVELOPE_MODE_FIXED))
                DevicePreferences.ZPREF_auxEnvelopeMode.putValue(DevicePreferences.ENVELOPE_MODE_SCALED);
            else if (s.equals(DevicePreferences.ENVELOPE_MODE_SCALED))
                DevicePreferences.ZPREF_auxEnvelopeMode.putValue(DevicePreferences.ENVELOPE_MODE_FIXED);
        }
    };
    ChangeListener cl = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            updateEnvelope();
        }
    };

    public EditableAuxEnvelopePanel init(ContextEditablePreset.EditableVoice[] voices) throws IllegalParameterIdException {
        init(voices, ParameterModelUtilities.getEditableParameterModelGroups(voices, 117, 12));
        return this;
    }

    private EditableAuxEnvelopePanel init(ContextEditablePreset.EditableVoice[] voices, EditableParameterModel[] models) {
        super.init(voices, ParameterCategories.VOICE_AUX_ENVELOPE, models, "Aux Envelope", toggleAction);
        getEnvModel().setMinLevel(-100);
        DevicePreferences.ZPREF_fillAuxEnvelopes.addChangeListener(cl);
        DevicePreferences.ZPREF_auxEnvelopeMode.addChangeListener(cl);
        getEnvelope().addMouseListener(new RatesEnvelopeMouseListener(getEnvelope(), DevicePreferences.ZPREF_fillAuxEnvelopes, DevicePreferences.ZPREF_auxEnvelopeMode));
        return this;
    }

    /* public EditableAuxEnvelopePanel stateInitial(ContextEditablePreset.EditableVoice voice) throws IllegalParameterIdException {
         super.stateInitial(voice, ParameterCategories.VOICE_AUX_ENVELOPE, IntPool.get(117), "Aux Envelope");
         getEnvModel().setMinLevel(-100);
         return this;
     }*/
    protected void updateEnvelope() {
        this.getEnvelope().setFill(DevicePreferences.ZPREF_fillAuxEnvelopes.getValue());
        String s = DevicePreferences.ZPREF_auxEnvelopeMode.getValue();
        if (s.equals(DevicePreferences.ENVELOPE_MODE_FIXED))
            getEnvelope().setMode(RatesEnvelope.MODE_FIXED);
        else if (s.equals(DevicePreferences.ENVELOPE_MODE_SCALED))
            getEnvelope().setMode(RatesEnvelope.MODE_SCALED);
    }

    public void zDispose() {
        super.zDispose();
        DevicePreferences.ZPREF_fillAuxEnvelopes.removeChangeListener(cl);
        DevicePreferences.ZPREF_auxEnvelopeMode.removeChangeListener(cl);
    }
}
