package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.Zoeos;

/**
 * User: paulmeehan
 * Date: 07-Aug-2004
 * Time: 13:59:07
 */
abstract class AbstractSendableTicket extends AbstractTicket implements SendableTicket {
    protected AbstractSendableTicket(String name, TaskQ tq) {
        super(name, tq);
    }

    // 0 specifies no timeout
    public final synchronized void send(long timeout) throws Exception, TicketExpiredException {
        if ( this.getQueue().isReentering())
            throw new IllegalArgumentException("TaskQ task cannot re-send to TaskQ");
        if (timeout < 0)
            timeout = 0;
        postTask(this);
        long start = Zoeos.getZoeosTime();
        if (timeout == 0)
            while (!isFinished())
                try {
                    wait();
                } catch (InterruptedException e) {
                }
        else
            while (!isFinished() && (Zoeos.getZoeosTime() - start < timeout)) {
                try {
                    wait(timeout);
                } catch (InterruptedException e) {
                }
            }
        if (getException() != null)
            throw getException();
    }

    final void handleTaskCallback() {
        notifyAll();
    }
}
