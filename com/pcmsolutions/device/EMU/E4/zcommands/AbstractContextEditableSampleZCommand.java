package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.system.AbstractZCommand;
import com.pcmsolutions.system.ZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextEditableSampleZCommand extends AbstractZCommand implements ZCommand, E4ContextEditableSampleZCommandMarker {
    // protected ContextEditablePreset preset = (ContextEditablePreset)target;

    protected AbstractContextEditableSampleZCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextEditableSample.class, presString, descString, argPresStrings, argDescStrings);
    }

    ContextEditableSample getTarget() {
        return (ContextEditableSample) target;
    }
}
