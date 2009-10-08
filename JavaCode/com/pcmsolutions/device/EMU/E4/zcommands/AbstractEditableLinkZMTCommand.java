package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractEditableLinkZMTCommand extends AbstractZMTCommand implements E4EditableLinkZCommandMarker {
    protected AbstractEditableLinkZMTCommand(String presString, String descString, String[] argPresStrings, String[] argDescStrings) {
        super(ContextEditablePreset.EditableLink.class, presString, descString, argPresStrings, argDescStrings);
    }

    public ContextEditablePreset.EditableLink getTarget() {
        return (ContextEditablePreset.EditableLink) target;
    }

    public ContextEditablePreset.EditableLink[] getTargets() {
        if (targets == null)
            return new ContextEditablePreset.EditableLink[0];

        int num = targets.length;
        ContextEditablePreset.EditableLink[] links = new ContextEditablePreset.EditableLink[num];

        for (int n = 0; n < num; n++) {
            links[n] = (ContextEditablePreset.EditableLink) targets[n];
        }
        return links;
    }
}
