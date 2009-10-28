package com.pcmsolutions.device.EMU.database.events.content;

import java.util.EventObject;

/**
 * User: paulmeehan
 * Date: 12-Aug-2004
 * Time: 13:20:04
 */
public abstract class ContentEvent <CL extends ContentListener> extends EventObject  implements EventComparator{
    private Integer index;

    public ContentEvent(Object source, Integer index) {
        super(source);
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }

    public final String toString() {
        return super.toString();
    }

    public abstract void fire(CL el);
}

