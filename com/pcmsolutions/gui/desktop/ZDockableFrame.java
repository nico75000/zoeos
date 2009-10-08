package com.pcmsolutions.gui.desktop;

import com.jidesoft.docking.DockableFrame;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.device.EMU.E4.gui.device.DeviceIcon;
import com.pcmsolutions.gui.EmptyIcon;

/**
 * User: paulmeehan
 * Date: 19-Jan-2004
 * Time: 07:35:56
 */
public class ZDockableFrame extends DockableFrame implements ZDisposable {
    public ZDockableFrame(String s) {
        super(s, EmptyIcon.getInstance());
    }

    public void zDispose() {
    }
}
