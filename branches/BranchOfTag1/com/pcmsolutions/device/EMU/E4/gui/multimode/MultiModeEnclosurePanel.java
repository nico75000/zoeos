package com.pcmsolutions.device.EMU.E4.gui.multimode;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.device.DefaultDeviceEnclosurePanel;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;


public class MultiModeEnclosurePanel extends DefaultDeviceEnclosurePanel implements ZDisposable {
    protected DeviceContext device;
    private MultiModeEditorPanel mmep;

    public void init(DeviceContext device) throws Exception {
        this.device = device;
        mmep = new MultiModeEditorPanel().init(device, false, null);
        super.init(device,mmep);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    public void zDispose() {
        super.zDispose();
        device = null;
        mmep = null;
    }
}
