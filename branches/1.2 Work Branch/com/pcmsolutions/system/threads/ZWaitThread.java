package com.pcmsolutions.system.threads;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Mar-2003
 * Time: 00:05:29
 * To change this template use Options | File Templates.
 */
public class ZWaitThread extends Thread {
    public static volatile int defaultPriority = 4;
    protected volatile boolean alive = true;

    public ZWaitThread(ThreadGroup group, String name) {
        super(group, name);
        //setPriority(defaultPriority);
    }

    public ZWaitThread() {
        //setPriority(defaultPriority);
    }

    public String toString() {
        return super.toString() + "  " + this.getName();
    }

    public ZWaitThread(String name) {
        super(name);
        //setPriority(defaultPriority);
    }

    public final void stopThread() {
        alive = false;
        this.interrupt();
    }
}
