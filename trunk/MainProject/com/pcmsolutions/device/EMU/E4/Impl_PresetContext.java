package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.requests.PresetRequestEvent;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMultimodeChannelException;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.database.*;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.tasking.*;
import com.pcmsolutions.system.threads.ZThread;
import com.pcmsolutions.util.IntegerUseMap;

import javax.sound.midi.Sequence;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 13:58:44
 */
class Impl_PresetContext extends AbstractContext<ReadablePreset, DatabasePreset, IsolatedPreset, PresetContext, PresetEvent, PresetRequestEvent, PresetListener> implements PresetContext, Serializable {
    protected E4Device device;
    protected String name;
    protected PresetDatabase db;
    protected transient ManageableTicketedQ presetContextQ;

    public Impl_PresetContext(E4Device device, String name, PresetDatabase db) {
        super(db);
        this.device = device;
        this.name = name;
        this.db = db;
        buildTransients();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        buildTransients();
    }

    void buildTransients() {
        presetContextQ = QueueFactory.createTicketedQueue(this, "presetContext", 6);
        presetContextQ.start();
    }

    public TicketedQ getContextQ() {
        return presetContextQ;
    }

    public boolean equals(Object o) {
        // identity comparison
        if (o == this)
            return true;
        return false;
    }

    public String toString() {
        return "Presets";
    }

    public Ticket assertRemote(final Integer preset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                try {
                    task_dropContent(Impl_PresetContext.this.getIsolatedPreset(preset, false), preset, getName(preset), ProgressCallback.DUMMY);
                } catch (EmptyException e) {
                    erase(preset).post();
                }
            }
        }, "assertRemote");
    }

    public String getPresetSummary(Integer preset) throws DeviceException, NoSuchContextIndexException {
        db.access();
        try {
            return db.tryGetPresetSummary(this, preset);
        } finally {
            db.release();
        }
    }

    public Ticket newContent(final Integer index, final String name) {
        return device.queues.presetContextQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_newContent(index, name);
            }
        }, "newContent");
    }

    void task_newContent(final Integer index, final String name) throws DeviceException, ContentUnavailableException {
        db.access();
        try {
            db.newContent(Impl_PresetContext.this, index, (name == null ? DeviceContext.UNTITLED_PRESET : name), null);
        } finally {
            db.release();
        }
    }

    public final boolean newIfEmpty(Integer index, String name) throws DeviceException, ContentUnavailableException {
        db.access();
        try {
            if (db.isEmpty(index)) {
                db.newContent(this, index, name, null);
                return true;
            } else
                return false;
        } finally {
            db.release();
        }
    }

    public final boolean newIfReallyEmpty(Integer index, String name) throws DeviceException, ContentUnavailableException, NoSuchContextException, ContentUnavailableException {
        db.access();
        try {
            task_refreshIfEmpty(index);
            return false;
        } catch (EmptyException e) {
            db.newContent(this, index, name, null);
        } finally {
            db.release();
        }
        return true;
    }

    public Set getPresetsDeepSet(Integer[] presets) throws DeviceException, ContentUnavailableException {
        Set s = new TreeSet();
        for (int i = 0; i < presets.length; i++)
            s.addAll(getPresetDeepSet(presets[i]));

        return s;
    }

    public Set<Integer> getPresetDeepSet(Integer preset) throws DeviceException, ContentUnavailableException {
        db.access();
        try {
            return taskGetPresetSet(preset, new HashSet<Integer>());
        } finally {
            db.release();
        }
    }

    private Set<Integer> taskGetPresetSet(Integer preset, Set<Integer> handledPresets) throws DeviceException, ContentUnavailableException {
        if (Thread.currentThread() instanceof ZThread && !((ZThread) Thread.currentThread()).isShouldRun())
            return handledPresets;
        try {
            assertInitialized(preset, false).send(0);
        } catch (Exception e) {
            throw new ContentUnavailableException(e.getMessage());
        }
        handledPresets.add(preset);
        db.access();
        Set<Integer> s;
        try {
            DatabasePreset pobj = db.getRead(this, preset);
            try {
                s = pobj.referencedPresetSet();
            } finally {
                db.releaseReadContent(preset);
            }
            s.removeAll((Set) handledPresets);
            handledPresets.addAll(s);

            for (Iterator i = s.iterator(); i.hasNext();)
                taskGetPresetSet((Integer) i.next(), handledPresets);

        } catch (EmptyException e) {
        } finally {
            db.release();
        }
        return handledPresets;
    }

    // PRESET
    // value between 0 and 1 representing fraction of dump completed
    // value < 0 means no dump in progress
    public double getInitializationStatus(Integer preset) throws DeviceException {
        db.access();
        try {
            return db.getInitializationStatus(this, preset);
        } finally {
            db.release();
        }
    }

    // AUDITION
    private static String SAMPLE_AUDITION_PRESET_NAME = "Z_AUD_SAMPLE";
    private static String VOICE_AUDITION_PRESET_NAME = "Z_AUD_VOICE";

    void checkAuditionEnabled() throws AuditioningDisabledException {
        if (!device.getDevicePreferences().ZPREF_enableAuditioning.getValue())
            throw new AuditioningDisabledException();
    }

    public Ticket auditionVoices(final Integer preset, final Integer[] voices, final boolean stepped) {
        return device.queues.auditionQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                try {
                    checkAuditionEnabled();
                    DeviceContext dc = getDeviceContext();
                    doVoiceAudition(preset, voices, IntPool.get(dc.getDevicePreferences().ZPREF_sampleAuditionPreset.getValue()), stepped);
                } catch (Exception e) {
                }
            }
        }, "auditionVoices");
    }

    // TODO!! this needs to be fixed up after we setup sample DB and throw exceptions from device locks on device not running conditions
    private void doVoiceAudition(Integer srcPreset, Integer[] voices, Integer audPreset, boolean consecutive) throws PresetException, DeviceException, AuditionManager.MultimodeChannelUnreachableException, IllegalMultimodeChannelException, AuditioningException, ContentUnavailableException, EmptyException, ParameterException, ResourceUnavailableException {
        int note = NoteUtilities.Note.getValueForString(getDeviceContext().getDevicePreferences().ZPREF_quickAuditionNote.getValue());
        db.access();
        try {
            final boolean ignoreLatch = getDeviceContext().getDevicePreferences().ZPREF_disableLatchDuringVoiceAudition.getValue();
            RCContent<DatabasePreset> rcc = db.getRC(this, srcPreset, audPreset);
            try {
                if (consecutive) {
                    task_newContent(audPreset, VOICE_AUDITION_PRESET_NAME);
                    for (int i = 0; i < voices.length; i++) {
                        int origKey = rcc.getReadable().getVoice(voices[i]).getValue(ID.origKey).intValue();
                        int lowKey = rcc.getReadable().getVoice(voices[i]).getValue(ID.lowKey).intValue();
                        int highKey = rcc.getReadable().getVoice(voices[i]).getValue(ID.highKey).intValue();

                        if (ZUtilities.inRange(origKey, lowKey, highKey))
                            note = origKey;
                        else
                            note = ZUtilities.constrain(note, lowKey, highKey);
                        int nv = numVoices(audPreset);
                        if (nv > 1)
                            task_rmvVoices(audPreset, new Integer[]{IntPool.get(nv - 1)});
                        task_copyVoice(srcPreset, voices[i], audPreset);
                        task_setVoiceParam(audPreset, IntPool.get(numVoices(audPreset) - 1), ID.delay, IntPool.zero);
                        if (ignoreLatch)
                            task_setVoiceParam(audPreset, IntPool.get(numVoices(audPreset) - 1), ID.latch, IntPool.zero);
                        performPresetAudition(audPreset, note);
                    }
                } else {
                    task_newContent(audPreset, VOICE_AUDITION_PRESET_NAME);
                    Integer[] lh = PresetContextMacros.getCommonKeyRange(this, srcPreset, voices);
                    if (lh != null)
                        note = ZUtilities.constrain(note, lh[0].intValue(), lh[1].intValue());
                    else
                        throw new AuditioningException("Voices have no common keyboard range");

                    final boolean truncateDelay = getDeviceContext().getDevicePreferences().ZPREF_truncateDelayDuringVoiceAudition.getValue();
                    for (int i = 0; i < voices.length; i++) {
                        task_copyVoice(srcPreset, voices[i], audPreset);
                        if (truncateDelay)
                            task_setVoiceParam(audPreset, IntPool.get(numVoices(audPreset) - 1), ID.delay, IntPool.zero);
                        if (ignoreLatch)
                            task_setVoiceParam(audPreset, IntPool.get(numVoices(audPreset) - 1), ID.latch, IntPool.zero);
                    }
                    performPresetAudition(audPreset, note, Math.min(voices.length * 20, 2000));
                }
            } finally {
                rcc.release();
            }
        } finally {
            db.release();
        }
    }

    public Ticket auditionSamples(final Integer[] samples, final boolean consecutive) {
        return device.queues.auditionQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                try {
                    checkAuditionEnabled();
                    DeviceContext dc = getDeviceContext();
                    Integer audPreset = IntPool.get(dc.getDevicePreferences().ZPREF_sampleAuditionPreset.getValue());
                    doSampleAudition(samples, audPreset, consecutive);
                } catch (Exception e) {
                }
            }
        }, "auditionSamples");
    }

    private void doSampleAudition(Integer[] samples, Integer audPreset, boolean consecutive) throws DeviceException, AuditionManager.MultimodeChannelUnreachableException, IllegalMultimodeChannelException, AuditioningException, ContentUnavailableException, ParameterValueOutOfRangeException, NoSuchVoiceException, IllegalParameterIdException, EmptyException, ResourceUnavailableException, TooManyZonesException, NoSuchZoneException {
        db.access();
        try {
            if (consecutive) {
                task_newContent(audPreset, SAMPLE_AUDITION_PRESET_NAME);
                for (int i = 0; i < samples.length; i++) {
                    task_setVoiceParam(audPreset, IntPool.zero, ID.sample, samples[i]);
                    performPresetAudition(audPreset);
                }
            } else {
                DatabasePreset p = db.getFreePreset();
                try {
                    int index = 0;
                    DatabaseVoice v = p.getVoice(IntPool.zero);
                    for (Integer s : samples) {
                        v.newZone();
                        v.getZone(index++).setValue(ID.sample, s);
                    }
                    task_dropContent(p.getIsolated(), audPreset, SAMPLE_AUDITION_PRESET_NAME, ProgressCallback.DUMMY);
                } finally {
                }
                performPresetAudition(audPreset, 150);
            }
        } finally {
            db.release();
        }
    }

    public Ticket auditionPreset(final Integer preset) {
        return device.queues.auditionQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                try {
                    checkAuditionEnabled();
                    performPresetAudition(preset);
                } catch (Exception e) {
                }
            }
        }, "auditionPreset");
    }

    void performPresetAudition(Integer preset) throws AuditionManager.MultimodeChannelUnreachableException, DeviceException, AuditioningException, ResourceUnavailableException {
        performPresetAudition(preset, 50);
    }

    void performPresetAudition(Integer preset, long sleep) throws AuditionManager.MultimodeChannelUnreachableException, DeviceException, AuditioningException, ResourceUnavailableException {
        performPresetAudition(preset, NoteUtilities.Note.getValueForString(getDeviceContext().getDevicePreferences().ZPREF_quickAuditionNote.getValue()), sleep);
    }

    void performPresetAudition(Integer preset, Sequence seq, float bpm) throws AuditionManager.MultimodeChannelUnreachableException, DeviceException, AuditioningException, ResourceUnavailableException {
        DeviceContext dc = getDeviceContext();
        Integer audChnl = IntPool.get(dc.getDevicePreferences().ZPREF_auditionChnl.getValue());
        Ticket as = dc.getAuditionManager().playSequence(seq, (audChnl.intValue() > 16 ? false : true), bpm);
        finalizePresetAudition(preset, 50);
        try {
            as.send(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Ticket getDefaultNoteTicket(int note) throws DeviceException {
        DeviceContext dc = getDeviceContext();
        Integer audChnl = IntPool.get(dc.getDevicePreferences().ZPREF_auditionChnl.getValue());
        int gate = dc.getDevicePreferences().ZPREF_quickAuditionGate.getValue();
        int vel = dc.getDevicePreferences().ZPREF_quickAuditionVel.getValue();
        return dc.getAuditionManager().getNote(note, audChnl.intValue(), vel, gate);
    }

    Ticket getDefaultNoteTicket() throws DeviceException {
        return getDefaultNoteTicket(NoteUtilities.Note.getValueForString(getDeviceContext().getDevicePreferences().ZPREF_quickAuditionNote.getValue()));
    }

    void performPresetAudition(Integer preset, int note, long sleep) throws AuditionManager.MultimodeChannelUnreachableException, DeviceException, AuditioningException, ResourceUnavailableException {
        Ticket noteTicket = getDefaultNoteTicket(note);
        finalizePresetAudition(preset, sleep);
        try {
            noteTicket.send(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finalizePresetAudition(Integer preset, long sleep) throws DeviceException {
        DeviceContext dc = getDeviceContext();
        Integer audChnl = IntPool.get(dc.getDevicePreferences().ZPREF_auditionChnl.getValue());
        int vol = dc.getDevicePreferences().ZPREF_auditionChnlVol.getValue();
        try {
            dc.getMultiModeContext().setPreset(audChnl, preset).send(250);
            dc.getMultiModeContext().setVolume(audChnl, IntPool.get(vol)).send(250);
        } catch (Exception e) {
            throw new DeviceException(e.getMessage());
        }

        db.getEventHandler().sync();
        dc.getMultiModeContext().syncToEdits();

        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //if (dc.getDevicePreferences().ZPREF_allNotesOffBetweenAuditions.getValue())
        //    dc.getAuditionManager().allNotesOff(audChnl.intValue());

        /*
        try {
                dc.sampleMemoryDefrag(false).post();
            } catch (ResourceUnavailableException e) {
                e.printStackTrace();
            }
            */
    }

    public IsolatedPreset getIsolated(Integer index, Object flags) throws DeviceException, ContentUnavailableException, EmptyException {
        db.access();
        try {
            return db.getIsolatedContent(index, flags);
        } finally {
            db.release();
        }
    }

    public Ticket dropContent(final IsolatedPreset ip, final Integer preset, final String name, final ProgressCallback prog) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_dropContent(ip, preset, name, prog);
            }
        }, "dropContent");
    }

    private void task_dropContent(final IsolatedPreset ip, final Integer preset, final String name, ProgressCallback prog) throws DeviceException, ContentUnavailableException {
        db.access();
        try {
            if (name != null)
                db.dropContent(this, ip, preset, name, prog);
            else
                db.dropContent(this, ip, preset, DeviceContext.UNTITLED_PRESET, prog);
        } finally {
            db.release();
        }
    }

    public Ticket dropContent(final IsolatedPreset ip, final Integer preset, final String name, final Map sampleTranslationMap, final Integer defaultSampleTranslation, final Map linkTranslationMap, final ProgressCallback prog) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    task_dropContent(ip, preset, name, prog);
                    if (linkTranslationMap != null)
                        task_remapLinkIndexes(preset, linkTranslationMap);
                    if (sampleTranslationMap != null)
                        task_remapSampleIndexes(preset, sampleTranslationMap, defaultSampleTranslation);
                } finally {
                    db.release();
                }
            }
        }, "dropContent");
    }

    public Ticket offsetLinkIndexes(final Integer preset, final Integer offset, final boolean user) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.offsetLinkIndexes(offset, user);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "offsetLinkIndexes");
    }

    public Ticket offsetSampleIndexes(final Integer preset, final Integer offset, final boolean user) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.offsetSampleIndexes(offset, user);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "offsetSampleIndexes");
    }

    public Ticket remapLinkIndexes(final Integer preset, final Map translationMap) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_remapLinkIndexes(preset, translationMap);
            }
        }, "remapLinkIndexes");
    }

    private void task_remapLinkIndexes(final Integer preset, final Map translationMap) throws DeviceException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(this, preset);
            try {
                p.remapLinkIndexes(translationMap);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket remapSampleIndexes(final Integer preset, final Map translationMap, final Integer defaultSampleTranslation) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_remapSampleIndexes(preset, translationMap, defaultSampleTranslation);
            }
        }, "remapSampleIndexes");
    }

    private void task_remapSampleIndexes(final Integer preset, final Map translationMap, final Integer defaultSampleTranslation) throws DeviceException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(this, preset);
            try {
                p.remapSampleIndexes(translationMap, defaultSampleTranslation);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket refreshPresetSamples(final Integer preset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_refreshPresetSamples(preset);
            }
        }, "refreshPresetSamples");
    }

    private void task_refreshPresetSamples(final Integer preset) throws DeviceException, EmptyException, ContentUnavailableException, ResourceUnavailableException {
        db.access();
        try {
            DatabasePreset dp = db.getRead(this, preset);
            try {
                for (Integer s : dp.referencedSampleUsage().getIntegers())
                    getRootSampleContext().refresh(s).post();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public IsolatedPreset getIsolatedPreset(Integer preset, boolean refreshSamples) throws DeviceException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            if (refreshSamples)
                try {
                    task_refreshPresetSamples(preset);
                } catch (ResourceUnavailableException e) {
                    throw new DeviceException(e.getMessage());
                }
            //return db.getIsolatedContent(this, preset);
            return db.getIsolatedContent(preset, null);
        } finally {
            db.release();
        }
    }

    public Ticket applySampleToPreset(final Integer preset, final Integer sample, final int mode) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    boolean newed = newIfReallyEmpty(preset, DeviceContext.UNTITLED_PRESET);
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        if (newed) {
                            p.getVoice(IntPool.get(p.numVoices() - 1)).setValue(IntPool.get(38), sample);
                        } else {
                            p.applySingleSample(sample, mode);
                            // TODO !! handle > 1 voice
                            if (mode == PresetContext.MODE_APPLY_SAMPLE_TO_NEW_VOICE && prefs.getBoolean(PREF_autoGroup, true))
                                p.getVoice(IntPool.get(p.numVoices() - 1)).setValue(IntPool.get(37), Impl_PresetContext.this.getNextAvailableGroup(preset, prefs.getBoolean(PREF_autoGroupAtTail, false)));
                        }
                        if (ZPREF_tryMatchAppliedSamples.getValue() && mode == MODE_APPLY_SAMPLE_TO_NEW_VOICE) {
                            // TODO !! handle > 1 voice
                            try {
                                Impl_PresetContext.this.trySetOriginalKeyFromSampleName(preset, IntPool.get(p.numVoices() - 1)).post();
                            } catch (ResourceUnavailableException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (NoSuchVoiceException e) {
                        uninitialize(preset).post();
                        device.logInternalError(e);
                    } catch (IllegalParameterIdException e) {
                        uninitialize(preset).post();
                        device.logInternalError(e);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "applySampleToPreset");
    }

    public Ticket applySamplesToPreset(final Integer preset, final Integer[] samples, final int mode) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    boolean newed = newIfReallyEmpty(preset, DeviceContext.UNTITLED_PRESET);
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.applySamples(samples, mode);
                        if (newed)
                            p.rmvVoices(new Integer[]{IntPool.get(0)});
                        if (mode == PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICE_AND_ZONES) {
                            if (prefs.getBoolean(PREF_autoGroup, true) && !newed)
                                p.getVoice(IntPool.get(p.numVoices() - 1)).setValue(IntPool.get(37), Impl_PresetContext.this.getNextAvailableGroup(preset, prefs.getBoolean(PREF_autoGroupAtTail, false)));
                            if (ZPREF_tryMatchAppliedSamples.getValue()) {
                                DatabaseVoice v = p.getVoice(IntPool.get(p.numVoices() - 1));
                                for (int i = 0, j = v.numZones(); i < j; i++)
                                    Impl_PresetContext.this.trySetOriginalKeyFromSampleName(preset, v.getVoice(), IntPool.get(i)).post();
                            }
                        } else if (mode == PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICES) {
                            if (prefs.getBoolean(PREF_autoGroup, true) && !newed) {
                                Integer ng = Impl_PresetContext.this.getNextAvailableGroup(preset, prefs.getBoolean(PREF_autoGroupAtTail, false));
                                for (int i = 0; i < samples.length; i++) {
                                    p.getVoice(IntPool.get(p.numVoices() - samples.length + i)).setValue(IntPool.get(37), ng);
                                }
                            }
                            if (ZPREF_tryMatchAppliedSamples.getValue()) {
                                try {
                                    for (int i = 0; i < samples.length; i++)
                                        trySetOriginalKeyFromSampleName(preset, IntPool.get(p.numVoices() - samples.length + i)).post();
                                } catch (ResourceUnavailableException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (CannotRemoveLastVoiceException e) {
                        SystemErrors.internal(e);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } catch (NoSuchVoiceException e) {
                    Impl_PresetContext.this.uninitialize(preset).post();
                    device.logInternalError(e);
                } catch (IllegalParameterIdException e) {
                    Impl_PresetContext.this.uninitialize(preset).post();
                    device.logInternalError(e);
                } finally {
                    db.release();
                }
            }
        }, "applySamplesToPreset");
    }

    public ContextEditablePreset[] getEditablePresets() throws DeviceException {
        Set s = getIndexesInContext();
        ArrayList ep = new ArrayList();
        for (Iterator i = s.iterator(); i.hasNext();) {
            Integer p = (Integer) i.next();
            if (p.intValue() < DeviceContext.BASE_ROM_SAMPLE)
                ep.add(new Impl_ContextEditablePreset(this, p));
        }
        return (ContextEditablePreset[]) ep.toArray(new ContextEditablePreset[ep.size()]);
    }


    private Object getMostCapablePresetObject(Integer preset) throws DeviceException, NoSuchContextException {
        PresetModel impl = getPresetImplementation(preset);
        try {
            impl = PresetClassManager.getMostDerivedPresetInstance(impl, getString(preset));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //if (mainView != null)
        //    impl.setPresetEditingMediator(((DeviceInternalFrame) mainView).getDesktopEditingMediator());
        return impl;
    }

    public ReadablePreset getContextItemForIndex(Integer index) throws DeviceException {
        return (ReadablePreset) getMostCapablePresetObject(index);
    }

    // returns List of ContextReadablePreset/ReadablePreset ( e.g FLASH/ROM samples returned as ReadablePreset)
    public List<ReadablePreset> getContextPresets() throws DeviceException {
        ArrayList<ReadablePreset> outList = new ArrayList<ReadablePreset>();
        db.access();
        try {
            Set<Integer> indexes = getIndexesInContext();
            for (Integer p : indexes)
                try {
                    outList.add((ReadablePreset) getMostCapablePresetObject(p));
                } catch (NoSuchContextIndexException e) {
                    e.printStackTrace();
                }
            return outList;
        } finally {
            db.release();
        }
    }

    // returns List of ReadablePreset or better
    // e.g FLASH/ROM and out of context samples returned as ReadablePreset
    // possibly more derived than ReadablePreset if preset is in context etc... )
    public List<ReadablePreset> getDatabasePresets() throws DeviceException {
        ArrayList<ReadablePreset> outList = new ArrayList<ReadablePreset>();
        db.access();
        try {
            Set<Integer> indexes = db.getDBIndexes(this);
            for (Iterator<Integer> i = indexes.iterator(); i.hasNext();)
                try {
                    outList.add(getPresetImplementation(i.next()));
                } catch (NoSuchContextIndexException e) {
                    e.printStackTrace();
                }
            return outList;
        } finally {
            db.release();
        }
    }


    private Impl_ReadablePreset getPresetImplementation(Integer preset) throws DeviceException, NoSuchContextIndexException {
        if (containsIndex(preset))
            if (preset.intValue() <= DeviceContext.MAX_USER_PRESET)
                return new Impl_ContextEditablePreset(this, preset);
            else
                return new Impl_ContextBasicEditablePreset(this, preset);
        else {
            db.access();
            try {
                if (db.readsIndex(this, preset))
                    return new Impl_ReadablePreset(this, preset);
                else
                    throw new NoSuchContextIndexException(preset);
            } finally {
                db.release();
            }
        }
    }

    /*private Impl_ContextReadablePreset getContextPresetImplementation(Integer preset) throws DeviceException {
        if (isPresetInContext(preset))
            return new Impl_ContextReadablePreset(this, preset);

        throw new DeviceException(preset);
    }

    private Impl_ReadablePreset getReadablePresetImplementation(Integer preset) throws DeviceException {
        PDBReader getReader = sdbp.getDBRead();
        try {
            if (getReader.readsPreset(this, preset))
                return new Impl_ReadablePreset(this, preset);
            else
                throw new DeviceException(preset);
        } finally {
            getReader.release();
        }
    } */

    public ContextReadablePreset getContextPreset(Integer preset) throws DeviceException, NoSuchContextException {
        Object rv = getPresetImplementation(preset);
        if (rv instanceof ContextReadablePreset)
            return (ContextReadablePreset) rv;
        else
            throw new NoSuchContextIndexException(preset);
    }

    public ReadablePreset getReadablePreset(Integer preset) throws DeviceException, NoSuchContextException {
        return getPresetImplementation(preset);
    }

    // TODO!! fix semantics of this to handle FLASH samples that cannot be returned as ContextEditablePreset
    /*
    public ContextEditablePreset getEditablePreset(Integer preset) throws DeviceException {
        if (isPresetInContext(preset))
            return new Impl_ContextEditablePreset(this, preset);

        throw new DeviceException(preset);
    }
    */
    public Ticket sortLinks(final Integer preset, final Integer[] ids) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.sortLinks(ids);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "sortLinks");
    }

    public Ticket sortZones(final Integer preset, final Integer[] ids) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.sortZones(ids);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "sortZones");

    }

    // VOICE
    public Ticket splitVoice(final Integer preset, final Integer voice, final int splitKey) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.splitVoice(voice, splitKey);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "splitVoice");
    }

    public Ticket sortVoices(final Integer preset, final Integer[] ids) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.sortVoices(ids);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "sortVoices");
    }

    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer preset, Integer voice) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException, NoSuchVoiceException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.getIsolatedVoice(voice);
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket newVoice(final Integer preset, final IsolatedPreset.IsolatedVoice iv) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    boolean newed = false;
                    newed = newIfReallyEmpty(preset, DeviceContext.UNTITLED_PRESET);
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.dropVoice(iv);
                        if (newed)
                            try {
                                p.rmvVoices(new Integer[]{IntPool.get(0)});
                            } catch (NoSuchVoiceException e) {
                                e.printStackTrace();
                            } catch (CannotRemoveLastVoiceException e) {
                                e.printStackTrace();
                            }
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "newVoice");
    }

    public Ticket combineVoices(final Integer preset, final Integer group) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.combineVoices(group);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "combineVoices");
    }

    public Set getUsedGroupIndexes(Integer preset) throws DeviceException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                HashSet usedGroups = new HashSet();
                for (int i = 0, j = p.numVoices(); i < j; i++)
                    usedGroups.add(p.getVoice(IntPool.get(i)).getValue(IntPool.get(37)));
                return usedGroups;
            } catch (NoSuchVoiceException e) {
            } catch (IllegalParameterIdException e) {
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
        //return new Integer[0];
        return new HashSet();
    }

    public Integer getNextAvailableGroup(Integer preset, boolean atTail) throws DeviceException, EmptyException, ContentUnavailableException {
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

    private interface ZoneMatcher {
        ZoneMatcher ALL = new ZoneMatcher() {
            public boolean isMatch(DatabaseZone z) {
                return true;
            }
        };

        boolean isMatch(DatabaseZone z);
    }

    private interface VoiceMatcher {
        VoiceMatcher ALL = new VoiceMatcher() {
            public boolean isMatch(DatabaseVoice v) {
                return true;
            }
        };

        boolean isMatch(DatabaseVoice v);
    }

    private interface LinkMatcher {
        LinkMatcher ALL = new LinkMatcher() {
            public boolean isMatch(DatabaseLink l) {
                return true;
            }
        };

        boolean isMatch(DatabaseLink l);
    }

    private void task_purgeZones(DatabaseVoice v, ZoneMatcher m) throws NoSuchZoneException {
        for (int x = v.numZones() - 1; x >= 0; x--)
            if (((x == 0 && v.numZones() > 0) || (x > 0)) && m.isMatch(v.getZone(IntPool.get(x))))
                v.rmvZone(IntPool.get(x));
    }

    private void task_purgeVoices(DatabasePreset p, VoiceMatcher m) throws NoSuchVoiceException {
        for (int x = p.numVoices() - 1; x >= 0; x--)
            if (m.isMatch(p.getVoice(IntPool.get(x))))
                try {
                    p.rmvVoices(new Integer[]{IntPool.get(x)});
                } catch (CannotRemoveLastVoiceException e) {
                    e.printStackTrace();
                }
    }

    private void task_purgeLinks(DatabasePreset p, LinkMatcher m) throws NoSuchLinkException {
        for (int x = p.numLinks() - 1; x >= 0; x--)
            if (m.isMatch(p.getLink(IntPool.get(x))))
                p.rmvLink(IntPool.get(x));
    }

    public Ticket purgeZones(final Integer preset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        for (int i = 0, j = p.numVoices(); i < j; i++) {
                            DatabaseVoice v = p.getVoice(IntPool.get(i));
                            task_purgeZones(v, ZoneMatcher.ALL);
                        }
                    } catch (NoSuchVoiceException e) {
                        e.printStackTrace();
                    } catch (NoSuchZoneException e) {
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "purgeZones");
    }

    public Ticket purgeLinks(final Integer preset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        task_purgeLinks(p, LinkMatcher.ALL);
                    } catch (NoSuchLinkException e) {
                        e.printStackTrace();
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "purgeLinks");
    }

    public Ticket purgeEmpties(final Integer preset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        for (int i = p.numVoices() - 1; i >= 0; i--)
                            task_purgeZones(p.getVoice(IntPool.get(i)), new ZoneMatcher() {
                                public boolean isMatch(DatabaseZone z) {
                                    try {
                                        if (Impl_PresetContext.this.getRootSampleContext().isEmpty(z.getSample()))
                                            return true;
                                    } catch (DeviceException e) {
                                        e.printStackTrace();
                                    }
                                    return false;
                                }
                            });
                        task_purgeVoices(p, new VoiceMatcher() {
                            public boolean isMatch(DatabaseVoice v) {
                                try {
                                    if (v.numZones() == 0 && Impl_PresetContext.this.getRootSampleContext().isEmpty(v.getSample()))
                                        return true;
                                } catch (DeviceException e) {
                                    e.printStackTrace();
                                }
                                return false;
                            }
                        });

                    } catch (NoSuchZoneException e) {
                        e.printStackTrace();
                    } catch (NoSuchVoiceException e) {
                        e.printStackTrace();
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "purgeEmpties");

    }

    public Ticket copyLink(final Integer srcPreset, final Integer srcLink, final Integer destPreset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    RWContent<DatabasePreset> rwc = db.getRW(Impl_PresetContext.this, srcPreset, destPreset);
                    try {
                        DatabaseLink l = rwc.getReadable().getLink(srcLink);
                        rwc.getWritable().copyDatabaseLink(l);
                    } finally {
                        rwc.release();
                    }
                } finally {
                    db.release();
                }
            }
        }, "");
    }

    public Ticket copy(final Integer srcPreset, final Integer destPreset, final Map presetLinkTranslationMap) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    if (srcPreset.intValue() == destPreset.intValue())
                        return;
                    db.copyContent(Impl_PresetContext.this, srcPreset, destPreset, null, null);
                    remapLinkIndexes(destPreset, presetLinkTranslationMap).post();
                } finally {
                    db.release();
                }
            }
        }, "copy");
    }

    public Ticket copyVoice(final Integer srcPreset, final Integer srcVoice, final Integer destPreset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_copyVoice(srcPreset, srcVoice, destPreset);
            }
        }, "copyVoice");
    }

    void task_copyVoice(final Integer srcPreset, final Integer srcVoice, final Integer destPreset) throws DeviceException, NoSuchVoiceException, ContentUnavailableException, EmptyException, TooManyVoicesException {
        db.access();
        try {
            RWContent<DatabasePreset> rwc = db.getRW(Impl_PresetContext.this, srcPreset, destPreset);
            try {
                DatabaseVoice v = rwc.getReadable().getVoice(srcVoice);
                rwc.getWritable().copyDatabaseVoice(v);
            } finally {
                rwc.release();
            }
        } finally {
            db.release();
        }
    }

    public Ticket expandVoice(final Integer preset, final Integer voice) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.expandVoice(voice);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "expandVoice");
    }

    public Ticket trySetOriginalKeyFromName(final Integer preset, final Integer voice, final String name) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_trySetOriginalKeyFromName(preset, voice, name);
            }
        }, "trySetOriginalKeyFromName");
    }

    private void task_trySetOriginalKeyFromName(final Integer preset, final Integer voice, final String name) throws ResourceUnavailableException {
        NoteUtilities.Note n = NoteUtilities.getNoteFromName(name);
        if (n != null) {
            setVoiceParam(preset, voice, IntPool.get(44), IntPool.get(n.getNoteValue())).post();
        }
    }

    public Ticket trySetOriginalKeyFromName(final Integer preset, final Integer voice, final Integer zone, final String name) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                NoteUtilities.Note n = NoteUtilities.getNoteFromName(name);
                if (n != null) {
                    setZoneParam(preset, voice, zone, IntPool.get(44), IntPool.get(n.getNoteValue())).post();
                }
            }
        }, "trySetOriginalKeyFromName");
    }

    public Ticket trySetOriginalKeyFromSampleName(final Integer preset, final Integer voice) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    String name = Impl_PresetContext.this.getRootSampleContext().getName(Impl_PresetContext.this.getVoiceParams(preset, voice, new Integer[]{ID.sample})[0]);
                    trySetOriginalKeyFromName(preset, voice, name).post();
                } catch (IllegalParameterIdException e) {
                } finally {
                    db.release();
                }
            }
        }, "trySetOriginalKeyFromSampleName");
    }

    public Ticket trySetOriginalKeyFromSampleName(final Integer preset, final Integer voice, final Integer zone) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                String name = null;
                try {
                    name = Impl_PresetContext.this.getRootSampleContext().getName(Impl_PresetContext.this.getZoneParams(preset, voice, zone, new Integer[]{ID.sample})[0]);
                    trySetOriginalKeyFromName(preset, voice, zone, name).post();
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                } catch (ResourceUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }, "");
    }

    public Integer[] getLinkParams(Integer preset, Integer link, Integer[] ids) throws IllegalParameterIdException, NoSuchLinkException, DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        Integer[] vals;
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                vals = p.getLink(link).getValues(ids);
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }

        return vals;
    }

    public Integer[] getPresetParams(Integer preset, Integer[] ids) throws IllegalParameterIdException, DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        Integer[] vals;
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                vals = p.getValues(ids);
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
        return vals;
    }

    public Integer[] getVoiceIndexesInGroupFromVoice(Integer preset, Integer voice) throws DeviceException, NoSuchVoiceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                DatabaseVoice v = p.getVoice(voice);
                Integer group = v.getValues(new Integer[]{IntPool.get(37)})[0];
                return getVoiceIndexesInGroup(preset, group);
            } catch (NoSuchGroupException e) {
                throw new IllegalStateException("Group missing!");
            } catch (IllegalParameterIdException e) {
                throw new IllegalStateException("Group missing!");

            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Integer[] getVoiceIndexesInGroup(Integer preset, Integer group) throws NoSuchGroupException, DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                Integer[] vi = p.getVoiceIndexesInGroup(group);
                if (vi.length == 0)
                    throw new NoSuchGroupException();
                return vi;
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    // LINK
    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer preset, Integer link) throws NoSuchLinkException, DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.getIsolatedLink(link);
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    // LINK
    public Ticket newLink(final Integer preset, final IsolatedPreset.IsolatedLink il) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                Integer addedIndex;
                try {
                    newIfReallyEmpty(preset, DeviceContext.UNTITLED_PRESET);
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        addedIndex = p.dropLink(il);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "newLink");
    }

    public Integer[] getGroupParams(Integer preset, Integer group, Integer[] ids) throws IllegalParameterIdException, NoSuchGroupException, DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                Integer lv = p.getLeadVoiceIndexInGroup(group);
                if (lv == null)
                    throw new NoSuchGroupException();
                return getVoiceParams(preset, lv, ids);
            } catch (NoSuchVoiceException e) {
                throw new NoSuchGroupException(); // should never get here!!!
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Integer[] getVoiceParams(final Integer preset, Integer voice, Integer[] ids) throws IllegalParameterIdException, NoSuchVoiceException, DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        Integer[] vals;
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                vals = p.getVoice(voice).getValues(ids);
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
        return vals;
    }


    public Integer[] getVoicesParam(Integer preset, Integer[] voices, Integer id) throws IllegalParameterIdException, NoSuchVoiceException, DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        Integer[] vals = new Integer[voices.length];
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                for (int i = 0; i < voices.length; i++)
                    vals[i] = p.getVoice(voices[i]).getValue(id);
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
        return vals;
    }

    public Ticket setGroupParamFromVoice(final Integer preset, final Integer voice, final Integer id, final Integer value) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_setGroupParamFromVoice(preset, voice, id, value);
            }
        }, "setGroupParamFromVoice");
    }

    void task_setGroupParamFromVoice(final Integer preset, final Integer voice, final Integer id, final Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException, NoSuchVoiceException, DeviceException, ContentUnavailableException, NoSuchContextException, EmptyException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
            try {
                Integer group = p.getVoice(voice).getValues(new Integer[]{IntPool.get(37)})[0];
                p.setGroupValue(group, id, value);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket offsetGroupParamFromVoice(final Integer preset, final Integer voice, final Integer id, final Integer offset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_offsetGroupParamFromVoice(preset, voice, id, offset);
            }
        }, "setGroupParamFromVoice");
    }

    public Ticket offsetGroupParamFromVoice(final Integer preset, final Integer voice, final Integer id, final Double offsetAsFOR) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                Integer offset = ParameterModelUtilities.calcIntegerOffset(device.deviceParameterContext.getVoiceContext().getParameterDescriptor(id), offsetAsFOR);
                task_offsetGroupParamFromVoice(preset, voice, id, offset);
            }
        }, "setGroupParamFromVoice");
    }

    void task_offsetGroupParamFromVoice(final Integer preset, final Integer voice, final Integer id, final Integer offset) throws ParameterValueOutOfRangeException, IllegalParameterIdException, NoSuchVoiceException, DeviceException, ContentUnavailableException, NoSuchContextException, EmptyException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
            try {
                Integer group = p.getVoice(voice).getValues(new Integer[]{IntPool.get(37)})[0];
                p.offsetGroupValue(group, id, offset, true);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket setGroupParam(final Integer preset, final Integer group, final Integer id, final Integer value) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_setGroupParam(preset, group, id, value);
            }
        }, "setGroupParam");
    }

    void task_setGroupParam(final Integer preset, final Integer group, final Integer id, final Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException, NoSuchGroupException, DeviceException, ContentUnavailableException, NoSuchContextException, EmptyException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
            try {
                if (p.getVoiceIndexesInGroup(group).length == 0)
                    throw new NoSuchGroupException();
                p.setGroupValue(group, id, value);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    /*
    void queue_setGroupParam(final Integer preset, final Integer group, final Integer[] voicesInGroup, final Integer id, final Integer value) throws QueueUnavailableException {
        internalParamQ.postTask(new SetGroupParameterValueTask(preset, group, voicesInGroup, id, value));
    }
    */
    public Integer[] getZoneParams(Integer preset, Integer voice, Integer zone, Integer[] ids) throws IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        Integer[] vals;
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                vals = p.getVoice(voice).getZone(zone).getValues(ids);
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
        return vals;
    }

    public Ticket newLinks(final Integer preset, final Integer[] presetNums) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    newIfReallyEmpty(preset, DeviceContext.UNTITLED_PRESET);
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.newLinks(presetNums);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "newLinks");
    }

    public Ticket newVoices(final Integer preset, final Integer[] sampleNums) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                if (sampleNums.length == 0)
                    throw new IllegalArgumentException("need some sample numbers to create new voices");
                boolean newed = false;
                Integer[] t_sampleNums = sampleNums;
                db.access();
                try {
                    newed = newIfReallyEmpty(preset, DeviceContext.UNTITLED_PRESET);
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        if (newed) {
                            try {
                                p.getVoice(IntPool.get(0)).setValue(ID.sample, sampleNums[0]);
                            } catch (IllegalParameterIdException e) {
                            } catch (ParameterValueOutOfRangeException e) {
                            } catch (NoSuchVoiceException e) {
                            }
                            if (sampleNums.length == 1)
                                return;
                            t_sampleNums = new Integer[sampleNums.length - 1];
                            System.arraycopy(sampleNums, 1, t_sampleNums, 0, sampleNums.length - 1);
                        }
                        p.newVoices(t_sampleNums);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "newVoices");
    }

    public Ticket newZones(final Integer preset, final Integer voice, final Integer num) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                Integer addedIndex;
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        addedIndex = IntPool.get(p.getVoice(voice).numZones());
                        DatabaseVoice v = p.getVoice(voice);
                        for (int i = 0, j = num.intValue(); i < j; i++)
                            v.newZone();
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "newZones");
    }

    public int numLinks(Integer preset) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.numLinks();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public int numPresetSamples(Integer preset) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.numReferencedSamples();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Set presetSampleSet(Integer preset) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.referencedSampleSet();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public IntegerUseMap presetSampleUsage(Integer preset) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.referencedSampleUsage();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public int numPresetLinkPresets(Integer preset) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.numReferencedPresets();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Set presetLinkPresetSet(Integer preset) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.referencedPresetSet();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public IntegerUseMap presetLinkPresetUsage(Integer preset) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.referencedPresetUsage();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public int numPresetZones(Integer preset) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.numZones();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public int numVoices(Integer preset) throws DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.numVoices();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public int numZones(Integer preset, Integer voice) throws NoSuchVoiceException, DeviceException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.getVoice(voice).numZones();
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket refreshVoiceParameters(final Integer preset, final Integer voice, final Integer[] ids) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                if (preset.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                    refresh(preset);
                else {
                    db.access();
                    try {
                        DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                        try {
                            p.getVoice(voice).refreshParameters(ids);
                        } finally {
                            db.releaseWriteContent(preset);
                        }
                    } finally {
                        db.release();
                    }
                }
            }
        }, "refreshVoiceParameters");
    }

    public Ticket rmvLinks(final Integer preset, final Integer[] links) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        for (int i = links.length - 1; i >= 0; i--)
                            p.rmvLink(links[i]);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "rmvLinks");
    }

    public Ticket rmvVoices(final Integer preset, final Integer[] voices) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_rmvVoices(preset, voices);
            }
        }, "rmvVoices");
    }

    void task_rmvVoices(final Integer preset, final Integer[] voices) throws DeviceException, ContentUnavailableException, EmptyException, CannotRemoveLastVoiceException, NoSuchVoiceException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
            try {
                p.rmvVoices(voices);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket rmvZones(final Integer preset, final Integer voice, final Integer[] zones) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    Collections.sort(Arrays.asList(zones));
                    try {
                        for (int i = zones.length - 1; i >= 0; i--)
                            p.getVoice(voice).rmvZone(zones[i]);
                    } catch (NoSuchZoneException e) {
                        e.printStackTrace();
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "rmvZones");
    }

    public Ticket setLinkParam(final Integer preset, final Integer link, final Integer id, final Integer value) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_setLinkParam(preset, link, id, value);
            }
        }, "setLinksParam");
    }

    void task_setLinkParam(final Integer preset, final Integer link, final Integer id, final Integer value) throws DeviceException, ContentUnavailableException, EmptyException, NoSuchLinkException, ParameterValueOutOfRangeException, IllegalParameterIdException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
            try {
                p.getLink(link).setValue(id, value);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket offsetLinkParam(final Integer preset, final Integer link, final Integer id, final Integer offset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.getLink(link).offsetValue(id, offset, true);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "offsetLinkParam");
    }

    public Ticket offsetLinkParam(final Integer preset, final Integer link, final Integer id, final Double offsetAsFOR) throws IllegalParameterIdException {
        Integer offset = ParameterModelUtilities.calcIntegerOffset(device.deviceParameterContext.getLinkContext().getParameterDescriptor(id), offsetAsFOR);
        return offsetLinkParam(preset, link, id, offset);
    }

    // ZONE
    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer preset, Integer voice, Integer zone) throws DeviceException, NoSuchVoiceException, NoSuchZoneException, NoSuchContextIndexException, ContentUnavailableException, EmptyException {
        db.access();
        try {
            DatabasePreset p = db.getRead(this, preset);
            try {
                return p.getVoice(voice).getIsolatedZone(zone);
            } finally {
                db.releaseReadContent(preset);
            }
        } finally {
            db.release();
        }
    }

    // ZONE
    public Ticket newZone(final Integer preset, final Integer voice, final IsolatedPreset.IsolatedVoice.IsolatedZone iz) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.getVoice(voice).dropZone(iz);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "newZone");
    }

    private Integer[] fxaIds = new Integer[]{IntPool.get(7), IntPool.get(8), IntPool.get(9), IntPool.get(10), IntPool.get(11), IntPool.get(12), IntPool.get(13)};
    private Integer[] fxbIds = new Integer[]{IntPool.get(15), IntPool.get(16), IntPool.get(17), IntPool.get(18), IntPool.get(19), IntPool.get(20), IntPool.get(21)};

    public Ticket setPresetParam(final Integer preset, final Integer id, final Integer value) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_setPresetParam(preset, id, value);
            }
        }, "setPresetParams");
    }

    void task_setPresetParam(final Integer preset, final Integer id, final Integer value) throws DeviceException, ContentUnavailableException, EmptyException, ParameterValueOutOfRangeException, IllegalParameterIdException {
        // fxA = id6 (6-13) & fxB = id14 (14-21)
        db.access();
        try {
            DatabasePreset p = db.getWrite(this, preset);
            try {
                p.setValue(id, value);
                /*
                if (id.equals(IntPool.get(6)))
                    refreshPresetParams(preset, fxaIds);
                if (id.equals(IntPool.get(14)))
                    refreshPresetParams(preset, fxbIds);
                    */
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket offsetPresetParam(final Integer preset, final Integer id, final Integer offset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                // fxA = id6 (6-13) & fxB = id14 (14-21)
                taskOffsetPresetParam(preset, id, offset);
            }
        }, "offsetPresetParam");
    }

    private void taskOffsetPresetParam(final Integer preset, final Integer id, final Integer offset) throws DeviceException, EmptyException, ContentUnavailableException, IllegalParameterIdException, ParameterValueOutOfRangeException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(this, preset);
            try {
                p.offsetValue(id, offset, true);
                /*
                if (id.equals(IntPool.get(6))){
                    taskRefreshPresetParams(preset, fxaIds);
                    // HACK!! - not updating properly without this
                    db.getEventHandler().postInternalEvent(new PresetChangeEvent(this, preset, new Integer[]{id}, new Integer[]{p.getValue(id)}));
                }
                if (id.equals(IntPool.get(14))){
                    taskRefreshPresetParams(preset, fxbIds);
                    // HACK!! - not updating properly without this
                    db.getEventHandler().postInternalEvent(new PresetChangeEvent(this, preset, new Integer[]{id}, new Integer[]{p.getValue(id)}));
                }
                */
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket offsetPresetParam(final Integer preset, final Integer id, final Double offsetAsFOR) throws IllegalParameterIdException {
        Integer offset = ParameterModelUtilities.calcIntegerOffset(device.deviceParameterContext.getZoneContext().getParameterDescriptor(id), offsetAsFOR);
        return offsetPresetParam(preset, id, offset);
    }

    void refreshPresetParams(final Integer preset, final Integer[] ids) {
        try {
            presetContextQ.getPostableTicket(new TicketRunnable() {
                public void run() throws Exception {
                    taskRefreshPresetParams(preset, ids);
                }
            }, "refreshPresetParams").post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void taskRefreshPresetParams(final Integer preset, final Integer[] ids) throws DeviceException, EmptyException, ContentUnavailableException, IllegalParameterIdException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(this, preset);
            try {
                db.getEventHandler().sync();
                p.refreshParameters(ids);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket setVoiceParam(final Integer preset, final Integer voice, final Integer id, final Integer value) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_setVoiceParam(preset, voice, id, value);
            }
        }, "setVoicesParam");
    }

    void task_setVoiceParam(final Integer preset, final Integer voice, final Integer id, final Integer value) throws DeviceException, ContentUnavailableException, EmptyException, NoSuchVoiceException, ParameterValueOutOfRangeException, IllegalParameterIdException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
            try {
                p.getVoice(voice).setValue(id, value);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket offsetVoiceParam(final Integer preset, final Integer voice, final Integer id, final Integer offset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.getVoice(voice).offsetValue(id, offset, true);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "offsetVoiceParam");
    }

    public Ticket offsetVoiceParam(final Integer preset, final Integer voice, final Integer id, final Double offsetAsFOR) throws IllegalParameterIdException {
        Integer offset = ParameterModelUtilities.calcIntegerOffset(device.deviceParameterContext.getVoiceContext().getParameterDescriptor(id), offsetAsFOR);
        return offsetVoiceParam(preset, voice, id, offset);
    }

    public Ticket setZoneParam(final Integer preset, final Integer voice, final Integer zone, final Integer id, final Integer value) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                task_setZoneParam(preset, voice, zone, id, value);
            }
        }, "setZonesParam");
    }

    void task_setZoneParam(final Integer preset, final Integer voice, final Integer zone, final Integer id, final Integer value) throws DeviceException, ContentUnavailableException, EmptyException, NoSuchVoiceException, NoSuchZoneException, ParameterValueOutOfRangeException, IllegalParameterIdException {
        db.access();
        try {
            DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
            try {
                p.getVoice(voice).getZone(zone).setValue(id, value);
            } finally {
                db.releaseWriteContent(preset);
            }
        } finally {
            db.release();
        }
    }

    public Ticket offsetZoneParam(final Integer preset, final Integer voice, final Integer zone, final Integer id, final Integer offset) {
        return presetContextQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    DatabasePreset p = db.getWrite(Impl_PresetContext.this, preset);
                    try {
                        p.getVoice(voice).getZone(zone).offsetValue(id, offset, true);
                    } finally {
                        db.releaseWriteContent(preset);
                    }
                } finally {
                    db.release();
                }
            }
        }, "offsetZoneParam");
    }

    public Ticket offsetZoneParam(final Integer preset, final Integer voice, final Integer zone, final Integer id, final Double offsetAsFOR) throws IllegalParameterIdException {
        Integer offset = ParameterModelUtilities.calcIntegerOffset(device.deviceParameterContext.getZoneContext().getParameterDescriptor(id), offsetAsFOR);
        return offsetZoneParam(preset, voice, zone, id, offset);
    }

    public DeviceParameterContext getDeviceParameterContext() throws DeviceException {
        return device.getDeviceParameterContext();
    }

    public DeviceContext getDeviceContext() {
        return device;
    }

    public SampleContext getRootSampleContext() {
        return device.sampleDB.getRootContext();
    }

    // EVENTS

    public void setDevice(E4Device device) {
        this.device = device;
    }

    public TicketedQ getRefreshQ() {
        return device.queues.refreshQ();
    }
}
