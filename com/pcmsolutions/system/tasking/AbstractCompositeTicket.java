package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.callback.Callback;

/**
 * User: paulmeehan
 * Date: 08-Aug-2004
 * Time: 16:29:46
 */
abstract class AbstractCompositeTicket extends AbstractTask implements Ticket {
    AbstractTicket ticket;

    public AbstractCompositeTicket(String name, TaskQ queue) {
        super(name, queue);
    }

    void testExpired() throws TicketExpiredException {
        if (ticket != null && ticket.isExpired())
            throw new TicketExpiredException("ticket expired");
    }

    // returns immediately
    public synchronized void post() throws ResourceUnavailableException {
        testExpired();
        ticket = new AbstractPostableTicket(getName(), getQueue()) {
            public void run() throws Exception {
                if (!isCancelled() && !isFinished())
                    AbstractCompositeTicket.this.run();
            }
        };
        ((AbstractPostableTicket) ticket).post();
    }

    // returns immediately, guarantees a callback to cb with a possible exception
    public synchronized void post(Callback cb) throws ResourceUnavailableException {
        testExpired();
        ticket = new AbstractPostableTicket(getName(), getQueue()) {
            public void run() throws Exception {
                if (!isCancelled() && !isFinished())
                    AbstractCompositeTicket.this.run();
            }
        };
        ((AbstractPostableTicket) ticket).post(cb);
    }

    public synchronized boolean isExpired() {
        return ticket != null && ticket.isExpired();
    }

    public synchronized boolean isFinished() {
        return ticket != null && ticket.isFinished();
    }

    public synchronized boolean isCancelled() {
        return ticket != null && ticket.isCancelled();
    }

    // blocks, throwing an exception if there is one
    // optionally time out after timeout ms, 0 specifies no timeout i.e wait until operation finishes (exception or not)
    public synchronized void send(long timeout) throws ResourceUnavailableException, Exception {
        testExpired();
        ticket = new AbstractSendableTicket(getName(), getQueue()) {
            public void run() throws Exception {
                if (!isCancelled() && !isFinished())
                    AbstractCompositeTicket.this.run();
            }
        };
        ((AbstractSendableTicket) ticket).send(timeout);
    }

    public final synchronized void cbCancelled() {
        if (ticket != null)
            ticket.cbCancelled();
    }

    public final synchronized void cbFinished(Exception e) {
        if (ticket != null)
            ticket.cbFinished(e);
    }

    public final synchronized void cancelPost() {
        if (ticket instanceof PostableTicket)
            ((PostableTicket) ticket).cancelPost();
    }
}
