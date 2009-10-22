package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.sample.SampleContextUserLocationCombo;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.tasking.TicketRunnable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyBlockContextSamplesZMTC extends AbstractContextEditableSampleZMTCommand {
    private static final ZCommandField<SampleContextUserLocationCombo, ContextLocation> destinationField = new AbstractZCommandField<SampleContextUserLocationCombo, ContextLocation>(new SampleContextUserLocationCombo(), "Destination", "Destination sample") {
        public ContextLocation getValue() {
            return getComponent().getSelectedLocation();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("Copy samples as a block", new ZCommandField[]{destinationField});
    }

    public String getPresentationString() {
        return "Copy as block";
    }

    public String getDescriptiveString() {
        return "Copy samples as a block";
    }

    public int getMinNumTargets() {
        return 2;
    }

    public String getMenuPathString() {
        return ";Copy";
    }

    public boolean handleTarget(ContextEditableSample sample, int total, int curr) throws Exception {
        destinationField.getComponent().init(sample.getSampleContext());
        destinationField.getComponent().selectFirstEmptyLocation();
        final ContextEditableSample[] samples = getTargets().toArray(new ContextEditableSample[numTargets()]);
        cmdDlg.run(new ZCommandDialog.Executable() {
            public void execute() throws Exception {
                doBlockCopy(samples, destinationField.getValue().getIndex().intValue());
            }
        });
        return false;
    }

    protected void doBlockCopy(final ContextEditableSample[] samples, final int destIndex) throws Exception {
        final Integer[] srcIndexes = ZUtilities.extractIndexes(samples);
        final Integer[] destIndexes = ZUtilities.fillIncrementally(new Integer[samples.length], destIndex);
        samples[0].getSampleContext().getDeviceContext().getQueues().zCommandQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                SampleContextMacros.copySamples(samples[0].getSampleContext(), srcIndexes, destIndexes, false, true, "Sample block copy");
            }
        }, "sample block copy");
    }
}

