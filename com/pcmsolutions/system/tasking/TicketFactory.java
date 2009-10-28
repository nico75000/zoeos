package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 08-Aug-2004
 * Time: 13:36:44
 */
class TicketFactory {
    public static Ticket createCompositeTicket(final TicketRunnable r, String taskName, TaskQ queue) {
        return new AbstractCompositeTicket(taskName, queue) {
            public void run() throws Exception {
               // if (!isCancelled() && !isFinished())  // why doesn't this work????
                    r.run();
            }
        };
    }

    public static PostableTicket createPostableTicket(final TicketRunnable r, String taskName, TaskQ queue) {
        return new AbstractPostableTicket(taskName, queue) {
            public void run() throws Exception {
                if (!isCancelled() && !isFinished())
                    r.run();
            }
        };
    }

    public static SendableTicket createSendableTicket(final TicketRunnable r, String taskName, TaskQ queue) {
        return new AbstractSendableTicket(taskName, queue) {
            public void run() throws Exception {
                if (!isCancelled() && !isFinished())
                    r.run();
            }
        };
    }


    public static SyncTicket createSynchronizationTicket(final String taskName, final TaskQ queue) {
        return new SyncTicket() {
            final SendableTicket st = new AbstractSendableTicket(taskName, queue) {
                public void run() throws Exception {
                }
            };

            public void sync() throws Exception {
                st.send(0);
            }
        };
    }
}
