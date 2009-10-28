package com.pcmsolutions.system.delays;

import com.pcmsolutions.system.ZDisposable;

/**
 * User: paulmeehan
 * Date: 02-Sep-2004
 * Time: 15:29:41
 */
public interface Delay extends ZDisposable {
    void beginDelay();

    void blockOnDelay();
}
