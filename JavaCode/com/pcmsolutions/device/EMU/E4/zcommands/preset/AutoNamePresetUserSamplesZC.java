package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.ContextBasicEditablePreset;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.callback.Callback;

import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class AutoNamePresetUserSamplesZC extends AbstractContextBasicEditablePresetZCommand {

    public String getPresentationString() {
        return "Auto-name user samples";
    }

    public String getDescriptiveString() {
        return "Apply preset name to user samples using a numerical postfix scheme for multiple samples";
    }

    public boolean handleTarget(final ContextBasicEditablePreset p, int total, int curr) throws Exception {
        final HashSet<Integer> handled = new HashSet<Integer>();
        for (ContextBasicEditablePreset preset : getTargets()) {
            final ContextBasicEditablePreset f_preset = preset;
            preset.getPresetContext().assertInitialized(preset.getIndex(), false).post(new Callback() {
                public void result(Exception e, boolean wasCancelled) {
                    try {
                        int postfix = 0;
                        String pname = f_preset.getName();
                        Integer[] samples = f_preset.getSampleUsage().getIntegers();
                        for (Integer s : samples)
                            if (!handled.contains(s) && s.intValue() < DeviceContext.BASE_ROM_SAMPLE && s.intValue() > 0) {
                                handled.add(s);
                                if (samples.length == 1)
                                    f_preset.getDeviceContext().getDefaultSampleContext().setName(s, pname).post();
                                else
                                    f_preset.getDeviceContext().getDefaultSampleContext().setName(s, ZUtilities.postfixString(pname, Integer.toString(postfix++), DeviceContext.MAX_NAME_LENGTH)).post();
                            }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
        return false;
    }

    public String getMenuPathString() {
        return ";Sample";
    }
}
