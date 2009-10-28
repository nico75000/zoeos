package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 07-Aug-2004
 * Time: 11:03:54
 */
public interface ManageableTaskQ<T extends Task> extends TaskQ<T>{
    void stop(boolean flush); // negates pause
    void start();
    public void pause();     
    public void resume();
    public void cancel();
    Object getOwner();
    public void waitUntilEmpty();
}
