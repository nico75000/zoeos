package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.CommandFailedException;

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
public class AssertRemotePresetZMTC extends AbstractContextEditablePresetZMTCommand {

    public AssertRemotePresetZMTC() {
        super("Assert Remote", "Send local preset to remote", null, null);
    }

    public int getMnemonic() {
        return KeyEvent.VK_A;
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
        ReadablePreset[] presets = getTargets();
        int num = presets.length;
        ReadablePreset p;
        try {
            if (num == 0) {
                // try use primary target
                p = getTarget();
                assertPreset(p);
            } else {
                HashMap done = new HashMap();
                for (int n = 0; n < num; n++) {
                    if (!done.containsKey(presets[n])) {
                        assertPreset(presets[n]);
                        done.put(presets[n], null);
                    }
                    Thread.yield();
                }
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        }
    }

    private void assertPreset(ReadablePreset p) throws CommandFailedException, NoSuchPresetException {
        if (p == null)
            throw new CommandFailedException("Null Target");
        if ( !p.isPresetInitialized())
            throw new CommandFailedException(p.getPresetDisplayName() + " is not initialized");
        p.assertPresetRemote();
    }

    public Icon getIcon() {
        return null;
    }

    public String getMenuPathString() {
        return ";Utility";
    }
}

