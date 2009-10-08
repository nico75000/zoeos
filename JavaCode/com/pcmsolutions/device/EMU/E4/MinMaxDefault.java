package com.pcmsolutions.device.EMU.E4;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 11-Apr-2003
 * Time: 22:03:10
 * To change this template use Options | File Templates.
 */
public interface MinMaxDefault extends Serializable{
    public Integer getID();

    public Integer getMin();

    public Integer getMax();

    public Integer getDefault();

}
