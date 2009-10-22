package com.pcmsolutions.device.EMU.database;

import com.pcmsolutions.device.EMU.database.events.context.ContextAdditionEvent;
import com.pcmsolutions.device.EMU.database.events.context.ContextReleaseEvent;
import com.pcmsolutions.device.EMU.database.events.context.ContextRemovalEvent;


/**
 * User: paulmeehan
 * Date: 12-Aug-2004
 * Time: 15:50:59
 */
public interface ContextListener {
    public void removalFromContext(ContextRemovalEvent ev);

    public void additionToContext(ContextAdditionEvent ev);

    public void contextReleased(ContextReleaseEvent ev);
}
