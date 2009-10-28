package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.gui.Impl_TableExclusiveSelectionContext;
import com.pcmsolutions.device.EMU.E4.gui.TableExclusiveSelectionContext;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Jul-2003
 * Time: 20:46:29
 * To change this template use Options | File Templates.
 */
public class CordPanel extends RowHeaderedAndSectionedTablePanel {
    public CordPanel init(final ReadablePreset.ReadableVoice voice, TableExclusiveSelectionContext tsc) throws ParameterException {
        final Integer[] cordIds = new Integer[54];
        for (int i = 0, j = 54; i < j; i++)
            cordIds[i] = IntPool.get(129 + i);

        ReadableParameterModel[] cordModels = new ReadableParameterModel[cordIds.length];

        try {
            for (int i = 0, j = 54; i < j; i++)
                cordModels[i] = voice.getParameterModel(cordIds[i]);
        } catch (ParameterException e) {
            ZUtilities.zDisposeCollection(Arrays.asList(cordModels));
            throw e;
        }
        Action rct = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), cordIds);
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        rct.putValue("tip", "Refresh CORDS");

        CordTable ct = new CordTable(voice, cordModels, "CORDS");
        if (tsc!= null)
        tsc.addTableToContext(ct);
       // ct.setHidingSelectionOnFocusLost(true);
        super.init(ct, "Show CORDS", UIColors.getTableBorder(), rct);
        return this;
    }

    public void zDispose() {
        super.zDispose();
    }
}
