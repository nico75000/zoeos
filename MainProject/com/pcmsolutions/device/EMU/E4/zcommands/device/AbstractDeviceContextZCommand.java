package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.zcommands.E4DeviceZCommandMarker;
import com.pcmsolutions.system.AbstractZCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractDeviceContextZCommand extends AbstractZCommand<DeviceContext> implements E4DeviceZCommandMarker {   
    public String getPresentationCategory() {
          return "Device";
      }
}
