package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.E4.packaging.PresetPackage;
import com.pcmsolutions.device.EMU.E4.packaging.SamplePackage;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Jan-2004
 * Time: 10:17:37
 * To change this template use Options | File Templates.
 */
public class PresetPackageHeaderInfoPane extends PackageHeaderInfoPane {
    protected SamplePackage.Header sampleHeader = null;

    public PresetPackageHeaderInfoPane(PresetPackage.Header hdr) {
        super(hdr);
    }

    public SamplePackage.Header getSampleHeader() {
        return sampleHeader;
    }

    public void setSampleHeader(SamplePackage.Header sampleHeader) {
        this.sampleHeader = sampleHeader;
        generateText();
    }

    protected void generateText() {
        if (header == null) {
            setText("");
            return;
        }

        super.generateText();

        StringBuffer buf = new StringBuffer();

        buf.append(ZUtilities.makeExactLengthString("Preset Count:", DESC_FIELD_LEN) + ((PresetPackage.Header) header).getPresetCount());
        buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("Inc Master:", DESC_FIELD_LEN) + ((PresetPackage.Header) header).isIncludingMasterSettings());
        buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("Inc Multimode:", DESC_FIELD_LEN) + ((PresetPackage.Header) header).isIncludingMultimodeSettings());
        buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("Inc Samples:", DESC_FIELD_LEN) + ((PresetPackage.Header) header).isIncludingSamples());
        buf.append(Zoeos.lineSeperator);

        if (sampleHeader != null) {
            buf.append(ZUtilities.makeExactLengthString("Sample Count:", DESC_FIELD_LEN) + ((SamplePackage.Header) sampleHeader).getSampleCount());
            buf.append(Zoeos.lineSeperator);

            buf.append(ZUtilities.makeExactLengthString("Physical Samples:", DESC_FIELD_LEN) + ((SamplePackage.Header) sampleHeader).getPhysicalSampleCount());
            buf.append(Zoeos.lineSeperator);
        }
        setText(getText() + buf.toString());
    }
}
