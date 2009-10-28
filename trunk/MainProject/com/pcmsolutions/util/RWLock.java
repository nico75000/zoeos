/*
 * RWLock.java
 *
 * Created on 17 October 2002, 05:43
 */

package com.pcmsolutions.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author pmeehan
 */
public class RWLock implements Serializable {

    transient Vector waiters;

    /**
     * Creates a new instance of RWLock
     */
    public RWLock() {
        waiters = new Vector();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        waiters = new Vector();
    }

    public synchronized boolean isWriteLocked() {
        if (waiters.size() > 0 && ((RWNode) waiters.get(0)).state == RWNode.WRITER)
            return true;
        return false;
    }

    private int firstWriter() {
        Enumeration e;
        int index;
        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            RWNode node = (RWNode) e.nextElement();
            if (node.state == RWNode.WRITER)
                return (index);
        }
        return (Integer.MAX_VALUE);
    }

    private int getIndex(Thread t) {
        Enumeration e;
        int index;
        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            RWNode node = (RWNode) e.nextElement();
            if (node.t == t)
                return index;
        }
        return (-1);
    }

    public synchronized void read() {
        RWNode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            node = new RWNode(me, RWNode.READER);
            waiters.addElement(node);
        } else
            node = (RWNode) waiters.elementAt(index);
        while (getIndex(me) > firstWriter()) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        node.nAcquires++;
    }

    // will return false and not wait on write lock if any writers in waiters list
    public synchronized boolean tryWrite() throws IllegalArgumentException {
        if (firstWriter() == Integer.MAX_VALUE) {
            write();
            return true;
        }
        return false;
    }

    // will return false and not wait on read lock if any writers in waiters list
    public synchronized boolean tryRead() throws IllegalArgumentException {
        if (firstWriter() == Integer.MAX_VALUE) {
            read();
            return true;
        }
        return false;
    }

    public synchronized void write() throws IllegalArgumentException {       
        //if ( SwingUtilities.isEventDispatchThread())
        //  JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "EDT in RWLock configure!");
        RWNode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            node = new RWNode(me, RWNode.WRITER);
            waiters.addElement(node);
        } else {
            node = (RWNode) waiters.elementAt(index);
            if (node.state == RWNode.READER)
                throw new IllegalArgumentException("upgrade lock!");
            node.state = RWNode.WRITER;
        }
        while (getIndex(me) != 0) {
            try {
                wait();
            } catch (Exception e) {
            }

        }
        node.nAcquires++;
    }

    public synchronized void unlock() throws IllegalArgumentException {
        RWNode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index > firstWriter() || index == -1)
            throw new IllegalArgumentException(this.getClass().toString() + ":releaseContent-> lock not held!");
        node = (RWNode) waiters.elementAt(index);
        node.nAcquires--;
        if (node.nAcquires == 0) {
            waiters.removeElementAt(index);
            notifyAll();
        }
    }

    private class RWNode {
        static final int READER = 0;
        static final int WRITER = 1;
        Thread t;
        int state;
        int nAcquires;

        RWNode(Thread t, int state) {
            this.t = t;
            this.state = state;
            nAcquires = 0;
        }
    }
}
