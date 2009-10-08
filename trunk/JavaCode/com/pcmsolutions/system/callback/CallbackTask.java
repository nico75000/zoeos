package com.pcmsolutions.system.callback;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * User: paulmeehan
 * Date: 23-Jan-2004
 * Time: 11:39:33
 */
public class CallbackTask {
    private CallbackTaskResult retval = null;
    private CallbackTaskRunnable cr;

    public void init(CallbackTaskRunnable cr) {
        this.cr = cr;
    }

    public final CallbackTaskResult execute() {
        Runnable r = new Runnable() {
            public void run() {
                final Object r;
                try {
                    r = cr.run();
                    retval = new CallbackTaskResult() {
                        public boolean suceeded() {
                            return true;
                        }

                        public Exception getException() {
                            return null;
                        }

                        public Object getResult() {
                            return r;
                        }
                    };
                } catch (final Exception e) {
                    e.printStackTrace();
                    retval = new CallbackTaskResult() {
                        public boolean suceeded() {
                            return false;
                        }

                        public Exception getException() {
                            return e;
                        }

                        public Object getResult() {
                            return null;
                        }
                    };
                } finally {
                }
            }
        };
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return retval;
    }
}
