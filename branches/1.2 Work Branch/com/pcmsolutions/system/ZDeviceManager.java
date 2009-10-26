package com.pcmsolutions.system;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 27-Mar-2003
 * Time: 23:20:43
 * To change this template use Options | File Templates.
 */
public interface ZDeviceManager extends ZDisposable{

    public void performHunt();

    public ZExternalDevice getDeviceMatchingIdentityMessageString(String imt);

    public List getPendingList();

    public List getRunningList();

    public List getStoppedList();

    public List getUnidentifiedList();

    public void addDeviceManagerListener(ZDeviceManagerListener zdml);

    public void removeDeviceManagerListener(ZDeviceManagerListener zdml);

    public boolean isDuplicate(Object deviceIndentityMessage);

    public void startDevice(ZExternalDevice d);

    public void stopDevice(ZExternalDevice d, String reason);

    public void removeDevice(ZExternalDevice d, boolean saveState);

    public void revokeDevices();

    public void revokeDevicesNonThreaded();

    public void revokeDevicesNonThreaded(String reason);

    public void unrevokeDevices();

    //public boolean getStartBarrier();

    // public void setStartBarrier(boolean v);

    public void clearUnidentified();

    public Map getDuplicateMap();

    public void clearDuplicates();
}
