package com.pcmsolutions.device.EMU.E4.gui.multimode;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.events.MultiModeChannelChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.MultiModeRefreshedEvent;
import com.pcmsolutions.device.EMU.E4.gui.preset.AbstractPresetTableCellEditor;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMidiChannelException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeChannel;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeListener;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class MultiModePresetTableCellEditor extends AbstractPresetTableCellEditor implements TableCellEditor, ActionListener, MultiModeListener, ZDisposable {
    private MultiModeContext mmc;
    private MultiModeChannel mmch;

    public MultiModePresetTableCellEditor(DeviceContext d) throws ZDeviceNotRunningException {
        super(d, Color.white, Color.black);
        this.mmc = d.getMultiModeContext();
        mmc.addMultiModeListener(this);
    }

    protected List buildPresetItemList() throws ZDeviceNotRunningException {
        List l = new ArrayList();
        l.add("Disabled");
        l.addAll(super.buildPresetItemList());
        return l;
    }

    protected Integer getSelectedIndex() {
        return IntPool.get(mmch.getPreset().intValue() + 1);
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        /*if ( mmc.has32Channels() && row > 31)
            row =31;
        else if (row > 15 )
            row = 15;
        */
        Integer chnl = IntPool.get(row + 1);
        try {
            mmch = mmc.getMultiModeChannel(chnl);
        } catch (IllegalMidiChannelException e) {
            // this should never happen!!
            e.printStackTrace();
        }
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    protected void doAction(Object selection) {
        if (selection.equals("Disabled"))
            mmch.setPreset(IntPool.get(-1));
        else if (selection instanceof ReadablePreset)
            mmch.setPreset(((ReadablePreset) selection).getPresetNumber());
    }

    public void mmChannelChanged(MultiModeChannelChangedEvent ev) {
        if (mmch != null)
            if (ev.getChannel().equals(mmch.getChannel()))
                setSelectedIndex(IntPool.get(mmch.getPreset().intValue() + 1));
    }

    public void mmRefreshed(MultiModeRefreshedEvent ev) {
        if (mmch != null)
            setSelectedIndex(IntPool.get(mmch.getPreset().intValue() + 1));
    }

    public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            if (me.getClickCount() >= 2) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    public void zDispose() {
        mmc.removeMultiModeListener(this);
        mmc = null;
        mmch = null;
    }
}
