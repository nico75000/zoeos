package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.sample.ContextBasicEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class EraseSampleZMTC extends AbstractContextBasicEditableSampleZMTCommand {

    public EraseSampleZMTC() {
        super("Erase", "Erase Sample", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public boolean isSuitableAsLaunchButton() {
        return true;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextBasicEditableSample[] samples = getTargets();
        int num = samples.length;
        ContextBasicEditableSample p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                if (p == null)
                    throw new CommandFailedException("Null Target");
                eraseSample(p);
            }
            for (int n = 0; n < num; n++) {
                eraseSample(samples[n]);
                Thread.yield();
            }
        } catch (NoSuchSampleException e) {
            throw new CommandFailedException("Sample Not Found.");
        }
    }

    private void eraseSample(ContextBasicEditableSample s) throws CommandFailedException, NoSuchSampleException {
        if (s == null)
            throw new CommandFailedException("Null Target");
        try {
            s.eraseSample();
        } catch (SampleEmptyException e) {
        }
    }

    // if returns null no verification of command required
    public String getVerificationString() {
        int len = getTargets().length;
        return "Erase " + (len > 1 ? len + " sample locations?" : "sample location?");
    }


    public Icon getIcon() {
        return null;
        //return new ImageIcon("toolbarButtonGraphics/general/delete16.gif");
    }
}
