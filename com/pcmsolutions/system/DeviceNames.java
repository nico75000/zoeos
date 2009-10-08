package com.pcmsolutions.system;

import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Sep-2003
 * Time: 00:18:05
 * To change this template use Options | File Templates.
 */
public class DeviceNames {
    public static String getNameForDevice(Object identityReplyMessage) {
        if (identityReplyMessage instanceof Readable)
            return Preferences.userNodeForPackage(DeviceNames.class).get(identityReplyMessage.toString(), ((Readable) identityReplyMessage).toReadable());
        else
            return Preferences.userNodeForPackage(DeviceNames.class).get(identityReplyMessage.toString(), identityReplyMessage.toString());
    }

    public static String getNameForDevice(Object identityReplyMessage, String def) {
        return Preferences.userNodeForPackage(DeviceNames.class).get(identityReplyMessage.toString(), def);
    }

    public static void setNameForDevice(Object identityReplyMessage, String name) {
        Preferences.userNodeForPackage(DeviceNames.class).put(identityReplyMessage.toString(), name);
    }
}
