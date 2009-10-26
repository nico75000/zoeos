/*
 * ProgressMultiBox.java
 *
 * Created on February 14, 2003, 7:20 PM
 */

package com.pcmsolutions.gui;

/**
 *
 * @author  pmeehan
 */
public interface ProgressBox {
    public void setMaximum(int max);

    public void updateElement(Object e, int status);

    public void updateElement(Object e);

    public void updateElement(Object e, String title);

    public void updateTitle(Object e, String title);
}
