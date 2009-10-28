package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 08-Aug-2004
 * Time: 17:40:51
 */
public interface TicketRunnable {
    TicketRunnable uselessTask = new TicketRunnable(){
        public void run() throws Exception {
        }
    };

    public void run() throws Exception;    
}
