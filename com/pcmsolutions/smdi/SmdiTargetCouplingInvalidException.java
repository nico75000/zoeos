package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 11-Sep-2003
 * Time: 22:42:10
 * To change this template use Options | File Templates.
 */
public class SmdiTargetCouplingInvalidException extends Exception {
    public SmdiTargetCouplingInvalidException(String message) {
        super(message);
    }

    public SmdiTargetCouplingInvalidException() {
        super("SMDI coupling for device is not valid");
    }
}
