package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.callback.Callback;

/**
 * User: paulmeehan
 * Date: 08-Aug-2004
 * Time: 16:03:04
 */
public interface PostableTicket {
    // returns immediately
    void post() throws ResourceUnavailableException;

    // returns immediately, guarantees a callback to cb with a possible exception
    void post(Callback cb) throws ResourceUnavailableException;

    public boolean isExpired();
    public boolean isFinished();
    public boolean isCancelled();

    public void cancelPost();
}
