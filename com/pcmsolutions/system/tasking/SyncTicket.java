package com.pcmsolutions.system.tasking;

/**
 * User: paulmeehan
 * Date: 08-Aug-2004
 * Time: 19:10:47
 */
public interface SyncTicket {
     SyncTicket uselessTicket = new SyncTicket(){
         public void sync() throws Exception {
         }
     };
    
    void sync() throws Exception;
}
