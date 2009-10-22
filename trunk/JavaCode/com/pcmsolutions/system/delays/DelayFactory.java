package com.pcmsolutions.system.delays;

import com.pcmsolutions.system.Zoeos;

/**
 * User: paulmeehan
 * Date: 02-Sep-2004
 * Time: 15:46:02
 */
public class DelayFactory {
    public static Delay createNoDelay() {
        return new Delay() {
            public void beginDelay() {
            }

            public void blockOnDelay() {
            }

            public void zDispose() {
            }
        };
    }

    public static Delay createDelay(final long delay) {
        return new Delay() {
            long begin = 0;

            public void beginDelay() {
                begin = Zoeos.getZoeosTime();
            }

            public void blockOnDelay() {
                long sleep = delay - (Zoeos.getZoeosTime() - begin);
                if (sleep > 0)
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }

            public void zDispose() {
            }
        };
    }
}
