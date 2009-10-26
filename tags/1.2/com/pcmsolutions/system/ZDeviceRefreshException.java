/*
 * IllegalParameterId.java
 *
 * Created on January 2, 2003, 7:41 PM
 */

package com.pcmsolutions.system;

/**
 *
 * @author  pmeehan
 */
public class ZDeviceRefreshException extends ZDeviceException {

    public ZDeviceRefreshException(ZExternalDevice device, String msg) {
        super(device, msg);
    }

}
