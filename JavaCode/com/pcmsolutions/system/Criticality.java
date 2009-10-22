package com.pcmsolutions.system;

/**
 * User: paulmeehan
 * Date: 21-Nov-2004
 * Time: 16:31:31
 */
public interface Criticality {
    void beginCritical(Object critical);

    void endCritical(Object critical);

    boolean runIfNonCritical(Runnable r);

    boolean isCritical();

    void waitOnCriticals();
}
