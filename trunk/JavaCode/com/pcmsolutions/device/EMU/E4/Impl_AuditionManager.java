package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.multimode.IllegalMultimodeChannelException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.smdi.SmdiGeneralException;
import com.pcmsolutions.smdi.TargetNotSMDIException;
import com.pcmsolutions.smdi.SmdiUnavailableException;
import com.pcmsolutions.system.AuditioningDisabledException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.SystemErrors;
import com.pcmsolutions.system.tasking.*;

import javax.sound.midi.*;
import java.util.Iterator;

/**
 * User: paulmeehan
 * Date: 12-Apr-2004
 * Time: 04:55:21
 */
class Impl_AuditionManager implements AuditionManager {
    E4Device device;
    private volatile int minAudChannel;
    private volatile int maxAudChannel;
    static final int CC = 0xB0;
    static final int NOTEON = 144;
    static final int NOTEOFF = 128;
    static final int ALLNOTESOFF = 123;
    static final int ALLSOUNDSOFF = 120;

    private volatile boolean RFXBusEnabled = false;

    private Sequencer seq;

    private final ManageableTicketedQ midiQ = QueueFactory.createTicketedQueue(this, "midiQ", 6);

    {
        midiQ.start();
    }

    public Impl_AuditionManager(E4Device device) throws DeviceException {
        this.device = device;
        refresh();
    }

    synchronized Sequencer getSequencer() throws MidiUnavailableException {
        if (seq == null)   {
            seq = MidiSystem.getSequencer(false);
            seq.open();
        }
        return seq;
    }

    public synchronized void refresh() throws DeviceException {
        determineMaxAudChannel();
        determineMinAudChannel();
    }

    public boolean isRFXBusEnabled() {
        return RFXBusEnabled;
    }

    void checkEnabled() throws AuditioningDisabledException {
        if (!device.getDevicePreferences().ZPREF_enableAuditioning.getValue())
            throw new AuditioningDisabledException();
    }

    public int getMinAudChannel() {
        return minAudChannel;
    }

    public int getMaxAudChannel() {
        return maxAudChannel;
    }

    public void audition(int ch) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isMidiBConnected() {
        return (maxAudChannel == 32);
    }

    public boolean isMidiChannelReachable(int ch) {
        if (ch >= 17 && ch <= 32 && isMidiBConnected())
            return true;
        else
            try {
                if (ch >= 1 && ch <= 16 && (!isMidiBConnected() || device.isSmdiCoupled()))
                    return true;
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        return false;
    }

    void validifyMidiChannel(int ch) throws MultimodeChannelUnreachableException {
        determineMinAudChannel();
        if (ch < minAudChannel || ch > maxAudChannel)
            throw new MultimodeChannelUnreachableException();
    }

    void sendMessage(MidiMessage m, int mmChannel) throws MultimodeChannelUnreachableException {
        try {
            if (isMidiBConnected() && mmChannel < 17 && !(mmChannel == -1))
                device.getRemote().getSmdiContext().sendMidiMessage(m);
            else
                device.getMultiModeContext().sendMidiMessage(m);
            return;
        } catch (RemoteUnreachableException e) {
            e.printStackTrace();
        } catch (SmdiGeneralException e) {
            e.printStackTrace();
        } catch (TargetNotSMDIException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
        } catch (SmdiUnavailableException e) {
            e.printStackTrace();
        }
        throw new MultimodeChannelUnreachableException();
    }

    public void noteOn(int note, int ch, int vel) throws MultimodeChannelUnreachableException, AuditioningDisabledException {
        checkEnabled();
        validifyMidiChannel(ch);
        sendNoteOn(ch, note, vel);
    }

    private void sendNoteOn(int ch, int note, int vel) throws AuditionManager.MultimodeChannelUnreachableException {
        ShortMessage sm = new ShortMessage();
        try {
            sm.setMessage(NOTEON + ((ch - 1) % 16), note, vel);
            sendMessage(sm, ch);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void noteOff(int note, int ch, int vel) throws MultimodeChannelUnreachableException, AuditioningDisabledException {
        checkEnabled();
        validifyMidiChannel(ch);
        sendNoteOff(ch, note, vel);
    }

    private void sendNoteOff(int ch, int note, int vel) throws AuditionManager.MultimodeChannelUnreachableException {
        ShortMessage sm = new ShortMessage();
        try {
            sm.setMessage(NOTEOFF + ((ch - 1) % 16), note, vel);
            sendMessage(sm, ch);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public Ticket getNote(final int note, final int ch, final int vel, final long duration) {
        return midiQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                checkEnabled();
                validifyMidiChannel(ch);
                noteOn(note, ch, vel);
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                noteOff(note, ch, vel);
            }
        }, "note");
    }

    synchronized void startUnloopedSequence(final Sequence seq, final float bpm ,Receiver r) throws MidiUnavailableException, InvalidMidiDataException {
        Sequencer s = getSequencer();
        s.stop();       
        s.setSequence(seq);
        s.setTempoInBPM(bpm);
        s.setTickPosition(0);
        s.setLoopCount(0);
        Iterator<Transmitter> i = s.getTransmitters().iterator();
        while (i.hasNext())
            i.next().close();
        s.getTransmitter().setReceiver(r);
        s.start();
    }

    public Ticket playSequence(final Sequence seq, final boolean first16, final float bpm) {
        return midiQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                checkEnabled();
                startUnloopedSequence(seq,bpm,
                        new Receiver() {
                            boolean disabled = false;

                            public void send(MidiMessage m, long timeStamp) {
                                try {
                                    if (!disabled)
                                        sendMessage(m, (first16 ? 1 : 17));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    disabled = true;
                                }
                            }

                            public void close() {
                            }
                        });
            }
        }, "note");
    }

    // on audition channel
    public void note(final int note, final int vel, final long duration) throws ResourceUnavailableException {
        //checkEnabled();
        final int ch = 1;
        note(note, ch, vel, duration);
    }

    public void note(final int note, final int ch, final int vel, final long duration) throws ResourceUnavailableException {
        getNote(note, ch, vel, duration).post();
    }

    public void midiAllNotesOff(int ch) throws MultimodeChannelUnreachableException {
        validifyMidiChannel(ch);
        ShortMessage sm = new ShortMessage();
        try {
            sm.setMessage(CC + ((ch - 1) % 16), ALLNOTESOFF, 0);
            sendMessage(sm, ch);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void midiAllSoundsOff(int ch) throws MultimodeChannelUnreachableException {
        validifyMidiChannel(ch);
        ShortMessage sm = new ShortMessage();
        try {
            sm.setMessage(CC + ((ch - 1) % 16), ALLSOUNDSOFF, 0);
            sendMessage(sm, ch);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void allNotesOff() {
        for (int i = 1; i <= 32; i++)
            try {
                midiAllNotesOff(i);
            } catch (MultimodeChannelUnreachableException e) {
                e.printStackTrace();
            }
    }

    public void allSoundsOff() {
        for (int i = minAudChannel; i <= maxAudChannel; i++)
            try {
                midiAllSoundsOff(i);
            } catch (MultimodeChannelUnreachableException e) {
                e.printStackTrace();
            }
    }

    public void sendCC(int cc, int ch, int data) throws MultimodeChannelUnreachableException {
        validifyMidiChannel(ch);
        ShortMessage sm = new ShortMessage();
        try {
            sm.setMessage(CC + ((ch - 1) % 16), cc, data);
            sendMessage(sm, ch);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public boolean attachSequencer(Sequencer seq) {
        return false;
    }

    public boolean detachSequencer(Sequencer seq) {
        return false;
    }

    void determineMaxAudChannel() throws DeviceException {
        maxAudChannel = 16;
        //Remotable remote = device.getRemote();
        final MultiModeMap map = device.getMultiModeContext().getMultimodeMap();
        boolean detectingRFX = true;//ZoeosPreferences.ZPREF_useRFXSubmixHeuristics.getValue();
        try {
            detectingRFX = device.getDeviceParameterContext().getParameterDescriptor(IntPool.get(250)).getMaxValue().intValue() < 8;
        } catch (IllegalParameterIdException e) {
            SystemErrors.internal(e);
        }
        final int[] submixes = new int[(map.has32() ? 32 : 16)];
        if (detectingRFX) {
            try {
                for (int i = 0; i < submixes.length; i++) {
                    submixes[i] = map.getSubmix(IntPool.get(i + 1)).intValue();
                    if (submixes[i] > 7) {
                        detectingRFX = false;
                        break;
                    }
                }
            } catch (IllegalMultimodeChannelException e) {
                e.printStackTrace();  // should never get here
            }
        }
        try {
            if (detectingRFX)
                try {
                    ShortMessage cc = new ShortMessage();
                    cc.setMessage(ShortMessage.CONTROL_CHANGE, 15, 79, 15);// 79 is CC to select RFX bus and 15 is GFX2 - this should enable RFX bussing if it is available
                    device.getMultiModeContext().sendMidiMessage(cc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            if (map.has32()) {
                try {
                    final int pan16 = map.getPan(IntPool.get(16)).intValue();
                    final int pan32 = map.getPan(IntPool.get(32)).intValue();
                    int test_pan = pan16;
                    while (test_pan == pan16 || test_pan == pan32)
                        test_pan = (test_pan == 63 ? -64 : test_pan + 1);
                    ShortMessage cc = new ShortMessage();
                    cc.setMessage(ShortMessage.CONTROL_CHANGE, 15, 10, test_pan + 64);
                    device.getMultiModeContext().sendMidiMessage(cc);

                    device.getMultiModeContext().syncRefresh();

                    MultiModeMap map2 = device.getMultiModeContext().getMultimodeMap();
                    if (map2.getPan(IntPool.get(16)).intValue() == test_pan) {
                        cc.setMessage(ShortMessage.CONTROL_CHANGE, 15, 10, pan16 + 64);
                        device.getMultiModeContext().sendMidiMessage(cc);
                        maxAudChannel = 16;
                    } else if (map2.getPan(IntPool.get(32)).intValue() == test_pan) {
                        cc.setMessage(ShortMessage.CONTROL_CHANGE, 15, 10, pan32 + 64);
                        device.getMultiModeContext().sendMidiMessage(cc);
                        maxAudChannel = 32;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    maxAudChannel = 16;
                }
            } else if (detectingRFX)
                try {
                    device.getMultiModeContext().syncRefresh();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        } finally {
            if (detectingRFX) {
                ShortMessage cc = new ShortMessage();
                try {
                    cc.setMessage(ShortMessage.CONTROL_CHANGE, 15, 79, submixes[maxAudChannel == 32 ? 31 : 15] + 1);
                    device.getMultiModeContext().sendMidiMessage(cc);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                } catch (RemoteUnreachableException e) {
                    e.printStackTrace();
                } finally {
                }
            }
            if (detectingRFX || map.has32())
                try {
                    device.getMultiModeContext().syncRefresh();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    void determineMinAudChannel() {
        minAudChannel = 1;
        try {
            if (!device.isSmdiCoupled() && maxAudChannel == 32)
                minAudChannel = 17;
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }
}
