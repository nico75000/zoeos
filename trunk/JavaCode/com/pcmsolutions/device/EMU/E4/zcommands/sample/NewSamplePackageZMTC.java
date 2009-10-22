package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.gui.packaging.PackagingGUIFactory;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.SamplePackageCreateIcon;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;
import com.pcmsolutions.system.ZCommandTargetsNotSuitableException;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class NewSamplePackageZMTC extends AbstractContextEditableSampleZMTCommand {
    protected SampleContext sc;
    protected Integer[] sampleIndexes;

    public String getMenuPathString() {
        return ";Packaging";
    }

    public boolean handleTarget(ContextEditableSample sample, int total, int curr) throws Exception {
        ContextEditableSample[] samples = getTargets().toArray(new ContextEditableSample[numTargets()]);
        sampleIndexes = SampleContextMacros.extractUniqueSampleIndexes(samples);
        sc = samples[0].getSampleContext();
        PackagingGUIFactory.newSamplePackage(samples, suggestPackageName(samples));
        return false;
    }


     public static String suggestPackageName(ReadableSample[] samples) throws  EmptyException, SampleException {
        String[] names = new String[samples.length];
        int i = 0;
        for (ReadableSample s : samples)
            names[i++] = s.getName();

        String suggName = ZUtilities.getCommonPrefix(names);
        if (suggName.length() < 1)
            if (samples.length == 1)
                try {
                    suggName = samples[0].getName();
                } catch (Exception e) {
                }
            else
                suggName = "Untitled";
        return suggName;
    }
    
    protected void acceptTargets() throws ZCommandTargetsNotSuitableException, ZCommandTargetsNotSpecifiedException {
        super.acceptTargets();
        ContextEditableSample[] samples = getTargets().toArray(new ContextEditableSample[numTargets()]);
        try {
            if (!samples[0].getSampleContext().getDeviceContext().isSmdiCoupled())
                throw new ZCommandTargetsNotSuitableException();
        } catch (DeviceException e) {
            throw new ZCommandTargetsNotSuitableException();
        }
        for (int i = 0; i < samples.length; i++)
            try {
                if (!samples[i].isEmpty() && !samples[i].isPending())
                    return;
            } catch (SampleException e) {
            }
        throw new ZCommandTargetsNotSuitableException();
    }

    public String getPresentationString() {
        return "Create new" + ZUtilities.DOT_POSTFIX;
    }

    public String getDescriptiveString() {
        return "Create a new sample package";
    }

    public Icon getIcon() {
        return SamplePackageCreateIcon.INSTANCE;
    }
}

