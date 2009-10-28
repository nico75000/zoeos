package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.TableExclusiveSelectionContext;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
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
public class EditableAmplifierPanel extends JPanel implements ZDisposable {
    EditableParameterModel[] ampModels;

    public EditableAmplifierPanel init(final ContextEditablePreset.EditableVoice[] voices, TableExclusiveSelectionContext tsc) throws ParameterException, DeviceException {
        if (voices == null || voices.length < 1)
            throw new IllegalArgumentException("Need at least one voice for am EditableAmplifierPanel");

        //this.setLayout(new FlowLayout(FlowLayout.LEADING));
        this.setFocusable(false);
        final List ampIds = voices[0].getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_AMPLIFIER);

        ampModels = ParameterModelUtilities.getEditableParameterModelGroups(voices, (Integer[]) ampIds.toArray(new Integer[ampIds.size()]));

        Action r1t = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                try {
                    voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) ampIds.toArray(new Integer[ampIds.size()]));
                } catch (PresetException e1) {
                    e1.printStackTrace();
                }
            }
        };
        r1t.putValue("tip", "Refresh Amplifier");
        RowHeaderedAndSectionedTablePanel ampPanel;
        EditableVoiceParameterTable evpt = new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_AMPLIFIER, ampModels, "Amplifier");
        tsc.addTableToContext(evpt);
        ampPanel = new RowHeaderedAndSectionedTablePanel().init(evpt, "Show Amplifier", UIColors.getTableBorder(), r1t);
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
