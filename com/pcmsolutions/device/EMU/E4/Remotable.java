package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.ByteStreamable;
import com.pcmsolutions.comms.RemoteDeviceDidNotRespondException;
import com.pcmsolutions.comms.RemoteMessagingException;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.system.StandardStateMachine;
import com.pcmsolutions.system.ZDisposable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author  pmeehan
 * @version
 */
public interface Remotable extends StandardStateMachine {

    public static byte BOX = (byte) 0xF0;
    public static byte EOX = (byte) 0xF7;
    public static byte EMU_ID = (byte) 0x18;
    public static byte E4_ID = (byte) 0x21;

    public static byte EDITOR_ID = (byte) 0x55;

    public byte getDeviceId();

    public RemotePreferences getRemotePreferences();

    public Master getMasterContext();

    public Preset getPresetContext();

    public Voice getVoiceContext();

    public Link getLinkContext();

    public Zone getZoneContext();

    public Sample getSampleContext();

    public String makeDeviceProgressTitle(String str);

    public Parameter getParameterContext();

    public Object getIdentityMessage();

    public File getDeviceLocalDir();

    public Object getIdentityMessageReadable();

    public String getMoniker();

    public String getName();

    public void setName(String name);

    public Object getInportIdentifier();

    public Object getOutportIdentifier();

    public ParameterEditLoader getEditLoader();

    public double getDeviceVersion();

    public void addMessageListener(RemoteEventListener rml);

    public void removeMessageListener(RemoteEventListener rml);

    public static interface Master {

        public void cmd_bankErase() throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_bankEraseAllPresets() throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_bankEraseAllSamples() throws RemoteUnreachableException, RemoteMessagingException;

        public MultiModeMap req_multimodeMap() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public void edit_multimodeMap(ByteStreamable map) throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_sampleDefrag() throws RemoteUnreachableException, RemoteMessagingException;

        public PresetMemory req_presetMemory() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public SampleMemory req_sampleMemory() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public DeviceConfig req_deviceConfig() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public DeviceExConfig req_deviceExConfig() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

    }

    public static interface Parameter {

        public Integer req_prmValue(Integer id) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public Integer[] req_prmValues(Integer[] ids) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public Integer[] req_prmValues(Integer[] ids, boolean returnIdVals) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public void edit_prmValue(Integer id, Integer value) throws RemoteUnreachableException, RemoteMessagingException;

        public void edit_prmValues(Integer[] idValues) throws IllegalStateException, RemoteUnreachableException, RemoteMessagingException;

        public void edit_prmValues(byte[] readyIdValues) throws IllegalStateException, RemoteUnreachableException, RemoteMessagingException;

        public MinMaxDefault req_prmMMD(Integer id) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

    }

    public static interface DeviceConfig extends Serializable {

        public boolean hasMidi();

        public Integer getVoices();

        public boolean hasFX();

        public boolean hasOctopus();

        public boolean hasDigitalIO();

        public Integer getSampleRAM();

    }

    public static interface DeviceExConfig extends DeviceConfig, Serializable {

        public boolean hasPresetFlash();

        public boolean hasADAT();

        public Integer getSampleROM();

        public Integer getSampleFlash();

    }

    public static interface Preset {

        public void edit_dump(ByteArrayInputStream dump, PresetInitializationMonitor mon) throws RemoteUnreachableException, RemoteMessagingException, RemoteDeviceDidNotRespondException, IOException;

        public ByteArrayInputStream req_dump(Integer preset, PresetInitializationMonitor mon) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException, PresetEmptyException;

        public void edit_name(Integer preset, String name) throws RemoteUnreachableException, RemoteMessagingException;

        public String req_name(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public void edit_nameChar(Integer preset, Integer pos, char c);

        public char req_nameChar(Integer preset, Integer pos) throws RemoteDeviceDidNotRespondException, RemoteMessagingException;

        public Integer req_numVoices(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public Integer req_numLinks(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public Integer req_numZones(Integer preset) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public void cmd_newLink(Integer preset) throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_combineVoices(Integer preset, Integer group) throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_copy(Integer sourcePreset, Integer destPreset) throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_erase(Integer preset) throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_newVoice(Integer preset) throws RemoteUnreachableException, RemoteMessagingException;

    }

    public static interface Sample {

        public void edit_name(Integer sample, String name) throws RemoteMessagingException, RemoteUnreachableException;

        public String req_name(Integer sample) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public void edit_nameChar(Integer sample, Integer pos, char c);

        public char req_nameChar(Integer sample, Integer pos);

        public void cmd_delete(Integer sample) throws RemoteUnreachableException, RemoteMessagingException;
    }

    public static interface Link {

        public void cmd_delete(Integer preset, Integer link) throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_copy(Integer preset, Integer link, Integer detsPreset) throws RemoteUnreachableException, RemoteMessagingException;
    }

    public static interface Voice {

        public Integer req_numZones(Integer preset, Integer voice) throws RemoteDeviceDidNotRespondException, RemoteMessagingException, RemoteUnreachableException;

        public void cmd_newZone(Integer preset, Integer voice) throws RemoteMessagingException, RemoteUnreachableException;

        public void cmd_expandVoice(Integer preset, Integer voice) throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_copy(Integer preset, Integer voice, Integer destPreset, Integer group) throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_delete(Integer preset, Integer voice) throws RemoteUnreachableException, RemoteMessagingException;

        public void cmd_getMultisample(Integer sourcePreset, Integer sourceVoice, Integer destPreset, Integer destVoice) throws RemoteUnreachableException, RemoteMessagingException;

    }

    public static interface Zone {

        public void cmd_delete(Integer preset, Integer voice, Integer sample) throws RemoteUnreachableException, RemoteMessagingException;
    }

    public interface PresetMemory extends Serializable {

        public Integer getPresetMemory();

        public Integer getPresetFreeMemory();

    }

    public interface SampleMemory extends Serializable {

        public Integer getSampleMemory();

        public Integer getSampleFreeMemory();

    }
}

