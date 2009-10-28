package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.requests.SampleRequestEvent;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.database.AbstractContext;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.NoSuchContextIndexException;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.tasking.*;

import javax.sound.sampled.AudioFileFormat;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 14:28:02
 */
class Impl_SampleContext extends AbstractContext<ReadableSample, DatabaseSample, IsolatedSample, SampleContext, SampleEvent, SampleRequestEvent, SampleListener> implements SampleContext, Serializable {

    protected E4Device device;
    protected String name;
    protected SampleDatabase db;
    protected transient ManageableTicketedQ scq;

    public Impl_SampleContext(E4Device device, String name, SampleDatabase db) {
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
        scq = QueueFactory.createTicketedQueue(this, "sampleContext", 6);
        scq.start();
    }

    public TicketedQ getContextQ() {
        return scq;
    }

    public String getSampleSummary(Integer sample) throws DeviceException {
        db.access();
        try {
            return db.tryGetSampleSummary(this, sample);
        } finally {
            db.release();
        }
    }

    // returns null for ROM samples or non-SMDI linked devices
    public SampleDescriptor getSampleDescriptor(Integer sample) throws DeviceException, ContentUnavailableException, EmptyException {
        db.access();
        try {
            DatabaseSample s = db.getRead(this, sample);
            try {
                return s.getSampleDescriptor();
            } finally {
                db.releaseReadContent(sample);
            }
        } finally {
            db.release();
        }
    }

    // returns List of ContextReadableSample/ReadableSample ( e.g FLASH/ROM samples returned as ReadableSample)
    public List<ContextReadableSample> getContextSamples() throws DeviceException {
        ArrayList<ContextReadableSample> outList = new ArrayList<ContextReadableSample>();
        db.access();
        try {
            Set<Integer> indexes = this.getIndexesInContext();
            Integer s;
            for (Iterator<Integer> i = indexes.iterator(); i.hasNext();) {
                s = i.next();
                if (this.containsIndex(s))
                    if (s.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                        outList.add(new Impl_ContextEditableSample(this, s));
                    else
                        outList.add(new Impl_ContextReadableSample(this, s));
            }
            return outList;
        } finally {
            db.release();
        }
    }

    // returns List of ContextEditableSample
    public List<ContextEditableSample> getContextEditableSamples() throws DeviceException {
        ArrayList<ContextEditableSample> outList = new ArrayList<ContextEditableSample>();
        db.access();
        try {
            Set<Integer> indexes = getIndexesInContext();
            Integer s;
            for (Iterator<Integer> i = indexes.iterator(); i.hasNext();) {
                s = i.next();
                if (this.containsIndex(s) && s.intValue() != 0)
                    if (s.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                        outList.add(new Impl_ContextEditableSample(this, s));
            }
            return outList;
        } finally {
            db.release();
        }
    }

    // returns List of ReadableSample or better
    // e.g FLASH/ROM and out of context samples returned as ReadableSample
    // possibly more derived than ReadableSample if sample is in context etc... )
    public List<ReadableSample> getDatabaseSamples() throws DeviceException {
        ArrayList<ReadableSample> outList = new ArrayList<ReadableSample>();
        db.access();
        try {
            Set<Integer> indexes = db.getDBIndexes(this);
            for (Iterator<Integer> i = indexes.iterator(); i.hasNext();)
                outList.add(new Impl_ReadableSample(this, i.next()));
            return outList;
        } finally {
            db.release();
        }
    }

    /*private Impl_ContextReadableSample getContextSampleImplementation(Integer sample) throws DeviceException {
        if (isSampleInContext(sample))
            return new Impl_ContextReadableSample(this, sample);

        throw new DeviceException(sample);
    }

    private Impl_ReadableSample getReadableSampleImplementation(Integer sample) throws DeviceException {
        SDBReader getReader = sdbp.getDBRead();
        try {
            if (getReader.readsSample(this, sample))
                return new Impl_ReadableSample(this, sample);
            else
                throw new DeviceException(sample);
        } finally {
            getReader.release();
        }
    } */

    public ReadableSample getContextItemForIndex(Integer index) throws DeviceException {
        if (containsIndex(index))
            if (index.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                return new Impl_ContextEditableSample(this, index);
            else
                return new Impl_ContextReadableSample(this, index);
        else
            throw new NoSuchContextIndexException(index);
    }

    public ContextReadableSample getContextSample(Integer sample) throws DeviceException {
        if (containsIndex(sample))
            if (sample.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                return new Impl_ContextEditableSample(this, sample);
            else
                return new Impl_ContextReadableSample(this, sample);
        else
            throw new NoSuchContextIndexException(sample);
    }

    public ReadableSample getReadableSample(Integer sample) throws DeviceException {
        if (containsIndex(sample))
            return getContextSample(sample);
        else if (db.readsIndex(this, sample))
            return new Impl_ReadableSample(this, sample);
        else
            throw new NoSuchContextIndexException(sample);
    }

    // TODO!! fix semantics of this to handle FLASH samples that cannot be returned as ContextEditableSample
    public ContextEditableSample getEditableSample(Integer sample) throws DeviceException {
        if (containsIndex(sample))
            return new Impl_ContextEditableSample(this, sample);

        throw new NoSuchContextIndexException(sample);
    }

    public Map<Integer, String> getContextUserNamesMap() throws DeviceException {
        Map<Integer, String> names = this.getContextNamesMap();
        Iterator<Integer> it = names.keySet().iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            if (i.intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                it.remove();
        }
        return names;
    }

    public DeviceParameterContext getDeviceParameterContext() throws DeviceException {
        return device.getDeviceParameterContext();
    }

    public DeviceContext getDeviceContext() {
        return device;
    }

    public PresetContext getRootPresetContext() {
        return db.getPresetDatabase().getRootContext();
    }

    public TicketedQ getRefreshQ() {
        return device.queues.refreshQ();
    }

    public IsolatedSample getIsolated(SampleDownloadDescriptor sdd) throws DeviceException, ContentUnavailableException, EmptyException {
        db.access();
        try {
            return db.getIsolatedContent(sdd.getIndex(), sdd);
        } finally {
            db.release();
        }
    }

    // pass SampleDownloadDescriptor as flags
    public IsolatedSample getIsolated(Integer index, Object flags) throws DeviceException, ContentUnavailableException, EmptyException {
        db.access();
        try {
            return db.getIsolatedContent(index, flags);
        } finally {
            db.release();
        }
    }

    public Ticket newContent(final IsolatedSample is, final Integer sample, final String name, final ProgressCallback prog) {
        return getContextQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    db.newContent(Impl_SampleContext.this, sample, name, new SampleDBO.NewSampleContentFlags() {
                        public IsolatedSample getIsolatedSample() {
                            return is;
                        }

                        public ProgressCallback getProgressCallback() {
                            return prog;
                        }
                    });
                } catch (Exception e) {
                    prog.updateProgress(1);
                    throw e;
                } finally {
                    db.release();
                }
            }
        }, "newContent");
    }

    public Ticket copy(final Integer srcIndex, final Integer[] destIndexes, final ProgressCallback prog) {
        return getContextQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                try {
                    String name = getName(srcIndex);
                    HashSet<Integer> destSet = new HashSet<Integer>();
                    destSet.addAll(Arrays.asList(destIndexes));
                    ProgressCallback[] progs = prog.splitTask(destSet.size() + 1, true);
                    int progIndex = 0;
                    IsolatedSample is = null;
                    try {
                        is = getIsolated(SampleDownloadDescriptorFactory.getDownloadToTempFile(srcIndex, AudioFileFormat.Type.AIFF));
                        is.assertSample(progs[progIndex++]);
                        db.access();
                        try {
                            for (Integer di : destSet)
                                db.dropContent(Impl_SampleContext.this, is, di, name, progs[progIndex++]);
                        } finally {
                            db.release();
                        }
                    } catch (Exception e) {
                        prog.updateProgress(1);
                        if (is != null)
                            is.zDispose();
                    }
                } catch (Exception e) {
                    prog.updateProgress(1);
                    throw e;
                }
            }
        }, "copy");
    }
}
