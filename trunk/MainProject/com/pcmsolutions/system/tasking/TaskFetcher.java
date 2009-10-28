package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.ZDisposable;

import java.util.List;

/**
 * User: paulmeehan
 * Date: 02-Sep-2004
 * Time: 14:16:00
 */
public interface TaskFetcher<T extends Task> extends ZDisposable{
    List<T> fetch(List<T> q);
}
