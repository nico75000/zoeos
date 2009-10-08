package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
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
public class RenamePresetZC extends AbstractContextBasicEditablePresetZCommand {

    public RenamePresetZC() {
        super("Rename", "Rename Preset", new String[]{"Name"}, new String[]{"New name for preset"});
    }

    public int getMnemonic() {
        return KeyEvent.VK_N;
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        String name = "";
        try {
            name = getTarget().getPresetName();
        } catch (NoSuchPresetException e) {
        } catch (PresetEmptyException e) {
        }
        StringBuffer bufName = new StringBuffer(name);
        if (name.length() > 16)
            name = name.substring(0, 15);

        FixedLengthTextField tf = new FixedLengthTextField(name, 16);
        tf.selectAll();

        return tf;
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        try {
            return "Rename " + getTarget().getPresetDisplayName();
        } catch (NoSuchPresetException e) {
            return "Rename preset ";
        }
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (getTarget() == null)
            throw new CommandFailedException();
        try {
            getTarget().setPresetName((String) arguments[0]);
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset is Empty.");
        }
    }

    public ZDialog generateVerificationDialog() {
        return null;
    }

    public Icon getIcon() {
        return null;
        //return new ImageIcon("toolbarButtonGraphics/text/normal16.gif");
    }
}
