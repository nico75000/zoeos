package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextBasicEditablePreset;
import com.pcmsolutions.system.AbstractZCommand;
import com.pcmsolutions.system.ZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextBasicEditablePresetZCommand extends AbstractZCommand implements ZCommand, E4ContextBasicEditablePresetZCommandMarker {
    // protected ContextEditablePreset preset = (ContextEditablePreset)target;

    protected AbstractContextBasicEditablePresetZCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextBasicEditablePreset.class, presString, descString, argPresStrings, argDescStrings);
    }

    ContextBasicEditablePreset getTarget() {
        return (ContextBasicEditablePreset) target;
    }
}
