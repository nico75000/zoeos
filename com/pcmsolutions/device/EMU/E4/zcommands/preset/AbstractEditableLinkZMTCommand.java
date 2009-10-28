package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.zcommands.E4EditableLinkZCommandMarker;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractEditableLinkZMTCommand extends AbstractZMTCommand<ContextEditablePreset.EditableLink> implements E4EditableLinkZCommandMarker {
    public String getPresentationCategory() {
        return "Link";
    }
}
