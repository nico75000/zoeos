package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 09-Aug-2004
 * Time: 10:21:01
 */
public interface TicketedQ {
    Ticket getTicket(final TicketRunnable r, String taskName);

    PostableTicket getPostableTicket(final TicketRunnable r, String taskName);

    SendableTicket getSendableTicket(final TicketRunnable r, String taskName);

    Ticket getTicket(final TicketRunnable r, String taskName, boolean isCritical);

    PostableTicket getPostableTicket(final TicketRunnable r, String taskName, boolean isCritical);

    SendableTicket getSendableTicket(final TicketRunnable r, String taskName, boolean isCritical);

    SyncTicket getSynchronizationTicket(final String taskName);
}
