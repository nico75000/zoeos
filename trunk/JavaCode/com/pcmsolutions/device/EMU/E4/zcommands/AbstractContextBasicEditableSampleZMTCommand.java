package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.sample.ContextBasicEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextBasicEditableSampleZMTCommand extends AbstractZMTCommand implements E4ContextBasicEditableSampleZCommandMarker {
    protected AbstractContextBasicEditableSampleZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextBasicEditableSample.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ContextEditableSample getTarget() {
        return (ContextEditableSample) target;
    }

    public ContextBasicEditableSample[] getTargets() {
        if (targets == null)
            return new ContextBasicEditableSample[0];

        int num = targets.length;
        ContextBasicEditableSample[] samples = new ContextBasicEditableSample[num];

        for (int n = 0; n < num; n++) {
            samples[n] = (ContextBasicEditableSample) targets[n];
        }
        return samples;
    }
}
