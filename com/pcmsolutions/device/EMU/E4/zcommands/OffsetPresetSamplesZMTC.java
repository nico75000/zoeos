package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class OffsetPresetSamplesZMTC extends AbstractContextEditablePresetZMTCommand {
   private boolean userMode;

    public OffsetPresetSamplesZMTC() {
        // stateInitial("Offset Sample Indexes", "Offset sample indexes in preset", new String[]{"Offset"}, new String[]{"Sample index offset"});
        this(true);
    }

    private OffsetPresetSamplesZMTC(boolean userMode) {
        if (userMode)
            init("Offset User Sample Indexes", "Offset user sample indexes in preset", new String[]{"Offset"}, new String[]{"Sample index offset"});
        else
            init("Offset ROM Sample Indexes", "Offset rom sample indexes in preset", new String[]{"Offset"}, new String[]{"Sample index offset"});

        this.userMode = userMode;
    }

    public ZMTCommand getNextMode() {
        if (userMode == true)
            return new OffsetPresetSamplesZMTC(false);
        return null;
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return new FixedLengthTextField("", 5);
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        Integer offset;
        try {
            offset = IntPool.get(Integer.parseInt(arguments[0].toString()));
        } catch (NumberFormatException e) {
            throw new CommandFailedException("not a valid offset");
        }

        ContextEditablePreset[] presets = getTargets();
        int num = presets.length;
        ContextEditablePreset p;
        try {
            for (int n = 0; n < num; n++) {
                offsetSamples(presets[n], offset);
                Thread.yield();
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        }
    }

    private void offsetSamples(ContextEditablePreset p, Integer offset) throws NoSuchPresetException {
        try {
            p.offsetSampleIndexes(offset, userMode);
        } catch (PresetEmptyException e) {
        }
    }

    public String getMenuPathString() {
        return ";Utility";
    }
}
