package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.ResourceException;

/**
 * User: paulmeehan
 * Date: 08-Aug-2004
 * Time: 13:42:14
 */
public class ResourceUnavailableException extends ResourceException{
    public ResourceUnavailableException(String message) {
        super(message);
    }

    public ResourceUnavailableException() {
        super("Resource unavailable");
    }
}
