package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.sample.ContextBasicEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ContextBasicEditableSampleZCommandMarker;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.ZCommandProviderHelper;


class Impl_ContextBasicEditableSample extends Impl_ContextReadableSample implements ContextBasicEditableSample, IconAndTipCarrier, Comparable {
    private static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ContextBasicEditableSampleZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.EraseSampleZMTC;com.pcmsolutions.device.EMU.E4.zcommands.RenameSampleZC;com.pcmsolutions.device.EMU.E4.zcommands.RenameSampleAllZMTC;com.pcmsolutions.device.EMU.E4.zcommands.SpecialSampleNamingZMTC");

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

    public void eraseSample() throws NoSuchSampleException, SampleEmptyException {
        try {
            sc.eraseSample(sample);
        } catch (com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public void setSampleName(String name) throws NoSuchSampleException, SampleEmptyException {
        try {
            sc.setSampleName(sample, name);
        } catch (com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public void lockSampleWrite() throws NoSuchSampleException, com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException {
        sc.lockSampleWrite(sample);
    }

    public com.pcmsolutions.system.ZCommand[] getZCommands() {
        com.pcmsolutions.system.ZCommand[] superCmdObjects = super.getZCommands();

        com.pcmsolutions.system.ZCommand[] cmdObjects = cmdProviderHelper.getCommandObjects(this);

        return com.pcmsolutions.system.ZUtilities.concatZCommands(superCmdObjects, cmdObjects);
    }
}

