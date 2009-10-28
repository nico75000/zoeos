package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.sample.SampleEditingMediator;
import com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.Ticket;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


class Impl_ReadableSample implements SampleModel, ReadableSample, IconAndTipCarrier, ZCommandProvider, Comparable {
    private static final int iconWidth = 10;
    private static final int iconHeight = 10;

    static final Color offset =  Color.white;//ZUtilities.invert(UIColors.getDefaultBG());

    private static final Icon flashSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, offset, UIColors.getPresetFlashIcon());
    private static final Icon emptyFlashSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, UIColors.getPresetFlashIcon(), offset, true);
    private static final Icon pendingSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, offset, UIColors.getPresetPendingIcon());
    private static final Icon initializingSampleIcon = new SampleIcon(iconWidth, iconHeight, offset, UIColors.getPresetInitializingIcon());
    private static final Icon namedSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, offset, UIColors.getPresetPendingIcon());
    private static final Icon initializedSampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, offset, UIColors.getPresetInitializedIcon());
    private static final Icon emptySampleIcon = new com.pcmsolutions.device.EMU.E4.gui.sample.SampleIcon(iconWidth, iconHeight, UIColors.getPresetInitializedIcon(), offset, true);

    private static final String TIP_NOINFO = "== NO INFO ==";
    private boolean stringFormatExtended = true;

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
            name = sc.getString(sample);
        } catch (DeviceException e) {
            name = "unknown";
        }
        if (stringFormatExtended)
            return " " + new DecimalFormat("0000").format(sample) + "  " + name;
        else
            return name;
    }

    // VIEW
    public Icon getIcon() {
        try {
            if (isEmpty()) {
                if (sample.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                    return emptyFlashSampleIcon;
                else
                    return emptySampleIcon;
            } else if (isPending()) {
                return pendingSampleIcon;
            } else if (isInitialized()) {
                if (sample.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                    return flashSampleIcon;
                else
                    return initializedSampleIcon;
            } else if (isInitializing()) {
                return initializingSampleIcon;
            }else
                System.out.println("error");
        } catch (SampleException e) {
        }
        return null;
    }

    // PRESET
    public void refresh() throws SampleException {
        try {
            sc.refresh(sample).post();
        } catch (ResourceUnavailableException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public boolean isSampleInitialized() throws DeviceException {
        return sc.isInitialized(sample);
    }

    /*
    public int getSampleState() throws DeviceException {
        try {
            return sc.getSampleState(sample);
        } catch (NoSuchContextException e) {
            throw new DeviceException(sample);
        }
    }

    public double getInitializationStatus() throws DeviceException, EmptyException {
        try {
            return sc.getInitializationStatus(sample);
        } catch (NoSuchContextException e) {
            throw new DeviceException(sample);
        }
    }
    */
    /*
    public boolean isSampleWriteLocked() throws DeviceException, EmptyException {
        try {
            return sc.isSampleWriteLocked(sample);
        } catch (NoSuchContextException e) {
            throw new DeviceException(sample);
        }
    }
    */

    public String getToolTipText() {
        try {
            return sc.getSampleSummary(sample);
        } catch (DeviceException e) {
        }
        return TIP_NOINFO;
    }

    public String getString() throws SampleException {
        try {
            return sc.getString(sample);
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public String getName() throws SampleException, EmptyException {
        try {
            return sc.getName(sample);
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public String getDisplayName() {
        try {
            return "S" + new ContextLocation(sample, sc.getString(sample)).toString();
        } catch (DeviceException e) {
            return "S" + new ContextLocation(sample, "unknown").toString();
        }
    }

    public Integer getIndex() {
        return sample;
    }

    // EVENTS
    public void addListener(SampleListener pl) throws DeviceException {
        sc.addContentListener(pl, new Integer[]{sample});
    }

    public void removeListener(SampleListener pl) {
        sc.removeContentListener(pl, new Integer[]{sample});
    }

    public ReadableSample getMostCapableNonContextEditableSampleDowngrade() {
        return this;
    }

    public void performDefaultAction() {
        /*
        new Impl_ZThread(){
              public void runBody() {
                  try {
                      Impl_ReadableSample.this.getSampleContext().getRootPresetContext().auditionSamples(new Integer[]{sample});
                  } catch (DeviceException e) {
                      e.printStackTrace();
                  } catch (NoSuchContextException e) {
                      e.printStackTrace();
                  } catch (EmptyException e) {
                      e.printStackTrace();
                  } catch (ParameterValueOutOfRangeException e) {
                      e.printStackTrace();
                  } catch (ZDeviceNotRunningException e) {
                      e.printStackTrace();
                  } catch (IllegalMultimodeChannelException e) {
                      e.printStackTrace();
                  } catch (AuditionManager.MultimodeChannelUnreachableException e) {
                      e.printStackTrace();
                  } catch (TooManyZonesException e) {
                      e.printStackTrace();
                  }
              }
          }.start();
          */
    }

    // AUDITION
    public Ticket audition() {
        return sc.getRootPresetContext().auditionSamples(new Integer[]{sample}, false);
    }

    public DeviceParameterContext getDeviceParameterContext() throws DeviceException {
        return sc.getDeviceParameterContext();
    }

    public DeviceContext getDeviceContext() {
        return sc.getDeviceContext();
    }

    public void setToStringFormatExtended(boolean extended) {
        this.stringFormatExtended = extended;
    }

    public void assertInitialized() throws SampleException {
        try {
            sc.assertInitialized(sample, true).post();
        } catch (ResourceUnavailableException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public int compareTo(Object o) {
        if (o instanceof ReadableSample) {
            Integer p = ((ReadableSample) o).getIndex();
            return sample.compareTo(p);
        }
        return 0;
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return ReadableSample.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return ReadableSample.cmdProviderHelper.getSupportedMarkers();
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

    public boolean isUser() {
        return sample.intValue() <= DeviceContext.MAX_USER_SAMPLE;
    }

    public boolean isEmpty() throws SampleException {
        try {
            return sc.isEmpty(sample);
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public boolean isPending() throws SampleException {
        try {
            return sc.isPending(sample);
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public boolean isInitializing() throws SampleException {
        try {
            return sc.isInitializing(sample);
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }

    public boolean isInitialized() throws SampleException {
        try {
            return sc.isInitialized(sample);
        } catch (DeviceException e) {
            throw new SampleException(sample, e.getMessage());
        }
    }
}

