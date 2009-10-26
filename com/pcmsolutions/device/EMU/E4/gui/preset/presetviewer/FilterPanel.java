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

public class FilterPanel extends JPanel implements ZDisposable {
    ReadableParameterModel[] filterModels;

    public FilterPanel init(final ReadablePreset.ReadableVoice voice) throws ZDeviceNotRunningException, IllegalParameterIdException {
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
                new ZDBModifyThread("Refresh Filter") {
                    public void run() {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) filterIds.toArray(new Integer[filterIds.size()]));
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
        ra.putValue("tip", "Refresh Filter");

        FilterParameterTableModel model = new FilterParameterTableModel(filterModels);

        RowHeaderedAndSectionedTablePanel ampPanel;
        ampPanel = new RowHeaderedAndSectionedTablePanel().init(new VoiceParameterTable(voice, ParameterCategories.VOICE_FILTER, model, "Filter"), "Show Filter", UIColors.getTableBorder(), ra);
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
