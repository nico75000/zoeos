package com.pcmsolutions.device.EMU.database;

/**
 * User: paulmeehan
 * Date: 13-Aug-2004
 * Time: 21:35:44
 */
public class ContentUnavailableException extends Exception{
    public ContentUnavailableException() {
        super("Content unavailable");
    }

    public ContentUnavailableException(String message) {
        super(message);
    }
}
