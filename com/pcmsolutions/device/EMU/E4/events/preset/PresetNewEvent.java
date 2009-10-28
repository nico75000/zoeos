package com.pcmsolutions.device.EMU.E4.events.preset;

import com.pcmsolutions.gui.ProgressCallback;

import java.io.ByteArrayInputStream;


public class PresetNewEvent extends PresetInitializeEvent {
    ByteArrayInputStream inputStream;
    ProgressCallback progressCallback;

    public PresetNewEvent(Object source, Integer preset, ByteArrayInputStream is,ProgressCallback progressCallback) {
        super(source, preset);
        this.inputStream = is;
        this.progressCallback = progressCallback;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public ByteArrayInputStream getInputStream() {
        return inputStream;
    }
}

