package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 21:38:45
 * To change this template use Options | File Templates.
 */
public class SmdiFileOpenException extends Exception {
    public SmdiFileOpenException(String message) {
        super(message);
    }

    public SmdiFileOpenException() {
        super("could not open File");
    }
}
