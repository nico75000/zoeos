package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Jul-2003
 * Time: 21:03:50
 * To change this template use Options | File Templates.
 */
public class EditableCordPanel extends RowHeaderedAndSectionedTablePanel {

    public EditableCordPanel init(final ContextEditablePreset.EditableVoice[] voices) throws ZDeviceNotRunningException, IllegalParameterIdException {
        if (voices == null || voices.length < 1)
            throw new IllegalArgumentException("Need at least one voice for am EditableCordPanel");

        final Integer[] cordIds = new Integer[54];
        for (int i = 0, j = 54; i < j; i++)
            cordIds[i] = IntPool.get(129 + i);
        EditableParameterModel[] cordModels = ParameterModelUtilities.getEditableParameterModelGroups(voices, cordIds);

        Action rct = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Cords") {
                    public void run() {
                        try {
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), cordIds);
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
        rct.putValue("tip", "Refresh Cords");
        super.init(new EditableCordTable(voices, cordModels, "Cords"), "Show Cords", UIColors.getTableBorder(), rct);
        return this;
    }
}
