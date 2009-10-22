package com.pcmsolutions.device.EMU.database;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.events.content.ContentEvent;
import com.pcmsolutions.device.EMU.database.events.content.ContentListener;
import com.pcmsolutions.device.EMU.database.events.content.ContentRequestEvent;
import com.pcmsolutions.device.EMU.database.events.context.ContextAdditionEvent;
import com.pcmsolutions.device.EMU.database.events.context.ContextReleaseEvent;
import com.pcmsolutions.device.EMU.database.events.context.ContextRemovalEvent;
import com.pcmsolutions.system.tasking.Ticket;
import com.pcmsolutions.system.tasking.TicketRunnable;
import com.pcmsolutions.system.tasking.TicketedQ;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 10-Aug-2004
 * Time: 22:12:23
 */
public abstract class AbstractContext <CI extends ContextElement, C extends Content,IC,CX extends Context,CE extends ContentEvent,RE extends ContentRequestEvent, CL extends ContentListener> implements Context<CI, CX, CL, IC>, Serializable {
    final protected AbstractDatabase db;
    private transient Vector<ContextListener> listeners;

    protected AbstractContext(AbstractDatabase<C, IC, CX, CE, RE, CL> db) {
        this.db = db;
        buildTransients();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        buildTransients();
    }

    private void buildTransients() {
        listeners = new Vector<ContextListener>();
    }

    abstract protected TicketedQ getContextQ();

    public final void addContentListener(CL contentListener, Integer[] indexes) throws DeviceException {
        db.access();
        try {
            db.addContentListener(contentListener, indexes);
        } finally {
            db.release();
        }
    }

    public final void removeContentListener(CL contentListener, Integer[] indexes) {
        db.removeContentListener(contentListener, indexes);
    }

    public final void addContextListener(ContextListener l) {
        listeners.add(l);
    }

    public final void removeContextListener(ContextListener l) {
        listeners.remove(l);
    }

    // returns List of ContextReadablePreset/ReadablePreset ( e.g FLASH/ROM samples returned as ReadablePreset)

    public List<CI> getContextElements() throws DeviceException {
        ArrayList<CI> outList = new ArrayList<CI>();
        db.access();
        try {
            Set<Integer> indexes = getIndexesInContext();
            for (Integer p : indexes)
                try {
                    outList.add(getContextItemForIndex(p));
                } catch (NoSuchContextIndexException e) {
                    e.printStackTrace();
                }
            return outList;
        } finally {
            db.release();
        }
    }

    abstract protected CI getContextItemForIndex(Integer index) throws DeviceException;

    public final boolean isReallyEmpty(Integer index) throws DeviceException, ContentUnavailableException {
        db.access();
        try {
            if (db.isEmpty(index)) {
                db.refresh(index);
            }
        } catch (EmptyException e) {
            return true;
        } finally {
            db.release();
        }
        return false;
    }

    public final Integer firstEmpty() throws DeviceException, NoSuchContextException {
        db.access();
        try {
            Iterator<Integer> it = db.getContextIndexes(this).iterator();
            Integer i;
            while (it.hasNext()) {
                i = it.next();
                if (i.intValue() != 0)
                    if (db.isEmpty(i))
                        return i;
            }
            return null;
        } finally {
            db.release();
        }
    }

    public final SortedSet<Integer> findEmpties(Integer reqd, Integer beginIndex, Integer maxIndex) throws DeviceException {
        db.access();
        try {
            return db.findEmpties(this, reqd.intValue(), beginIndex, maxIndex);
        } finally {
            db.release();
        }
    }

    public final SortedSet<Integer> getIndexesInContext() throws DeviceException {
        db.access();
        try {
            return db.getContextIndexes(this);
        } finally {
            db.release();
        }
    }

    public final SortedSet getDatabaseIndexes() throws DeviceException {
        db.access();
        try {
            return db.getDBIndexes(this);
        } finally {
            db.release();
        }
    }

    public final Map<Integer, String> getContextNamesMap() throws DeviceException {
        db.access();
        try {
            return db.getNamesMap(this);
        } finally {
            db.release();
        }
    }

    public List<ContextLocation> getContextIndexNamesInRange(Integer lowIndex, Integer highIndex) throws DeviceException {
        db.access();
        try {
            return db.getIndexedNamesInRange(this, lowIndex, highIndex);
        } finally {
            db.release();
        }
    }

    public final boolean containsIndex(Integer index) throws DeviceException {
        db.access();
        try {
            return db.containsIndex(this, index);
        } finally {
            db.release();
        }
    }

    public final boolean readsIndex(Integer index) throws DeviceException {
        db.access();
        try {
            return db.readsIndex(this, index);
        } finally {
            db.release();
        }
    }

    public Ticket copy(final Integer srcIndex, final Integer destIndex, final String name) {
        return getContextQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                if (srcIndex.intValue() == destIndex.intValue())
                    return;
                db.access();
                try {
                    db.copyContent(AbstractContext.this, srcIndex, destIndex, name, null);
                } finally {
                    db.release();
                }
            }
        }, "copy");
    }

    public Ticket copy(final Integer srcIndex, final Integer destIndex) {
        return getContextQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                if (srcIndex.intValue() == destIndex.intValue())
                    return;
                db.access();
                try {
                    db.copyContent(AbstractContext.this, srcIndex, destIndex, null, null);
                } finally {
                    db.release();
                }
            }
        }, "copy");
    }

    public final Ticket release() {
        return getRefreshQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.configure();
                try {
                    db.releaseContext(AbstractContext.this);
                } finally {
                    db.release();
                }
            }
        }, "release");
    }

    protected abstract TicketedQ getRefreshQ();

    public Ticket refresh(final Integer index) {
        return getRefreshQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    db.refresh(index);
                } finally {
                    db.release();
                }
            }
        }, "refresh");
    }

    public Ticket assertInitialized(final Integer index, final boolean refreshIfEmpty) {
        return getRefreshQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    if (refreshIfEmpty)
                        try {
                            task_refreshIfEmpty(index);
                        } catch (EmptyException e) {
                            return;
                        }
                    db.assertInitialized(AbstractContext.this, index);
                } finally {
                    db.release();
                }
            }
        }, "assertInitialized");
    }

    public Ticket refreshIfRemoteNameMismatch(final Integer index) {
        return getRefreshQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    if (db.remoteNameMismatch(index))
                        db.refresh(index);
                } finally {
                    db.release();
                }
            }
        }, "refreshIfRemoteNameMismatch");

    }

    public final boolean isEmpty(Integer index) throws DeviceException, NoSuchContextException {
        db.access();
        try {
            return db.isEmpty(index);
        } finally {
            db.release();
        }
    }

    public boolean isPending(Integer index) throws DeviceException {
        db.access();
        try {
            return db.isPending(index);
        } finally {
            db.release();
        }
    }

    public boolean isInitializing(Integer index) throws DeviceException {
        db.access();
        try {
            return db.isInitializing(index);
        } finally {
            db.release();
        }
    }

    public Ticket assertNamed(final Integer index, final boolean refreshIfEmpty) {
        return getContextQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    if (refreshIfEmpty)
                        try {
                            task_refreshIfEmpty(index);
                        } catch (EmptyException e) {
                            return;
                        }
                    db.assertNamed(index);
                } finally {
                    db.release();
                }
            }
        }, "refreshIfEmpty");
    }

    public final Ticket refreshIfEmpty(final Integer index) {
        return getRefreshQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                try {
                    task_refreshIfEmpty(index);
                } catch (Exception e) {
                   // catch it so it isn't printed out
                }
            }
        }, "refreshIfEmpty");
    }

    protected final boolean task_refreshIfEmpty(final Integer index) throws DeviceException, ContentUnavailableException, EmptyException {
        db.access();
        try {
            if (db.isEmpty(index)) {
                db.refresh(index);
                return true;
            } else
                return false;
        } finally {
            db.release();
        }
    }

    public final boolean isInitialized(Integer index) throws DeviceException, NoSuchContextException {
        db.access();
        try {
            return db.isInitialized(index);
        } finally {
            db.release();
        }
    }

    public Ticket erase(final Integer index) {
        return getRefreshQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    db.eraseContent(AbstractContext.this, index);
                } finally {
                    db.release();
                }
            }
        }, "erase");
    }

    public Ticket uninitialize(final Integer index) {
        return getRefreshQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    db.uninitialize(index);
                } finally {
                    db.release();
                }

            }
        }, "uninitialize");
    }

    public Ticket setName(final Integer index, final String name) {
        return getRefreshQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                db.access();
                try {
                    db.setName(AbstractContext.this, index, name);
                } finally {
                    db.release();
                }
            }
        }, "setName");
    }

    public String getString(Integer index) throws DeviceException {
        db.access();
        try {
            return db.getString(index);
        } finally {
            db.release();
        }
    }

    public String getName(Integer index) throws DeviceException, ContentUnavailableException, NoSuchContextException, EmptyException {
        db.access();
        try {
            return db.getName(index);
        } finally {
            db.release();
        }
    }

    public final int size() throws DeviceException {
        db.access();
        try {
            return db.getContextSize(this);
        } finally {
            db.release();
        }
    }

    public final int databaseSize() throws DeviceException {
        db.access();
        try {
            return db.getDBCount();
        } finally {
            db.release();
        }
    }

    public final CX newContext(String name, Integer[] indexes) throws DeviceException, NoSuchContextException {
        db.configure();
        try {
            return (CX) db.newContext(this, name, indexes);
        } finally {
            db.release();
        }
    }

    public final int numEmpties(Integer[] indexes) throws DeviceException, NoSuchContextException {
        db.access();
        int count = 0;
        try {
            for (Integer i : indexes)
                if (db.isEmpty(i))
                    count++;
            return count;
        } finally {
            db.release();
        }
    }

    protected final void fireAddedToContext(final Integer[] indexes) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (Iterator<ContextListener> i = listeners.iterator(); i.hasNext();) {
                        try {
                            i.next().additionToContext(new ContextAdditionEvent(this, indexes));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    protected final void fireRemovedFromContext(final Integer[] indexes) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (Iterator<ContextListener> i = listeners.iterator(); i.hasNext();) {
                        try {
                            i.next().removalFromContext(new ContextRemovalEvent(this, indexes));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    protected final void fireContextReleased() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (Iterator<ContextListener> i = listeners.iterator(); i.hasNext();) {
                        try {
                            i.next().contextReleased(new ContextReleaseEvent(this));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
