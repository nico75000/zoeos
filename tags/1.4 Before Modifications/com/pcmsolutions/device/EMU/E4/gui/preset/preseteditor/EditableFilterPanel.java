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
public class EditableFilterPanel extends JPanel implements ZDisposable {
    EditableParameterModel[] filterModels;

    public EditableFilterPanel init(final ContextEditablePreset.EditableVoice[] voices, TableExclusiveSelectionContext tsc) throws ParameterException, DeviceException {
        if (voices == null || voices.length < 1)
            throw new IllegalArgumentException("Need at least one voice for am EditableFilterPanel");

        //this.setLayout(new FlowLayout(FlowLayout.LEADING));
        final List filterIds = voices[0].getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_FILTER);

        filterModels = ParameterModelUtilities.getEditableParameterModelGroups(voices, (Integer[]) filterIds.toArray(new Integer[filterIds.size()]));

        Action ra = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) filterIds.toArray(new Integer[filterIds.size()]));
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        ra.putValue("tip", "Refresh Filter");
        RowHeaderedAndSectionedTablePanel ampPanel;
        EditableFilterParameterTableModel model = new EditableFilterParameterTableModel(filterModels);
        EditableVoiceParameterTable evpt =new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_FILTER, model, "Filter");
        tsc.addTableToContext(evpt);
        ampPanel = new RowHeaderedAndSectionedTablePanel().init(evpt, "Show Filter", UIColors.getTableBorder(), ra);
        this.add(ampPanel);
        return this;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public void zDispose() {
        ZUtilities.zDisposeCollection(Arrays.asList(filterModels));
    }
}
