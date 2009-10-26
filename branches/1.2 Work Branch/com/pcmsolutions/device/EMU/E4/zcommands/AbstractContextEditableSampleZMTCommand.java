package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextEditableSampleZMTCommand extends AbstractZMTCommand implements E4ContextEditableSampleZCommandMarker {
    protected AbstractContextEditableSampleZMTCommand() {
        super(ContextEditableSample.class);
    }

    protected AbstractContextEditableSampleZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextEditableSample.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ContextEditableSample getTarget() {
        return (ContextEditableSample) target;
    }

    public ContextEditableSample[] getTargets() {
        if (targets == null)
            return new ContextEditableSample[0];

        int num = targets.length;
        ContextEditableSample[] samples = new ContextEditableSample[num];

        for (int n = 0; n < num; n++) {
            samples[n] = (ContextEditableSample) targets[n];
        }
        return samples;
    }
}
