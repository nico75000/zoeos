package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.E4.packaging.SamplePackage;
import com.pcmsolutions.system.IntPool;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Jan-2004
 * Time: 10:17:37
 * To change this template use Options | File Templates.
 */
class SamplePackageHeaderInfoTable extends PackageHeaderInfoTable {
    public SamplePackageHeaderInfoTable(SamplePackage.Header hdr) {
        super(hdr);
    }

    private static final String SAMPLE_COUNT = "Sample count";
    private static final String PHYSICAL_SAMPLES = "Physical samples";

    protected Vector getDataVector() {
        Vector data = super.getDataVector();
        Vector row;
        if (getHeader() != null) {
            row = new Vector();
            row.add(SAMPLE_COUNT);
            row.add(IntPool.get(((SamplePackage.Header) getHeader()).getSampleCount()));
            data.add(row);

            row = new Vector();
            row.add(PHYSICAL_SAMPLES);
            row.add(IntPool.get(((SamplePackage.Header) getHeader()).getPhysicalSampleCount()));
            data.add(row);
        }
        return data;
    }
}
