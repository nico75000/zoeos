package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;

import java.awt.event.KeyEvent;

public class TakeDeviceWorkspaceSnapshotZC extends AbstractDeviceContextZCommand {
    private static final String UNTITLED_SNAPSHOT = "Untitled";

    private static final ZCommandField<FixedLengthTextField, String> nameField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", 24), "Name", "Name for this snapshot") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();
    static {
        cmdDlg.init("Take snapshot", new ZCommandField[]{nameField});
    }

    public int getMnemonic() {
        return KeyEvent.VK_S;
    }

    public int getMaxNumTargets() {
        return 1;
    }

    public String getPresentationString() {
        return "Take a workspace snapshot";
    }

    public String getDescriptiveString() {
        return "Take a workspace snapshot";
    }

    public boolean handleTarget(final DeviceContext deviceContext, final int total, final int curr) throws Exception {
        nameField.getComponent().setText(UNTITLED_SNAPSHOT);
        nameField.getComponent().selectAll();
        return cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                deviceContext.getViewManager().takeSnapshot(nameField.getValue()).post();
            }
        }, total, curr) != ZCommandDialog.CANCELLED_ALL;
    }
}
