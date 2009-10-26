package com.pcmsolutions.system;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-Apr-2003
 * Time: 08:34:54
 * To change this template use Options | File Templates.
 */
public interface ZDeviceEventHandler {
    public void postZDeviceEvent(ZDeviceEvent ev);

    public void addZDeviceListener(ZDeviceListener zdl);

    public void removeZDeviceListener(ZDeviceListener zdl);
}
