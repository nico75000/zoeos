package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.gui.sample.PresetContextLocationCombo;
import com.pcmsolutions.device.EMU.E4.preset.ContextReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.ZUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyBlockContextPresetsZMTC extends AbstractContextReadablePresetZMTCommand {
    private static final ZCommandField<PresetContextLocationCombo, ContextLocation> destinationField = new AbstractZCommandField<PresetContextLocationCombo, ContextLocation>(new PresetContextLocationCombo(), "Destination", "Destination preset") {
        public ContextLocation getValue() {
            return getComponent().getSelectedLocation();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Copy presets as a block", new ZCommandField[]{destinationField});
    }

    public String getPresentationString() {
        return "Copy as block";
    }

    public String getDescriptiveString() {
        return "Copy presets as a block";
    }

    public int getMinNumTargets() {
        return 2;
    }

    public String getMenuPathString() {
        return ";Copy";
    }

    public boolean handleTarget(ContextReadablePreset p, int total, int curr) throws Exception {
        destinationField.getComponent().init(p.getPresetContext());
        destinationField.getComponent().selectFirstEmptyLocation();
        final ContextReadablePreset[] presets = getTargets().toArray(new ContextReadablePreset[numTargets()]);
        cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                Integer destIndex = destinationField.getComponent().getSelectedLocation().getIndex();
                Integer[] srcIndexes = ZUtilities.extractIndexes(presets);
                Integer[] destIndexes = ZUtilities.fillIncrementally(new Integer[presets.length], destIndex.intValue());
                PresetContextMacros.copyPresets(presets[0].getPresetContext(), srcIndexes, destIndexes, false, true, "Preset block copy");
            }
        });
        return false;
    }
}

