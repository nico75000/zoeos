package com.pcmsolutions.gui.desktop;

import com.jidesoft.docking.DockableFrame;
import com.pcmsolutions.gui.EmptyIcon;
import com.pcmsolutions.system.ZDisposable;

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
