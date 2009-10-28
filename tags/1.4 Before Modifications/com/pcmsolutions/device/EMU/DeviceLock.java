/*
 * RWLock.java
 *
 * Created on 17 October 2002, 05:43
 */

package com.pcmsolutions.device.EMU;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.ZoeosFrame;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author pmeehan
 */
public abstract class DeviceLock <CEX extends DeviceException> implements Serializable {

    transient Vector<ConditionalCANode> waiters;
    String conditional = null;

    public DeviceLock(String conditional) {
        waiters = new Vector<ConditionalCANode>();
        this.conditional = conditional;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        waiters = new Vector<ConditionalCANode>();
    }

    protected synchronized String getConditionString() {
        return conditional;
    }

    public synchronized boolean isConfigureLocked() {
        if (waiters.size() > 0 && ((ConditionalCANode) waiters.get(0)).state == ConditionalCANode.CONFIGURER)
            return true;
        return false;
    }

    private int firstConfigurer() {
        Enumeration e;
        int index;
        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            ConditionalCANode node = (ConditionalCANode) e.nextElement();
            if (node.state == ConditionalCANode.CONFIGURER)
                return (index);
        }
        return (Integer.MAX_VALUE);
    }

    // CONFIGURE
    // will return false and not wait on configure lock if any configureres in waiters list or is conditional
    public synchronized boolean tryConfigure() throws IllegalArgumentException {
        if (firstConfigurer() == getIndex(Thread.currentThread()) || (firstConfigurer() == Integer.MAX_VALUE && conditional == null)) {
            try {
                configure();
                return true;
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public synchronized boolean tryImmuneConfigure(String conditional) throws IllegalArgumentException {
        if (firstConfigurer() == getIndex(Thread.currentThread()) || firstConfigurer() == Integer.MAX_VALUE) {
            immuneConfigure(conditional);
            return true;
        }
        return false;
    }

    public synchronized void configure() throws IllegalArgumentException, CEX {
        if (SwingUtilities.isEventDispatchThread())
            JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "EDT in DeviceLock configure!");
        ConditionalCANode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            if (conditional != null)
                throw generateConditionException();
            node = new ConditionalCANode(me, ConditionalCANode.CONFIGURER, false);
            waiters.addElement(node);
        } else {
            node = waiters.elementAt(index);
            if (node.state == ConditionalCANode.ACCESSOR)
                throw new IllegalArgumentException("upgrade lock: access -> configure");
            if (!node.immune && conditional != null)
                throw generateConditionException();
            node.state = ConditionalCANode.CONFIGURER;
        }
        while (getIndex(me) != 0) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        if (!node.immune && conditional != null){
            if ( node.nAcquires == 0)
                waiters.remove(node);
            throw generateConditionException();
        }

        node.nAcquires++;
    }

    public synchronized void immuneConfigure(String conditional) throws IllegalArgumentException {
        this.conditional = conditional;
        immuneConfigure();
    }

    public synchronized void immuneConfigure() throws IllegalArgumentException {
        if (SwingUtilities.isEventDispatchThread())
            JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "EDT in CALock configure!");
        ConditionalCANode node;
        final Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            node = new ConditionalCANode(me, ConditionalCANode.CONFIGURER, true);
            waiters.addElement(node);
        } else {
            node = waiters.elementAt(index);
            if (node.state == ConditionalCANode.ACCESSOR)
                throw new IllegalArgumentException("upgrade lock: (immune) access -> immune configure");
            if (!node.immune)
                throw new IllegalArgumentException("upgrade lock: configure -> immune configure");
            node.state = ConditionalCANode.CONFIGURER;
        }
        while (getIndex(me) != 0) {
           // System.out.println("immune config index : " + getIndex(me) + "  waiters size = " + waiters.size() + " : thread at 0: " + waiters.get(0).t);
            try {
                wait();
            } catch (Exception e) {
            }
        }

        node.nAcquires++;
    }

    // ACCESS
    // will return false and not wait on access lock if any configurers in waiters list or lock is conditional
    public synchronized boolean tryAccess() throws IllegalArgumentException {
        if (firstConfigurer() == Integer.MAX_VALUE && conditional == null) {
            try {
                access();
                return true;
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public synchronized void access() throws CEX {
        ConditionalCANode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            if (conditional != null)
                throw generateConditionException();
            node = new ConditionalCANode(me, ConditionalCANode.ACCESSOR, false);
            waiters.addElement(node);
        } else {
            node = waiters.elementAt(index);
            if (!node.immune && conditional != null)
                throw generateConditionException();
        }
        while (getIndex(me) > firstConfigurer()) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        if (!node.immune && conditional != null){
            if ( node.nAcquires == 0)
                waiters.remove(node);
            throw generateConditionException();
        }

        node.nAcquires++;
    }

    /*
    public synchronized void immuneAccess() {
        ConditionalCANode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index == -1) {
            node = new ConditionalCANode(me, ConditionalCANode.ACCESSOR, true);
            waiters.addElement(node);
        } else {
            node = waiters.elementAt(index);
            if (!node.immune)
                throw new IllegalArgumentException("upgrade lock: access/condifure -> immune access");
        }
        while (getIndex(me) > firstConfigurer()) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        node.nAcquires++;
    }
    */
    // UNLOCK
    public synchronized void unlock() throws IllegalArgumentException {
       ConditionalCANode node;
        Thread me = Thread.currentThread();
        int index = getIndex(me);
        if (index > firstConfigurer() || index == -1){
            System.out.println(":unlock-> lock not held!");
            throw new IllegalArgumentException(this.getClass().toString() + ":unlock-> lock not held!");
        }
        node = waiters.elementAt(index);
        node.nAcquires--;
        if (node.nAcquires == 0) {
            waiters.removeElementAt(index);
            notifyAll();
        }
    }

    public synchronized void unlock(String conditional) throws IllegalArgumentException {
        this.conditional = conditional;
        unlock();
    }

    private int getIndex(Thread t) {
        Enumeration<ConditionalCANode> e;
        int index;
        for (index = 0, e = waiters.elements(); e.hasMoreElements(); index++) {
            ConditionalCANode node = e.nextElement();
            if (node.t == t)
                return index;
        }
        return (-1);
    }

    protected abstract CEX generateConditionException();

    private class ConditionalCANode {
        static final int ACCESSOR = 0;
        static final int CONFIGURER = 1;
        boolean immune;
        Thread t;
        int state;
        int nAcquires;

        ConditionalCANode(Thread t, int state, boolean immune) {
            this.t = t;
            this.state = state;
            nAcquires = 0;
            this.immune = immune;
        }
    }
}
