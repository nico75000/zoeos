/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.gui.IconAndTipCarrier;


/**
 *
 * @author  pmeehan
 */

public interface ReadableSample extends SampleModel, IconAndTipCarrier {
    // EVENTS
    public void addSampleListener(SampleListener pl);

    public void removeSampleListener(SampleListener pl);

    public ReadableSample getMostCapableNonContextEditableSampleDowngrade();

    // UTILITY
    public DeviceContext getDeviceContext();

    public void setToStringFormatExtended(boolean extended);

    // SAMPLE
    public void refreshSample() throws NoSuchSampleException;

    public void lockSampleRead() throws NoSuchSampleException, NoSuchContextException;

    public void unlockSample();

    public boolean isSampleInitialized() throws NoSuchSampleException;

    public int getSampleState() throws NoSuchSampleException;

    public double getInitializationStatus() throws NoSuchSampleException, SampleEmptyException;

    public boolean isSampleWriteLocked() throws NoSuchSampleException, SampleEmptyException;

    public String getSampleName() throws NoSuchSampleException, SampleEmptyException;

    public String getSampleDisplayName() throws NoSuchSampleException;

    public Integer getSampleNumber();
}
