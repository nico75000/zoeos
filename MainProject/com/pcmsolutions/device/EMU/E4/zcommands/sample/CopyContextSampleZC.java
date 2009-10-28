package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.sample.SampleContextUserLocationCombo;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.ProgressCallbackTree;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;

import javax.swing.*;


public class CopyContextSampleZC extends AbstractContextEditableSampleZCommand {
    private static final ZCommandField<SampleContextUserLocationCombo, ContextLocation> destinationField = new AbstractZCommandField<SampleContextUserLocationCombo, ContextLocation>(new SampleContextUserLocationCombo(), "Destination", "Destination sample") {
        public ContextLocation getValue() {
            return getComponent().getSelectedLocation();
        }
    };
    private static final ZCommandField<FixedLengthTextField, String> nameField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", 16), "Rename", "Name for destination sample") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Copy sample", new ZCommandField[]{destinationField, nameField});
    }

    public String getPresentationString() {
        return "Copy";
    }

    public String getDescriptiveString() {
        return "Copy sample";
    }

    public String getMenuPathString() {
        return ";Copy";
    }

    public boolean handleTarget(final ContextEditableSample s, int total, int curr) throws Exception {
        destinationField.getComponent().init(s.getSampleContext());
        destinationField.getComponent().selectFirstEmptyLocation();
        nameField.getComponent().setText(s.getString());
        return cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                ContextLocation loc = destinationField.getValue();
                String confirmStr = SampleContextMacros.getOverwriteConfirmationString(s.getSampleContext(), loc.getIndex(), 1);

                if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), confirmStr, "Confirm sample copy", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    s.copySample(new Integer[]{loc.getIndex()}, new ProgressCallbackTree("Sample copy", false) {
                        public String finalizeString(String str) {
                            return s.getDeviceContext().makeDeviceProgressTitle(str);
                        }
                    });
                    s.getSampleContext().setName(loc.getIndex(), nameField.getValue()).post();
                }
            }
        }, total, curr) != ZCommandDialog.CANCELLED_ALL;
    }
}
