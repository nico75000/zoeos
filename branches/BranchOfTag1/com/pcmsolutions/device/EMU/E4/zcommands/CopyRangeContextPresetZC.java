package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.RemoteObjectStates;
import com.pcmsolutions.device.EMU.E4.preset.*;
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
public class CopyRangeContextPresetZC extends AbstractContextReadablePresetZCommand {
    private Object[] retArray = null;
    private int firstEmptyIndex = -1;

    public CopyRangeContextPresetZC() {
        super("Copy to Range", "Copy preset to all preset locations in specified range", new String[]{"Low ", "High"}, new String[]{"Low Destination Preset", "High Destination Preset"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (index > numArgs - 1)
            throw new IllegalArgumentException("Argument Out Of Range");

        if (retArray == null) {
            Map presetMap = getTarget().getPresetNamesInContext();
            // presetMap will be ordered
            presetMap.remove(getTarget().getPresetNumber());
            retArray = new Object[presetMap.size()];
            Iterator i = presetMap.keySet().iterator();
            int k = 0;
            Integer p;
            while (i.hasNext()) {
                p = (Integer) i.next();
                try {
                    if (firstEmptyIndex == -1 && getTarget().getPresetContext().getPresetState(p) == RemoteObjectStates.STATE_EMPTY)
                        firstEmptyIndex = k;
                } catch (NoSuchPresetException e) {
                    e.printStackTrace();
                } catch (NoSuchContextException e) {
                    e.printStackTrace();
                }
                retArray[k] = new AggRemoteName(p, (String) presetMap.get(p));
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

        AggRemoteName pn;
        try {
            pn = new AggRemoteName(getTarget().getPresetNumber(), getTarget().getPresetName());
        } catch (NoSuchPresetException e) {
            pn = new AggRemoteName(getTarget().getPresetNumber(), "=Unkown Name=");
        } catch (PresetEmptyException e) {
            pn = new AggRemoteName(getTarget().getPresetNumber(), DeviceContext.EMPTY_PRESET);
        }
        return "Copy " + pn + " to preset range (" + arguments[0] + ") ... (" + arguments[1] + ")";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (arguments.length < numArgs)
            throw new IllegalArgumentException("Insufficient Arguments");

        Integer lowPreset = ((AggRemoteName) arguments[0]).getIndex();
        Integer highPreset = ((AggRemoteName) arguments[1]).getIndex();

        if (lowPreset.intValue() > highPreset.intValue())
            throw new CommandFailedException("Invalid Range");

        ContextReadablePreset targ = getTarget();
        int num = highPreset.intValue() - lowPreset.intValue() + 1;
        Integer[] destIndexes = ZUtilities.fillIncrementally(new Integer[num], lowPreset.intValue());
        Integer[] srcIndexes = new Integer[destIndexes.length];
        Arrays.fill(srcIndexes, targ.getPresetNumber());
        try {
            PresetContextMacros.copyPresets(targ.getPresetContext(), srcIndexes, destIndexes, false, true, "Preset Copy to Range");
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("No preset: " + e.getAggName());
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("No preset context");
        }
    }

    public String getMenuPathString() {
        return ";Copy";
    }
}


