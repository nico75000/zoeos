package com.pcmsolutions.device.EMU.E4.zcommands;


import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class RefreshSampleZMTC extends AbstractReadableSampleZMTCommand {

    public RefreshSampleZMTC() {
        super("Refresh", "Refresh Samples From Remote", null, null);
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
        com.pcmsolutions.device.EMU.E4.sample.ReadableSample[] samples = getTargets();
        int num = samples.length;
        ReadableSample p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                refreshSample(p);
            } else {
                HashMap done = new HashMap();
                for (int n = 0; n < num; n++) {
                    if (!done.containsKey(samples[n])) {
                        refreshSample(samples[n]);
                        done.put(samples[n], null);
                    }
                    Thread.yield();
                }
            }
        } catch (NoSuchSampleException e) {
            throw new CommandFailedException("Sample Not Found.");
        }
    }

    private void refreshSample(ReadableSample p) throws CommandFailedException, NoSuchSampleException {
        if (p == null)
            throw new CommandFailedException("Null Target");
        p.refreshSample();
    }

    public Icon getIcon() {
        return null;
        //return new ImageIcon("toolbarButtonGraphics/general/refresh16.gif");
    }
}

