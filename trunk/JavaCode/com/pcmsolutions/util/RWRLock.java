/*
 * RWLock.java
 *
 * Created on 17 October 2002, 05:43
 */

package com.pcmsolutions.util;

import com.pcmsolutions.device.EMU.database.InitializingException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * @author pmeehan
 */

/*
  LEGAL TRANSITIONS:
    '*' = 0 or more
    '+' = 1 or more

    read+ -> unlock+
    write+ -> read* -> write* -> unlock+
    write+ -> read* -> refresh* -> unlock+
    refresh+ -> read* -> unlock+

*/
public class RWRLock implements Serializable {
    static final int WAITERS_SIZE = 5;
    transient ArrayList<Node> waiters;

    /**
     * Creates a new instance of RWLock
     */
    public RWRLock() {
        waiters = new ArrayList<Node>(WAITERS_SIZE);
    }

    private Node checkTransition(Thread t, int next) {
        int index = waiters.indexOf(t);
        Node node = null;
        if (index != -1) {
            node = waiters.get(index);
            switch (node.state) {
                case Node.READER:
                    if (next == Node.WRITER || next == Node.REFRESHER)
                        throw new IllegalArgumentException("upgrade lock");
                    break;
                case Node.WRITER:
                    break;
                case Node.REFRESHER:
                    if (next == Node.WRITER)
                        throw new IllegalArgumentException("downgrade lock");
                    break;
            }
        }
        return node;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        waiters = new ArrayList<Node>(WAITERS_SIZE);
    }

    public synchronized boolean isWriteLocked() {
        if (waiters.size() > 0)
            if (waiters.get(0).state == Node.WRITER)
                return true;
        return false;
    }

    public synchronized boolean isRefreshLocked() {
        if (waiters.size() > 0)
            if (waiters.get(0).state == Node.REFRESHER)
                return true;
        return false;
    }

    public synchronized boolean isWriteOrRefreshLocked() {
        return isWriteLocked() || isRefreshLocked();
    }

    private int firstWriter() {
        for (int index = 0, s = waiters.size(); index < s; index++)
            if (waiters.get(index).state == Node.WRITER)
                return (index);
        return (Integer.MAX_VALUE);
    }

    private int firstRefresher() {
        for (int index = 0, s = waiters.size(); index < s; index++)
            if (waiters.get(index).state == Node.REFRESHER)
                return (index);
        return (Integer.MAX_VALUE);
    }

    private int firstWriterOrRefresher() {
        return Math.min(firstWriter(), firstRefresher());
    }

    public synchronized void reqRead() throws InitializingException {
        Thread me = Thread.currentThread();
        if (waiters.indexOf(me) != firstRefresher())
            throw new InitializingException();
        read();
    }

    public synchronized void read() {
        Thread me = Thread.currentThread();
        Node node = checkTransition(me, Node.READER);
        if (node == null) {
            node = new Node(me, Node.READER);
            waiters.add(node);
        }

        while (waiters.indexOf(me) > firstWriterOrRefresher()) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        node.nAcquires++;
    }

    public synchronized void reqRefresh() throws IllegalArgumentException, InitializingException {
        Thread me = Thread.currentThread();
        if (waiters.indexOf(me) != firstRefresher())
            throw new InitializingException();

        Node node = checkTransition(me, Node.REFRESHER);

        if (node == null) {
            node = new Node(me, Node.REFRESHER);
            waiters.add(node);
        } else
            node.state = Node.REFRESHER;

        while (waiters.indexOf(me) != 0) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        node.nAcquires++;
    }

    public synchronized void write() throws IllegalArgumentException {
        Thread me = Thread.currentThread();
        Node node = checkTransition(me, Node.WRITER);

        if (node == null) {
            node = new Node(me, Node.WRITER);
            waiters.add(node);
        } else
            node.state = Node.WRITER;

        while (waiters.indexOf(me) != 0) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        node.nAcquires++;
    }

    public synchronized void unlock() throws IllegalArgumentException {
        Node node;
        Thread me = Thread.currentThread();
        int index = waiters.indexOf(me);
        if (index > firstWriterOrRefresher() || index == -1)
            throw new IllegalArgumentException(this.getClass().getName() + ":unlock-> lock not held!");
        node = waiters.get(index);
        node.nAcquires--;
        if (node.nAcquires == 0) {
            waiters.remove(index);
            notifyAll();
        }
    }

    private class Node {
        static final int READER = 0;
        static final int WRITER = 1;
        static final int REFRESHER = 2;
        Thread t;
        int state;
        int nAcquires;

        Node(Thread t, int state) {
            this.t = t;
            this.state = state;
            nAcquires = 0;
        }

        public boolean equals(Object o) {
            return o == t;
        }
    }
}
