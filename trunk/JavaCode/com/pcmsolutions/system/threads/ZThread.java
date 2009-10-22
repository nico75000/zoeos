package com.pcmsolutions.system.threads;

/**
 * User: paulmeehan
 * Date: 02-Sep-2004
 * Time: 13:58:55
 */
public interface ZThread {
    public interface OnCompletedAction {
           public void completed(ZThread t);
       }    

    void performCompletedActions();

    // should only be called on this thread
    void addOnCompletedAction(Impl_ZThread.OnCompletedAction ca);

    boolean isShouldRun();

    void stopThreadSafely();

    void start();

    void setPriority(int priority);
}
