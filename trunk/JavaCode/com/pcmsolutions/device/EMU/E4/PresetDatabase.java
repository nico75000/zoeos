package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.requests.PresetRequestEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.requests.PresetRequestInitializationStatusEvent;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.database.*;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;
import com.pcmsolutions.device.EMU.database.events.content.EventHandlerFactory;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.tasking.ManageableTicketedQ;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

class PresetDatabase extends AbstractDatabase<DatabasePreset, IsolatedPreset, PresetContext, PresetEvent, PresetRequestEvent, PresetListener> implements ZDisposable, Serializable {

    private static final String SUMMARY_PRESET_EMPTY = "Preset empty";
    private static final String NO_SUMMARY_USER_PRESET = "USER preset  [no info]";
    private static final String NO_SUMMARY_PRESET_FLASH = "FLASH preset [no info]";
    private static final String SUMMARY_PRESET_WRITE_LOCKED = "== preset locked ==";

    protected DeviceParameterContext deviceParameterContext;

    protected E4Device device;

    protected int maxPresetIndex;

    private SampleDatabase sampleDatabase;

    public void init(DeviceParameterContext dpc, E4Device device, SampleDatabase sdb, String name, int maxIndex) {
        this.device = device;
        this.deviceParameterContext = dpc;
        this.sampleDatabase = sdb;
        this.maxPresetIndex = maxIndex;
        super.init(name);

        for (int i = 0; i <= maxIndex; i++)
            addDBObject(new PresetDBO(IntPool.get(i), getEventHandler(), dpc));
    }

    public SampleContext getRootSampleContext() {
        return sampleDatabase.getRootContext();
    }

    public void setDevice(E4Device device) {
        this.device = device;
    }

    public Impl_PresetContext createNewContext() {
        return new Impl_PresetContext(device, "", this);
    }

    public PresetContext getRootContext() {
        return super.getRootContext();
    }

    public void access() throws DeviceException {
        device.deviceLock.access();
    }

    public void configure() throws DeviceException {
        device.deviceLock.configure();
    }

    public void release() {
        device.deviceLock.unlock();
    }

    public ManageableContentEventHandler<PresetEvent, PresetRequestEvent, PresetListener> createEventHandler() {
        return (ManageableContentEventHandler<PresetEvent, PresetRequestEvent, PresetListener>)EventHandlerFactory.createContentEventHandler(10000);
    }

    public DatabasePreset getFreeContent() {
        PresetObject pobj = new PresetObject();
        pobj.initNew(IntPool.zero, DeviceContext.UNTITLED_PRESET, PresetDBO.dummyCEH, deviceParameterContext);
        return pobj;
    }

    public void initializeAllPresetNames(boolean chainToData) {
        initializeUserPresetNames(chainToData);
        initializeFlashPresetNames(chainToData);
        //initializeSpecifiedPresetNames(0, getDBSize(), chainToData);
    }

    public void initializeUserPresetNames(boolean chainToData) {
        initializeSpecifiedPresetNames(0, DeviceContext.BASE_FLASH_PRESET / 2, chainToData);
        initializeSpecifiedPresetNames(DeviceContext.BASE_FLASH_PRESET / 2, DeviceContext.BASE_FLASH_PRESET - DeviceContext.BASE_FLASH_PRESET / 2, chainToData);
    }

    public void initializeFlashPresetNames(boolean chainToData) {
        initializeSpecifiedPresetNames(DeviceContext.BASE_FLASH_PRESET, getDBCount() - DeviceContext.BASE_FLASH_PRESET, chainToData);
    }

    public void initializeSpecifiedPresetNames(final int lowIndex, final int count, final boolean chainToData) {
        if (lowIndex + count > getDBCount())
            throw new IllegalArgumentException("preset index out of range");

        int[] indexes = new int[count];

        for (int ti = 0, i = lowIndex, j = lowIndex + count; i < j; i++, ti++)
            indexes[ti] = i;

        initializeSpecifiedPresetNames(indexes, chainToData);
    }

    // assumes that all indexes are valid!
    private void initializeSpecifiedPresetNames(final int[] indexes, final boolean chainToData) {
        final ManageableTicketedQ namesPassWorkerQ = newWorkerQueue("Preset naming worker", 6);
        namesPassWorkerQ.start();
        try {
            for (int n = 0, o = indexes.length; n < o; n++) {
                final Integer preset = IntPool.get(indexes[n]);
                namesPassWorkerQ.getPostableTicket(new TicketRunnable() {
                    public void run() throws Exception {
                        try {
                            access();
                            try {
                                assertNamed(preset);
                            } finally {
                                release();
                            }
                        } catch (Exception e) {
                        }
                        Thread.yield();
                    }
                }, "preset name " + n).post();
            }
        } catch (ResourceUnavailableException e) {
        } finally {
            if (chainToData)
                initializeSpecifiedPresetData(indexes);
        }
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

        for (int ti = 0, i = lowIndex, j = lowIndex + count; i < j; i++, ti++)
            indexes[ti] = i;

        initializeSpecifiedPresetData(indexes);
    }

    // assumes that all indexes are valid!
    private void initializeSpecifiedPresetData(final int[] indexes) {
        final ManageableTicketedQ initPassWorkerQ = newWorkerQueue("Preset initialization worker", 6);
        initPassWorkerQ.start();
        try {
            for (int n = 0, o = indexes.length; n < o; n++) {
                final Integer preset = IntPool.get(indexes[n]);
                initPassWorkerQ.getPostableTicket(new TicketRunnable() {
                    public void run() throws Exception {
                        try {
                            access();
                            try {
                                if (!isInitialized(preset))
                                    refresh(preset);
                            } finally {
                                release();
                            }
                        } catch (Exception e) {
                        }
                    }
                }, "preset initialization " + n).post();
            }
        } catch (ResourceUnavailableException e) {
        } finally {

        }
    }

    public void eraseUserForBankErase() {
        Iterator<DBO<DatabasePreset, IsolatedPreset>> i = getDBOHeadIterator(IntPool.get(DeviceContext.BASE_FLASH_PRESET));
        while (i.hasNext()) {
            DBO next = i.next();
            if (next.getIndex().intValue() == 0)
                next.restoreRawContent(null);
            else
                next.restoreRawContent(PresetDBO.EMPTY);
            getEventHandler().postEvent(new PresetInitializeEvent(this, next.getIndex()));
        }
    }

    public void uninitializeUser() {
        Iterator<DBO<DatabasePreset, IsolatedPreset>> i = getDBOHeadIterator(IntPool.get(DeviceContext.BASE_FLASH_PRESET));
        while (i.hasNext()) {
            try {
                uninitialize(i.next().getIndex());
            } catch (NoSuchContextIndexException e) {
                e.printStackTrace();
            } catch (NoSuchContextException e) {
                e.printStackTrace();
            }
        }
    }

    public void uninitializeFlash() {
        Iterator<DBO<DatabasePreset, IsolatedPreset>> i = getDBOTailIterator(IntPool.get(DeviceContext.BASE_FLASH_PRESET));
        while (i.hasNext()) {
            try {
                uninitialize(i.next().getIndex());
            } catch (NoSuchContextIndexException e) {
                e.printStackTrace();
            } catch (NoSuchContextException e) {
                e.printStackTrace();
            }
        }
    }

    public DatabasePreset getFreePreset() {
        PresetObject pobj = new PresetObject();
        pobj.initNew(IntPool.get(0), DeviceContext.UNTITLED_PRESET, PresetDBO.dummyCEH, deviceParameterContext);
        return pobj;
    }

    void loadFlashSnapshot(Object[] snap) {
        Iterator<DBO<DatabasePreset, IsolatedPreset>> it = getDBOTailIterator(IntPool.get(DeviceContext.BASE_FLASH_PRESET));
        for (int i = 0; it.hasNext(); i++) {
            if (i >= snap.length)
                break;
            it.next().restoreRawContent(snap[i]);
        }
    }

    Object[] getFlashSnapshot() {
        ArrayList flash = new ArrayList();
        Iterator<DBO<DatabasePreset, IsolatedPreset>> i = getDBOTailIterator(IntPool.get(DeviceContext.BASE_FLASH_PRESET));
        while (i.hasNext()) {
            DBO next = i.next();
            try {
                Object o = retrieveRawContent(next.getIndex());
                if (o instanceof IsolatedPreset)
                    flash.add(PackageFactory.makeSerializableIsolatedPreset((IsolatedPreset) o));
                else
                    flash.add(o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flash.toArray();
    }

    public String tryGetPresetSummary(PresetContext pc, Integer preset) throws NoSuchContextException, NoSuchContextIndexException {
        if (isWriteLocked(preset) == true)
            return SUMMARY_PRESET_WRITE_LOCKED;
        try {
            DatabasePreset p = reqRead(pc, preset);
            try {
                return p.getSummary();
            } finally {
                releaseReadContent(preset);
            }
        } catch (EmptyException e) {
            return SUMMARY_PRESET_EMPTY;
        } catch (ContentUnavailableException e) {
            if (preset.intValue() > DeviceContext.MAX_USER_PRESET)
                return NO_SUMMARY_PRESET_FLASH;
            else
                return NO_SUMMARY_USER_PRESET;
        }
    }

    public double getInitializationStatus(PresetContext pc, Integer preset) throws NoSuchContextException {
        PresetRequestInitializationStatusEvent pris = new PresetRequestInitializationStatusEvent(this, preset);
        if (getEventHandler().sendRequest(pris))
            return pris.getRequestedData().doubleValue();
        else
            return STATUS_INITIALIZED;
    }
}



