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

public class AmplifierPanel extends JPanel implements ZDisposable {
    ReadableParameterModel[] ampModels;

    public AmplifierPanel init(final ReadablePreset.ReadableVoice voice) throws ZDeviceNotRunningException, IllegalParameterIdException {
        this.setLayout(new FlowLayout());
        this.setFocusable(false);
        final List ampIds = voice.getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_AMPLIFIER);

        ampModels = new ReadableParameterModel[ampIds.size()];

        try {
            for (int i = 0, j = ampIds.size(); i < j; i++)
                ampModels[i] = voice.getParameterModel((Integer) ampIds.get(i));
        } catch (IllegalParameterIdException e) {
            ZUtilities.zDisposeCollection(Arrays.asList(ampModels));
            throw e;
        }

        Action r1t = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Amplifier") {
                    public void run() {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) ampIds.toArray(new Integer[ampIds.size()]));
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
        r1t.putValue("tip", "Refresh Amplifier");
        RowHeaderedAndSectionedTablePanel ampPanel;
        ampPanel = new RowHeaderedAndSectionedTablePanel().init(new VoiceParameterTable(voice, ParameterCategories.VOICE_AMPLIFIER, ampModels, "Amplifier"), "Show Amplifier", UIColors.getTableBorder(), r1t);
        this.add(ampPanel);
        return this;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public void zDispose() {
        ZUtilities.zDisposeCollection(Arrays.asList(ampModels));
        ampModels = null;
    }
}
