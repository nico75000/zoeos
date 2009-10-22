package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.gui.TableExclusiveSelectionContext;
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

public class FilterPanel extends JPanel implements ZDisposable {
    ReadableParameterModel[] filterModels;

    public FilterPanel init(final ReadablePreset.ReadableVoice voice, TableExclusiveSelectionContext tsc) throws ParameterException, DeviceException {
        this.setLayout(new FlowLayout());
        final List filterIds = voice.getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_FILTER);

        filterModels = new ReadableParameterModel[filterIds.size()];

        try {
            for (int i = 0, j = filterIds.size(); i < j; i++)
                filterModels[i] = voice.getParameterModel((Integer) filterIds.get(i));
        } catch (IllegalParameterIdException e) {
            ZUtilities.zDisposeCollection(Arrays.asList(filterModels));
            throw e;
        }

        Action ra = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) filterIds.toArray(new Integer[filterIds.size()]));
                        }catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        ra.putValue("tip", "Refresh Filter");

        FilterParameterTableModel model = new FilterParameterTableModel(filterModels);

        RowHeaderedAndSectionedTablePanel ampPanel;
        VoiceParameterTable vpt =new VoiceParameterTable(voice, ParameterCategories.VOICE_FILTER, model, "Filter");
        tsc.addTableToContext(vpt);
        ampPanel = new RowHeaderedAndSectionedTablePanel().init(vpt, "Show Filter", UIColors.getTableBorder(), ra);
        this.add(ampPanel);
        return this;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public void zDispose() {
        ZUtilities.zDisposeCollection(Arrays.asList(filterModels));
        filterModels = null;
    }
}
