package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextBasicEditableSampleZCommandMarker;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;


class Impl_ContextBasicEditableSample extends Impl_ContextReadableSample implements ContextBasicEditableSample, IconAndTipCarrier, Comparable {

    static {
        SampleClassManager.addSampleClass(Impl_ContextBasicEditableSample.class, null);
    }

    public Impl_ContextBasicEditableSample(SampleContext sc, Integer sample) {
        super(sc, sample);
    }


    public boolean equals(Object o) {
        Impl_ContextBasicEditableSample p;
        if (o instanceof Impl_ContextBasicEditableSample) {
            p = (Impl_ContextBasicEditableSample) o;
            if (p.sample.equals(sample) && p.sc.equals(sc))
                return true;
        }
        /*else    // try and compare using just sample number
            if ( o instanceof Integer ){
                if ( o.equals(sample))
                    return true;
            }
        */
        return false;
    }

    public void eraseSample() throws SampleException {
        try {
            sc.erase(sample).post();
        } catch (ResourceUnavailableException e) {
            throw new SampleException(sample);
        }
    }

    public void setSampleName(String name) throws SampleException {
        try {
            sc.setName(sample, name).post();
        } catch (ResourceUnavailableException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return ContextBasicEditableSample.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return ContextBasicEditableSample.cmdProviderHelper.getSupportedMarkers();
    }
}

