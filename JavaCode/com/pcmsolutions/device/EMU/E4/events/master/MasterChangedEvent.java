package com.pcmsolutions.device.EMU.E4.events.master;


import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.master.MasterListener;

public class MasterChangedEvent extends MasterEvent {

    private Integer[] parameters;

    public MasterChangedEvent(Object source, DeviceContext device, Integer[] parameters) {
        super(source, device);
        this.parameters = (Integer[]) parameters.clone();
    }

    public String toString() {
        return "MasterChangedEvent";
    }

    public Integer[] getParameters() {
        return parameters;
    }

    public void fire(MasterListener ml) {
        if (ml != null)
            ml.masterChanged(this);
    }

}

