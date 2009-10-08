/*
 * DeviceListener.java
 *
 * Created on January 27, 2003, 10:29 PM
 */

package com.pcmsolutions.device.EMU.E4.master;


import com.pcmsolutions.device.EMU.E4.events.MasterChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.MasterRefreshedEvent;

/**
 *
 * @author  pmeehan
 */
public interface MasterListener {
    public void masterChanged(MasterChangedEvent ev);

    public void masterRefreshed(MasterRefreshedEvent ev);
}
