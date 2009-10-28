package com.pcmsolutions.device.EMU.database.events.context;

import java.util.EventObject;

/**
 * User: paulmeehan
 * Date: 12-Aug-2004
 * Time: 16:06:54
 */
abstract class ContextEvent extends EventObject {
    public ContextEvent(Object source) {
        super(source);
    }
}
