package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
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
public class EditableTuningPanel extends JPanel implements ZDisposable {
    protected EditableParameterModel[] tuningModels;
    protected EditableParameterModel[] setupModels;
    protected EditableParameterModel[] modifierModels;

    public EditableTuningPanel init(final ContextEditablePreset.EditableVoice[] voices) throws ZDeviceNotRunningException, IllegalParameterIdException {
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
                new ZDBModifyThread("Refresh Tuning") {
                    public void run() {
                        try {
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) tuningIds.toArray(new Integer[tuningIds.size()]));
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
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) modifierIds.toArray(new Integer[modifierIds.size()]));
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
        rm.putValue("tip", "Refresh Tuning Modifiers");

        Action rs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Tuning Setup") {
                    public void run() {
                        try {
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) setupIds.toArray(new Integer[setupIds.size()]));
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
        tuningPanel = new RowHeaderedAndSectionedTablePanel().init(new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_TUNING, tuningModels, "Tuning"), "Show Tuning", UIColors.getTableBorder(), rt);
        modifierPanel = new RowHeaderedAndSectionedTablePanel().init(new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_TUNING_MODIFIERS, modifierModels, "Tuning Modifiers"), "Show Tuning Modifiers", UIColors.getTableBorder(), rm);
        setupPanel = new RowHeaderedAndSectionedTablePanel().init(new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_TUNING_SETUP, setupModels, "Tuning Setup"), "Show Tuning Setup", UIColors.getTableBorder(), rs);
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
