package com.pcmsolutions.device.EMU;

import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 31-Aug-2004
 * Time: 19:50:07
 */
public final class ROMLocation implements Serializable {
    private static final long serialVersionUID = 1;
    private final Integer index;
    private final String name;

    public ROMLocation(Integer index, String name) {
        this.index = index;
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
