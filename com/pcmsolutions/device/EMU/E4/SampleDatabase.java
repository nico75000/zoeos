package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleRefreshEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.requests.SampleRequestEvent;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.ROMLocation;
import com.pcmsolutions.device.EMU.database.*;
import com.pcmsolutions.device.EMU.database.events.content.EventHandlerFactory;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.StandardStateMachine;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.tasking.ManageableTicketedQ;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;
import com.pcmsolutions.gui.ProgressCallback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;

// PresteDatabseProxy also happens to be the database by way of implementing PDBReader and PDBWriter
// please device samples in agreed direction which is deemed ascending - this prevents race conditions on samples locks
// PDBReader:getSampleRW does this very job with two samples

class SampleDatabase extends AbstractDatabase<DatabaseSample, IsolatedSample, SampleContext, SampleEvent, SampleRequestEvent, SampleListener> implements ZDisposable, StandardStateMachine, java.io.Serializable {
    private static final String SUMMARY_SAMPLE_EMPTY = "Empty Sample";
    private static final String SUMMARY_NOINFO = "== no info ==";
    private static final String SUMMARY_SAMPLE_WRITE_LOCKED = "== sample locked ==";

    protected E4Device device;

    protected int maxSampleIndex;

    private PresetDatabase presetDatabase;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

    public void init(E4Device device, PresetDatabase pdb, String name, int maxIndex) {
        this.device = device;
        this.presetDatabase = pdb;
        this.maxSampleIndex = maxIndex;
        super.init(name);
        for (int i = 0; i <= maxSampleIndex; i++)
            addDBObject(new SampleDBO(IntPool.get(i), getEventHandler()));
    }

    public int getNumberOfInstalledSampleRoms() {
        return (getDBCount() - DeviceContext.MAX_USER_SAMPLE) / 1000;
    }

    public SampleContext createNewContext() {
        return new Impl_SampleContext(device, "sampleContext", this);
    }

    public PresetDatabase getPresetDatabase() {
        return presetDatabase;
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

    public void initializeAllSampleData() {
        initializeUserSampleData();
        initializeRomSampleData();
    }

    public void initializeUserSampleData() {
        initializeSpecifiedSampleData(0, DeviceContext.BASE_ROM_SAMPLE / 2);
        initializeSpecifiedSampleData(DeviceContext.BASE_ROM_SAMPLE / 2, DeviceContext.BASE_ROM_SAMPLE - DeviceContext.BASE_ROM_SAMPLE / 2);
    }

    public void initializeRomSampleData() {
        initializeSpecifiedSampleData(DeviceContext.BASE_ROM_SAMPLE, getDBCount() - DeviceContext.BASE_ROM_SAMPLE);
    }

    public void initializeSpecifiedSampleData(final int lowIndex, final int count) {
        if (lowIndex + count > getDBCount())
            throw new IllegalArgumentException("sample index out of range");

        int[] indexes = new int[count];

        for (int ti = 0, i = lowIndex, j = lowIndex + count; i < j; i++, ti++)
            indexes[ti] = i;

        initializeSpecifiedSampleData(indexes);
    }

    private void initializeSpecifiedSampleData(final int[] indexes) {
        final ManageableTicketedQ initPassWorkerQ = newWorkerQueue("Sample initialization worker", 7);
        initPassWorkerQ.start();
        try {
            for (int n = 0, o = indexes.length; n < o; n++) {
                final Integer sample = IntPool.get(indexes[n]);
                initPassWorkerQ.getPostableTicket(new TicketRunnable() {
                    public void run() throws Exception {
                        try {
                            access();
                            try {
                                if (!isInitialized(sample))
                                    refresh(sample);
                            } finally {
                                release();
                            }
                        } catch (Exception e) {
                        }
                    }
                }, "sample initialization " + n).post();
            }
        } catch (ResourceUnavailableException e) {
        } finally {
        }
    }

    public void eraseUserForBankErase() {
        Iterator<DBO<DatabaseSample, IsolatedSample>> i = getDBOHeadIterator(IntPool.get(DeviceContext.BASE_ROM_SAMPLE));
        while (i.hasNext()) {
            DBO next = i.next();
            if (next.getIndex().intValue() == 0)
                next.restoreRawContent(null);
            else
                next.restoreRawContent(SampleDBO.EMPTY);
            getEventHandler().postEvent(new SampleRefreshEvent(this, next.getIndex()));
        }
    }

    void loadRomSnapshot(Object[] snap) {
        Iterator<DBO<DatabaseSample, IsolatedSample>> it = getDBOTailIterator(IntPool.get(DeviceContext.BASE_ROM_SAMPLE));
        for (int i = 0; it.hasNext(); i++) {
            if (i >= snap.length)
                break;
            it.next().restoreRawContent(snap[i]);
        }
    }

    Object[] getRomSnapshot() {
        ArrayList rom = new ArrayList();
        Iterator<DBO<DatabaseSample, IsolatedSample>> i = getDBOTailIterator(IntPool.get(DeviceContext.BASE_ROM_SAMPLE));
        while (i.hasNext()) {
            DBO next = i.next();
            try {
                rom.add(new ROMLocation(next.getIndex(), next.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rom.toArray();
    }

    public void uninitializeUser() {
        Iterator<DBO<DatabaseSample, IsolatedSample>> i = getDBOHeadIterator(IntPool.get(DeviceContext.BASE_ROM_SAMPLE));
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
        Iterator<DBO<DatabaseSample, IsolatedSample>> i = getDBOTailIterator(IntPool.get(DeviceContext.BASE_ROM_SAMPLE));
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

    public ManageableContentEventHandler<SampleEvent, SampleRequestEvent, SampleListener> createEventHandler() {
        return (ManageableContentEventHandler<SampleEvent, SampleRequestEvent, SampleListener>)EventHandlerFactory.createContentEventHandler(10000);
    }

    public DatabaseSample getFreeContent() {
        return null;
    }

    public String tryGetSampleSummary(SampleContext sc, Integer sample) throws NoSuchContextIndexException, NoSuchContextException {

        if (isWriteLocked(sample) == true)
            return SUMMARY_SAMPLE_WRITE_LOCKED;
        try {
            DatabaseSample s = reqRead(sc, sample);
            try {
                return s.getSummary();
            } finally {
                releaseReadContent(sample);
            }
        } catch (EmptyException e) {
            return SUMMARY_SAMPLE_EMPTY;
        } catch (ContentUnavailableException e) {
        }
        return SUMMARY_NOINFO;
    }   
}


