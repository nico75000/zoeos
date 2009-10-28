package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 09-Aug-2004
 * Time: 09:08:54
 */
public interface TaskQ<T extends Task> {
    void postTask(T t) throws QueueUnavailableException;

    String getName();

    boolean isReentering();
}
