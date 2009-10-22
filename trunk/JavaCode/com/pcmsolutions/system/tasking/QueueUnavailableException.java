package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 07-Aug-2004
 * Time: 11:04:37
 */
public class QueueUnavailableException extends ResourceUnavailableException{
    String name;
    public QueueUnavailableException(String name, String msg) {
        super(msg);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
