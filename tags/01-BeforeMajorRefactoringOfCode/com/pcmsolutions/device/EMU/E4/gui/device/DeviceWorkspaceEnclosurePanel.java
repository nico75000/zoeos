package com.pcmsolutions.device.EMU.E4.gui.device;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;

import javax.swing.*;

/**
 * User: paulmeehan
 * Date: 10-Feb-2004
 * Time: 13:15:30
 */
public class DeviceWorkspaceEnclosurePanel extends DefaultDeviceEnclosurePanel {
    public void init(DeviceContext device, JComponent enclosedComponent) throws Exception {
        if (!(enclosedComponent instanceof TitleProvider))
            throw new IllegalArgumentException("DeviceWorkspaceEnclosurePanel component must be a TitleProvider");
        super.init(device, enclosedComponent);
    }

    public String getTitle() {
        return ((TitleProvider) enclosedComponent).getTitle();
    }

    public String getReducedTitle() {
        return ((TitleProvider) enclosedComponent).getReducedTitle();
    }

    public final void addTitleProviderListener(TitleProviderListener tpl) {
        if (enclosedComponent instanceof TitleProvider)
            ((TitleProvider) enclosedComponent).addTitleProviderListener(tpl);
    }

    public final void removeTitleProviderListener(TitleProviderListener tpl) {
        if (enclosedComponent instanceof TitleProvider)
            ((TitleProvider) enclosedComponent).removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return ((TitleProvider) enclosedComponent).getIcon();
    }
}
