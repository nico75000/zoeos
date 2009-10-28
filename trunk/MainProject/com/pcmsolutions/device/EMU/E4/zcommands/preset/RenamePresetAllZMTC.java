package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextBasicEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class RenamePresetAllZMTC extends AbstractContextBasicEditablePresetZMTCommand {
    private static final ZCommandField<FixedLengthTextField, String> nameField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", 16), "Name", "New name for all presets") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Rename all presets", new ZCommandField[]{nameField});
    }

    public int getMinNumTargets() {
        return 2;
    }

    public String getMenuPathString() {
        return ";Special naming";
    }

    public boolean handleTarget(ContextBasicEditablePreset p, int total, int curr) throws Exception {
        nameField.getComponent().setText(p.getName());
        nameField.getComponent().selectAll();
        cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                ContextBasicEditablePreset[] presets = getTargets().toArray(new ContextBasicEditablePreset[numTargets()]);
                for (ContextBasicEditablePreset p : presets)
                    try {
                        p.setPresetName(nameField.getValue());
                    } catch (PresetException e) {
                    }
            }
        });
        return false;
    }

    public String getPresentationString() {
        return "Rename all";
    }

    public String getDescriptiveString() {
        return "Apply single name to all presets";
    }
}
