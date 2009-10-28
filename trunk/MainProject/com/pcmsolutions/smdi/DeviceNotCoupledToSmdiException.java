package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 14-Sep-2003
 * Time: 00:13:21
 * To change this template use Options | File Templates.
 */
public class DeviceNotCoupledToSmdiException extends Exception {
    public DeviceNotCoupledToSmdiException(String message) {
        super(message);
    }

    public DeviceNotCoupledToSmdiException() {
        super("device is not SMDI coupled");
    }
}
