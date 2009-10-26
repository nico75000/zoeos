/*
 * PresetListener.java
 *
 * Created on January 16, 2003, 8:33 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.E4.events.*;


/**
 *
 * @author  pmeehan
 */
public interface SampleListener {
    public void sampleInitialized(SampleInitializeEvent ev);

    public void sampleRefreshed(SampleRefreshEvent ev);

    public void sampleChanged(SampleChangeEvent ev);

    public void sampleNameChanged(SampleNameChangeEvent ev);

    public void sampleInitializationStatusChanged(SampleInitializationStatusChangedEvent ev);

}
