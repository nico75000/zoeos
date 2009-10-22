/*
 * ZViewProvider.java
 *
 * Created on January 21, 2003, 10:23 PM
 */

package com.pcmsolutions.system;

/**
 *
 * @author  pmeehan
 */
public interface ZCommandProvider {
    public ZCommand[] getZCommands(Class markerClass);
    // most capable/super first
    public Class[] getZCommandMarkers();
}
