package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.device.EMU.E4.RemotePreferences;
import com.pcmsolutions.system.preferences.ZIntPref;
import com.pcmsolutions.system.preferences.ZEnumPref;

import javax.sound.midi.MidiDevice;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 09:43:37
 */
public class RemoteFactory {
    public static Remotable createRemotable(EMU_E4_IRM irm, MidiDevice.Info inDevice, MidiDevice.Info outDevice, RemotePreferences prefs) {
        return new RawDevice(irm, inDevice, outDevice, prefs);
    }

    public static SampleMediator createSampleMediator(Remotable remote) {
        return new Impl_SampleMediator(remote, remote.getRemotePreferences().ZPREF_smdiPacketSizeInKb, remote.getRemotePreferences().ZPREF_maxSmdiSampleRate);
    }
}
