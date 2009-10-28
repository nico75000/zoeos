package com.pcmsolutions.system;

import java.util.Vector;

/**
 * User: paulmeehan
 * Date: 21-Nov-2004
 * Time: 16:03:43
 */
public class CriticalityMonitor implements Criticality {
    private final Vector criticals = new Vector();

    public void beginCritical(Object critical) {
        synchronized (criticals) {
            criticals.add(critical);
            criticals.notifyAll();
        }
    }

    public void endCritical(Object critical) {
        synchronized (criticals) {
            criticals.remove(critical);
            criticals.notifyAll();
        }
    }

    public boolean runIfNonCritical(Runnable r) {
        synchronized (criticals) {
            if (criticals.isEmpty()) {
                try {
                    r.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }

    public boolean isCritical() {
        return !criticals.isEmpty();
    }

    public void waitOnCriticals() {
        synchronized (criticals) {
            while (!criticals.isEmpty())
                try {
                    criticals.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }
}
