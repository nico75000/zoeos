package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.TableExclusiveSelectionContext;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Jul-2003
 * Time: 21:45:43
 * To change this template use Options | File Templates.
 */
public class EditableTuningPanel extends JPanel implements ZDisposable {
    protected EditableParameterModel[] tuningModels;
    protected EditableParameterModel[] setupModels;
    protected EditableParameterModel[] modifierModels;

    public EditableTuningPanel init(final ContextEditablePreset.EditableVoice[] voices, TableExclusiveSelectionContext tsc) throws ParameterException, DeviceException {
        if (voices == null || voices.length < 1)
            throw new IllegalArgumentException("Need at least one voice for am EditableTuningPanel");

        //this.setLayout(new TopAligningFlowLayout(FlowLayout.RIGHT));
        final List tuningIds = voices[0].getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_TUNING);
        final List modifierIds = voices[0].getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_TUNING_MODIFIERS);
        final List setupIds = voices[0].getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_TUNING_SETUP);

        tuningModels = ParameterModelUtilities.getEditableParameterModelGroups(voices, (Integer[]) tuningIds.toArray(new Integer[tuningIds.size()]));
        modifierModels = ParameterModelUtilities.getEditableParameterModelGroups(voices, (Integer[]) modifierIds.toArray(new Integer[modifierIds.size()]));
        setupModels = ParameterModelUtilities.getEditableParameterModelGroups(voices, (Integer[]) setupIds.toArray(new Integer[setupIds.size()]));
        this.setFocusable(false);
        Action rt = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) tuningIds.toArray(new Integer[tuningIds.size()]));
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }

            }
        };
        rt.putValue("tip", "Refresh Tuning");
        Action rm = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) modifierIds.toArray(new Integer[modifierIds.size()]));
                        }catch (PresetException e1) {
                            e1.printStackTrace();
                        }

            }
        };
        rm.putValue("tip", "Refresh Tuning Modifiers");

        Action rs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) setupIds.toArray(new Integer[setupIds.size()]));
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        rs.putValue("tip", "Refresh Tuning Setup");

        RowHeaderedAndSectionedTablePanel tuningPanel;
        RowHeaderedAndSectionedTablePanel modifierPanel;
        RowHeaderedAndSectionedTablePanel setupPanel;
        EditableVoiceParameterTable evpt;

        evpt =new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_TUNING, tuningModels, "Tuning");
        tsc.addTableToContext(evpt);
        tuningPanel = new RowHeaderedAndSectionedTablePanel().init(evpt, "Show Tuning", UIColors.getTableBorder(), rt);

        evpt =new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_TUNING_MODIFIERS, modifierModels, "Tuning Modifiers");
        tsc.addTableToContext(evpt);
        modifierPanel = new RowHeaderedAndSectionedTablePanel().init(evpt, "Show Tuning Modifiers", UIColors.getTableBorder(), rm);

        evpt =new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_TUNING_SETUP, setupModels, "Tuning Setup");
        tsc.addTableToContext(evpt);
        setupPanel = new RowHeaderedAndSectionedTablePanel().init(evpt, "Show Tuning Setup", UIColors.getTableBorder(), rs);
        
        tuningPanel.setAlignmentX(Component.TOP_ALIGNMENT);
        modifierPanel.setAlignmentX(Component.TOP_ALIGNMENT);
        setupPanel.setAlignmentX(Component.TOP_ALIGNMENT);
        this.add(tuningPanel);
        this.add(modifierPanel);
        this.add(setupPanel);
        return this;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public void zDispose() {
        ZUtilities.zDisposeCollection(Arrays.asList(tuningModels));
        ZUtilities.zDisposeCollection(Arrays.asList(modifierModels));
        ZUtilities.zDisposeCollection(Arrays.asList(setupModels));
    }
}
