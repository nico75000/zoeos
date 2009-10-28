package com.pcmsolutions.system.callback;

import com.pcmsolutions.gui.ProgressCallback;

/**
 * User: paulmeehan
 * Date: 22-Aug-2004
 * Time: 07:17:08
 */
public final class CompleteProgressOnCancelledCallback implements Callback {
    ProgressCallback prog;
    Callback cb = null;

    public CompleteProgressOnCancelledCallback(ProgressCallback prog) {
        this.prog = prog;
    }

    public CompleteProgressOnCancelledCallback(ProgressCallback prog, Callback cb) {
        this.prog = prog;
        this.cb = cb;
    }

    public void result(final Exception e, boolean wasCancelled) {
        if (wasCancelled)
            prog.updateProgress(1);
        if (cb != null)
            cb.result(e, wasCancelled);
    }
}
