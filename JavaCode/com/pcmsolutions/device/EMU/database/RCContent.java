package com.pcmsolutions.device.EMU.database;

/**
 * User: paulmeehan
 * Date: 11-Aug-2004
 * Time: 23:56:30
 */
public interface RCContent <C> {
    C getReadable();

    void release();
}
