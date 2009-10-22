package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.gui.sample.PresetContextLocationCombo;
import com.pcmsolutions.device.EMU.E4.preset.ContextReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;
import com.pcmsolutions.system.preferences.ZBoolPref;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyDeepContextPresetZMTC extends AbstractContextReadablePresetZMTCommand {
    private static final Preferences prefs = Preferences.userNodeForPackage(CopyDeepContextPresetZMTC.class.getClass());
    private static final ZBoolPref PREF_copyEmpties = new Impl_ZBoolPref(prefs, "CopyDeep_copyEmpties", true);
    private static final ZBoolPref PREF_searchForEmptiesAtDest = new Impl_ZBoolPref(prefs, "CopyDeep_searchForEmptiesAtDest", true);

    private static final ZCommandField<PresetContextLocationCombo, ContextLocation> destinationField = new AbstractZCommandField<PresetContextLocationCombo, ContextLocation>(new PresetContextLocationCombo(), "Destination", "Destination preset") {
        public ContextLocation getValue() {
            return getComponent().getSelectedLocation();
        }
    };
    private static final ZCommandField<JCheckBox, Boolean> checkField1 = new AbstractZCommandField<JCheckBox, Boolean>(new JCheckBox() {
        {
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PREF_copyEmpties.putValue(isSelected());
                }
            });
        }
    }, "Copy empties at source") {
        public Boolean getValue() {
            return Boolean.valueOf(getComponent().isSelected());
        }
    };

    private static final ZCommandField<JCheckBox, Boolean> checkField2 = new AbstractZCommandField<JCheckBox, Boolean>(new JCheckBox() {
        {
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //arg1.setEnabled(!arg2.isSelected());
                    PREF_searchForEmptiesAtDest.putValue(isSelected());
                }
            });
        }
    }, "Search for (and only use) empties at destination") {
        public Boolean getValue() {
            return Boolean.valueOf(getComponent().isSelected());
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Copy deep", new ZCommandField[]{destinationField, checkField1, checkField2});
    }

    public String getPresentationString() {
        return "Copy deep";
    }

    public String getDescriptiveString() {
        return "Copy preset and linked presets (recursively)";
    }

    public String getMenuPathString() {
        return ";Copy";
    }

    public boolean handleTarget(ContextReadablePreset p, int total, int curr) throws Exception {
        destinationField.getComponent().init(p.getPresetContext());
        destinationField.getComponent().selectFirstEmptyLocation();
        checkField1.getComponent().setSelected(PREF_copyEmpties.getValue());
        checkField2.getComponent().setSelected(PREF_searchForEmptiesAtDest.getValue());
        cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                ContextReadablePreset[] presets = getTargets().toArray(new ContextReadablePreset[numTargets()]);
                Integer[] indexes = ZUtilities.extractIndexes(presets);
                PresetContextMacros.copyPresetDeep(indexes, presets[0].getPresetContext(), destinationField.getValue().getIndex(), PREF_copyEmpties.getValue(), PREF_searchForEmptiesAtDest.getValue(), true, true);
            }
        });
        return false;
    }
}


