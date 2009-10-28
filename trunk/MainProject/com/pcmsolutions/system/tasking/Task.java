package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 07-Aug-2004
 * Time: 11:03:12
 */
public interface Task {
    public void run() throws Exception;
    public void cbCancelled();
    public String getName();
    public void cbFinished(Exception e);
    //public boolean isCritical();
}
