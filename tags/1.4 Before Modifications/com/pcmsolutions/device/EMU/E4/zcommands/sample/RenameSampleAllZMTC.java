package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.E4.sample.ContextBasicEditableSample;
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
public class RenameSampleAllZMTC extends AbstractContextBasicEditableSampleZMTCommand {
    private static final ZCommandField<FixedLengthTextField, String> nameField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", 16), "Name", "New name for all samples") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Rename all samples", new ZCommandField[]{nameField});
    }

    public int getMinNumTargets() {
        return 2;
    }

    public String getMenuPathString() {
        return ";Special Naming";
    }

    public boolean handleTarget(ContextBasicEditableSample s, int total, int curr) throws Exception {
        nameField.getComponent().setText(s.getName());
        nameField.getComponent().selectAll();
        cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                ContextBasicEditableSample[] samples = getTargets().toArray(new ContextBasicEditableSample[numTargets()]);
                for (ContextBasicEditableSample s: samples)
                    s.setSampleName(nameField.getValue());
            }
        });
        return false;
    }

    public String getPresentationString() {
        return "Rename all";
    }

    public String getDescriptiveString() {
        return "Apply single name to all samples";
    }
}
