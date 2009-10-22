package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadableSampleZCommandMarker;
import com.pcmsolutions.system.AbstractZMTCommand;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractReadableSampleZMTCommand extends AbstractZMTCommand<ReadableSample> implements E4ReadableSampleZCommandMarker {
    public String getPresentationCategory() {
        return "Sample";
    }
}
