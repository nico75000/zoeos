package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.ZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class EditVoiceZMTC extends AbstractEditableVoiceZMTCommand {
    protected boolean editAsCollection;

    public EditVoiceZMTC() {
        init(false);
    }

    protected void init(boolean editAsCollection) {
        this.editAsCollection = editAsCollection;
    }

    public ZMTCommand getNextMode() {
        if (editAsCollection)
            return null;
        else {
            EditVoiceZMTC o = new EditVoiceZMTC();
            o.init(true);
            return o;
        }
    }
        public String getPresentationCategory() {
        return "Visibility";
    }
    public boolean isSuitableAsButton() {
        return true;
    }

    public boolean isSuitableInToolbar() {
        return true;
    }
    
    public boolean overrides(ZCommand cmd) {
        return cmd.getClass() == ViewVoiceZMTC.class;
    }

    public String getPresentationString() {
        return (editAsCollection ? "OpenC" : "OpenV");
    }

    public String getDescriptiveString() {
        return (editAsCollection ? "Open voice collection for editing" : "Open voice for editing");
    }

    public boolean handleTarget(ContextEditablePreset.EditableVoice editableVoice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice[] voices = getTargets().toArray(new ContextEditablePreset.EditableVoice[numTargets()]);
        boolean tabbed = voices[0].getPreset().getDeviceContext().getDevicePreferences().ZPREF_usePartitionedVoiceEditing.getValue();
        boolean grouped = voices[0].getPreset().getDeviceContext().getDevicePreferences().ZPREF_groupEnvelopesWhenVoiceTabbed.getValue();
        if (editAsCollection) {
            if (tabbed)
                voices[0].getPreset().getDeviceContext().getViewManager().openTabbedVoices(voices, grouped, true).post();
            else
                voices[0].getPreset().getDeviceContext().getViewManager().openVoices(voices, true).post();
        } else
            for (int i = 0; i < voices.length; i++)
                if (tabbed)
                    voices[i].getPreset().getDeviceContext().getViewManager().openTabbedVoice(voices[i], grouped, (i == 0 ? true : false)).post();
                else
                    voices[i].getPreset().getDeviceContext().getViewManager().openVoice(voices[i], (i == 0 ? true : false)).post();
        return false;
    }

    public int getMinNumTargets() {
        if (editAsCollection)
            return 2;
        else
            return 1;
    }
}


