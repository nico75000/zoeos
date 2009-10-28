/*
 * NoSuchLinkException.java
 *
 * Created on January 4, 2003, 4:56 PM
 */

package com.pcmsolutions.system;


/**
 *
 * @author  pmeehan
 */
public class ZDeviceCannotBeRemovedException extends ZDeviceException {

    public ZDeviceCannotBeRemovedException(ZExternalDevice device, String msg) {
        super(device, msg);
    }
}
