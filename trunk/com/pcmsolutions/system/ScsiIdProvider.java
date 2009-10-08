package com.pcmsolutions.system;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Sep-2003
 * Time: 00:45:36
 * To change this template use Options | File Templates.
 */
public interface ScsiIdProvider {
    public int getScsiId() throws ZDeviceNotRunningException;
}
