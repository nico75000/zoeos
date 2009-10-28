package com.pcmsolutions.system.tasking;


/**
 * User: paulmeehan
 * Date: 07-Aug-2004
 * Time: 13:59:07
 */
abstract class AbstractTicket extends AbstractTask {
    private boolean expired = false;
    private boolean finished = false;
    private boolean cancelled = false;
    private Exception exception = null;

    protected AbstractTicket(String name, TaskQ tq) {
        super(name, tq);
    }

    public final synchronized void postTask(Task t) throws QueueUnavailableException, TicketExpiredException {
        if ( expired)
            throw new TicketExpiredException("ticket expired");
        this.getQueue().postTask(t);
        expired = true;
    }

    public final synchronized void cbCancelled() {
        if (!finished && !cancelled) {
            cancelled = true;
            exception = new ResourceUnavailableException("Operation cancelled");
            handleTaskCallback();
        }
    }

    public final synchronized void cbFinished(Exception e) {
        if (!finished) {
            finished = true;
            exception = e;
            handleTaskCallback();
        }
    }

    abstract void handleTaskCallback();

    public final synchronized Exception getException() {
        return exception;
    }

    public final synchronized boolean isExpired() {
        return expired;
    }

    public final synchronized boolean isFinished() {
        return finished;
    }

    public final synchronized boolean isCancelled() {
        return cancelled;
    }
}
