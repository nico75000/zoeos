package com.pcmsolutions.device.EMU.E4;

import java.io.Serializable;


class EmptyPreset implements Serializable{
    private static EmptyPreset INSTANCE = new EmptyPreset();


    public static EmptyPreset getInstance() {
        return INSTANCE;
    }

    private EmptyPreset() {
    }

    public String toString() {
        return DeviceContext.EMPTY_PRESET;
    }

}

