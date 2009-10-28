package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
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
public class RenameDeviceZC extends AbstractDeviceContextZCommand {
    private static final ZCommandField<FixedLengthTextField, String> nameField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", 16), "Name", "New for this device") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Rename device", new ZCommandField[]{nameField});
    }

    public int getMnemonic() {
        return KeyEvent.VK_N;
    }

    public String getPresentationString() {
        return "Rename device";
    }

    public String getDescriptiveString() {
        return "Give this device a new name";
    }

    public boolean handleTarget(final DeviceContext device, int total, int curr) throws Exception {
        nameField.getComponent().setText(device.getName());
        nameField.getComponent().selectAll();
        return cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                device.setName(nameField.getValue());
            }
        }, total, curr) != ZCommandDialog.CANCELLED_ALL;
    }
}
