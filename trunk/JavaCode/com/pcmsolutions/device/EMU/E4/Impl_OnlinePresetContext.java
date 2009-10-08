package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.RemoteDeviceDidNotRespondException;
import com.pcmsolutions.comms.RemoteMessagingException;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 14:27:03
 */
class Impl_OnlinePresetContext implements PresetContext, RemoteObjectStates, Serializable, RemoteAssignable {
    protected transient Remotable remote;
    protected Impl_PresetContext presetContext;

    public Impl_OnlinePresetContext(DeviceContext device, String name, PresetDatabaseProxy pdbp, Remotable remote) {
        this.remote = remote;
        presetContext = new Impl_PresetContext(device, name, pdbp) {
            public PresetContext getDelegatingPresetContext() {
                return Impl_OnlinePresetContext.this;
            }
        };
    }

    public void setRemote(Remotable remote) {
        this.remote = remote;
    }

    public String toString() {
        return "Online Presets";
    }

    public void applySampleToPreset(Integer preset, Integer sample, int mode) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, ParameterValueOutOfRangeException, TooManyVoicesException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                presetContext.applySampleToPreset(preset, sample, mode);
                ParameterEditLoader pl = remote.getEditLoader();
                switch (mode) {
                    case PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES:
                        for (int i = 0, j = p.numVoices(); i < j; i++) {
                            pl.selVoice(preset, IntPool.get(i));
                            pl.add(ID.sample, sample);
                        }
                        pl.dispatch();
                        break;
                    case PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES_AND_ZONES:
                        for (int i = 0, j = p.numVoices(); i < j; i++) {
                            pl.selVoice(preset, IntPool.get(i));
                            pl.add(ID.sample, sample);
                            for (int k = 0, l = p.getVoice(IntPool.get(i)).numZones(); k < l; k++) {
                                pl.selZone(preset, IntPool.get(i), IntPool.get(k));
                                pl.add(ID.sample, sample);
                            }
                        }
                        pl.dispatch();
                        break;
                    case PresetContext.MODE_APPLY_SAMPLE_TO_ALL_ZONES:
                        for (int i = 0, j = p.numVoices(); i < j; i++) {
                            for (int k = 0, l = p.getVoice(IntPool.get(i)).numZones(); k < l; k++) {
                                pl.selZone(preset, IntPool.get(i), IntPool.get(k));
                                pl.add(ID.sample, sample);
                            }
                        }
                        pl.dispatch();
                        break;
                    case PresetContext.MODE_APPLY_SAMPLE_TO_NEW_VOICE:
                        remote.getPresetContext().cmd_newVoice(preset);
                        pl.selVoice(preset, IntPool.get(p.numVoices() - 1));
                        pl.add(ID.sample, sample);
                        pl.add(IntPool.get(37), presetContext.getVoiceParams(preset, IntPool.get(p.numVoices() - 1), new Integer[]{IntPool.get(37)})[0]);
                        pl.add(IntPool.get(44), presetContext.getVoiceParams(preset, IntPool.get(p.numVoices() - 1), new Integer[]{IntPool.get(44)})[0]);
                        pl.dispatch();
                        break;
                }
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logInternalError(e);
            } catch (IllegalParameterIdException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logInternalError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void applySamplesToPreset(Integer preset, Integer[] samples, int mode) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, ParameterValueOutOfRangeException, TooManyVoicesException, TooManyZonesException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                presetContext.applySamplesToPreset(preset, samples, mode);
                ParameterEditLoader pl = remote.getEditLoader();
                switch (mode) {
                    case PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICE_AND_ZONES:
                        {
                            Integer lv = IntPool.get(p.numVoices() - 1);
                            remote.getPresetContext().cmd_newVoice(preset);
                            pl.add(IntPool.get(37), presetContext.getVoiceParams(preset, lv, new Integer[]{IntPool.get(37)})[0]);

                            for (int k = 0, l = p.getVoice(lv).numZones(); k < l; k++) {
                                if (k != 0)
                                    remote.getVoiceContext().cmd_newZone(preset, lv);
                                pl.selZone(preset, lv, IntPool.get(k));
                                pl.add(ID.sample, samples[k]);
                                pl.add(IntPool.get(44), presetContext.getZoneParams(preset, lv, IntPool.get(k), new Integer[]{IntPool.get(44)})[0]);
                            }
                            pl.dispatch();
                        }
                        break;
                    case PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICES:
                        int j = p.numVoices();
                        int si = 0;
                        for (int i = j - samples.length; i < j; i++) {
                            remote.getPresetContext().cmd_newVoice(preset);
                            pl.selVoice(preset, IntPool.get(i));
                            pl.add(ID.sample, samples[si++]);
                            pl.add(IntPool.get(37), presetContext.getVoiceParams(preset, IntPool.get(i), new Integer[]{IntPool.get(37)})[0]);
                            pl.add(IntPool.get(44), presetContext.getVoiceParams(preset, IntPool.get(i), new Integer[]{IntPool.get(44)})[0]);
                        }
                        pl.dispatch();
                        break;
                }
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logInternalError(e);
            } catch (IllegalParameterIdException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logInternalError(e);
            } catch (NoSuchZoneException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logInternalError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void lockPresetWrite(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        presetContext.lockPresetWrite(preset);
    }

    public void unlockPreset(Integer preset) {
        presetContext.unlockPreset(preset);
    }

    public boolean isPresetInitialized(Integer preset) throws NoSuchPresetException {
        return presetContext.isPresetInitialized(preset);
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
        presetContext.assertPresetNamed(preset);
    }

    public int getPresetState(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        return presetContext.getPresetState(preset);
    }

    public String getPresetSummary(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        return presetContext.getPresetSummary(preset);
    }

    public boolean isPresetWriteLocked(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        return presetContext.isPresetWriteLocked(preset);
    }

    public boolean isPresetWritable(Integer preset) {
        return presetContext.isPresetWritable(preset);
    }

    public Set getPresetSet(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        return presetContext.getPresetSet(preset);
    }

    public String getPresetName(Integer preset) throws NoSuchPresetException, PresetEmptyException {
        return presetContext.getPresetName(preset);
    }

    public void newPreset(Integer preset, String name) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            reader.lockPresetWrite(getDelegatingPresetContext(), preset);
            try {
                PresetObject np;
                presetContext.newPreset(preset, name);
                np = reader.getPresetRead(getDelegatingPresetContext(), preset);
                try {
                    remote.getPresetContext().edit_dump(new ByteArrayInputStream(np.getDumpBytes().toByteArray()), null);
                } finally {
                    reader.unlockPreset(preset);
                }
            } catch (IOException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteDeviceDidNotRespondException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (PresetEmptyException e) {
                // should never get here!!
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void newPreset(IsolatedPreset ip, Integer preset, String name) throws NoSuchPresetException, NoSuchContextException {
        if (preset.intValue() >= DeviceContext.BASE_FLASH_PRESET)
            throw new IllegalArgumentException("Can't write to a Flash Preset");
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            reader.lockPresetWrite(getDelegatingPresetContext(), preset);
            try {
                PresetObject np;
                presetContext.newPreset(ip, preset, name);
                np = reader.getPresetRead(getDelegatingPresetContext(), preset);
                try {
                    if (reader.remoteInitializePresetAtIndex(preset, new ByteArrayInputStream(np.getDumpBytes().toByteArray())) == false) {
                        reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                        presetContext.device.logCommError("Could not remotely initialize preset " + preset);
                    }
                } catch (IOException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                } finally {
                    reader.unlockPreset(preset);
                }
            } catch (PresetEmptyException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void newPreset(IsolatedPreset ip, Integer preset, String name, Map sampleTranslationMap, Integer defaultSampleTranslation, Map linkTranslationMap) throws NoSuchPresetException, NoSuchContextException {
        presetContext.newPreset(ip, preset, name, sampleTranslationMap, defaultSampleTranslation, linkTranslationMap);
        assertPresetRemote(preset);
    }

    public void combineVoices(Integer preset, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Integer[] vig = p.getVoiceIndexesInGroup(group);
                if (vig.length == 0)
                    return;
                presetContext.combineVoices(preset, group);
                remote.getPresetContext().cmd_combineVoices(preset, group);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
                e.printStackTrace();
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
                e.printStackTrace();
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Set getUsedGroupIndexes(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.getUsedGroupIndexes(preset);
    }

    public Integer getNextAvailableGroup(Integer preset, boolean atTail) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.getNextAvailableGroup(preset, atTail);
    }

    private final Integer[] klh = new Integer[]{IntPool.get(45), IntPool.get(47)};

    public void splitVoice(Integer preset, Integer voice, int splitKey) throws NoSuchContextException, NoSuchPresetException, PresetEmptyException, TooManyVoicesException, ParameterValueOutOfRangeException, NoSuchVoiceException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Integer ai = p.splitVoice(voice, splitKey);
                VoiceObject ov = p.getVoice(voice), nv = p.getVoice(ai);
                remote.getVoiceContext().cmd_copy(preset, voice, preset, ov.getValue(IntPool.get(37)));
                ParameterEditLoader pel = remote.getEditLoader();
                pel.selVoice(preset, voice);
                pel.add(klh, ov.getValues(klh));
                pel.selVoice(preset, ai);
                pel.add(klh, nv.getValues(klh));
                pel.dispatch();
            } catch (IllegalParameterIdException e) {
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer preset, Integer voice) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException, NoSuchVoiceException {
        return presetContext.getIsolatedVoice(preset, voice);
    }

    public void purgeZones(Integer preset) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int i = 0, j = p.numVoices(); i < j; i++) {
                    VoiceObject v = p.getVoice(IntPool.get(i));
                    for (int x = v.numZones() - 1; x > 0; x--) {
                        v.rmvZone(IntPool.get(x));
                        remote.getZoneContext().cmd_delete(preset, v.getVoice(), IntPool.get(x));
                    }
                }
                //super.purgeZones(preset);
            } catch (NoSuchVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchZoneException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public int numPresetZones(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.numPresetZones(preset);
    }

    public int numPresetSamples(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.numPresetSamples(preset);
    }

    public Set presetSampleSet(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.presetSampleSet(preset);
    }

    public IntegerUseMap presetSampleUsage(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.presetSampleUsage(preset);
    }

    public int numPresetLinkPresets(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.numPresetLinkPresets(preset);
    }

    public Set presetLinkPresetSet(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.presetLinkPresetSet(preset);
    }

    public IntegerUseMap presetLinkPresetUsage(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.presetLinkPresetUsage(preset);
    }

    public ContextReadablePreset getContextPreset(Integer preset) throws NoSuchPresetException {
        return presetContext.getContextPreset(preset);
    }

    public ReadablePreset getReadablePreset(Integer preset) throws NoSuchPresetException {
        return presetContext.getReadablePreset(preset);
    }

    public ContextEditablePreset getEditablePreset(Integer preset) throws NoSuchPresetException {
        return presetContext.getEditablePreset(preset);
    }


    public Map offsetLinkIndexes(Integer preset, Integer offset, boolean user) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Map m = presetContext.offsetLinkIndexes(preset, offset, user);
                ParameterEditLoader pl = remote.getEditLoader();
                Object key;
                for (Iterator i = m.keySet().iterator(); i.hasNext();) {
                    key = i.next();
                    pl.selLink(preset, (Integer) key);
                    pl.add(IntPool.get(23), (Integer) m.get(key));
                }
                pl.dispatch();
                return m;
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return new HashMap();
    }

    public Map offsetSampleIndexes(Integer preset, Integer offset, boolean user) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Map m1,m2;
                m1 = presetContext.offsetSampleIndexes(preset, offset, user);
                ParameterEditLoader pl = remote.getEditLoader();
                Object key, key2;
                for (Iterator i = m1.keySet().iterator(); i.hasNext();) {
                    key = i.next();
                    if (m1.get(key) instanceof Integer) {
                        pl.selVoice(preset, (Integer) key);
                        pl.add(ID.sample, (Integer) m1.get(key));
                    } else if (m1.get(key) instanceof Map) {
                        m2 = (Map) m1.get(key);
                        for (Iterator j = m2.keySet().iterator(); j.hasNext();) {
                            key2 = j.next();
                            pl.selZone(preset, (Integer) key, (Integer) key2);
                            pl.add(ID.sample, (Integer) m2.get(key2));
                        }
                    }
                }
                pl.dispatch();
                return m1;
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return new HashMap();
    }

    public Map remapLinkIndexes(Integer preset, Map translationMap) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Map m = presetContext.remapLinkIndexes(preset, translationMap);
                ParameterEditLoader pl = remote.getEditLoader();
                Object key;
                for (Iterator i = m.keySet().iterator(); i.hasNext();) {
                    key = i.next();
                    pl.selLink(preset, (Integer) key);
                    pl.add(IntPool.get(23), (Integer) m.get(key));
                }
                pl.dispatch();
                return m;
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return new HashMap();
    }

    public Map remapSampleIndexes(Integer preset, Map translationMap, Integer defaultSampleTranslation) throws NoSuchPresetException, NoSuchContextException, PresetEmptyException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Map m1,m2;
                m1 = presetContext.remapSampleIndexes(preset, translationMap, null);
                ParameterEditLoader pl = remote.getEditLoader();
                Object key, key2;
                for (Iterator i = m1.keySet().iterator(); i.hasNext();) {
                    key = i.next();
                    if (m1.get(key) instanceof Integer) {
                        pl.selVoice(preset, (Integer) key);
                        pl.add(ID.sample, (Integer) m1.get(key));
                    } else if (m1.get(key) instanceof Map) {
                        m2 = (Map) m1.get(key);
                        for (Iterator j = m2.keySet().iterator(); j.hasNext();) {
                            key2 = j.next();
                            pl.selZone(preset, (Integer) key, (Integer) key2);
                            pl.add(ID.sample, (Integer) m2.get(key2));
                        }
                    }
                }
                pl.dispatch();
                return m1;
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return new HashMap();
    }

    public IsolatedPreset getIsolatedPreset(Integer preset) throws NoSuchContextException, NoSuchPresetException, PresetEmptyException {
        return presetContext.getIsolatedPreset(preset);
    }

    public void copyLink(Integer srcPreset, Integer srcLink, Integer destPreset) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject[] pobjs = reader.getPresetRW(getDelegatingPresetContext(), srcPreset, destPreset);

            try {
                LinkObject l = pobjs[0].getLink(srcLink);
                pobjs[1].addLinks(new LinkObject[]{l});
                remote.getLinkContext().cmd_copy(srcPreset, srcLink, destPreset);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, new UninitPresetObject(destPreset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(srcPreset);
                reader.unlockPreset(destPreset);
            }
        } finally {
            reader.release();
        }
    }

    public Integer[] getLinkParams(Integer preset, Integer link, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, NoSuchContextException {
        return presetContext.getLinkParams(preset, link, ids);
    }

    public void copyPreset(Integer srcPreset, Integer destPreset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        copyPreset(srcPreset, destPreset, (String) null);
    }

    public void copyPreset(Integer srcPreset, Integer destPreset, Map presetLinkTranslationMap) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        boolean srcIsUser = srcPreset.intValue() <= DeviceContext.MAX_USER_PRESET;
        boolean destIsUser = destPreset.intValue() <= DeviceContext.MAX_USER_PRESET;

        if (!srcIsUser && !destIsUser)
            throw new IllegalArgumentException("can't translate flash presets");
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            reader.getPresetRC(getDelegatingPresetContext(), srcPreset, destPreset);
            try {
                ParameterEditLoader pl = remote.getEditLoader();
                Integer link;
                Integer val;
                int nl = numLinks(srcPreset);

                if (srcIsUser) {
                    Integer[] origVals = new Integer[nl];

                    for (int l = 0; l < nl; l++) {
                        link = IntPool.get(l);
                        val = getLinkParams(srcPreset, link, new Integer[]{IntPool.get(23)})[0];
                        if (presetLinkTranslationMap.containsKey(val)) {
                            pl.selLink(srcPreset, link);
                            pl.add(IntPool.get(23), (Integer) presetLinkTranslationMap.get(val));
                            origVals[l] = val;
                        } else
                            origVals = null;
                    }
                    pl.dispatch();

                    copyPreset(srcPreset, destPreset, (String) null);
                    remapLinkIndexes(destPreset, presetLinkTranslationMap);

                    for (int l = 0; l < nl; l++) {
                        if (origVals[l] != null) {
                            link = IntPool.get(l);
                            pl.selLink(srcPreset, link);
                            pl.add(IntPool.get(23), origVals[l]);
                        }
                    }
                    pl.dispatch();
                } else {   // dest must be user
                    copyPreset(srcPreset, destPreset, (String) null);
                    remapLinkIndexes(destPreset, presetLinkTranslationMap);
                    for (int l = 0; l < nl; l++) {
                        link = IntPool.get(l);
                        val = getLinkParams(destPreset, link, new Integer[]{IntPool.get(23)})[0];
                        if (presetLinkTranslationMap.containsKey(val)) {
                            pl.selLink(destPreset, link);
                            pl.add(IntPool.get(23), (Integer) presetLinkTranslationMap.get(val));
                        }
                    }
                    pl.dispatch();
                }
            } catch (IllegalParameterIdException e) {
                reader.changePresetObject(getDelegatingPresetContext(), srcPreset, new UninitPresetObject(srcPreset));
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, new UninitPresetObject(destPreset));
                presetContext.device.logInternalError(e);
            } catch (NoSuchLinkException e) {
                reader.changePresetObject(getDelegatingPresetContext(), srcPreset, new UninitPresetObject(srcPreset));
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, new UninitPresetObject(destPreset));
                presetContext.device.logInternalError(e);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), srcPreset, new UninitPresetObject(srcPreset));
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, new UninitPresetObject(destPreset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), srcPreset, new UninitPresetObject(srcPreset));
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, new UninitPresetObject(destPreset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(srcPreset);
                reader.unlockPreset(destPreset);
            }
        } finally {
            reader.release();
        }
    }

    public void copyPreset(Integer srcPreset, Integer destPreset, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        if (srcPreset.intValue() == destPreset.intValue())
            return;
        if (isPresetEmpty(srcPreset))
            throw new PresetEmptyException(srcPreset);

        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            Object[] pobjs = reader.getPresetRC(getDelegatingPresetContext(), srcPreset, destPreset);
            try {
                PresetObject np = new PresetObject((PresetObject) pobjs[0], destPreset, presetContext.presetDatabaseProxy.getPresetEventHandler(), presetContext.device.getDeviceParameterContext());
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, np);

                remote.getPresetContext().cmd_copy(srcPreset, destPreset);

                if (name != null) {
                    np.setName(name);
                    remote.getPresetContext().edit_name(destPreset, name);
                }

            } catch (NoSuchPresetException e) {
                // error!
                throw new IllegalStateException(this.getClass().toString() + ":copyPreset->Cannot find destination preset after locking it!");
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, new UninitPresetObject(destPreset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, new UninitPresetObject(destPreset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(srcPreset);
                reader.unlockPreset(destPreset);
            }
        } finally {
            reader.release();
        }
    }

    public void copyVoice(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyVoicesException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
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
                remote.getVoiceContext().cmd_copy(srcPreset, srcVoice, destPreset, group);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, new UninitPresetObject(destPreset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), destPreset, new UninitPresetObject(destPreset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(srcPreset);
                reader.unlockPreset(destPreset);
            }
        } finally {
            reader.release();
        }
    }

    public int numVoices(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.numVoices(preset);
    }

    public Integer[] getGroupParams(Integer preset, Integer group, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException, NoSuchContextException {
        return presetContext.getGroupParams(preset, group, ids);
    }

    public Integer[] getVoiceParams(Integer preset, Integer voice, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException {
        return presetContext.getVoiceParams(preset, voice, ids);
    }

    public Integer[] setGroupParamFromVoice(Integer preset, Integer voice, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        Integer[] changed = new Integer[0];
        try {
            PresetObject p = reader.getPresetWrite(this, preset);
            try {
                Integer group = p.getVoice(voice).getValues(new Integer[]{IntPool.get(37)})[0];
                changed = setGroupParam(preset, group, id, value);
            } catch (NoSuchGroupException e) {
                throw new NoSuchVoiceException();  // should never get here!!!
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return changed;
    }

    public Integer[] setGroupParam(Integer preset, Integer group, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        Integer[] changed = new Integer[0];
        try {
            PresetObject p = reader.getPresetWrite(this, preset);
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


    public void erasePreset(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            reader.changePresetObject(getDelegatingPresetContext(), preset, EmptyPreset.getInstance());
            remote.getPresetContext().cmd_erase(preset);
        } catch (RemoteUnreachableException e) {
            reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            presetContext.device.logCommError(e);
        } catch (RemoteMessagingException e) {
            reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            presetContext.device.logCommError(e);
        } finally {
            reader.release();
        }
    }

    public void refreshPreset(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        presetContext.refreshPreset(preset);
    }

    public Integer[] getPresetParams(Integer preset, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchContextException {
        return presetContext.getPresetParams(preset, ids);
    }

    public void expandVoice(Integer preset, Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException, TooManyVoicesException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                presetContext.expandVoice(preset, voice);
                remote.getVoiceContext().cmd_expandVoice(preset, voice);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public boolean trySetOriginalKeyFromName(Integer preset, Integer voice, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            boolean b = presetContext.trySetOriginalKeyFromName(preset, voice, name);
            if (b) {
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selVoice(preset, voice);
                try {
                    pl.add(IntPool.get(44), presetContext.getVoiceParams(preset, voice, new Integer[]{IntPool.get(44)})[0]);
                    pl.dispatch();
                } catch (IllegalParameterIdException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logInternalError(e);
                } catch (RemoteUnreachableException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                } catch (RemoteMessagingException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                }
            }
            return b;
        } finally {
            reader.release();
        }
    }

    public boolean trySetOriginalKeyFromName(Integer preset, Integer voice, Integer zone, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException, NoSuchZoneException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            boolean b = presetContext.trySetOriginalKeyFromName(preset, voice, zone, name);
            if (b) {
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selVoice(preset, voice);
                try {
                    pl.add(IntPool.get(44), presetContext.getZoneParams(preset, voice, zone, new Integer[]{IntPool.get(44)})[0]);
                    pl.dispatch();
                } catch (IllegalParameterIdException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logInternalError(e);
                } catch (RemoteUnreachableException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                } catch (RemoteMessagingException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                }
            }
            return b;
        } finally {
            reader.release();
        }
    }

    public boolean trySetOriginalKeyFromSampleName(Integer preset, Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException, SampleEmptyException, NoSuchSampleException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            boolean b = presetContext.trySetOriginalKeyFromSampleName(preset, voice);
            if (b) {
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selVoice(preset, voice);
                try {
                    pl.add(IntPool.get(44), presetContext.getVoiceParams(preset, voice, new Integer[]{IntPool.get(44)})[0]);
                    pl.dispatch();
                } catch (IllegalParameterIdException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logInternalError(e);
                } catch (RemoteUnreachableException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                } catch (RemoteMessagingException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                }
            }
            return b;
        } finally {
            reader.release();
        }
    }

    public boolean trySetOriginalKeyFromSampleName(Integer preset, Integer voice, Integer zone) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchVoiceException, NoSuchZoneException, SampleEmptyException, NoSuchSampleException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            boolean b = presetContext.trySetOriginalKeyFromSampleName(preset, voice, zone);
            if (b) {
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selVoice(preset, voice);
                try {
                    pl.add(IntPool.get(44), presetContext.getZoneParams(preset, voice, zone, new Integer[]{IntPool.get(44)})[0]);
                    pl.dispatch();
                } catch (IllegalParameterIdException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logInternalError(e);
                } catch (RemoteUnreachableException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                } catch (RemoteMessagingException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                }
            }
            return b;
        } finally {
            reader.release();
        }
    }

    public void getVoiceMultiSample(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer destVoice) {
        presetContext.getVoiceMultiSample(srcPreset, srcVoice, destPreset, destVoice);
    }

    public void sortVoices(Integer preset, Integer[] ids) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                presetContext.sortVoices(preset, ids);
                remote.getPresetContext().edit_dump(new ByteArrayInputStream(p.getDumpBytes().toByteArray()), null);
            } catch (IOException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteDeviceDidNotRespondException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void sortLinks(Integer preset, Integer[] ids) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                presetContext.sortLinks(preset, ids);
                ParameterEditLoader pl = remote.getEditLoader();
                Integer l;
                for (int i = 0; i < p.numLinks(); i++) {
                    l = IntPool.get(i);
                    pl.selLink(preset, l);
                    pl.add(p.getLink(l).getAllIds(), p.getLink(l).getAllValues());
                    pl.dispatch();
                }
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchLinkException e) {
                // should never get here
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void sortZones(Integer preset, Integer[] ids) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                presetContext.sortZones(preset, ids);
                ParameterEditLoader pl = remote.getEditLoader();
                Integer v;
                Integer z;
                for (int i = 0; i < p.numVoices(); i++) {
                    v = IntPool.get(i);
                    VoiceObject vobj = p.getVoice(v);
                    for (int j = 0; j < vobj.numZones(); j++) {
                        z = IntPool.get(j);
                        pl.selZone(preset, v, z);
                        pl.add(vobj.getZone(z).getAllIds(), vobj.getZone(z).getAllValues());
                        pl.dispatch();
                    }
                }
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchVoiceException e) {
                // should never get here
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchZoneException e) {
                // should never get here
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void refreshVoiceParameters(Integer preset, Integer voice, Integer[] ids) throws NoSuchContextException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException, ParameterValueOutOfRangeException, IllegalParameterIdException {
        if (preset.intValue() > DeviceContext.MAX_USER_PRESET) {
            refreshPreset(preset);
            return;
        }

        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                VoiceObject v = p.getVoice(voice);
                remote.getEditLoader().selVoice(preset, voice).dispatch();
                v.setValues(ids, remote.getParameterContext().req_prmValues(ids, false));
            } catch (RemoteUnreachableException e) {
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                presetContext.device.logCommError(e);
            } catch (RemoteDeviceDidNotRespondException e) {
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public Integer[] getVoiceIndexesInGroupFromVoice(Integer preset, Integer voice) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, NoSuchVoiceException {
        return presetContext.getVoiceIndexesInGroupFromVoice(preset, voice);
    }

    public Integer[] getVoiceIndexesInGroup(Integer preset, Integer group) throws NoSuchGroupException, PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        return presetContext.getVoiceIndexesInGroup(preset, group);
    }

    // LINK
    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer preset, Integer link) throws NoSuchLinkException, NoSuchPresetException, NoSuchContextException, PresetEmptyException {
        return presetContext.getIsolatedLink(preset, link);
    }
/*
        public Integer newZone(Integer preset, Integer voice, IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, NoSuchVoiceException, TooManyZonesException {
            PDBReader reader = ipc.pdbp.getDBRead();
            Integer addedIndex = IntPool.get(0);
            try {
                PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
                try {
                    addedIndex = p.getIsloatedZone(voice).addZone(iz, true);
                    remote.getVoiceContext().cmd_newZone(preset, voice);
                    ParameterEditLoader pl = remote.getEditLoader();
                    pl.selZone(preset, voice, addedIndex);
                    ZoneObject z =p.getIsloatedZone(voice).getIsloatedZone(addedIndex);
                    pl.addDesktopElement(z.getAllIds(), z.getAllValues());
                    pl.dispatch();
                } catch (RemoteMessagingException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    logCommError(e);
                } catch (RemoteUnreachableException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    logCommError(e);
                } catch (NoSuchZoneException e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    logCommError(e);
                } finally {
                    reader.unlockPreset(preset);
                }
            } finally {
                reader.release();
            }
            return addedIndex;
        }
  */
    // LINK
    public Integer newLink(Integer preset, IsolatedPreset.IsolatedLink il) throws TooManyVoicesException, PresetEmptyException, NoSuchContextException, NoSuchPresetException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        Integer addedIndex = IntPool.get(0);
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                addedIndex = p.addLink(il);
                remote.getPresetContext().cmd_newLink(preset);
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selLink(preset, addedIndex);
                LinkObject l = p.getLink(addedIndex);
                pl.add(l.getAllIds(), l.getAllValues());
                pl.dispatch();
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchLinkException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return addedIndex;
    }

    public Integer newLinks(Integer preset, Integer num, Integer[] presetNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        Integer addedIndex = IntPool.get(0);
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                addedIndex = p.addLinks(num, presetNums);
                ParameterEditLoader pl = remote.getEditLoader();
                int tot = num.intValue();
                int ai = addedIndex.intValue();
                Integer id = IntPool.get(23);
                for (int n = 0; n < tot; n++) {
                    remote.getPresetContext().cmd_newLink(preset);
                    pl.selLink(preset, IntPool.get(ai + n));
                    pl.add(id, presetNums[n]);
                }
                pl.dispatch();
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return addedIndex;
    }

    public Integer newVoice(Integer preset, IsolatedPreset.IsolatedVoice iv) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, TooManyVoicesException, TooManyZonesException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        Integer addedIndex = IntPool.get(0);
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                addedIndex = p.addVoice(iv);
                remote.getPresetContext().cmd_newVoice(preset);
                ParameterEditLoader pl = remote.getEditLoader();

                pl.selVoice(preset, addedIndex);
                VoiceObject v = p.getVoice(addedIndex);
                pl.add(v.getAllIds(), v.getAllValues());
                pl.dispatch();

                for (int i = 0,j = iv.numZones(); i < j; i++) {
                    if (i != j - 1) // don't need the last zone as creation of first zone yields two zones!
                        remote.getVoiceContext().cmd_newZone(preset, addedIndex);
                    pl.selZone(preset, addedIndex, IntPool.get(i));
                    ZoneObject z = p.getVoice(addedIndex).getZone(IntPool.get(i));
                    pl.add(z.getAllIds(), z.getAllValues());
                    pl.dispatch();
                }

            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchVoiceException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchZoneException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return addedIndex;

    }

    public Integer newVoices(Integer preset, Integer num, Integer[] sampleNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        Integer addedIndex = IntPool.get(0);
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                addedIndex = p.addVoices(num, sampleNums);
                ParameterEditLoader pl = remote.getEditLoader();
                int tot = num.intValue();
                int ai = addedIndex.intValue();
                Integer id = ID.sample;           // sample number
                for (int n = 0; n < tot; n++) {
                    remote.getPresetContext().cmd_newVoice(preset);
                    pl.selVoice(preset, IntPool.get(ai + n));
                    pl.add(id, sampleNums[n]);
                }
                pl.dispatch();
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return addedIndex;
    }

    public Integer newZones(Integer preset, Integer voice, Integer num) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyZonesException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        Integer addedIndex = IntPool.get(0);
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                addedIndex = p.getVoice(voice).addZones(num);
                //ParameterEditLoader pl = remote.getEditLoader();
                int tot = num.intValue();
                // int ai = addedIndex.intValue();
                //Integer id = ID.sample;           // sample number
                for (int n = 0; n < tot; n++) {
                    remote.getVoiceContext().cmd_newZone(preset, voice);
                    //pl.selZone(preset, voice, IntPool.get(ai + n));
                    //pl.addDesktopElement(id, sampleNums[n]);
                }
                //pl.dispatch();
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return addedIndex;
    }

    public void rmvLinks(Integer preset, Integer[] links) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Arrays.sort(links);
                for (int i = links.length - 1; i >= 0; i--) {
                    p.rmvLink(links[i]);
                    remote.getLinkContext().cmd_delete(preset, links[i]);
                }
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public int numLinks(Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return presetContext.numLinks(preset);
    }

    public void rmvVoices(Integer preset, Integer[] voices) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException, CannotRemoveLastVoiceException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                ///////////////////////////////////////////////////////
                // THIS IS A HACK TO COMPENSATE FOR A SYSEX BUG!!
                ///////////////////////////////////////////////////////
                // last to first
                for (int i = p.numLinks() - 1; i >= 0; i--)
                    remote.getLinkContext().cmd_delete(preset, IntPool.get(i));
                ///////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////

                try {
                    Arrays.sort(voices);
                    for (int i = voices.length - 1; i >= 0; i--) {
                        p.rmvVoice(voices[i]);
                        remote.getVoiceContext().cmd_delete(preset, voices[i]);
                    }
                } finally {
                    ///////////////////////////////////////////////////////
                    // THIS IS A HACK TO COMPENSATE FOR A SYSEX BUG!!
                    ///////////////////////////////////////////////////////
                    if (p.numLinks() > 0) {
                        ParameterEditLoader pl = remote.getEditLoader();
                        Integer[] values;
                        Set s = presetContext.device.getDeviceParameterContext().getLinkContext().getIds();
                        Integer[] linkIds = new Integer[s.size()];
                        s.toArray(linkIds);
                        for (int i = 0,n = p.numLinks(); i < n; i++) {
                            pl.selLink(preset, IntPool.get(i));
                            try {
                                values = p.getLink(IntPool.get(i)).getValues(linkIds);
                            } catch (IllegalParameterIdException e) {
                                // critical
                                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                                return;
                            } catch (NoSuchLinkException e) {
                                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                                return;
                            }
                            remote.getPresetContext().cmd_newLink(preset);
                            for (int j = 0, k = linkIds.length; j < k; j++)
                                pl.add(linkIds[j], values[j]);
                            pl.dispatch();
                            pl.reset();
                        }
                    }
                    ///////////////////////////////////////////////////////
                    ///////////////////////////////////////////////////////
                }

            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public void rmvZones(Integer preset, Integer voice, Integer[] zones) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                Arrays.sort(zones);
                for (int i = zones.length - 1; i >= 0; i--) {
                    p.getVoice(voice).rmvZone(zones[i]);
                    remote.getZoneContext().cmd_delete(preset, voice, zones[i]);
                }
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
    }

    public int numZones(Integer preset, Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchContextException {
        return presetContext.numZones(preset, voice);
    }

    public Integer[] getZoneParams(Integer preset, Integer voice, Integer zone, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextException {
        return presetContext.getZoneParams(preset, voice, zone, ids);
    }

    public Integer[] setLinksParam(Integer preset, Integer[] links, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        Integer[] changed = new Integer[0];
        try {
            if (links.length != values.length)
                throw new IllegalArgumentException(this.getClass().toString() + ":setLinksParam -> mismatch between number of links and number of parameter2 values!");
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selPreset(preset);
                for (int n = 0; n < links.length; n++) {
                    changed = presetContext.setLinkValue(p.getLink(links[n]), id, values[n]);
                    pl.selLink(preset, links[n]);
                    pl.add(changed, p.getLink(links[n]).getValues(changed));
                }
                pl.dispatch();
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
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
        return presetContext.getIsolatedZone(preset, voice, zone);
    }

    // ZONE
    public Integer newZone(Integer preset, Integer voice, IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws PresetEmptyException, NoSuchContextException, NoSuchPresetException, NoSuchVoiceException, TooManyZonesException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        Integer addedIndex = IntPool.get(0);
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                addedIndex = p.getVoice(voice).addZone(iz);
                remote.getVoiceContext().cmd_newZone(preset, voice);
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selZone(preset, voice, addedIndex);
                ZoneObject z = p.getVoice(voice).getZone(addedIndex);
                pl.add(z.getAllIds(), z.getAllValues());
                pl.dispatch();
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (NoSuchZoneException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return addedIndex;
    }

    public void setPresetName(Integer preset, String name) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            reader.setPresetName(getDelegatingPresetContext(), preset, name);
            remote.getPresetContext().edit_name(preset, name);
        } catch (RemoteUnreachableException e) {
            reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            presetContext.device.logCommError(e);
        } catch (RemoteMessagingException e) {
            reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
            presetContext.device.logCommError(e);
        } finally {
            reader.release();
        }
    }

    private Integer[] fxaIds = new Integer[]{IntPool.get(7), IntPool.get(8), IntPool.get(9), IntPool.get(10), IntPool.get(11), IntPool.get(12), IntPool.get(13)};
    private Integer[] fxbIds = new Integer[]{IntPool.get(15), IntPool.get(16), IntPool.get(17), IntPool.get(18), IntPool.get(19), IntPool.get(20), IntPool.get(21)};

    public Integer[] setPresetParams(Integer preset, Integer[] ids, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchContextException, ParameterValueOutOfRangeException {
        // fxA = id6 (6-13) & fxB = id14 (14-21)
        boolean fxaChanged = false;
        boolean fxbChanged = false;

        for (int i = 0,j = ids.length; i < j; i++)
            if (ids[i].equals(IntPool.get(6)))
                fxaChanged = true;
            else if (ids[i].equals(IntPool.get(14)))
                fxbChanged = true;

        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                p.setValues(ids, values);
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selPreset(preset);
                pl.add(ids, values);
                pl.dispatch();

                Integer[] vals;
                if (fxaChanged) {
                    vals = remote.getParameterContext().req_prmValues(fxaIds);
                    p.setValues(fxaIds, ZUtilities.extractOneOfIntegerPairs(vals, false));
                }
                if (fxbChanged) {
                    vals = remote.getParameterContext().req_prmValues(fxbIds);
                    p.setValues(fxbIds, ZUtilities.extractOneOfIntegerPairs(vals, false));
                }
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteDeviceDidNotRespondException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return ids;
    }

    //private Integer[] filterSubIds = new Integer[]{IntPool.get(83), IntPool.get(84), IntPool.get(87), IntPool.get(88), IntPool.get(89), IntPool.get(90), IntPool.get(91), IntPool.get(92)};

    protected Integer[] setGroupParamHelper(Integer preset, Integer[] updateVoices, Integer group, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();

        int len = updateVoices.length;
        Integer[] changed = new Integer[0];
        try {
            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                for (int n = 0; n < len; n++) {
                    changed = presetContext.setVoiceValue(p.getVoice(updateVoices[n]), id, value);
                    //p.getVoice(updateVoices[n]).setValues(new Integer[]{id}, new Integer[]{finalValue});
                }
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selGroup(preset, group);
                pl.add(changed, p.getVoice(updateVoices[0]).getValues(changed));
                pl.dispatch();

            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return changed;
    }

    public Integer[] setVoicesParam(Integer preset, Integer[] voices, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();

        Integer[] changedIds = new Integer[0];
        try {
            if (voices.length != values.length)
                throw new IllegalArgumentException(this.getClass().toString() + ":setVoicesParam -> mismatch between number of voices and number of parameter2 values!");

            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                ParameterEditLoader pl = remote.getEditLoader();
                for (int n = 0; n < voices.length; n++) {
                    changedIds = presetContext.setVoiceValue(p.getVoice(voices[n]), id, values[n]);
                    pl.selVoice(preset, voices[n]);
                    pl.add(changedIds, p.getVoice(voices[n]).getValues(changedIds));
                }
                pl.dispatch();
            } catch (RemoteUnreachableException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                presetContext.device.logCommError(e);
            } /*catch (RemoteDeviceDidNotRespondException e) {
                reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                logCommError(e);
            } */ finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return changedIds;
    }

    public Integer[] setZonesParam(Integer preset, Integer voice, Integer[] zones, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextException, ParameterValueOutOfRangeException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        int len = zones.length;
        Integer[] changedIds = new Integer[0];
        try {
            if (zones.length != values.length)
                throw new IllegalArgumentException(this.getClass().toString() + ":setZonesParam -> mismatch between number of zones and number of parameter2 values!");

            PresetObject p = reader.getPresetWrite(getDelegatingPresetContext(), preset);
            try {
                ParameterEditLoader pl = remote.getEditLoader();
                pl.selVoice(preset, voice);
                VoiceObject v = p.getVoice(voice);
                for (int n = 0; n < len; n++) {
                    changedIds = presetContext.setZoneValue(v.getZone(zones[n]), id, values[n]);
                    pl.selZone(preset, voice, zones[n]);
                    pl.add(changedIds, v.getZone(zones[n]).getValues(changedIds));
                }
                try {
                    pl.dispatch();
                } catch (Exception e) {
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                    presetContext.device.logCommError(e);
                }
            } finally {
                reader.unlockPreset(preset);
            }
        } finally {
            reader.release();
        }
        return changedIds;
    }

/*        public void setDiversePresetParams(PresetContext.AbstractPresetParameterProfile[] paramProfiles) throws NoSuchContextException, NoSuchPresetException, ParameterValueOutOfRangeException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchLinkException, NoSuchZoneException, NoSuchGroupException {
            PDBReader reader = ipc.pdbp.getDBRead();
            HashSet presets = new HashSet();
            try {
                ipc.setDiversePresetParams(paramProfiles);
                ParameterEditLoader pl = remote.getEditLoader();
                for (int i = 0; i < paramProfiles.length; i++) {
                    if (paramProfiles[i] instanceof ZoneParameterProfile) {
                        ZoneParameterProfile p = (ZoneParameterProfile) paramProfiles[i];
                        Integer[] ids = p.getIds();
                        Integer[] values = new Integer[ids.length];
                        presets.addDesktopElement(p.getPreset());
                        for (int j = 0; j < ids.length; j++)
                            values[j] = getZoneParams(p.getPreset(), p.getVoice(), p.getZone(), new Integer[]{ids[j]})[0];
                        pl.selZone(p.getPreset(), p.getVoice(), p.getZone());
                        pl.addDesktopElement(ids, values);
                    } else if (paramProfiles[i] instanceof VoiceParameterProfile) {
                        VoiceParameterProfile p = (VoiceParameterProfile) paramProfiles[i];
                        Integer[] ids = p.getIds();
                        Integer[] values = new Integer[ids.length];
                        presets.addDesktopElement(p.getPreset());
                        for (int j = 0; j < ids.length; j++)
                            values[j] = getVoiceParams(p.getPreset(), p.getVoice(), new Integer[]{ids[j]})[0];
                        pl.selVoice(p.getPreset(), p.getVoice());
                        pl.addDesktopElement(ids, values);
                    } else if (paramProfiles[i] instanceof GroupParameterProfile) {
                        GroupParameterProfile p = (GroupParameterProfile) paramProfiles[i];
                        Integer[] ids = p.getIds();
                        Integer[] values = new Integer[ids.length];
                        for (int j = 0; j < ids.length; j++)
                            values[j] = getGroupParams(p.getPreset(), p.getGroup(), new Integer[]{ids[j]})[0];
                        pl.selGroup(p.getPreset(), p.getGroup());
                        pl.addDesktopElement(ids, values);
                    } else if (paramProfiles[i] instanceof LinkParameterProfile) {
                        LinkParameterProfile p = (LinkParameterProfile) paramProfiles[i];
                        Integer[] ids = p.getIds();
                        Integer[] values = new Integer[ids.length];
                        presets.addDesktopElement(p.getPreset());
                        for (int j = 0; j < ids.length; j++)
                            values[j] = getLinkParams(p.getPreset(), p.getLink(), new Integer[]{ids[j]})[0];
                        pl.selLink(p.getPreset(), p.getLink());
                        pl.addDesktopElement(ids, values);
                    } else if (paramProfiles[i] instanceof PresetParameterProfile) {
                        PresetParameterProfile p = (PresetParameterProfile) paramProfiles[i];
                        Integer[] ids = p.getIds();
                        Integer[] values = new Integer[ids.length];
                        presets.addDesktopElement(p.getPreset());
                        for (int j = 0; j < ids.length; j++)
                            values[j] = getPresetParams(p.getPreset(), new Integer[]{ids[j]})[0];
                        pl.selPreset(p.getPreset());
                        pl.addDesktopElement(ids, values);
                    } else
                        throw new IllegalArgumentException("unknown preset parameter profile");
                }
                pl.dispatch();
            } catch (RemoteUnreachableException e) {
                for (Iterator i = presets.iterator(); i.hasNext();) {
                    Integer preset = (Integer) i.next();
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                }
                logCommError(e);
            } catch (RemoteMessagingException e) {
                for (Iterator i = presets.iterator(); i.hasNext();) {
                    Integer preset = (Integer) i.next();
                    reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
                }
                logCommError(e);
            } finally {
                reader.release();
            }
        }
  */

    public void setDiversePresetParams(PresetContext.AbstractPresetParameterProfile[] paramProfiles) throws NoSuchPresetException, NoSuchContextException {
        PDBReader reader = presetContext.presetDatabaseProxy.getDBRead();
        HashSet presets = new HashSet();
        Integer[] changed;
        try {
            ParameterEditLoader pl = remote.getEditLoader();
            for (int i = 0; i < paramProfiles.length; i++) {
                if (paramProfiles[i] instanceof PresetParameterProfile)
                    presets.add(((PresetParameterProfile) paramProfiles[i]).getPreset());

                if (paramProfiles[i] instanceof ZoneParameterProfile) {
                    ZoneParameterProfile p = (ZoneParameterProfile) paramProfiles[i];
                    changed = presetContext.setZonesParam(p.getPreset(), p.getVoice(), new Integer[]{p.getZone()}, p.getId(), new Integer[]{p.getValue()});
                    pl.selZone(p.getPreset(), p.getVoice(), p.getZone());
                    pl.add(changed, presetContext.getZoneParams(p.getPreset(), p.getVoice(), p.getZone(), changed));
                } else if (paramProfiles[i] instanceof VoiceParameterProfile) {
                    VoiceParameterProfile p = (VoiceParameterProfile) paramProfiles[i];
                    changed = presetContext.setVoicesParam(p.getPreset(), new Integer[]{p.getVoice()}, p.getId(), new Integer[]{p.getValue()});
                    pl.selVoice(p.getPreset(), p.getVoice());
                    pl.add(changed, presetContext.getVoiceParams(p.getPreset(), p.getVoice(), changed));
                } else if (paramProfiles[i] instanceof GroupParameterProfile) {
                    GroupParameterProfile p = (GroupParameterProfile) paramProfiles[i];
                    changed = presetContext.setGroupParam(p.getPreset(), p.getGroup(), p.getId(), p.getValue());
                    pl.selGroup(p.getPreset(), p.getGroup());
                    pl.add(changed, presetContext.getGroupParams(p.getPreset(), p.getGroup(), changed));
                } else if (paramProfiles[i] instanceof LinkParameterProfile) {
                    LinkParameterProfile p = (LinkParameterProfile) paramProfiles[i];
                    changed = presetContext.setLinksParam(p.getPreset(), new Integer[]{p.getLink()}, p.getId(), new Integer[]{p.getValue()});
                    pl.selLink(p.getPreset(), p.getLink());
                    pl.add(changed, presetContext.getLinkParams(p.getPreset(), p.getLink(), changed));
                } else if (paramProfiles[i] instanceof PresetParameterProfile) {
                    PresetParameterProfile p = (PresetParameterProfile) paramProfiles[i];
                    changed = presetContext.setPresetParams(p.getPreset(), new Integer[]{p.getId()}, new Integer[]{p.getValue()});
                    pl.selPreset(p.getPreset());
                    pl.add(changed, presetContext.getPresetParams(p.getPreset(), changed));
                } else
                    throw new IllegalArgumentException("unknown preset parameter profile");
            }
            pl.dispatch();
        } catch (RemoteUnreachableException e) {
            setUninit(presets, reader);
            presetContext.device.logCommError(e);
        } catch (RemoteMessagingException e) {
            setUninit(presets, reader);
            presetContext.device.logCommError(e);
        } catch (NoSuchPresetException e) {
            setUninit(presets, reader);
        } catch (PresetEmptyException e) {
            setUninit(presets, reader);
        } catch (IllegalParameterIdException e) {
            setUninit(presets, reader);
        } catch (NoSuchVoiceException e) {
            setUninit(presets, reader);
        } catch (NoSuchZoneException e) {
            setUninit(presets, reader);
        } catch (NoSuchContextException e) {
            setUninit(presets, reader);
        } catch (ParameterValueOutOfRangeException e) {
            setUninit(presets, reader);
        } catch (NoSuchGroupException e) {
            setUninit(presets, reader);
        } catch (NoSuchLinkException e) {
            setUninit(presets, reader);
        } finally {
            reader.release();
        }
    }

    private void setUninit(Set presets, PDBReader reader) throws NoSuchPresetException, NoSuchContextException {
        for (Iterator i = presets.iterator(); i.hasNext();) {
            Integer preset = (Integer) i.next();
            reader.changePresetObject(getDelegatingPresetContext(), preset, new UninitPresetObject(preset));
        }
    }

    public PresetContext getDelegatingPresetContext() {
        return this;
    }

    public String getDeviceString
            () {
        return remote.toString();
    }

    public DeviceParameterContext getDeviceParameterContext() {
        return presetContext.device.getDeviceParameterContext();
    }

    public DeviceContext getDeviceContext() {
        return presetContext.device;
    }

    public SampleContext getRootSampleContext() {
        return presetContext.getRootSampleContext();
    }

    // EVENTS
    public void addPresetContextListener(PresetContextListener pl) {
        presetContext.addPresetContextListener(pl);
    }

    public void removePresetContextListener(PresetContextListener pl) {
        presetContext.removePresetContextListener(pl);
    }

    public void addPresetListener(PresetListener pl, Integer[] presets) {
        presetContext.addPresetListener(pl, presets);
    }

    public void removePresetListener(PresetListener pl, Integer[] presets) {
        presetContext.removePresetListener(pl, presets);
    }

    // PRESET COLLECTION
    // returns Set of Integer
    public Set getPresetIndexesInContext() throws NoSuchContextException {
        return presetContext.getPresetIndexesInContext();
    }

    // returns List of ContextReadablePreset/ReadablePreset ( e.g FLASH/ROM samples returned as ReadablePreset)
    public List getContextPresets() throws NoSuchContextException {
        return presetContext.getContextPresets();
    }

    // returns List of ReadablePreset   or better ( e.g FLASH/ROM and out of context samples returned as ReadablePreset)
    public List getDatabasePresets() throws NoSuchContextException {
        return presetContext.getDatabasePresets();
    }

    // set of integers
    public Set getDatabaseIndexes() throws NoSuchContextException {
        return presetContext.getDatabaseIndexes();
    }

    // returns Map of Integer -> String
    public Map getPresetNamesInContext() throws NoSuchContextException {
        return presetContext.getPresetNamesInContext();
    }

    public boolean isPresetInContext(Integer preset) {
        return presetContext.isPresetInContext(preset);
    }

    public int size() {
        return presetContext.size();
    }

    public boolean isPresetEmpty(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        return presetContext.isPresetEmpty(preset);
    }

    public void expandContext(PresetContext pc, Integer[] presets) throws NoSuchContextException, NoSuchPresetException {
        presetContext.expandContext(pc, presets);
    }

    public List expandContextWithEmptyPresets(PresetContext pc, Integer reqd) throws NoSuchContextException {
        return presetContext.expandContextWithEmptyPresets(pc, reqd);
    }

    public int numEmpties(Integer[] presets) throws NoSuchPresetException, NoSuchContextException {
        return presetContext.numEmpties(presets);
    }

    public int numEmpties(Integer lowPreset, int num) throws NoSuchPresetException, NoSuchContextException {
        return presetContext.numEmpties(lowPreset, num);
    }

    public List findEmptyPresetsInContext(Integer reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException {
        return presetContext.findEmptyPresetsInContext(reqd, beginIndex, maxIndex);
    }

    public Integer firstEmptyPresetInContext() throws NoSuchContextException, NoSuchPresetException {
        return presetContext.firstEmptyPresetInContext();
    }

    public Integer firstEmptyPresetInDatabaseRange(Integer lowPreset, Integer highPreset) throws NoSuchContextException, NoSuchPresetException {
        return presetContext.firstEmptyPresetInDatabaseRange(lowPreset, highPreset);
    }

    public PresetContext newContext(String name, Integer[] presets) throws NoSuchPresetException, NoSuchContextException {
        return presetContext.newContext(name, presets);
    }

    public void release() throws NoSuchContextException {
        presetContext.release();
    }

    // PRESET
    // value between 0 and 1 representing fraction of dump completed
    // value < 0 means no dump in progress
    public IntegerUseMap getSampleIndexesInUseForUserPresets() throws NoSuchContextException {
        return presetContext.getSampleIndexesInUseForUserPresets();
    }

    public IntegerUseMap getSampleIndexesInUseForFlashPresets() throws NoSuchContextException {
        return presetContext.getSampleIndexesInUseForFlashPresets();
    }

    public IntegerUseMap getSampleIndexesInUseForAllPresets() throws NoSuchContextException {
        return presetContext.getSampleIndexesInUseForAllPresets();
    }

    public double getInitializationStatus(Integer preset) throws NoSuchPresetException, NoSuchContextException {
        return presetContext.getInitializationStatus(preset);
    }
}
