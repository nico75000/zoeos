package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.threads.ZThread;

/**
 * User: paulmeehan
 * Date: 02-Sep-2004
 * Time: 13:53:46
 */
public interface TaskThread extends ZThread {
    TaskQ getAssociatedQueue();
}
