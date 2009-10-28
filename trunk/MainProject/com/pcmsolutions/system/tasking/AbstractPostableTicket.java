package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.callback.Callback;

/**
 * User: paulmeehan
 * Date: 07-Aug-2004
 * Time: 13:59:07
 */
abstract class AbstractPostableTicket extends AbstractTicket implements PostableTicket {
    Callback cb = null;

    protected AbstractPostableTicket(String name, TaskQ tq) {
        super(name, tq);
    }

    // returns immediately
    public void post() throws ResourceUnavailableException {
        postTask(this);
    }

    public synchronized void post(Callback cb) throws ResourceUnavailableException {
        this.cb = cb;
        post();
    }

    final synchronized void handleTaskCallback() {
        if (cb != null)
            try {
                cb.result(getException(), isCancelled());
            } catch (Exception e) {
                e.printStackTrace();
            }
        else {
            Exception e = getException();
            if (e != null)
                e.printStackTrace();
        }
    }

    public void cancelPost(){
        cbCancelled();
    }
}
