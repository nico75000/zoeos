package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.sample.ContextBasicEditableSample;
import com.pcmsolutions.system.AbstractZCommand;
import com.pcmsolutions.system.ZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextBasicEditableSampleZCommand extends AbstractZCommand implements ZCommand, E4ContextBasicEditableSampleZCommandMarker {
    // protected ContextEditableSample sample = (ContextEditableSample)target;

    protected AbstractContextBasicEditableSampleZCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextBasicEditableSample.class, presString, descString, argPresStrings, argDescStrings);
    }

    ContextBasicEditableSample getTarget() {
        return (ContextBasicEditableSample) target;
    }
}
