package com.pcmsolutions.device.EMU.database;

/**
 * User: paulmeehan
 * Date: 11-Aug-2004
 * Time: 23:56:30
 */
public interface RWContent <C> {
    C getReadable();

    C getWritable();

    void release();
}
