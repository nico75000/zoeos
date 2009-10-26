package com.pcmsolutions.comms;

import javax.sound.midi.MidiDevice;

public interface SysexTransactionRecord {

    public MidiDevice.Info getOutDeviceInfo();

    public MidiDevice.Info getInDeviceInfo();

    public IdentityRequest getRequest();

    public FinalMidiMessage getReply();
}

