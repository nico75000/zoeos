package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.gui.packaging.PackagingGUIFactory;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.zcommands.icons.SamplePackageLoadIcon;
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
public class LoadSamplePackageZMTC extends AbstractContextEditableSampleZMTCommand {

    public String getMenuPathString() {
        return ";Packaging";
    }

    public boolean handleTarget(ContextEditableSample sample, int total, int curr) throws Exception {
        PackagingGUIFactory.loadSamplePackage(getTargets().get(0));
        return false;
    }

    protected void acceptTargets() throws ZCommandTargetsNotSuitableException, ZCommandTargetsNotSpecifiedException {
        super.acceptTargets();
        try {
            if (!getTargets().get(0).getSampleContext().getDeviceContext().isSmdiCoupled())
                throw new ZCommandTargetsNotSuitableException();
        } catch (DeviceException e) {
            throw new ZCommandTargetsNotSuitableException();
        }
    }

    protected ContextEditableSample sample;

    public int getMaxNumTargets() {
        return 1;
    }

    public String getPresentationString() {
        return "Load" + ZUtilities.DOT_POSTFIX;
    }

    public String getDescriptiveString() {
        return "Load a sample package";
    }

    public Icon getIcon() {
        return SamplePackageLoadIcon.INSTANCE;
    }
}

