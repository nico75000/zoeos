package com.pcmsolutions.device.EMU.E4.packaging;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 02-Dec-2003
 * Time: 06:18:24
 * To change this template use Options | File Templates.
 */
public interface PackageHeader {
    public double getDeviceVersion();

    public String getDeviceName();

    public double getZoeosVersion();

    public String getName();

    public String getNotes();

    public Date getCreationDate();
}
