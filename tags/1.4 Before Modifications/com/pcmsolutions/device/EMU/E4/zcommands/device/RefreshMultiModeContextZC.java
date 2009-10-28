package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class RefreshMultiModeContextZC extends AbstractMultiModeContextZCommand {

    public String getPresentationString() {
        return "Refresh multimode";
    }

    public String getDescriptiveString() {
        return "Refresh multimode parameters from remote";
    }

    public Icon getIcon() {
        return new ImageIcon("toolbarButtonGraphics/general/refresh16.gif");
    }

    public boolean handleTarget(MultiModeContext multiModeContext, int total, int curr) throws Exception {
        multiModeContext.refresh().post();
        return true;
    }
}
