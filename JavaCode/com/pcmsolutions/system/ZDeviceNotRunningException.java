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
public class ZDeviceNotRunningException extends ZDeviceException {

    public ZDeviceNotRunningException(ZExternalDevice device, String msg) {
        super(device, msg);
    }
}
