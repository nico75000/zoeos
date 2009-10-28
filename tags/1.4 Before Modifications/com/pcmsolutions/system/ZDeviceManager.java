package com.pcmsolutions.system;

import com.pcmsolutions.system.tasking.Ticket;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 27-Mar-2003
 * Time: 23:20:43
 * To change this template use Options | File Templates.
 */
public interface ZDeviceManager extends ZDisposable {

    public void performHunt();

    public ZExternalDevice getDeviceMatchingIdentityMessageString(String imt);

    public List getPendingList();

    public List getRunningList();

    public List getStoppedList();

    public List getUnidentifiedList();

    public void addDeviceManagerListener(ZDeviceManagerListener zdml);

    public void removeDeviceManagerListener(ZDeviceManagerListener zdml);

    public boolean isDuplicate(Object deviceIndentityMessage);

    public Ticket startDevice(ZExternalDevice d);

    public Ticket stopDevice(ZExternalDevice d, String reason);

    public Ticket removeDevice(ZExternalDevice d, boolean saveState);

    public Ticket revokeDevices(String reason);

    public Ticket unrevokeDevices();

    public void clearUnidentified();

    public Map<ZExternalDevice,ZExternalDevice> getDuplicateMap();

    public void clearDuplicates();
}
