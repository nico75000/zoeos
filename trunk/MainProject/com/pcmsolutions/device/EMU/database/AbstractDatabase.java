package com.pcmsolutions.device.EMU.database;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.events.content.*;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.tasking.ManageableTicketedQ;
import com.pcmsolutions.system.tasking.QueueFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 23-Jul-2004
 * Time: 18:09:03
 */
public abstract class AbstractDatabase <C extends Content, IC, CX extends Context ,CE extends ContentEvent,RE extends ContentRequestEvent, CL extends ContentListener> implements Serializable, ZDisposable, StandardStateMachine {
    public static final double STATUS_INITIALIZED = Integer.MIN_VALUE;

    public interface DBO <C,IC> extends Comparable<DBO>, Serializable, ZDisposable {
        public static final ContentEventHandler dummyCEH = new ContentEventHandler() {
            public void postEvent(ContentEvent event) {

            }

            public void postEvent(ContentEvent event, boolean externalFirst) {

            }

            public void postInternalEvent(ContentEvent event) {

            }

            public void postExternalEvent(ContentEvent event) {

            }

            public void sendInternalEvent(ContentEvent event) throws Exception {

            }

            public boolean sendRequest(ContentRequestEvent event) {
                return false;
            }

            public void sync() {

            }
        };

        public String getName() throws ContentUnavailableException, EmptyException;                                    // blocking with implicit lock and release

        public void assertInitialized() throws ContentUnavailableException;                                    // blocking with implicit lock and release

        public void refreshContent() throws EmptyException, ContentUnavailableException;         // blocking, implicit lock and release

        public IC getIsolatedContent(Object flags) throws EmptyException, ContentUnavailableException;      // implicit lock and release

        public boolean isEmpty();

        public void setName(String name) throws EmptyException, ContentUnavailableException;

        public Integer getIndex();

        public boolean isInitialized();

        public boolean isPending();

        public boolean isInitializing();

        public boolean isWriteLocked();

        public void erase();

        public boolean remoteNameMismatch() throws ContentUnavailableException;

        public void uninitialize();

        public C requestRead() throws EmptyException, ContentUnavailableException;    // requires release

        public C getRead() throws EmptyException, ContentUnavailableException;    // requires release

        public void getCopy();    // requires release

        public C getWrite() throws EmptyException, ContentUnavailableException;   // requires release

        public void releaseReadContent();

        public void releaseWriteContent();

        public String toString();

        public Object retrieveRawContent();                          // implicit lock and release

        public void restoreRawContent(Object content);                          // implicit lock and release

        public void newContent(String name, Object flags) throws ContentUnavailableException;                                   // implicit lock and release

        public void dropContent(IC isoContent, String name, Object flags) throws ContentUnavailableException;                                   // implicit lock and release

        public void copyRawContent(C content, String name, Object flags);                          // implicit lock and release
    }

    private String name;

    private final SortedMap<Integer, DBO<C, IC>> dbi2dbo = new TreeMap<Integer, DBO<C, IC>>();

    private final HashMap<CX, SortedSet<Integer>> c2dbi = new HashMap<CX, SortedSet<Integer>>();

    private final HashMap<Integer, CX> dbi2c = new HashMap<Integer, CX>();

    private final HashMap<CX, Set<CX>> c2cc = new HashMap<CX, Set<CX>>();

    private final HashMap<CX, CX> c2pc = new HashMap<CX, CX>();

    private CX rootContext;

    private ManageableContentEventHandler<CE, RE, CL> eventHandler;

    transient private Vector<ManageableTicketedQ> workerQueues;

    private final StdStateMachineHelper sm = new StdStateMachineHelper(StdStates.STATE_INITIALIZED);

    public void init(String name) {
        this.name = name;
        rootContext = createNewContext();
        c2dbi.put(rootContext, createIntegerSet());
        c2cc.put(rootContext, createContextSet());
        c2pc.put(rootContext, null);
        buildTransients();
        eventHandler = createEventHandler();
    }

    public void stateInitial() throws IllegalStateTransitionException {
        sm.transition(StdStates.STATE_INITIALIZED);
    }

    public void stateStart() throws IllegalStateTransitionException {
        sm.transition(StdStates.STATE_STARTED);
    }

    public void stateStop() throws IllegalStateTransitionException {
        stopWorkerQueues();
        sm.transition(StdStates.STATE_STOPPED);
    }

    public void stopWorkerQueues() {
        synchronized (workerQueues) {
            Iterator<ManageableTicketedQ> i = workerQueues.iterator();
            while (i.hasNext())
                i.next().stop(false);
            workerQueues.clear();
        }
    }

    public final ManageableTicketedQ newWorkerQueue(String name, int priority) {
        ManageableTicketedQ q = QueueFactory.createTicketedQueue(this, name, priority);
        workerQueues.add(q);
        return q;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        buildTransients();
    }

    private void buildTransients() {
        workerQueues = new Vector<ManageableTicketedQ>();
    }

    public final int getState() {
        return sm.getState();
    }

    public String getName() {
        return name;
    }

    public void addContentListener(CL l, Integer[] indexes) {
        for (Integer i : indexes)
            eventHandler.addListener(l, i);
    }

    public void removeContentListener(CL l, Integer[] indexes) {
        for (Integer i : indexes)
            eventHandler.removeListener(l, i);
    }

    public ManageableContentEventHandler getEventHandler() {
        return eventHandler;
    }

    public abstract CX createNewContext();

    public abstract void access() throws DeviceException;

    public abstract void configure() throws DeviceException;

    public abstract void release();

    public abstract ManageableContentEventHandler<CE, RE, CL> createEventHandler();

    public abstract C getFreeContent();

    private Set<CX> createContextSet() {
        return new HashSet<CX>();
    }

    private SortedSet<Integer> createIntegerSet() {
        return new TreeSet<Integer>();
    }

    private Set<DBO<C, IC>> createDBOSet() {
        return new TreeSet<DBO<C, IC>>();
    }

    private void task_transferDBO(CX sc, CX dc, Integer dbi) {
        // remove from child context
        c2dbi.get(sc).remove(dbi);
        // place in dest context
        c2dbi.get(dc).add(dbi);
    }

    private void task_createContext(CX c, CX nc) {
        c2dbi.put(nc, createIntegerSet());
        c2cc.put(nc, createContextSet());
        c2pc.put(nc, c);
    }

    private CX getParentContext(CX c) {
        return c2pc.get(c);
    }

    private boolean task_releaseContextIndex(CX c, Integer dbi) {
        CX parent = getParentContext(c);
        if (parent == null)
            return false;
        task_transferDBO(c, parent, dbi);
        return true;
    }

    private boolean task_releaseContextDBOs(CX c) {
        CX pc = getParentContext(c);
        if (pc == null)
            return false;

        Iterator<Integer> i = c2dbi.get(c).iterator();
        while (i.hasNext())
            task_transferDBO(c, pc, i.next());

        return true;
    }

    protected void assertContext(CX c) throws NoSuchContextException {
        if (!c2dbi.containsKey(c))
            throw new NoSuchContextException();
    }

    protected void assertDBIndex(Integer dbi) throws NoSuchContextIndexException {
        if (!dbi2dbo.containsKey(dbi))
            throw new NoSuchContextIndexException(dbi);
    }

    protected void assertContextIndex(CX c, Integer dbi) throws NoSuchContextIndexException, NoSuchContextException {
        assertContext(c);
        if (!c2dbi.get(c).contains(dbi))
            throw new NoSuchContextIndexException(dbi);
    }

    protected void assertContextIndexes(CX c, Integer[] dbis) throws NoSuchContextIndexException, NoSuchContextException {
        for (int i = 0; i < dbis.length; i++)
            assertContextIndex(c, dbis[i]);
    }

    private void assertIsChildContext(CX pc, CX cc) throws NoSuchContextException {
        if (!c2cc.get(pc).contains(cc))
            throw new NoSuchContextException();
    }

    protected void addDBObject(DBO<C, IC> o) {
        initDBObjectAt(o, IntPool.get(getDBCount()));
    }

    protected void initDBObjectAt(DBO<C, IC> o, Integer index) {
        dbi2dbo.put(index, o);
        c2dbi.get(rootContext).add(index);
        dbi2c.put(index, rootContext);
        dbi2c.put(index, rootContext);
    }

    public CX getRootContext() {
        return rootContext;
    }

    public String getDBOString(CX c, Integer dbi) throws NoSuchContextIndexException, NoSuchContextException {
        assertContext(c);
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).toString();
    }

    public String getDBOName(CX c, Integer dbi) throws NoSuchContextIndexException, NoSuchContextException, ContentUnavailableException, EmptyException {
        assertContext(c);
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).getName();
    }

    public boolean remoteNameMismatch(Integer dbi) throws NoSuchContextIndexException, ContentUnavailableException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).remoteNameMismatch();
    }

    public SortedSet<Integer> findEmpties(CX c, int reqd) throws NoSuchContextException {
        return findEmpties(c, reqd, IntPool.zero, IntPool.MAX);
    }

    // maxIndex exclusive
    public SortedSet<Integer> findEmpties(CX c, int reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException {
        assertContext(c);
       // Integer max = IntPool.get(Math.min(AbstractDatabase.this.getDBCount(), maxIndex.intValue() + 1));
        Iterator<Integer> i = c2dbi.get(c).subSet(beginIndex, maxIndex).iterator();
        Integer index;
        SortedSet<Integer> found = new TreeSet<Integer>();
        while (i.hasNext()) {
            index = i.next();
            if (index.intValue() != 0)
                try {
                    if (isEmpty(index)) {
                        found.add(index);
                        if (--reqd < 1)
                            break;

                    }
                } catch (NoSuchContextIndexException e) {
                    e.printStackTrace();
                }
        }
        return found;
    }

    public SortedSet<Integer> getContextIndexes(CX c) throws NoSuchContextException {
        assertContext(c);
        return new TreeSet<Integer>(c2dbi.get(c));
    }

    public SortedSet<Integer> getDBIndexes(CX c) throws NoSuchContextException {
        assertContext(c);
        return new TreeSet<Integer>(dbi2dbo.keySet());
    }

    public void newContent(CX c, Integer dbi, String name, Object flags) throws NoSuchContextException, NoSuchContextIndexException, ContentUnavailableException {
        assertContextIndex(c, dbi);
        dbi2dbo.get(dbi).newContent(name, flags);
    }

    public void dropContent(CX c, IC content, Integer dbi, String name, Object flags) throws NoSuchContextException, NoSuchContextIndexException, ContentUnavailableException {
        assertContextIndex(c, dbi);
        dbi2dbo.get(dbi).dropContent(content, name, flags);
    }

    public void copyContent(CX c, Integer srcIndex, Integer destIndex, String name, Object flags) throws NoSuchContextException, NoSuchContextIndexException, ContentUnavailableException, EmptyException {
        assertContextIndex(c, destIndex);
        assertDBIndex(srcIndex);
        RCContent<C> rcc = getRC(c, srcIndex, destIndex);
        try {
            dbi2dbo.get(destIndex).copyRawContent(rcc.getReadable(), name, null);
        } finally {
            rcc.release();
        }
    }

    public int getContextSize(CX c) throws NoSuchContextException {
        assertContext(c);
        return c2dbi.get(c).size();
    }

    public SortedMap<Integer, String> getNamesMap(CX c) throws NoSuchContextException {
        assertContext(c);
        SortedMap<Integer, String> map = new TreeMap<Integer, String>();
        Iterator<Integer> it = getContextIndexes(c).iterator();
        Integer i;
        while (it.hasNext()) {
            i = it.next();
            map.put(i, dbi2dbo.get(i).toString());
        }
        return map;
    }

    public List<ContextLocation> getIndexedNamesInRange(CX c, Integer lowIndex, Integer highIndex) throws NoSuchContextException {
        assertContext(c);
        final Iterator<Integer> it = c2dbi.get(c).iterator();
        Integer i;
        final ArrayList<ContextLocation> names = new ArrayList<ContextLocation>();
        while (it.hasNext()) {
            i = it.next();
            if (ZUtilities.inRange(i, lowIndex, highIndex))
                names.add(new ContextLocation(i, dbi2dbo.get(i).toString()));
        }
        return names;
    }

    public int getDBCount() {
        return dbi2dbo.size();
    }

    public boolean isWriteLocked(Integer dbi) throws NoSuchContextIndexException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).isWriteLocked();
    }

    public void restoreRawContent(Integer dbi, Object content) throws NoSuchContextIndexException {
        assertDBIndex(dbi);
        dbi2dbo.get(dbi).restoreRawContent(content);
    }

    protected Object retrieveRawContent(Integer dbi) throws NoSuchContextIndexException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).retrieveRawContent();
    }

    protected Iterator<DBO<C, IC>> getDBOIterator() {
        return dbi2dbo.values().iterator();
    }

    public Set<Map.Entry<Integer, DBO<C, IC>>> getDBOEntrySet() {
        return dbi2dbo.entrySet();
    }

    protected Iterator<DBO<C, IC>> getDBOTailIterator(Integer fromIndex) {
        return dbi2dbo.tailMap(fromIndex).values().iterator();
    }

    protected Iterator<DBO<C, IC>> getDBOHeadIterator(Integer toIndex) {
        return dbi2dbo.headMap(toIndex).values().iterator();
    }

    protected Iterator<DBO<C, IC>> getDBOSubIterator(Integer fromIndex, Integer toIndex) {
        return dbi2dbo.subMap(fromIndex, toIndex).values().iterator();
    }

    public void releaseContext(CX c) throws NoSuchContextException {
        assertContext(c);

        // release subcontexts
        Iterator<CX> i = c2cc.get(c).iterator();
        while (i.hasNext())
            releaseContext(i.next());

        if (task_releaseContextDBOs(c)) {
            // remove this context
            c2dbi.remove(c);
            c2cc.remove(c);
            c2pc.remove(c);
        }
    }

    public boolean releaseContextIndex(CX c, Integer dbi) throws NoSuchContextException, NoSuchContextIndexException {
        assertContextIndex(c, dbi);
        return task_releaseContextIndex(c, dbi);
    }

    public CX newContext(CX pc, String name, Integer[] dbis) throws NoSuchContextIndexException, NoSuchContextException {
        assertContext(pc);
        assertContextIndexes(pc, dbis);
        CX nc = createNewContext();
        task_createContext(pc, nc);

        for (int n = 0; n < dbis.length; n++)
            task_transferDBO(pc, nc, dbis[n]);
        return nc;
    }

    public void uninitialize(Integer dbi) throws NoSuchContextIndexException, NoSuchContextException {
        assertDBIndex(dbi);
        dbi2dbo.get(dbi).uninitialize();
    }

    public C getRead(CX c, Integer dbi) throws NoSuchContextException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        try {
            assertContext(c);
            assertDBIndex(dbi);
            return dbi2dbo.get(dbi).getRead();
        } catch (ContentUnavailableException e) {
            /*
            if (SwingUtilities.isEventDispatchThread())
                try {
                    internalPresetDBOQ.getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
                            assertInitialized();
                        }
                    }, "refresh preset DBO").post();
                } catch (Exception e) {
                }
             */
            throw e;
        }
    }

    public C reqRead(CX c, Integer dbi) throws NoSuchContextException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        assertContext(c);
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).requestRead();
    }

    public C getWrite(CX c, Integer dbi) throws NoSuchContextException, NoSuchContextIndexException, EmptyException, ContentUnavailableException {
        assertContextIndex(c, dbi);
        return dbi2dbo.get(dbi).getWrite();
    }

    public boolean isInitialized(Integer dbi) throws NoSuchContextIndexException, NoSuchContextException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).isInitialized();
    }

    public boolean isPending(Integer dbi) throws NoSuchContextIndexException, NoSuchContextException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).isPending();
    }

    public boolean isInitializing(Integer dbi) throws NoSuchContextIndexException, NoSuchContextException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).isInitializing();
    }

    public String assertNamed(Integer dbi) throws NoSuchContextIndexException, NoSuchContextException, ContentUnavailableException, EmptyException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).getName();
    }

    public void assertInitialized(CX context, Integer dbi) throws DeviceException, ContentUnavailableException {
        assertContext(context);
        assertDBIndex(dbi);
        dbi2dbo.get(dbi).assertInitialized();
    }

    public void refresh(Integer dbi) throws NoSuchContextIndexException, ContentUnavailableException, EmptyException {
        assertDBIndex(dbi);
        dbi2dbo.get(dbi).refreshContent();
    }

    public boolean isEmpty(Integer dbi) throws NoSuchContextIndexException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).isEmpty();
    }

    public boolean containsIndex(CX context, Integer dbi) throws NoSuchContextException {
        assertContext(context);
        return c2dbi.get(context).contains(dbi);
    }

    public boolean readsIndex(CX context, Integer dbi) throws NoSuchContextException {
        assertContext(context);
        return dbi2dbo.containsKey(dbi);
    }

    public void setName(CX c, Integer dbi, String name) throws NoSuchContextIndexException, NoSuchContextException, EmptyException, ContentUnavailableException {
        assertContextIndex(c, dbi);
        dbi2dbo.get(dbi).setName(name);
    }

    public String getName(Integer dbi) throws NoSuchContextIndexException, NoSuchContextException, EmptyException, ContentUnavailableException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).getName();
    }

    public String getString(Integer dbi) throws NoSuchContextIndexException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).toString();
    }

    public void eraseContent(CX c, Integer dbi) throws NoSuchContextIndexException, NoSuchContextException {
        assertContextIndex(c, dbi);
        dbi2dbo.get(dbi).erase();
    }

    public IC getIsolatedContent(Integer dbi, Object flags) throws NoSuchContextIndexException, NoSuchContextException, EmptyException, ContentUnavailableException {
        assertDBIndex(dbi);
        return dbi2dbo.get(dbi).getIsolatedContent(flags);
    }

    public RWContent<C> getRW(CX c, Integer rdbi, Integer wdbi) throws ContentUnavailableException, EmptyException, NoSuchContextIndexException, NoSuchContextException {
        assertContextIndex(c, wdbi);
        assertDBIndex(rdbi);

        C rc, wc;
        if (rdbi.intValue() < wdbi.intValue()) {
            rc = dbi2dbo.get(rdbi).getRead();
            try {
                wc = dbi2dbo.get(wdbi).getWrite();
            } catch (EmptyException e) {
                releaseReadContent(rdbi);
                throw e;
            } catch (ContentUnavailableException e) {
                releaseReadContent(rdbi);
                throw e;
            }
        } else {
            wc = dbi2dbo.get(wdbi).getWrite();
            try {
                rc = dbi2dbo.get(rdbi).getRead();
            } catch (EmptyException e) {
                releaseWriteContent(wdbi);
                throw e;
            } catch (ContentUnavailableException e) {
                releaseWriteContent(wdbi);
                throw e;
            }
        }

        return new Impl_RWContent<C>(rc, wc, rdbi, wdbi);
    }

    public WWContent<C> getWW(CX c, Integer wdbi1, Integer wdbi2) throws ContentUnavailableException, EmptyException, NoSuchContextIndexException, NoSuchContextException {
        assertContextIndex(c, wdbi2);
        assertDBIndex(wdbi1);

        C rc, wc;
        if (wdbi1.intValue() < wdbi2.intValue()) {
            rc = dbi2dbo.get(wdbi1).getWrite();
            try {
                wc = dbi2dbo.get(wdbi2).getWrite();
            } catch (EmptyException e) {
                releaseWriteContent(wdbi1);
                throw e;
            } catch (ContentUnavailableException e) {
                releaseWriteContent(wdbi1);
                throw e;
            }
        } else {
            wc = dbi2dbo.get(wdbi2).getWrite();
            try {
                rc = dbi2dbo.get(wdbi1).getWrite();
            } catch (EmptyException e) {
                releaseWriteContent(wdbi2);
                throw e;
            } catch (ContentUnavailableException e) {
                releaseWriteContent(wdbi2);
                throw e;
            }
        }

        return new Impl_WWContent<C>(rc, wc, wdbi1, wdbi2);
    }

    public RRContent<C> getRR(CX c, Integer rdbi1, Integer rdbi2) throws ContentUnavailableException, EmptyException, NoSuchContextIndexException, NoSuchContextException {
        assertContextIndex(c, rdbi2);
        assertDBIndex(rdbi1);

        C rc1, rc2;
        if (rdbi1.intValue() < rdbi2.intValue()) {
            rc1 = dbi2dbo.get(rdbi1).getRead();
            try {
                rc2 = dbi2dbo.get(rdbi2).getRead();
            } catch (EmptyException e) {
                releaseReadContent(rdbi1);
                throw e;
            } catch (ContentUnavailableException e) {
                releaseReadContent(rdbi1);
                throw e;
            }
        } else {
            rc2 = dbi2dbo.get(rdbi2).getRead();
            try {
                rc1 = dbi2dbo.get(rdbi1).getRead();
            } catch (EmptyException e) {
                releaseReadContent(rdbi2);
                throw e;
            } catch (ContentUnavailableException e) {
                releaseReadContent(rdbi2);
                throw e;
            }
        }

        return new Impl_RRContent<C>(rc1, rc2, rdbi1, rdbi2);
    }

    public RCContent<C> getRC(CX c, Integer rdbi, Integer cdbi) throws ContentUnavailableException, EmptyException, NoSuchContextIndexException, NoSuchContextException {
        assertContextIndex(c, cdbi);
        assertDBIndex(rdbi);
        C rc;
        if (rdbi.intValue() < cdbi.intValue()) {
            rc = dbi2dbo.get(rdbi).getRead();
            dbi2dbo.get(cdbi).getCopy();
        } else {
            dbi2dbo.get(cdbi).getCopy();
            try {
                rc = dbi2dbo.get(rdbi).getRead();
            } catch (EmptyException e) {
                releaseWriteContent(cdbi);
                throw e;
            } catch (ContentUnavailableException e) {
                releaseWriteContent(cdbi);
                throw e;
            }
        }
        return new Impl_RCContent<C>(rc, rdbi, cdbi);
    }

    public void zDispose() {
        eventHandler.zDispose();
        Iterator<DBO<C, IC>> i = this.getDBOIterator();
        while (i.hasNext())
            i.next().zDispose();
        dbi2dbo.clear();
        c2dbi.clear();
        dbi2c.clear();
        c2cc.clear();
        c2pc.clear();
    }

    public void releaseReadContent(Integer dbi) {
        //assertDBIndex(dbi);
        dbi2dbo.get(dbi).releaseReadContent();
    }

    public void releaseWriteContent(Integer dbi) {
        //assertDBIndex(dbi);
        dbi2dbo.get(dbi).releaseWriteContent();
    }

    class Impl_RWContent <C> implements RWContent<C> {
        C rc, wc;
        Integer ri, wi;

        public Impl_RWContent(C rc, C wc, Integer ri, Integer wi) {
            this.rc = rc;
            this.wc = wc;
            this.ri = ri;
            this.wi = wi;
        }

        public C getReadable() {
            return rc;
        }

        public C getWritable() {
            return wc;
        }

        public void release() {
            AbstractDatabase.this.releaseReadContent(ri);
            AbstractDatabase.this.releaseWriteContent(wi);
        }
    }

    class Impl_RRContent <C> implements RRContent<C> {
        C r1c, r2c;
        Integer ri1, ri2;

        public Impl_RRContent(C r1c, C r2c, Integer ri1, Integer ri2) {
            this.ri1 = ri1;
            this.ri2 = ri2;
            this.r1c = r1c;
            this.r2c = r2c;
        }

        public C getReadable1() {
            return r1c;
        }

        public C getReadable2() {
            return r2c;
        }

        public void release() {
            AbstractDatabase.this.releaseReadContent(ri1);
            AbstractDatabase.this.releaseReadContent(ri2);
        }
    }

    class Impl_WWContent <C> implements WWContent<C> {
        C w1c, w2c;
        Integer wi1, wi2;

        public Impl_WWContent(C w1c, C w2c, Integer wi1, Integer wi2) {
            this.wi1 = wi1;
            this.wi2 = wi2;
            this.w1c = w1c;
            this.w2c = w2c;
        }

        public C getWritable1() {
            return w1c;
        }

        public C getWritable2() {
            return w2c;
        }

        public void release() {
            AbstractDatabase.this.releaseWriteContent(wi1);
            AbstractDatabase.this.releaseWriteContent(wi2);
        }
    }

    class Impl_RCContent <C> implements RCContent<C> {
        C rc;
        Integer ri, ci;

        public Impl_RCContent(C rc, Integer ri, Integer ci) {
            this.rc = rc;
            this.ri = ri;
            this.ci = ci;
        }

        public C getReadable() {
            return rc;
        }

        public void release() {
            AbstractDatabase.this.releaseReadContent(ri);
            AbstractDatabase.this.releaseWriteContent(ci);
        }
    }
}
