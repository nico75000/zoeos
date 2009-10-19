package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 21:40:56
 * To change this template use Options | File Templates.
 */
public class SmdiNoMemoryException extends Exception {
    public SmdiNoMemoryException(String message) {
        super(message);
    }

    public SmdiNoMemoryException() {
        super("out of sample memory");
    }
}
