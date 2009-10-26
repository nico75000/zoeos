package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.DevicePreferences;
import com.pcmsolutions.system.IntPool;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import java.awt.event.ActionEvent;


public class FilterEnvelopePanel extends VoiceEnvelopePanel {
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

    public FilterEnvelopePanel init(ReadablePreset.ReadableVoice voice) throws IllegalParameterIdException {
        super.init(voice, ParameterCategories.VOICE_FILTER_ENVELOPE, IntPool.get(93), "Filter Envelope", toggleAction);
        DevicePreferences.ZPREF_fillFilterEnvelopes.addChangeListener(cl);
        DevicePreferences.ZPREF_filterEnvelopeMode.addChangeListener(cl);
        getEnvelope().addMouseListener(new RatesEnvelopeMouseListener(getEnvelope(), DevicePreferences.ZPREF_fillFilterEnvelopes, DevicePreferences.ZPREF_filterEnvelopeMode));
        updateEnvelope();

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
}
