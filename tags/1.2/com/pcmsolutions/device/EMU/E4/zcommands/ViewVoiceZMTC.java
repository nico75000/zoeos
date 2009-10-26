package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class ViewVoiceZMTC extends AbstractReadableVoiceZMTCommand {
    public ViewVoiceZMTC() {
        super("Open", "Open for viewing", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ReadablePreset.ReadableVoice[] voices = getTargets();
        int num = voices.length;
        ReadablePreset.ReadableVoice v;

        try {
            if (num == 0) {
                // try use primary target
                v = getTarget();
                viewVoices(new ReadablePreset.ReadableVoice[]{v});
            } else {
                Arrays.sort(voices);
                viewVoices(voices);
            }
        } finally {

        }
    }

    private void viewVoices(ReadablePreset.ReadableVoice[] voices) {
        boolean tabbed = voices[0].getPreset().getDeviceContext().getDevicePreferences().ZPREF_useTabbedVoicePanel.getValue();
        boolean grouped = voices[0].getPreset().getDeviceContext().getDevicePreferences().ZPREF_groupEnvelopesWhenVoiceTabbed.getValue();

        for (int i = 0; i < voices.length; i++)
            if (tabbed) {
                voices[i].getPreset().getDeviceContext().getViewManager().openTabbedVoice(voices[i], grouped, (i == 0 ? true : false));
            } else
                voices[i].getPreset().getDeviceContext().getViewManager().openVoice(voices[i], (i == 0 ? true : false));
    }
}

