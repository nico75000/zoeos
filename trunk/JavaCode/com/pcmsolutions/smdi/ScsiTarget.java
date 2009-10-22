package com.pcmsolutions.smdi;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 19:05:16
 * To change this template use Options | File Templates.
 */
public interface ScsiTarget extends Comparable, Serializable {

    public byte getHA_Id();

    public byte getSCSI_Id();

    public String getDeviceName();

    public boolean isDevice();

    public String getDeviceManufacturer();

    public int getDeviceType();

    public boolean isSMDI();
}
