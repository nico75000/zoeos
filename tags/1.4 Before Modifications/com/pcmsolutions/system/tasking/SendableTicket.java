package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 08-Aug-2004
 * Time: 16:03:27
 */
public interface SendableTicket {
    SendableTicket uselessTicket = new SendableTicket(){
        public void send(long timeout) throws Exception {
        }

        public boolean isExpired() {
            return false;
        }

        public boolean isFinished() {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }
    };

    // blocks, throwing an exception if there is one
    // optionally time out after timeout ms, 0 specifies no timeout i.e wait until operation finishes (exception or not)
    void send(long timeout) throws ResourceUnavailableException, Exception;

    public boolean isExpired();
    public boolean isFinished();
    public boolean isCancelled();
}
