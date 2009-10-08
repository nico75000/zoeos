package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class CopyLinkZMTC extends AbstractEditableLinkZMTCommand {

    public CopyLinkZMTC() {
        super("Copy", "Copy Link", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditablePreset.EditableLink[] links = getTargets();
        int num = links.length;
        ContextEditablePreset.EditableLink l;

        try {
            if (num == 0) {
                // try use primary target
                l = getTarget();
                l.copyLink();
            } else {
                //Arrays.sort(links);
                for (int n = 0; n < num; n++) {
                    links[n].copyLink();
                    Thread.yield();
                }
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("Preset Not Found.");
        } catch (NoSuchLinkException e) {
            throw new CommandFailedException("No such link ");
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Preset Empty");
        } catch (TooManyVoicesException e) {
            throw new CommandFailedException("Too many voices");
        }
    }
}

