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
public interface ProgressMultiBox {
    public static final String PROGRESS_DONE_TITLE = "Done. ";

    public void newElement(Object e, String title, int maximum);

    public void updateElement(Object e, int status);

    public void updateElement(Object e);

    public void setElementIndeterminate(Object e, boolean b);

    public void updateElement(Object e, String title);

    public void updateTitle(Object e, String title);

    public void killElement(Object e);

    public void kill();

    public void centreAboutFrame();

    public void show();

    public void hide();

    public void setShowable(boolean s);

    public boolean getShowable();
}
