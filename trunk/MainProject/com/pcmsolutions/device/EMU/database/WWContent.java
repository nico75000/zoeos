package com.pcmsolutions.device.EMU.database;

/**
 * User: paulmeehan
 * Date: 11-Aug-2004
 * Time: 23:56:30
 */
public interface WWContent <C> {
    C getWritable1();

    C getWritable2();

    void release();
}
