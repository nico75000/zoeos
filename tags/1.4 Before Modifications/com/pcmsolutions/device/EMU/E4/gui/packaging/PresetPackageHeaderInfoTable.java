package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.E4.packaging.PresetPackage;
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
class PresetPackageHeaderInfoTable extends PackageHeaderInfoTable {
    private SamplePackage.Header sampleHeader = null;

    public PresetPackageHeaderInfoTable(PresetPackage.Header hdr) {
        super(hdr);
    }

    public SamplePackage.Header getSampleHeader() {
        return sampleHeader;
    }

    public void setSampleHeader(SamplePackage.Header sampleHeader) {
        this.sampleHeader = sampleHeader;
        setHeader(getHeader());
    }

    private static final String PRESET_COUNT = "Preset count";
    private static final String INC_MASTER = "Includes master";
    private static final String INC_MULTIMODE = "Includes multimode";
    private static final String INC_SAMPLES = "Includes samples";
    private static final String SAMPLE_COUNT = "Sample count";
    private static final String PHYSICAL_SAMPLES = "Physical samples";

    protected Vector getDataVector() {
        Vector data = super.getDataVector();
        Vector row;
        if (getHeader() != null) {
            row = new Vector();
            row.add(PRESET_COUNT);
            row.add(IntPool.get(((PresetPackage.Header) getHeader()).getPresetCount()));
            data.add(row);

            if (getSampleHeader() != null) {
                row = new Vector();
                row.add(SAMPLE_COUNT);
                row.add(IntPool.get(((SamplePackage.Header) getSampleHeader()).getSampleCount()));
                data.add(row);

                row = new Vector();
                row.add(PHYSICAL_SAMPLES);
                row.add(IntPool.get((((SamplePackage.Header) getSampleHeader()).getPhysicalSampleCount())));
                data.add(row);

            } else {
                row = new Vector();
                row.add(INC_SAMPLES);
                row.add(new Boolean(((PresetPackage.Header) getHeader()).isIncludingSamples()));
                data.add(row);
            }

            row = new Vector();
            row.add(INC_MASTER);
            row.add(new Boolean(((PresetPackage.Header) getHeader()).isIncludingMasterSettings()));
            data.add(row);

            row = new Vector();
            row.add(INC_MULTIMODE);
            row.add(new Boolean(((PresetPackage.Header) getHeader()).isIncludingMultimodeSettings()));
            data.add(row);
        }
        return data;
    }
}
