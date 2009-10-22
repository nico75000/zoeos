package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class OffsetPresetLinksZMTC extends AbstractContextEditablePresetZMTCommand {
    private final boolean userMode;

    private static final AbstractZCommandField<FixedLengthTextField, String> inputField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("Offset links", 5), "Offset", "") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("", new ZCommandField[]{inputField});
    }

    public OffsetPresetLinksZMTC() {
        this(true);
    }

    private OffsetPresetLinksZMTC(boolean userMode) {
        this.userMode = userMode;
    }

    public ZMTCommand getNextMode() {
        if (userMode == true)
            return new OffsetPresetLinksZMTC(false);
        return null;
    }

    public String getPresentationString() {
        return (userMode ? "Offset user links" : "Offset flash links");
    }

    public String getDescriptiveString() {
        return (userMode ? "Offset linked user preset indexes" : "Offset linked flash preset indexes");
    }

    public String getMenuPathString() {
        return ";Utility";
    }

    public boolean handleTarget(ContextEditablePreset p, int total, int curr) throws Exception {
        inputField.getComponent().setToolTipText((userMode ? "Offset to link user indexes" : "Offset to link flash indexes"));
        cmdDlg.setTitle(getPresentationString());
        cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                for (Iterator<ContextEditablePreset> i = getTargets().iterator(); i.hasNext();)
                    i.next().offsetLinkIndexes(IntPool.get(Integer.parseInt(inputField.getValue())), userMode);
            }
        });
        return false;
    }
}
