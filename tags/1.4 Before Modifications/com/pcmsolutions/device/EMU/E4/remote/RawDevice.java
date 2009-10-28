package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.comms.*;
import com.pcmsolutions.device.DeviceDescriptable;
import com.pcmsolutions.device.EMU.E4.*;
import com.pcmsolutions.device.EMU.E4.events.RemoteEvent;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.smdi.*;
import com.pcmsolutions.system.*;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;


class RawDevice implements StandardStateMachine, DeviceDescriptable, Remotable, ZDisposable, SMDIAgent.SmdiListener {
    // PEPTALK
    static class PeptalkSendIDFactory {
        private static AtomicInteger idSequenecer = new AtomicInteger();

        static int next() {
            return idSequenecer.getAndIncrement();
        }
    }

    private final byte[] peptalkHeader;

    // CONSTANTS
    private final static String EOF_REPLY = "EOF";
    private final static String CANCEL_REPLY = "CANCEL";
    private final static String WAIT_REPLY = "WAIT";
    private final static String UNKNOWN_REPLY = "UNKNOWN";
    private final static String NEW_ACK_REPLY = "NEW_ACK";
    private final static String NEW_NACK_REPLY = "NEW_NACK";
    private final static String NEW_DUMP_HEADER_REPLY = "NEW_DUMP_HEADER";
    private final static String NEW_DUMP_DATA_REPLY = "NEW_DUMP_DATA";

    final static String REMOTE_TAG = "-> REMOTE";
    final static String LOCAL_TAG = "LOCAL <- ";
    private final static String HS = "/";
    private final static String ASSIGN = " -> ";

    // ATTRIBUTES
    private EMU_E4_IRM e4irm;
    private String moniker;
    private final byte[] rmtHeader;

    protected File deviceLocalDir;

    byte[] getRemoteHeader() {
        return (byte[]) rmtHeader.clone();
    }

    byte[] getPeptalkHeader() {
        return (byte[]) peptalkHeader.clone();
    }

    private String name;

    // MIDI RESOURCES
    ZMidiSystem.Inlet inlet = null;
    ZMidiSystem.PausingOutlet outlet = null;
    private MidiDevice.Info inDevice;
    private MidiDevice.Info outDevice;

    // CONTEXTS
    public final Remotable.Parameter parameter = new Impl_Parameter();
    public final Remotable.Preset preset = new Impl_Preset();
    public final Remotable.Voice voice = new Impl_Voice();
    public final Remotable.Link link = new Impl_Link();
    public final Remotable.Master master = new Impl_Master();
    public final Remotable.Sample sample = new Impl_Sample();
    public final Remotable.Zone zone = new Impl_Zone();
    public final Remotable.Peptalk peptalk = new Impl_Peptalk();
    public final Remotable.Smdi smdi = new Impl_Smdi();

    // STATE
    private StdStateMachineHelper sts = new StdStateMachineHelper(STATE_INITIALIZED, stdStateTransitions, stdStateNames);

    // EVENTS
    private Vector listeners = new Vector();

    private RemotePreferences remotePreferences;
    private volatile SmdiTarget smdiTarget = null;

    public RawDevice(EMU_E4_IRM irm, MidiDevice.Info inDevice, MidiDevice.Info outDevice, RemotePreferences prefs) {
        this.e4irm = irm;
        name = DeviceNames.getNameForDevice(e4irm);
        this.remotePreferences = prefs;

        deviceLocalDir = new File(Zoeos.getZoeosLocalDir(), irm.getByteString());
        System.out.println(Zoeos.getZoeosTime() + ": Set Outlet pause to " + getRemotePreferences().ZPREF_commPause.getValue() + "ms (" + irm + ")");

        this.outDevice = outDevice;
        this.inDevice = inDevice;
        rmtHeader = new byte[]{Remotable.BOX, Remotable.EMU_ID, Remotable.E4_ID, irm.getDeviceId(), Remotable.EDITOR_ID};
        peptalkHeader = new byte[]{Remotable.BOX, Remotable.EMU_ID, Remotable.PEPTALK_ID, irm.getDeviceId(), (byte) PeptalkSendIDFactory.next()};

        this.moniker = getManufacturer() + ZUtilities.STRING_FIELD_SEPERATOR + getModel() + ZUtilities.STRING_FIELD_SEPERATOR + getVersion();
        assertLocalDirectories();
        SMDIAgent.addSmdiListener(this);
        updateSmdiCoupling();
    }

    private synchronized void updateSmdiCoupling() {
        try {
            smdiTarget = SMDIAgent.getSmdiTargetForIdentityMessage(getIdentityMessage());
        } catch (DeviceNotCoupledToSmdiException e) {
            smdiTarget = null;
        }
    }

    public void smdiChanged() {
        updateSmdiCoupling();
    }

    public void zDispose() {
        SMDIAgent.removeSmdiListener(this);
        listeners.clear();
        remotePreferences.zDispose();
        listeners = null;
        remotePreferences = null;
    }

    public synchronized boolean isSmdiCoupled() {
        if (smdiTarget != null)
            return true;
        else
            return false;
    }

    private void assertLocalDirectories() {
        TempFileManager.getNewTempFile();
        File zoeosLocal = Zoeos.getZoeosLocalDir();
        if (!zoeosLocal.exists() && !zoeosLocal.mkdir())
            throw new IllegalArgumentException("Could not create ZoeOS local directory");

        if (!deviceLocalDir.exists() && !deviceLocalDir.mkdir())
            throw new IllegalArgumentException("Could not create device local directory");
    }

    public String makeDeviceProgressTitle(String str) {
        return this.getName() + " [ " + str + " ]";
    }

    public synchronized void stateInitial() throws IllegalStateTransitionException {
        sts.transition(StdStates.STATE_INITIALIZED);
    }

    public synchronized void stateStart() throws IllegalStateTransitionException {
        if (sts.testTransition(StdStates.STATE_STARTED) == StdStates.STATE_STARTED)
            return;
        try {
            if (openPorts()) {
                /*
                peptalk.openSession();
                Thread.sleep(1000);
                peptalk.noteOn((byte) 72, (byte) 100, (byte) 1);
                Thread.sleep(1000);
                peptalk.noteOff((byte) 72, (byte) 100, (byte) 1);
                peptalk.pressRightArrow();
                peptalk.releaseRightArrow();
                peptalk.closeSession();
                */
                sts.transition(StdStates.STATE_STARTED);
                return;
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        } catch (MidiDeviceNotPermittedException e) {
            e.printStackTrace();
        } catch (RemoteDeviceDidNotRespondException e) {
            e.printStackTrace();
        } catch (RemoteUnreachableException e) {
            e.printStackTrace();
        } catch (RemoteMessagingException e) {
            e.printStackTrace();
        }
        throw new IllegalStateTransitionException(sts.getCurrentStateName(), sts.getStateName(StdStates.STATE_STARTED), "Could not find remote device");
    }

    public synchronized void stateStop() throws IllegalStateTransitionException {
        if (sts.testTransition(StdStates.STATE_STOPPED) == StdStates.STATE_STOPPED)
            return;
        try {
            getMasterContext().cmd_sampleDefrag();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closePorts();
        /*
        try {
            peptalk.closeSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        sts.transition(StdStates.STATE_STOPPED);
    }

    public int getState() {
        return sts.getState();
    }

    private void closePorts() {
        if (outlet != null)
            outlet.discard();

        if (inlet != null)
            inlet.discard();
    }

    private boolean openPorts() throws MidiUnavailableException, MidiDeviceNotPermittedException, RemoteUnreachableException, RemoteDeviceDidNotRespondException, RemoteMessagingException {
        try {
            outlet = ZMidiSystem.getInstance().getPausingOutlet(outDevice, this, toString() + " outlet");
            inlet = ZMidiSystem.getInstance().getInlet(inDevice, this, toString() + " inlet");
            inlet.setTimeout(getRemotePreferences().ZPREF_commTimeout.getValue());
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            closePorts();
            throw e;
        } catch (MidiDeviceNotPermittedException e) {
            e.printStackTrace();
            closePorts();
            throw e;
        }
        //return true;
        /*
        for (int i = 0; i < 3; i++) {
            Object r = null;
            try {
                r = inlet.dispatchAndWaitForReply(outlet, new IdentityRequest((byte) 127), new IdentityReply());
                if (r.equals(this.getIdentityMessage())) {
                    return true;
                }
            } catch (RemoteMessagingException e) {
                e.printStackTrace();
            } catch (RemoteDeviceDidNotRespondException e) {
                e.printStackTrace();
            } catch (RemoteUnreachableException e) {
                e.printStackTrace();
            }
        }
        return false;
        */

        Object r = null;
        try {
            r = inlet.dispatchAndWaitForReply(outlet, new IdentityRequest((byte) 127), new IdentityReply());
        } catch (RemoteMessagingException e) {
            e.printStackTrace();
            closePorts();
            throw e;
        } catch (RemoteDeviceDidNotRespondException e) {
            e.printStackTrace();
            closePorts();
            throw e;
        } catch (RemoteUnreachableException e) {
            e.printStackTrace();
            closePorts();
            throw e;
        }
        if (r.equals(this.getIdentityMessage())) {
            return true;
        } else {
            closePorts();
            return false;
        }
        //return true;

    }

    public Object getIdentityMessage() {
        return e4irm;
    }

    public Object getIdentityMessageReadable() {
        return e4irm.toReadable();
    }

    public String getMoniker() {
        return moniker;
    }

    public Object getInportIdentifier() {
        return inDevice;
    }

    public Object getOutportIdentifier() {
        return outDevice;
    }

    public File getDeviceLocalDir() {
        return deviceLocalDir;
    }

    public ParameterEditLoader getEditLoader() {
        return new Impl_ParameterEditLoader();
    }

    public double getDeviceVersion() {
        return new Double(e4irm.getVersion()).doubleValue();
    }

    public void addMessageListener(RemoteEventListener rml) {
        listeners.add(rml);
    }

    public void removeMessageListener(RemoteEventListener rml) {
        listeners.remove(rml);
    }

    public byte getDeviceId() {
        return e4irm.getDeviceId();
    }

    public RemotePreferences getRemotePreferences() {
        return remotePreferences;
    }

    public Remotable.Master getMasterContext() {
        return master;
    }

    public Remotable.Peptalk getPeptalkContext() {
        return peptalk;
    }

    public Remotable.Link getLinkContext() {
        return link;
    }

    public Remotable.Parameter getParameterContext() {
        return parameter;
    }

    public Remotable.Preset getPresetContext() {
        return preset;
    }

    public Remotable.Sample getSampleContext() {
        return sample;
    }

    public Remotable.Smdi getSmdiContext() {
        return smdi;
    }

    public Remotable.Voice getVoiceContext() {
        return voice;
    }

    public Remotable.Zone getZoneContext() {
        return zone;
    }

    public Integer[] genPresetRmtSel(Integer preset) {
        return new Integer[]{IntPool.get(223), preset};
    }

    public Integer[] genVoiceRmtSel(Integer preset, Integer voice) {
        return new Integer[]{IntPool.get(223), preset, IntPool.get(225), voice};
    }

    public Integer[] genZoneRmtSel(Integer preset, Integer voice, Integer zone) {
        return new Integer[]{IntPool.get(223), preset, IntPool.get(225), voice, IntPool.get(226), zone};
    }

    private Integer[] genLinkRmtSel(Integer preset, Integer link) {
        return new Integer[]{IntPool.get(223), preset, IntPool.get(224), link};
    }

    public String toString() {
        return getName();
    }

    public String getManufacturer() {
        return e4irm.getManufacturer();
    }

    public String getModel() {
        return e4irm.getModel();
    }

    public String getVersion() {
        return e4irm.getVersion();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.equals(""))
            return;
        DeviceNames.setNameForDevice(e4irm, name);
        this.name = name;
    }

    private void fireRemoteEvent(RemoteEvent ev) {
        synchronized (listeners) {
            int num = listeners.size();
            for (int n = 0; n < num; n++)
                try {
                    ((RemoteEventListener) listeners.get(n)).remoteEvent(ev);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    private class Impl_Parameter implements Remotable.Parameter {

        public void edit_prmValue(Integer id, Integer value) throws RemoteUnreachableException, RemoteMessagingException {
            edit_prmValues(new Integer[]{id, value});
        }

        public void edit_prmValues(Integer[] idValues) throws IllegalStateException, RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdEditParameters(idValues, RawDevice.this);

            /*RMF s = new RMF("PARAM_VALUE_EDIT", REMOTE_TAG);
            int num = idValues.length;
            for (int n = 0; n < num; n += 2)
                s.addLine(idValues[n] + ASSIGN + idValues[n + 1]);

            fireRemoteEvent(new Impl_RemoteEvent(s.toString(), RemoteEvent.STATUS_OUTGOING_MESSAGE, Zoeos.getZoeosTime()));
            */
        }

        public void edit_prmValues(byte[] idValueBytes) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdEditParametersBytes(idValueBytes, RawDevice.this);

            /*RMF s = new RMF("PARAM_VALUE_EDIT", REMOTE_TAG);
            int num = idValueBytes.length;
            for (int n = 0; n < num; n += 4)
                s.addLine(idValueBytes[n] + idValueBytes[n + 1] + ASSIGN + idValueBytes[n + 2] + idValueBytes[n + 3]);

            fireRemoteEvent(new Impl_RemoteEvent(s.toString(), RemoteEvent.STATUS_OUTGOING_MESSAGE, Zoeos.getZoeosTime()));
            */
        }

        /**
         * @param id
         * @return
         */
        public MinMaxDefault req_prmMMD(Integer id) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqMMD(id, RawDevice.this);

        }

        /**
         * @param id the id of the parameter2 to be requested
         * @return
         */
        public Integer req_prmValue(Integer id) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            Integer[] r = req_prmValues(new Integer[]{id});
            return r[1];
        }

        public Integer[] req_prmValues(Integer[] ids) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return req_prmValues(ids, true);
        }

        public Integer[] req_prmValues(Integer[] ids, boolean returnIdVals) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqParameterValues(ids, RawDevice.this, returnIdVals);
        }
    }

    private class Impl_Preset implements Remotable.Preset {

        public void cmd_combineVoices(Integer preset, Integer group) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdCombineVoices(preset, group, RawDevice.this);
        }

        public void cmd_copy(Integer srcPreset, Integer destPreset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdCopyPreset(srcPreset, destPreset, RawDevice.this);
        }

        public void cmd_erase(Integer preset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdDeletePreset(preset, RawDevice.this);
        }

        public void cmd_newLink(Integer preset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdNewLink(preset, RawDevice.this);
            fireRemoteEvent(new Impl_RemoteEvent(new RMF("NEW_LINK", REMOTE_TAG).addLine(preset).toString(), RemoteEvent.STATUS_OUTGOING_MESSAGE, Zoeos.getZoeosTime()));
        }

        public void cmd_newVoice(Integer preset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdNewVoice(preset, RawDevice.this);
            fireRemoteEvent(new Impl_RemoteEvent(new RMF("NEW_VOICE", REMOTE_TAG).addLine(preset).toString(), RemoteEvent.STATUS_OUTGOING_MESSAGE, Zoeos.getZoeosTime()));
        }

        public void edit_name(Integer preset, String name) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdPresetName(preset, name, RawDevice.this);
        }

        /**
         * @param preset
         * @param pos
         * @param c
         */
        public void edit_nameChar(Integer preset, Integer pos, char c) {
        }

        public void edit_dump(ByteArrayInputStream dump, DumpMonitor mon) throws RemoteUnreachableException, RemoteMessagingException, RemoteDeviceDidNotRespondException, IOException {
            RMH.editPresetDump(RawDevice.this, dump, mon);
        }

        public ByteArrayInputStream req_dump(Integer preset, DumpMonitor mon) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException, EmptyException {
            return RMH.reqPresetDump(preset, RawDevice.this, mon);
        }

        /**
         * @param preset
         * @return
         */
        public String req_name(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqPresetName(preset, RawDevice.this);
        }

        /**
         * @param preset
         * @param pos
         * @return
         */
        public char req_nameChar(Integer preset, Integer pos) throws RemoteDeviceDidNotRespondException, RemoteMessagingException {
            return ' ';
        }

        public Integer req_numLinks(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqPresetLinks(preset, RawDevice.this);
        }

        public Integer req_numVoices(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqPresetVoices(preset, RawDevice.this);
        }

        public Integer req_numZones(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqPresetZones(preset, RawDevice.this);
        }
    }

    private class Impl_Link implements Remotable.Link {

        public void cmd_delete(Integer preset, Integer link) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdDeleteLink(preset, link, RawDevice.this);
        }

        public void cmd_copy(Integer preset, Integer link, Integer destPreset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdCopyLink(preset, link, destPreset, RawDevice.this);
        }
    }

    private class Impl_Master implements Remotable.Master {

        public void sendMidiMessage(MidiMessage m) throws RemoteUnreachableException {
            synchronized (this) {
                outlet.dispatch(m, 0, 0);
            }
        }

        public void cmd_bankErase() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdEraseBank(RawDevice.this);
        }

        public void cmd_bankEraseAllPresets() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdEraseRAMPresets(RawDevice.this);
        }

        public void cmd_bankEraseAllSamples() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdEraseRAMSamples(RawDevice.this);
        }

        public void cmd_sampleDefrag() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdSampleDefrag(RawDevice.this);
        }

        public Remotable.DeviceConfig req_deviceConfig() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqConfig(RawDevice.this);
        }

        public Remotable.DeviceExConfig req_deviceExConfig() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqExConfig(RawDevice.this);
        }

        public MultiModeMap req_multimodeMap() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqMultiModeDump(RawDevice.this);
        }

        public void cmd_multimodeMap(MultiModeMap map) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            RMH.cmdMultiModeDump(map.getByteStream(), RawDevice.this);
        }

        public void edit_multimodeMap(ByteStreamable map) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdMultiModeDump(map.getByteStream(), RawDevice.this);
            fireRemoteEvent(new Impl_RemoteEvent(new RMF("MULTI_MODE_DUMP", REMOTE_TAG).toString(), RemoteEvent.STATUS_OUTGOING_MESSAGE, Zoeos.getZoeosTime()));
        }
/*        public PresetMemory req_presetMemory() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            EMU_E4_PRESET_MEMORY_REQ_MSG msg = new EMU_E4_PRESET_MEMORY_REQ_MSG();
            Object o = msg.dispatch(new EMU_E4_PRESET_MEMORY_REPLY_MSG());
            if (o == null) {
                throw new RemoteDeviceDidNotRespondException();
            } else if (!(o instanceof PresetMemory)) {
                throw new RemoteMessagingException();
            }
            return (PresetMemory) o;
        }
        */
        public Remotable.PresetMemory req_presetMemory() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqPresetMemory(RawDevice.this);
        }

        public Remotable.SampleMemory req_sampleMemory() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqSampleMemory(RawDevice.this);
        }

        /*public void sel_preset(int preset) {
            parameter2.edit_prmValue(223, preset);
        }

        public void sel_group(int group) {
            parameter2.edit_prmValue(227, group);
        }

        public void sel_group(int preset, int group) {
            parameter2.edit_prmValues(new int[]{223,227},new int[]{ preset,group});
        }

        public void sel_link(int link) {
            parameter2.edit_prmValue(224, link);
        }

        public void sel_link(int preset, int link) {
            parameter2.edit_prmValues(new int[]{223,224},new int[]{ preset,link});
        }

        public void sel_voice(int voice) {
            parameter2.edit_prmValue(225, voice);
        }

        public void sel_voice(int preset, int voice) {
            parameter2.edit_prmValues(new int[]{223,225},new int[]{ preset,voice});
        }

        public void sel_zone(int zone) {
            parameter2.edit_prmValue(226, zone);
        }

        public void sel_zone(int preset, int voice, int zone) {
            parameter2.edit_prmValues(new int[]{223,225, 226},new int[]{ preset,voice,zone});
        }*/
    }

    private class Impl_Zone implements Remotable.Zone {
        public void cmd_delete(Integer preset, Integer voice, Integer sample) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdDeleteZone(preset, voice, sample, RawDevice.this);
        }
    }

    private class Impl_Smdi implements Remotable.Smdi {
        SmdiTarget getSmdi() throws SmdiUnavailableException {
            synchronized (this) {
                if (smdiTarget == null)
                    throw new SmdiUnavailableException();
                return smdiTarget;
            }
        }

        public void sendSync(AudioInputStream ais, int sampleNum, String sampleName, int packetSize, ProgressCallback prog) throws SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, UnsupportedAudioFileException, IOException, SmdiUnsupportedConversionException, SmdiSampleEmptyException, SmdiTransferAbortedException, SmdiUnavailableException {
            synchronized (this) {
                getSmdi().sendSync(ais, sampleNum, sampleName, packetSize, prog);
            }
        }

        public void recvSync(SMDIAgent.SampleInputStreamHandler ish, AudioFileFormat.Type fileType, int sampleNum, int packetSize, ProgressCallback prog) throws SmdiOutOfRangeException, TargetNotSMDIException, SmdiGeneralException, SmdiNoMemoryException, SmdiSampleEmptyException, SmdiUnavailableException {
            synchronized (this) {
                getSmdi().recvSync(ish, fileType, sampleNum, packetSize, prog);
            }
        }

        public byte[] sendMidiMessage(MidiMessage m) throws TargetNotSMDIException, SmdiGeneralException, SmdiUnavailableException {
            synchronized (this) {
                return getSmdi().sendMidiMessage(m);
            }
        }

        public boolean deleteSample(int sampleNum) {
            synchronized (this) {
                return false;
            }
        }

        public SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiUnavailableException {
            synchronized (this) {
                return getSmdi().getSampleHeader(sampleNum);
            }
        }
    }

    private class Impl_Peptalk implements Remotable.Peptalk {
        public void openSession() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkOpenSession(RawDevice.this);
        }

        public void closeSession() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkCloseSession(RawDevice.this);
        }

        public void noteOn(byte note, byte vel, byte track) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkNoteOn(RawDevice.this, note, vel, track);
        }

        public void noteOff(byte note, byte vel, byte track) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkNoteOff(RawDevice.this, note, vel, track);
        }

        public void note(byte note, byte onVel, byte offVel, byte track, int gateInMs) {

        }

        public void pressRightArrow() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkButtonEvent(RawDevice.this, (byte) 0, RMH.PEPTALK_RIGHT_ARROW, true);
        }

        public void releaseRightArrow() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkButtonEvent(RawDevice.this, (byte) 0, RMH.PEPTALK_RIGHT_ARROW, false);
        }

        public void pressSampleEdit() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkButtonEvent(RawDevice.this, (byte) 0, RMH.PEPTALK_SAMPLE_EDIT, true);
        }

        public void releaseSampleEdit() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkButtonEvent(RawDevice.this, (byte) 0, RMH.PEPTALK_SAMPLE_EDIT, false);
        }

        public void pressExit() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkButtonEvent(RawDevice.this, (byte) 0, RMH.PEPTALK_EXIT, true);
        }

        public void releaseExit() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.peptalkButtonEvent(RawDevice.this, (byte) 0, RMH.PEPTALK_EXIT, false);
        }
    }

    private class Impl_Sample implements Remotable.Sample {

        public void cmd_delete(Integer sample) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdDeleteSample(sample, RawDevice.this);
        }

        public void edit_name(Integer sample, String name) throws RemoteMessagingException, RemoteUnreachableException {
            RMH.cmdSampleName(sample, name, RawDevice.this);
        }

        public void edit_nameChar(Integer sample, Integer pos, char c) {
        }

        public String req_name(Integer sample) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqSampleName(sample, RawDevice.this);
        }

        public char req_nameChar(Integer sample, Integer pos) {
            return ' ';
        }
    }

    private class Impl_Voice implements Remotable.Voice {

        public void cmd_copy(Integer preset, Integer voice, Integer destPreset, Integer group) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdCopyVoice(preset, voice, destPreset, group, RawDevice.this);
        }

        public void cmd_delete(Integer preset, Integer voice) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdDeleteVoice(preset, voice, RawDevice.this);
        }

        public void cmd_expandVoice(Integer preset, Integer voice) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdExpandVoice(preset, voice, RawDevice.this);
        }

        public void cmd_getMultisample(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer destVoice) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdGetMultiSample(srcPreset, srcVoice, destPreset, destVoice, RawDevice.this);
        }

        public void cmd_newZone(Integer preset, Integer voice) throws RemoteMessagingException, RemoteUnreachableException {
            RMH.cmdNewZone(preset, voice, RawDevice.this);
        }

        public Integer req_numZones(Integer preset, Integer voice) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqVoiceZones(preset, voice, RawDevice.this);

        }
    }

    /**
     * @author pmeehan
     */
    static class RMF {
        private StringBuffer buf = new StringBuffer();
        private final static String COLON = ";";

        public RMF(Object hdr, Object tag) {
            buf.append(hdr.toString() + COLON + tag.toString());
        }

        public RMF addLine(Object o) {
            buf.append(COLON + o.toString());
            return this;
        }

        public RMF addText(Object o) {
            buf.append(o.toString());
            return this;
        }

        public String toString() {
            return buf.toString();
        }
    }

    interface NewDumpStatus {
        public final int NEW_ACK = 0;
        public final int NEW_NACK = 2;
        public final int WAIT = 3;
        public final int CANCEL = 4;
        public final int EOF = 5;
        public final int UNKNOWN = 6;

        public int getStatus();   // returns one of "NEW ACK", "NEW NACK", "WAIT", "CANCEL", "EOF"

        public int getPacketNumber();

    }

    interface NewDumpHeader {
        public int getPreset();

        public int getNumDataBytes();

        public int getNumGlobalParams();

        public int getNumLinkParams();

        public int getNumVoiceParams();

        public int getNumZoneParams();

        public byte[] getHeaderBytes();
    }

    interface NewDumpData extends Comparable {
        public int getPacketCount();

        public byte[] getData();

        public int getByteCount();
    }

    class Impl_RemoteEvent implements RemoteEvent {
        private RemoteEvent.Element[] elements;
        private int numElements;

        public Impl_RemoteEvent(RemoteEvent.Element[] elements) {
            this.elements = elements;
            numElements = elements.length;
        }

        public Impl_RemoteEvent(final String element, final int status, final long timeStamp) {
            this.elements = new RemoteEvent.Element[]
            {new RemoteEvent.Element() {
                // in relative ZoeOS time
                public long getTimestamp() {
                    return timeStamp;
                }

                // formatted string describing the element
                public String getString() {
                    return element;
                }

                // status one of above
                public int getStatus() {
                    return status;
                }
            }};
            numElements = 1;
        }

        public RemoteEvent.Element[] getElements() {
            return elements;
        }

        public int getNumElements() {
            return numElements;
        }
    }

    public class Impl_ParameterEditLoader implements ParameterEditLoader {

        private ByteArrayOutputStream msg = new ByteArrayOutputStream();

        private Integer lsp = null;

        private Integer lsg = null;

        private Integer lsl = null;

        private Integer lsv = null;

        private Integer lsz = null;

        private static final int chunkSize = 240;

        public void dispatch() throws RemoteUnreachableException, RemoteMessagingException {
            try {
                byte[] bytes = getBytes();
                int pos = 0;
                synchronized (RawDevice.this) {
                    byte[] currBytes;
                    while (pos < bytes.length) {
                        if (pos + chunkSize < bytes.length) {
                            currBytes = new byte[chunkSize];
                            System.arraycopy(bytes, pos, currBytes, 0, chunkSize);
                            pos += chunkSize;
                        } else {
                            currBytes = new byte[bytes.length - pos];
                            System.arraycopy(bytes, pos, currBytes, 0, bytes.length - pos);
                            pos += bytes.length - pos;
                        }
                        RawDevice.this.getParameterContext().edit_prmValues(currBytes);
                    }
                }
            } finally {
                reset();
            }
        }

        public ParameterEditLoader add(Integer id, Integer value) {
            if (id != null && value != null) {
                msg.write(SysexHelper.DataOut(id), 0, 2);
                msg.write(SysexHelper.DataOut(value), 0, 2);
            }
            return this;
        }

        public ParameterEditLoader add(Integer[] ids, Integer[] values) {
            if (ids == null || values == null)
                return this;
            int num = ids.length;
            for (int n = 0; n < num; n++) {
                if (ids[n] == null || values[n] == null)
                    continue;
                msg.write(SysexHelper.DataOut(ids[n]), 0, 2);
                msg.write(SysexHelper.DataOut(values[n]), 0, 2);
            }
            return this;
        }

        public ParameterEditLoader reset() {
            msg = new ByteArrayOutputStream();
            return this;
        }

        public ParameterEditLoader selPreset(Integer preset) {
            if (preset.intValue() > DeviceContext.MAX_USER_PRESET)
                throw new IllegalArgumentException("Can only remotely select user presets");
            if (!preset.equals(lsp)) {
                msg.write(SysexHelper.DataOut(223), 0, 2);
                msg.write(SysexHelper.DataOut(preset), 0, 2);
            }
            lsp = preset;
            lsg = null;
            lsl = null;
            lsv = null;
            lsz = null;
            return this;
        }

        public ParameterEditLoader selLink(Integer preset, Integer link) {
            if (!preset.equals(lsp)) {
                msg.write(SysexHelper.DataOut(223), 0, 2);
                msg.write(SysexHelper.DataOut(preset), 0, 2);
            }
            if (!link.equals(lsl)) {
                msg.write(SysexHelper.DataOut(224), 0, 2);
                msg.write(SysexHelper.DataOut(link), 0, 2);
            }
            lsp = preset;
            lsg = null;
            lsl = link;
            lsv = null;
            lsz = null;
            return this;
        }

        public ParameterEditLoader selVoice(Integer preset, Integer voice) {
            if (!preset.equals(lsp)) {
                msg.write(SysexHelper.DataOut(223), 0, 2);
                msg.write(SysexHelper.DataOut(preset), 0, 2);
            }
            if (!voice.equals(lsv)) {
                msg.write(SysexHelper.DataOut(225), 0, 2);
                msg.write(SysexHelper.DataOut(voice), 0, 2);
            }
            lsp = preset;
            lsg = null;
            lsl = null;
            lsv = voice;
            lsz = null;

            return this;
        }

        // group 1..32
        public ParameterEditLoader selGroup(Integer preset, Integer group) {
            if (!preset.equals(lsp)) {
                msg.write(SysexHelper.DataOut(223), 0, 2);
                msg.write(SysexHelper.DataOut(preset), 0, 2);
            }
            if (!group.equals(lsg)) {
                msg.write(SysexHelper.DataOut(227), 0, 2);
                msg.write(SysexHelper.DataOut(group.intValue() - 1), 0, 2);
            }
            lsp = preset;
            lsg = group;
            lsl = null;
            lsv = null;
            lsz = null;
            return this;
        }

        public ParameterEditLoader selZone(Integer preset, Integer voice, Integer zone) {
            if (!preset.equals(lsp)) {
                msg.write(SysexHelper.DataOut(223), 0, 2);
                msg.write(SysexHelper.DataOut(preset), 0, 2);
            }
            if (!voice.equals(lsv)) {
                msg.write(SysexHelper.DataOut(225), 0, 2);
                msg.write(SysexHelper.DataOut(voice), 0, 2);
            }
            if (!zone.equals(lsz)) {
                msg.write(SysexHelper.DataOut(226), 0, 2);
                msg.write(SysexHelper.DataOut(zone), 0, 2);
            }
            lsp = preset;
            lsg = null;
            lsl = null;
            lsv = voice;
            lsz = zone;
            return this;
        }

        public byte[] getBytes() {
            return msg.toByteArray();
        }
    }

    final Filterable filterPresetMemoryReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetMemoryReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetMemoryReq";
        }
    };
    final Filterable filterSampleMemoryReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterSampleMemoryReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterSampleMemoryReq";
        }
    };
    final Filterable filterPresetNameReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetNameReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetNameReq";
        }
    };
    final Filterable filterSampleNameReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterSampleNameReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterSampleNameReq";
        }
    };
    final Filterable filterMultiModeMapReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterMultiModeMapReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterMultiModeMapReq";
        }
    };
    final Filterable filterPresetVoicesReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetVoicesReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetVoicesReq";
        }
    };
    final Filterable filterPresetLinksReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetLinksReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetLinksReq";
        }
    };
    final Filterable filterVoiceZonesReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterVoiceZonesReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterVoiceZonesReq";
        }
    };
    final Filterable filterPresetZonesReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetZonesReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetZonesReq";
        }
    };
    final Filterable filterConfigReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterConfigReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterConfigReq";
        }
    };
    final Filterable filterExConfigReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterExConfigReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterExConfigReq";
        }
    };
    final Filterable filterParameterValuesReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterParameterValuesReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterParameterValuesReq";
        }
    };
    final Filterable filterPresetDumpReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetDumpReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetDumpReq";
        }
    };
    final Filterable filterMMDReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterMMDReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterMMDReq";
        }
    };
}