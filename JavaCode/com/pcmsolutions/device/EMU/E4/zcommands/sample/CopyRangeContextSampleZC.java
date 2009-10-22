package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.E4.gui.sample.SampleContextUserLocationCombo;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.gui.ProgressCallbackTree;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyRangeContextSampleZC extends AbstractContextEditableSampleZCommand {

    private static final ZCommandField<SampleContextUserLocationCombo, ContextLocation> lowDestinationField = new AbstractZCommandField<SampleContextUserLocationCombo, ContextLocation>(new SampleContextUserLocationCombo(), "Low destination", "Low destination sample") {
        public ContextLocation getValue() {
            return getComponent().getSelectedLocation();
        }
    };
    private static final ZCommandField<SampleContextUserLocationCombo, ContextLocation> highDestinationField = new AbstractZCommandField<SampleContextUserLocationCombo, ContextLocation>(new SampleContextUserLocationCombo(), "High destination", "High destination sample") {
        public ContextLocation getValue() {
            return getComponent().getSelectedLocation();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Copy to sample range", new ZCommandField[]{lowDestinationField, highDestinationField});
    }

    public String getPresentationString() {
        return "Copy to range";
    }

    public String getDescriptiveString() {
        return "Copy sample to all sample locations in specified range";
    }

    public String getMenuPathString() {
        return ";Copy";
    }

    public boolean handleTarget(final ContextEditableSample s, int total, int curr) throws Exception {
        lowDestinationField.getComponent().init(s.getSampleContext());
        lowDestinationField.getComponent().selectFirstEmptyLocation();
        highDestinationField.getComponent().init(s.getSampleContext());
        highDestinationField.getComponent().selectFirstEmptyLocation();

        return cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                Integer lowSample = lowDestinationField.getValue().getIndex();
                Integer highSample = highDestinationField.getValue().getIndex();

                if (lowSample.intValue() > highSample.intValue())
                    throw new CommandFailedException("Invalid range");

                int num = highSample.intValue() - lowSample.intValue() + 1;
                Integer[] destIndexes = ZUtilities.fillIncrementally(new Integer[num], lowSample.intValue());

                s.copySample(destIndexes, new ProgressCallbackTree("Sample range copy", false) {
                    public String finalizeString(String str) {
                        return s.getDeviceContext().makeDeviceProgressTitle(str);
                    }
                });
            }
        }, total, curr) != ZCommandDialog.CANCELLED_ALL;
    }
}


