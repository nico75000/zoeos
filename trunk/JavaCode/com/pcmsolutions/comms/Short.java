package com.pcmsolutions.comms;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;

public class Short extends FinalMidiMessage {

    public Short(){
        super();
    }
    public Short(MidiMessage msg, MidiDevice.Info source) {
        super(msg, source);
    }

    protected Short(byte[] data, MidiDevice.Info source) {
        super(data, source);
    }
}


