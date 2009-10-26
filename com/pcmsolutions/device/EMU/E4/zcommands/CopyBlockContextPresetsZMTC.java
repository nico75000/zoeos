package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.RemoteObjectStates;
import com.pcmsolutions.device.EMU.E4.preset.AggRemoteName;
import com.pcmsolutions.device.EMU.E4.preset.ContextReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
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
public class CopyBlockContextPresetsZMTC extends AbstractContextReadablePresetZMTCommand {
    private Object[] retArray = null;

    public CopyBlockContextPresetsZMTC() {
        super("Copy as Block", "Copy presets as a block", new String[]{"Destination "}, new String[]{"Destination Preset"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (index > numArgs - 1)
            throw new IllegalArgumentException("Argument Out Of Range");

        int firstEmptyIndex = -1;

        ContextReadablePreset[] targets = getTargets();
        if (retArray == null) {
            Map presetMap = targets[0].getPresetNamesInContext();
            // presetMap will be ordered
            for (int i = 0; i < targets.length; i++)
                presetMap.remove(targets[i].getPresetNumber());

            retArray = new Object[presetMap.size()];
            Iterator i = presetMap.keySet().iterator();
            int k = 0;
            Integer p;
            while (i.hasNext()) {
                p = (Integer) i.next();
                try {
                    if (firstEmptyIndex == -1 && targets[0].getPresetContext().getPresetState(p) == RemoteObjectStates.STATE_EMPTY)
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
        ContextReadablePreset[] presets = getTargets();
        if (presets.length == 0)
            doBlockCopy(new ContextReadablePreset[]{getTarget()}, destIndex);
        else
            doBlockCopy(presets, destIndex);
    }

    protected void doBlockCopy(ContextReadablePreset[] presets, int destIndex) throws CommandFailedException {
        Integer[] srcIndexes = ZUtilities.extractIndexes(presets);
        Integer[] destIndexes = ZUtilities.fillIncrementally(new Integer[presets.length], destIndex);
        try {
            PresetContextMacros.copyPresets(presets[0].getPresetContext(), srcIndexes, destIndexes, false, true, "Preset Block Copy");
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("No preset: " + e.getAggName());
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("No preset context");
        }
    }
}

