package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchVoiceException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.ZDeviceNotRunningException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 20-Aug-2003
 * Time: 03:25:12
 * To change this template use Options | File Templates.
 */
public class ZoneParameterSelectionXXX extends VoiceParameterSelection {
    public ZoneParameterSelectionXXX(ReadablePreset.ReadableVoice voice, Integer[] ids) throws ZDeviceNotRunningException, IllegalParameterIdException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException {
        super(voice, ids);
        checkIds();
    }

    protected void checkIds() {
        int iv;
        for (int i = 0,j = ids.length; i < j; i++) {
            iv = ids[i].intValue();
            if (!((iv >= 38 && iv <= 40) || iv == 42 || (iv >= 44 && iv <= 52)))
                throw new IllegalArgumentException("ZoneSelection cannot take non-zone related ids");
        }
    }

    public ZoneParameterSelectionXXX(ReadablePreset.ReadableVoice voice, Integer[] ids, int category) throws ZDeviceNotRunningException, IllegalParameterIdException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException {
        super(voice, ids, category);
    }

    public ZoneParameterSelectionXXX(VoiceParameterSelection vps) {
        super(vps);
    }
}
