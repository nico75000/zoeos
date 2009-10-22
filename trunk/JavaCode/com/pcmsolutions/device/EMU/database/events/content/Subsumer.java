package com.pcmsolutions.device.EMU.database.events.content;

/**
 * User: paulmeehan
 * Date: 03-Sep-2004
 * Time: 19:08:56
 */
public interface Subsumer {
    boolean subsumes(ContentEvent ev);
}
