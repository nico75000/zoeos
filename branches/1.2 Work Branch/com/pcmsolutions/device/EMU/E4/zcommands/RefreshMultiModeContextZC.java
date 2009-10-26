package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class RefreshMultiModeContextZC extends AbstractMultiModeContextZCommand {

    public RefreshMultiModeContextZC() {
        super("Refresh MultiMode", "Refresh MultiMode Parameters from Remote", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (getTarget() == null)
            throw new CommandFailedException("No target");
        getTarget().refresh();
    }

    public ZDialog generateVerificationDialog() {
        return null;
    }

    public Icon getIcon() {
        return new ImageIcon("toolbarButtonGraphics/general/refresh16.gif");
    }
}
