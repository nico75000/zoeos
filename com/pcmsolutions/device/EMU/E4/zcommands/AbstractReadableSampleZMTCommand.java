package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractReadableSampleZMTCommand extends AbstractZMTCommand implements E4ReadableSampleZCommandMarker {

    protected AbstractReadableSampleZMTCommand() {
        super(ReadableSample.class);
    }
    protected AbstractReadableSampleZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ReadableSample.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ReadableSample getTarget() {
        return (ReadableSample) target;
    }

    public ReadableSample[] getTargets() {
        if (targets == null)
            return new ReadableSample[0];

        int num = targets.length;
        ReadableSample[] samples = new ReadableSample[num];

        for (int n = 0; n < num; n++) {
            samples[n] = (ReadableSample) targets[n];
        }
        return samples;
    }
}
