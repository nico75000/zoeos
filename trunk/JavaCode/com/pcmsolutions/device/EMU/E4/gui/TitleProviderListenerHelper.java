package com.pcmsolutions.device.EMU.E4.gui;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * User: paulmeehan
 * Date: 21-Jan-2004
 * Time: 07:37:33
 */
public class TitleProviderListenerHelper implements Serializable {
    transient private Vector listeners;
    private final Object owner;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        makeTransients();
    }

    private void makeTransients() {
        listeners = new Vector();
    }

    public TitleProviderListenerHelper(Object owner) {
        this.owner = owner;
        makeTransients();
    }

    public void clearListeners(){
        listeners.clear();
    }
    public final void addTitleProviderListener(TitleProviderListener tpl) {
        listeners.add(tpl);
    }

    public final void removeTitleProviderListener(TitleProviderListener tpl) {
        listeners.remove(tpl);
    }

    public final void fireTitleProviderDataChanged() {
        fireTitleProviderDataChanged(owner);
    }

    public final void fireTitleProviderDataChanged(final Object source) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (int i = 0, j = listeners.size(); i < j; i++)
                        try {
                            ((TitleProviderListener) listeners.get(i)).titleProviderDataChanged(source);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            }
        });
    }
}
