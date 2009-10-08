package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZMTCommand;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class NewVoiceZoneZMTC extends AbstractEditableVoiceZMTCommand {
    private int zoneCount;

    public NewVoiceZoneZMTC() {
        this("New Zone", "Add new zone to voice", 1);
    }

    public NewVoiceZoneZMTC(String presString, String descString, int voiceCount) {
        super(presString, descString, null, null);
        this.zoneCount = voiceCount;
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public ZMTCommand getNextMode() {
        if (zoneCount == 8)
            return null;
        return new NewVoiceZoneZMTC((zoneCount + 1) + " zones", "Add " + (zoneCount + 1) + " new zones", zoneCount + 1);
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
                newZone(v);
            } else {
                HashMap done = new HashMap();
                for (int n = 0; n < num; n++) {
                    if (!done.containsKey(voices[n])) {
                        newZone(voices[n]);
                        done.put(voices[n], null);
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
        } catch (TooManyZonesException e) {
            throw new CommandFailedException("Too many zones");
        }
    }

    private void newZone(ContextEditablePreset.EditableVoice v) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyZonesException {
        v.newZones(IntPool.get(zoneCount));
    }

    public String getMenuPathString() {
        if (zoneCount == 1)
            return "";
        return ";New Zones";
    }
}

