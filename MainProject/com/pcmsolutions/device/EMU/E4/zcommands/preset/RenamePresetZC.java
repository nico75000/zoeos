package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextBasicEditablePreset;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;

import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class RenamePresetZC extends AbstractContextBasicEditablePresetZCommand {

    private static final ZCommandField<FixedLengthTextField, String> nameField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", 16), "Name", "New name") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog() {
        public void setVisible(boolean b) {
            super.setVisible(b);
        }
    };

    static {
        cmdDlg.init("Rename preset", new ZCommandField[]{nameField});
    }

    public int getMnemonic() {
        return KeyEvent.VK_N;
    }

    public String getPresentationString() {
        return "Rename";
    }

    public String getDescriptiveString() {
        return "Rename preset";
    }

    public boolean handleTarget(final ContextBasicEditablePreset p, int total, int curr) throws Exception {
        nameField.getComponent().setText(p.getName());
        nameField.getComponent().selectAll();
        return cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                p.setPresetName(nameField.getValue());
            }
        }, total, curr) != ZCommandDialog.CANCELLED_ALL;
    }
}
