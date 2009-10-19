package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.DevicePreferences;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.preferences.ZBoolPref;
import com.pcmsolutions.system.preferences.ZEnumPref;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;


public class AmpEnvelopePanel extends VoiceEnvelopePanel {
    Action toggleAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            String s = DevicePreferences.ZPREF_ampEnvelopeMode.getValue();
            if (s.equals(DevicePreferences.ENVELOPE_MODE_FIXED))
                DevicePreferences.ZPREF_ampEnvelopeMode.putValue(DevicePreferences.ENVELOPE_MODE_SCALED);
            else if (s.equals(DevicePreferences.ENVELOPE_MODE_SCALED))
                DevicePreferences.ZPREF_ampEnvelopeMode.putValue(DevicePreferences.ENVELOPE_MODE_FIXED);
        }
    };
    ChangeListener cl = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            updateEnvelope();
        }
    };

    public AmpEnvelopePanel init(ReadablePreset.ReadableVoice voice) throws IllegalParameterIdException {
        super.init(voice, ParameterCategories.VOICE_AMPLIFIER_ENVELOPE, IntPool.get(70), "Amp Envelope", toggleAction);
        DevicePreferences.ZPREF_fillAmpEnvelopes.addChangeListener(cl);
        DevicePreferences.ZPREF_ampEnvelopeMode.addChangeListener(cl);
        getEnvelope().addMouseListener(new RatesEnvelopeMouseListener(getEnvelope(), DevicePreferences.ZPREF_fillAmpEnvelopes, DevicePreferences.ZPREF_ampEnvelopeMode));
        updateEnvelope();
        return this;
    }


    protected void updateEnvelope() {
        this.getEnvelope().setFill(DevicePreferences.ZPREF_fillAmpEnvelopes.getValue());
        String s = DevicePreferences.ZPREF_ampEnvelopeMode.getValue();
        if (s.equals(DevicePreferences.ENVELOPE_MODE_FIXED))
            getEnvelope().setMode(RatesEnvelope.MODE_FIXED);
        else if (s.equals(DevicePreferences.ENVELOPE_MODE_SCALED))
            getEnvelope().setMode(RatesEnvelope.MODE_SCALED);
    }

    public void zDispose() {
        super.zDispose();
        DevicePreferences.ZPREF_fillAmpEnvelopes.removeChangeListener(cl);
        DevicePreferences.ZPREF_ampEnvelopeMode.removeChangeListener(cl);
    }
}
