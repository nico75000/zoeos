package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.gui.NoteCombo;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.NoteUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class SplitVoiceZMTC extends AbstractEditableVoiceZMTCommand {
    private static final ZCommandField<NoteCombo, NoteUtilities.Note> noteField = new AbstractZCommandField<NoteCombo, NoteUtilities.Note>(new NoteCombo(), "Note", "Note to split on") {
        public NoteUtilities.Note getValue() {
            return getComponent().getSelectedNote();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Split voice on key", new ZCommandField[]{noteField});
    }

    private static final Integer[] klh = new Integer[]{IntPool.get(45), IntPool.get(47)};

    public int getMaxNumTargets() {
        return 1;
    }

    public String getPresentationString() {
        return "Split";
    }

    public String getDescriptiveString() {
        return "Split voice on key";
    }

    public boolean handleTarget(final ContextEditablePreset.EditableVoice voice, int total, int curr) throws Exception {
        ContextEditablePreset.EditableVoice v = getTargets().get(0);
        Integer[] vals = v.getVoiceParams(klh);
        int low = vals[0].intValue();
        int high = vals[1].intValue();
        if (low == high)
            throw new IllegalArgumentException("Can't split voice");
        noteField.getComponent().init(low, high);
        cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                voice.splitVoice(noteField.getValue().getNoteValue());
            }
        });
        return false;
    }

    public int getMinNumTargets() {
        return 1;
    }
}

