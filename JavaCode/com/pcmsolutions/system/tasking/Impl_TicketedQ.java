package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 09-Aug-2004
 * Time: 10:29:04
 */
abstract class Impl_TicketedQ extends Impl_TaskQ implements ManageableTicketedQ {

    public Impl_TicketedQ(Object owner, String name, int priority) {
        super(owner, name, priority);
    }

    public Ticket getTicket(TicketRunnable r, String taskName) {
        return TicketFactory.createCompositeTicket(r, taskName, this);
    }

    public PostableTicket getPostableTicket(TicketRunnable r, String taskName) {
        return TicketFactory.createPostableTicket(r, taskName, this);
    }

    public SendableTicket getSendableTicket(TicketRunnable r, String taskName) {
        return TicketFactory.createSendableTicket(r, taskName, this);
    }


    public Ticket getTicket(TicketRunnable r, String taskName, boolean isCritical) {
        return TicketFactory.createCompositeTicket(r, taskName, this);
    }

    public PostableTicket getPostableTicket(TicketRunnable r, String taskName, boolean isCritical) {
        return TicketFactory.createPostableTicket(r, taskName, this);
    }

    public SendableTicket getSendableTicket(TicketRunnable r, String taskName, boolean isCritical) {
        return TicketFactory.createSendableTicket(r, taskName, this);
    }

    public SyncTicket getSynchronizationTicket(String taskName) {
        return TicketFactory.createSynchronizationTicket(taskName, this);
    }
}
