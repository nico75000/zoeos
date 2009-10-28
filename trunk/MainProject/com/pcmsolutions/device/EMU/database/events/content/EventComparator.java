package com.pcmsolutions.device.EMU.database.events.content;

/**
 * User: paulmeehan
 * Date: 04-Sep-2004
 * Time: 12:50:35
 */
public interface EventComparator {
    boolean independentOf(ContentEvent ev);
    boolean subsumes(ContentEvent ev);
}
