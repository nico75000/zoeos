package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.tasking.TaskQ;
import com.pcmsolutions.system.tasking.TicketedQ;

/**
 * User: paulmeehan
 * Date: 09-Aug-2004
 * Time: 10:00:38
 */
public interface DeviceQueues {
    public TicketedQ generalQ();

    public TicketedQ refreshQ();

    public TicketedQ zCommandQ();

    public TicketedQ parameterQ();

    public TicketedQ auditionQ();

    public TicketedQ ddQ();

    public TicketedQ externalizationQ();

    public TicketedQ presetContextQ();
}
