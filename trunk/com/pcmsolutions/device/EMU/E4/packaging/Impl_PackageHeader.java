package com.pcmsolutions.device.EMU.E4.packaging;

import com.pcmsolutions.system.Zoeos;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 02-Dec-2003
 * Time: 06:40:40
 * To change this template use Options | File Templates.
 */
class Impl_PackageHeader implements Serializable, PackageHeader {
    protected double deviceVersion;
    protected String deviceName;
    protected String name;
    protected String notes;
    protected Date creationDate;
    protected double zoeosVersion = Zoeos.version;

    public double getDeviceVersion() {
        return deviceVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceVersion(double deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public double getZoeosVersion() {
        return zoeosVersion;
    }

    public void setZoeosVersion(double zoeosVersion) {
        this.zoeosVersion = zoeosVersion;
    }
}
