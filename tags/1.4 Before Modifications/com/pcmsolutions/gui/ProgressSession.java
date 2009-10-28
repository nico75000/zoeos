package com.pcmsolutions.gui;

/**
 * User: paulmeehan
 * Date: 24-Feb-2004
 * Time: 04:11:04
 */
public interface ProgressSession {
    public void updateTitle(String title);
    public void end();
    public boolean isActive();
    public void updateStatus(int status);
    public void setIndeterminate(boolean ind);
    public void updateStatus();
    public boolean isCancelled();
}
