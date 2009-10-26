package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-May-2003
 * Time: 05:25:13
 * To change this template use Options | File Templates.
 */
public class VoiceOverviewRowHeaderTableCellRenderer extends GeneralTableCellRenderer implements TableCellRenderer {
    public static final VoiceOverviewRowHeaderTableCellRenderer INSTANCE = new VoiceOverviewRowHeaderTableCellRenderer();

    private VoiceOverviewRowHeaderTableCellRenderer() {
    }

    protected void setupLook(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof VoiceOverviewTableModel.Voice.Zone) {
            setForeground(UIColors.getVoiceOverViewTableRowHeaderSectionZoneFG());
            setBackground(UIColors.getVoiceOverViewTableRowHeaderSectionZoneBG());
            bdrSel = new BevelBorder(BevelBorder.LOWERED, UIColors.getVoiceOverViewTableRowHeaderSectionZoneBG(), UIColors.getVoiceOverViewTableRowHeaderSectionZoneFG());
        } else {
            setForeground(UIColors.getVoiceOverViewTableRowHeaderSectionVoiceFG());
            setBackground(UIColors.getVoiceOverViewTableRowHeaderSectionVoiceBG());
            bdrSel = new BevelBorder(BevelBorder.LOWERED, UIColors.getVoiceOverViewTableRowHeaderSectionVoiceBG(), UIColors.getVoiceOverViewTableRowHeaderSectionVoiceFG());
        }
    }
}
