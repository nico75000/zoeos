package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.sample.ContextBasicEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZMTCommandTargetsNotSuitableException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class RenameSampleAllZMTC extends AbstractContextBasicEditableSampleZMTCommand {

    public RenameSampleAllZMTC() {
        super("Rename All", "Rename All Samples with a single name", new String[]{"Name"}, new String[]{"New name for samples"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        String name = "";
        try {
            name = getTargets()[0].getSampleName();
        } catch (NoSuchSampleException e) {
        } catch (SampleEmptyException e) {
        }
        if (name.length() > 16)
            name = name.substring(0, 15);

        FixedLengthTextField tf = new FixedLengthTextField(name, 16);
        tf.selectAll();

        return tf;
    }

    public int getMinNumTargets() {
        return 2;
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        try {
            return "Rename All Samples" + getTarget().getSampleDisplayName();
        } catch (NoSuchSampleException e) {
            return "Rename sample ";
        }
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextBasicEditableSample[] samples = getTargets();

        for (int i = 0; i < samples.length; i++)
            try {
                samples[i].setSampleName(arguments[0].toString());
            } catch (NoSuchSampleException e) {
            } catch (SampleEmptyException e) {
            }
    }

    public ZDialog generateVerificationDialog() {
        return null;
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        if (targets.length < 2)
            throw new IllegalArgumentException("not enough targets");
        super.setTargets(targets);
    }

    public String getMenuPathString() {
        return ";Special Naming";
    }

    public Icon getIcon() {
        return null;
    }
}
