package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;
import com.pcmsolutions.system.ZCommandTargetsNotSuitableException;
import com.pcmsolutions.system.ZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class SendPresetToMultiModeZMTC extends AbstractReadablePresetZMTCommand {
    private int ch;
    private int maxCh = 32;
    private MultiModeContext mmc;

    public SendPresetToMultiModeZMTC() {
        this(1);
    }

    private SendPresetToMultiModeZMTC(int ch) {
        this.ch = ch;
    }

    public ZMTCommand getNextMode() {
        if (ch >= maxCh)
            return null;

        return new SendPresetToMultiModeZMTC(ch + 1);
    }

    public void acceptTargets() throws IllegalArgumentException, ZCommandTargetsNotSuitableException, ZCommandTargetsNotSpecifiedException {
        super.acceptTargets();
        ReadablePreset[] targets = getTargets().toArray(new ReadablePreset[numTargets()]);
        try {
            mmc = targets[0].getDeviceContext().getMultiModeContext();
            if (mmc.has32Channels())
                maxCh = 32;
            else
                maxCh = 16;
            if (ch > maxCh)
                throw new ZCommandTargetsNotSuitableException();
        } catch (DeviceException e) {
            throw new ZCommandTargetsNotSuitableException();
        }
    }

    public String getPresentationString() {
        return "Ch " + String.valueOf(ch);
    }

    public String getDescriptiveString() {
        return "Send preset to multimode " + "Ch " + String.valueOf(ch);
    }

    public String getMenuPathString() {
        return ";Send to multimode";
    }

    public boolean handleTarget(ReadablePreset readablePreset, int total, int curr) throws Exception {
        ReadablePreset[] presets = getTargets().toArray(new ReadablePreset[numTargets()]);
        int num = presets.length;
        for (int n = 0; n < num; n++) {
            int cch = ch + n;
            if (cch <= maxCh)
                presets[n].sendToMultiMode(IntPool.get(cch));
        }
        return false;
    }
}

