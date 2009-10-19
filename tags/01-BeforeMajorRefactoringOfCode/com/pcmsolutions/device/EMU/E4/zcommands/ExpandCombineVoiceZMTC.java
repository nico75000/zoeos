package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.ZMTCommandTargetsNotSuitableException;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class ExpandCombineVoiceZMTC extends AbstractEditableVoiceZMTCommand {
    private int mode = 0;

    public ExpandCombineVoiceZMTC() {
        super("Expand", "Expand zones to seperate voices", null, null);
    }

    public ExpandCombineVoiceZMTC(int mode) {
        this.mode = mode;
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        super.setTargets(targets);
        if (mode == 1) {
            Integer grp;
            try {
                grp = getTargets()[0].getVoiceParams(new Integer[]{IntPool.get(37)})[0];
                init("Combine G" + grp, "Combine voices in group " + grp, null, null);
                return;
            } catch (NoSuchPresetException e) {
                e.printStackTrace();
            } catch (PresetEmptyException e) {
                e.printStackTrace();
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            } catch (NoSuchVoiceException e) {
                e.printStackTrace();
            }
            throw new ZMTCommandTargetsNotSuitableException();
        }
    }

    public ZMTCommand getNextMode() {
        if (mode == 0)
            return new ExpandCombineVoiceZMTC(1);
        return null;
    }

    public int getMaxNumTargets() {
        if (mode == 1)
            return 1;
        return super.getMaxNumTargets();
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditablePreset.EditableVoice[] voices = getTargets();
        int num = voices.length;
        ContextEditablePreset.EditableVoice v;
        try {
            if (num == 0) {
                // try use primary target
                v = getTarget();
                if (mode == 0)
                    v.expandVoice();
                else
                    v.combineVoiceGroup();
            } else {
                Arrays.sort(voices);
                HashSet done = new HashSet();
                for (int n = num - 1; n >= 0; n--) {
                    if (!done.contains(voices[n])) {
                        if (mode == 0)
                            voices[n].expandVoice();
                        else
                            voices[n].combineVoiceGroup();
                        done.add(voices[n]);
                    }
                    Thread.yield();
                }
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        } catch (NoSuchVoiceException e) {
            throw new CommandFailedException("No such voice");
        } catch (TooManyVoicesException e) {
            throw new CommandFailedException("Too many voices");
        }
    }
}

