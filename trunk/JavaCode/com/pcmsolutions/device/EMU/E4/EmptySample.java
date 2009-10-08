package com.pcmsolutions.device.EMU.E4;

import java.io.Serializable;


class EmptySample implements Serializable{
    private static EmptySample INSTANCE = new EmptySample();


    public static EmptySample getInstance() {
        return INSTANCE;
    }

    private EmptySample() {
    }

    public String toString() {
        return DeviceContext.EMPTY_SAMPLE;
    }
}

