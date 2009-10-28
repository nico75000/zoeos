package com.pcmsolutions.device.EMU.E4.zcommands.sample;


import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;

import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class RefreshSampleZMTC extends AbstractReadableSampleZMTCommand {
    public String getPresentationString() {
        return "Refresh";
    }

    public String getDescriptiveString() {
        return "Refresh samples from remote";
    }

    public boolean handleTarget(ReadableSample readableSample, int total, int curr) throws Exception {
        ReadableSample[] samples = getTargets().toArray(new ReadableSample[numTargets()]);
        int num = samples.length;
        HashSet done = new HashSet();
        for (int n = 0; n < num; n++) {
            if (!done.contains(samples[n])) {
                samples[n].refresh();
                done.add(samples[n]);
            }
        }
        return false;
    }
}

