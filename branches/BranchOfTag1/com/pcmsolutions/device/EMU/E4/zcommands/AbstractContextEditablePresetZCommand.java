package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.AbstractZCommand;
import com.pcmsolutions.system.ZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextEditablePresetZCommand extends AbstractZCommand implements ZCommand, E4ContextEditablePresetZCommandMarker {
    // protected ContextEditablePreset preset = (ContextEditablePreset)target;

    protected AbstractContextEditablePresetZCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextEditablePreset.class, presString, descString, argPresStrings, argDescStrings);
    }

    ContextEditablePreset getTarget() {
        return (ContextEditablePreset) target;
    }
}
