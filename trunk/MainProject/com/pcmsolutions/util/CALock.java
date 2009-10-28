/*
 * RWLock.java
 *
 * Created on 17 October 2002, 05:43
 */

package com.pcmsolutions.util;

import com.pcmsolutions.gui.ZoeosFrame;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.locks.Condition;

/**
 *
 * @author  pmeehan
 */
public class CALock implements Serializable {

    transient Vector waiters;

    /** Creates a new instance of RWLock */
    public CALock() {
        waiters = new Vector();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        waiters = new Vector();
    }

    public synchronized boolean isConfigureLocked() {
        if (waiters.size() > 0 && ((CANode) waiters.get(0)).state == CANode.CONFIGURER)
            return true;
        return false;
    }

    private int firstConfigurer() {
        Enumeration e;
        int index;
        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            CANode node = (CANode) e.nextElement();
            if (node.state == CANode.CONFIGURER)
                return (index);
        }
        return (Integer.MAX_VALUE);
    }

    private int getIndex(Thread t) {
        Enumeration e;
        int index;
        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            CANode node = (CANode) e.nextElement();
            if (node.t == t)
                return index;
        }
        return (-1);
    }

    // will return false and not wait on configure lock if any configureres in waiters list
    public synchronized boolean tryConfigure() throws IllegalArgumentException {
        if (firstConfigurer() == Integer.MAX_VALUE) {
            configure();
            return true;
        }
        return false;
    }

    // will return false and not wait on access lock if any configureres in waiters list
    public synchronized boolean tryAccess() throws IllegalArgumentException {
        if (firstConfigurer() == Integer.MAX_VALUE) {
            access();
            return true;
        }
        return false;
    }

    // will return false and not wait on configure lock if any accessors or configurers in waiters list
    public synchronized boolean tryConfigureEx() throws IllegalArgumentException {
        if (waiters.size() == 0) {
            configure();
            return true;
        }
        return false;
    }

    public synchronized void access() {

        CANode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            node = new CANode(me, CANode.ACCESSOR);
            waiters.addElement(node);
        } else
            node = (CANode) waiters.elementAt(index);
        //while (getIndex(me) != 0) {
        while (getIndex(me) > firstConfigurer()) {
            try {
                wait();
            } catch (Exception e) {
            }

        }
        node.nAcquires++;
    }

    public synchronized void configure() throws IllegalArgumentException {
       if ( SwingUtilities.isEventDispatchThread())
           JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "EDT in CALock configure!");
        CANode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            node = new CANode(me, CANode.CONFIGURER);
            waiters.addElement(node);
        } else {
            node = (CANode) waiters.elementAt(index);
            if (node.state == CANode.ACCESSOR)
                throw new IllegalArgumentException("Attempt to configure device after obtaining an access lock.");
            node.state = CANode.CONFIGURER;
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

        CANode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index > firstConfigurer() || index == -1)
            throw new IllegalArgumentException(this.getClass().toString() + ":releaseContent-> lock not held!");
        node = (CANode) waiters.elementAt(index);
        node.nAcquires--;
        if (node.nAcquires == 0) {
            waiters.removeElementAt(index);
            notifyAll();
        }
    }

    private class CANode {
        static final int ACCESSOR = 0;
        static final int CONFIGURER = 1;
        Thread t;
        int state;
        int nAcquires;

        CANode(Thread t, int state) {
            this.t = t;
            this.state = state;
            nAcquires = 0;
        }
    }
}
