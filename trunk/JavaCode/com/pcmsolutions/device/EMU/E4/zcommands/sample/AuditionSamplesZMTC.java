package com.pcmsolutions.device.EMU.E4.zcommands.sample;


import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.callback.Callback;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:40:54
 * To change this template use Options | File Templates.
 */
public class AuditionSamplesZMTC extends AbstractReadableSampleZMTCommand {
    boolean stepped;

    public AuditionSamplesZMTC() {
        this(false);
    }

    public AuditionSamplesZMTC(boolean stepped) {
        this.stepped = stepped;
    }

    public ZMTCommand getNextMode() {
        if (!stepped)
            return new AuditionSamplesZMTC(true);
        else
            return null;
    }

    public String getPresentationString() {
        return (stepped ? "Audition [Stepped]" : "Audition");
    }

    public String getDescriptiveString() {
        return (stepped ? "Audition samples individually in sequenece" : "Audition samples ");
    }

    public boolean handleTarget(ReadableSample readableSample, int total, int curr) throws Exception {
        ReadableSample[] samples = getTargets().toArray(new ReadableSample[numTargets()]);
        samples[0].getDeviceContext().getDefaultPresetContext().auditionSamples(SampleContextMacros.extractSampleIndexes(samples), stepped).post(new Callback() {
            public void result(Exception e, boolean wasCancelled) {
                if (e != null && !wasCancelled)
                    UserMessaging.showCommandFailed(e.getMessage());
            }
        });
        return false;
    }

    public int getMinNumTargets() {
        if (stepped)
            return 2;
        else
            return 1;
    }

    public int getMaxNumTargets() {
        return (stepped ? Integer.MAX_VALUE : 128);
    }
}

