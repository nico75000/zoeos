package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.gui.sample.PresetContextLocationCombo;
import com.pcmsolutions.device.EMU.E4.preset.ContextReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.callback.Callback;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyContextPresetZC extends AbstractContextReadablePresetZCommand {
    private static final ZCommandField<PresetContextLocationCombo, ContextLocation> destinationField = new AbstractZCommandField<PresetContextLocationCombo, ContextLocation>(new PresetContextLocationCombo(), "Destination", "Destination preset") {
        public ContextLocation getValue() {
            return getComponent().getSelectedLocation();
        }
    };
    private static final ZCommandField<FixedLengthTextField, String> nameField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", 16), "Rename", "Name for destination preset") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Copy preset", new ZCommandField[]{destinationField, nameField});
    }

    public boolean isSuitableAsButton() {
        return true;
    }

    public String getPresentationString() {
        return "Copy";
    }

    public String getDescriptiveString() {
        return "Copy preset";
    }

    public String getMenuPathString() {
        return ";Copy";
    }

    public boolean handleTarget(final ContextReadablePreset p, int total, int curr) throws Exception {
        destinationField.getComponent().init(p.getPresetContext());
        destinationField.getComponent().selectFirstEmptyLocation();
        nameField.getComponent().setText(p.getString());
        nameField.getComponent().selectAll();
        return cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                int ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "OK to overwrite preset " + destinationField.getComponent().getSelectedLocation().toString() + " ?", "Confirm preset copy", JOptionPane.YES_NO_OPTION);
                if (ok == 0) {
                   final  Integer destIndex = destinationField.getComponent().getSelectedLocation().getIndex();
                    //p.copyPreset(destIndex, nameField.getValue());
                    p.getPresetContext().copy(p.getIndex(), destIndex,nameField.getValue() ).post(new Callback(){
                        public void result(Exception e, boolean wasCancelled) {
                            if (p.getDeviceContext().getDevicePreferences().ZPREF_askToOpenAfterPresetCopy.getValue()) {
                                try {
                                    ReadablePreset dp = p.getPresetContext().getContextPreset((destIndex));
                                    if (UserMessaging.askYesNo("Open '" + dp.getDisplayName() + "' now?"))
                                        try {
                                            dp.getDeviceContext().getViewManager().openPreset(dp, true).post();
                                        } catch (ResourceUnavailableException e1) {
                                            e1.printStackTrace();
                                        }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                }

            }
        }, total, curr) != ZCommandDialog.CANCELLED_ALL;
    }
}

