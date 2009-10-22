package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.util.IntegerUseMap;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class ErasePresetSamplesZMTC extends AbstractContextEditablePresetZMTCommand {

    public int getMnemonic() {
        return KeyEvent.VK_S;
    }

    public String getPresentationString() {
        return "Erase samples";
    }

    public String getDescriptiveString() {
        return "Erase any user samples referenced by preset";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        ContextBasicEditablePreset[] presets = getTargets().toArray(new ContextBasicEditablePreset[numTargets()]);
        PresetContext pc = presets[0].getPresetContext();
        SampleContext sc = pc.getRootSampleContext();
        IntegerUseMap refSamples;
        Integer[] refUserSamples;
        Integer[] targetIndexes = PresetContextMacros.extractPresetIndexes(presets);
        List targetIndexesList = Arrays.asList(targetIndexes);
        try {
            refSamples = PresetContextMacros.getSampleUsage(pc, targetIndexes);
            refSamples.removeIntegerReference(IntPool.get(0));
            refUserSamples = SampleContextMacros.extractUserIndexes(refSamples.getIntegers());
            refUserSamples = SampleContextMacros.getNonEmpty(sc, refUserSamples);
            if (refUserSamples.length == 0) {
                UserMessaging.showInfo("No erasable samples");
                return false;
            }
            List dp = pc.getDatabasePresets();
            int uninitCount = 0;
            int warningCount = 0;
            for (Iterator i = dp.iterator(); i.hasNext();) {
                try {
                    ReadablePreset p = (ReadablePreset) i.next();
                    if (p.isInitialized()) {
                        if (targetIndexesList.contains(p.getIndex()))
                            continue;
                        IntegerUseMap um = p.getSampleUsage();
                        um.removeIntegerReference(IntPool.get(0));
                        if (um.containsAnyOf(refUserSamples))
                            warningCount++;
                    } else
                        uninitCount++;
                } catch (EmptyException e) {

                }
            }

            if (warningCount > 0) {
                if (uninitCount > 0) {
                    if (UserMessaging.askYesNo("You have chosen to erase " + ZUtilities.quantify(refUserSamples.length, "user sample") + ", but there are at least " + warningCount + " other presets using these samples (and there might be more). Continue with erase? "))
                        doErase(sc, refUserSamples);
                } else if (UserMessaging.askYesNo("You have chosen to erase " + ZUtilities.quantify(refUserSamples.length, "user sample") + ", but there are " + warningCount + " other presets using these samples. Continue with erase?"))
                    doErase(sc, refUserSamples);
            } else {
                if (uninitCount > 0) {
                    if (UserMessaging.askYesNo("You have chosen to erase " + ZUtilities.quantify(refUserSamples.length, "user sample") + ", but there could be other presets using these samples. Continue with erase? "))
                        doErase(sc, refUserSamples);
                } else if (UserMessaging.askYesNo("Erase " + ZUtilities.quantify(refUserSamples.length, "user sample") + "?")) ;
                doErase(sc, refUserSamples);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandFailedException("Internal error");
        }
        return false;
    }

    static void doErase(SampleContext sc, Integer[] samples) throws ResourceUnavailableException {
        for (int i = 0; i < samples.length; i++)
            sc.erase(samples[i]).post();
    }

    public String getMenuPathString() {
        return ";Sample";
    }
}
