package com.pcmsolutions.gui.desktop;

/**
 * User: paulmeehan
 * Date: 17-May-2004
 * Time: 11:29:34
 */
public interface ViewMessageReceiver {
    void receiveMessage(String msg);
    boolean testCondition(String condition);
}
