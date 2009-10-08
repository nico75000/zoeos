package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 21:38:45
 * To change this template use Options | File Templates.
 */
public class SmdiNoSampleException extends Exception {
    public SmdiNoSampleException(String message) {
        super(message);
    }

    public SmdiNoSampleException() {
        super("no sample at specified location");
    }
}
