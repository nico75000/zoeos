package com.pcmsolutions.gui;

/**
 * User: paulmeehan
 * Date: 15-Jan-2004
 * Time: 04:13:51
 */
public interface ProgressUpdater {
    // fraction 0..1
    // < 0 signifies inactive
    public void setProgress(double p);
}
