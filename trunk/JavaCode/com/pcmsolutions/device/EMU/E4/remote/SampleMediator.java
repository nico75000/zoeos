package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.SampleDownloadDescriptor;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.smdi.SmdiTransferAbortedException;
import com.pcmsolutions.smdi.SmdiSampleEmptyException;

import java.io.File;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 25-Sep-2003
 * Time: 05:05:17
 * To change this template use Options | File Templates.
 */
public interface SampleMediator extends Serializable {

    public File retrieveSample(SampleDownloadDescriptor sri, ProgressCallback prog) throws SampleMediationException, DeviceException;

    public void sendSample(Integer sample, File fn, String sampleName, boolean obeyMaxSampleRate, ProgressCallback prog) throws SampleMediationException, SmdiSampleEmptyException, SmdiTransferAbortedException, DeviceException;

    public void sendSampleMulti(File fn, Integer[] destSamples, String[] destNames, ProgressCallback prog) throws SampleMediationException, SmdiSampleEmptyException, SmdiTransferAbortedException, DeviceException;

    public void sendSample(IsolatedSample is, Integer[] destSamples, String[] destNames, ProgressCallback prog) throws SampleMediationException, IsolatedSampleUnavailableException, SmdiSampleEmptyException, SmdiTransferAbortedException, DeviceException;

    public void copySample(Integer srcSample, Integer[] destSamples, String[] destNames, ProgressCallback prog) throws SampleMediationException, SmdiSampleEmptyException, SmdiTransferAbortedException, DeviceException;

    public void sendSample(Integer sample, IsolatedSample is, String sampleName, ProgressCallback prog) throws SampleMediationException, IsolatedSampleUnavailableException, SmdiTransferAbortedException, DeviceException;

    public SampleDescriptor getSampleDescriptor(Integer sample) throws SampleMediationException, SmdiSampleEmptyException;

    public class SampleMediationException extends Exception {
        public SampleMediationException(String message) {
            super(message);
        }
    };
}
