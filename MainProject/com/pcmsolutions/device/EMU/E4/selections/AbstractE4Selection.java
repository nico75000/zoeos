package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.DeviceContext;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 21-May-2003
 * Time: 01:55:20
 * To change this template use Options | File Templates.
 */
public class AbstractE4Selection implements E4Selection {
    private transient DeviceContext srcDevice;
    private String name;
    private transient static final String DEF_NAME = "Unititled Selection";

    public AbstractE4Selection(DeviceContext d) {
        this.srcDevice = d;
        this.name = DEF_NAME;
    }

    public AbstractE4Selection(DeviceContext d, String name) {
        this.srcDevice = d;
        this.name = name;
    }

    public DeviceContext getSrcDevice() {
        return srcDevice;
    }

    public String getName() {
        return name;
    }
}
