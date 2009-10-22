package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

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
public class TuningPanel extends JPanel implements ZDisposable {
    ReadableParameterModel[] tuningModels;
    ReadableParameterModel[] modifierModels;
    ReadableParameterModel[] setupModels;

    public TuningPanel init(final ReadablePreset.ReadableVoice voice, TableExclusiveSelectionContext tsc) throws ParameterException, DeviceException {
        // this.setLayout(new TopAligningFlowLayout(FlowLayout.RIGHT));
        final List tuningIds = voice.getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_TUNING);
        final List modifierIds = voice.getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_TUNING_MODIFIERS);
        final List setupIds = voice.getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_TUNING_SETUP);

        tuningModels = new ReadableParameterModel[tuningIds.size()];
        modifierModels = new ReadableParameterModel[modifierIds.size()];
        setupModels = new ReadableParameterModel[setupIds.size()];
        this.setFocusable(false);
        try {
            for (int i = 0, j = tuningIds.size(); i < j; i++)
                tuningModels[i] = voice.getParameterModel((Integer) tuningIds.get(i));
            for (int i = 0, j = modifierIds.size(); i < j; i++)
                modifierModels[i] = voice.getParameterModel((Integer) modifierIds.get(i));
            for (int i = 0, j = setupIds.size(); i < j; i++)
                setupModels[i] = voice.getParameterModel((Integer) setupIds.get(i));
        } catch (ParameterException e) {
            ZUtilities.zDisposeCollection(Arrays.asList(tuningModels));
            ZUtilities.zDisposeCollection(Arrays.asList(modifierModels));
            ZUtilities.zDisposeCollection(Arrays.asList(setupModels));
            throw e;
        }

        Action rt = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) tuningIds.toArray(new Integer[tuningIds.size()]));
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        rt.putValue("tip", "Refresh Tuning");
        Action rm = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) modifierIds.toArray(new Integer[modifierIds.size()]));
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        rm.putValue("tip", "Refresh Tuning Modifier");
        Action rs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) setupIds.toArray(new Integer[setupIds.size()]));
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        rs.putValue("tip", "Refresh Tuning Setup");
        RowHeaderedAndSectionedTablePanel tuningPanel;
        RowHeaderedAndSectionedTablePanel modifierPanel;
        RowHeaderedAndSectionedTablePanel setupPanel;
        VoiceParameterTable vpt;

        vpt = new VoiceParameterTable(voice, ParameterCategories.VOICE_TUNING, tuningModels, "Tuning");
        if (tsc != null)
            tsc.addTableToContext(vpt);
        tuningPanel = new RowHeaderedAndSectionedTablePanel().init(vpt, "Show Tuning", UIColors.getTableBorder(), rt);

        vpt = new VoiceParameterTable(voice, ParameterCategories.VOICE_TUNING_MODIFIERS, modifierModels, "Tuning Modifiers");
        if (tsc != null)
            tsc.addTableToContext(vpt);
        modifierPanel = new RowHeaderedAndSectionedTablePanel().init(vpt, "Show Tuning Modifiers", UIColors.getTableBorder(), rm);

        vpt = new VoiceParameterTable(voice, ParameterCategories.VOICE_TUNING_SETUP, setupModels, "Tuning Setup");
        if (tsc != null)
            tsc.addTableToContext(vpt);
        setupPanel = new RowHeaderedAndSectionedTablePanel().init(vpt, "Show Tuning Setup", UIColors.getTableBorder(), rs);

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

    public void zDispose() {
        ZUtilities.zDisposeCollection(Arrays.asList(tuningModels));
        ZUtilities.zDisposeCollection(Arrays.asList(modifierModels));
        ZUtilities.zDisposeCollection(Arrays.asList(setupModels));
    }
}
