package com.pcmsolutions.system.callback;

/**
 * User: paulmeehan
 * Date: 07-Aug-2004
 * Time: 17:11:33
 */
public interface Callback {
    void result(Exception e, boolean wasCancelled);
}
