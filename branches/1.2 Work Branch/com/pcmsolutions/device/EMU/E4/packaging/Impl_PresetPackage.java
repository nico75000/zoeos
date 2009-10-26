package com.pcmsolutions.device.EMU.E4.packaging;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.util.ClassUtility;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 20-Sep-2003
 * Time: 23:33:59
 * To change this template use Options | File Templates.
 */
class Impl_PresetPackage implements PresetPackage, Serializable {

    static final long serialVersionUID = 1;

    private IsolatedPreset[] presets;
    private Integer[] masterIds;       // possibly null
    private Integer[] masterVals;       // possibly null
    private MultiModeMap multiModeMap;      // possibly null
    private Header header;
    private Map customObjectMap = new HashMap();

    private transient Impl_SamplePackage samplePackage;

    public Impl_PresetPackage(IsolatedPreset[] presets) {
        this.presets = (IsolatedPreset[]) presets.clone();
    }

    public SamplePackage getSamplePackage() {
        return samplePackage;
    }

    public void setPresets(IsolatedPreset[] presets) {
        this.presets = presets;
    }

    public void setMasterIds(Integer[] masterIds) {
        this.masterIds = masterIds;
    }

    public void setMasterVals(Integer[] masterVals) {
        this.masterVals = masterVals;
    }

    public void setMultiModeMap(MultiModeMap multiModeMap) {
        this.multiModeMap = multiModeMap;
    }

    public void setCustomObjectMap(Map customObjectMap) {
        if (customObjectMap != null &&
                !(ClassUtility.instanceCount(customObjectMap.entrySet().toArray(), Serializable.class) == customObjectMap.size())
                || !(ClassUtility.instanceCount(customObjectMap.keySet().toArray(), Serializable.class) == customObjectMap.size())
        )
            throw new IllegalArgumentException("not all custom objects are serializable");
        this.customObjectMap = new HashMap(customObjectMap);
    }

    public void setSamplePackage(Impl_SamplePackage samplePackage) {
        this.samplePackage = samplePackage;
    }

    protected void validatePresetReferences() throws PresetReferenceException {
        IntegerUseMap pm = new IntegerUseMap();
        Set presetIndexes = new HashSet();

        for (int p = 0; p < presets.length; p++) {
            pm.mergeUseMap(presets[p].referencedPresetUsage());
            presetIndexes.add(presets[p].getOriginalIndex());
        }

        if (!presetIndexes.containsAll(pm.getUsedIntegerSet()))
            throw new PresetReferenceException("");
    }

    public Map getCustomObjectMap() {
        return new HashMap(customObjectMap);
    }

    public MultiModeMap getMultiModeMap()    // possibly null
    {
        if (multiModeMap != null)
            return multiModeMap.getCopy();
        else
            return multiModeMap;
    }

    public Integer[] getMasterVals() {
        if (masterVals != null)
            return (Integer[]) masterVals.clone();
        else
            return null;
    }

    public Integer[] getMasterIds()       // possibly null
    {
        if (masterIds != null)
            return (Integer[]) masterIds.clone();
        else
            return null;
    }

    public PresetPackage.Header getHeader() {
        return header;
    }

    public IntegerUseMap getSampleUsage() {
        if (presets == null) {
            return new IntegerUseMap();
        } else
            return PresetContextMacros.getSampleUsage(presets);
    }

    public void setHeader(PresetPackage.Header header) {
        this.header = header;
    }

    public IsolatedPreset[] getPresets() {
        if (presets != null)
            return (IsolatedPreset[]) presets.clone();
        else
            return null;
    }

    public class PresetReferenceException extends Exception {
        public PresetReferenceException(String message) {
            super(message);
        }
    }
}
