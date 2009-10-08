package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope;

import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.IntPool;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 29-Jun-2003
 * Time: 22:04:38
 * To change this template use Options | File Templates.
 */
public class VoiceEnvelopeModel extends DefaultRatesEnvelopeModel implements PresetListener {
    protected ReadablePreset.ReadableVoice voice;
    protected Integer[] ids;

    public VoiceEnvelopeModel(ReadablePreset.ReadableVoice voice, Integer startId) {
        this.voice = voice;
        voice.getPreset().addPresetListener(this);
        ids = new Integer[12];
        for (int i = 0,j = 12; i < j; i++)
            ids[i] = IntPool.get(startId.intValue() + i);
        updateParameters();
    }

    public void refresh() {
        updateParameters();
    }

    protected void updateParameters() {
        try {
            Integer[] vals = voice.getVoiceParams(ids);

            atk1Rate = vals[0].intValue();
            atk1Level = vals[1].intValue();
            atk2Rate = vals[2].intValue();
            atk2Level = vals[3].intValue();
            dec1Rate = vals[4].intValue();
            dec1Level = vals[5].intValue();
            dec2Rate = vals[6].intValue();
            dec2Level = vals[7].intValue();
            rls1Rate = vals[8].intValue();
            rls1Level = vals[9].intValue();
            rls2Rate = vals[10].intValue();
            rls2Level = vals[11].intValue();
            this.fireModelChanged();

        } catch (NoSuchPresetException e) {
            //e.printStackTrace();
        } catch (PresetEmptyException e) {
            //e.printStackTrace();
        } catch (IllegalParameterIdException e) {
            //e.printStackTrace();
        } catch (NoSuchVoiceException e) {
            //e.printStackTrace();
        }
    }

    public void presetInitialized(PresetInitializeEvent ev) {
        updateParameters();
    }

    public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev) {
    }

    public void presetRefreshed(PresetRefreshEvent ev) {
        updateParameters();
    }

    public void presetChanged(PresetChangeEvent ev) {
    }

    public void presetNameChanged(PresetNameChangeEvent ev) {
    }

    public void voiceAdded(VoiceAddEvent ev) {
        updateParameters();
    }

    public void voiceRemoved(VoiceRemoveEvent ev) {
        updateParameters();
    }

    public void voiceChanged(VoiceChangeEvent ev) {
        Integer[] params = ev.getParameters();
        for (int i = 0,j = params.length; i < j; i++)
            if (params[i].intValue() >= ids[0].intValue() && params[i].intValue() < ids[0].intValue() + 12) {
                updateParameters();
                break;
            }
    }

    public void linkAdded(LinkAddEvent ev) {
    }

    public void linkRemoved(LinkRemoveEvent ev) {
    }

    public void linkChanged(LinkChangeEvent ev) {
    }

    public void zoneAdded(ZoneAddEvent ev) {
    }

    public void zoneRemoved(ZoneRemoveEvent ev) {
    }

    public void zoneChanged(ZoneChangeEvent ev) {
    }

    public void zDispose() {
        voice.getPreset().removePresetListener(this);
        voice = null;
    }
}
