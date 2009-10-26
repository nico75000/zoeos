package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 21:38:45
 * To change this template use Options | File Templates.
 */
public class SmdiUnknownFileFormatException extends Exception {
    public SmdiUnknownFileFormatException(String message) {
        super(message);
    }

    public SmdiUnknownFileFormatException() {
        super("unknown File format");
    }
}
