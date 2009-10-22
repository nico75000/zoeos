package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.comms.ByteStreamable;
import com.pcmsolutions.comms.RemoteDeviceDidNotRespondException;
import com.pcmsolutions.comms.RemoteMessagingException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.*;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.system.StandardStateMachine;
import com.pcmsolutions.smdi.*;
import com.pcmsolutions.gui.ProgressCallback;

import javax.sound.midi.MidiMessage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFileFormat;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author  pmeehan
 * @version
 */
public interface Remotable extends StandardStateMachine {

    byte BOX = (byte) 0xF0;
    byte EOX = (byte) 0xF7;
    byte EMU_ID = (byte) 0x18;
    byte E4_ID = (byte) 0x21;
    byte PEPTALK_ID = (byte) 0x7F;

    static byte EDITOR_ID = (byte) 0x55;

    byte getDeviceId();

    boolean isSmdiCoupled();
    
    RemotePreferences getRemotePreferences();

    Master getMasterContext();

    Peptalk getPeptalkContext();

    Preset getPresetContext();

    Voice getVoiceContext();

    Link getLinkContext();

    Zone getZoneContext();

    Sample getSampleContext();

    Smdi getSmdiContext();

    String makeDeviceProgressTitle(String str);

    Parameter getParameterContext();

    Object getIdentityMessage();

    File getDeviceLocalDir();

    Object getIdentityMessageReadable();

    String getMoniker();

    String getName();

    void setName(String name);

    Object getInportIdentifier();

    Object getOutportIdentifier();

    ParameterEditLoader getEditLoader();

    double getDeviceVersion();

    void addMessageListener(RemoteEventListener rml);

    void removeMessageListener(RemoteEventListener rml);

    public static interface Master {

        void sendMidiMessage(MidiMessage m) throws RemoteUnreachableException;

        void cmd_bankErase() throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_bankEraseAllPresets() throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_bankEraseAllSamples() throws RemoteUnreachableException, RemoteMessagingException;

        MultiModeMap req_multimodeMap() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        void cmd_multimodeMap(MultiModeMap map) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        void edit_multimodeMap(ByteStreamable map) throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_sampleDefrag() throws RemoteUnreachableException, RemoteMessagingException;

        PresetMemory req_presetMemory() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        SampleMemory req_sampleMemory() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        DeviceConfig req_deviceConfig() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        DeviceExConfig req_deviceExConfig() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

    }

    public static interface Parameter {

        Integer req_prmValue(Integer id) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        Integer[] req_prmValues(Integer[] ids) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        Integer[] req_prmValues(Integer[] ids, boolean returnIdVals) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        void edit_prmValue(Integer id, Integer value) throws RemoteUnreachableException, RemoteMessagingException;

        void edit_prmValues(Integer[] idValues) throws IllegalStateException, RemoteUnreachableException, RemoteMessagingException;

        void edit_prmValues(byte[] readyIdValues) throws IllegalStateException, RemoteUnreachableException, RemoteMessagingException;

        MinMaxDefault req_prmMMD(Integer id) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

    }

    public static interface DeviceConfig extends Serializable {

        boolean hasMidi();

        Integer getVoices();

        boolean hasFX();

        boolean hasOctopus();

        boolean hasDigitalIO();

        Integer getSampleRAM();

    }

    public static interface DeviceExConfig extends DeviceConfig, Serializable {

        boolean hasPresetFlash();

        boolean hasADAT();

        Integer getSampleROM();

        Integer getSampleFlash();

    }

    public static interface Preset {

        void edit_dump(ByteArrayInputStream dump, DumpMonitor mon) throws RemoteUnreachableException, RemoteMessagingException, RemoteDeviceDidNotRespondException, IOException;

        ByteArrayInputStream req_dump(Integer preset, DumpMonitor mon) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException, EmptyException;

        void edit_name(Integer preset, String name) throws RemoteUnreachableException, RemoteMessagingException;

        String req_name(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        void edit_nameChar(Integer preset, Integer pos, char c);

        char req_nameChar(Integer preset, Integer pos) throws RemoteDeviceDidNotRespondException, RemoteMessagingException;

        Integer req_numVoices(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        Integer req_numLinks(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        Integer req_numZones(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        void cmd_newLink(Integer preset) throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_combineVoices(Integer preset, Integer group) throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_copy(Integer sourcePreset, Integer destPreset) throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_erase(Integer preset) throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_newVoice(Integer preset) throws RemoteUnreachableException, RemoteMessagingException;

    }

    public static interface Sample {

        void edit_name(Integer sample, String name) throws RemoteMessagingException, RemoteUnreachableException;

        String req_name(Integer sample) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        void edit_nameChar(Integer sample, Integer pos, char c);

        char req_nameChar(Integer sample, Integer pos);

        void cmd_delete(Integer sample) throws RemoteUnreachableException, RemoteMessagingException;
    }

    public static interface Smdi{
        void sendSync(AudioInputStream ais, int sampleNum, String sampleName, int packetSize,ProgressCallback prog) throws SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, UnsupportedAudioFileException, IOException, SmdiUnsupportedConversionException, SmdiSampleEmptyException, SmdiTransferAbortedException, SmdiUnavailableException;

        void recvSync(SMDIAgent.SampleInputStreamHandler ish,AudioFileFormat.Type fileType, int sampleNum,int packetSize,  ProgressCallback prog) throws SmdiOutOfRangeException, TargetNotSMDIException, SmdiGeneralException, SmdiNoMemoryException, SmdiSampleEmptyException, SmdiUnavailableException;

        byte[] sendMidiMessage(MidiMessage m) throws TargetNotSMDIException, SmdiGeneralException, SmdiUnavailableException;

        boolean deleteSample(int sampleNum);

        SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiUnavailableException;
    }

    public static interface Link {

        void cmd_delete(Integer preset, Integer link) throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_copy(Integer preset, Integer link, Integer detsPreset) throws RemoteUnreachableException, RemoteMessagingException;
    }

    public static interface Voice {

        Integer req_numZones(Integer preset, Integer voice) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        void cmd_newZone(Integer preset, Integer voice) throws RemoteMessagingException, RemoteUnreachableException;

        void cmd_expandVoice(Integer preset, Integer voice) throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_copy(Integer preset, Integer voice, Integer destPreset, Integer group) throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_delete(Integer preset, Integer voice) throws RemoteUnreachableException, RemoteMessagingException;

        void cmd_getMultisample(Integer sourcePreset, Integer sourceVoice, Integer destPreset, Integer destVoice) throws RemoteUnreachableException, RemoteMessagingException;

    }

    public static interface Zone {

        void cmd_delete(Integer preset, Integer voice, Integer sample) throws RemoteUnreachableException, RemoteMessagingException;
    }

    public static interface Peptalk{
        void openSession() throws RemoteUnreachableException, RemoteMessagingException;
        void closeSession() throws RemoteUnreachableException, RemoteMessagingException;
        void noteOn(byte note, byte vel, byte track) throws RemoteUnreachableException, RemoteMessagingException;
        void noteOff(byte note, byte vel, byte track) throws RemoteUnreachableException, RemoteMessagingException;
        void note(byte note, byte onVel, byte offVel, byte track, int gateInMs);
        void pressRightArrow() throws RemoteUnreachableException, RemoteMessagingException;
        void releaseRightArrow() throws RemoteUnreachableException, RemoteMessagingException;
        void pressSampleEdit() throws RemoteUnreachableException, RemoteMessagingException;
        void releaseSampleEdit() throws RemoteUnreachableException, RemoteMessagingException;
        void pressExit() throws RemoteUnreachableException, RemoteMessagingException;
        void releaseExit() throws RemoteUnreachableException, RemoteMessagingException;
    }

    public interface PresetMemory extends Serializable {

        Integer getPresetMemory();

        Integer getPresetFreeMemory();

    }

    public interface SampleMemory extends Serializable {

        Integer getSampleMemory();

        Integer getSampleFreeMemory();

    }
}

