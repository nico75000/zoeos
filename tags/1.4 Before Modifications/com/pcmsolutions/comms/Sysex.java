package com.pcmsolutions.comms;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import java.util.Arrays;

public class Sysex extends FinalMidiMessage {

    public Sysex() {
        super();
    }

    public Sysex(MidiMessage msg, MidiDevice.Info source) {
        super(msg, source);
    }

    protected Sysex(byte[] data, MidiDevice.Info source) {
        super(data, source);
    }

    public boolean equals(Object o) {
        if (o instanceof Sysex) {
            Sysex sx = (Sysex) o;
            if (Arrays.equals(this.getMessage(), sx.getMessage()))
                return true;
        }
        return false;
    }
}


