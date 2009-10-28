package com.pcmsolutions.gui.desktop;

/**
 * User: paulmeehan
 * Date: 12-Jun-2004
 * Time: 19:00:27
 */
public interface SessionableComponent {    
    String retrieveComponentSession();
    void restoreComponentSession(String sessStr);
}
