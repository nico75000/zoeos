/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMidiChannelException;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadableVoiceZCommandMarker;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.Serializable;
import java.util.Set;


/**
 *
 * @author  pmeehan
 */

public interface ReadablePreset extends PresetModel, IconAndTipCarrier, ParameterModelProvider, Serializable, ZCommandProvider {

    public ReadablePreset getReadablePresetDowngrade();

    public ReadablePreset getMostCapableNonContextEditablePresetDowngrade();

    public void performDefaultAction();

    public void performOpenAction();

    public boolean isSamePresetContext(ReadablePreset p);

    public boolean isSameDevice(ReadablePreset p);

    public void assertPresetRemote() throws NoSuchPresetException;

    public void assertPresetInitialized() throws NoSuchPresetException;

    // EVENTS
    public void addPresetListener(PresetListener pl);

    public void removePresetListener(PresetListener pl);

    // UTILITY
    public DeviceParameterContext getDeviceParameterContext() throws ZDeviceNotRunningException;

    public DeviceContext getDeviceContext();

    public void sendToMultiMode(Integer ch) throws IllegalMidiChannelException;

    public void setToStringFormatExtended(boolean extended);

    // PRESET
    public Set getPresetSet() throws NoSuchPresetException, PresetEmptyException;

    public IntegerUseMap presetSampleUsage() throws NoSuchPresetException, PresetEmptyException;

    public IntegerUseMap presetLinkPresetUsage() throws NoSuchPresetException, PresetEmptyException;

    public IsolatedPreset getIsolated() throws NoSuchPresetException, PresetEmptyException;

    public void refreshPreset() throws NoSuchPresetException;

    public void unlockPreset();

    public boolean isPresetInitialized() throws NoSuchPresetException;

    public int getPresetState() throws NoSuchPresetException;

    public double getInitializationStatus() throws NoSuchPresetException, PresetEmptyException;

    public boolean isPresetWriteLocked() throws NoSuchPresetException, PresetEmptyException;

    public String getPresetName() throws NoSuchPresetException, PresetEmptyException;

    public String getPresetDisplayName() throws NoSuchPresetException;

    public Integer getPresetNumber();

    public Integer[] getPresetParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException;

    public int numPresetZones() throws NoSuchPresetException, PresetEmptyException;

    public int numPresetSamples() throws NoSuchPresetException, PresetEmptyException;

    // VOICE
    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException;

    public void refreshVoiceParameters(Integer voice, Integer[] ids) throws NoSuchContextException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException, ParameterValueOutOfRangeException, IllegalParameterIdException;

    public Integer[] getVoiceIndexesInGroupFromVoice(Integer voice) throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException;

    public Integer[] getVoiceIndexesInGroup(Integer group) throws PresetEmptyException, NoSuchPresetException, NoSuchGroupException;

    public int numVoices() throws NoSuchPresetException, PresetEmptyException;

    public Integer[] getGroupParams(Integer group, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException;

    public Integer[] getVoiceParams(Integer voice, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException;

    // LINK
    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer link) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException;

    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer voice, Integer zone) throws NoSuchZoneException, NoSuchPresetException, PresetEmptyException, NoSuchVoiceException;

    public int numLinks() throws NoSuchPresetException, PresetEmptyException;

    public Integer[] getLinkParams(Integer link, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException;

    // ZONE
    public int numZones(Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException;

    public Integer[] getZoneParams(Integer voice, Integer zone, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException;

    public ReadableVoice getReadableVoice(Integer voice);

    public ReadableLink getReadableLink(Integer link);

    public ReadableParameterModel[] getAllParameterModels();

    public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException;

    // SUB INTERFACES
    public interface ReadableVoice extends Comparable, ParameterModelProvider, ZCommandProvider, Serializable {
        public final static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ReadableVoiceZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.ViewVoiceZMTC;");

        public ReadablePreset getPreset();

        public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer zone) throws NoSuchZoneException, NoSuchPresetException, PresetEmptyException, NoSuchVoiceException;

        public Integer[] getVoiceParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException;

        public Integer getVoiceNumber();

        public void setVoiceNumber(Integer voice);

        public void performOpenAction();

        public Integer getPresetNumber();

        public IsolatedPreset.IsolatedVoice getIsolated() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException;

        public ReadableZone getReadableZone(Integer zone);

        public int numZones() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException;

        public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException;

        public Integer[] getVoiceIndexesInGroup() throws PresetEmptyException, NoSuchContextException, NoSuchVoiceException, NoSuchPresetException;

        public interface ReadableZone extends Comparable, ParameterModelProvider, Serializable {
            public Integer[] getZoneParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException;

            public Integer getVoiceNumber();

            public ReadableVoice getVoice();

            public Integer getPresetNumber();

            public Integer getZoneNumber();

            public ReadablePreset getPreset();

            public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolated() throws PresetEmptyException, NoSuchZoneException, NoSuchVoiceException, NoSuchPresetException;

            public void setZoneNumber(Integer zone);

            public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException;
        }
    }

    public interface ReadableLink extends Comparable, ParameterModelProvider, Serializable {
        public Integer[] getLinkParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException;

        public Integer getLinkNumber();

        public void setLinkNumber(Integer link);

        public IsolatedPreset.IsolatedLink getIsolated() throws PresetEmptyException, NoSuchPresetException, NoSuchLinkException;

        public ReadablePreset getPreset();

        public Integer getPresetNumber();

        public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException;
    }
}
