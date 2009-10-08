package com.pcmsolutions.license;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 03-Nov-2003
 * Time: 12:30:58
 * To change this template use Options | File Templates.
 */
public class InvalidLicenseKeyException extends Exception {
    public InvalidLicenseKeyException() {
    }

    public InvalidLicenseKeyException(Throwable cause) {
        super(cause);
    }

    public InvalidLicenseKeyException(String message) {
        super(message);
    }

    public InvalidLicenseKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
