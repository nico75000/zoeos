/*
 * DeviceMarshall.java
 *
 * Created on November 17, 2002, 8:54 PM
 */

package com.pcmsolutions.device;

import com.pcmsolutions.system.ZExternalDevice;


/**
 *
 * @author  pmeehan
 */
public interface DeviceMarshall {
    public boolean understandsClass(Class c);

    public ZExternalDevice tryIdentify(Object msg) throws IllegalArgumentException;
}
