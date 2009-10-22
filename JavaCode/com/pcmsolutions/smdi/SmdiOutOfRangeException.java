package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 21:38:02
 * To change this template use Options | File Templates.
 */
public class SmdiOutOfRangeException extends Exception {
    public SmdiOutOfRangeException(String message) {
        super(message);
    }

    public SmdiOutOfRangeException() {
        super("Sample index outside supported range");
    }
}
