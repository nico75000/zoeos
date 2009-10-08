package com.pcmsolutions.device.EMU.E4.packaging;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Dec-2003
 * Time: 01:00:24
 * To change this template use Options | File Templates.
 */
class Impl_SamplePackageHeader extends Impl_PackageHeader implements SamplePackage.Header, Serializable {
    static final long serialVersionUID = 1;

    protected int sampleCount = 0;
    protected int physicalSampleCount = 0;

    public int getPhysicalSampleCount() {
        return physicalSampleCount;
    }

    public void setPhysicalSampleCount(int physicalSampleCount) {
        this.physicalSampleCount = physicalSampleCount;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }
}
