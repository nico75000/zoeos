package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.ZDisposable;

import java.util.List;

/**
 * User: paulmeehan
 * Date: 02-Sep-2004
 * Time: 14:17:33
 */
public interface TaskInserter<T extends Task> extends ZDisposable {
    public void insertTask(final T t, final List<T> q, String qName) throws QueueUnavailableException;
}
