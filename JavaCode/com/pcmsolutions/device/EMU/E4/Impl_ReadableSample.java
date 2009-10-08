package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.sample.SampleEditingMediator;
import com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadableSampleZCommandMarker;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


class Impl_ReadableSample implements SampleModel, ReadableSample, IconAndTipCarrier, ZCommandProvider, Comparable {
    private static final int iconWidth = 10;
    private static final int iconHeight = 10;
    private static final Icon flashSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetFlashIcon());
    private static final Icon emptyFlashSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, UIColors.getPresetFlashIcon(), Color.white, true);
    private static final Icon pendingSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetPendingIcon());
    private static final Icon initializingSampleIcon = new SampleIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetInitializingIcon());
    private static final Icon namedSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetPendingIcon());
    private static final Icon initializedSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetInitializedIcon());
    private static final Icon emptySampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, UIColors.getPresetInitializedIcon(), Color.white, true);

    private static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ReadableSampleZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.RefreshSampleZMTC;");

    private static final String TIP_ERROR = "== NO INFO ==";
    private boolean stringFormatExtended = true;

    protected SampleEditingMediator sem;
    protected Integer sample;
    protected SampleContext sc;

    static {
        SampleClassManager.addSampleClass(Impl_ReadableSample.class, null);
    }

    public Impl_ReadableSample(SampleContext sc, Integer sample) {
        this.sample = sample;
        this.sc = sc;
    }

    public boolean equals(Object o) {
        Impl_ReadableSample p;
        if (o instanceof Impl_ReadableSample) {
            p = (Impl_ReadableSample) o;
            if (p.sample.equals(sample) && p.sc.equals(sc))
                return true;
        } else    // try and compare using just sample number
            if (o instanceof Integer && o.equals(sample))
                return true;

        return false;
    }

    public String toString() {
        String name;
        try {
            name = getSampleName();
        } catch (SampleEmptyException e) {
            name = DeviceContext.EMPTY_PRESET;
        } catch (NoSuchSampleException e) {
            name = "Unreachable Sample?";
        }
        if (stringFormatExtended)
            return " " + new DecimalFormat("0000").format(sample) + "  " + name;
        else
            return name;
    }

    // VIEW
    public Icon getIcon() {
        int st;
        try {
            st = this.getSampleState();
            if (sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                if (st == RemoteObjectStates.STATE_EMPTY)
                    return emptyFlashSampleIcon;
                else
                    return flashSampleIcon;

            switch (st) {
                case RemoteObjectStates.STATE_PENDING:
                    return pendingSampleIcon;
                case RemoteObjectStates.STATE_NAMED:
                    return namedSampleIcon;
                case RemoteObjectStates.STATE_INITIALIZED:
                    return initializedSampleIcon;
                case RemoteObjectStates.STATE_INITIALIZING:
                     return initializingSampleIcon;
                 case RemoteObjectStates.STATE_EMPTY:
                    return emptySampleIcon;
            }
        } catch (NoSuchSampleException e) {
        }
        return null;
    }

    // PRESET
    public void refreshSample() throws NoSuchSampleException {
        try {
            sc.refreshSample(sample);
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public void lockSampleRead() throws NoSuchSampleException, NoSuchContextException {
        sc.lockSampleRead(sample);
    }

    public void unlockSample() {
        sc.unlockSample(sample);
    }

    public boolean isSampleInitialized() throws NoSuchSampleException {
        return sc.isSampleInitialized(sample);
    }

    public int getSampleState() throws NoSuchSampleException {
        try {
            return sc.getSampleState(sample);
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public double getInitializationStatus() throws NoSuchSampleException, SampleEmptyException {
        try {
            return sc.getInitializationStatus(sample);
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public boolean isSampleWriteLocked() throws NoSuchSampleException, SampleEmptyException {
        try {
            return sc.isSampleWriteLocked(sample);
        } catch (NoSuchContextException e) {
            throw new NoSuchSampleException(sample);
        }
    }

    public String getToolTipText() {
        try {
            return sc.getSampleSummary(sample);
        } catch (NoSuchSampleException e) {
        } catch (NoSuchContextException e) {
        }
        return TIP_ERROR;
    }

    public String getSampleName() throws NoSuchSampleException, SampleEmptyException {
        return sc.getSampleName(sample);
    }

    public String getSampleDisplayName() throws NoSuchSampleException {
        try {
            return "S" + new AggSampleName(sample, getSampleName()).toString();
        } catch (SampleEmptyException e) {
            return "S" + new AggSampleName(sample, DeviceContext.EMPTY_PRESET).toString();
        }
    }

    public Integer getSampleNumber() {
        return sample;
    }

    // EVENTS
    public void addSampleListener(SampleListener pl) {
        sc.addSampleListener(pl, new Integer[]{sample});
    }

    public void removeSampleListener(SampleListener pl) {
        sc.removeSampleListener(pl, new Integer[]{sample});
    }

    public ReadableSample getMostCapableNonContextEditableSampleDowngrade() {
        return this;
    }

    public DeviceParameterContext getDeviceParameterContext() {
        return sc.getDeviceParameterContext();
    }

    public DeviceContext getDeviceContext() {
        return sc.getDeviceContext();
    }

    public void setToStringFormatExtended(boolean extended) {
        this.stringFormatExtended = extended;
    }

    public int compareTo(Object o) {
        if (o instanceof ReadableSample) {
            Integer p = ((ReadableSample) o).getSampleNumber();

            if (p.intValue() < sample.intValue())
                return 1;
            else if (p.intValue() > sample.intValue())
                return -1;
        }
        return 0;
    }

    public ZCommand[] getZCommands() {
        return cmdProviderHelper.getCommandObjects(this);
    }

    public void setSampleContext(SampleContext pc) {
        this.sc = pc;
    }

    public void setSample(Integer p) {
        this.sample = p;
    }

    public SampleContext getSampleContext() {
        return sc;
    }

    public Integer getSample() {
        return sample;
    }

    public void setSampleEditingMediator(com.pcmsolutions.device.EMU.E4.gui.sample.SampleEditingMediator pem) {
        this.sem = pem;
    }

    public SampleEditingMediator getSampleEditingMediator() {
        return sem;
    }
}

