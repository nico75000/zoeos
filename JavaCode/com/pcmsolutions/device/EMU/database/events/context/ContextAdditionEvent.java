package com.pcmsolutions.device.EMU.database.events.context;

/**
 * User: paulmeehan
 * Date: 12-Aug-2004
 * Time: 16:09:13
 */
public class ContextAdditionEvent extends ContextModifyEvent {
    public ContextAdditionEvent(Object source, Integer[] indexes) {
        super(source, indexes);
    }
}
