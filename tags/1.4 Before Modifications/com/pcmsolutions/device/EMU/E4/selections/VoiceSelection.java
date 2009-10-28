package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Aug-2003
 * Time: 19:25:16
 * To change this template use Options | File Templates.
 */
public class VoiceSelection extends AbstractE4Selection {
    protected IsolatedPreset.IsolatedVoice[] voices;
    protected ReadablePreset.ReadableVoice[] readableVoices;

    public VoiceSelection(DeviceContext dc, ReadablePreset.ReadableVoice[] voices) {
        super(dc);
        this.readableVoices = voices;
    }

    public int voiceCount() {
        return readableVoices.length;
    }

    public IsolatedPreset.IsolatedVoice[] getIsolatedVoices() {
        if (voices == null) {
            voices = new IsolatedPreset.IsolatedVoice[readableVoices.length];
        }
        for (int i = 0,j = readableVoices.length; i < j; i++)
            if (voices[i] == null)
                voices[i] = getIsloatedVoice(i);
        return voices;
    }

    public IsolatedPreset.IsolatedVoice getIsloatedVoice(int i) {
        if (voices == null)
            voices = new IsolatedPreset.IsolatedVoice[readableVoices.length];

        if (i >= 0 && i < readableVoices.length) {
            if (voices[i] == null)
                try {
                    voices[i] = readableVoices[i].getIsolated();
                    return voices[i];
                } catch (EmptyException e) {
                    e.printStackTrace();
                } catch (PresetException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    public ReadablePreset.ReadableVoice[] getReadableVoices() {
        return (ReadablePreset.ReadableVoice[]) Arrays.asList(readableVoices).toArray(new ReadablePreset.ReadableVoice[readableVoices.length]);
    }
}
