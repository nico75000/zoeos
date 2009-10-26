package com.pcmsolutions.system.threads;

import com.pcmsolutions.system.Zoeos;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 12-Jun-2003
 * Time: 10:07:36
 * To change this template use Options | File Templates.
 */
public class ZChainedThread extends ZWaitThread {
    protected Thread link;
    protected Runnable r;

    public ZChainedThread(Thread link, Runnable r) {
        this("Unknown com.pcmsolutions.system.threads.ZChainedThread", link, r);
    }

    public ZChainedThread(String name, Thread link, Runnable r) {
        super(Zoeos.defaultTG, name);
        this.link = link;
        this.r = r;
    }

    public final void run() {
        if (link != null)
            while (link.isAlive())
                try {
                    link.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        r.run();
    }
}
