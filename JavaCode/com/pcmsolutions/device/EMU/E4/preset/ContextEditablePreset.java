/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModelProvider;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextEditablePresetZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4EditableLinkZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4EditableVoiceZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4EditableZoneZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.preset.ViewVoiceZMTC;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;

import java.util.Map;

/**
 * @author pmeehan
 */

public interface ContextEditablePreset extends ContextBasicEditablePreset, Comparable, EditableParameterModelProvider {
    final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextEditablePresetZCommandMarker.class, ContextBasicEditablePreset.cmdProviderHelper);

    public ContextBasicEditablePreset getContextBasicEditablePresetDowngrade();

    // PRESET
    public void offsetLinkIndexes(Integer offset, boolean user) throws EmptyException, PresetException;

    public void offsetSampleIndexes(Integer offset, boolean user) throws EmptyException, PresetException;

    public void remapLinkIndexes(Map translationMap) throws EmptyException, PresetException;

    public void remapSampleIndexes(Map translationMap) throws EmptyException, PresetException;

    public void newPreset(Integer preset, String name) throws PresetException;

    public void newPreset(Integer preset, String name, IsolatedPreset ip) throws PresetException;

    public void setPresetParam(Integer id, Integer value) throws PresetException;

    public void offsetPresetParam(Integer id, Integer offset) throws PresetException;

    public void offsetPresetParam(Integer id, Double offsetAsFOR) throws PresetException;

    public void combineVoices(Integer group) throws EmptyException, PresetException;

    public void purgeZones() throws EmptyException, PresetException;

    public void purgeLinks() throws PresetException;

    public void purgeEmpties() throws PresetException;

    public void applySampleToPreset(Integer sample, int mode) throws EmptyException, ParameterException, PresetException;

    public void sortVoices(Integer[] ids) throws PresetException;

    public void sortLinks(Integer[] ids) throws PresetException;

    public void sortZones(Integer[] ids) throws PresetException;
    // VOICE

    public void newVoice(IsolatedPreset.IsolatedVoice iv) throws PresetException;

    public void newVoices(Integer num, Integer[] sampleNums) throws PresetException;

    public void rmvVoices(Integer[] voices) throws PresetException;

    public void copyVoice(Integer srcVoice, Integer group) throws PresetException;

    public void setVoiceParam(Integer voice, Integer id, Integer value) throws PresetException;

    public void offsetVoiceParam(Integer voice, Integer id, Integer offset) throws PresetException;

    public void offsetVoiceParam(Integer voice, Integer id, Double offsetAsFOR) throws PresetException;

    public void setGroupParamFromVoice(Integer voice, Integer id, Integer value) throws PresetException;

    public void offsetGroupParamFromVoice(Integer voice, Integer id, Integer offset) throws PresetException;

    public void offsetGroupParamFromVoice(Integer voice, Integer id, Double offsetAsFOR) throws PresetException;

    public void setGroupParam(Integer group, Integer id, Integer value) throws PresetException;

    public void expandVoice(Integer voice) throws PresetException;

    public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException;

    // LINK
    public void newLink(IsolatedPreset.IsolatedLink il) throws PresetException;

    public void newLinks(Integer[] presetNums) throws PresetException;

    public void rmvLinks(Integer[] links) throws PresetException;

    public void copyLink(Integer srcLink) throws EmptyException, PresetException;

    public void setLinkParam(Integer link, Integer id, Integer value) throws PresetException;

    public void offsetLinkParam(Integer link, Integer id, Integer offset) throws PresetException;

    public void offsetLinkParam(Integer link, Integer id, Double offsetAsFOR) throws PresetException;

    // ZONE
    public void newZone(Integer voice, IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws PresetException;

    public void newZones(Integer voice, Integer num) throws PresetException;

    public void rmvZones(Integer voice, Integer[] zones) throws PresetException;

    public void setZoneParam(Integer voice, Integer zone, Integer id, Integer value) throws PresetException;

    public void offsetZoneParam(Integer voice, Integer zone, Integer id, Integer offset) throws PresetException;

    public void offsetZoneParam(Integer voice, Integer zone, Integer id, Double offsetAsFOR) throws PresetException;

    public EditableVoice getEditableVoice(Integer voice);

    public EditableLink getEditableLink(Integer link);

    // SUB INTERFACES
    public interface EditableVoice extends ReadablePreset.ReadableVoice, ZCommandProvider, EditableParameterModelProvider {
        final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4EditableVoiceZCommandMarker.class, ReadableVoice.cmdProviderHelper);

        public EditableVoice duplicate();

        public void setGroupMode(boolean groupMode);

        public boolean getGroupMode();

        public void trySetOriginalKeyFromSampleName() throws PresetException, ParameterException;

        public void setVoiceParam(Integer id, Integer value) throws PresetException;

        public void offsetVoiceParam(Integer id, Integer offset) throws PresetException;

        public void offsetVoiceParam(Integer id, Double offsetAsFOR) throws PresetException;

        public int numZones() throws EmptyException, PresetException;

        public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException;

        public void removeVoice() throws PresetException;

        public void splitVoice(int splitKey) throws PresetException;

        public void expandVoice() throws PresetException;

        public void combineVoiceGroup() throws EmptyException, PresetException, ParameterException;

        public void copyVoice() throws PresetException, EmptyException, ParameterException;

        public void newZones(Integer num) throws PresetException;

        public PresetContext getPresetContext();

        public EditableZone getEditableZone(Integer zone);

        public interface EditableZone extends ReadableZone, ZCommandProvider {
            final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4EditableZoneZCommandMarker.class, ReadableZone.cmdProviderHelper);

            public void setZoneParam(Integer id, Integer value) throws EmptyException, ParameterException, PresetException;

            public void offsetZoneParam(Integer id, Integer offset) throws PresetException;

            public void offsetZoneParam(Integer id, Double offsetAsFOR) throws PresetException;

            public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException;

            public void trySetOriginalKeyFromSampleName() throws EmptyException, ParameterException, PresetException;

            public void removeZone() throws PresetException;

            public PresetContext getPresetContext();
        }
    }

    public interface EditableLink extends ReadablePreset.ReadableLink, ZCommandProvider, EditableParameterModelProvider {
        final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4EditableLinkZCommandMarker.class);

        public void setLinkParam(Integer id, Integer value) throws PresetException;

        public void offsetLinkParam(Integer id, Integer offset) throws PresetException;

        public void offsetLinkParam(Integer id, Double offsetAsFOR) throws PresetException;

        public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException;

        public void removeLink() throws PresetException;

        public void copyLink() throws EmptyException, PresetException;

        public PresetContext getPresetContext();
    }
}
