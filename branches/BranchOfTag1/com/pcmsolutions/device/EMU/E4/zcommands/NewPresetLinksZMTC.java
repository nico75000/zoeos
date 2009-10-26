package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.device.EMU.E4.preset.TooManyVoicesException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class NewPresetLinksZMTC extends AbstractContextEditablePresetZMTCommand {
    private int linkCount;

    public NewPresetLinksZMTC() {
        this("New Link", "Add 1 new link to preset", 1);
    }

    public NewPresetLinksZMTC(String presString, String descString, int voiceCount) {
        super(presString, descString, null, null);
        this.linkCount = voiceCount;
    }

    public int getMnemonic() {
        if (linkCount == 1)
            return KeyEvent.VK_L;

        return super.getMnemonic();
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public ZMTCommand getNextMode() {
        if (linkCount == 8)
            return null;
        return new NewPresetLinksZMTC((linkCount + 1) + " links", "Add " + (linkCount + 1) + " new links to preset", linkCount + 1);
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditablePreset[] presets = getTargets();
        int num = presets.length;
        ContextEditablePreset p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                newLink(p);
            } else {
                HashMap done = new HashMap();
                for (int n = 0; n < num; n++) {
                    if (!done.containsKey(presets[n])) {
                        newLink(presets[n]);
                        done.put(presets[n], null);
                    }
                    Thread.yield();
                }
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        } catch (TooManyVoicesException e) {
            throw new CommandFailedException("Too Many Voices");
        }
    }

    private void newLink(ContextEditablePreset p) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException {
        Integer[] presets = new Integer[linkCount];
        for (int i = 0, n = linkCount; i < n; i++)
            presets[i] = IntPool.get(0);

        p.newLinks(IntPool.get(linkCount), presets);
    }

    public String getMenuPathString() {
        if (linkCount == 1)
            return "";
        return ";New;Links";
    }
}

