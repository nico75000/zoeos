package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;
import com.pcmsolutions.system.ZCommandTargetsNotSuitableException;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 26-Jul-2003
 * Time: 21:01:43
 * To change this template use Options | File Templates.
 */
public class EditVoiceAsGroupZMTC extends AbstractEditableVoiceZMTCommand {

      public String getPresentationCategory() {
        return "Visibility";
    }
    public String getPresentationString() {
        return "OpenG";
    }

    public String getDescriptiveString() {
        return "Open group for editing";
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        // should only be 1 voice in array!!
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        ContextEditablePreset.EditableVoice gv = voices[0].duplicate();
        gv.setGroupMode(true);
        try {
            if (gv.getPreset().getDeviceContext().getDevicePreferences().ZPREF_usePartitionedVoiceEditing.getValue()) {
                gv.getPreset().getDeviceContext().getViewManager().openTabbedVoice(gv, gv.getPreset().getDeviceContext().getDevicePreferences().ZPREF_groupEnvelopesWhenVoiceTabbed.getValue(), true).post();
            } else
                gv.getPreset().getDeviceContext().getViewManager().openVoice(gv, true).post();
        } catch (ResourceUnavailableException e) {
            throw e;
        }
        return false;
    }

    protected void acceptTargets() throws ZCommandTargetsNotSuitableException, ZCommandTargetsNotSpecifiedException {
        super.acceptTargets();
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        int grp = -1;
        try {
            for (int i = 0; i < voices.length; i++) {
                int t_grp = voices[i].getVoiceParams(new Integer[]{IntPool.get(37)})[0].intValue();
                if (grp == -1)
                    grp = t_grp;
                else if (grp != t_grp)
                    throw new ZCommandTargetsNotSuitableException();
            }
            return;
        } catch (Exception e) {
            throw new ZCommandTargetsNotSuitableException();
        }
    }
}
