package com.pcmsolutions.device.EMU.database;

import com.pcmsolutions.device.EMU.E4.preset.PresetException;

/**
 * User: paulmeehan
 * Date: 14-Dec-2004
 * Time: 00:03:21
 */
public interface ContextElement {
    public String getString() throws ContextException;

    public String getName() throws ContextException, EmptyException;

    public String getDisplayName() throws ContextException;

    public Integer getIndex();

    public void setToStringFormatExtended(boolean extended);

    public void performDefaultAction();
    
}
