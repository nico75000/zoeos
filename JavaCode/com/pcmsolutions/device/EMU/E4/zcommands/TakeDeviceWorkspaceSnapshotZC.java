package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.gui.FixedLengthTextField;
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
public class TakeDeviceWorkspaceSnapshotZC extends AbstractDeviceContextZCommand {
    private static final String UNTITLED_SNAPSHOT = "Untitled";

    public TakeDeviceWorkspaceSnapshotZC() {
        super("Take a workspace snapshot", "Take workspace snapshot", new String[]{"Snapshot title"}, new String[]{"Title for this snapshot"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        FixedLengthTextField tf = new FixedLengthTextField(UNTITLED_SNAPSHOT, 48);
        tf.selectAll();
        return tf;
    }

    public int getMnemonic() {
        return KeyEvent.VK_S;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (getTarget() == null)
            throw new CommandFailedException();
        getTarget().getViewManager().takeSnapshot(arguments[0].toString());
    }
}
