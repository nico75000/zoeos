package com.pcmsolutions.system.callback;

/**
 * User: paulmeehan
 * Date: 23-Jan-2004
 * Time: 11:39:02
 */
public interface CallbackTaskResult {
    public boolean suceeded();

    public Exception getException();

    public Object getResult();
}
