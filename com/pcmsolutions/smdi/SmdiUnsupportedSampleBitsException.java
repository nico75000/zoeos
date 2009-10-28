package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 21:40:38
 * To change this template use Options | File Templates.
 */
public class SmdiUnsupportedSampleBitsException extends Exception {
    public SmdiUnsupportedSampleBitsException(String message) {
        super(message);
    }

    public SmdiUnsupportedSampleBitsException() {
        super("Unsupported bit resolution");
    }
}
