package com.pcmsolutions.device.EMU.E4.zcommands.preset;


import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.callback.ShowCommandFailedCallback;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AuditionVoicesZMTC extends AbstractReadableVoiceZMTCommand {

    boolean stepped;

    public AuditionVoicesZMTC() {
        this(false);
    }

    public String getPresentationCategory() {
        return "Audition";
    }

    protected AuditionVoicesZMTC(boolean stepped) {
        this.stepped = stepped;
    }

    public boolean isSuitableAsButton() {
        return true;
    }

    public boolean isSuitableInToolbar() {
        return true;
    }

    public ZMTCommand getNextMode() {
        if (!stepped)
            return new AuditionVoicesZMTC(true);
        else
            return null;
    }

    public String getPresentationString() {
        if (stepped)
            return "AudS";
        else
            return "Aud";
    }

    public String getDescriptiveString() {
        if (stepped)
            return "Audition voices individually in sequenece";
        else
            return "Audition voices";
    }

    public boolean handleTarget(ReadablePreset.ReadableVoice voice, int total, int curr) throws Exception {
        ReadablePreset.ReadableVoice[] rva = getTargets().toArray(new ReadablePreset.ReadableVoice[numTargets()]);
        voice.getPreset().getDeviceContext().getDefaultPresetContext().auditionVoices(voice.getPreset().getIndex(), PresetContextMacros.extractVoiceIndexes(rva), stepped).post(ShowCommandFailedCallback.INSTANCE);
        return false;
    }

    public int getMinNumTargets() {
        if (stepped)
            return 2;
        else
            return 1;
    }

    public int getMaxNumTargets() {
        return (stepped ? Integer.MAX_VALUE : 128);
    }
}

