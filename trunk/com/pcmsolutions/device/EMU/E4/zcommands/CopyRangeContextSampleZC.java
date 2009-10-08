package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.RemoteObjectStates;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.AggRemoteName;
import com.pcmsolutions.device.EMU.E4.sample.ContextReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyRangeContextSampleZC extends AbstractContextEditableSampleZCommand {
    private Object[] retArray = null;
    private int firstEmptyIndex = -1;

    public CopyRangeContextSampleZC() {
        super("Copy to Range", "Copy sample to all sample locations in specified range", new String[]{"Low ", "High"}, new String[]{"Low Destination Sample", "High Destination Sample"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (index > numArgs - 1)
            throw new IllegalArgumentException("Argument Out Of Range");

        if (retArray == null) {
            Map sampleMap = getTarget().getUserSampleNamesInContext();
            // sampleMap will be ordered
            sampleMap.remove(getTarget().getSampleNumber());
            retArray = new Object[sampleMap.size()];
            Iterator i = sampleMap.keySet().iterator();
            int k = 0;
            Integer s;
            while (i.hasNext()) {
                s = (Integer) i.next();
                try {
                    if (firstEmptyIndex == -1 && getTarget().getSampleContext().getSampleState(s) == RemoteObjectStates.STATE_EMPTY)
                        firstEmptyIndex = k;
                } catch (NoSuchSampleException e) {
                    e.printStackTrace();
                } catch (com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException e) {
                    e.printStackTrace();
                }
                retArray[k] = new com.pcmsolutions.device.EMU.E4.preset.AggRemoteName(s, (String) sampleMap.get(s));
                k++;
            }
        }
        JComboBox j = new JComboBox(retArray);
        if (firstEmptyIndex != -1)
            j.setSelectedIndex(firstEmptyIndex);
        return j;

    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        if (arguments.length < numArgs)
            throw new IllegalArgumentException("Insufficient Arguments");

        com.pcmsolutions.device.EMU.E4.preset.AggRemoteName sn;
        try {
            sn = new com.pcmsolutions.device.EMU.E4.preset.AggRemoteName(getTarget().getSampleNumber(), getTarget().getSampleName());
        } catch (NoSuchSampleException e) {
            sn = new com.pcmsolutions.device.EMU.E4.preset.AggRemoteName(getTarget().getSampleNumber(), "=Unkown Name=");
        } catch (SampleEmptyException e) {
            sn = new com.pcmsolutions.device.EMU.E4.preset.AggRemoteName(getTarget().getSampleNumber(), DeviceContext.EMPTY_PRESET);
        }
        return "Copy " + sn + " to sample range (" + arguments[0] + ") ... (" + arguments[1] + ")";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (arguments.length < numArgs)
            throw new IllegalArgumentException("Insufficient Arguments");

        Integer lowSample = ((AggRemoteName) arguments[0]).getIndex();
        Integer highSample = ((AggRemoteName) arguments[1]).getIndex();

        if (lowSample.intValue() > highSample.intValue())
            throw new CommandFailedException("Invalid Range");

        ContextReadableSample targ = getTarget();
        int num = highSample.intValue() - lowSample.intValue() + 1;
        Integer[] destIndexes = ZUtilities.fillIncrementally(new Integer[num], lowSample.intValue());
        Integer[] srcIndexes = new Integer[destIndexes.length];
        Arrays.fill(srcIndexes, targ.getSampleNumber());
        try {
            SampleContextMacros.copySamples(targ.getSampleContext(), srcIndexes, destIndexes, false, true, "Sample Copy to Range");
        } catch (NoSuchSampleException e) {
            throw new CommandFailedException("No sample: " + e.getAggName());
        } catch (com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException e) {
            throw new CommandFailedException("No sample context");
        } catch (IsolatedSampleUnavailableException e) {
            throw new CommandFailedException("Could not copy samples: " + e.getMessage());
        }
    }

    public String getMenuPathString() {
        return ";Copy";
    }
}


