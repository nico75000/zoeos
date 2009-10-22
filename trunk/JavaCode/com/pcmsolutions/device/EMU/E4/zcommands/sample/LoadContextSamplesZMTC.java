package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.E4.gui.packaging.PackagingGUIFactory;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;
import com.pcmsolutions.system.preferences.ZBoolPref;

import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class LoadContextSamplesZMTC extends AbstractContextEditableSampleZMTCommand {

    public static final ZBoolPref stripSampleAppendages = new Impl_ZBoolPref(Preferences.userNodeForPackage(LoadContextSamplesZMTC.class.getClass()), "stripSampleAppendages", true);

    public String getPresentationString() {
        return "Load" + ZUtilities.DOT_POSTFIX;
    }

    public String getDescriptiveString() {
        return "Load samples from a local directory";
    }

    public String getMenuPathString() {
        return "";
    }

    public boolean handleTarget(ContextEditableSample sample, int total, int curr) throws Exception {
        PackagingGUIFactory.loadSamples(getTargets().get(0));
        return false;
    }

    public int getMaxNumTargets() {
        return 1;
    }
}

