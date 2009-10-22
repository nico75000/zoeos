package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.gui.TableExclusiveSelectionContext;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

public class AmplifierPanel extends JPanel implements ZDisposable {
    ReadableParameterModel[] ampModels;

    public AmplifierPanel init(final ReadablePreset.ReadableVoice voice, TableExclusiveSelectionContext tsc) throws ParameterException, DeviceException {
        this.setLayout(new FlowLayout());
        this.setFocusable(false);
        final List ampIds = voice.getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_AMPLIFIER);

        ampModels = new ReadableParameterModel[ampIds.size()];

        try {
            for (int i = 0, j = ampIds.size(); i < j; i++)
                ampModels[i] = voice.getParameterModel((Integer) ampIds.get(i));
        } catch (ParameterException e) {
            ZUtilities.zDisposeCollection(Arrays.asList(ampModels));
            throw e;
        }

        Action r1t = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) ampIds.toArray(new Integer[ampIds.size()]));
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        r1t.putValue("tip", "Refresh Amplifier");
        RowHeaderedAndSectionedTablePanel ampPanel;
        VoiceParameterTable vpt =new VoiceParameterTable(voice, ParameterCategories.VOICE_AMPLIFIER, ampModels, "Amplifier");
        tsc.addTableToContext(vpt);
        ampPanel = new RowHeaderedAndSectionedTablePanel().init(vpt, "Show Amplifier", UIColors.getTableBorder(), r1t);
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
