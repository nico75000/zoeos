package com.pcmsolutions.device.EMU.E4.gui.packaging;

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
public class SamplePackageHeaderInfoPane extends PackageHeaderInfoPane {
    public SamplePackageHeaderInfoPane(SamplePackage.Header hdr) {
        super(hdr);
    }

    protected void generateText() {
        if (header == null) {
            setText("");
            return;
        }

        super.generateText();

        StringBuffer buf = new StringBuffer();

        buf.append(ZUtilities.makeExactLengthString("Sample Count:", DESC_FIELD_LEN) + ((SamplePackage.Header) header).getSampleCount());
        buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("Physical Samples:", DESC_FIELD_LEN) + ((SamplePackage.Header) header).getPhysicalSampleCount());
        buf.append(Zoeos.lineSeperator);

        setText(getText() + buf.toString());
    }
}
