package com.pcmsolutions.device.EMU.database;

import java.io.Serializable;


/**
 * User: paulmeehan
 * Date: 10-Aug-2004
 * Time: 15:18:50
 */
public interface Content extends IsolatedContent, Serializable {
    public void setName(String name);
}
