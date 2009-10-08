package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.threads.ZBackgroundRemoterThread;
import com.pcmsolutions.system.threads.ZDBModifyThread;
import com.pcmsolutions.system.threads.ZWaitThread;
import com.pcmsolutions.util.CALock;
import com.pcmsolutions.util.RWLock;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

// PresteDatabseProxy also happens to be the database by way of implementing PDBReader and PDBWriter
// please device samples in agreed direction which is deemed ascending - this prevents race conditions on samples locks
// PDBReader:getSampleRW does this very job with two samples

class SampleDatabase implements ZDisposable, StandardStateMachine, SampleDatabaseProxy, SDBWriter, SDBReader, java.io.Serializable, RemoteAssignable {
    private static final String SUMMARY_SAMPLE_EMPTY = "Empty Sample";
    private static final String SUMMARY_SAMPLE_NOT_INITIALIZED = "USER Sample (Uninitialized)";
    private static final String SUMMARY_SAMPLE_ROM = "ROM Sample";
    private static final String SUMMARY_ERROR = "== no info ==";
    private static final String SUMMARY_SAMPLE_WRITE_LOCKED = "== sample locked ==";
    private static final String PREF_romSamplingFrequency = "RomSamplingCount";
    private static final int defRomSamplingFrequency = 30;

    private static final int SAMPLE_LOAD = 4000;

    protected String name;

    // HashMaps beacause these are only modfified under a device configure lock
    protected HashMap sc2s = new HashMap();

    protected HashMap sc2sc = new HashMap();

    protected HashMap s2sc = new HashMap(SAMPLE_LOAD);

    protected HashMap sc2parentsc = new HashMap();

    // Hashtables beacause these are can modified under a multi-client device access lock
    protected Hashtable s2sl = new Hashtable(SAMPLE_LOAD);

    protected Hashtable s2sobj = new Hashtable(SAMPLE_LOAD);

    transient protected Hashtable s2rt;

    transient protected Hashtable sampleInitializationMonitors;

    protected SampleContext rootSampleContext;

    protected Impl_SampleEventHandler se_handler;

    protected SampleContextFactory scf;

    protected CALock device;

    protected int maxSampleIndex;

    transient protected Vector workerThreads;

    private StateMachineHelper sm = new StateMachineHelper();

    private PresetDatabaseProxy presetDatabaseProxy;

    transient private DevicePreferences devicePreferences;

    public SampleDatabase(String name, int maxIndex) {
        this.name = name;
        this.maxSampleIndex = maxIndex;
        buildTransients();
    }

    private void buildTransients() {
        s2rt = new Hashtable(SAMPLE_LOAD);
        sampleInitializationMonitors = new Hashtable();
        workerThreads = new Vector();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        buildTransients();
    }

    protected void setDevicePreferences(DevicePreferences devicePreferences) {
        this.devicePreferences = devicePreferences;
    }

    public void setRemote(Remotable r) {
        Object sc;
        for (Iterator i = sc2s.keySet().iterator(); i.hasNext();) {
            sc = i.next();
            if (sc instanceof RemoteAssignable)
                ((RemoteAssignable) sc).setRemote(r);
        }
    }

    public void init(SampleContextFactory scf, CALock device, DevicePreferences prefs) {
        this.scf = scf;
        this.device = device;
        this.rootSampleContext = scf.newSampleContext("Default Sample Context", (SampleDatabaseProxy) this);
        this.se_handler = new Impl_SampleEventHandler();
        this.devicePreferences = prefs;
    }

    public void setPresetDatabaseProxy(PresetDatabaseProxy presetDatabaseProxy) {
        this.presetDatabaseProxy = presetDatabaseProxy;
    }

    public void zDispose() {
        se_handler.zDispose();
    }

    public int getNumberOfInstalledSampleRoms() {
        return (getDBCount() - DeviceContext.MAX_USER_SAMPLE) / 1000;
    }

    public void stateInitial() throws IllegalStateTransitionException {
        device.configure();
        try {
            if (sm.testTransition(sm.STATE_INITIALIZED) == sm.STATE_INITIALIZED)
                return;
            // create entries for new root
            sc2s.put(rootSampleContext, new TreeMap());
            sc2sc.put(rootSampleContext, new HashMap());
            sc2parentsc.put(rootSampleContext, null);
            sm.transition(STATE_INITIALIZED);
        } finally {
            device.unlock();
        }
    }

    public void stateStart() throws IllegalStateTransitionException {
        device.configure();
        try {
            if (sm.transition(sm.STATE_STARTED) == sm.STATE_STARTED)
                return;
            se_handler.start();
        } finally {
            device.unlock();
        }
    }

    public void stateStop() throws IllegalStateTransitionException {
        device.configure();
        try {
            if (sm.transition(sm.STATE_STOPPED) == sm.STATE_STOPPED)
                return;
            //se_handler.clearListeners();
            se_handler.stop();
            stopWorkerThreads();
        } finally {
            device.unlock();
        }
    }

    public Thread[] stopWorkerThreads() {
        device.configure();
        ZWaitThread t;
        try {
            synchronized (workerThreads) {
                Vector wt_clone = (Vector) workerThreads.clone();
                int size = workerThreads.size();
                for (int n = 0; n < size; n++) {
                    t = (ZWaitThread) workerThreads.get(n);
                    t.stopThread();
                }
                workerThreads.clear();
                return (Thread[]) wt_clone.toArray(new Thread[wt_clone.size()]);
            }
        } finally {
            device.unlock();
        }
    }

    public int getState() {
        return sm.getState();
    }


/*    public void initializeAllSampleNames(boolean chainToData) {
        int roms = this.getNumberOfInstalledSampleRoms();
        initializeSpecifiedSampleNames(0, getDBCount(), chainToData);
    }

    public void initializeUserSampleNames(boolean chainToData) {
        initializeSpecifiedSampleNames(0, DeviceContext.BASE_ROM_SAMPLE, chainToData);
    }

    public void initializeAllRomSampleNames(boolean chainToData) {
        int roms = this.getNumberOfInstalledSampleRoms();
        initializeSpecifiedSampleNames(DeviceContext.BASE_ROM_SAMPLE, getDBCount() - DeviceContext.BASE_ROM_SAMPLE, chainToData);
    }

    public void initializeSpecifiedRomSampleNames(int rom, boolean chainToData) {
        if (rom < 1 || rom > this.getNumberOfInstalledSampleRoms())
            throw new IllegalArgumentException("Unknown rom index");

        initializeSpecifiedSampleNames(DeviceContext.SAMPLE_ROM_SIZE * rom, DeviceContext.SAMPLE_ROM_SIZE, chainToData);
    }

    public void initializeSpecifiedSampleNames(final int lowIndex, final int count, final boolean chainToData) {
        if (lowIndex + count > getDBCount())
            throw new IllegalArgumentException("sample index out of range");

        int[] indexes = new int[count];

        for (int ti = 0,i = lowIndex, j = lowIndex + count; i < j; i++, ti++)
            indexes[ti] = i;

        initializeSpecifiedSampleNames(indexes, chainToData);
    }

    // assumes all passed indexes are valid!
    private void initializeSpecifiedSampleNames(final int[] indexes, final boolean chainToData) {
        ZWaitThread t = new ZBackgroundRemoterThread() {
            public void run() {
                this.setName("SampleDataPass");

                Integer sample;
                Object sobj;
                UninitSampleObject u_sobj;
                String name;
                RWLock sLock;

                for (int n = 0, o = indexes.length; n < o; n++) {
                    if (this.alive == false)
                        return;
                    device.access();
                    try {
                        sample = IntPool.get(indexes[n]);
                        sLock = ((RWLock) s2sl.get(sample));
                        sLock.write();
                        try {
                            sobj = s2sobj.get(sample);
                            if (sobj != null && sobj instanceof UninitSampleObject) {
                                u_sobj = ((UninitSampleObject) sobj);
                                if (u_sobj.getState() == RemoteObjectStates.STATE_PENDING) {
                                    name = scf.initializeSampleNameAtIndex(sample, se_handler);
                                    if (name == null)
                                        continue;
                                    if (name.trim().equals(DeviceContext.EMPTY_SAMPLE)) {
                                        s2sobj.put(sample, EmptySample.getInstance());
                                        se_handler.postSampleEvent(new SampleInitializeEvent(this, sample));
                                    } else {
                                        u_sobj.setName(name);
                                        se_handler.postSampleEvent(new SampleNameChangeEvent(this, sample));
                                    }
                                }
                            }
                        } finally {
                            sLock.unlock();
                        }
                    } finally {
                        device.unlock();
                    }
                    yield();
                }
                if (chainToData)
                    initializeSpecifiedSampleData(indexes);
            }
        };
        workerThreads.addDesktopElement(t);
        t.stateStart();
    }
  */

    public void initializeAllSampleData() {
        initializeUserSampleData();
        initializeAllRomSampleData();
    }

    public void initializeUserSampleData() {
        initializeSpecifiedSampleData(0, DeviceContext.BASE_ROM_SAMPLE);
    }

    public void initializeAllRomSampleData() {
        int roms = this.getNumberOfInstalledSampleRoms();

        for (int i = 1; i <= roms; i++)
            initializeSpecifiedRomSampleData(i);
    }

    public void initializeSpecifiedRomSampleData(final int rom) {
        if (rom < 1 || rom > this.getNumberOfInstalledSampleRoms())
            throw new IllegalArgumentException("Unknown rom index");

        SampleRomStore.RomProfile[] profs = new SampleRomStore.RomProfile[0];
        if (devicePreferences.ZPREF_useRomMatching.getValue())
            profs = testRom(rom);

        final SampleRomStore.RomProfile[] f_profs = profs;

        if (f_profs.length == 0) {
            new ZBackgroundRemoterThread("sample name initialization") {
                public void run() {
                    SampleRomNameProvisionMediator npm = new SampleRomNameProvisionMediator() {
                        protected String[] names = new String[DeviceContext.SAMPLE_ROM_SIZE];

                        public boolean wantsNames() {
                            return true;
                        }

                        public void setName(int index, String name) {
                            int ni = index % DeviceContext.SAMPLE_ROM_SIZE;
                            if (ni >= 0 && ni < names.length)
                                this.names[ni] = name;
                        }

                        public boolean providesNames() {
                            return false;
                        }

                        public String getName(int index) {
                            return null;
                        }

                        public void done() {
                            try {
                                SampleRomStore.addRom("", "", "", this.names);
                            } catch (BackingStoreException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    initializeSpecifiedSampleData(DeviceContext.SAMPLE_ROM_SIZE * rom, DeviceContext.SAMPLE_ROM_SIZE, npm);
                }
            }.start();
        } else {
            SampleRomNameProvisionMediator npm = new SampleRomNameProvisionMediator() {
                protected String[] names = f_profs[0].sampleNames();

                public boolean wantsNames() {
                    return false;
                }

                public void setName(int index, String name) {
                }

                public boolean providesNames() {
                    return true;
                }

                public String getName(int index) {
                    int ni = index % DeviceContext.SAMPLE_ROM_SIZE;
                    if (ni < names.length)
                        return names[ni];
                    return null;
                }

                public void done() {
                }
            };
            initializeSpecifiedSampleData(DeviceContext.SAMPLE_ROM_SIZE * rom, DeviceContext.SAMPLE_ROM_SIZE, npm);
        }
    }

    public void initializeSpecifiedSampleData(final int lowIndex, final int count) {
        initializeSpecifiedSampleData(lowIndex, count, null);
    }

    public void initializeSpecifiedSampleData(final int lowIndex, final int count, SampleRomNameProvisionMediator cb) {
        if (lowIndex + count > getDBCount())
            throw new IllegalArgumentException("sample index out of range");

        int[] indexes = new int[count];

        for (int ti = 0,i = lowIndex, j = lowIndex + count; i < j; i++, ti++)
            indexes[ti] = i;

        initializeSpecifiedSampleData(indexes, cb);
    }

    private interface SampleRomNameProvisionMediator {
        public boolean wantsNames();

        public void setName(int index, String name);

        public boolean providesNames();

        public String getName(int index);

        public void done();
    }

    private void initializeSpecifiedSampleData(final int[] indexes) {
        initializeSpecifiedSampleData(indexes, null);
    }

    private void initializeSpecifiedSampleData(final int[] indexes, final SampleRomNameProvisionMediator cb) {
        //if (true)
        //  return;
        ZWaitThread t = new ZBackgroundRemoterThread() {
            public void run() {
                try {
                    this.setName("SampleDataPass");
                    Integer sample;
                    RWLock sLock;
                    Object sobj;
                    for (int n = 0, o = indexes.length; n < o; n++) {
                        device.access();
                        try {
                            if (this.alive == false) {
                                return;
                            }
                            sample = IntPool.get(indexes[n]);
                            sLock = ((RWLock) s2sl.get(sample));
                            sLock.write();
                            try {
                                sobj = s2sobj.get(sample);
                                if (sobj != null && sobj instanceof UninitSampleObject) {
                                    if (cb != null && cb.providesNames())
                                        sobj = scf.initializeSampleAtIndex(sample, cb.getName(sample.intValue()), se_handler);
                                    else
                                        sobj = scf.initializeSampleAtIndex(sample, se_handler);
                                    if (sobj == null)
                                        continue;
                                    s2sobj.put(sample, sobj);
                                    se_handler.postSampleEvent(new SampleInitializeEvent(this, sample));
                                }
                                if (cb != null && sobj != null && cb.wantsNames())
                                    cb.setName(sample.intValue(), sobj.toString());
                            } finally {
                                sLock.unlock();
                            }
                        } finally {
                            device.unlock();
                        }
                        // yield();
                    }
                } finally {
                    if (cb != null)
                        cb.done();
                }
            }
        };
        workerThreads.add(t);
        t.start();
    }

    //PREF_ROM_SAMPLING_INTERVAL

    private SampleRomStore.RomProfile[] testRom(int rom) {
        if (rom < 1 || rom > this.getNumberOfInstalledSampleRoms())
            throw new IllegalArgumentException("Unknown rom index");

        int testPoints = Preferences.userNodeForPackage(this.getClass()).getInt(PREF_romSamplingFrequency, defRomSamplingFrequency);

        int[] indexes = new int[testPoints];
        String[] names = new String[testPoints];
        int inc = DeviceContext.SAMPLE_ROM_SIZE / testPoints;

        for (int i = 0,testIndex = rom * DeviceContext.SAMPLE_ROM_SIZE; i < testPoints; testIndex += inc, i++) {
            names[i] = initializeSampleData(testIndex);
            indexes[i] = testIndex % DeviceContext.SAMPLE_ROM_SIZE;
        }

        try {
            return SampleRomStore.tryMatchRom(indexes, names);
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return new SampleRomStore.RomProfile[0];
    }

    private String initializeSampleData(int index) {
        device.access();
        Integer sample;
        RWLock sLock;
        Object sobj;
        try {
            sample = IntPool.get(index);
            sLock = ((RWLock) s2sl.get(sample));
            sLock.write();
            try {
                sobj = s2sobj.get(sample);
                if (sobj != null && sobj instanceof UninitSampleObject) {
                    sobj = scf.initializeSampleAtIndex(sample, se_handler);
                    if (sobj != null) {
                        s2sobj.put(sample, sobj);
                        se_handler.postSampleEvent(new SampleInitializeEvent(this, sample));
                        return sobj.toString();
                    }
                }
            } finally {
                sLock.unlock();
            }
        } finally {
            device.unlock();
        }
        return null;
    }

    public void loadSampleObjects(Map sampleObjects) {
        for (Iterator i = sampleObjects.keySet().iterator(); i.hasNext();)
            addObjectToDB(sampleObjects.get(i.next()));
    }

    public void addObjectToDB(Object o) {
        Integer index = IntPool.get(size());
        addObjectToDB(o, index);
    }

    public void addObjectToDB(Object o, Integer index) {
        if (o instanceof SampleObject)
            addObjectToDB((SampleObject) o, index);
        else if (o instanceof UninitSampleObject)
            addObjectToDB((UninitSampleObject) o, index);
        else if (o instanceof EmptySample)
            addObjectToDB((EmptySample) o, index);
        else
            throw new IllegalArgumentException("Not a valid object for the sample database");
    }

    private void addObjectToDB(SampleObject s, Integer index) {
        finalizeAddObjectToDB(new SampleObject(index, s, se_handler), index);
    }

    private void addObjectToDB(UninitSampleObject s, Integer index) {
        s.setSEH(se_handler);
        finalizeAddObjectToDB(s, index);
    }

    private void addObjectToDB(EmptySample es, Integer index) {
        finalizeAddObjectToDB(es, index);
    }

    private void finalizeAddObjectToDB(Object s, Integer index) {
        s2sl.put(index, new RWLock());
        s2sobj.put(index, s);
        addIndexToContext(index, rootSampleContext);
    }

    private void addIndexToContext(Integer index, SampleContext context) {
        s2sc.put(index, context);
        ((Map) sc2s.get(context)).put(index, null);
    }

    public void eraseUser() {
        device.configure();
        try {
            Object o;
            Integer sample;
            for (int i = 0,j = DeviceContext.MAX_USER_SAMPLE; i <= j; i++) {
                sample = IntPool.get(i);
                o = s2sobj.put(IntPool.get(i), EmptySample.getInstance());
                if (o != null && o instanceof ZDisposable)
                    ((ZDisposable) o).zDispose();
                se_handler.postSampleEvent(new SampleRefreshEvent(this, sample));
            }
        } finally {
            device.unlock();
        }
    }

    public void uninitializeUser() {
        device.configure();
        try {
            Object o;
            Integer sample;
            for (int i = 0,j = DeviceContext.MAX_USER_SAMPLE; i <= j; i++) {
                sample = IntPool.get(i);
                o = s2sobj.put(IntPool.get(i), new UninitSampleObject(sample));
                if (o != null && o instanceof ZDisposable)
                    ((ZDisposable) o).zDispose();
                se_handler.postSampleEvent(new SampleRefreshEvent(this, sample));
            }
        } finally {
            device.unlock();
        }
    }

    private int size() {
        return s2sl.size();
    }

    public String toString() {
        return name;
    }

    public SampleEventHandler getSampleEventHandler() {
        return se_handler;
    }

    public void addSampleListener(SampleListener sl, Integer[] samples) {
        int count = samples.length;
        for (int n = 0; n < count; n++)
            se_handler.addSampleListener(sl, samples[n]);

    }

    public void removeSampleListener(SampleListener pl, Integer[] samples) {
        int count = samples.length;
        for (int n = 0; n < count; n++)
            se_handler.removeSampleListener(pl, samples[n]);
    }

    public SDBWriter getDBWrite() {
        device.configure();
        return this;
    }

    public SDBReader getDBRead() {
        device.access();
        return this;
    }

    public void release() {
        device.unlock();
    }

    public Set getReadableSamples() {
        Set rp;
        device.access();
        try {
            rp = ((Hashtable) s2sobj.clone()).keySet();
        } finally {
            device.unlock();
        }
        if (rp == null)
            rp = new HashMap().keySet();
        return rp;
    }

    public void releaseContext(SampleContext sc) throws NoSuchContextException {
        if (!assertContext(sc))
            throw new NoSuchContextException();

        HashMap subContextMap = (HashMap) sc2sc.get(sc);

// release subcontexts
        Iterator i = subContextMap.keySet().iterator();
        for (; i.hasNext();)
            releaseContext((SampleContext) subContextMap.get(i.next()));

// release samples
        if (releaseAllSamples(sc)) {
// remove this SampleContext
            sc2s.remove(sc);
            sc2sc.remove(sc);
            sc2parentsc.remove(sc);
        }
    }

    public void releaseContextSample(SampleContext sc, Integer sample) throws NoSuchContextException, NoSuchSampleException {
        if (!assertContext(sc))
            throw new NoSuchContextException();

        Map samples = (Map) sc2s.get(sc);
        if (!samples.containsKey(sample))
            throw new NoSuchSampleException(sample);

        releaseSample(sc, sample);
    }

    public SampleContext newContext(SampleContext sc, String name, Integer[] samples) throws NoSuchSampleException, NoSuchContextException {
        if (!assertContext(sc))
            throw new NoSuchContextException();

        SampleContext npc;
        for (int n = 0; n < samples.length; n++)
            if (!hasSample(sc, samples[n]))
                throw new NoSuchSampleException(samples[n]);

        npc = scf.newSampleContext(name, this);
        createContext(sc, npc);

        for (int n = 0; n < samples.length; n++)
            transferSample(sc, npc, samples[n]);

        return npc;
    }

    public void removeSamplesFromContext(SampleContext src, Integer[] samples) throws NoSuchContextException, NoSuchSampleException {
        if (!assertContext(src)) {
            throw new NoSuchContextException();
        }
        for (int n = 0; n < samples.length; n++)
            if (!hasSample(src, samples[n]))
                throw new NoSuchSampleException(samples[n]);
        for (int n = 0; n < samples.length; n++)
            removeSampleFrom(src, samples[n]);
    }

    public void addSamplesToContext(SampleContext dest, Integer[] samples) throws NoSuchContextException, NoSuchSampleException {
        if (!assertContext(dest)) {
            throw new NoSuchContextException();
        }
        for (int n = 0; n < samples.length; n++)
            if (!readsSample(dest, samples[n]))
                throw new NoSuchSampleException(samples[n]);
        for (int n = 0; n < samples.length; n++)
            addSampleTo(dest, samples[n]);
    }

    public List expandContextWithEmptySamples(SampleContext src, SampleContext dest, Integer reqd) throws NoSuchContextException {
        if (!assertContext(src) || !assertContext(dest)) {
            throw new NoSuchContextException();
        }
        List found = findEmptySamples(src, reqd.intValue());
        for (int n = 0; n < found.size(); n++)
            transferSample(src, dest, (Integer) found.get(n));

        return found;
    }

    public int getDBCount() {
        return s2sl.size();
    }

    public boolean seesSample(SampleContext sc, Integer sample) {
        boolean rv;

        if (sc == null)
            rv = false;
        else if (s2sc.get(sample) != sc)
            return seesSample((SampleContext) sc2parentsc.get(sc), sample);
        else
            rv = true;

        return rv;
    }

    public boolean readsSample(SampleContext sc, Integer sample) {

        if (sc == null || !sc2s.containsKey(sc))
            return false;

        if (s2sobj.containsKey(sample))
            return true;

        return false;
    }

    public boolean isSampleInitialized(Integer sample) throws NoSuchSampleException {
        Object o = s2sobj.get(sample);
        if (o == null)
            throw new NoSuchSampleException(sample);
        if (o instanceof UninitSampleObject)
            return false;
        return true;
    }

    public boolean hasSample(SampleContext sc, Integer sample) {
        if (sc != null && s2sc.get(sample) == sc)
            return true;

        return false;
    }

    public SampleContext getRootContext() {
        return rootSampleContext;
    }

    public PresetContext getRootPresetContext() {
        return presetDatabaseProxy.getRootContext();
    }

    public String getSampleName(Integer sample) throws NoSuchSampleException {
        Object sobj = s2sobj.get(sample);
        if (sobj == null)
            throw new NoSuchSampleException(sample);
        return sobj.toString();
    }

    public String getSampleNameExtended(SampleContext pc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        lockSampleWrite(pc, sample);
        try {
            Object sobj = s2sobj.get(sample);
            if (sobj == null)
                throw new NoSuchSampleException(sample);
            if (sobj instanceof UninitSampleObject) {
                sobj = scf.initializeSampleAtIndex(sample, se_handler);
                if (sobj != null)
                    s2sobj.put(sample, sobj);
            }
            return s2sobj.get(sample).toString();
        } finally {
            unlockSample(sample);
        }
    }

    public void setSampleName(SampleContext sc, Integer sample, String name) throws SampleEmptyException, NoSuchSampleException, NoSuchContextException {
        lockSampleWrite(sc, sample);
        try {
            Object sobj = s2sobj.get(sample);

            if ((sobj instanceof UninitSampleObject) && ((UninitSampleObject) sobj).getState() == RemoteObjectStates.STATE_NAMED) {
                ((UninitSampleObject) sobj).setName(name);
                return;
            }
            SampleObject s_obj = getSampleWrite(sc, sample);
            try {
                s_obj.setName(name);
            } finally {
                unlockSample(sample);
            }
        } finally {
            unlockSample(sample);
        }
        //getSampleWrite(sc, sample).setName(name);
        //unlockSample(sample);
    }

    public void refreshSample(SampleContext sc, Integer sample) throws NoSuchContextException, NoSuchSampleException {
        lockSampleWrite(sc, sample);
        Object n_pobj;
        UninitSampleObject uso;
        try {
            //synchronized (p2rt) {
            Thread rt;
            rt = (Thread) s2rt.get(sample);
            if (rt != null)
                return;
            s2rt.put(sample, Thread.currentThread());
            // }
        } finally {
            unlockSample(sample);
        }

        lockSampleWrite(sc, sample);
        try {
            uso = new UninitSampleObject(sample, getSampleName(sample));
            uso.setSEH(se_handler);
            uso.markInitializing();
            Object o = s2sobj.put(sample, uso);
            if (o instanceof ZDisposable)
                ((ZDisposable) o).zDispose();
            se_handler.postSampleEvent(new SampleInitializationStatusChangedEvent(this, sample));

        } finally {
            unlockSample(sample);
        }

        n_pobj = scf.initializeSampleAtIndex(sample, se_handler);

        lockSampleWrite(sc, sample);
        try {
            if (n_pobj != null) {
                Object o = s2sobj.put(sample, n_pobj);
                if (o != null && o instanceof ZDisposable)
                    ((ZDisposable) o).zDispose();
                se_handler.postSampleEvent(new SampleInitializeEvent(this, sample));
            } else {
                //UninitSampleObject uso = new UninitSampleObject(sample);
                //uso.setSEH(pe_handler);
                uso.markPending();
                // Object o = p2pobj.put(sample, uso);
                //if (o instanceof ZDisposable)
                //   ((ZDisposable) o).zDispose();
                se_handler.postSampleEvent(new SampleInitializationStatusChangedEvent(this, sample));
            }
        } finally {
            //synchronized (p2rt) {
            if (s2rt.get(sample) == Thread.currentThread())
                s2rt.remove(sample);
            //}
            unlockSample(sample);
        }

    }

    public Set getSampleIndexesInContext(SampleContext sc) throws NoSuchContextException {
        if (!assertContext(sc))
            throw new NoSuchContextException();

        Set rv;
        // rv = ((Map) ((TreeMap) sc2s.get(sc)).clone()).keySet();
        rv = new TreeSet(((TreeMap) sc2s.get(sc)).keySet());
        return rv;
    }

    public Set getReadableSampleIndexes(SampleContext sc) throws NoSuchContextException {
        if (!assertContext(sc))
            throw new NoSuchContextException();
        TreeMap sortMap = new TreeMap();
        sortMap.putAll(s2sl);
        return sortMap.keySet();
    }

    public Map getSampleNamesInContext(SampleContext sc) throws NoSuchContextException {
        Set samples = getSampleIndexesInContext(sc);
        TreeMap rv = new TreeMap();
        Iterator i = samples.iterator();
        Integer p;
        String name;
        while (i.hasNext()) {
            p = (Integer) i.next();
            try {
                name = getSampleName(p);
                rv.put(p, name);
            } catch (NoSuchSampleException e) {
                rv.put(p, null);
            }
        }
        return rv;
    }

    public Map getUserSampleNamesInContext(SampleContext sc) throws NoSuchContextException {
        Set samples = getSampleIndexesInContext(sc);
        TreeMap rv = new TreeMap();
        Iterator i = samples.iterator();
        Integer p;
        String name;
        while (i.hasNext()) {
            p = (Integer) i.next();
            if (p.intValue() > 0 && p.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                try {
                    name = getSampleName(p);
                    rv.put(p, name);
                } catch (NoSuchSampleException e) {
                    rv.put(p, null);
                }
        }
        return rv;
    }

    public void assertSampleNamed(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        lockSampleWrite(sc, sample);
        try {
            Object pobj = s2sobj.get(sample);
            UninitSampleObject u_pobj;
            if (pobj == null)
                throw new NoSuchSampleException(sample);
            if (pobj instanceof UninitSampleObject) {
                u_pobj = ((UninitSampleObject) pobj);
                if (u_pobj.getState() == RemoteObjectStates.STATE_PENDING) {
                    name = scf.initializeSampleNameAtIndex(sample, se_handler);
                    if (name == null)
                        return;
                    if (name.trim().equals(DeviceContext.EMPTY_PRESET)) {
                        s2sobj.put(sample, EmptySample.getInstance());
                        se_handler.postSampleEvent(new SampleInitializeEvent(this, sample));
                    } else {
                        u_pobj.setName(name);
                        se_handler.postSampleEvent(new SampleNameChangeEvent(this, sample));
                    }
                }
            }
        } finally {
            unlockSample(sample);
        }
    }

    public void changeSampleObject(SampleContext sc, Integer sample, Object sobj) throws NoSuchSampleException, NoSuchContextException {
        lockSampleWrite(sc, sample);
        try {
            Object o = s2sobj.put(sample, sobj);
            if (o != null && o instanceof ZDisposable)
                ((ZDisposable) o).zDispose();
            se_handler.postSampleEvent(new SampleRefreshEvent(this, sample));
        } finally {
            unlockSample(sample);
        }
    }

    public void lockSampleRead(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        if (sc == null)
            throw new NoSuchContextException();

        RWLock pLock = (RWLock) s2sl.get(sample);

        if (pLock != null)
            pLock.read();
        else
            throw new NoSuchSampleException(sample);
    }

    public boolean tryLockSampleRead(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        if (sc == null)
            throw new NoSuchContextException();

        RWLock pLock = (RWLock) s2sl.get(sample);
        if (pLock != null) {
            return pLock.tryRead();
        } else
            throw new NoSuchSampleException(sample);
    }

    public void lockSampleWrite(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        if (sc == null)
            throw new NoSuchContextException();

        if (s2sc.get(sample) != sc)
            lockSampleWrite(getParent(sc), sample);
        else {
            RWLock pLock = (RWLock) s2sl.get(sample);
            if (pLock != null)
                pLock.write();
            else
                throw new NoSuchSampleException(sample);
        }
    }

    public boolean tryLockSampleWrite(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        if (sc == null)
            throw new NoSuchContextException();

        if (s2sc.get(sample) != sc)
            return tryLockSampleWrite(getParent(sc), sample);
        else {
            RWLock pLock = (RWLock) s2sl.get(sample);
            if (pLock != null) {
                return pLock.tryWrite();
            } else
                throw new NoSuchSampleException(sample);
        }
    }

    public SampleObject[] getSampleRW(SampleContext sc, Integer readSample, Integer writeSample) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException {
        if (!assertContext(sc))
            throw new NoSuchContextException();

        if (readSample == null || writeSample == null)
            throw new NoSuchSampleException(IntPool.get(Integer.MIN_VALUE));

        SampleObject[] sobjs = new SampleObject[2];

        if (readSample.intValue() < writeSample.intValue()) {
            sobjs[0] = getSampleRead(sc, readSample);
            try {
                sobjs[1] = getSampleWrite(sc, writeSample);
            } catch (NoSuchSampleException e) {
                unlockSample(readSample);
                throw e;
            } catch (SampleEmptyException e) {
                unlockSample(readSample);
                throw e;
            }
        } else {
            sobjs[1] = getSampleWrite(sc, writeSample);
            try {
                sobjs[0] = getSampleRead(sc, readSample);
            } catch (NoSuchSampleException e) {
                unlockSample(writeSample);
                throw e;
            } catch (SampleEmptyException e) {
                unlockSample(writeSample);
                throw e;
            }
        }

        return sobjs;
    }

    public Object[] getSampleRC(SampleContext sc, Integer readSample, Integer copySample) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException {
        if (!assertContext(sc))
            throw new NoSuchContextException();

        if (readSample == null || copySample == null)
            throw new NoSuchSampleException(IntPool.get(Integer.MIN_VALUE));

        Object[] sobjs = new Object[2];

        // access them consecutively to prevent race conditions
        if (readSample.intValue() < copySample.intValue()) {
            sobjs[0] = getSampleRead(sc, readSample);
            try {
                sobjs[1] = getSampleObjectWrite(sc, copySample);
            } catch (NoSuchSampleException e) {
                unlockSample(readSample);
                throw e;
            }
        } else {
            sobjs[1] = getSampleObjectWrite(sc, copySample);
            try {
                sobjs[0] = getSampleRead(sc, readSample);
            } catch (NoSuchSampleException e) {
                unlockSample(copySample);
                throw e;
            } catch (SampleEmptyException e) {
                unlockSample(copySample);
                throw e;
            }
        }

        return sobjs;
    }

    public void unlockSample(Integer sample) {
        ((RWLock) s2sl.get(sample)).unlock();
    }

    public SampleObject getSampleRead(final SampleContext sc, final Integer sample) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException {
        lockSampleRead(sc, sample);
        final Object pobj = s2sobj.get(sample);
        Object r_pobj = pobj;
        Thread rt;
        if ((pobj instanceof EmptySample))
            try {
                throw new SampleEmptyException(sample);
            } finally {
                unlockSample(sample);
            }
        else if ((pobj instanceof UninitSampleObject)) {
            try {
                rt = (Thread) s2rt.get(sample);
                if (rt == null) {
                    rt = new ZDBModifyThread() {
                        public void run() {
                            try {
                                refreshSample(sc, sample);
                            } catch (NoSuchContextException e) {
                            } catch (NoSuchSampleException e) {
                            }
                        }
                    };
                    rt.start();
                }
            } finally {
                unlockSample(sample);
            }
        } else
            return (SampleObject) r_pobj;

        if (SwingUtilities.isEventDispatchThread())
            throw new NoSuchSampleException(sample);
        else
            while (rt.isAlive())
                try {
                    rt.join();
                } catch (InterruptedException e) {
                }
        return getSampleRead(sc, sample);
    }

    public SampleObject getSampleWrite(final SampleContext sc, final Integer sample) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException {
        lockSampleWrite(sc, sample);
        final Object pobj = s2sobj.get(sample);
        Object r_pobj = pobj;
        Thread rt;
        if ((pobj instanceof EmptySample))
            try {
                throw new SampleEmptyException(sample);
            } finally {
                unlockSample(sample);
            }
        else if ((pobj instanceof UninitSampleObject)) {
            try {
                rt = (Thread) s2rt.get(sample);
                if (rt == null) {
                    rt = new ZDBModifyThread("Refresh Sample") {
                        public void run() {
                            try {
                                refreshSample(sc, sample);
                            } catch (NoSuchContextException e) {
                            } catch (NoSuchSampleException e) {
                            }
                        }
                    };
                    rt.start();
                }
            } finally {
                unlockSample(sample);
            }
        } else
            return (SampleObject) r_pobj;

        // if (SwingUtilities.isEventDispatchThread())
        //   throw new NoSuchSampleException(sample);
        // else
        while (rt.isAlive())
            try {
                rt.join();
            } catch (InterruptedException e) {
            }
        return getSampleWrite(sc, sample);
    }

    public String tryGetSampleSummary(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        if (tryLockSampleRead(sc, sample) == false)
            return SUMMARY_SAMPLE_WRITE_LOCKED;
        try {
            Object sobj = s2sobj.get(sample);

            if ((sobj instanceof EmptySample))
                return SUMMARY_SAMPLE_EMPTY;
            else if (sobj instanceof UninitSampleObject) {
                if (sample.intValue() > DeviceContext.MAX_USER_SAMPLE)
                    return SUMMARY_SAMPLE_ROM;
                else
                    return SUMMARY_SAMPLE_NOT_INITIALIZED;
            } else if (sobj == null)
                throw new NoSuchSampleException(sample);

            if (sobj instanceof SampleObject)
                return ((SampleObject) sobj).getSummary();

            return SUMMARY_ERROR;
        } finally {
            unlockSample(sample);
        }
    }

    // was getSampleCopy
    public Object getSampleObjectWrite(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        lockSampleWrite(sc, sample);
        Object sobj = s2sobj.get(sample);
        return sobj;
    }

    public int getSampleState(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        if (s2sc.get(sample) != sc || sc == null)
            throw new NoSuchContextException();

        Object sobj = s2sobj.get(sample);
        if (sobj == null)
            throw new NoSuchSampleException(sample);
        if (sobj instanceof UninitSampleObject)
            return ((UninitSampleObject) sobj).getState();
        if (sobj instanceof EmptySample)
            return RemoteObjectStates.STATE_EMPTY;
        return RemoteObjectStates.STATE_INITIALIZED;
    }

    public double getInitializationStatus(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        return scf.getSampleInitializationStatus(sample);
    }

    public boolean isSampleWriteLocked(SampleContext sc, Integer sample) throws NoSuchSampleException, NoSuchContextException {
        if (s2sc.get(sample) != sc || sc == null)
            throw new NoSuchContextException();

        RWLock pLock = (RWLock) s2sl.get(sample);
        if (pLock == null)
            throw new NoSuchSampleException(sample);
        return pLock.isWriteLocked();
    }

    private void createContext(SampleContext sc, SampleContext npc) {
        sc2s.put(npc, new TreeMap());
        sc2sc.put(npc, new HashMap());
        sc2parentsc.put(npc, sc);
    }

    private void transferSample(SampleContext src, SampleContext dest, Integer sample) {
        // remove from child context
        ((Map) sc2s.get(src)).remove(sample);
        // place in dest context
        ((Map) sc2s.get(dest)).put(sample, null);
        // remap SampleContext for sample
        s2sc.put(sample, dest);
    }

    private void removeSampleFrom(SampleContext src, Integer sample) {
        // remove from child context
        ((Map) sc2s.get(src)).remove(sample);
    }

    private void addSampleTo(SampleContext dest, Integer sample) {
        // place in dest context
        ((Map) sc2s.get(dest)).put(sample, null);
        // remap SampleContext for sample
        s2sc.put(sample, dest);
    }

    private SampleContext getParent(SampleContext sc) {
        return (SampleContext) sc2parentsc.get(sc);
    }

    private boolean releaseSample(SampleContext sc, Integer sample) {
        SampleContext parent = getParent(sc);
        if (parent == null)
            return false;

        transferSample(sc, parent, sample);

        return true;
    }

    private boolean releaseAllSamples(SampleContext sc) {
        SampleContext parent = getParent(sc);
        if (parent == null)
            return false;

        TreeMap samples = (TreeMap) sc2s.get(sc);
        Iterator i = ((TreeMap) samples.clone()).keySet().iterator(); // this must be cloned because we will updating this very table in transferSample - otherwise we would get a ConcurrentModificationException from the key set
        Integer sample;
        for (; i.hasNext();) {
            sample = (Integer) i.next();
            transferSample(sc, parent, sample);
        }
        return true;
    }

    private boolean assertContext(SampleContext sc) {
        if (!sc2s.containsKey(sc))
            return false;
        return true;
    }

    public List findEmptySamples(SampleContext sc, int reqd) throws NoSuchContextException {
        return findEmptySamples(sc, reqd, IntPool.get(1), IntPool.get(Integer.MAX_VALUE));
    }

    public List findEmptySamples(SampleContext sc, int reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException {
        if (beginIndex.intValue() == 0)
            beginIndex = IntPool.get(1);

        ArrayList found = new ArrayList();
        Integer s;
        //int count = reqd.intValue();
        TreeMap m = (TreeMap) sc2s.get(sc);
        if (m == null)
            throw new NoSuchContextException();
        TreeMap pClone = (TreeMap) m.clone();

        for (Iterator i = pClone.keySet().iterator(); i.hasNext();) {
            s = (Integer) i.next();
            try {
                if (s.intValue() >= beginIndex.intValue() && s.intValue() <= maxIndex.intValue() && getSampleState(sc, s) == RemoteObjectStates.STATE_EMPTY) {
                    found.add(s);
                    if (--reqd < 1)
                        break;
                }
            } catch (NoSuchSampleException e) {
                // throw new IllegalStateException(this.getClass().toString() + ":findEmptySamplesInContext-> isEmpty returned NoSuchSampleException - database corrupt?");
            }
        }
        return found;
    }

    public class Impl_SampleEventHandler implements Serializable, SampleEventHandler, ZDisposable {
        transient private WeakHashMap sampleListeners = new WeakHashMap();

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            sampleListeners = new WeakHashMap();
        }

        public Impl_SampleEventHandler() {
        }

        public void zDispose() {
            sampleListeners.clear();
        }

        public void start() {
        }

        public void stop() {
        }

      public void addSampleListener(SampleListener sl, Integer sample) {
            synchronized (sampleListeners) {
                ArrayList listeners = (ArrayList) sampleListeners.get(sample);
                if (listeners == null) {
                    listeners = new ArrayList();
                    sampleListeners.put(sample, listeners);
                }
                listeners.add(sl);
            }
        }

        public void removeSampleListener(SampleListener sl, Integer sample) {
            synchronized (sampleListeners) {
                ArrayList listeners = (ArrayList) sampleListeners.get(sample);
                if (listeners == null)
                    return;
                listeners.remove(sl);
            }
        }

        public void postSampleEvent(final SampleEvent ev) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ArrayList listeners;
                    synchronized (sampleListeners) {
                        listeners = (ArrayList) sampleListeners.get(ev.getSample());
                        if (listeners == null)
                            return;
                        for (int i = 0, j = listeners.size(); i < j; i++)
                            try {
                                ev.fire((SampleListener) listeners.get(i));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        }
    }
}



