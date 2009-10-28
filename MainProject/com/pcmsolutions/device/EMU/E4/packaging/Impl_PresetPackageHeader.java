package com.pcmsolutions.device.EMU.E4.packaging;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Dec-2003
 * Time: 00:58:19
 * To change this template use Options | File Templates.
 */
class Impl_PresetPackageHeader extends Impl_PackageHeader implements PresetPackage.Header, Serializable {
    static final long serialVersionUID = 1;

    protected boolean includingMasterSettings;
    protected boolean includingMultimodeSettings;
    protected boolean includingSamples;
    protected int presetCount;

    public boolean isIncludingMasterSettings() {
        return includingMasterSettings;
    }

    public void setIncludingMasterSettings(boolean includingMasterSettings) {
        this.includingMasterSettings = includingMasterSettings;
    }

    public boolean isIncludingMultimodeSettings() {
        return includingMultimodeSettings;
    }

    public boolean isIncludingSamples() {
        return includingSamples;
    }

    public void setIncludingSamples(boolean includingSamples) {
        this.includingSamples = includingSamples;
    }

    public void setIncludingMultimodeSettings(boolean includingMultimodeSettings) {
        this.includingMultimodeSettings = includingMultimodeSettings;
    }

    public int getPresetCount() {
        return presetCount;
    }

    public void setPresetCount(int presetCount) {
        this.presetCount = presetCount;
    }
}
