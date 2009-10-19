package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 21:46:23
 * To change this template use Options | File Templates.
 */
public class SmdiGeneralException extends Exception {
    public SmdiGeneralException(String message) {
        super(message);
    }

    public SmdiGeneralException() {
        super("unknown SMDI problem");
    }
}
