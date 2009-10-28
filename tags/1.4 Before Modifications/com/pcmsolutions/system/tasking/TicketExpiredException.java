package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 08-Aug-2004
 * Time: 14:59:00
 */
public class TicketExpiredException extends ResourceUnavailableException{
    public TicketExpiredException(String message) {
        super(message);
    }
}
