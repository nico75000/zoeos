package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.threads.ZBackgroundRemoterThread;
import com.pcmsolutions.system.threads.ZRemoteDumpThread;
import com.pcmsolutions.system.threads.ZWaitThread;
import com.pcmsolutions.util.CALock;
import com.pcmsolutions.util.IntegerUseMap;
import com.pcmsolutions.util.RWLock;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

// PresteDatabseProxy also happens to be the database by way of implementing PDBReader and PDBWriter
// please device samples in agreed direction which is deemed ascending - this prevents race conditions on samples locks
// PDBReader:getPresetRW does this very job with two samples

class PresetDatabase implements ZDisposable, StandardStateMachine, PresetDatabaseProxy, PDBWriter, PDBReader, Serializable, RemoteAssignable {
    private static final String SUMMARY_PRESET_EMPTY = "Preset Empty";
    private static final String SUMMARY_PRESET_NOT_INITIALIZED = "USER Preset (Uninitialized)";
    private static final String SUMMARY_PRESET_FLASH = "FLASH Preset (Uninitialized)";
    private static final String SUMMARY_ERROR = "== no info ==";
    private static final String SUMMARY_PRESET_WRITE_LOCKED = "== preset locked ==";

    private static final int PRESET_LOAD = 2000;

    protected String name;

    protected DeviceParameterContext deviceParameterContext;

    // HashMaps beacause these are only modfified under a device configure lock
    protected HashMap pc2p = new HashMap();

    protected HashMap pc2pc = new HashMap();

    protected HashMap p2pc = new HashMap(PRESET_LOAD);

    protected HashMap pc2parentpc = new HashMap();

    // Hashtables beacause these can modified under a multi-client device access lock
    protected Hashtable p2pl = new Hashtable(PRESET_LOAD);

    Hashtable p2pobj = new Hashtable(PRESET_LOAD);

    transient protected Hashtable p2rt;  // preset to refresh thread

    transient protected Hashtable presetInitializationMonitors;

    protected PresetContext rootPresetContext;

    protected Impl_PresetEventHandler pe_handler;

    protected PresetContextFactory presetContextFactory;

    protected CALock deviceLock;

    protected int maxPresetIndex;

    transient protected Vector workerThreads;

    private final StateMachineHelper sm = new StateMachineHelper();

    private SampleDatabaseProxy sampleDatabaseProxy;

    public PresetDatabase(String name, int maxIndex) {
        this.name = name;
        this.maxPresetIndex = maxIndex;
        buildTransients();
    }

    private void buildTransients() {
        p2rt = new Hashtable(PRESET_LOAD);
        presetInitializationMonitors = new Hashtable();
        workerThreads = new Vector();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        buildTransients();
    }

    public void setRemote(Remotable r) {
        Object pc;
        for (Iterator i = pc2p.keySet().iterator(); i.hasNext();) {
            pc = i.next();
            if (pc instanceof RemoteAssignable)
                ((RemoteAssignable) pc).setRemote(r);
        }
    }

    public void init(DeviceParameterContext dpc, PresetContextFactory pcf, CALock device, SampleDatabaseProxy sdbp) {
        this.deviceParameterContext = dpc;
        this.presetContextFactory = pcf;
        this.deviceLock = device;
        this.rootPresetContext = pcf.newPresetContext("Default Preset Context", (PresetDatabaseProxy) this);
        this.pe_handler = new Impl_PresetEventHandler();
        this.sampleDatabaseProxy = sdbp;
    }

    public SampleContext getRootSampleContext() {
        return sampleDatabaseProxy.getRootContext();
    }

    public void setDeviceLock(CALock deviceLock) {
        this.deviceLock = deviceLock;
    }

    public void zDispose() {
        pe_handler.zDispose();
    }

    public static Integer[] parseDumpStream(ByteArrayInputStream dis, ParameterContext pc) {
        Set setIds = pc.getIds();
        return parseDumpStream(dis, pc, setIds.size());
    }

    public static Integer[] parseDumpStream(ByteArrayInputStream dis, ParameterContext pc, int num) {
        Set setIds = pc.getIds();
        if (num > setIds.size())
            throw new IllegalArgumentException("too ids for parameter context");
        int nBytes = num * 2;
        Integer[] arrIds = (Integer[]) setIds.toArray(new Integer[setIds.size()]);

        Integer[] idVals = new Integer[nBytes];
        byte[] dumpField = new byte[nBytes];
        dis.read(dumpField, 0, nBytes);

        for (int n = 0; n < nBytes; n += 2) {
            idVals[n] = arrIds[n / 2];
            idVals[n + 1] = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn(dumpField, n);
        }
        return idVals;
    }

    public static Integer[] parseDumpStream(ByteArrayInputStream dis, Integer[] arrIds) {
        int nBytes = arrIds.length * 2;

        Integer[] idVals = new Integer[nBytes];
        byte[] dumpField = new byte[nBytes];
        dis.read(dumpField, 0, nBytes);

        for (int n = 0; n < nBytes; n += 2) {
            idVals[n] = arrIds[n / 2];
            idVals[n + 1] = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn(dumpField, n);
        }
        return idVals;
    }

    public void stateInitial() throws IllegalStateTransitionException {
        deviceLock.configure();
        try {
            if (sm.testTransition(sm.STATE_INITIALIZED) == sm.STATE_INITIALIZED)
                return;
            // create entries for new root
            pc2p.put(rootPresetContext, new TreeMap());
            pc2pc.put(rootPresetContext, new HashMap());
            pc2parentpc.put(rootPresetContext, null);
            sm.transition(STATE_INITIALIZED);
        } finally {
            deviceLock.unlock();
        }
    }

    public void stateStart() throws IllegalStateTransitionException {
        deviceLock.configure();
        try {
            if (sm.transition(sm.STATE_STARTED) == sm.STATE_STARTED)
                return;
            pe_handler.start();
        } finally {
            deviceLock.unlock();
        }
    }

    public void stateStop() throws IllegalStateTransitionException {
        deviceLock.configure();
        try {
            if (sm.transition(sm.STATE_STOPPED) == sm.STATE_STOPPED)
                return;
            pe_handler.stop();
            stopWorkerThreads();
        } finally {
            deviceLock.unlock();
        }
    }

    public Thread[] stopWorkerThreads() {
        deviceLock.configure();
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
            deviceLock.unlock();
        }
    }

    public int getState() {
        return sm.getState();
    }

    public void initializeAllPresetNames(boolean chainToData) {
        initializeUserPresetNames(chainToData);
        initializeFlashPresetNames(chainToData);
        //initializeSpecifiedPresetNames(0, getDBCount(), chainToData);
    }

    public void initializeUserPresetNames(boolean chainToData) {
        initializeSpecifiedPresetNames(0, DeviceContext.BASE_FLASH_PRESET, chainToData);
    }

    public void initializeFlashPresetNames(boolean chainToData) {
        initializeSpecifiedPresetNames(DeviceContext.BASE_FLASH_PRESET, getDBCount() - DeviceContext.BASE_FLASH_PRESET, chainToData);
    }

    public void initializeSpecifiedPresetNames(final int lowIndex, final int count, final boolean chainToData) {
        if (lowIndex + count > getDBCount())
            throw new IllegalArgumentException("preset index out of range");

        int[] indexes = new int[count];

        for (int ti = 0,i = lowIndex, j = lowIndex + count; i < j; i++, ti++)
            indexes[ti] = i;

        initializeSpecifiedPresetNames(indexes, chainToData);
    }

    // assumes that all indexes are valid!
    private void initializeSpecifiedPresetNames(final int[] indexes, final boolean chainToData) {
        // if (true)
        //   return;
        ZWaitThread t = new ZBackgroundRemoterThread() {
            public void run() {
                this.setName("PresetNamePass");
                Integer preset;
                for (int n = 0, o = indexes.length; n < o; n++) {
                    deviceLock.access();
                    try {
                        if (this.alive == false)
                            return;
                        preset = IntPool.get(indexes[n]);
                        assertPresetNamed(rootPresetContext, preset);
                    } catch (NoSuchPresetException e) {
                    } catch (NoSuchContextException e) {
                    } finally {
                        deviceLock.unlock();
                    }
                }
                if (chainToData)
                    initializeSpecifiedPresetData(indexes);
            }
        };
        workerThreads.add(t);
        t.start();
    }

    public void initializeAllPresetData() {
        initializeSpecifiedPresetData(0, getDBCount());
    }

    public void initializeUserPresetData() {
        initializeSpecifiedPresetData(0, DeviceContext.BASE_FLASH_PRESET);
    }

    public void initializeSpecifiedPresetData(final int lowIndex, final int count) {
        if (lowIndex + count > getDBCount())
            throw new IllegalArgumentException("preset index out of range");

        int[] indexes = new int[count];

        for (int ti = 0,i = lowIndex, j = lowIndex + count; i < j; i++, ti++)
            indexes[ti] = i;

        initializeSpecifiedPresetData(indexes);
    }

    // assumes that all indexes are valid!
    private void initializeSpecifiedPresetData(final int[] indexes) {
        ZWaitThread t = new ZRemoteDumpThread() {
            public void run() {
                this.setName("PresetDataPass");
                Integer preset;
                for (int n = 0, o = indexes.length; n < o; n++) {
                    deviceLock.access();
                    try {
                        if (this.alive == false) {
                            return;
                        }
                        preset = IntPool.get(indexes[n]);
                        try {
                            if (!isPresetInitialized(preset))
                                refreshPreset(rootPresetContext, preset);
                        } catch (NoSuchPresetException e) {
                        } catch (NoSuchContextException e) {
                        }
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }
        };
        workerThreads.add(t);
        t.start();
    }

    public void loadPresetObjects(Map presetObjects) {
        for (Iterator i = presetObjects.keySet().iterator(); i.hasNext();)
            addObjectToDB(presetObjects.get(i.next()));
    }

    public void addObjectToDB(Object o) {
        Integer index = IntPool.get(size());
        addObjectToDB(o, index);
    }

    public void addObjectToDB(Object o, Integer index) {
        if (o instanceof PresetObject)
            addObjectToDB((PresetObject) o, index);
        else if (o instanceof UninitPresetObject)
            addObjectToDB((UninitPresetObject) o, index);
        else if (o instanceof EmptyPreset)
            addObjectToDB((EmptyPreset) o, index);
        else
            throw new IllegalArgumentException("Not a valid object for the preset database");
    }

    private void addObjectToDB(PresetObject p, Integer index) {
        finalizeAddObjectToDB(new PresetObject(p, index, pe_handler, deviceParameterContext), index);
    }

    private void addObjectToDB(UninitPresetObject p, Integer index) {
        p.setPEH(pe_handler);
        finalizeAddObjectToDB(p, index);
    }

    private void addObjectToDB(EmptyPreset ep, Integer index) {
        finalizeAddObjectToDB(ep, index);
    }

    private void finalizeAddObjectToDB(Object p, Integer index) {
        p2pl.put(index, new RWLock());
        p2pobj.put(index, p);
        addIndexToContext(index, rootPresetContext);
    }

    private void addIndexToContext(Integer index, PresetContext context) {
        p2pc.put(index, context);
        ((Map) pc2p.get(context)).put(index, null);
    }

    public void eraseUser() {
        deviceLock.configure();
        try {
            Object o;
            Integer preset;
            for (int i = 0,j = DeviceContext.MAX_USER_PRESET; i <= j; i++) {
                preset = IntPool.get(i);
                if (i == 0)
                    o = p2pobj.put(preset, new UninitPresetObject(preset));
                else
                    o = p2pobj.put(IntPool.get(i), EmptyPreset.getInstance());

                if (o != null && o instanceof ZDisposable)
                    ((ZDisposable) o).zDispose();
                pe_handler.postPresetEvent(new PresetRefreshEvent(this, preset));
            }
        } finally {
            deviceLock.unlock();
        }
    }

    public void uninitializeUser() {
        deviceLock.configure();
        try {
            Object o;
            Integer preset;
            for (int i = 0,j = DeviceContext.MAX_USER_PRESET; i <= j; i++) {
                preset = IntPool.get(i);
                o = p2pobj.put(preset, new UninitPresetObject(preset));
                if (o != null && o instanceof ZDisposable)
                    ((ZDisposable) o).zDispose();
                pe_handler.postPresetEvent(new PresetRefreshEvent(this, preset));
            }
        } finally {
            deviceLock.unlock();
        }
    }

    private int size() {
        return p2pl.size();
    }

    public String toString() {
        return name;
    }

    public PresetEventHandler getPresetEventHandler() {
        return pe_handler;
    }

    public void addPresetListener(PresetListener pl, Integer[] presets) {
        int count = presets.length;
        for (int n = 0; n < count; n++)
            pe_handler.addPresetListener(pl, presets[n]);

    }

    public void removePresetListener(PresetListener pl, Integer[] presets) {
        int count = presets.length;
        for (int n = 0; n < count; n++)
            pe_handler.removePresetListener(pl, presets[n]);
    }

    public PDBWriter getDBWrite() {
        deviceLock.configure();
        return this;
    }

    public PDBReader getDBRead() {
        deviceLock.access();
        return this;
    }

    public void release() {
        deviceLock.unlock();
    }

    public Set getReadablePresets() {
        Set rp;
        deviceLock.access();
        try {
            rp = ((Hashtable) p2pobj.clone()).keySet();
        } finally {
            deviceLock.unlock();
        }
        if (rp == null)
            rp = new HashMap().keySet();
        return rp;
    }

    public void releaseContext(PresetContext pc) throws NoSuchContextException {
        if (!assertContext(pc))
            throw new NoSuchContextException();

        HashMap subContextMap = (HashMap) pc2pc.get(pc);

// release subcontexts
        Iterator i = subContextMap.keySet().iterator();
        for (; i.hasNext();)
            releaseContext((PresetContext) subContextMap.get(i.next()));

// release samples
        if (releaseAllPresets(pc)) {
// remove this PresetContext
            pc2p.remove(pc);
            pc2pc.remove(pc);
            pc2parentpc.remove(pc);
        }
    }

    public void releaseContextPreset(PresetContext pc, Integer preset) throws NoSuchContextException, NoSuchPresetException {
        if (!assertContext(pc))
            throw new NoSuchContextException();

        Map presets = (Map) pc2p.get(pc);
        if (!presets.containsKey(preset))
            throw new NoSuchPresetException(preset);

        releasePreset(pc, preset);
    }

    public PresetContext newContext(PresetContext pc, String name, Integer[] presets) throws NoSuchPresetException, NoSuchContextException {
        if (!assertContext(pc))
            throw new NoSuchContextException();

        PresetContext npc;
        for (int n = 0; n < presets.length; n++)
            if (!hasPreset(pc, presets[n]))
                throw new NoSuchPresetException(presets[n]);

        npc = presetContextFactory.newPresetContext(name, this);
        createContext(pc, npc);

        for (int n = 0; n < presets.length; n++)
            transferPreset(pc, npc, presets[n]);

        return npc;
    }

    public void removePresetsFromContext(PresetContext src, Integer[] presets) throws NoSuchContextException, NoSuchPresetException {
        if (!assertContext(src)) {
            throw new NoSuchContextException();
        }
        for (int n = 0; n < presets.length; n++)
            if (!hasPreset(src, presets[n]))
                throw new NoSuchPresetException(presets[n]);
        for (int n = 0; n < presets.length; n++)
            removePresetFrom(src, presets[n]);
    }

    public void addPresetsToContext(PresetContext dest, Integer[] presets) throws NoSuchContextException, NoSuchPresetException {
        if (!assertContext(dest)) {
            throw new NoSuchContextException();
        }
        for (int n = 0; n < presets.length; n++)
            if (!readsPreset(dest, presets[n]))
                throw new NoSuchPresetException(presets[n]);
        for (int n = 0; n < presets.length; n++)
            addPresetTo(dest, presets[n]);
    }

    public List expandContextWithEmptyPresets(PresetContext src, PresetContext dest, Integer reqd) throws NoSuchContextException {
        if (!assertContext(src) || !assertContext(dest)) {
            throw new NoSuchContextException();
        }
        List found = findEmptyPresets(src, reqd);
        for (int n = 0; n < found.size(); n++)
            transferPreset(src, dest, (Integer) found.get(n));

        return found;
    }

    public int getDBCount() {
        return p2pl.size();
    }

    public IntegerUseMap getSampleIndexesInUseForUserPresets(PresetContext pc) throws NoSuchContextException {
        return getSampleIndexesInUseForPresetRange(pc, 0, DeviceContext.MAX_USER_PRESET);
    }

    public IntegerUseMap getSampleIndexesInUseForFlashPresets(PresetContext pc) throws NoSuchContextException {
        return getSampleIndexesInUseForPresetRange(pc, DeviceContext.MAX_USER_PRESET + 1, getDBCount() - 1);
    }

    public IntegerUseMap getSampleIndexesInUseForAllPresets(PresetContext pc) throws NoSuchContextException {
        return getSampleIndexesInUseForPresetRange(pc, 0, getDBCount() - 1);
    }

    private IntegerUseMap getSampleIndexesInUseForPresetRange(PresetContext pc, int lowPreset, int highPreset) throws NoSuchContextException {
        IntegerUseMap useMap = new IntegerUseMap();
        PresetObject p;
        Integer preset;

        Set s = getReadablePresetIndexes(pc);
        ArrayList targetPresets = new ArrayList(1000);
        for (Iterator i = s.iterator(); i.hasNext();) {
            preset = (Integer) i.next();
            if (preset.intValue() >= lowPreset && preset.intValue() <= highPreset)
                targetPresets.add(preset);
        }

        int size = targetPresets.size();
        Zoeos.getInstance().beginProgressElement(this, "Determining unreferenced samples", size);
        try {
            for (int i = 0,j = targetPresets.size(); i < j; i++) {
                preset = (Integer) targetPresets.get(i);
                try {
                    p = getPresetRead(pc, preset);
                    try {
                        useMap.mergeUseMap(p.referencedSampleUsage());
                    } finally {
                        unlockPreset(preset);
                    }
                } catch (NoSuchPresetException e) {
                } catch (PresetEmptyException e) {
                } catch (NoSuchContextException e) {
                }
                Zoeos.getInstance().updateProgressElement(this);
            }
        } finally {
            Zoeos.getInstance().endProgressElement(this);
        }
        return useMap;
    }

    public boolean seesPreset(PresetContext pc, Integer preset) {
        boolean rv;

        if (pc == null)
            rv = false;
        else if (p2pc.get(preset) != pc)
            return seesPreset((PresetContext) pc2parentpc.get(pc), preset);
        else
            rv = true;

        return rv;
    }

    public boolean readsPreset(PresetContext pc, Integer preset) {

        if (pc == null || !pc2p.containsKey(pc))
            return false;

        if (p2pobj.containsKey(preset))
            return true;

        return false;
    }

    public boolean isPresetInitialized(Integer preset) throws NoSuchPresetException {
        Object o = p2pobj.get(preset);
        if (o == null)
            throw new NoSuchPresetException(preset);
        if (o instanceof UninitPresetObject)
            return false;
        return true;
    }

    public boolean hasPreset(PresetContext pc, Integer preset) {
        if (pc != null && p2pc.get(preset) == pc)
            return true;

        return false;
    }

    public PresetContext getRootContext() {
        return rootPresetContext;
    }

    public String getPresetName(Integer preset) throws NoSuchPresetException {
        Object pobj = p2pobj.get(preset);
        if (pobj == null)
            throw new NoSuchPresetException(preset);
        return pobj.toString();
    }

    public void assertPresetNamed(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        lockPresetWrite(pc, preset);
        try {
            Object pobj = p2pobj.get(preset);
            UninitPresetObject u_pobj;
            if (pobj == null)
                throw new NoSuchPresetException(preset);
            if (pobj instanceof UninitPresetObject) {
                u_pobj = ((UninitPresetObject) pobj);
                if (u_pobj.getState() == RemoteObjectStates.STATE_PENDING) {
                    name = presetContextFactory.initializePresetNameAtIndex(preset, pe_handler);
                    if (name == null)
                        return;
                    if (name.trim().equals(DeviceContext.EMPTY_PRESET)) {
                        p2pobj.put(preset, EmptyPreset.getInstance());
                        pe_handler.postPresetEvent(new PresetInitializeEvent(this, preset));
                    } else {
                        u_pobj.setName(name);
                        pe_handler.postPresetEvent(new PresetNameChangeEvent(this, preset));
                    }
                }
            }
        } finally {
            unlockPreset(preset);
        }
    }

    public String getPresetNameExtended(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        lockPresetWrite(pc, preset);
        try {
            String name;
            Object pobj = p2pobj.get(preset);
            if (pobj == null)
                throw new NoSuchPresetException(preset);
            if (pobj instanceof UninitPresetObject) {
                name = presetContextFactory.initializePresetNameAtIndex(preset, pe_handler);
                if (name != null)
                    if (name.equals(DeviceContext.EMPTY_PRESET))
                        p2pobj.put(preset, EmptyPreset.getInstance());
                    else
                        ((UninitPresetObject) pobj).setName(name);
            }
            return p2pobj.get(preset).toString();
        } finally {
            unlockPreset(preset);
        }
    }

    public void setPresetName(PresetContext pc, Integer preset, String name) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException {
        lockPresetWrite(pc, preset);
        try {
            Object pobj = p2pobj.get(preset);

            if ((pobj instanceof UninitPresetObject) && ((UninitPresetObject) pobj).getState() == RemoteObjectStates.STATE_NAMED) {
                ((UninitPresetObject) pobj).setName(name);
                return;
            }
            PresetObject p_obj = getPresetWrite(pc, preset);
            try {
                p_obj.setName(name);
            } finally {
                unlockPreset(preset);
            }
        } finally {
            unlockPreset(preset);
        }
    }

    public void refreshPreset(PresetContext pc, Integer preset) throws NoSuchContextException, NoSuchPresetException {
        lockPresetWrite(pc, preset);
        Object n_pobj = null;
        UninitPresetObject upo;
        try {
            //synchronized (p2rt) {
            Thread rt;
            rt = (Thread) p2rt.get(preset);
            if (rt != null)
                return;
            p2rt.put(preset, Thread.currentThread());
            // }
            /* } finally {
                 unlockPreset(preset);
             }

             lockPresetWrite(pc, preset);
             try {
             */
            upo = new UninitPresetObject(preset, getPresetName(preset));
            upo.setPEH(pe_handler);
            upo.markInitializing();
            Object o = p2pobj.put(preset, upo);
            if (o instanceof ZDisposable)
                ((ZDisposable) o).zDispose();
            pe_handler.postPresetEvent(new PresetInitializationStatusChangedEvent(this, preset, 0));

        } finally {
            unlockPreset(preset);
        }

        try {
            n_pobj = presetContextFactory.initializePresetAtIndex(preset, pe_handler);
        } finally {
            lockPresetWrite(pc, preset);
            try {
                if (n_pobj != null) {
                    Object o = p2pobj.put(preset, n_pobj);
                    if (o != null && o instanceof ZDisposable)
                        ((ZDisposable) o).zDispose();
                    pe_handler.postPresetEvent(new PresetInitializeEvent(this, preset));
                } else {
                    //UninitPresetObject upo = new UninitPresetObject(preset);
                    //upo.setSEH(pe_handler);
                    upo.markPending();
                    // Object o = p2pobj.put(preset, upo);
                    //if (o instanceof ZDisposable)
                    //   ((ZDisposable) o).zDispose();
                    pe_handler.postPresetEvent(new PresetInitializationStatusChangedEvent(this, preset));
                }
            } finally {
                //synchronized (p2rt) {
                if (p2rt.get(preset) == Thread.currentThread())
                    p2rt.remove(preset);
                //}
                unlockPreset(preset);
            }
        }
    }

    public Set getPresetIndexesInContext(PresetContext pc) throws NoSuchContextException {
        if (!assertContext(pc))
            throw new NoSuchContextException();

        Set rv;
        rv = ((Map) ((TreeMap) pc2p.get(pc)).clone()).keySet();
        return rv;
    }

    public Set getReadablePresetIndexes(PresetContext pc) throws NoSuchContextException {
        if (!assertContext(pc))
            throw new NoSuchContextException();
        TreeMap sortMap = new TreeMap();
        sortMap.putAll(p2pl);
        return sortMap.keySet();
    }

    public Map getPresetNamesInContext(PresetContext pc) throws NoSuchContextException {
        Set presets = getPresetIndexesInContext(pc);
        TreeMap rv = new TreeMap();
        Iterator i = presets.iterator();
        Integer p;
        String name;
        while (i.hasNext()) {
            p = (Integer) i.next();
            try {
                name = getPresetName(p);
                rv.put(p, name);
            } catch (NoSuchPresetException e) {
                rv.put(p, null);
            }
        }
        return rv;
    }

    public void changePresetObject(PresetContext pc, Integer preset, Object pobj) throws NoSuchPresetException, NoSuchContextException {
        lockPresetWrite(pc, preset);
        try {
            Object o = p2pobj.put(preset, pobj);
            if (o != null && o instanceof ZDisposable)
                ((ZDisposable) o).zDispose();
            pe_handler.postPresetEvent(new PresetRefreshEvent(this, preset));
        } finally {
            unlockPreset(preset);
        }
    }

    public void lockPresetRead(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        if (pc == null)
            throw new NoSuchContextException();

        RWLock pLock = (RWLock) p2pl.get(preset);

        if (pLock != null)
            pLock.read();
        else
            throw new NoSuchPresetException(preset);
    }

    public boolean tryLockPresetRead(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        if (pc == null)
            throw new NoSuchContextException();

        RWLock pLock = (RWLock) p2pl.get(preset);
        if (pLock != null) {
            return pLock.tryRead();
        } else
            throw new NoSuchPresetException(preset);
    }

    public void lockPresetWrite(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        if (pc == null)
            throw new NoSuchContextException();

        if (p2pc.get(preset) != pc)
            lockPresetWrite(getParent(pc), preset);
        else {
            RWLock pLock = (RWLock) p2pl.get(preset);
            if (pLock != null)
                pLock.write();
            else
                throw new NoSuchPresetException(preset);
        }
    }

    public boolean tryLockPresetWrite(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        if (pc == null)
            throw new NoSuchContextException();

        if (p2pc.get(preset) != pc)
            return tryLockPresetWrite(getParent(pc), preset);
        else {
            RWLock pLock = (RWLock) p2pl.get(preset);
            if (pLock != null) {
                return pLock.tryWrite();
            } else
                throw new NoSuchPresetException(preset);
        }
    }

    public PresetObject[] getPresetRW(PresetContext pc, Integer readPreset, Integer writePreset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        if (!assertContext(pc))
            throw new NoSuchContextException();

        if (readPreset == null || writePreset == null)
            throw new NoSuchPresetException(IntPool.get(Integer.MIN_VALUE));

        PresetObject[] pobjs = new PresetObject[2];

        if (readPreset.intValue() < writePreset.intValue()) {
            pobjs[0] = getPresetRead(pc, readPreset);
            try {
                pobjs[1] = getPresetWrite(pc, writePreset);
            } catch (NoSuchPresetException e) {
                unlockPreset(readPreset);
                throw e;
            } catch (PresetEmptyException e) {
                unlockPreset(readPreset);
                throw e;
            }
        } else {
            pobjs[1] = getPresetWrite(pc, writePreset);
            try {
                pobjs[0] = getPresetRead(pc, readPreset);
            } catch (NoSuchPresetException e) {
                unlockPreset(writePreset);
                throw e;
            } catch (PresetEmptyException e) {
                unlockPreset(writePreset);
                throw e;
            }
        }

        return pobjs;
    }

    public Object[] getPresetRC(PresetContext pc, Integer readPreset, Integer copyPreset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        if (!assertContext(pc))
            throw new NoSuchContextException();

        if (readPreset == null || copyPreset == null)
            throw new NoSuchPresetException(IntPool.get(Integer.MIN_VALUE));

        Object[] pobjs = new Object[2];

// access them consecutively to prevent race conditions
        if (readPreset.intValue() < copyPreset.intValue()) {
            pobjs[0] = getPresetRead(pc, readPreset);
            try {
                pobjs[1] = getPresetObjectWrite(pc, copyPreset);
            } catch (NoSuchPresetException e) {
                unlockPreset(readPreset);
                throw e;
            }
        } else {
            pobjs[1] = getPresetObjectWrite(pc, copyPreset);
            try {
                pobjs[0] = getPresetRead(pc, readPreset);
            } catch (NoSuchPresetException e) {
                unlockPreset(copyPreset);
                throw e;
            } catch (PresetEmptyException e) {
                unlockPreset(copyPreset);
                throw e;
            }
        }

        return pobjs;
    }

    public void unlockPreset(Integer preset) {
        ((RWLock) p2pl.get(preset)).unlock();
    }

    public PresetObject getPresetRead(final PresetContext pc, final Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return taskGetPresetRead(pc, preset, 1);
    }

    private PresetObject taskGetPresetRead(final PresetContext pc, final Integer preset, final int retries) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        lockPresetRead(pc, preset);
        final Object pobj = p2pobj.get(preset);
        Object r_pobj = pobj;
        Thread rt;
        if ((pobj instanceof EmptyPreset))
            try {
                throw new PresetEmptyException(preset);
            } finally {
                unlockPreset(preset);
            }
        else if ((pobj instanceof UninitPresetObject)) {
            try {
                // synchronized (p2rt) {
                rt = (Thread) p2rt.get(preset);
                if (rt == null) {
                    //       p2rt.put(preset, rt);
                    rt = new ZRemoteDumpThread("refresh preset " + preset) {
                        public void run() {
                            try {
                                refreshPreset(pc, preset);
                            } catch (NoSuchContextException e) {
                            } catch (NoSuchPresetException e) {
                            }
                        }
                    };
                    rt.start();
                    // }
                }
                if (retries == 0 || SwingUtilities.isEventDispatchThread())
                    throw new NoSuchPresetException(preset);
            } finally {
                unlockPreset(preset);
            }
        } else
            return (PresetObject) r_pobj;

/* if (SwingUtilities.isEventDispatchThread())
     throw new NoSuchPresetException(preset);
 else
     while (rt.isAlive())
         try {
             rt.join();
         } catch (InterruptedException e) {
         }
 return getPresetRead(pc, preset);
 */
        while (rt.isAlive())
            try {
                rt.join();
            } catch (InterruptedException e) {
            }
        return taskGetPresetRead(pc, preset, retries - 1);
    }

// Swing Event Dispatch Thread should never enter here!
    public PresetObject getPresetWrite(final PresetContext pc, final Integer preset) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        return taskGetPresetWrite(pc, preset, 1);
    }

    private PresetObject taskGetPresetWrite(final PresetContext pc, final Integer preset, final int retries) throws NoSuchPresetException, PresetEmptyException, NoSuchContextException {
        lockPresetWrite(pc, preset);
        final Object pobj = p2pobj.get(preset);
        Object r_pobj = pobj;
        Thread rt;
        if ((pobj instanceof EmptyPreset))
            try {
                throw new PresetEmptyException(preset);
            } finally {
                unlockPreset(preset);
            }
        else if ((pobj instanceof UninitPresetObject)) {
            try {
                //synchronized (p2rt) {
                rt = (Thread) p2rt.get(preset);
                if (rt == null) {
                    //p2rt.put(preset, rt);
                    rt = new ZRemoteDumpThread("Refresh Preset") {
                        public void run() {
                            try {
                                refreshPreset(pc, preset);
                            } catch (NoSuchContextException e) {
                            } catch (NoSuchPresetException e) {
                            }
                        }
                    };
                    rt.start();
                    //  }
                }
                if (retries == 0)
                    throw new NoSuchPresetException(preset);
            } finally {
                unlockPreset(preset);
            }
        } else
            return (PresetObject) r_pobj;

        while (rt.isAlive())
            try {
                rt.join();
            } catch (InterruptedException e) {
            }

        return taskGetPresetWrite(pc, preset, retries - 1);
    }

    public String tryGetPresetSummary(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        if (tryLockPresetRead(pc, preset) == false)
            return SUMMARY_PRESET_WRITE_LOCKED;
        try {
            Object pobj = p2pobj.get(preset);

            if ((pobj instanceof EmptyPreset))
                return SUMMARY_PRESET_EMPTY;
            else if (pobj instanceof UninitPresetObject) {
                if (preset.intValue() > DeviceContext.MAX_USER_PRESET)
                    return SUMMARY_PRESET_FLASH;
                else
                    return SUMMARY_PRESET_NOT_INITIALIZED;
            } else if (pobj == null)
                throw new NoSuchPresetException(preset);

            if (pobj instanceof PresetObject)
                return ((PresetObject) pobj).getSummary();

            return SUMMARY_ERROR;
        } finally {
            unlockPreset(preset);
        }
    }

// was getPresetCopy
    public Object getPresetObjectWrite(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        lockPresetWrite(pc, preset);
        return p2pobj.get(preset);
    }

    public int getPresetState(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        if (p2pc.get(preset) != pc || pc == null)
            throw new NoSuchContextException();

        Object pobj = p2pobj.get(preset);
        if (pobj == null)
            throw new NoSuchPresetException(preset);
        if (pobj instanceof UninitPresetObject)
            return ((UninitPresetObject) pobj).getState();
        if (pobj instanceof EmptyPreset)
            return RemoteObjectStates.STATE_EMPTY;
        return RemoteObjectStates.STATE_INITIALIZED;
    }

    public boolean remoteInitializePresetAtIndex(Integer index, ByteArrayInputStream is) {
        return presetContextFactory.remoteInitializePresetAtIndex(index, pe_handler, is);
    }

    public double getInitializationStatus(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        return presetContextFactory.getPresetInitializationStatus(preset);
    }

    public boolean isPresetWriteLocked(PresetContext pc, Integer preset) throws NoSuchPresetException, NoSuchContextException {
        if (p2pc.get(preset) != pc || pc == null)
            throw new NoSuchContextException();

        RWLock pLock = (RWLock) p2pl.get(preset);
        if (pLock == null)
            throw new NoSuchPresetException(preset);
        return pLock.isWriteLocked();
    }

    private void createContext(PresetContext pc, PresetContext npc) {
        pc2p.put(npc, new TreeMap());
        pc2pc.put(npc, new HashMap());
        pc2parentpc.put(npc, pc);
    }

    private void transferPreset(PresetContext src, PresetContext dest, Integer preset) {
        // remove from child context
        ((Map) pc2p.get(src)).remove(preset);
// place in dest context
        ((Map) pc2p.get(dest)).put(preset, null);
// remap PresetContext for preset
        p2pc.put(preset, dest);
    }

    private void removePresetFrom(PresetContext src, Integer preset) {
        // remove from child context
        ((Map) pc2p.get(src)).remove(preset);
    }

    private void addPresetTo(PresetContext dest, Integer preset) {
        // place in dest context
        ((Map) pc2p.get(dest)).put(preset, null);
// remap PresetContext for preset
        p2pc.put(preset, dest);
    }

    private PresetContext getParent(PresetContext pc) {
        return (PresetContext) pc2parentpc.get(pc);
    }

    private boolean releasePreset(PresetContext pc, Integer preset) {
        PresetContext parent = getParent(pc);
        if (parent == null)
            return false;

        transferPreset(pc, parent, preset);

        return true;
    }

    private boolean releaseAllPresets(PresetContext pc) {
        PresetContext parent = getParent(pc);
        if (parent == null)
            return false;

        TreeMap presets = (TreeMap) pc2p.get(pc);
        Iterator i = ((TreeMap) presets.clone()).keySet().iterator(); // this must be cloned because we will updating this very table in transferPreset - otherwise we would get a ConcurrentModificationException from the key set
        Integer preset;
        for (; i.hasNext();) {
            preset = (Integer) i.next();
            transferPreset(pc, parent, preset);
        }
        return true;
    }

    private boolean assertContext(PresetContext pc) {
        if (!pc2p.containsKey(pc))
            return false;
        return true;
    }

    public List findEmptyPresets(PresetContext pc, Integer reqd) throws NoSuchContextException {
        return findEmptyPresets(pc, reqd, IntPool.get(1), IntPool.get(Integer.MAX_VALUE));
    }

    public List findEmptyPresets(PresetContext pc, Integer reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException {
        if (beginIndex.intValue() == 0)
            beginIndex = IntPool.get(1);

        ArrayList found = new ArrayList();
        Integer p;
        int count = reqd.intValue();
        TreeMap pClone = (TreeMap) ((TreeMap) pc2p.get(pc)).clone();

        for (Iterator i = pClone.keySet().iterator(); i.hasNext();) {
            p = (Integer) i.next();
            try {
                if (p.intValue() >= beginIndex.intValue() && p.intValue() <= maxIndex.intValue() && getPresetState(pc, p) == RemoteObjectStates.STATE_EMPTY) {
                    found.add(p);
                    if (--count < 1)
                        break;
                }
            } catch (NoSuchPresetException e) {
            }
        }
        return found;
    }

    public class Impl_PresetEventHandler implements Serializable, PresetEventHandler, ZDisposable {
        transient private WeakHashMap presetListeners = new WeakHashMap();

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            presetListeners = new WeakHashMap();
        }

        public Impl_PresetEventHandler() {
        }

        public void zDispose() {
            presetListeners.clear();
        }

        public void start() {
        }

        public void stop() {
        }

        public void addPresetListener(PresetListener pl, Integer preset) {
            synchronized (presetListeners) {
                ArrayList listeners = (ArrayList) presetListeners.get(preset);
                if (listeners == null) {
                    listeners = new ArrayList();
                    presetListeners.put(preset, listeners);
                }
                listeners.add(pl);
            }
        }

        public void removePresetListener(PresetListener pl, Integer preset) {
            synchronized (presetListeners) {
                ArrayList listeners = (ArrayList) presetListeners.get(preset);
                if (listeners == null)
                    return;
                listeners.remove(pl);
            }
        }

        public void postPresetEvent(final PresetEvent ev) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ArrayList listeners;
                    synchronized (presetListeners) {
                        listeners = (ArrayList) presetListeners.get(ev.getPreset());
                        if (listeners == null)
                            return;
                        for (int i = 0, j = listeners.size(); i < j; i++)
                            try {
                                ev.fire((PresetListener) listeners.get(i));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        }
    }
}



