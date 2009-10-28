package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.AuditioningDisabledException;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.Ticket;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

/**
 * User: paulmeehan
 * Date: 12-Apr-2004
 * Time: 06:02:38
 */
public interface AuditionManager {
    int getMinAudChannel();

    int getMaxAudChannel();

    public void audition(int ch);

    public boolean isMidiBConnected();

    public boolean isMidiChannelReachable(int ch);

    void noteOn(int note, int ch, int vel) throws MultimodeChannelUnreachableException, AuditioningDisabledException;

    void noteOff(int note, int ch, int vel) throws MultimodeChannelUnreachableException, AuditioningDisabledException;

    public Ticket getNote(final int note, final int ch, final int vel, final long duration);

    void note(int note, int ch, int vel, long duration) throws ResourceUnavailableException;

    public Ticket playSequence(final Sequence seq, final boolean first16, final float bpm);

    void midiAllNotesOff(int ch) throws MultimodeChannelUnreachableException;

    void allNotesOff();

    void midiAllSoundsOff(int ch) throws MultimodeChannelUnreachableException;

    void allSoundsOff();

    public void sendCC(int cc, int ch, int data) throws MultimodeChannelUnreachableException;

    // on audition channel
    void note(int note, int vel, long duration) throws ResourceUnavailableException;

    boolean attachSequencer(Sequencer seq);

    boolean detachSequencer(Sequencer seq);

    public class MultimodeChannelUnreachableException extends Exception {
        public MultimodeChannelUnreachableException() {
            super("Midi channel unreachable");
        }
    }
}
