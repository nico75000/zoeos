/*
 * PresetListener.java
 *
 * Created on January 15, 2003, 10:55 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;


/**
 *
 * @author  pmeehan
 */
public interface SampleContextListener {
    public void samplesRemovedFromContext(SampleContext pc, Integer[] samples);

    public void samplesAddedToContext(SampleContext pc, Integer[] samples);

    public void contextReleased(SampleContext pc);
}
