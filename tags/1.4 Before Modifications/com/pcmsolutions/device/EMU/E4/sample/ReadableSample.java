/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.E4.AuditionManager;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMultimodeChannelException;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadableSampleZCommandMarker;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.ContextElement;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.AuditioningDisabledException;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.tasking.Ticket;


/**
 *
 * @author  pmeehan
 */

public interface ReadableSample extends ContextElement, SampleModel, IconAndTipCarrier {
    final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ReadableSampleZCommandMarker.class);

    // EVENTS
    public void addListener(SampleListener pl) throws DeviceException;

    public void removeListener(SampleListener pl);

    public ReadableSample getMostCapableNonContextEditableSampleDowngrade();

    // AUDITION
    public Ticket audition();

    // UTILITY
    public DeviceContext getDeviceContext();

    public void setToStringFormatExtended(boolean extended);

    public void assertInitialized() throws SampleException;

    // SAMPLE
    public void refresh() throws SampleException;

    public boolean isSampleInitialized() throws SampleException, DeviceException;

   // public double getInitializationStatus() throws SampleException, EmptyException;

  //  public boolean isSampleWriteLocked() throws DeviceException, EmptyException;

    public String getName() throws SampleException, EmptyException;

    public String getString() throws SampleException;

    public String getDisplayName();

    public Integer getIndex();

    public boolean isUser();

    public boolean isEmpty() throws SampleException;

    public boolean isPending() throws SampleException;

    public boolean isInitializing() throws SampleException;

    public boolean isInitialized() throws SampleException;

}
