package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 21:38:45
 * To change this template use Options | File Templates.
 */
public class SmdiUnsupportedConversionException extends Exception {
    public SmdiUnsupportedConversionException(String message) {
        super(message);
    }

    public SmdiUnsupportedConversionException() {
        super("Required audio conversion not supported");
    }
}
