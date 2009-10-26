package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.RemoteObjectStates;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.AggRemoteName;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.ContextReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyBlockContextSamplesZMTC extends AbstractContextEditableSampleZMTCommand {
    private Object[] retArray = null;

    public CopyBlockContextSamplesZMTC() {
        super("Copy as Block", "Copy samples as a block", new String[]{"Destination "}, new String[]{"Destination Sample"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (index > numArgs - 1)
            throw new IllegalArgumentException("Argument Out Of Range");

        int firstEmptyIndex = -1;

        ContextReadableSample[] targets = getTargets();
        if (retArray == null) {
            Map sampleMap = targets[0].getUserSampleNamesInContext();
            // sampleMap will be ordered
            for (int i = 0; i < targets.length; i++)
                sampleMap.remove(targets[i].getSampleNumber());

            retArray = new Object[sampleMap.size()];
            Iterator i = sampleMap.keySet().iterator();
            int k = 0;
            Integer p;
            while (i.hasNext()) {
                p = (Integer) i.next();
                try {
                    if (firstEmptyIndex == -1 && targets[0].getSampleContext().getSampleState(p) == RemoteObjectStates.STATE_EMPTY)
                        firstEmptyIndex = k;
                } catch (NoSuchSampleException e) {
                    e.printStackTrace();
                } catch (NoSuchContextException e) {
                    e.printStackTrace();
                }
                retArray[k] = new AggRemoteName(p, (String) sampleMap.get(p));
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
        //if (!(arguments[0] instanceof ReadableSample))
        //  throw new IllegalArgumentException("Argument must be a sample");
        return "";
    }

    public Icon getIcon() {
        return null;
    }

    public int getMinNumTargets() {
        return 2;
    }

    public String getMenuPathString() {
        return ";Copy";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        int destIndex = ((AggRemoteName) arguments[0]).getIndex().intValue();
        ContextEditableSample[] samples = getTargets();

        if (samples.length == 0)
            doBlockCopy(new ContextEditableSample[]{getTarget()}, destIndex);
        else
            doBlockCopy(samples, destIndex);
    }

    protected void doBlockCopy(ContextEditableSample[] samples, int destIndex) throws CommandFailedException {

        Integer[] srcIndexes = ZUtilities.extractIndexes(samples);
        Integer[] destIndexes = ZUtilities.fillIncrementally(new Integer[samples.length], destIndex);
        try {
            SampleContextMacros.copySamples(samples[0].getSampleContext(), srcIndexes, destIndexes, false, true, "Sample Block Copy");
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("No preset context");
        } catch (NoSuchSampleException e) {
            throw new CommandFailedException("No sample: " + e.getAggName());
        } catch (IsolatedSampleUnavailableException e) {
            throw new CommandFailedException("Could not copy samples: " + e.getMessage());
        }
    }
}

