package com.pcmsolutions.device.EMU.database.events.context;


/**
 * User: paulmeehan
 * Date: 12-Aug-2004
 * Time: 16:08:09
 */
abstract class ContextModifyEvent extends ContextEvent {
    Integer[] indexes;

    protected ContextModifyEvent(Object source, Integer[] indexes) {
        super(source);
        this.indexes = indexes;
    }

    public Integer[] getIndexes() {
        return (Integer[]) indexes.clone();
    }
}
