/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMultimodeChannelException;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterModelProvider;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadablePresetZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadableVoiceZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadableZoneZCommandMarker;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.ContextElement;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.tasking.Ticket;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.Serializable;
import java.util.Set;


/**
 * @author pmeehan
 */

public interface ReadablePreset extends ContextElement, PresetModel, IconAndTipCarrier, ParameterModelProvider, Serializable, ZCommandProvider {

    final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ReadablePresetZCommandMarker.class);

    public ReadablePreset getReadablePresetDowngrade();

    public ReadablePreset getMostCapableNonContextEditablePreset();

    public boolean isUser();

    public boolean isEmpty() throws PresetException;

    public boolean isPending() throws PresetException;

    public boolean isInitializing() throws PresetException;

    public void performOpenAction(boolean activate);

    public boolean isSamePresetContext(ReadablePreset p);

    public boolean isSameDevice(ReadablePreset p);

    public void assertRemote() throws PresetException;

    public void assertInitialized(boolean refreshEmpty) throws PresetException;

    // EVENTS
    public void addListener(PresetListener pl);

    public void removeListener(PresetListener pl);

    // UTILITY
    public DeviceParameterContext getDeviceParameterContext() throws DeviceException;

    public DeviceContext getDeviceContext();

    public void sendToMultiMode(Integer ch) ;

    public void setToStringFormatExtended(boolean extended);

    // PRESET

    public Ticket audition();

    public Set<Integer> getPresetSet() throws PresetException, EmptyException;

    public IntegerUseMap getSampleUsage() throws PresetException, EmptyException;

    public IntegerUseMap getLinkedPresetUage() throws PresetException, EmptyException;

    public IsolatedPreset getIsolated() throws PresetException, EmptyException;

    public void refresh();

    public boolean isInitialized() throws PresetException;

    public double getInitializationStatus() throws PresetException, EmptyException;

    public String getString() throws PresetException;

    public String getName() throws PresetException, EmptyException;

    public String getDisplayName() throws PresetException;

    public Integer getIndex();

    public Integer[] getPresetParams(Integer[] ids) throws PresetException, EmptyException, ParameterException;

    public int numPresetZones() throws PresetException, EmptyException;

    public int numPresetSamples() throws PresetException, EmptyException;

    // VOICE
    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer voice) throws PresetException, EmptyException;

    public void refreshVoiceParameters(Integer voice, Integer[] ids) throws PresetException;

    public Integer[] getVoiceIndexesInGroupFromVoice(Integer voice) throws EmptyException, PresetException;

    public Integer[] getVoiceIndexesInGroup(Integer group) throws EmptyException, PresetException;

    public int numVoices() throws PresetException, EmptyException;

    public Integer[] getGroupParams(Integer group, Integer[] ids) throws PresetException, EmptyException, ParameterException;

    public Integer[] getVoiceParams(Integer voice, Integer[] ids) throws PresetException, EmptyException, ParameterException;

    // LINK
    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer link) throws PresetException, EmptyException;

    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer voice, Integer zone) throws PresetException, EmptyException;

    public int numLinks() throws PresetException, EmptyException;

    public Integer[] getLinkParams(Integer link, Integer[] ids) throws PresetException, EmptyException, ParameterException;

    // ZONE
    public int numZones(Integer voice) throws PresetException, EmptyException;

    public Integer[] getZoneParams(Integer voice, Integer zone, Integer[] ids) throws EmptyException, ParameterException, PresetException;

    public ReadableVoice getReadableVoice(Integer voice);

    public ReadableLink getReadableLink(Integer link);

    public ReadableParameterModel getParameterModel(Integer id) throws ParameterException;

    // SUB INTERFACES
    public interface ReadableVoice extends Comparable, ParameterModelProvider, ZCommandProvider, Serializable {
        final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ReadableVoiceZCommandMarker.class);

        public ReadablePreset getPreset();

        public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer zone) throws PresetException, EmptyException;

        public Integer[] getVoiceParams(Integer[] ids) throws PresetException, EmptyException, ParameterException;

        public Integer getVoiceNumber();

        public void setVoiceNumber(Integer voice);

        public void performOpenAction();

        public Integer getPresetNumber();

        public IsolatedPreset.IsolatedVoice getIsolated() throws EmptyException, PresetException;

        public ReadableZone getReadableZone(Integer zone);

        public int numZones() throws EmptyException, PresetException;

        public ReadableParameterModel getParameterModel(Integer id) throws ParameterException;

        public Integer[] getVoiceIndexesInGroup() throws EmptyException, PresetException;

        public interface ReadableZone extends Comparable, ParameterModelProvider, Serializable {
            final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ReadableZoneZCommandMarker.class);

            public Integer[] getZoneParams(Integer[] ids) throws PresetException, EmptyException, ParameterException;

            public Integer getVoiceNumber();

            public ReadableVoice getVoice();

            public Integer getPresetNumber();

            public Integer getZoneNumber();

            public ReadablePreset getPreset();

            public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolated() throws EmptyException, PresetException;

            public void setZoneNumber(Integer zone);

            public ReadableParameterModel getParameterModel(Integer id) throws ParameterException;
        }
    }

    public interface ReadableLink extends Comparable, ParameterModelProvider, Serializable {
        public Integer[] getLinkParams(Integer[] ids) throws PresetException, EmptyException, ParameterException;

        public Integer getLinkNumber();

        public void setLinkNumber(Integer link);

        public IsolatedPreset.IsolatedLink getIsolated() throws EmptyException, PresetException;

        public ReadablePreset getPreset();

        public Integer getPresetNumber();

        public ReadableParameterModel getParameterModel(Integer id) throws ParameterException;
    }
}
