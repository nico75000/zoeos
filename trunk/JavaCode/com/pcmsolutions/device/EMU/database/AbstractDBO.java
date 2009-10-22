package com.pcmsolutions.device.EMU.database;

import com.pcmsolutions.device.EMU.database.events.content.ContentEventHandler;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 05:20:10
 */
public abstract class AbstractDBO <C extends Content, IC> implements AbstractDatabase.DBO<C, IC>, Serializable {
    private static final String REFRESH_PREFIX = "..";
    private static final String PENDING = "+pending+";
    private final Integer index;
    private ContentEventHandler eventHandler;
    private volatile Object currObj = PENDING;
    private volatile boolean initializing = false;
    private transient ReentrantReadWriteLock lock;

    public AbstractDBO(Integer index, ContentEventHandler ceh) {
        this.index = index;
        this.eventHandler = ceh;
        buildTransients();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        buildTransients();
    }

    void buildTransients() {
        lock = new ReentrantReadWriteLock(false);
    }

    protected abstract Object provideCopiedContent(C c, String name);

    protected abstract Object retrieveName() throws ContentUnavailableException;

    protected abstract void handleNamedAsEmptyEvent();

    protected abstract void handleNameChangedEvent(String name);

    protected abstract void handleUninitializeEvent();

    protected abstract void handleEraseEvent();

    protected abstract Object performRefresh();

    protected abstract boolean testEmpty(String str);

    protected abstract String emptyString();

    protected abstract IC acquireIsolated(C content, Object flags) throws ContentUnavailableException;

    protected abstract C synthesizeNewContent(String name, Object flags) throws ContentUnavailableException;

    protected abstract C specifyContentAfterDrop(IC ic, String name, Object flags) throws ContentUnavailableException;

    protected abstract Object translateContent(Object rawContent);

    public final void restoreRawContent(Object content) {
        checkedSet(translateContent(content));
    }

    public final ContentEventHandler getEventHandler() {
        return eventHandler;
    }

    protected final String pendingString() {
        return PENDING;
    }

    public final boolean isInitialized() {
        return currObj instanceof Content || currObj.toString().equals(emptyString());
    }

    public final boolean isInitializing() {
        return initializing;
    }

    public final boolean isEmpty() {
        return currObj.toString().equals(emptyString());
    }

    public final boolean isWriteLocked() {
        return lock.isWriteLocked();
    }

    private void switchR2W() throws ContentUnavailableException {
        checkEDT();
        lock.readLock().unlock();
        lock.writeLock().lock();
    }

    private void switchW2R() {
        lock.readLock().lock();
        lock.writeLock().unlock();
    }

    private void checkEDT() throws ContentUnavailableException {
        if (SwingUtilities.isEventDispatchThread())
            throw new ContentUnavailableException();
    }

    public String getName() throws ContentUnavailableException, EmptyException {
        lock.readLock().lock();
        try {
            if (isPending()) {
                switchR2W();
                initializing = true;
                try {
                    task_assertNamed();
                } finally {
                    initializing = false;
                    switchW2R();
                }
            }
            if (isEmpty())
                throw new EmptyException();
            return currObj.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void task_assertNamed() throws ContentUnavailableException {
        if (isPending()) {
            Object name = retrieveName();
            if (name instanceof String) {
                if (testEmpty(name.toString().trim())) {
                    checkedSet(emptyString());
                    handleNamedAsEmptyEvent();
                } else {
                    checkedSet(name);
                    handleNameChangedEvent(name.toString());
                }
            } else if (name instanceof Content) {
                checkedSet(name);
            } else
                throw new ContentUnavailableException();
        }
    }

    public final void copyRawContent(C c, String name, Object flags) {
        checkedSet(provideCopiedContent(c, name));
    }

    private void checkedSet(Object o) {
        ZUtilities.zdispose(currObj);
        if (o == null)
            currObj = PENDING;
        else
            currObj = o;
    }

    // should be called from synchronized
    protected final C assertContent(final boolean underRead) throws EmptyException, ContentUnavailableException {
        if (currObj.toString().equals(emptyString()))
            throw new EmptyException();
        else if (!(currObj instanceof Content)) {
            if (underRead)
                switchR2W();
            try {
                if (!(currObj instanceof Content)) {
                    initializing = true;
                    try {
                        checkedSet(REFRESH_PREFIX + currObj.toString());
                        checkedSet(performRefresh());
                    } finally {
                        initializing = false;
                    }
                }
            } finally {
                if (underRead)
                    switchW2R();
            }
            if (!(currObj instanceof Content))
                throw new ContentUnavailableException();
        }
        return (C) currObj;
    }

    public final boolean remoteNameMismatch() throws ContentUnavailableException {
        lock.readLock().lock();
        try {
            return retrieveName().toString().trim().equals(currObj.toString().trim());
        } finally {
            lock.readLock().unlock();
        }
    }

    public final void assertInitialized() throws ContentUnavailableException {
        try {
            getRead();
            releaseReadContent();
        } catch (EmptyException e) {
        }
    }

    public final IC getIsolatedContent(Object flags) throws EmptyException, ContentUnavailableException {
        C c = getRead();
        try {
            return acquireIsolated(c, flags);
        } finally {
            releaseReadContent();
        }
    }

    public final void erase() {
        lock.writeLock().lock();
        try {
            ZUtilities.zdispose(currObj);
            checkedSet(emptyString());
            handleEraseEvent();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final C getRead() throws ContentUnavailableException, EmptyException {
        if (SwingUtilities.isEventDispatchThread())
            return requestRead();
        lock.readLock().lock();
        try {
            return assertContent(true);
        } catch (EmptyException e) {
            lock.readLock().unlock();
            throw e;
        } catch (Exception e) {
            lock.readLock().unlock();
            throw new ContentUnavailableException();
        }
    }

    public final C requestRead() throws EmptyException, ContentUnavailableException {
        if (/*lock.isWriteLocked() ||*/ initializing)
            throw new ContentUnavailableException("initializing");
        try {
            if (lock.readLock().tryLock(50, TimeUnit.MILLISECONDS)) {
                try {
                    if (currObj instanceof Content)
                        return (C) currObj;
                    else if (currObj.toString().equals(emptyString()))
                        throw new EmptyException();
                    else {
                        throw new ContentUnavailableException();
                    }
                } catch (EmptyException e) {
                    lock.readLock().unlock();
                    throw e;
                } catch (ContentUnavailableException e) {
                    lock.readLock().unlock();
                    throw e;
                } catch (Exception e) {
                    lock.readLock().unlock();
                    throw new ContentUnavailableException();
                }
            }
        } catch (InterruptedException e) {
        }
        throw new ContentUnavailableException();
    }

    public final C getWrite() throws EmptyException, ContentUnavailableException {
        checkEDT();
        lock.writeLock().lock();
        try {
            return assertContent(false);
        } catch (EmptyException e) {
            lock.writeLock().unlock();
            throw e;
        } catch (Exception e) {
            lock.writeLock().unlock();
            throw new ContentUnavailableException();
        }
    }

    public final void getCopy() {
        lock.writeLock().lock();
    }

    public final void uninitialize() {
        lock.writeLock().lock();
        try {
            checkedSet(PENDING);
            handleUninitializeEvent();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final void refreshContent() throws ContentUnavailableException, EmptyException {
        try {
            task_refresh(true);
        } catch (Exception e) {
            throw new ContentUnavailableException();
        } finally {
            if (isEmpty())
                throw new EmptyException();
        }
    }

    protected final void task_refresh(boolean force) {
        lock.writeLock().lock();
        try {
            if (!(currObj instanceof Content) || force) {
                initializing = true;
                try {
                    checkedSet(REFRESH_PREFIX + currObj.toString());
                    checkedSet(performRefresh());
                } finally {
                    initializing = false;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private final void task_assertNotEmpty() throws EmptyException {
        if (currObj.toString().equals(emptyString()))
            throw new EmptyException();
    }

    public final void newContent(String name, Object flags) throws ContentUnavailableException {
        lock.writeLock().lock();
        initializing = true;
        try {
            checkedSet(synthesizeNewContent(name, flags));
        } finally {
            initializing = false;
            lock.writeLock().unlock();
        }
    }

    public final void dropContent(IC ic, String name, Object flags) throws ContentUnavailableException {
        lock.writeLock().lock();
        try {
            checkedSet(specifyContentAfterDrop(ic, name, flags));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final void setName(String name) throws EmptyException, ContentUnavailableException {
        try {
            task_assertNotEmpty();
            assertInitialized();
            ((C) currObj).setName(name);
        } finally {
        }
    }

    public final Integer getIndex() {
        return index;
    }

    public final boolean isPending() {
        return currObj.toString().equals(PENDING);
    }

    public final void releaseReadContent() {
        lock.readLock().unlock();
    }

    public final void releaseWriteContent() {
        lock.writeLock().unlock();
    }

    public final String toString() {
        return currObj.toString();
    }

    public final void zDispose() {
        ZUtilities.zdispose(currObj);
    }

    public final int compareTo(AbstractDatabase.DBO dbo) {
        return getIndex().compareTo(dbo.getIndex());
    }

    public final Object retrieveRawContent() {
        return currObj;
    }
}
