package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.*;
import com.pcmsolutions.device.DeviceDescriptable;
import com.pcmsolutions.device.EMU.E4.events.RemoteEvent;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMidiChannelException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.system.*;
import com.pcmsolutions.util.ClassUtility;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.SysexMessage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;


class RawMidiDevice implements StandardStateMachine, DeviceDescriptable, Remotable, ZDisposable {
    // CONSTANTS
    private final static String EOF_REPLY = "EOF";
    private final static String CANCEL_REPLY = "CANCEL";
    private final static String WAIT_REPLY = "WAIT";
    private final static String UNKNOWN_REPLY = "UNKNOWN";
    private final static String NEW_ACK_REPLY = "NEW_ACK";
    private final static String NEW_NACK_REPLY = "NEW_NACK";
    private final static String NEW_DUMP_HEADER_REPLY = "NEW_DUMP_HEADER";
    private final static String NEW_DUMP_DATA_REPLY = "NEW_DUMP_DATA";

    private final static String REMOTE_TAG = "-> REMOTE";
    private final static String LOCAL_TAG = "LOCAL <- ";
    private final static String HS = "/";
    private final static String ASSIGN = " -> ";

    // ATTRIBUTES
    private EMU_E4_IRM e4irm;
    private String moniker;
    private byte[] rmtHeader;

    protected File deviceLocalDir;

    private byte[] getRemoteHeader() {
        return (byte[]) rmtHeader.clone();
    }

    private String name;

    // MIDI RESOURCES
    private MidiSystemFacade.Inlet inlet = null;
    private MidiSystemFacade.PausingOutlet outlet = null;
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

    // STATE
    private StateMachineHelper sts = new StateMachineHelper(STATE_INITIALIZED, stdStateTransitions, stdStateNames);

    // EVENTS
    private Vector listeners = new Vector();

    public void zDispose() {
        listeners.clear();
        remotePreferences.zDispose();
        listeners = null;
        remotePreferences = null;
    }

    private RemotePreferences remotePreferences;

    public RawMidiDevice(EMU_E4_IRM irm, MidiDevice.Info inDevice, MidiDevice.Info outDevice, RemotePreferences prefs) {
        this.e4irm = irm;
        name = DeviceNames.getNameForDevice(e4irm);
        this.remotePreferences = prefs;

        deviceLocalDir = new File(Zoeos.getZoeosLocalDir(), irm.getByteString());

        System.out.println(Zoeos.getZoeosTime() + ": Set Outlet pause to " + getRemotePreferences().ZPREF_commPause.getValue() + "ms (" + irm + ")");

        this.outDevice = outDevice;
        this.inDevice = inDevice;
        rmtHeader = new byte[]{Remotable.BOX, Remotable.EMU_ID, Remotable.E4_ID, irm.getDeviceId(), Remotable.EDITOR_ID};
        this.moniker = getManufacturer() + ZUtilities.STRING_FIELD_SEPERATOR + getModel() + ZUtilities.STRING_FIELD_SEPERATOR + getVersion();
        assertLocalDirectories();
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
        return this.getName() + " : " + str;
    }

    public synchronized void stateInitial() throws IllegalStateTransitionException {
        sts.transition(sts.STATE_INITIALIZED);
    }

    public synchronized void stateStart() throws IllegalStateTransitionException {
        if (sts.testTransition(sts.STATE_STARTED) == sts.STATE_STARTED)
            return;
        try {
            if (openPorts()) {
                sts.transition(sts.STATE_STARTED);
                return;
            }
        } catch (Exception e) {
            throw new IllegalStateTransitionException(sts.getCurrentStateName(), sts.getStateName(sts.STATE_STARTED), e.getMessage());
        }
        throw new IllegalStateTransitionException(sts.getCurrentStateName(), sts.getStateName(sts.STATE_STARTED), "Could not find remote device");
    }

    public synchronized void stateStop() throws IllegalStateTransitionException {
        if (sts.testTransition(sts.STATE_STOPPED) == sts.STATE_STOPPED)
            return;
        closePorts();
        sts.transition(sts.STATE_STOPPED);
    }

    public int getState() {
        return sts.getState();
    }

    private void closePorts() {
        if (inlet != null) {
            inlet.discard();
            inlet = null;
        }
        if (outlet != null) {
            outlet.discard();
            outlet = null;
        }
    }

    private boolean openPorts() throws UnknownMidiDeviceException, MidiUnavailableException, MidiDeviceNotPermittedException, RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
        try {
            outlet = MidiSystemFacade.getInstance().getPausingOutlet(outDevice, this, toString() + " outlet");
            inlet = MidiSystemFacade.getInstance().getInlet(inDevice, this, toString() + " inlet");
            inlet.setTimeout(getRemotePreferences().ZPREF_commTimeout.getValue());
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            closePorts();
            throw e;
        } catch (UnknownMidiDeviceException e) {
            e.printStackTrace();
            closePorts();
            throw e;
        } catch (MidiDeviceNotPermittedException e) {
            e.printStackTrace();
            closePorts();
            throw e;
        }

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
            RMH.cmdEditParameters(idValues, RawMidiDevice.this);

            /*RMF s = new RMF("PARAM_VALUE_EDIT", REMOTE_TAG);
            int num = idValues.length;
            for (int n = 0; n < num; n += 2)
                s.addLine(idValues[n] + ASSIGN + idValues[n + 1]);

            fireRemoteEvent(new Impl_RemoteEvent(s.toString(), RemoteEvent.STATUS_OUTGOING_MESSAGE, Zoeos.getZoeosTime()));
            */
        }

        public void edit_prmValues(byte[] idValueBytes) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdEditParametersBytes(idValueBytes, RawMidiDevice.this);

            /*RMF s = new RMF("PARAM_VALUE_EDIT", REMOTE_TAG);
            int num = idValueBytes.length;
            for (int n = 0; n < num; n += 4)
                s.addLine(idValueBytes[n] + idValueBytes[n + 1] + ASSIGN + idValueBytes[n + 2] + idValueBytes[n + 3]);

            fireRemoteEvent(new Impl_RemoteEvent(s.toString(), RemoteEvent.STATUS_OUTGOING_MESSAGE, Zoeos.getZoeosTime()));
            */
        }

        /** @param id
         * @return             */
        public MinMaxDefault req_prmMMD(Integer id) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqMMD(id, RawMidiDevice.this);

        }

        /** @param id the id of the parameter2 to be requested
         * @return             */
        public Integer req_prmValue(Integer id) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            Integer[] r = req_prmValues(new Integer[]{id});
            return r[1];
        }

        public Integer[] req_prmValues(Integer[] ids) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return req_prmValues(ids, true);
        }

        public Integer[] req_prmValues(Integer[] ids, boolean returnIdVals) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqParameterValues(ids, RawMidiDevice.this, returnIdVals);
        }
    }

    private class Impl_Preset implements Remotable.Preset {

        public void cmd_combineVoices(Integer preset, Integer group) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdCombineVoices(preset, group, RawMidiDevice.this);
        }

        public void cmd_copy(Integer srcPreset, Integer destPreset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdCopyPreset(srcPreset, destPreset, RawMidiDevice.this);
        }

        public void cmd_erase(Integer preset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdDeletePreset(preset, RawMidiDevice.this);
        }

        public void cmd_newLink(Integer preset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdNewLink(preset, RawMidiDevice.this);
            fireRemoteEvent(new Impl_RemoteEvent(new RMF("NEW_LINK", REMOTE_TAG).addLine(preset).toString(), RemoteEvent.STATUS_OUTGOING_MESSAGE, Zoeos.getZoeosTime()));
        }

        public void cmd_newVoice(Integer preset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdNewVoice(preset, RawMidiDevice.this);
            fireRemoteEvent(new Impl_RemoteEvent(new RMF("NEW_VOICE", REMOTE_TAG).addLine(preset).toString(), RemoteEvent.STATUS_OUTGOING_MESSAGE, Zoeos.getZoeosTime()));
        }

        public void edit_name(Integer preset, String name) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdPresetName(preset, name, RawMidiDevice.this);
        }

        /** @param preset
         * @param pos
         * @param c               */
        public void edit_nameChar(Integer preset, Integer pos, char c) {
        }

        public void edit_dump(ByteArrayInputStream dump, PresetInitializationMonitor mon) throws RemoteUnreachableException, RemoteMessagingException, RemoteDeviceDidNotRespondException, IOException {
            RMH.editPresetDump(RawMidiDevice.this, dump, mon);
        }

        public ByteArrayInputStream req_dump(Integer preset, PresetInitializationMonitor mon) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException, PresetEmptyException {
            return RMH.reqPresetDump(preset, RawMidiDevice.this, mon);
        }

        /** @param preset
         * @return               */
        public String req_name(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqPresetName(preset, RawMidiDevice.this);
        }

        /** @param preset
         * @param pos
         * @return               */
        public char req_nameChar(Integer preset, Integer pos) throws RemoteDeviceDidNotRespondException, RemoteMessagingException {
            return ' ';
        }

        public Integer req_numLinks(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqPresetLinks(preset, RawMidiDevice.this);
        }

        public Integer req_numVoices(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqPresetVoices(preset, RawMidiDevice.this);
        }

        public Integer req_numZones(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqPresetZones(preset, RawMidiDevice.this);
        }
    }

    private class Impl_Link implements Remotable.Link {

        public void cmd_delete(Integer preset, Integer link) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdDeleteLink(preset, link, RawMidiDevice.this);
        }

        public void cmd_copy(Integer preset, Integer link, Integer destPreset) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdCopyLink(preset, link, destPreset, RawMidiDevice.this);
        }
    }

    private class Impl_Master implements Remotable.Master {

        public void cmd_bankErase() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdEraseBank(RawMidiDevice.this);
        }

        public void cmd_bankEraseAllPresets() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdEraseRAMPresets(RawMidiDevice.this);
        }

        public void cmd_bankEraseAllSamples() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdEraseRAMSamples(RawMidiDevice.this);
        }

        public void cmd_sampleDefrag() throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdSampleDefrag(RawMidiDevice.this);
        }

        public Remotable.DeviceConfig req_deviceConfig() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqConfig(RawMidiDevice.this);
        }

        public Remotable.DeviceExConfig req_deviceExConfig() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqExConfig(RawMidiDevice.this);
        }

        public MultiModeMap req_multimodeMap() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqMultiModeDump(RawMidiDevice.this);
        }

        public void edit_multimodeMap(ByteStreamable map) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdMultiModeDump(map.getByteStream(), RawMidiDevice.this);
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
            return RMH.reqPresetMemory(RawMidiDevice.this);
        }

        public Remotable.SampleMemory req_sampleMemory() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqSampleMemory(RawMidiDevice.this);
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
            RMH.cmdDeleteZone(preset, voice, sample, RawMidiDevice.this);
        }
    }

    private class Impl_Sample implements Remotable.Sample {

        public void cmd_delete(Integer sample) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdDeleteSample(sample, RawMidiDevice.this);
        }

        public void edit_name(Integer sample, String name) throws RemoteMessagingException, RemoteUnreachableException {
            RMH.cmdSampleName(sample, name, RawMidiDevice.this);
        }

        public void edit_nameChar(Integer sample, Integer pos, char c) {
        }

        public String req_name(Integer sample) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqSampleName(sample, RawMidiDevice.this);
        }

        public char req_nameChar(Integer sample, Integer pos) {
            return ' ';
        }
    }

    private class Impl_Voice implements Remotable.Voice {

        public void cmd_copy(Integer preset, Integer voice, Integer destPreset, Integer group) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdCopyVoice(preset, voice, destPreset, group, RawMidiDevice.this);
        }

        public void cmd_delete(Integer preset, Integer voice) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdDeleteVoice(preset, voice, RawMidiDevice.this);
        }

        public void cmd_expandVoice(Integer preset, Integer voice) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdExpandVoice(preset, voice, RawMidiDevice.this);
        }

        public void cmd_getMultisample(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer destVoice) throws RemoteUnreachableException, RemoteMessagingException {
            RMH.cmdGetMultiSample(srcPreset, srcVoice, destPreset, destVoice, RawMidiDevice.this);
        }

        public void cmd_newZone(Integer preset, Integer voice) throws RemoteMessagingException, RemoteUnreachableException {
            RMH.cmdNewZone(preset, voice, RawMidiDevice.this);
        }

        public Integer req_numZones(Integer preset, Integer voice) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException {
            return RMH.reqVoiceZones(preset, voice, RawMidiDevice.this);

        }
    }

    /**
     * @author  pmeehan
     */
    private static class RMF {
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

    private interface NewDumpStatus {
        public final int NEW_ACK = 0;
        public final int NEW_NACK = 2;
        public final int WAIT = 3;
        public final int CANCEL = 4;
        public final int EOF = 5;
        public final int UNKNOWN = 6;

        public int getStatus();   // returns one of "NEW ACK", "NEW NACK", "WAIT", "CANCEL", "EOF"

        public int getPacketNumber();

    }

    private interface NewDumpHeader {
        public int getPreset();

        public int getNumDataBytes();

        public int getNumGlobalParams();

        public int getNumLinkParams();

        public int getNumVoiceParams();

        public int getNumZoneParams();

        public byte[] getHeaderBytes();
    }

    private interface NewDumpData extends Comparable {
        public int getPacketCount();

        public byte[] getData();

        public int getByteCount();
    }

    private class Impl_RemoteEvent implements RemoteEvent {
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
                synchronized (RawMidiDevice.this) {
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
                        RawMidiDevice.this.getParameterContext().edit_prmValues(currBytes);
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
                throw new IllegalArgumentException("Can only remotely select user samples");
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

    private final Filterable filterPresetMemoryReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetMemoryReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetMemoryReq";
        }
    };

    private final Filterable filterSampleMemoryReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterSampleMemoryReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterSampleMemoryReq";
        }
    };

    private final Filterable filterPresetNameReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetNameReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetNameReq";
        }
    };
    private final Filterable filterSampleNameReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterSampleNameReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterSampleNameReq";
        }
    };

    private final Filterable filterMultiModeMapReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterMultiModeMapReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterMultiModeMapReq";
        }
    };
    private final Filterable filterPresetVoicesReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetVoicesReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetVoicesReq";
        }
    };
    private final Filterable filterPresetLinksReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetLinksReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetLinksReq";
        }
    };
    private final Filterable filterVoiceZonesReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterVoiceZonesReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterVoiceZonesReq";
        }
    };
    private final Filterable filterPresetZonesReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetZonesReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetZonesReq";
        }
    };
    private final Filterable filterConfigReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterConfigReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterConfigReq";
        }
    };
    private final Filterable filterExConfigReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterExConfigReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterExConfigReq";
        }
    };
    private final Filterable filterParameterValuesReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterParameterValuesReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterParameterValuesReq";
        }
    };
    private final Filterable filterPresetDumpReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterPresetDumpReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterPresetDumpReq";
        }
    };
    private final Filterable filterMMDReq = new Filterable() {
        public Object filter(ByteStreamable o) {
            return RMH.filterMMDReq(o, getRemoteHeader());
        }

        public String toString() {
            return "filterMMDReq";
        }
    };

    private static class Impl_MultiModeMap implements MultiModeMap, Serializable {
        private byte[] mapData;
        private boolean has32Channels = false;

        public Impl_MultiModeMap(byte[] mapData) {
            setMapData(mapData);
        }

        public Impl_MultiModeMap(Impl_MultiModeMap mmMap) {
            byte[] in = mmMap.mapData;
            byte[] mapData = new byte[in.length];
            System.arraycopy(in, 0, mapData, 0, in.length);
            setMapData(mapData);
        }

        public void setMapData(byte[] mapData) {
            if (mapData.length < 128)
                throw new IllegalArgumentException();
            this.mapData = mapData;
            if (mapData.length == 256)
                has32Channels = true;

        }

        public String toString() {
            return "MultiModeMap";
        }

        public Integer getPan(Integer ch) throws IllegalMidiChannelException {
            assertCh(ch);
            int val = mapData[(ch.intValue() - 1) * 8 + 4];
            // another E4 sysex inconsistency!!
            if (val > 63)
                val -= 128;
            return IntPool.get(val);
        }

        public Integer getPreset(Integer ch) throws IllegalMidiChannelException {
            assertCh(ch);

            return SysexHelper.DataIn(mapData[(ch.intValue() - 1) * 8], mapData[(ch.intValue() - 1) * 8 + 1]);
        }

        public Integer getSubmix(Integer ch) throws IllegalMidiChannelException {
            assertCh(ch);
            return SysexHelper.DataIn(mapData[(ch.intValue() - 1) * 8 + 6], mapData[(ch.intValue() - 1) * 8 + 7]);
        }

        public Integer getVolume(Integer ch) throws IllegalMidiChannelException {
            assertCh(ch);
            return SysexHelper.DataIn(mapData[(ch.intValue() - 1) * 8 + 2], mapData[(ch.intValue() - 1) * 8 + 3]);
        }

        public void setPan(Integer ch, Integer pan) throws IllegalMidiChannelException {
            assertCh(ch);
            SysexHelper.DataOut(mapData, (ch.intValue() - 1) * 8 + 4, pan);
        }

        public void setPreset(Integer ch, Integer preset) throws IllegalMidiChannelException {
            assertCh(ch);
            SysexHelper.DataOut(mapData, (ch.intValue() - 1) * 8, preset);
        }

        public void setSubmix(Integer ch, Integer submix) throws IllegalMidiChannelException {
            assertCh(ch);
            SysexHelper.DataOut(mapData, (ch.intValue() - 1) * 8 + 6, submix);
        }

        public MultiModeMap getCopy() {
            return new Impl_MultiModeMap(this);
        }

        public void setVolume(Integer ch, Integer volume) throws IllegalMidiChannelException {
            assertCh(ch);
            SysexHelper.DataOut(mapData, (ch.intValue() - 1) * 8 + 2, volume);
        }

        public boolean has32() {
            return has32Channels;
        }

        private void assertCh(Integer ch) throws IllegalMidiChannelException {
            if (ch.intValue() < 1 || (has32Channels && ch.intValue() > 32) || (!has32Channels && ch.intValue() > 16))
                throw new IllegalMidiChannelException(ch);
        }

        public ByteArrayInputStream getByteStream() {
            return new ByteArrayInputStream(mapData);
        }
    }

    private static class RMH {
        private final static int HDR_SIZE = 6;
        private final static byte[] cmdTail = new byte[]{(byte) 0xF7};
        private final static byte[] cmdCSTail = new byte[]{(byte) 0x7F, (byte) 0xF7};

        private final static byte PARAMETER_VALUE_EDIT_CMD = 0x01;
        private final static byte PARAMETER_VALUE_REQ = 0x02;
        private final static byte PARAMETER_MMD_REQ = 0x03;
        private final static byte PRESET_NAME_CMD = 0x05;
        private final static byte PRESET_NAME_REQ = 0x06;
        private final static byte SAMPLE_NAME_CMD = 0x09;
        private final static byte SAMPLE_NAME_REQ = 0x0A;
        private final static byte NEW_PRESET_DUMP_CMD = 0x0d;
        private final static byte NEW_PRESET_DUMP_HEADER_SUB_CMD = 0x03;
        private final static byte NEW_PRESET_DUMP_REQ_SUB_CMD = 0x05;
        private final static byte NEW_PRESET_DUMP_DATA_SUB_CMD = 0x04;
        private final static byte PRESET_MEMORY_REQ = 0x10;
        private final static byte SAMPLE_MEMORY_REQ = 0x12;
        private final static byte CONFIG_REQ = 0x14;
        private final static byte PRESET_VOICES_REQ = 0x16;
        private final static byte PRESET_LINKS_REQ = 0x18;
        private final static byte PRESET_ZONES_REQ = 0x1a;
        private final static byte VOICE_ZONES_REQ = 0x1c;
        private final static byte EX_CONFIG_REQ = 0x1e;
        private final static byte NEW_VOICE_CMD = 0x20;
        private final static byte DELETE_VOICE_CMD = 0x21;
        private final static byte COPY_VOICE_CMD = 0x22;
        private final static byte NEW_SAMPLE_ZONE_CMD = 0x30;
        private final static byte GET_MULTISAMPLE_CMD = 0x31;
        private final static byte DELETE_ZONE_CMD = 0x32;
        private final static byte COMBINE_VOICES_CMD = 0x33;
        private final static byte EXPAND_VOICE_CMD = 0x34;
        private final static byte NEW_LINK_CMD = 0x40;
        private final static byte DELETE_LINK_CMD = 0x41;
        private final static byte COPY_LINK_CMD = 0x42;
        private final static byte DELETE_SAMPLE_CMD = 0x50;
        private final static byte SAMPLE_DEFRAG_CMD = 0x52;
        private final static byte COPY_PRESET_CMD = 0x70;
        private final static byte DELETE_PRESET_CMD = 0x71;
        private final static byte MULTI_MODE_DUMP_CMD = 0x72;
        private final static byte MULTI_MODE_DUMP_REQ = 0x73;
        private final static byte ERASE_RAM_BANK_CMD = 0x74;
        private final static byte ERASE_RAM_PRESETS_CMD = 0x75;
        private final static byte ERASE_RAM_SAMPLES_CMD = 0x76;
        private final static byte NACK_CMD = 0x79;
        private final static byte ACK_CMD = 0x7a;
        private final static byte EOF_CMD = 0x7b;
        private final static byte WAIT_CMD = 0x7c;
        private final static byte CANCEL_CMD = 0x7d;

        private final static int WAIT_TIME = 200;

        // HELPERS
        private static ByteArrayOutputStream genHeader(byte[] hdr, byte cmd) throws RemoteMessagingException {
            ByteArrayOutputStream os = new ByteArrayOutputStream(HDR_SIZE);
            try {
                os.write(hdr);
                os.write(cmd);
            } catch (IOException e) {
                throw new RemoteMessagingException(e.getMessage());
            }
            return os;
        }

        private static SysexMessage wrapCmd(ByteArrayOutputStream os) throws RemoteMessagingException {
            SysexMessage m = new SysexMessage();
            try {
                os.write(cmdTail);
                m.setMessage(os.toByteArray(), os.size());
                os.close();
            } catch (Exception e) {
                throw new RemoteMessagingException(e.getMessage());
            }
            return m;

        }

        private static SysexMessage wrapChecksummedCmd(ByteArrayOutputStream os) throws RemoteMessagingException {
            SysexMessage m = new SysexMessage();
            try {
                os.write(cmdCSTail);
                m.setMessage(os.toByteArray(), os.size());
                os.close();
            } catch (Exception e) {
                throw new RemoteMessagingException(e.getMessage());
            }
            return m;

        }

        private static SysexMessage wrapChecksummedCmd(ByteArrayOutputStream os, byte cs) throws RemoteMessagingException {
            SysexMessage m = new SysexMessage();
            try {
                //if ( cs < 0)
                //  cs+=128;
                os.write(cs);
                os.write(cmdTail);
                m.setMessage(os.toByteArray(), os.size());
                os.close();
            } catch (Exception e) {
                throw new RemoteMessagingException(e.getMessage());
            }
            return m;

        }

        private static void dispatchCmd(SysexMessage m, RawMidiDevice d, long pause) throws IllegalStateException, RemoteUnreachableException {
            synchronized (d) {
                d.outlet.dispatch(m, 0, pause);
            }
        }

        private static Object dispatchCmdReply(SysexMessage m, RawMidiDevice d, Filterable f) throws RemoteUnreachableException, RemoteDeviceDidNotRespondException, RemoteMessagingException {
            synchronized (d) {
                return d.inlet.dispatchAndWaitForReply(d.outlet, m, f);
            }
        }

        private static Object[] dispatchCmdLongReply(SysexMessage m, RawMidiDevice d, Filterable f) throws RemoteUnreachableException, RemoteDeviceDidNotRespondException, RemoteMessagingException {
            synchronized (d) {
                return d.inlet.dispatchAndWaitForLongReply(d.outlet, m, f);
            }
        }

        // private static Object waitForReply(RawMidiDevice d, Filterable f) throws RemoteMessagingException, RemoteDeviceDidNotRespondException {
        //   synchronized (d) {
        //     return d.inlet.waitForReply(f);
        //   }
        // }

        private static Object[] dispatchCmdReplies(SysexMessage m, RawMidiDevice d, Filterable f, int numReplies) throws RemoteUnreachableException, RemoteDeviceDidNotRespondException, RemoteMessagingException {
            synchronized (d) {
                return d.inlet.dispatchAndWaitForReplies(d.outlet, m, f, numReplies);
            }
        }

        private static Object[] dispatchCmdLongReplies(SysexMessage m, RawMidiDevice d, Filterable f, int numReplies) throws RemoteUnreachableException, RemoteDeviceDidNotRespondException, RemoteMessagingException {
            synchronized (d) {
                return d.inlet.dispatchAndWaitForLongReplies(d.outlet, m, f, numReplies);
            }
        }

        private static ByteArrayOutputStream putInteger(ByteArrayOutputStream os, Integer intv) throws RemoteMessagingException {
            try {
                os.write(SysexHelper.DataOut(intv.shortValue()));
            } catch (Exception e) {
                throw new RemoteMessagingException("Midi message construction error");
            }
            return os;
        }

        private static ByteArrayOutputStream putByte(ByteArrayOutputStream os, byte bytev) throws RemoteMessagingException {
            try {
                os.write(bytev);
            } catch (Exception e) {
                throw new RemoteMessagingException("Midi message construction error");
            }
            return os;
        }

        private static ByteArrayOutputStream putName(ByteArrayOutputStream os, String name) throws RemoteMessagingException {
            try {
                //StringBuffer buf = new StringBuffer(name);
                //buf.setLength(16);
                //name = ;
                os.write(ZUtilities.makeExactLengthString(name, 16, ' ', true).getBytes("US-ASCII"));
            } catch (Exception e) {
                throw new RemoteMessagingException("Midi message construction error");
            }
            return os;
        }

        private static ByteArrayOutputStream putStream(ByteArrayOutputStream os, ByteArrayInputStream is) throws RemoteMessagingException {
            try {
                int cnt = is.available();
                byte[] data = new byte[cnt];
                if (is.read(data, 0, cnt) == cnt) {
                    os.write(data, 0, cnt);
                } else
                    throw new RemoteMessagingException("Midi message construction error");

            } catch (Exception e) {
                throw new RemoteMessagingException("Midi message construction error");
            }
            return os;
        }

        private static ByteArrayOutputStream putIntegers(ByteArrayOutputStream os, Integer[] ints) throws RemoteMessagingException {
            try {
                int len = ints.length;
                os.write((byte) (len));
                for (int n = 0; n < len; n++) {
                    os.write(SysexHelper.DataOut(ints[n].shortValue()));
                }
            } catch (Exception e) {
                throw new RemoteMessagingException("Midi message construction error");
            }
            return os;
        }

        private static ByteArrayOutputStream putBytes(ByteArrayOutputStream os, byte[] bytes, boolean putLength) throws RemoteMessagingException {
            try {
                if (putLength)
                    os.write((byte) (bytes.length / 2));
                os.write(bytes);
            } catch (Exception e) {
                throw new RemoteMessagingException("Midi message construction error");
            }
            return os;
        }

        private static ByteArrayOutputStream putBytes(ByteArrayOutputStream os, byte[] bytes, boolean putLength, int count) throws RemoteMessagingException {
            try {
                if (putLength)
                    os.write((byte) (bytes.length / 2));
                os.write(bytes, 0, count);
            } catch (Exception e) {
                throw new RemoteMessagingException("Midi message construction error");
            }
            return os;
        }

        // REQUESTS

        public static Remotable.DeviceConfig reqConfig(final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (Remotable.DeviceConfig) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), CONFIG_REQ)), d, d.filterConfigReq);
        }

        public static Remotable.DeviceExConfig reqExConfig(final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (Remotable.DeviceExConfig) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), EX_CONFIG_REQ)), d, d.filterExConfigReq);
        }

        public static Integer reqPresetVoices(Integer preset, final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (Integer) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_VOICES_REQ), preset)), d, d.filterPresetVoicesReq);
        }

        public static Integer reqPresetZones(Integer preset, final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (Integer) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_ZONES_REQ), preset)), d, d.filterPresetZonesReq);
        }

        public static Integer reqVoiceZones(Integer preset, Integer voice, final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (Integer) dispatchCmdReply(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), VOICE_ZONES_REQ), preset), voice)), d, d.filterVoiceZonesReq);
        }

        public static Integer reqPresetLinks(Integer preset, final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (Integer) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_LINKS_REQ), preset)), d, d.filterPresetLinksReq);
        }

        public static Remotable.PresetMemory reqPresetMemory(final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (Remotable.PresetMemory) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), PRESET_MEMORY_REQ)), d, d.filterPresetMemoryReq);
        }

        public static Remotable.SampleMemory reqSampleMemory(final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (Remotable.SampleMemory) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), SAMPLE_MEMORY_REQ)), d, d.filterSampleMemoryReq);
        }

        public static String reqPresetName(Integer preset, final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            /*
            try {
                byte[] reply = SMDILogic.sendMidi(0, 2, wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_NAME_REQ), preset)).getMessage());
                System.out.println(reply.length);
            } catch (SMDILogic.SMDILogicException e) {
                e.printStackTrace();
            } catch (SMDILogic.SMDISampleEmptyException e) {
                e.printStackTrace();
            } catch (RemoteMessagingException e) {
                e.printStackTrace();
            } */

            return (String) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_NAME_REQ), preset)), d, d.filterPresetNameReq);
            //return "test";
        }

        public static String reqSampleName(Integer sample, final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (String) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), SAMPLE_NAME_REQ), sample)), d, d.filterSampleNameReq);
        }

        public static MinMaxDefault reqMMD(Integer id, final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (MinMaxDefault) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PARAMETER_MMD_REQ), id)), d, d.filterMMDReq);
        }

        public static Integer[] reqParameterValues(Integer[] parameters, final RawMidiDevice d, boolean returnIdVals) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            int num = parameters.length;
            Object[] replies = dispatchCmdReplies(wrapChecksummedCmd(putIntegers(genHeader(d.getRemoteHeader(), PARAMETER_VALUE_REQ), parameters)), d, d.filterParameterValuesReq, num);
            int numIdVals = num * 2;
            Object reply;
            if (replies != null && replies.length >= num) {
                Integer[] output;
                if (returnIdVals)
                    output = new Integer[numIdVals];
                else
                    output = new Integer[numIdVals / 2];

                for (int n = 0; n < numIdVals; n += 2) {
                    reply = replies[n / 2];
                    if (reply != null && reply instanceof Integer[] && ((Integer[]) reply).length > 1) {
                        // E4 sysex spec states that it will return one parameter2 edit message for each requested parameter2
                        // so there should not be more than one parameter2/value in each message
                        // so only access 2 integers from output - there shouldn't be any more!!
                        if (returnIdVals) {
                            output[n] = ((Integer[]) reply)[0];
                            output[n + 1] = ((Integer[]) reply)[1];
                        } else
                            output[n / 2] = ((Integer[]) reply)[1];
                    } else
                        return null;
                }
                return output;
            } else
                throw new RemoteMessagingException("Not enough replies ( or no reply ) to a request for parameter values");
        }

        public static MultiModeMap reqMultiModeDump(final RawMidiDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            return (MultiModeMap) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), MULTI_MODE_DUMP_REQ)), d, d.filterMultiModeMapReq);
        }

        public static void editPresetDump(final RawMidiDevice d, ByteArrayInputStream dump, PresetInitializationMonitor mon) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException, IOException {
            Object[] headerReplies;
            int lastDataPacketSize = -1;
            synchronized (d) {
                MidiSystemFacade.midiLock.access();
                try {
                    byte[] leadBytes = new byte[14];
                    dump.read(leadBytes);
                    headerReplies = dispatchCmdLongReply(wrapCmd(putBytes(putByte(genHeader(d.getRemoteHeader(), NEW_PRESET_DUMP_CMD), NEW_PRESET_DUMP_HEADER_SUB_CMD), leadBytes, false)), d, d.filterPresetDumpReq);
                    int i = ClassUtility.firstIndexOfClass(headerReplies, NewDumpStatus.class, false);
                    if (i != -1 && ((NewDumpStatus) headerReplies[i]).getStatus() == NewDumpStatus.NEW_ACK) {
                        if (mon != null)
                            mon.setStatus(0);
                        doEditDumpLoop(d, (NewDumpStatus) headerReplies[i], dump, lastDataPacketSize, mon);
                    } else if (i != -1 && ((NewDumpStatus) headerReplies[i]).getStatus() == NewDumpStatus.WAIT) {
                        try {
                            Thread.sleep(WAIT_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (mon != null)
                            mon.setStatus(0);
                        doEditDumpLoop(d, (NewDumpStatus) headerReplies[i], dump, lastDataPacketSize, mon);
                    } else {
                        cmdCancel(d);
                        throw new RemoteMessagingException("Incorrect response to preset dump header");
                    }
                } finally {
                    MidiSystemFacade.midiLock.unlock();
                    if (mon != null)
                        mon.setStatus(RemoteObjectStates.STATUS_INITIALIZED);
                }
            }
        }

        private static void doEditDumpLoop(RawMidiDevice d, NewDumpStatus reply, ByteArrayInputStream dump, int lastDataPacketSize, PresetInitializationMonitor mon) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
            byte[] data = new byte[244];
            Object[] robjs;
            int packetCount = 1;
            int nackCount = 0;
            byte checksum;
            dump.mark(0);
            double dump_tot = dump.available();
            while (true) {
                switch (reply.getStatus()) {
                    case NewDumpStatus.NEW_ACK:
                        //nackCount = 0;
                        dump.mark(0);
                        checksum = 0;
                        int av = dump.available();
                        if (av == 0) {
                            cmdEof(d);
                            return;
                        } else if (av < 244) {
                            dump.read(data, 0, av);
                            for (int i = 0,j = av; i < j; i++)
                                    //checksum += (data[i] < 0 ? (~data[i] + 128) : ~data[i]);
                                checksum += data[i];
                        } else {
                            dump.read(data, 0, 244);
                            for (int i = 0,j = 244; i < j; i++)
                                    //checksum += (data[i] < 0 ? (~data[i] + 128) : ~data[i]);
                                checksum += data[i];
                        }
                        checksum = (byte) ~checksum;
                        if (checksum < 0)
                            checksum += 128;
                        robjs = dispatchCmdLongReply(wrapChecksummedCmd(putBytes(putInteger(putByte(genHeader(d.getRemoteHeader(), NEW_PRESET_DUMP_CMD), NEW_PRESET_DUMP_DATA_SUB_CMD), IntPool.get(packetCount++)), data, false, (av < 244 ? av : 244)), (byte) checksum), d, d.filterPresetDumpReq);
                        int i = ClassUtility.firstIndexOfClass(robjs, NewDumpStatus.class, false);
                        if (i == -1) {
                            cmdCancel(d);
                            throw new RemoteMessagingException("Incorrect reply during preset dump loop");
                        }
                        reply = (NewDumpStatus) robjs[i];
                        break;
                    case NewDumpStatus.NEW_NACK:
                        if (nackCount++ < 5) {
                            dump.reset();
                            packetCount--;
                            final int pc = packetCount;
                            reply = new NewDumpStatus() {
                                public int getStatus()   // returns one of "NEW ACK", "NEW NACK", "WAIT", "CANCEL", "EOF"
                                {
                                    return NEW_ACK;
                                }

                                public int getPacketNumber() {
                                    return pc;
                                }
                            };
                            break;
                        }
                        cmdCancel(d);
                        throw new RemoteMessagingException("Too many NACKS");
                    case NewDumpStatus.WAIT:
                        try {
                            Thread.sleep(WAIT_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        final int pn = reply.getPacketNumber();
                        reply = new NewDumpStatus() {
                            public int getStatus()   // returns one of "NEW ACK", "NEW NACK", "WAIT", "CANCEL", "EOF"
                            {
                                return NEW_ACK;
                            }

                            public int getPacketNumber() {
                                return pn;
                            }
                        };
                        break;
                        //System.out.println("CANCELLED BECAUSE OF WAIT");
                    default:
                        //System.out.println("CANCELLED BECAUSE OF UNKNOWN");
                        // CANCEL, EOF OR UNKNOWN
                        cmdCancel(d);
                        throw new RemoteMessagingException("Incorrect reply during preset dump loop");
                }
                if (mon != null)
                    mon.setStatus(-(dump_tot - dump.available()) / dump_tot);
            }
        }

        public static ByteArrayInputStream reqPresetDump(Integer preset, final RawMidiDevice d, PresetInitializationMonitor mon) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException, PresetEmptyException {
            Object[] replies;
            ArrayList dataPackets = new java.util.ArrayList();
            synchronized (d) {
                int totalBytes;
                MidiSystemFacade.midiLock.access();
                try {
                    replies = dispatchCmdLongReply(wrapCmd(putInteger(putByte(genHeader(d.getRemoteHeader(), NEW_PRESET_DUMP_CMD), NEW_PRESET_DUMP_REQ_SUB_CMD), preset)), d, d.filterPresetDumpReq);
                    int i = ClassUtility.firstIndexOfClass(replies, NewDumpHeader.class, false);
                    if (i != -1) {
                        dataPackets.add(replies[i]);
                        totalBytes = ((NewDumpHeader) replies[i]).getNumDataBytes();
                        if (mon != null)
                            mon.setStatus(0);
                    } else {
                        i = ClassUtility.firstIndexOfClass(replies, NewDumpStatus.class, false);
                        if (i != -1 && ((NewDumpStatus) replies[i]).getStatus() == NewDumpStatus.CANCEL)
                            throw new PresetEmptyException(preset);
                        else {
                            cmdCancel(d);
                            throw new RemoteMessagingException("First replies from dump request was not a dump header");
                        }
                    }
                    doMainDumpLoop(d, mon, totalBytes, dataPackets);
                    return dumpPacketsToInputStream(dataPackets);
                } finally {
                    MidiSystemFacade.midiLock.unlock();
                    mon.setStatus(RemoteObjectStates.STATUS_INITIALIZED);
                }
            }
        }

        private static void doMainDumpLoop(RawMidiDevice d, PresetInitializationMonitor mon, int totalBytes, ArrayList dataPackets) throws RemoteUnreachableException, RemoteMessagingException, RemoteDeviceDidNotRespondException {
            Object[] replies;
            boolean done = false;
            int rpc = 0;
            int runningDataByteCount = 0;
            while (!done) {
                replies = cmdAck(IntPool.get(rpc++), d);
                if (replies == null) {
                    cmdCancel(d);
                    throw new RemoteMessagingException("No replies to packet acknowledge");
                }
                int i = ClassUtility.firstIndexOfClass(replies, NewDumpData.class, false);

                if (i != -1) {
                    dataPackets.add(replies[i]);
                    runningDataByteCount += ((NewDumpData) replies[i]).getByteCount();
                    if (mon != null)
                        mon.setStatus((double) runningDataByteCount / (double) totalBytes);
                } else {
                    i = ClassUtility.firstIndexOfClass(replies, NewDumpStatus.class, false);
                    if (i != -1)
                        switch (((NewDumpStatus) replies[i]).getStatus()) {
                            case NewDumpStatus.EOF:
                                done = true;
                                break;
                            case NewDumpStatus.CANCEL:
                                throw new RemoteMessagingException("Dump cancelled by remote");
                            default:
                                cmdCancel(d);
                                throw new RemoteMessagingException("Invalid dump response");
                        }
                    else
                        throw new RemoteMessagingException("Invalid dump response");
                }
            }
        }

        private static ByteArrayInputStream dumpPacketsToInputStream(ArrayList dataPackets) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int num = dataPackets.size();
            if (num > 2) {      // must have header + at least one data packet
                try {
                    os.write(((NewDumpHeader) dataPackets.get(0)).getHeaderBytes());

                    for (int n = 1; n < num; n++)
                        os.write(((NewDumpData) dataPackets.get(n)).getData());

                    return new ByteArrayInputStream(os.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private static Object[] cmdAck(Integer packet, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException, RemoteDeviceDidNotRespondException {
            return dispatchCmdLongReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), ACK_CMD), packet)), d, d.filterPresetDumpReq);
        }

        // COMMANDS

        public static void cmdEditParameters(Integer[] idVals, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapChecksummedCmd(putIntegers(genHeader(d.getRemoteHeader(), PARAMETER_VALUE_EDIT_CMD), idVals)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdEditParametersBytes(byte[] idValBytes, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapChecksummedCmd(putBytes(genHeader(d.getRemoteHeader(), PARAMETER_VALUE_EDIT_CMD), idValBytes, true)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdPresetName(Integer preset, String name, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putName(putInteger(genHeader(d.getRemoteHeader(), PRESET_NAME_CMD), preset), name)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdSampleName(Integer sample, String name, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putName(putInteger(genHeader(d.getRemoteHeader(), SAMPLE_NAME_CMD), sample), name)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdNewZone(Integer preset, Integer voice, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), NEW_SAMPLE_ZONE_CMD), preset), voice)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
        }

        public static void cmdNewVoice(Integer preset, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), NEW_VOICE_CMD), preset)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
        }

        public static void cmdNewLink(Integer preset, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), NEW_LINK_CMD), preset)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdMultiModeDump(ByteArrayInputStream is, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putStream(genHeader(d.getRemoteHeader(), MULTI_MODE_DUMP_CMD), is)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdCancel(RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), CANCEL_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdEof(RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), EOF_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdGetMultiSample(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer destVoice, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(putInteger(putInteger(putInteger(genHeader(d.getRemoteHeader(), GET_MULTISAMPLE_CMD), srcPreset), srcVoice), destPreset), destVoice)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdDeleteZone(Integer preset, Integer voice, Integer zone, final RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(putInteger(putInteger(genHeader(d.getRemoteHeader(), DELETE_ZONE_CMD), preset), voice), zone)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
        }

        public static void cmdSampleDefrag(RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), SAMPLE_DEFRAG_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 100);
        }

        public static void cmdEraseBank(RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), ERASE_RAM_BANK_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 100);
        }

        public static void cmdEraseRAMPresets(RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), ERASE_RAM_PRESETS_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 100);
        }

        public static void cmdEraseRAMSamples(RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), ERASE_RAM_SAMPLES_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 100);
        }

        public static void cmdCopyLink(Integer srcPreset, Integer link, Integer destPreset, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(putInteger(putInteger(genHeader(d.getRemoteHeader(), COPY_LINK_CMD), srcPreset), link), destPreset)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
        }

        public static void cmdCopyVoice(Integer srcPreset, Integer voice, Integer destPreset, Integer destGroup, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putByte(putInteger(putInteger(putInteger(genHeader(d.getRemoteHeader(), COPY_VOICE_CMD), srcPreset), voice), destPreset), (byte) (destGroup.intValue() - 1))), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 5);
        }

        public static void cmdDeleteLink(Integer preset, Integer link, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), DELETE_LINK_CMD), preset), link)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 2);
        }

        public static void cmdCombineVoices(Integer preset, Integer group, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), COMBINE_VOICES_CMD), preset), IntPool.get(group.intValue() - 1))), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 5);
        }

        public static void cmdExpandVoice(Integer preset, Integer voice, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), EXPAND_VOICE_CMD), preset), voice)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 15);
        }

        public static void cmdDeleteVoice(Integer preset, Integer voice, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), DELETE_VOICE_CMD), preset), voice)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
        }

        public static void cmdCopyPreset(Integer srcPreset, Integer destPreset, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), COPY_PRESET_CMD), srcPreset), destPreset)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
        }

        public static void cmdDeletePreset(Integer preset, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), DELETE_PRESET_CMD), preset)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 5);
        }

        public static void cmdDeleteSample(Integer sample, RawMidiDevice d) throws RemoteMessagingException, RemoteUnreachableException {
            dispatchCmd(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), DELETE_SAMPLE_CMD), sample)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 10);
        }

        // FILTERS
        private static Remotable.PresetMemory filterPresetMemoryReq(ByteStreamable o, byte[] rmtHeader) {
            Remotable.PresetMemory robj = null;
            ByteArrayInputStream is = o.getByteStream();
            byte[] header = new byte[rmtHeader.length];

            if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x11) {
                byte[] data = new byte[4];  // big enough to accomodate both 2 byte fields

                if (is.read(data, 0, 4) == 4) {
                    final Integer presetMemory = SysexHelper.DataIn(data[0], data[1]);
                    final Integer presetFreeMemory = SysexHelper.DataIn(data[2], data[3]);
                    robj = new Remotable.PresetMemory() {
                        public String toString() {
                            return "Preset Memory   " + presetMemory + " KB (free:" + presetFreeMemory + " KB)";
                        }
                        /*public String toString() {
                            return new RMF("PRESET_MEMORY", LOCAL_TAG).toString();
                        } */

                        public Integer getPresetMemory() {
                            return presetMemory;
                        }

                        public Integer getPresetFreeMemory() {
                            return presetFreeMemory;
                        }
                    };
                }
            }
            return robj;
        }

        private static Remotable.SampleMemory filterSampleMemoryReq(ByteStreamable o, byte[] rmtHeader) {
            Remotable.SampleMemory robj = null;
            ByteArrayInputStream is = o.getByteStream();
            byte[] header = new byte[rmtHeader.length];

            if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x13) {
                byte[] data = new byte[4];  // big enough to accomodate both 2 byte fields

                if (is.read(data, 0, 4) == 4) {
                    final Integer sampleMemory = SysexHelper.DataIn(data[0], data[1]);
                    final Integer sampleFreeMemory = SysexHelper.DataIn(data[2], data[3]);
                    robj = new Remotable.SampleMemory() {
                        public String toString() {
                            return "Sample Memory   " + sampleMemory + " MB (free:" + IntPool.get(sampleFreeMemory.intValue() * 10) + " KB)";
                        }
                        /*public String toString() {
                            return new RMF("SAMPLE_MEMORY", LOCAL_TAG).toString();
                        } */

                        public Integer getSampleMemory() {
                            return sampleMemory;
                        }

                        public Integer getSampleFreeMemory() {
                            return sampleFreeMemory;
                        }
                    };
                }
            }
            return robj;
        }

        private static String filterPresetNameReq(ByteStreamable o, byte[] rmtHeader) {
            String rstr = null;
            ByteArrayInputStream is = o.getByteStream();
            byte[] header = new byte[rmtHeader.length];

            if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x05) {
                byte[] data = new byte[16];  // big enough to accomodate name

                if (is.read(data, 0, 2) == 2) {
                    if (/*preset == (int)SysexHelper.DataIn(data[0], data[1]) &&*/ is.read(data, 0, 16) == 16)
                        return new String(data).trim();
                }
            }
            return rstr;
        }

        private static String filterSampleNameReq(ByteStreamable o, byte[] rmtHeader) {
            String rstr = null;
            ByteArrayInputStream is = o.getByteStream();
            byte[] header = new byte[rmtHeader.length];

            if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x09) {
                byte[] data = new byte[16];  // big enough to accomodate name

                if (is.read(data, 0, 2) == 2) {
                    if (/*sample.equals(SysexHelper.DataIn(data[0], data[1])) &&*/ is.read(data, 0, 16) == 16)
                        return new String(data);
                }
            }
            return rstr;
        }

        private static MultiModeMap filterMultiModeMapReq(ByteStreamable o, byte[] rmtHeader) {
            ByteArrayInputStream is = o.getByteStream();
            byte[] header = new byte[rmtHeader.length];
            if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x72) {
                byte[] first16 = new byte[128];  // to accomodate first 16 midi channels
                byte[] all32 = new byte[256];  // to accomodate 32 midi channels available
                if (is.read(first16, 0, 128) == 128) {
                    if (is.read(all32, 128, 128) == 128) {
                        System.arraycopy(first16, 0, all32, 0, 128);
                        return new Impl_MultiModeMap(all32);
                    } else
                        return new Impl_MultiModeMap(first16);
                }
            }
            return null;
        }

        private static Integer filterPresetVoicesReq(ByteStreamable o, byte[] rmtHeader) {
            Integer rint = null;
            ByteArrayInputStream is = o.getByteStream();
            byte[] data = new byte[rmtHeader.length];

            if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x17) {
                if (is.read(data, 0, 2) == 2)
                    return SysexHelper.DataIn(data[0], data[1]);
            }
            return rint;
        }

        private static Integer filterPresetLinksReq(ByteStreamable o, byte[] rmtHeader) {
            Integer rint = null;
            ByteArrayInputStream is = o.getByteStream();
            byte[] data = new byte[rmtHeader.length];

            if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x19) {
                if (is.read(data, 0, 2) == 2)
                    return SysexHelper.DataIn(data[0], data[1]);
            }
            return rint;
        }

        private static Integer filterVoiceZonesReq(ByteStreamable o, byte[] rmtHeader) {
            Integer rint = null;
            ByteArrayInputStream is = o.getByteStream();
            byte[] data = new byte[rmtHeader.length];

            if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x1d) {
                if (is.read(data, 0, 2) == 2)
                    return SysexHelper.DataIn(data[0], data[1]);
            }
            return rint;
        }

        private static Integer filterPresetZonesReq(ByteStreamable o, byte[] rmtHeader) {
            Integer rint = null;
            ByteArrayInputStream is = o.getByteStream();
            byte[] data = new byte[rmtHeader.length];

            if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x1b) {
                if (is.read(data, 0, 2) == 2)
                    return SysexHelper.DataIn(data[0], data[1]);
            }
            return rint;
        }

        private static Remotable.DeviceConfig filterConfigReq(ByteStreamable o, byte[] rmtHeader) {
            ByteArrayInputStream is = o.getByteStream();
            byte[] data = new byte[rmtHeader.length];
            if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x15) {
                final byte[] info = new byte[3];
                if (is.read(info, 0, 3) == 3)
                    return new Remotable.DeviceConfig() {
                        public String toString() {
                            return new RMF("CONFIG_REPLY", LOCAL_TAG).toString();
                        }

                        public Integer getVoices() {
                            if ((info[0] & 1) != 0) return IntPool.get(128); else return IntPool.get(64);
                        }

                        public boolean hasFX() {
                            if ((info[0] & 2) != 0) return true; else return false;
                        }

                        public boolean hasMidi() {
                            if ((info[0] & 4) != 0) return true; else return false;
                        }

                        public boolean hasOctopus() {
                            if ((info[0] & 8) != 0) return true; else return false;
                        }

                        public boolean hasDigitalIO() {
                            if ((info[0] & 16) != 0) return true; else return false;
                        }

                        public Integer getSampleRAM() {
                            return SysexHelper.DataIn(info[1], info[2]);
                        }
                    };
            }
            return null;
        }

        private static Remotable.DeviceExConfig filterExConfigReq(ByteStreamable o, byte[] rmtHeader) {

            ByteArrayInputStream is = o.getByteStream();
            byte[] data = new byte[rmtHeader.length];
            if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x1f) {
                final byte[] info = new byte[10];
                if (is.read(info, 0, 10) == 10)
                    return new Remotable.DeviceExConfig() {

                        public Integer getVoices() {
                            if ((info[0] & 1) != 0) return IntPool.get(128); else return IntPool.get(64);
                        }

                        public boolean hasFX() {
                            if ((info[0] & 2) != 0) return true; else return false;
                        }

                        public boolean hasMidi() {
                            if ((info[0] & 4) != 0) return true; else return false;
                        }

                        public boolean hasOctopus() {
                            if ((info[0] & 8) != 0) return true; else return false;
                        }

                        public boolean hasDigitalIO() {
                            if ((info[0] & 16) != 0) return true; else return false;
                        }

                        public boolean hasPresetFlash() {
                            if ((info[0] & 32) != 0) return true; else return false;
                        }

                        public boolean hasADAT() {
                            if ((info[0] & 64) != 0) return true; else return false;
                        }

                        public Integer getSampleRAM() {
                            return SysexHelper.DataIn(info[2], info[3]);
                        }

                        public Integer getSampleROM() {
                            return IntPool.get((int) info[4]);
                        }

                        public Integer getSampleFlash() {
                            return IntPool.get((int) info[5]);
                        }

                        public String toString() {
                            String ls = Zoeos.getLineSeperator();
                            DecimalFormat df3 = new DecimalFormat("##0");
                            //DecimalFormat df2 = new DecimalFormat("#0");
                            return ls
                                    + "Voices          " + getVoices() + ls
                                    + (hasFX() ? "Legacy FX       Present" : "No Legacy FX    Present") + ls
                                    + "Midi Channels   " + (hasMidi() ? IntPool.get(32) : IntPool.get(16)) + ls
                                    + (hasDigitalIO() ? "Digital IO      Present" : "No Digital IO   Present") + ls
                                    + (hasPresetFlash() ? "Preset Flash    Present" : "No Preset Flash Present") + ls
                                    + (hasADAT() ? "ADAT            Present" : "No ADAT         Present") + ls
                                    + "Sample RAM      " + df3.format(getSampleRAM()) + " MB" + ls
                                    + "Sample ROM      " + df3.format(getSampleROM()) + " MB" + ls
                                    + "Sample Flash    " + df3.format(getSampleFlash()) + " MB" + ls;
                        }
                    };
                // 4 reserved bytes for future expansion not handled
            }
            return null;
        }

        private static Integer[] filterParameterValuesReq(ByteStreamable o, byte[] rmtHeader) {
            ByteArrayInputStream is = o.getByteStream();
            byte[] header = new byte[rmtHeader.length];

            if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x01) {
                int bc = is.read() * 2;
                byte[] data = new byte[bc];
                if (is.read(data, 0, bc) == bc) {
                    Integer[] output = new Integer[bc / 2];
                    int i = 0;
                    for (int n = 0; n < bc; n += 2)
                        output[i++] = SysexHelper.DataIn(data[n], data[n + 1]);

                    return output;
                }
            }
            return null;
        }

        private static Object filterPresetDumpReq(ByteStreamable o, byte[] rmtHeader) {
            Object robj = null;
            final ByteArrayInputStream is = o.getByteStream();
            byte[] data = new byte[rmtHeader.length];
            final int msgSize = is.available();
            if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data)) {
                int c = is.read();
                switch (c) {
                    // NEW DUMP HEADER or NEW DUMP DATA
                    case 0x0d:
                        {
                            int subCommand = is.read();
                            // NEW DUMP HEADER
                            if (subCommand == NEW_PRESET_DUMP_HEADER_SUB_CMD) {
                                //System.out.println("NEW DUMP HEADER" + " " + d);
                                final byte[] words = new byte[14];
                                if (is.read(words, 0, 14) == 14) {
                                    return new NewDumpHeader() {
                                        public int getPreset() {
                                            return SysexHelper.DataIn(words[0], words[1]).intValue();
                                        }

                                        public int getNumDataBytes() {
                                            return SysexHelper.UnsignedLongDataIn_int(words[2], words[3], words[4], words[5]);
                                        }

                                        public int getNumGlobalParams() {
                                            return SysexHelper.DataIn(words[6], words[7]).intValue();
                                        }

                                        public int getNumLinkParams() {
                                            return SysexHelper.DataIn(words[8], words[9]).intValue();
                                        }

                                        public int getNumVoiceParams() {
                                            return SysexHelper.DataIn(words[10], words[11]).intValue();
                                        }

                                        public int getNumZoneParams() {
                                            return SysexHelper.DataIn(words[12], words[13]).intValue();
                                        }

                                        public byte[] getHeaderBytes() {
                                            return words;
                                        }
                                    };
                                }
                            }
                            // NEW DUMP DATA
                            else if (subCommand == NEW_PRESET_DUMP_DATA_SUB_CMD) {
                                final byte[] pc = new byte[2];
                                int nDataBytes = msgSize - 11; // 5 bytes for header, 2 command bytes,2 packet bytes, 1 checksum byte and 1 EOX byte
                                final byte[] dataBytes = new byte[nDataBytes];
                                if (is.read(pc, 0, 2) == 2 && is.read(dataBytes, 0, nDataBytes) == nDataBytes) {
                                    //  System.out.println(NEW_DUMP_DATA_REPLY + " " + d + " " + SysexHelper.DataIn(pc).intValue());
                                    return new NewDumpData() {
                                        public int getPacketCount() {
                                            return SysexHelper.DataIn(pc).intValue();
                                        }

                                        public byte[] getData() {
                                            return dataBytes;
                                        }

                                        public int getByteCount() {
                                            return dataBytes.length;
                                        }

                                        public int compareTo(Object o) {
                                            if (o instanceof NewDumpData) {
                                                NewDumpData ndd = (NewDumpData) o;
                                                if (ndd.getPacketCount() < getPacketCount())
                                                    return 1;
                                                else if (ndd.getPacketCount() > getPacketCount())
                                                    return -1;
                                                else
                                                    return 0;
                                            }
                                            return 1;
                                        }
                                    };
                                }
                            }
                            break;
                        }

                        // NEW ACK
                    case ACK_CMD:
                        {
                            final byte[] packet = new byte[2];
                            // System.out.println(NEW_ACK_REPLY);
                            return new NewDumpStatus() {
                                public int getStatus() {
                                    return NEW_ACK;
                                }

                                public int getPacketNumber() {
                                    is.read(packet, 0, 2);
                                    return (int) SysexHelper.DataIn(packet).intValue();
                                }
                            };
                        }
                        // NEW NACK
                    case NACK_CMD:
                        {
                            final byte[] packet = new byte[2];
                            // System.out.println(NEW_NACK_REPLY);
                            return new NewDumpStatus() {
                                public int getStatus() {
                                    return NEW_NACK;
                                }

                                public int getPacketNumber() {
                                    is.read(packet, 0, 2);
                                    return SysexHelper.DataIn(packet).intValue();
                                }
                            };
                        }
                        // WAIT
                    case WAIT_CMD:
                        // System.out.println(WAIT_REPLY);
                        return new NewDumpStatus() {
                            public int getStatus() {
                                return WAIT;
                            }

                            public int getPacketNumber() {
                                return -1;
                            }
                        };
                        // CANCEL
                    case CANCEL_CMD:
                        // System.out.println(CANCEL_REPLY);
                        return new NewDumpStatus() {
                            public int getStatus() {
                                return CANCEL;
                            }

                            public int getPacketNumber() {
                                return -1;
                            }
                        };
                        // EOF
                    case EOF_CMD:
                        // System.out.println(EOF_REPLY);
                        return new NewDumpStatus() {
                            public int getStatus() {
                                return EOF;
                            }

                            public int getPacketNumber() {
                                return -1;
                            }
                        };
                }
            }
            return robj;
        }

        private static MinMaxDefault filterMMDReq(ByteStreamable o, byte[] rmtHeader) {
            ByteArrayInputStream is = o.getByteStream();
            byte[] data = new byte[rmtHeader.length];
            if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x04) {
                final byte[] info = new byte[8];
                if (is.read(info, 0, 8) == 8)
                    return new MinMaxDefault() {
                        public int id = getID().intValue();
                        public int min = getMin().intValue();
                        public int max = getMax().intValue();
                        public int def = getDefault().intValue();

                        public boolean equals(Object obj) {
                            if (obj instanceof MinMaxDefault) {
                                MinMaxDefault mmd = (MinMaxDefault) obj;
                                return (mmd.getID().equals(getID()) && mmd.getMin().equals(getMin()) && mmd.getMax().equals(getMax()) && mmd.getDefault().equals(getDefault()));
                            }
                            return false;
                        }

                        public Integer getID() {
                            return SysexHelper.DataIn(info[0], info[1]);
                        }

                        public Integer getMin() {
                            return SysexHelper.DataIn(info[2], info[3]);
                        }

                        public Integer getMax() {
                            return SysexHelper.DataIn(info[4], info[5]);
                        }

                        public Integer getDefault() {
                            return SysexHelper.DataIn(info[6], info[7]);
                        }

                        public String toString() {
                            RMF s = new RMF("PARAMETER_MMD", LOCAL_TAG).addLine(String.valueOf(id));
                            s.addLine("MIN =" + getMin().toString());
                            s.addLine("MAX =" + getMax().toString());
                            s.addLine("DEF =" + getDefault().toString());
                            return s.toString();
                        }
                    };
            }
            return null;
        }
    }

}