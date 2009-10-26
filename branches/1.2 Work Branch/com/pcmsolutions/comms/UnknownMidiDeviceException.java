package com.pcmsolutions.comms;

import javax.sound.midi.MidiDevice;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Sep-2003
 * Time: 17:09:17
 * To change this template use Options | File Templates.
 */
public class UnknownMidiDeviceException extends Exception {
    private MidiDevice.Info unknownDevice;

    public UnknownMidiDeviceException(MidiDevice.Info unknown) {
        this.unknownDevice = unknown;
    }

    public MidiDevice.Info getUnknownDevice() {
        return unknownDevice;
    }
}
