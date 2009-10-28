package com.pcmsolutions.device.EMU.E4.gui.multimode;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeChannelChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeRefreshedEvent;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeChannelChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeRefreshedEvent;
import com.pcmsolutions.device.EMU.E4.gui.preset.AbstractPresetTableCellEditor;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMultimodeChannelException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeChannel;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeListener;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.threads.Impl_ZThread;

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

    public MultiModePresetTableCellEditor(DeviceContext d) throws DeviceException {
        super(d, Color.white, Color.black);
        this.mmc = d.getMultiModeContext();
        mmc.addMultiModeListener(this);
    }

    protected List buildPresetItemList()  {
        List l = new ArrayList();
        l.add("Disabled");
        l.addAll(super.buildPresetItemList());
        return l;
    }

    protected Integer getSelectedIndex() throws ParameterException {
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
        } catch (IllegalMultimodeChannelException e) {
            // this should never happen!!
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    protected void doAction(final Object selection) {
       // Impl_ZThread.parameterTQ.postTask(new Impl_ZThread.Task(){
       //     public void doTask() {
        try{
        if (selection.equals("Disabled"))
                    mmch.setPreset(IntPool.get(-1));
                else if (selection instanceof ReadablePreset)
                    mmch.setPreset(((ReadablePreset) selection).getIndex());
        }catch(Exception e){
            e.printStackTrace();
        }
       //     }
      //  });
    }

    public void mmChannelChanged(MultiModeChannelChangedEvent ev) {
        if (mmch != null)
            if (ev.getChannel().equals(mmch.getChannel()))
                try {
                    setSelectedIndex(IntPool.get(mmch.getPreset().intValue() + 1));
                } catch (ParameterException e) {
                    e.printStackTrace();
                }
    }

    public void mmRefreshed(MultiModeRefreshedEvent ev) {
        if (mmch != null)
            try {
                setSelectedIndex(IntPool.get(mmch.getPreset().intValue() + 1));
            } catch (ParameterException e) {
                e.printStackTrace();
            }
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
