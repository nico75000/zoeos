package com.pcmsolutions.system.threads;

import java.util.ArrayList;

public abstract class Impl_ZThread extends Thread implements ZThread {
    protected volatile boolean shouldRun = true;

    protected Impl_ZThread(ThreadGroup group, String name) {
        super(group, name);
    }

    protected Impl_ZThread(String name) {
        super(name);
    }

    protected Impl_ZThread() {
    }

    public final void run() {
        try {
            runBody();
        } finally {
            performCompletedActions();
        }
    }

    private ArrayList completedActions = new ArrayList();   

    public void performCompletedActions() {
        if (Thread.currentThread() != this)
            throw new IllegalArgumentException("Impl_ZThread must perform it's own on completed actions");
        for (int i = 0, j = completedActions.size(); i < j; i++)
            try {
                ((Impl_ZThread.OnCompletedAction) completedActions.get(i)).completed(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        completedActions.clear();
    }

    // should only be called on this thread
    public void addOnCompletedAction(Impl_ZThread.OnCompletedAction ca) {
        if (Thread.currentThread() != this)
            throw new IllegalArgumentException("Impl_ZThread must add it's own on completed actions");
        completedActions.add(ca);
    }

    public boolean isShouldRun() {
        return shouldRun;
    }

    public abstract void runBody();

    public String toString() {
        return super.toString() + "  " + this.getName();
    }

    public final void stopThreadSafely() {
        shouldRun = false;
        this.interrupt();
    }
}
