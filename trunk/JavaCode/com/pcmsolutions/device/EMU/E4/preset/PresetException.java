/*
 * NoSuchPresetException.java
 *
 * Created on January 4, 2003, 5:05 PM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.system.IntPool;

/**
 *
 * @author  pmeehan
 */
public class PresetException extends Exception {
    protected Integer preset = IntPool.get(Integer.MIN_VALUE);
    protected String name = "=Unknown=";


    public PresetException(Integer preset) {
        this.preset = preset;
    }

    public PresetException(Integer preset, String name) {
        this.preset = preset;
        this.name = name;
    }

    public PresetException(Integer preset, String name, String msg) {
        super(msg);
        this.preset = preset;
        this.name = name;
    }

    public Integer getPreset() {
        return preset;
    }


    public String getName() {
        return name;
    }

    public AggRemoteName getAggName() {
        return new AggRemoteName(preset, name);
    }

}
