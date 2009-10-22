package com.pcmsolutions.device.EMU.database.events.content;

/**
 * User: paulmeehan
 * Date: 03-Sep-2004
 * Time: 18:51:40
 */
public class EventHandlerFactory {
    public static <CE extends ContentEvent,RE extends ContentRequestEvent, CL extends ContentListener>ManageableContentEventHandler createContentEventHandler(int size){
        return new Impl_ManageableContentEventHandler<CE,RE,CL>(size);   
    }
}
