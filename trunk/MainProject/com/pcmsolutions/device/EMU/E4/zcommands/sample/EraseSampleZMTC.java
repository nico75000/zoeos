package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.sample.ContextBasicEditableSample;
import com.pcmsolutions.gui.UserMessaging;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class EraseSampleZMTC extends AbstractContextBasicEditableSampleZMTCommand {

    public String getPresentationString() {
        return "Erase";
    }

    public String getDescriptiveString() {
        return "Erase sample";
    }

    public boolean handleTarget(ContextBasicEditableSample s, int total, int curr) throws Exception {
        ContextBasicEditableSample[] samples = getTargets().toArray(new ContextBasicEditableSample[numTargets()]);
        int num = samples.length;
        int nonempty = SampleContextMacros.getNonEmpty(samples).length;
        if (UserMessaging.askYesNo("Erase " + samples.length + " sample location" + (samples.length == 1 ? "" : "s (" + nonempty + " non-empty)")))
            for (int n = 0; n < num; n++)
                samples[n].eraseSample();
        return false;
    }
}
