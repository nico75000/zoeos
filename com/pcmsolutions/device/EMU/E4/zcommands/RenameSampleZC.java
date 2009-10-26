package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.gui.FixedLengthTextField;
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
public class RenameSampleZC extends AbstractContextBasicEditableSampleZCommand {

    public RenameSampleZC() {
        super("Rename", "Rename Sample", new String[]{"Name"}, new String[]{"New name for sample"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        String name = "";
        try {
            name = getTarget().getSampleName();
        } catch (NoSuchSampleException e) {
        } catch (SampleEmptyException e) {
        }
        StringBuffer bufName = new StringBuffer(name);
        if (name.length() > 16)
            name = name.substring(0, 15);

        FixedLengthTextField tf = new FixedLengthTextField(name, 16);
        tf.selectAll();

        return tf;
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        return "Rename sample " + getTarget();
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (getTarget() == null)
            throw new CommandFailedException();
        try {
            getTarget().setSampleName((String) arguments[0]);
        } catch (NoSuchSampleException e) {
            throw new CommandFailedException("Sample Not Found.");
        } catch (SampleEmptyException e) {
            throw new CommandFailedException("Sample is Empty.");
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
