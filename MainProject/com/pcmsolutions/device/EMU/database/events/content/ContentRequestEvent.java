package com.pcmsolutions.device.EMU.database.events.content;

import java.util.EventObject;

/**
 * User: paulmeehan
 * Date: 03-Sep-2004
 * Time: 13:24:23
 */
public abstract class ContentRequestEvent <C extends Object> extends EventObject{
    private Integer index;
    C requestedData;
    public ContentRequestEvent(Object source, Integer index) {
        super(source);
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }

    public final C getRequestedData(){
        return requestedData;
    }
}
