package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.master.MasterContext;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class RefreshMasterContextZC extends AbstractMasterContextZCommand {

    public String getPresentationString() {
        return "Refresh Master";
    }

    public String getDescriptiveString() {
        return "Refresh Master Parameters from Remote";
    }

    public Icon getIcon() {
        return new ImageIcon("toolbarButtonGraphics/general/refresh16.gif");
    }

    public boolean handleTarget(MasterContext masterContext, int total, int curr) throws Exception {
        masterContext.refresh().post();
        return false;
    }
}
