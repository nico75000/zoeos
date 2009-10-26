/*
 * IsloatedPreset.java
 *
 * Created on February 9, 2003, 2:16 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextEditablePresetZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4EditableLinkZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4EditableVoiceZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4EditableZoneZCommandMarker;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;

import java.util.Map;

/**
 *
 * @author  pmeehan
 */

public interface ContextEditablePreset extends ContextBasicEditablePreset, Comparable, EditableParameterModelProvider {
    public static final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextEditablePresetZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.PurgePresetZonesZMTC;com.pcmsolutions.device.EMU.E4.zcommands.NewPresetZMTC;com.pcmsolutions.device.EMU.E4.zcommands.NewPresetVoicesZMTC;com.pcmsolutions.device.EMU.E4.zcommands.NewPresetLinksZMTC;com.pcmsolutions.device.EMU.E4.zcommands.OffsetPresetLinksZMTC;com.pcmsolutions.device.EMU.E4.zcommands.OffsetPresetSamplesZMTC;com.pcmsolutions.device.EMU.E4.zcommands.MatchOriginalKeysToSampleNameZMTC;com.pcmsolutions.device.EMU.E4.zcommands.SortPresetZMTC;com.pcmsolutions.device.EMU.E4.zcommands.AutoMapPresetZMTC;com.pcmsolutions.device.EMU.E4.zcommands.AssertRemotePresetZMTC;com.pcmsolutions.device.EMU.E4.zcommands.LoadPresetPackageZMTC");
    //REMOVED:   com.pcmsolutions.device.EMU.E4.zcommands.DefaultPresetEditorZMTC

    public ContextBasicEditablePreset getContextBasicEditablePresetDowngrade();

    // PRESET
    public Map offsetLinkIndexes(Integer offset, boolean user) throws PresetEmptyException, NoSuchPresetException;

    public Map offsetSampleIndexes(Integer offset, boolean user) throws NoSuchPresetException, PresetEmptyException;

    public Map remapLinkIndexes(Map translationMap) throws PresetEmptyException, NoSuchPresetException;

    public Map remapSampleIndexes(Map translationMap) throws PresetEmptyException, NoSuchPresetException;

    public void newPreset(Integer preset, String name) throws NoSuchPresetException;

    public void newPreset(Integer preset, String name, IsolatedPreset ip) throws NoSuchPresetException;

    public void setPresetParams(Integer[] ids, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, ParameterValueOutOfRangeException;

    public void combineVoices(Integer group) throws NoSuchPresetException, PresetEmptyException;

    public void purgeZones() throws PresetEmptyException, NoSuchPresetException;

    public void applySampleToPreset(Integer sample,int mode) throws NoSuchPresetException, PresetEmptyException, ParameterValueOutOfRangeException, TooManyVoicesException;

    public void sortVoices(Integer[] ids) throws PresetEmptyException, NoSuchPresetException;

    public void sortLinks(Integer[] ids) throws PresetEmptyException, NoSuchPresetException;

    public void sortZones(Integer[] ids) throws PresetEmptyException, NoSuchPresetException;
    // VOICE

    public Integer newVoice(IsolatedPreset.IsolatedVoice iv) throws NoSuchPresetException, TooManyZonesException, TooManyVoicesException, PresetEmptyException;

    public Integer newVoices(Integer num, Integer[] sampleNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException;

    public void rmvVoices(Integer[] voices) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, CannotRemoveLastVoiceException;

    public void copyVoice(Integer srcVoice, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyVoicesException;

    public void setVoicesParam(Integer[] voices, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, ParameterValueOutOfRangeException;

    public void setGroupParamFromVoice(Integer voice, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException, ParameterValueOutOfRangeException;

    public void setGroupParam(Integer group, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException, NoSuchContextException, ParameterValueOutOfRangeException;

    public void expandVoice(Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyVoicesException;

    public void getVoiceMultiSample(Integer srcVoice, Integer destVoice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException;

    public EditableParameterModel[] getAllEditableParameterModels();

    public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException;

    // LINK
    public Integer newLink(IsolatedPreset.IsolatedLink il) throws NoSuchPresetException, TooManyVoicesException, NoSuchContextException, PresetEmptyException;

    public Integer newLinks(Integer num, Integer[] presetNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException;

    public void rmvLinks(Integer[] links) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException;

    public void copyLink(Integer srcLink) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException;

    public void setLinksParam(Integer[] links, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, ParameterValueOutOfRangeException;

    // ZONE
    public Integer newZone(Integer voice, IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws NoSuchPresetException, TooManyZonesException, PresetEmptyException, NoSuchVoiceException;

    public Integer newZones(Integer voice, Integer num) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyZonesException;

    public void rmvZones(Integer voice, Integer[] zones) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchZoneException;

    public void setZonesParam(Integer voice, Integer[] zones, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, ParameterValueOutOfRangeException;

    public EditableVoice getEditableVoice(Integer voice);

    public EditableLink getEditableLink(Integer link);

    public void setDiversePresetParams(EditableParameterModel[] models, EditableParameterModel.EditChainValueProvider ecvp) throws NoSuchContextException, NoSuchPresetException, ParameterValueOutOfRangeException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchLinkException, NoSuchZoneException, ParameterUnavailableException, NoSuchGroupException;

    // SUB INTERFACES
    public interface EditableVoice extends ReadablePreset.ReadableVoice, ZCommandProvider, EditableParameterModelProvider {
        public static final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4EditableVoiceZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.EditVoiceZMTC;com.pcmsolutions.device.EMU.E4.zcommands.EditVoiceAsGroupZMTC;com.pcmsolutions.device.EMU.E4.zcommands.CopyVoiceZMTC;com.pcmsolutions.device.EMU.E4.zcommands.DeleteVoiceZMTC;com.pcmsolutions.device.EMU.E4.zcommands.NewVoiceZoneZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ExpandCombineVoiceZMTC;com.pcmsolutions.device.EMU.E4.zcommands.SplitVoiceZMTC;com.pcmsolutions.device.EMU.E4.zcommands.MatchVoiceKeyToSampleNameZMTC;com.pcmsolutions.device.EMU.E4.zcommands.AutoMapVoiceGroupZMTC;com.pcmsolutions.device.EMU.E4.zcommands.AutoMapVoicesZMTC");

        public EditableVoice duplicate();

        public void setGroupMode(boolean groupMode);

        public boolean getGroupMode();

        public boolean trySetOriginalKeyFromSampleName() throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException;

        public void setVoicesParam(Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, ParameterValueOutOfRangeException;

        public int numZones() throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException;

        public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException;

        public void removeVoice() throws NoSuchPresetException, NoSuchVoiceException, PresetEmptyException, CannotRemoveLastVoiceException;

        public void splitVoice(int splitKey) throws NoSuchContextException, NoSuchPresetException, PresetEmptyException, TooManyVoicesException, ParameterValueOutOfRangeException, NoSuchVoiceException;

        public void expandVoice() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException, TooManyVoicesException;

        public void combineVoiceGroup() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException, TooManyVoicesException;

        public void copyVoice() throws NoSuchPresetException, NoSuchVoiceException, PresetEmptyException, CannotRemoveLastVoiceException, TooManyVoicesException;

        public void newZones(Integer num) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyZonesException;

        public PresetContext getPresetContext();

        public EditableZone getEditableZone(Integer zone);

        public interface EditableZone extends ReadableZone, ZCommandProvider {
            public static final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4EditableZoneZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.RemoveZoneZMTC;com.pcmsolutions.device.EMU.E4.zcommands.MatchZoneKeyToSampleNameZMTC;com.pcmsolutions.device.EMU.E4.zcommands.AutoMapZonesZMTC");

            public void setZonesParam(Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, ParameterValueOutOfRangeException;

            public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException;

            public boolean trySetOriginalKeyFromSampleName() throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchZoneException;

            public void removeZone() throws NoSuchPresetException, NoSuchVoiceException, PresetEmptyException, NoSuchZoneException;

            public PresetContext getPresetContext();
        }
    }

    public interface EditableLink extends ReadablePreset.ReadableLink, ZCommandProvider, EditableParameterModelProvider {
        public static final ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4EditableLinkZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.RemoveLinkZMTC;com.pcmsolutions.device.EMU.E4.zcommands.CopyLinkZMTC;");

        public void setLinksParam(Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, ParameterValueOutOfRangeException;

        public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException;

        public void removeLink() throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException;

        public void copyLink() throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException;

        public PresetContext getPresetContext();
    }
}
