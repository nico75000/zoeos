package com.pcmsolutions.system.tasking;

import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.delays.Delay;

import java.util.List;

/**
 * User: paulmeehan
 * Date: 07-Aug-2004
 * Time: 11:10:47
 */
public class QueueFactory {
    public static ManageableTaskQ createTaskQueue(final Object owner, final String name, final int priority) {
        return new Impl_TaskQ(owner, name, priority) {
            public boolean allowThread() {
                return true;
            }
        };
    }

    public static ManageableTaskQ createTaskQueue(final Object owner, final String name, final int priority, final ThreadAllower ta) {
        return new Impl_TaskQ(owner, name, priority) {
            public boolean allowThread() {
                return ta.allowThread();
            }
            public void zDispose() {
                super.zDispose();
                ZUtilities.zdispose(ta);
            }
        };
    }

    public static <T extends Task>ManageableTaskQ createTaskQueue(final Object owner, final String name, int priority, final ThreadAllower ta, final TaskInserter<T> ti, final TaskFetcher<T> tf, final Delay td) {
        return new Impl_TaskQ<T>(owner, name, priority) {
            public boolean allowThread() {
                return ta.allowThread();
            }

            public List<T> fetch(List<T> ts) {
                return tf.fetch(ts);
            }

            public void insertTask(T t, List<T> ts, String qName) throws QueueUnavailableException {
                ti.insertTask(t, ts, qName);
            }

            public void beginDelay() {
                td.beginDelay();
            }

            public void blockOnDelay() {
                td.blockOnDelay();
            }

            public void zDispose() {
                super.zDispose();
                ZUtilities.zdispose(ta);
                ZUtilities.zdispose(ti);
                ZUtilities.zdispose(tf);
                ZUtilities.zdispose(td);
            }
        };
    }

    public static ManageableTicketedQ createTicketedQueue(final Object owner, final String name, final int priority, final ThreadAllower ta) {
        return new Impl_TicketedQ(owner, name, priority) {
            public boolean allowThread() {
                return ta.allowThread();
            }
             public void zDispose() {
                super.zDispose();
                ZUtilities.zdispose(ta);
            }
        };
    }

    public static ManageableTicketedQ createTicketedQueue(final Object owner, final String name, final int priority) {
        return new Impl_TicketedQ(owner, name, priority) {
            public boolean allowThread() {
                return true;
            }
        };
    }
}

