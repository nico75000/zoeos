package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.ZDisposable;

/**
 * User: paulmeehan
 * Date: 21-Aug-2004
 * Time: 17:14:41
 */
public interface ThreadAllower extends ZDisposable{
    boolean allowThread();
}
