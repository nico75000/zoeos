package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class RenameE4DeviceZC extends AbstractDeviceContextZCommand {

    public RenameE4DeviceZC() {
        super("Rename Device", "Give this device a new name", new String[]{"Name"}, new String[]{"New name for device"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        String name = "";
        name = getTarget().getName();

        FixedLengthTextField tf = new FixedLengthTextField(name, 48);
        tf.selectAll();

        return tf;
    }

    public int getMnemonic() {
        return KeyEvent.VK_N;
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        return "Rename device " + getTarget();
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (getTarget() == null)
            throw new CommandFailedException();
        getTarget().setName((String) arguments[0]);
    }

    public ZDialog generateVerificationDialog() {
        return null;
    }

    public Icon getIcon() {
        return null;
        // return new ImageIcon("toolbarButtonGraphics/text/normal16.gif");
    }
}
