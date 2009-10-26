package com.pcmsolutions.system.threads;

import com.pcmsolutions.system.Zoeos;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 12-Jun-2003
 * Time: 10:07:36
 * To change this template use Options | File Templates.
 */
public class ZBackgroundRemoterThread extends ZWaitThread {
    public static volatile int defaultPriority = 4;

    public ZBackgroundRemoterThread() {
        this("Unknown ZBackgroundRemoterThread");
    }

    public ZBackgroundRemoterThread(String name) {
        super(Zoeos.BackgroundRemoterTG, name);
        //setPriority(Zoeos.BackgroundRemoterTG.getMaxPriority());
        //setPriority(defaultPriority);
    }
/*    public static final ThreadGroup defaultTG = new ThreadGroup("Ddefault Group");
    public static final ThreadGroup DBModifyTG = new ThreadGroup("Database Modifier Group");
    public static final ThreadGroup DBEventDispatchTG = new ThreadGroup("Database Event Dispatch Group");
    public static final ThreadGroup BackgroundRemoterTG = new ThreadGroup("Background Remoting Group");
    public static final ThreadGroup UIRefreshHandlerTG = new ThreadGroup("UI ComponentRefresh Handler Group");
  */
}
