package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.threads.ZDBModifyThread;

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

    public TuningPanel init(final ReadablePreset.ReadableVoice voice) throws ZDeviceNotRunningException, IllegalParameterIdException {
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
        } catch (IllegalParameterIdException e) {
            ZUtilities.zDisposeCollection(Arrays.asList(tuningModels));
            ZUtilities.zDisposeCollection(Arrays.asList(modifierModels));
            ZUtilities.zDisposeCollection(Arrays.asList(setupModels));
            throw e;
        }

        Action rt = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Tuning") {
                    public void run() {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) tuningIds.toArray(new Integer[tuningIds.size()]));
                        } catch (NoSuchContextException e1) {
                            e1.printStackTrace();
                        } catch (PresetEmptyException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchPresetException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchVoiceException e1) {
                            e1.printStackTrace();
                        } catch (ParameterValueOutOfRangeException e1) {
                            e1.printStackTrace();
                        } catch (IllegalParameterIdException e1) {
                            e1.printStackTrace();
                        }

                    }
                }.start();
            }
        };
        rt.putValue("tip", "Refresh Tuning");
        Action rm = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Tuning Modifiers") {
                    public void run() {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) modifierIds.toArray(new Integer[modifierIds.size()]));
                        } catch (NoSuchContextException e1) {
                            e1.printStackTrace();
                        } catch (PresetEmptyException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchPresetException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchVoiceException e1) {
                            e1.printStackTrace();
                        } catch (ParameterValueOutOfRangeException e1) {
                            e1.printStackTrace();
                        } catch (IllegalParameterIdException e1) {
                            e1.printStackTrace();
                        }
                    }
                }.start();
            }
        };
        rm.putValue("tip", "Refresh Tuning Modifier");
        Action rs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("REfresh Tuning Setup") {
                    public void run() {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) setupIds.toArray(new Integer[setupIds.size()]));
                        } catch (NoSuchContextException e1) {
                            e1.printStackTrace();
                        } catch (PresetEmptyException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchPresetException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchVoiceException e1) {
                            e1.printStackTrace();
                        } catch (ParameterValueOutOfRangeException e1) {
                            e1.printStackTrace();
                        } catch (IllegalParameterIdException e1) {
                            e1.printStackTrace();
                        }
                    }
                }.start();
            }
        };
        rs.putValue("tip", "Refresh Tuning Setup");
        RowHeaderedAndSectionedTablePanel tuningPanel;
        RowHeaderedAndSectionedTablePanel modifierPanel;
        RowHeaderedAndSectionedTablePanel setupPanel;
        tuningPanel = new RowHeaderedAndSectionedTablePanel().init(new VoiceParameterTable(voice, ParameterCategories.VOICE_TUNING, tuningModels, "Tuning"), "Show Tuning", UIColors.getTableBorder(), rt);
        modifierPanel = new RowHeaderedAndSectionedTablePanel().init(new VoiceParameterTable(voice, ParameterCategories.VOICE_TUNING_MODIFIERS, modifierModels, "Tuning Modifiers"), "Show Tuning Modifiers", UIColors.getTableBorder(), rm);
        setupPanel = new RowHeaderedAndSectionedTablePanel().init(new VoiceParameterTable(voice, ParameterCategories.VOICE_TUNING_SETUP, setupModels, "Tuning Setup"), "Show Tuning Setup", UIColors.getTableBorder(), rs);
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
