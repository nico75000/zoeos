package com.pcmsolutions.system;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 27-Mar-2003
 * Time: 23:25:53
 * To change this template use Options | File Templates.
 */
public interface ZDeviceManagerListener {
    public void pendingListChanged();

    public void startedListChanged();

    public void stoppedListChanged();

    public void unidentifiedListChanged();

    public void duplicateListChanged();
}
