package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextReadablePreset;
import com.pcmsolutions.system.AbstractZCommand;
import com.pcmsolutions.system.ZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:59:51
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextReadablePresetZCommand extends AbstractZCommand implements ZCommand, E4ContextReadablePresetZCommandMarker {
    //protected ContextReadablePreset cp = (ContextReadablePreset)target;

    protected AbstractContextReadablePresetZCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextReadablePreset.class, presString, descString, argPresStrings, argDescStrings);
    }

    ContextReadablePreset getTarget() {
        return (ContextReadablePreset) target;
    }
}
