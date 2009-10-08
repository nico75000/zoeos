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
public class ZDeviceNotInitializedException extends ZDeviceException {

    public ZDeviceNotInitializedException(ZExternalDevice device, String msg) {
        super(device, msg);
    }
}
