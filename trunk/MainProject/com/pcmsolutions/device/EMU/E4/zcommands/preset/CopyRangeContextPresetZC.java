package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.gui.sample.PresetContextLocationCombo;
import com.pcmsolutions.device.EMU.E4.preset.ContextReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyRangeContextPresetZC extends AbstractContextReadablePresetZCommand {
    private static final ZCommandField<PresetContextLocationCombo, ContextLocation> lowDestinationField = new AbstractZCommandField<PresetContextLocationCombo, ContextLocation>(new PresetContextLocationCombo(), "Low destination", "Low destination preset") {
        public ContextLocation getValue() {
            return getComponent().getSelectedLocation();
        }
    };
    private static final ZCommandField<PresetContextLocationCombo, ContextLocation> highDestinationField = new AbstractZCommandField<PresetContextLocationCombo, ContextLocation>(new PresetContextLocationCombo(), "High destination", "High destination preset") {
        public ContextLocation getValue() {
            return getComponent().getSelectedLocation();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Copy to preset range", new ZCommandField[]{lowDestinationField, highDestinationField});
    }

    public int getMaxNumTargets() {
        return 1;
    }

    public String getPresentationString() {
        return "Copy to range";
    }

    public String getDescriptiveString() {
        return "Copy preset to all preset locations in specified range";
    }

    public String getMenuPathString() {
        return ";Copy";
    }

    public boolean handleTarget(final ContextReadablePreset p, int total, int curr) throws Exception {
        lowDestinationField.getComponent().init(p.getPresetContext());
        lowDestinationField.getComponent().selectFirstEmptyLocation();
        highDestinationField.getComponent().init(p.getPresetContext());
        highDestinationField.getComponent().selectFirstEmptyLocation();

        return cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                Integer lowPreset = lowDestinationField.getValue().getIndex();
                Integer highPreset = highDestinationField.getValue().getIndex();

                if (lowPreset.intValue() > highPreset.intValue())
                    throw new CommandFailedException("Invalid range");

                int num = highPreset.intValue() - lowPreset.intValue() + 1;
                Integer[] destIndexes = ZUtilities.fillIncrementally(new Integer[num], lowPreset.intValue());
                Integer[] srcIndexes = new Integer[destIndexes.length];
                Arrays.fill(srcIndexes, p.getIndex());
                PresetContextMacros.copyPresets(p.getPresetContext(), srcIndexes, destIndexes, false, true, "Preset copy to range");
            }
        }, total, curr) != ZCommandDialog.CANCELLED_ALL;
    }
}


