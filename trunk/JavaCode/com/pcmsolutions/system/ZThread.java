package com.pcmsolutions.system;



/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 12-Jun-2003
 * Time: 06:39:22
 * To change this template use Options | File Templates.
 */
public class ZThread extends Thread {
    public ZThread() {
        this("Unnamed ZThread");
    }

    public ZThread(ThreadGroup group, String name) {
        super(group, name);
    }

    public ZThread(String name) {
        super(name);
    }
}
