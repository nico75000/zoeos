package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.gui.preset.DesktopEditingMediator;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.NoteUtilities;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.util.IntegerUseMap;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 13:58:44
 */
class Impl_PresetContext implements PresetContext, RemoteObjectStates, Serializable {
    protected transient Vector listeners;
    protected PresetDatabaseProxy presetDatabaseProxy;
    protected DeviceContext device;

    protected String name;

    public Impl_PresetContext(DeviceContext device, String name, PresetDatabaseProxy pdbp) {
        this.device = device;
        this.name = name;
        this.presetDatabaseProxy = pdbp;
        makeTransients();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        makeTransients();
    }

    private void makeTransients(){
        listeners = new Vector();
    }
    public boolean equals(Object o) {
        // identity comparison
        if (o == getDelegatingPresetContext())
            return true;
        return false;
    }

    public String toString() {
        return "Presets";
    }

    public void addPresetContextListener(PresetContextListener pl) {
        listeners.add(pl);
    }

    public void removePresetContextListener(PresetContextListener pl) {
        listeners.remove(pl);

    }

    public void addPresetListener(PresetListener pl, Integer[] presets) {
        presetDatabaseProxy.addPresetListener(pl, presets);
    }

    public void removePresetListener(PresetListener pl, Integer[] presets) {
        presetDatabaseProxy.removePresetListener(pl, presets);
    }

    private void firePresetsAddedToContext(final Integer[] presets) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
                        try {
                            ((PresetContextListener) e.nextElement()).presetsAddedToContext(getDelegatingPresetContext(), presets);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void firePresetsRemovedFromContext(final Integer[] presets) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
                        try {
                            ((PresetContextListener) e.nextElement()).presetsRemovedFromContext(getDelegatingPresetContext(), presets);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void fireContextReleased() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
                        ((PresetContextListener) e.nextElement()).contextReleased(getDelegatingPresetContext());
                    }
                }
            }
        });
    }

    public boolean isPresetInitialized(Integer preset) throws NoSuchPresetException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.isPresetInitialized(preset);
        } finally {
            reader.release();
        }
    }

    public void assertPresetInitialized(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        if (!isPresetInitialized(preset))
            refreshPreset(preset);
    }

    public void assertPresetRemote(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        try {
            newPreset(this.getIsolatedPreset(preset), preset, getPresetName(preset));
        } catch (PresetEmptyException e) {
            erasePreset(preset);
        }
    }

    public void assertPresetNamed(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            reader.assertPresetNamed(getDelegatingPresetContext(), preset);
        } finally {
            reader.release();
        }
    }

    public int getPresetState(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.getPresetState(getDelegatingPresetContext(), preset);
        } finally {
            reader.release();
        }
    }

    public String getPresetSummary(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.tryGetPresetSummary(getDelegatingPresetContext(), preset);
        } finally {
            reader.release();
        }
    }

    public boolean isPresetWriteLocked(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.isPresetWriteLocked(getDelegatingPresetContext(), preset);
        } finally {
            reader.release();
        }
    }

    public boolean isPresetWritable(Integer preset) {
        return isPresetInContext(preset);
    }

    public Set getPresetSet(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            Set s = taskGetPresetSet(preset, new HashSet());
            return s;
        } finally {
            reader.release();
        }
    }

    private Set taskGetPresetSet(Integer preset, Set handledPresets) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Set s;
        handledPresets.add(preset);
        try {
            PresetObject pobj = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                s = pobj.referencedPresetSet();
            } finally {
                reader.unlockPreset(preset);
            }
            s.removeAll(handledPresets);
            handledPresets.addAll(s);

            for (Iterator i = s.iterator(); i.hasNext();)
                taskGetPresetSet((Integer) i.next(), handledPresets);

        } catch (PresetEmptyException e) {
        } finally {
            reader.release();
        }
        return handledPresets;
    }

    public void release() throws NoSuchContextException {
        PDBWriter writer = presetDatabaseProxy.getDBWrite();
        try {
            writer.releaseContext(getDelegatingPresetContext());
        } finally {
            writer.release();
        }
        fireContextReleased();
    }

    public IntegerUseMap getSampleIndexesInUseForUserPresets() throws NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.getSampleIndexesInUseForUserPresets(getDelegatingPresetContext());
        } finally {
            reader.release();
        }
    }

    public IntegerUseMap getSampleIndexesInUseForFlashPresets() throws NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.getSampleIndexesInUseForFlashPresets(getDelegatingPresetContext());
        } finally {
            reader.release();
        }
    }

    public IntegerUseMap getSampleIndexesInUseForAllPresets() throws NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.getSampleIndexesInUseForAllPresets(getDelegatingPresetContext());
        } finally {
            reader.release();
        }
    }

    // PRESET
    // value between 0 and 1 representing fraction of dump completed
    // value < 0 means no dump in progress
    public double getInitializationStatus(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.getInitializationStatus(getDelegatingPresetContext(), preset);
        } finally {
            reader.release();
        }
    }

    public void newPreset(Integer preset, String name) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            reader.lockPresetWrite(getDelegatingPresetContext(), preset);
            try {
                PresetObject np;
                if (name != null)
                    np = new PresetObject(preset, name, presetDatabaseProxy.getPresetEventHandler(), device.getDeviceParameterContext());
                else
                    np = new PresetObject(preset, DeviceContext.UNTITLED_PRESET, presetDatabaseProxy.getPresetEventHandler(), device.getDeviceParameterContext());

                reader.changePresetObject(getDelegatingPresetContext(), preset, np);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void newPreset(IsolatedPreset ip, Integer preset, String name) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            reader.lockPresetWrite(getDelegatingPresetContext(), preset);
            try {
                PresetObject np;
                if (name != null)
                    np = new PresetObject(preset, name, ip, presetDatabaseProxy.getPresetEventHandler(), device.getDeviceParameterContext());
                else
                    np = new PresetObject(preset, DeviceContext.UNTITLED_PRESET, ip, presetDatabaseProxy.getPresetEventHandler(), device.getDeviceParameterContext());

                reader.changePresetObject(getDelegatingPresetContext(), preset, np);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void newPreset(IsolatedPreset ip, Integer preset, String name, Map sampleTranslationMap, Integer defaultSampleTranslation, Map linkTranslationMap) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            reader.lockPresetWrite(getDelegatingPresetContext(), preset);
            try {
                newPreset(ip, preset, name);
                if (linkTranslationMap != null)
                    remapLinkIndexes(preset, linkTranslationMap);
                if (sampleTranslationMap != null)
                    remapSampleIndexes(preset, sampleTranslationMap, defaultSampleTranslation);
            } catch (PresetEmptyException e) {
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Map offsetLinkIndexes(Integer preset, Integer offset, boolean user) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                return p.offsetLinkIndexes(offset, user);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Map offsetSampleIndexes(Integer preset, Integer offset, boolean user) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                return p.offsetSampleIndexes(offset, user);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Map remapLinkIndexes(Integer preset, Map translationMap) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                return p.remapLinkIndexes(translationMap);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Map remapSampleIndexes(Integer preset, Map translationMap, Integer defaultSampleTranslation) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                return p.remapSampleIndexes(translationMap, defaultSampleTranslation);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public IsolatedPreset getIsolatedPreset(Integer preset) throws NoSuchContextException, NoSuchPresetException, PresetEmptyException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject pobj = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                PresetObject np;
                np = new PresetObject(preset, pobj.getName(), pobj, presetDatabaseProxy.getPresetEventHandler(), device.getDeviceParameterContext());
                np.setDeviceParameterContext(null);
                np.setPresetEventHandler(null);
                return np;
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void applySampleToPreset(Integer preset, Integer sample, int mode) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, ParameterValueOutOfRangeException, TooManyVoicesException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                p.applySingleSample(sample, mode);
                if (mode == PresetContext.MODE_APPLY_SAMPLE_TO_NEW_VOICE && prefs.getBoolean(PREF_autoGroup, true)) {
                    p.getVoice(IntPool.get(p.numVoices() - 1)).setValue(IntPool.get(37), this.getNextAvailableGroup(preset, prefs.getBoolean(PREF_autoGroupAtTail, false)));
                }
                if (ZPREF_tryMatchAppliedSamples.getValue()) {
                    try {
                        this.trySetOriginalKeyFromSampleName(preset, IntPool.get(p.numVoices() - 1));
                    } catch (SampleEmptyException e) {
                    } catch (NoSuchSampleException e) {
                    }
                }
            } catch (NoSuchVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                device.logInternalError(e);
            } catch (IllegalParameterIdException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                device.logInternalError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void applySamplesToPreset(Integer preset, Integer[] samples, int mode) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, ParameterValueOutOfRangeException, TooManyVoicesException, TooManyZonesException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                p.applySamples(samples, mode);
                if (mode == PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICE_AND_ZONES) {
                    if (prefs.getBoolean(PREF_autoGroup, true))
                        p.getVoice(IntPool.get(p.numVoices() - 1)).setValue(IntPool.get(37), this.getNextAvailableGroup(preset, prefs.getBoolean(PREF_autoGroupAtTail, false)));
                    if (ZPREF_tryMatchAppliedSamples.getValue()) {
                        VoiceObject v = p.getVoice(IntPool.get(p.numVoices() - 1));
                        for (int i = 0,j = v.numZones(); i < j; i++)
                            try {
                                this.trySetOriginalKeyFromSampleName(preset, v.getVoice(), IntPool.get(i));
                            } catch (SampleEmptyException e) {
                            } catch (NoSuchSampleException e) {
                            }
                    }
                } else if (mode == PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICES) {
                    if (prefs.getBoolean(PREF_autoGroup, true)) {
                        Integer ng = this.getNextAvailableGroup(preset, prefs.getBoolean(PREF_autoGroupAtTail, false));
                        for (int i = 0; i < samples.length; i++) {
                            p.getVoice(IntPool.get(p.numVoices() - samples.length + i)).setValue(IntPool.get(37), ng);
                        }
                    }
                    if (ZPREF_tryMatchAppliedSamples.getValue()) {
                        try {
                            for (int i = 0; i < samples.length; i++) {
                                this.trySetOriginalKeyFromSampleName(preset, IntPool.get(p.numVoices() - samples.length + i));
                            }
                        } catch (SampleEmptyException e) {
                        } catch (NoSuchSampleException e) {
                        }
                    }
                }
            } catch (NoSuchVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                device.logInternalError(e);
            } catch (NoSuchZoneException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                device.logInternalError(e);
            } catch (IllegalParameterIdException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                device.logInternalError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void lockPresetWrite(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            reader.lockPresetWrite(getDelegatingPresetContext(), preset);
        } finally {
            reader.release();
        }
    }

    public void unlockPreset(Integer preset) {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            reader.unlockPreset(preset);
        } finally {
            reader.release();
        }
    }

    public PresetContext newContext(String name, Integer[] presets) throws NoSuchPresetException, NoSuchContextException {
        PDBWriter writer = presetDatabaseProxy.getDBWrite();
        PresetContext rv;
        try {
            rv = writer.newContext(getDelegatingPresetContext(), name, presets);
        } finally {
            writer.release();
        }
        firePresetsRemovedFromContext(presets);
        return rv;
    }

    public void expandContext(PresetContext dpc, Integer[] presets) throws NoSuchContextException, NoSuchPresetException {
        if (dpc == getDelegatingPresetContext())
            throw new NoSuchContextException("Can't expand self");
        if (!(dpc instanceof Impl_PresetContext))
            throw new NoSuchContextException("Destination context is not compatible");
        Impl_PresetContext real_dpc = (Impl_PresetContext) dpc;
        removeFromContext(presets);
        real_dpc.addToContext(presets);
    }

    private void addToContext(Integer[] presets) throws NoSuchContextException, NoSuchPresetException {
        PDBWriter writer = presetDatabaseProxy.getDBWrite();
        try {
            writer.addPresetsToContext(getDelegatingPresetContext(), presets);
        } finally {
            writer.release();
        }
        firePresetsAddedToContext(presets);
    }

    private void removeFromContext(Integer[] presets) throws NoSuchContextException, NoSuchPresetException {
        PDBWriter writer = presetDatabaseProxy.getDBWrite();
        try {
            writer.removePresetsFromContext(getDelegatingPresetContext(), presets);
        } finally {
            writer.release();
        }
        firePresetsRemovedFromContext(presets);
    }

    public List expandContextWithEmptyPresets(PresetContext dpc, Integer reqd) throws NoSuchContextException {
        List removed;
        if (dpc == getDelegatingPresetContext())
            throw new NoSuchContextException();
        PDBWriter writer = presetDatabaseProxy.getDBWrite();
        try {
            removed = writer.expandContextWithEmptyPresets(getDelegatingPresetContext(), dpc, reqd);
        } finally {
            writer.release();
        }
        for (int n = 0; n < removed.size(); n++)
            firePresetsRemovedFromContext((Integer[]) removed.toArray());

        return removed;
    }

    public int numEmpties(Integer[] presets) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            int num = 0;
            for (int i = 0; i < presets.length; i++)
                if (reader.getPresetNameExtended(getDelegatingPresetContext(), presets[i]).equals(DeviceContext.EMPTY_PRESET))
                    num++;
            return num;
        } finally {
            reader.release();
        }
    }

    public int numEmpties(Integer lowPreset, int num) throws NoSuchPresetException, NoSuchContextException {
        Integer[] presets = new Integer[num];

        ZUtilities.fillIncrementally(presets, lowPreset.intValue());

        return numEmpties(presets);
    }

    public List findEmptyPresetsInContext(Integer reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.findEmptyPresets(getDelegatingPresetContext(), reqd, beginIndex, maxIndex);
        } finally {
            reader.release();
        }
    }

    public Integer firstEmptyPresetInContext() throws NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            Set s = reader.getPresetIndexesInContext(getDelegatingPresetContext());
            for (Iterator i = s.iterator(); i.hasNext();) {
                Integer index = (Integer) i.next();
                try {
                    if (isPresetEmpty(index))
                        return index;
                } catch (NoSuchPresetException e) {
                }
            }
        } finally {
            reader.release();
        }
        throw new NoSuchPresetException(IntPool.get(Integer.MIN_VALUE));
    }

    // high is exclusive
    public Integer firstEmptyPresetInDatabaseRange(Integer lowPreset, Integer highPreset) throws NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            Set s = reader.getReadablePresetIndexes(getDelegatingPresetContext());
            int hi = highPreset.intValue();
            int li = lowPreset.intValue();
            if (hi < li)
                throw new IllegalArgumentException("lowPreset is higher than highPreset");
            for (int i = li; i < hi; i++) {
                Integer index = IntPool.get(i);
                if (s.contains(index))
                    try {
                        if (isPresetEmpty(index))
                            return index;
                    } catch (NoSuchPresetException e) {
                    }
            }
        } finally {
            reader.release();
        }
        throw new NoSuchPresetException(IntPool.get(Integer.MIN_VALUE));
    }

    public Set getPresetIndexesInContext() throws NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.getPresetIndexesInContext(getDelegatingPresetContext());
        } finally {
            reader.release();
        }
    }

    private Object getMostCapablePresetObject(Integer preset) throws NoSuchPresetException {
        PresetModel impl = getPresetImplementation(preset);
        try {
            impl = PresetClassManager.getMostDerivedPresetInstance(impl, getPresetName(preset));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (PresetEmptyException e) {
        }
        //if (mainView != null)
        //    impl.setPresetEditingMediator(((DeviceInternalFrame) mainView).getDesktopEditingMediator());
        return impl;
    }

    // returns List of ContextReadablePreset/ReadablePreset ( e.g FLASH/ROM samples returned as ReadablePreset)
    public List getContextPresets() throws NoSuchContextException {
        ArrayList outList = new ArrayList();
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            Set indexes = getPresetIndexesInContext();
            Integer p;
            for (Iterator i = indexes.iterator(); i.hasNext();)
                try {
                    p = (Integer) i.next();
                    outList.add(getMostCapablePresetObject(p));
                } catch (NoSuchPresetException e) {
                }
            return outList;
        } finally {
            reader.release();
        }
    }

    // returns List of ReadablePreset or better
    // e.g FLASH/ROM and out of context samples returned as ReadablePreset
    // possibly more derived than ReadablePreset if preset is in context etc... )
    public List getDatabasePresets() throws NoSuchContextException {
        ArrayList outList = new ArrayList();
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            Set indexes = reader.getReadablePresetIndexes(getDelegatingPresetContext());
            for (Iterator i = indexes.iterator(); i.hasNext();)
                try {
                    outList.add(getMostCapablePresetObject((Integer) i.next()));
                } catch (NoSuchPresetException e) {
                }
            return outList;
        } finally {
            reader.release();
        }
    }

    public Set getDatabaseIndexes() throws NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.getReadablePresetIndexes(getDelegatingPresetContext());
        } finally {
            reader.release();
        }
    }

    public Map getPresetNamesInContext() throws NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.getPresetNamesInContext(getDelegatingPresetContext());
        } finally {
            reader.release();
        }
    }

    public boolean isPresetInContext(Integer preset) {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            return reader.hasPreset(getDelegatingPresetContext(), preset);
        } finally {
            reader.release();
        }
    }

    public int size() {
        try {
            return getPresetIndexesInContext().size();
        } catch (NoSuchContextException e) {
            return 0;
        }
    }

    public boolean isPresetEmpty(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        return getPresetState(preset) == RemoteObjectStates.STATE_EMPTY;
    }

    private Impl_ReadablePreset getPresetImplementation(Integer preset) throws NoSuchPresetException {
        DesktopEditingMediator dem = null;
        //  if (mainView != null)
        //    dem = ((DeviceInternalFrame) mainView).getDesktopEditingMediator();

        if (isPresetInContext(preset))
            if (preset.intValue() <= DeviceContext.MAX_USER_PRESET)
                return new Impl_ContextEditablePreset(getDelegatingPresetContext(), preset, dem);
            else
                return new Impl_ContextBasicEditablePreset(getDelegatingPresetContext(), preset, dem);
        else {
            PDBReader reader = presetDatabaseProxy.getDBRead();
            try {
                if (reader.readsPreset(getDelegatingPresetContext(), preset))
                    return new Impl_ReadablePreset(getDelegatingPresetContext(), preset, dem);
                else
                    throw new NoSuchPresetException(preset);
            } finally {
                reader.release();
            }
        }

        // return getMostCapablePresetObject(preset);
    }

    /*private Impl_ContextReadablePreset getContextPresetImplementation(Integer preset) throws NoSuchPresetException {
        if (isPresetInContext(preset))
            return new Impl_ContextReadablePreset(getDelegatingPresetContext(), preset);

        throw new NoSuchPresetException(preset);
    }

    private Impl_ReadablePreset getReadablePresetImplementation(Integer preset) throws NoSuchPresetException {
        PDBReader reader = sdbp.getDBRead();
        try {
            if (reader.readsPreset(getDelegatingPresetContext(), preset))
                return new Impl_ReadablePreset(getDelegatingPresetContext(), preset);
            else
                throw new NoSuchPresetException(preset);
        } finally {
            reader.release();
        }
    } */

    public ContextReadablePreset getContextPreset(Integer preset) throws NoSuchPresetException {
        Object rv = getPresetImplementation(preset);
        if (rv instanceof ContextReadablePreset)
            return (ContextReadablePreset) rv;
        else
            throw new NoSuchPresetException(preset);
    }

    public ReadablePreset getReadablePreset(Integer preset) throws NoSuchPresetException {
        return getPresetImplementation(preset);
    }

    // TODO!! fix semantics of this to handle FLASH samples that cannot be returned as ContextEditablePreset
    public ContextEditablePreset getEditablePreset(Integer preset) throws NoSuchPresetException {
        if (isPresetInContext(preset))
            return new Impl_ContextEditablePreset(getDelegatingPresetContext(), preset);

        throw new NoSuchPresetException(preset);
    }

    public void sortLinks(Integer preset, Integer[] ids) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                p.sortLinks(ids);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void sortZones(Integer preset, Integer[] ids) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                p.sortZones(ids);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    // VOICE
    public void splitVoice(Integer preset, Integer voice, int splitKey) throws NoSuchContextException, NoSuchPresetException, PresetEmptyException, TooManyVoicesException, ParameterValueOutOfRangeException, NoSuchVoiceException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                p.splitVoice(voice, splitKey);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void sortVoices(Integer preset, Integer[] ids) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                p.sortVoices(ids);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer preset, Integer voice) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, NoSuchVoiceException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.getIsolatedVoice(voice);

            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Integer newVoice(Integer preset, IsolatedPreset.IsolatedVoice iv) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, TooManyVoicesException, TooManyZonesException {
        return null;
    }

    private final Integer[] z_mergeIds = new Integer[]{IntPool.get(39), IntPool.get(40), IntPool.get(42)};
    private final Integer[] z_rtIds = new Integer[]{IntPool.get(53), IntPool.get(54), IntPool.get(55), IntPool.get(56)};

    public void combineVoices(Integer preset, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                VoiceObject[] vig = p.getVoicesInGroup(group);
                if (vig.length < 2)
                    return;
                //int fv_nz = vig[0].numZones();

                IsolatedPreset.IsolatedVoice.IsolatedZone iz;
                int nz; // num zones
                Integer nzi; // new zone index
                ParameterContext zpc = device.getDeviceParameterContext().getZoneContext();
                Set z_ids = zpc.getIds();
                Integer[] zoneIds = (Integer[]) z_ids.toArray(new Integer[z_ids.size()]);
                Integer[] vals;
                Integer[] vals2 = null;

                boolean postProcessingReqd = vig[0].numZones() == 0;

                if (postProcessingReqd)
                    vals2 = vig[0].getValues(zoneIds);

                for (int i = 1; i < vig.length; i++) {
                    nz = vig[i].numZones();
                    if (nz == 0) {
                        vals = vig[i].getValues(zoneIds);
                        nzi = vig[0].addZones(IntPool.get(1));
                        vig[0].getZone(nzi).setValues(zoneIds, vals);
                    } else
                        for (int j = 0; j < nz; j++) {
                            iz = vig[i].getIsolatedZone(IntPool.get(j));
                            vig[0].getZone(vig[0].addZone(iz)).offsetValues(z_mergeIds, vig[i].getValues(z_mergeIds));
                        }
                }

                if (postProcessingReqd) {
                    vig[0].defaultValues(zoneIds);
                    // realtime stuff needs to be defaulted as well
                    vig[0].defaultValues(z_rtIds);
                    vig[0].getZone(IntPool.get(0)).setValues(zoneIds, vals2);
                }
                for (int i = vig.length - 1; i > 0; i--)
                    p.rmvVoice(vig[i].getVoice());

            } catch (IllegalParameterIdException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } catch (NoSuchVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } catch (CannotRemoveLastVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } catch (NoSuchZoneException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } catch (TooManyZonesException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } catch (ParameterValueOutOfRangeException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Set getUsedGroupIndexes(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                HashSet usedGroups = new HashSet();
                for (int i = 0, j = p.numVoices(); i < j; i++)
                    usedGroups.add(p.getVoice(IntPool.get(i)).getValue(IntPool.get(37)));
                //return (Integer[]) usedGroups.toArray(new Integer[usedGroups.size()]);
                return usedGroups;
            } catch (NoSuchVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                device.logInternalError(e);
            } catch (IllegalParameterIdException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                device.logInternalError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        //return new Integer[0];
        return new HashSet();
    }

    public Integer getNextAvailableGroup(Integer preset, boolean atTail) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        Set usedGroups = getUsedGroupIndexes(preset);
        if (atTail) {
            Integer max = (Integer) Collections.max(usedGroups);
            if (max.intValue() == 32)
                return IntPool.get(32);
            else
                return IntPool.get(max.intValue() + 1);
        } else {
            for (int i = 1; i <= 32; i++)
                if (!usedGroups.contains(IntPool.get(i)))
                    return IntPool.get(i);
            return IntPool.get(32);
        }
    }

    public void purgeZones(Integer preset) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int i = 0, j = p.numVoices(); i < j; i++) {
                    VoiceObject v = p.getVoice(IntPool.get(i));
                    for (int x = v.numZones() - 1; x > 0; x--)
                        v.rmvZone(IntPool.get(x));
                }
            } catch (NoSuchVoiceException e) {
            } catch (NoSuchZoneException e) {
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void copyLink(Integer srcPreset, Integer srcLink, Integer destPreset) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject[] pobjs = reader.getPresetRW(getDelegatingPresetContext(), srcPreset, destPreset);

            try {
                LinkObject l = pobjs[0].getLink(srcLink);
                pobjs[1].addLinks(new LinkObject[]{l});
            } finally {
                reader.unlockPreset(srcPreset);
                reader.unlockPreset(destPreset);
            }
        } finally {
            reader.release();
        }
    }

    public void copyPreset(Integer srcPreset, Integer destPreset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        copyPreset(srcPreset, destPreset, (String) null);
    }

    public void copyPreset(Integer srcPreset, Integer destPreset, Map presetLinkTranslationMap) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            copyPreset(srcPreset, destPreset, (String) null);
            remapLinkIndexes(destPreset, presetLinkTranslationMap);
        } finally {
            reader.release();
        }
    }

    public void copyPreset(Integer srcPreset, Integer destPreset, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        if (srcPreset.intValue() == destPreset.intValue())
            return;
        if (isPresetEmpty(srcPreset))
            throw new PresetEmptyException(srcPreset);

        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            Object[] pobjs = reader.getPresetRC(getDelegatingPresetContext(), srcPreset, destPreset);
            try {
                PresetObject np = new PresetObject((PresetObject) pobjs[0], destPreset, presetDatabaseProxy.getPresetEventHandler(), device.getDeviceParameterContext());
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, np);
                if (name != null)
                    np.setName(name);
            } catch (NoSuchPresetException e) {
                // error!
                throw new IllegalStateException(this.getClass().toString() + ":copyPreset->Cannot find destination preset after locking it!");
            } finally {
                reader.unlockPreset(srcPreset);
                reader.unlockPreset(destPreset);
            }
        } finally {
            reader.release();
        }
    }

    public void copyVoice(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyVoicesException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject[] pobjs = reader.getPresetRW(getDelegatingPresetContext(), srcPreset, destPreset);

            try {
                VoiceObject v = pobjs[0].getVoice(srcVoice);
                v = pobjs[1].getVoice(pobjs[1].addVoices(new VoiceObject[]{v}));
                try {
                    v.setValues(new Integer[]{IntPool.get(37)}, new Integer[]{group});  // group number
                } catch (IllegalParameterIdException e) {
                    throw new IllegalStateException(this.getClass().toString() + ":removeVoice->setting group number failed!");
                } catch (ParameterValueOutOfRangeException e) {
                    throw new IllegalStateException(this.getClass().toString() + ":removeVoice->setting group number value failed!");
                }
            } finally {
                reader.unlockPreset(srcPreset);
                reader.unlockPreset(destPreset);
            }
        } finally {
            reader.release();
        }
    }

    public void erasePreset(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            reader.changePresetObject(getDelegatingPresetContext(), preset, EmptyPreset.getInstance());
        } finally {
            reader.release();
        }
    }

    private final Integer[] exceptIds = new Integer[]{IntPool.get(39), IntPool.get(40), IntPool.get(42)};

    public void expandVoice(Integer preset, Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException, TooManyVoicesException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                VoiceObject v = p.getVoice(voice);
                VoiceObject nv;
                Integer addIndex;
                IsolatedPreset.IsolatedVoice.IsolatedZone[] zones = new IsolatedPreset.IsolatedVoice.IsolatedZone[v.numZones()];
                Integer[] voiceVals = v.getAllValues();
                Integer[] voiceIds = v.getAllIds();

                if (zones.length > 0) {

                    int nz = v.numZones();

                    for (int i = 0, j = nz; i < j; i++)
                        zones[i] = v.getIsolatedZone(IntPool.get(i));

                    for (int i = nz - 1; i > 0; i--)
                        v.rmvZone(IntPool.get(i));

                    for (int i = 0, j = zones.length; i < j; i++) {
                        addIndex = p.addVoices(IntPool.get(1), new Integer[]{IntPool.get(0)});
                        nv = p.getVoice(addIndex);
                        nv.setValues(voiceIds, voiceVals);
                        nv.setValues(zones[i].getAllIdsExcept(exceptIds), zones[i].getAllValuesExcept(exceptIds));
                        nv.offsetValues(exceptIds, zones[i].getValues(exceptIds));  // addDesktopElement zone vol, pan and ftune to corresponding voice values
                    }

                    p.rmvVoice(voice);
                }
            } catch (NoSuchZoneException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } catch (IllegalParameterIdException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } catch (ParameterValueOutOfRangeException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } catch (CannotRemoveLastVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public boolean trySetOriginalKeyFromName(Integer preset, Integer voice, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException {
        NoteUtilities.Note n = NoteUtilities.getNoteFromName(name);
        if (n != null) {
            try {
                setVoicesParam(preset, new Integer[]{voice}, IntPool.get(44), new Integer[]{IntPool.get(n.getNoteValue())});
                return true;
            } catch (IllegalParameterIdException e) {
            } catch (ParameterValueOutOfRangeException e) {
            }
        }
        return false;
    }

    public boolean trySetOriginalKeyFromName(Integer preset, Integer voice, Integer zone, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException, NoSuchZoneException {
        NoteUtilities.Note n = NoteUtilities.getNoteFromName(name);
        if (n != null) {
            try {
                setZonesParam(preset, voice, new Integer[]{zone}, IntPool.get(44), new Integer[]{IntPool.get(n.getNoteValue())});
                return true;
            } catch (IllegalParameterIdException e) {
            } catch (ParameterValueOutOfRangeException e) {
            }
        }
        return false;
    }

    public boolean trySetOriginalKeyFromSampleName(Integer preset, Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException, SampleEmptyException, NoSuchSampleException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            String name = this.getRootSampleContext().getSampleName(this.getVoiceParams(preset, voice, new Integer[]{ID.sample})[0]);
            return trySetOriginalKeyFromName(preset, voice, name);
        } catch (IllegalParameterIdException e) {
            reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            device.logInternalError(e);
        } finally {
            reader.release();
        }
        return false;
    }

    public boolean trySetOriginalKeyFromSampleName(Integer preset, Integer voice, Integer zone) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException, NoSuchZoneException, SampleEmptyException, NoSuchSampleException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            String name = this.getRootSampleContext().getSampleName(this.getZoneParams(preset, voice, zone, new Integer[]{ID.sample})[0]);
            return trySetOriginalKeyFromName(preset, voice, zone, name);
        } catch (IllegalParameterIdException e) {
            reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            device.logInternalError(e);
        } finally {
            reader.release();
        }
        return false;
    }

    public Integer[] getLinkParams(Integer preset, Integer link, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Integer[] vals;
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                vals = p.getLink(link).getValues(ids);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }

        return vals;
    }

    public String getPresetName(Integer preset) throws NoSuchPresetException, PresetEmptyException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            String name = reader.getPresetName(preset);
            if (name == DeviceContext.EMPTY_PRESET)
                throw new PresetEmptyException(preset);
            else
                return name;
        } finally {
            reader.release();
        }
    }

    public Integer[] getPresetParams(Integer preset, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Integer[] vals;
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                vals = p.getValues(ids);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return vals;
    }

    public void getVoiceMultiSample(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer destVoice) {
    }

    public Integer[] getVoiceIndexesInGroupFromVoice(Integer preset, Integer voice) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, NoSuchVoiceException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                VoiceObject v = p.getVoice(voice);
                Integer group = v.getValues(new Integer[]{IntPool.get(37)})[0];
                return getVoiceIndexesInGroup(preset, group);
            } catch (NoSuchGroupException e) {
                throw new IllegalStateException("Group missing!");
            } catch (IllegalParameterIdException e) {
                throw new IllegalStateException("Group missing!");

            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Integer[] getVoiceIndexesInGroup(Integer preset, Integer group) throws NoSuchGroupException, PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                Integer[] vi = p.getVoiceIndexesInGroup(group);
                if (vi.length == 0)
                    throw new NoSuchGroupException();
                return vi;
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    // LINK
    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer preset, Integer link) throws NoSuchLinkException, NoSuchPresetException, NoSuchContextException, PresetEmptyException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.getIsolatedLink(link);

            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    // LINK
    public Integer newLink(Integer preset, IsolatedPreset.IsolatedLink il) throws TooManyVoicesException, PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        return null;
    }

    public Integer[] getGroupParams(Integer preset, Integer group, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                Integer lv = p.getLeadVoiceIndexInGroup(group);

                if (lv == null)
                    throw new NoSuchGroupException();
                return getVoiceParams(preset, lv, ids);
            } catch (NoSuchVoiceException e) {
                throw new NoSuchGroupException(); // should never get here!!!
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Integer[] getVoiceParams(Integer preset, Integer voice, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Integer[] vals;
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                vals = p.getVoice(voice).getValues(ids);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return vals;
    }

    public Integer[] setGroupParamFromVoice(Integer preset, Integer voice, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Integer group = p.getVoice(voice).getValues(new Integer[]{IntPool.get(37)})[0];
                return setGroupParam(preset, group, id, value);
            } catch (NoSuchGroupException e) {
                throw new NoSuchVoiceException();  // should never get here!!!
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Integer[] setGroupParam(Integer preset, Integer group, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Integer[] changed = new Integer[0];
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Integer[] vIndexes = p.getVoiceIndexesInGroup(group);
                if (vIndexes.length == 0)
                    throw new NoSuchGroupException();
                //Integer[] vals = new Integer[vIndexes.elementCount];
                //Arrays.fill(vals, value);
                changed = setGroupParamHelper(preset, vIndexes, group, id, value);
                //setVoicesParam(preset, vIndexes, id, vals);
            } catch (NoSuchVoiceException e) {
                throw new NoSuchGroupException(); // should never get here!!!
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return changed;
    }

    protected Integer[] setGroupParamHelper(Integer preset, Integer[] updateVoices, Integer group, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetDatabaseProxy.getDBRead();

        int len = updateVoices.length;
        //Integer[] changedValues;
        Integer[] changed = new Integer[0];
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int n = 0; n < len; n++) {
                    changed = setVoiceValue(p.getVoice(updateVoices[n]), id, value);
                    //VoiceObject v = p.getVoice(updateVoices[n]);
                    //v.setValues(changedValues, v.);
                }

            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return changed;
    }

    public Integer[] getZoneParams(Integer preset, Integer voice, Integer zone, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Integer[] vals;
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                vals = p.getVoice(voice).getZone(zone).getValues(ids);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return vals;
    }

    public Integer newLinks(Integer preset, Integer num, Integer[] presetNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Integer addedIndex;
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                addedIndex = p.addLinks(num, presetNums);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return addedIndex;
    }

    public Integer newVoices(Integer preset, Integer num, Integer[] sampleNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Integer addedIndex;
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                addedIndex = p.addVoices(num, sampleNums);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return addedIndex;
    }

    public Integer newZones(Integer preset, Integer voice, Integer num) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyZonesException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Integer addedIndex;
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                addedIndex = p.getVoice(voice).addZones(num);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return addedIndex;
    }

    public int numLinks(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.numLinks();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public int numPresetSamples(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.numReferencedSamples();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Set presetSampleSet(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.referencedSampleSet();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public IntegerUseMap presetSampleUsage(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.referencedSampleUsage();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public int numPresetLinkPresets(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.numReferencedPresets();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Set presetLinkPresetSet(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.referencedPresetSet();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public IntegerUseMap presetLinkPresetUsage(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.referencedPresetUsage();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public int numPresetZones(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.numZones();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public int numVoices(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.numVoices();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public int numZones(Integer preset, Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.getVoice(voice).numZones();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void refreshPreset(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            reader.refreshPreset(getDelegatingPresetContext(), preset);
        } finally {
            reader.release();
        }
    }

    public void refreshVoiceParameters(Integer preset, Integer voice, Integer[] ids) throws NoSuchContextException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException, ParameterValueOutOfRangeException, IllegalParameterIdException {

    }

    public void rmvLinks(Integer preset, Integer[] links) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {

            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int i = links.length - 1; i >= 0; i--)
                    p.rmvLink(links[i]);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void rmvVoices(Integer preset, Integer[] voices) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException, CannotRemoveLastVoiceException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int i = voices.length - 1; i >= 0; i--)
                    p.rmvVoice(voices[i]);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void rmvZones(Integer preset, Integer voice, Integer[] zones) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int i = zones.length - 1; i >= 0; i--)
                    p.getVoice(voice).rmvZone(zones[i]);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Integer[] setLinksParam(Integer preset, Integer[] links, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        int len = links.length;
        Integer[] changed = new Integer[0];
        try {
            if (links.length != values.length)
                throw new IllegalArgumentException(this.getClass().toString() + ":setLinksParam -> mismatch between number of links and number of parameter values!");

            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int n = 0; n < len; n++) {
                    changed = setLinkValue(p.getLink(links[n]), id, values[n]);
                }
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return changed;
    }

    // ZONE
    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer preset, Integer voice, Integer zone) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, NoSuchVoiceException, NoSuchZoneException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetRead(getDelegatingPresetContext(), preset);
            try {
                return p.getVoice(voice).getIsolatedZone(zone);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    // ZONE
    public Integer newZone(Integer preset, Integer voice, IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, NoSuchVoiceException, TooManyZonesException {
        return null;
    }

    public void setPresetName(Integer preset, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            reader.setPresetName(getDelegatingPresetContext(), preset, name);
        } finally {
            reader.release();
        }
    }

    public Integer[] setPresetParams(Integer preset, Integer[] ids, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                p.setValues(ids, values);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return ids;
    }

    public Integer[] setVoicesParam(Integer preset, Integer[] voices, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        Integer[] changed = new Integer[0];
        try {
            if (voices.length != values.length)
                throw new IllegalArgumentException(this.getClass().toString() + ":setVoicesParam -> mismatch between number of voices and number of parameter2 values!");

            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int n = 0; n < voices.length; n++) {
                    changed = setVoiceValue(p.getVoice(voices[n]), id, values[n]);
                    //p.getVoice(voices[n]).setValues(new Integer[]{id}, new Integer[]{values[n]});
                }
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return changed;
    }

    public Integer[] setZonesParam(Integer preset, Integer voice, Integer[] zones, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        //int len = zones.length;
        Integer[] changed = new Integer[0];
        try {
            if (zones.length != values.length)
                throw new IllegalArgumentException(this.getClass().toString() + ":setZonesParam -> mismatch between number of zones and number of parameter2 values!");

            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int n = 0; n < zones.length; n++)
                    changed = setZoneValue(p.getVoice(voice).getZone(zones[n]), id, values[n]);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return changed;
    }

    public void setDiversePresetParams(PresetContext.AbstractPresetParameterProfile[] paramProfiles) {
        PDBReader reader = presetDatabaseProxy.getDBRead();
        try {
            for (int i = 0; i < paramProfiles.length; i++) {
                if (paramProfiles[i] instanceof ZoneParameterProfile) {
                    ZoneParameterProfile p = (ZoneParameterProfile) paramProfiles[i];
                    try {
                        setZonesParam(p.getPreset(), p.getVoice(), new Integer[]{p.getZone()}, p.getId(), new Integer[]{p.getValue()});
                    } catch (NoSuchPresetException e) {
                    } catch (PresetEmptyException e) {
                    } catch (IllegalParameterIdException e) {
                    } catch (NoSuchVoiceException e) {
                    } catch (NoSuchZoneException e) {
                    } catch (NoSuchContextException e) {
                    } catch (ParameterValueOutOfRangeException e) {
                    }
                } else if (paramProfiles[i] instanceof VoiceParameterProfile) {
                    VoiceParameterProfile p = (VoiceParameterProfile) paramProfiles[i];
                    try {
                        setVoicesParam(p.getPreset(), new Integer[]{p.getVoice()}, p.getId(), new Integer[]{p.getValue()});
                    } catch (NoSuchPresetException e) {
                    } catch (PresetEmptyException e) {
                    } catch (IllegalParameterIdException e) {
                    } catch (NoSuchVoiceException e) {
                    } catch (NoSuchContextException e) {
                    } catch (ParameterValueOutOfRangeException e) {
                    }
                } else if (paramProfiles[i] instanceof GroupParameterProfile) {
                    GroupParameterProfile p = (GroupParameterProfile) paramProfiles[i];
                    try {
                        setGroupParam(p.getPreset(), p.getGroup(), p.getId(), p.getValue());
                    } catch (NoSuchPresetException e) {
                    } catch (PresetEmptyException e) {
                    } catch (IllegalParameterIdException e) {
                    } catch (NoSuchGroupException e) {
                    } catch (NoSuchContextException e) {
                    } catch (ParameterValueOutOfRangeException e) {
                    }
                } else if (paramProfiles[i] instanceof LinkParameterProfile) {
                    LinkParameterProfile p = (LinkParameterProfile) paramProfiles[i];
                    try {
                        setLinksParam(p.getPreset(), new Integer[]{p.getLink()}, p.getId(), new Integer[]{p.getValue()});
                    } catch (NoSuchPresetException e) {
                    } catch (PresetEmptyException e) {
                    } catch (IllegalParameterIdException e) {
                    } catch (NoSuchLinkException e) {
                    } catch (NoSuchContextException e) {
                    } catch (ParameterValueOutOfRangeException e) {
                    }
                } else if (paramProfiles[i] instanceof PresetParameterProfile) {
                    PresetParameterProfile p = (PresetParameterProfile) paramProfiles[i];
                    try {
                        setPresetParams(p.getPreset(), new Integer[]{p.getId()}, new Integer[]{p.getValue()});
                    } catch (NoSuchPresetException e) {
                    } catch (PresetEmptyException e) {
                    } catch (IllegalParameterIdException e) {
                    } catch (NoSuchContextException e) {
                    } catch (ParameterValueOutOfRangeException e) {
                    }
                }
            }

            /*for (int i = 0; i < paramProfiles.length; i++)
                paramProfiles[i].setNewValues(this);
                */
        } finally {
            reader.release();
        }
    }

    protected Integer[] setVoiceValue(VoiceObject v, Integer id, Integer val) throws PresetEmptyException, NoSuchVoiceException, IllegalParameterIdException, NoSuchContextException, NoSuchPresetException, ParameterValueOutOfRangeException {
        Integer[] ivals;
        int diff;
        ArrayList changed = new ArrayList();
        int idv = id.intValue();
        if (idv >= 129 && idv <= 182) {
            if ((idv - 129) % 3 == 0) {
                // we've got a cord src parameter
                v.setValue(id, getDeviceParameterContext().getNearestCordSrcValue(val));
            } else if ((idv - 129) % 3 == 1) {
                // we've got a cord dest parameter
                v.setValue(id, getDeviceParameterContext().getNearestCordDestValue(val));
            } else
                v.setValue(id, val);

            changed.add(id);
        } else
            switch (idv) {
                default:
                    v.setValue(id, val);
                    changed.add(id);
                    break;

                    // Key Win Low Key
                case 45:
                    ivals = v.getValues(new Integer[]{IntPool.get(46), IntPool.get(47), IntPool.get(48)});
                    if (ivals[1].intValue() < val.intValue())
                        v.setValue(id, ivals[1]);
                    else
                        v.setValue(id, val);

                    changed.add(id);

                    diff = Math.abs(ivals[1].intValue() - val.intValue());
                    if (ivals[0].intValue() > diff) {
                        v.setValue(IntPool.get(46), IntPool.get(diff));
                        changed.add(IntPool.get(46));
                    }
                    if (ivals[2].intValue() > diff) {
                        v.setValue(IntPool.get(48), IntPool.get(diff));
                        changed.add(IntPool.get(48));
                    }
                    break;

                    // Key Win Low Fade
                case 46:
                    ivals = v.getValues(new Integer[]{IntPool.get(45), IntPool.get(47)});

                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    v.setValue(id, val);
                    changed.add(id);

                    break;

                    // Key Win High Key
                case 47:
                    ivals = v.getValues(new Integer[]{IntPool.get(45), IntPool.get(46), IntPool.get(48)});
                    if (ivals[0].intValue() > val.intValue())
                        val = ivals[0];

                    v.setValue(id, val);
                    changed.add(id);

                    diff = Math.abs(val.intValue() - ivals[0].intValue());

                    if (ivals[1].intValue() > diff) {
                        v.setValue(IntPool.get(46), IntPool.get(diff));
                        changed.add(IntPool.get(46));
                    }
                    if (ivals[2].intValue() > diff) {
                        v.setValue(IntPool.get(48), IntPool.get(diff));
                        changed.add(IntPool.get(48));
                    }
                    break;
                    // Key Win High Fade
                case 48:
                    ivals = v.getValues(new Integer[]{IntPool.get(45), IntPool.get(47)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    v.setValue(id, val);
                    changed.add(id);
                    break;

                    // Vel Win Low Key
                case 49:
                    ivals = v.getValues(new Integer[]{IntPool.get(50), IntPool.get(51), IntPool.get(52)});
                    if (ivals[1].intValue() < val.intValue())
                        val = ivals[1];

                    v.setValue(id, val);
                    changed.add(id);

                    diff = Math.abs(ivals[1].intValue() - val.intValue());
                    if (ivals[0].intValue() > diff) {
                        v.setValue(IntPool.get(50), IntPool.get(diff));
                        changed.add(IntPool.get(50));
                    }
                    if (ivals[2].intValue() > diff) {
                        v.setValue(IntPool.get(52), IntPool.get(diff));
                        changed.add(IntPool.get(52));
                    }
                    break;
                    // Vel Win Low Fade
                case 50:
                    ivals = v.getValues(new Integer[]{IntPool.get(49), IntPool.get(51)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    v.setValue(id, val);
                    changed.add(id);
                    break;
                    // Vel Win High Key
                case 51:
                    ivals = v.getValues(new Integer[]{IntPool.get(49), IntPool.get(50), IntPool.get(52)});
                    if (ivals[0].intValue() > val.intValue())
                        val = ivals[0];

                    v.setValue(id, val);
                    changed.add(id);

                    diff = Math.abs(val.intValue() - ivals[0].intValue());

                    if (ivals[1].intValue() > diff) {
                        v.setValue(IntPool.get(50), IntPool.get(diff));
                        changed.add(IntPool.get(50));
                    }
                    if (ivals[2].intValue() > diff) {
                        v.setValue(IntPool.get(52), IntPool.get(diff));
                        changed.add(IntPool.get(52));
                    }
                    break;
                    // Vel Win High Fade
                case 52:
                    ivals = v.getValues(new Integer[]{IntPool.get(49), IntPool.get(51)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    v.setValue(id, val);
                    changed.add(id);
                    break;
                    // RT Win Low Key
                case 53:
                    ivals = v.getValues(new Integer[]{IntPool.get(54), IntPool.get(55), IntPool.get(56)});
                    if (ivals[1].intValue() < val.intValue())
                        val = ivals[1];

                    v.setValue(id, val);
                    changed.add(id);

                    diff = Math.abs(ivals[1].intValue() - val.intValue());
                    if (ivals[0].intValue() > diff) {
                        v.setValue(IntPool.get(54), IntPool.get(diff));
                        changed.add(IntPool.get(54));
                    }
                    if (ivals[2].intValue() > diff) {
                        v.setValue(IntPool.get(56), IntPool.get(diff));
                        changed.add(IntPool.get(56));
                    }
                    break;
                    // RT Win Low Fade
                case 54:
                    ivals = v.getValues(new Integer[]{IntPool.get(53), IntPool.get(55)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    v.setValue(id, val);
                    changed.add(id);
                    break;
                    // RT Win High Key
                case 55:
                    ivals = v.getValues(new Integer[]{IntPool.get(53), IntPool.get(54), IntPool.get(56)});
                    if (ivals[0].intValue() > val.intValue())
                        val = ivals[0];

                    v.setValue(id, val);
                    changed.add(id);

                    diff = Math.abs(val.intValue() - ivals[0].intValue());

                    if (ivals[1].intValue() > diff) {
                        v.setValue(IntPool.get(54), IntPool.get(diff));
                        changed.add(IntPool.get(54));
                    }
                    if (ivals[2].intValue() > diff) {
                        v.setValue(IntPool.get(56), IntPool.get(diff));
                        changed.add(IntPool.get(56));
                    }
                    break;
                    // RT Win High Fade
                case 56:
                    ivals = v.getValues(new Integer[]{IntPool.get(53), IntPool.get(55)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    v.setValue(id, val);
                    changed.add(id);

                    break;
            }
        return (Integer[]) changed.toArray(new Integer[changed.size()]);
    }

    protected Integer[] setZoneValue(ZoneObject z, Integer id, Integer val) throws PresetEmptyException, NoSuchZoneException, NoSuchVoiceException, IllegalParameterIdException, NoSuchContextException, NoSuchPresetException, ParameterValueOutOfRangeException {
        Integer[] ivals;
        int diff;
        ArrayList changed = new ArrayList();
        switch (id.intValue()) {
            default:
                z.setValue(id, val);
                changed.add(id);
                break;
                // Key Win Low Key
            case 45:
                ivals = z.getValues(new Integer[]{IntPool.get(46), IntPool.get(47), IntPool.get(48)});
                if (ivals[1].intValue() < val.intValue())
                    val = ivals[1];

                changed.add(id);
                z.setValue(id, val);

                diff = Math.abs(ivals[1].intValue() - val.intValue());
                if (ivals[0].intValue() > diff) {
                    z.setValue(IntPool.get(46), IntPool.get(diff));
                    changed.add(IntPool.get(46));
                }
                if (ivals[2].intValue() > diff) {
                    z.setValue(IntPool.get(48), IntPool.get(diff));
                    changed.add(IntPool.get(48));
                }
                break;
                // Key Win Low Fade
            case 46:
                ivals = z.getValues(new Integer[]{IntPool.get(45), IntPool.get(47)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                z.setValue(id, val);
                changed.add(id);
                break;
                // Key Win High Key
            case 47:
                ivals = z.getValues(new Integer[]{IntPool.get(45), IntPool.get(46), IntPool.get(48)});

                if (ivals[0].intValue() > val.intValue())
                    val = ivals[0];

                z.setValue(id, val);
                changed.add(id);

                diff = Math.abs(val.intValue() - ivals[0].intValue());

                if (ivals[1].intValue() > diff) {
                    z.setValue(IntPool.get(46), IntPool.get(diff));
                    changed.add(IntPool.get(46));
                }
                if (ivals[2].intValue() > diff) {
                    z.setValue(IntPool.get(48), IntPool.get(diff));
                    changed.add(IntPool.get(48));
                }
                break;
                // Key Win High Fade
            case 48:
                ivals = z.getValues(new Integer[]{IntPool.get(45), IntPool.get(47)});

                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                z.setValue(id, val);
                changed.add(id);
                break;

                // Vel Win Low Key
            case 49:
                ivals = z.getValues(new Integer[]{IntPool.get(50), IntPool.get(51), IntPool.get(52)});

                if (ivals[1].intValue() < val.intValue())
                    val = ivals[1];

                z.setValue(id, val);
                changed.add(id);

                diff = Math.abs(ivals[1].intValue() - val.intValue());
                if (ivals[0].intValue() > diff) {
                    z.setValue(IntPool.get(50), IntPool.get(diff));
                    changed.add(IntPool.get(50));
                }
                if (ivals[2].intValue() > diff) {
                    z.setValue(IntPool.get(52), IntPool.get(diff));
                    changed.add(IntPool.get(52));
                }
                break;
                // Vel Win Low Fade
            case 50:
                ivals = z.getValues(new Integer[]{IntPool.get(49), IntPool.get(51)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                z.setValue(id, val);
                changed.add(id);

                break;
                // Vel Win High Key
            case 51:
                ivals = z.getValues(new Integer[]{IntPool.get(49), IntPool.get(50), IntPool.get(52)});
                if (ivals[0].intValue() > val.intValue())
                    val = ivals[0];

                z.setValue(id, val);
                changed.add(id);

                diff = Math.abs(val.intValue() - ivals[0].intValue());

                if (ivals[1].intValue() > diff) {
                    z.setValue(IntPool.get(50), IntPool.get(diff));
                    changed.add(IntPool.get(50));
                }
                if (ivals[2].intValue() > diff) {
                    z.setValue(IntPool.get(52), IntPool.get(diff));
                    changed.add(IntPool.get(50));
                }
                break;

                // Vel Win High Fade
            case 52:
                ivals = z.getValues(new Integer[]{IntPool.get(49), IntPool.get(51)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                z.setValue(id, val);
                changed.add(id);

                break;
        }
        return (Integer[]) changed.toArray(new Integer[changed.size()]);
    }

    protected Integer[] setLinkValue(LinkObject l, Integer id, Integer val) throws PresetEmptyException, IllegalParameterIdException, NoSuchContextException, NoSuchPresetException, ParameterValueOutOfRangeException, NoSuchLinkException {
        Integer[] ivals;
        int diff;
        ArrayList changed = new ArrayList();
        switch (id.intValue()) {
            default:
                l.setValue(id, val);
                changed.add(id);
                break;
                // Key Win Low Key
            case 28:
                ivals = l.getValues(new Integer[]{IntPool.get(29), IntPool.get(30), IntPool.get(31)});
                if (ivals[1].intValue() < val.intValue())
                    val = ivals[1];

                l.setValue(id, val);
                changed.add(id);

                diff = Math.abs(ivals[1].intValue() - val.intValue());
                if (ivals[0].intValue() > diff) {
                    l.setValue(IntPool.get(29), IntPool.get(diff));
                    changed.add(IntPool.get(29));
                }
                if (ivals[2].intValue() > diff) {
                    l.setValue(IntPool.get(31), IntPool.get(diff));
                    changed.add(IntPool.get(31));
                }
                break;
                // Key Win Low Fade
            case 29:
                ivals = l.getValues(new Integer[]{IntPool.get(28), IntPool.get(30)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));
                l.setValue(id, val);
                changed.add(id);

                break;
                // Key Win High Key
            case 30:
                ivals = l.getValues(new Integer[]{IntPool.get(28), IntPool.get(29), IntPool.get(31)});
                if (ivals[0].intValue() > val.intValue())
                    val = ivals[0];
                l.setValue(id, val);
                changed.add(id);

                diff = Math.abs(val.intValue() - ivals[0].intValue());

                if (ivals[1].intValue() > diff) {
                    l.setValue(IntPool.get(29), IntPool.get(diff));
                    changed.add(IntPool.get(29));
                }
                if (ivals[2].intValue() > diff) {
                    l.setValue(IntPool.get(31), IntPool.get(diff));
                    changed.add(IntPool.get(31));
                }
                break;
                // Key Win High Fade
            case 31:
                ivals = l.getValues(new Integer[]{IntPool.get(28), IntPool.get(30)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));
                l.setValue(id, val);
                changed.add(id);

                break;

                // Vel Win Low Key
            case 32:
                ivals = l.getValues(new Integer[]{IntPool.get(33), IntPool.get(34), IntPool.get(35)});
                if (ivals[1].intValue() < val.intValue())
                    val = ivals[1];

                l.setValue(id, val);
                changed.add(id);

                diff = Math.abs(ivals[1].intValue() - val.intValue());
                if (ivals[0].intValue() > diff) {
                    l.setValue(IntPool.get(33), IntPool.get(diff));
                    changed.add(IntPool.get(33));
                }
                if (ivals[2].intValue() > diff) {
                    l.setValue(IntPool.get(35), IntPool.get(diff));
                    changed.add(IntPool.get(35));
                }
                break;
                // Vel Win Low Fade
            case 33:
                ivals = l.getValues(new Integer[]{IntPool.get(32), IntPool.get(34)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                l.setValue(id, val);
                changed.add(id);

                break;
                // Vel Win High Key
            case 34:
                ivals = l.getValues(new Integer[]{IntPool.get(32), IntPool.get(33), IntPool.get(35)});
                if (ivals[0].intValue() > val.intValue())
                    val = ivals[0];
                l.setValue(id, val);
                changed.add(id);

                diff = Math.abs(val.intValue() - ivals[0].intValue());

                if (ivals[1].intValue() > diff) {
                    l.setValue(IntPool.get(33), IntPool.get(diff));
                    changed.add(IntPool.get(33));
                }
                if (ivals[2].intValue() > diff) {
                    l.setValue(IntPool.get(35), IntPool.get(diff));
                    changed.add(IntPool.get(35));
                }
                break;
                // Vel Win High Fade
            case 35:
                ivals = l.getValues(new Integer[]{IntPool.get(32), IntPool.get(34)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));
                l.setValue(id, val);
                changed.add(id);

                break;
        }
        return (Integer[]) changed.toArray(new Integer[changed.size()]);
    }

    public PresetContext getDelegatingPresetContext() {
        return this;
    }

    public String getDeviceString() {
        return "No Device - PresetContext is offline.";
    }

    public DeviceParameterContext getDeviceParameterContext() {
        return device.getDeviceParameterContext();
    }

    public DeviceContext getDeviceContext() {
        return device;
    }

    public SampleContext getRootSampleContext() {
        return presetDatabaseProxy.getRootSampleContext();
    }

    public void setDevice(DeviceContext device) {
        this.device = device;
    }
}
