/*
 * DeviceMarshall.java
 *
 * Created on November 17, 2002, 8:54 PM
 */

package com.pcmsolutions.device;

import com.pcmsolutions.comms.SysexTransactionRecord;
import com.pcmsolutions.system.ZExternalDevice;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.ZoeosPreferences;

import java.util.*;

/**
 *
 * @author  pmeehan
 */
public class MidiDeviceMarshall implements DeviceMarshall {
    private final List deviceMarshalls = new ArrayList();
    private static final MidiDeviceMarshall INSTANCE = new MidiDeviceMarshall();

    {
        final List deviceClasses = new ArrayList();

        String s = ZoeosPreferences.ZPREF_deviceClasses.getValue();

        if (s != null) {
            Enumeration tok = new StringTokenizer(s, Zoeos.preferenceFieldSeperator);

            while (tok.hasMoreElements()) {
                deviceClasses.add(tok.nextElement());
            }
            String className;
            for (Iterator i = deviceClasses.iterator(); i.hasNext();) {
                className = (String) i.next() + ".Marshall";
                try {
                    Object o = (Class.forName(className)).newInstance();

                    if (o instanceof DeviceMarshall && ((DeviceMarshall) o).understandsClass(SysexTransactionRecord.class))
                        deviceMarshalls.add(o);
                    else
                        System.out.println("Configured Marshall does not understand MIDI Identity Reply Messages.");
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    public static MidiDeviceMarshall getInstance() {
        return INSTANCE;
    }

    public boolean understandsClass(Class c) {
        if (c == SysexTransactionRecord.class)
            return true;
        return false;
    }

    public ZExternalDevice tryIdentify(Object p) {
        if (!(p instanceof SysexTransactionRecord))
            return null;
        for (Iterator i = deviceMarshalls.iterator(); i.hasNext();) {
            try {
                DeviceMarshall o = (DeviceMarshall) i.next();
                ZExternalDevice d = o.tryIdentify(p);
                if (d != null)
                    return d;
            } catch (IllegalArgumentException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return null;
    }
}
