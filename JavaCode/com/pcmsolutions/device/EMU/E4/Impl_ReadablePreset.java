package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.PresetIcon;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMultimodeChannelException;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.Ticket;
import com.pcmsolutions.util.IntegerUseMap;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.Set;


class Impl_ReadablePreset implements PresetModel, ReadablePreset, IconAndTipCarrier, Comparable, ZCommandProvider {
    private static final int iconWidth = 9;
    private static final int iconHeight = 9;

    //public static final Icon flashPresetUserIcon = new PresetUserIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetFlashIcon());
    //public static final Icon initializedPresetUserIcon = new PresetIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetInitializedIcon());
    //public static final Icon emptyPresetUserIcon = new PresetIcon(iconWidth, iconHeight, UIColors.getPresetInitializedIcon(), Color.white, true);
    static final Color offset =  Color.white;//ZUtilities.invert(UIColors.getDefaultBG());
    private static final Icon flashPresetIcon = new PresetIcon(iconWidth, iconHeight, offset, UIColors.getPresetFlashIcon());
    //private static final Icon pendingFlashPresetIcon = new PresetIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetFlashIcon(), UIColors.getPresetPendingIcon());
    private static final Icon namedFlashPresetIcon = new PresetIcon(iconWidth, iconHeight, offset, UIColors.getPresetFlashIcon(), UIColors.getPresetPendingIcon());
    private static final Icon emptyFlashPresetIcon = new PresetIcon(iconWidth, iconHeight, UIColors.getPresetFlashIcon(), offset, true);

    private static final Icon initializingPresetIcon = new PresetIcon(iconWidth, iconHeight, offset, UIColors.getPresetInitializingIcon());

    private static final Icon pendingPresetIcon = new PresetIcon(iconWidth, iconHeight, offset, UIColors.getPresetPendingIcon());
    private static final Icon namedPresetIcon = new PresetIcon(iconWidth, iconHeight, offset, UIColors.getPresetInitializedIcon(), UIColors.getPresetPendingIcon());
    private static final Icon initializedPresetIcon = new PresetIcon(iconWidth, iconHeight, offset, UIColors.getPresetInitializedIcon());
    private static final Icon emptyPresetIcon = new PresetIcon(iconWidth, iconHeight, UIColors.getPresetInitializedIcon(), UIColors.getDefaultBG(), true);


    private static final String TIP_ERROR = "== NO INFO ==";

    protected Integer preset;
    protected PresetContext pc;
    protected boolean stringFormatExtended = true;

    static {
        PresetClassManager.addPresetClass(Impl_ReadablePreset.class, null, "AbstractDatabase Preset");
    }

    public Impl_ReadablePreset(PresetContext pc, Integer preset) {
        this.preset = preset;
        this.pc = pc;
    }

    public boolean equals(Object o) {
        ReadablePreset p;
        if (o instanceof ReadablePreset) {
            p = (ReadablePreset) o;
            if (p.getIndex().equals(preset) && p.getPresetContext().equals(pc))
                return true;
        } else    // try and compare using just preset number
            if (o instanceof Integer && o.equals(preset))
                return true;

        return false;
    }

    public boolean isUser() {
        return preset.intValue() <= DeviceContext.MAX_USER_PRESET;
    }

    public boolean isEmpty() throws PresetException {
        try {
            return pc.isEmpty(preset);
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public boolean isPending() throws PresetException {
        try {
            return pc.isPending(preset);
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public boolean isInitializing() throws PresetException {
        try {
            return pc.isInitializing(preset);
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public String toString() {
        String name;
        try {
            name = pc.getString(preset);
        } catch (DeviceException e) {
            name = "unknown";
        }
        if (stringFormatExtended)
            return " " + new DecimalFormat("0000").format(preset) + "  " + name;
        else
            return name;
    }

    public ReadablePreset getReadablePresetDowngrade() {
        Impl_ReadablePreset np = new Impl_ReadablePreset(pc, preset);
        np.stringFormatExtended = stringFormatExtended;
        return np;
    }

    public ReadablePreset getMostCapableNonContextEditablePreset() {
        return this;
    }

    public void performDefaultAction() {
        performOpenAction(true);
    }

    public void performOpenAction(final boolean activate) {
        try {
            if (pc.isInitialized(preset))
                try {
                    pc.refreshIfEmpty(preset).post(new Callback(){
                        public void result(Exception e, boolean wasCancelled) {
                            try {
                                getDeviceContext().getViewManager().openPreset(Impl_ReadablePreset.this, activate).post();
                            } catch (ResourceUnavailableException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            else {
                pc.assertInitialized(preset, true).post(new Callback() {
                    public void result(Exception e, boolean wasCancelled) {
                        if (!wasCancelled)
                            try {
                                getDeviceContext().getViewManager().openPreset(Impl_ReadablePreset.this, activate).send(0);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                    }
                });
            }
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    public boolean isSamePresetContext(ReadablePreset p) {
        if (p instanceof Impl_ReadablePreset)
            if (((Impl_ReadablePreset) p).pc.equals(pc))
                return true;
        return false;
    }

    public boolean isSameDevice(ReadablePreset p) {
        return (p.getDeviceContext().equals(pc.getDeviceContext()));
    }

    public void assertRemote() throws PresetException {
        try {
            pc.assertRemote(preset).post();
        } catch (Exception e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void assertInitialized(boolean refreshEmpty) throws PresetException {
        try {
            pc.assertInitialized(preset, refreshEmpty).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    // VIEW
    public Icon getIcon() {
        try {
            if (isEmpty()) {
                if (preset.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                    return emptyFlashPresetIcon;
                else
                    return emptyPresetIcon;
            } else if (isPending()) {
                return pendingPresetIcon;
            } else if (isInitialized()) {
                if (preset.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                    return flashPresetIcon;
                else
                    return initializedPresetIcon;
            } else if (isInitializing()) {
                return initializingPresetIcon;
            } else {
                if (preset.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                    return namedFlashPresetIcon;
                else
                    return namedPresetIcon;
            }
        } catch (PresetException e) {
        }
        return null;
    }

    // PRESET
    public void refresh() {
        try {
            pc.refresh(preset).post();
        } /*catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        } */ catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
    }

    // PRESET
    public Ticket audition() {
        return pc.auditionPreset(preset);
    }

    public boolean isInitialized() throws PresetException {
        try {
            return pc.isInitialized(preset);
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public double getInitializationStatus() throws PresetException, EmptyException {
        try {
            return pc.getInitializationStatus(preset);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public String getToolTipText() {
        try {
            return pc.getPresetSummary(preset);
        } catch (Exception e) {
        }
        return TIP_ERROR;
    }

    public Integer[] getLinkParams(Integer link, Integer[] ids) throws PresetException, EmptyException, ParameterException {
        try {
            return pc.getLinkParams(preset, link, ids);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public String getString() throws PresetException {
        try {
            return pc.getString(preset);
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public String getName() throws PresetException, EmptyException {
        try {
            return pc.getName(preset);
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public String getDisplayName() throws PresetException {
        try {
            return "P" + new ContextLocation(preset, pc.getString(preset)).toString();
        } catch (DeviceException e) {
            return "P" + new ContextLocation(preset, "unknown").toString();
        }
        // } catch (EmptyException e) {
        //   return "P" + new ContextLocation(preset, DeviceContext.EMPTY_PRESET).toString();
        // }
    }

    public Integer getIndex() {
        return preset;
    }

    public ReadableParameterModel getParameterModel(Integer id) throws ParameterException {

        try {
            return new Impl_PresetReadableParameterModel(pc.getDeviceParameterContext().getPresetContext().getParameterDescriptor(id));
        } catch (DeviceException e) {
            throw new ParameterException(e.getMessage());
        }
    }


    class Impl_PresetReadableParameterModel extends AbstractReadableParameterModel {
        protected Integer[] id;
        private Integer value;

        public Integer getValue() throws ParameterUnavailableException {
            if (value == null)
                retrieveValue();
            return value;
        }

        private void retrieveValue() throws ParameterUnavailableException {
            try {
                value = getPresetParams(new Integer[]{pd.getId()})[0];
            } catch (Exception e) {
                throw new ParameterUnavailableException(pd.getId());
            }
        }

        public Impl_PresetReadableParameterModel(GeneralParameterDescriptor pd) {
            super(pd);
            id = new Integer[]{pd.getId()};
            addListener(pla);
        }

        public String getToolTipText() {
            try {
                return getValueString();
            } catch (ParameterException e) {
            }
            return super.getToolTipText();
        }

        public void zDispose() {
            super.zDispose();
            removeListener(pla);
        }

        protected PresetListenerAdapter pla = new PresetListenerAdapter() {
            public void presetRefreshed(PresetInitializeEvent ev) {
                value = null;
                // fireChanged();
            }

            public void presetChanged(PresetChangeEvent ev) {
                if (ev.getIndex().equals(preset) && ev.containsId(id[0])) {
                    value = null;
                    fireChanged();
                }
            }
        };
    }

    // EVENTS
    public void addListener(PresetListener pl) {
        try {
            pc.addContentListener(pl, new Integer[]{preset});
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    public void removeListener(PresetListener pl) {
        pc.removeContentListener(pl, new Integer[]{preset});
    }

    public DeviceParameterContext getDeviceParameterContext() throws DeviceException {
        return pc.getDeviceParameterContext();
    }

    public DeviceContext getDeviceContext() {
        return pc.getDeviceContext();
    }

    public void sendToMultiMode(Integer ch) {
        try {
            getDeviceContext().getMultiModeContext().setPreset(ch, preset).post();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setToStringFormatExtended(boolean extended) {
        this.stringFormatExtended = extended;
    }

    // PRESET
    public Set<Integer> getPresetSet() throws PresetException, EmptyException {
        try {
            return pc.getPresetDeepSet(preset);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    // PRESET
    public IntegerUseMap getSampleUsage() throws PresetException, EmptyException {
        try {
            return pc.presetSampleUsage(preset);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public IntegerUseMap getLinkedPresetUage() throws PresetException, EmptyException {
        try {
            return pc.presetLinkPresetUsage(preset);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    // PRESET
    public IsolatedPreset getIsolated() throws PresetException, EmptyException {
        try {
            return pc.getIsolatedPreset(preset, false);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public Integer[] getPresetParams(Integer[] ids) throws PresetException, EmptyException, IllegalParameterIdException {
        try {
            return pc.getPresetParams(preset, ids);
        } catch (Exception e) {
            throw new PresetException(e.getMessage());
        }
    }

    public Integer[] getVoiceParams(Integer voice, Integer[] ids) throws PresetException, EmptyException, ParameterException {
        try {
            return pc.getVoiceParams(preset, voice, ids);
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    // LINK
    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer link) throws PresetException, EmptyException {
        try {
            return pc.getIsolatedLink(preset, link);
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer voice, Integer zone) throws PresetException, EmptyException {
        try {
            return pc.getIsolatedZone(preset, voice, zone);
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public Integer[] getZoneParams(Integer voice, Integer zone, Integer[] ids) throws PresetException, EmptyException, ParameterException {
        try {
            return pc.getZoneParams(preset, voice, zone, ids);
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public ReadablePreset.ReadableVoice getReadableVoice(final Integer voice) {
        return new Impl_ReadableVoice(voice);
    }

    class Impl_ReadableVoice implements ReadableVoice, Comparable {
        protected Integer voice;

        public Impl_ReadableVoice(Integer voice) {
            this.voice = voice;
        }

        public ReadablePreset getPreset() {
            return Impl_ReadablePreset.this;
        }

        public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer zone) throws PresetException, EmptyException {
            return Impl_ReadablePreset.this.getIsolatedZone(voice, zone);
        }

        public Integer[] getVoiceParams(Integer[] ids) throws PresetException, EmptyException, ParameterException {
            return Impl_ReadablePreset.this.getVoiceParams(voice, ids);
        }

        public Integer getVoiceNumber() {
            return voice;
        }

        public void setVoiceNumber(Integer voice) {
            this.voice = voice;
        }

        public void performOpenAction() {
            try {
                assertInitialized(true);
                if (getDeviceContext().getDevicePreferences().ZPREF_usePartitionedVoiceEditing.getValue())
                    getDeviceContext().getViewManager().openTabbedVoice(Impl_ReadablePreset.Impl_ReadableVoice.this, getDeviceContext().getDevicePreferences().ZPREF_groupEnvelopesWhenVoiceTabbed.getValue(), true).post();
                else
                    getDeviceContext().getViewManager().openVoice(Impl_ReadablePreset.Impl_ReadableVoice.this, true).post();
            } catch (ResourceUnavailableException e) {
                e.printStackTrace();
            } catch (PresetException e) {
                e.printStackTrace();
            }
        }

        public Integer getPresetNumber() {
            return preset;
        }

        public IsolatedPreset.IsolatedVoice getIsolated() throws EmptyException, PresetException {
            return getIsolatedVoice(voice);
        }

        public ReadableParameterModel getParameterModel(Integer id) throws ParameterException {

            try {
                return new Impl_VoiceReadableParameterModel(pc.getDeviceParameterContext().getVoiceContext().getParameterDescriptor(id));
            } catch (DeviceException e) {
                throw new ParameterException(e.getMessage());
            }
        }

        public Integer[] getVoiceIndexesInGroup() throws EmptyException, PresetException {
            return Impl_ReadablePreset.this.getVoiceIndexesInGroupFromVoice(voice);
        }

        public int compareTo(Object o) {
            if (o instanceof ReadableVoice) {
                Integer v = ((ReadableVoice) o).getVoiceNumber();

                if (v.intValue() < voice.intValue())
                    return 1;
                else if (v.intValue() > voice.intValue())
                    return -1;
            }
            return 0;
        }

        public ReadableZone getReadableZone(final Integer zone) {
            return new Impl_ReadableZone(zone);
        }

        public int numZones() throws EmptyException, PresetException {
            return Impl_ReadablePreset.this.numZones(voice);
        }

        public ZCommand[] getZCommands(Class markerClass) {
            return ReadableVoice.cmdProviderHelper.getCommandObjects(markerClass, this);
        }

        public Class[] getZCommandMarkers() {
            return ReadableVoice.cmdProviderHelper.getSupportedMarkers();
        }

        class Impl_ReadableZone implements ReadableZone, Comparable {
            protected Integer zone;

            public Impl_ReadableZone(Integer zone) {
                this.zone = zone;
            }

            public Integer[] getZoneParams(Integer[] ids) throws PresetException, EmptyException, ParameterException {
                return Impl_ReadablePreset.this.getZoneParams(voice, zone, ids);
            }

            public ReadablePreset.ReadableVoice getVoice() {
                return Impl_ReadableVoice.this;
            }

            public Integer getVoiceNumber() {
                return voice;
            }

            public Integer getPresetNumber() {
                return preset;
            }

            public Integer getZoneNumber() {
                return zone;
            }

            public ReadablePreset getPreset() {
                return Impl_ReadablePreset.this;
            }

            public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolated() throws EmptyException, PresetException {
                return getIsolatedZone(zone);
            }

            public void setZoneNumber(Integer zone) {
                this.zone = zone;
            }

            public ReadableParameterModel getParameterModel(Integer id) throws ParameterException {
                try {
                    return new Impl_ZoneReadableParameterModel(pc.getDeviceParameterContext().getZoneContext().getParameterDescriptor(id));
                } catch (DeviceException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public int compareTo(Object o) {
                if (o instanceof ReadableZone) {
                    Integer z = ((ReadableZone) o).getZoneNumber();

                    if (z.intValue() < zone.intValue())
                        return 1;
                    else if (z.intValue() > zone.intValue())
                        return -1;
                }
                return 0;
            }

            class Impl_ZoneReadableParameterModel extends AbstractReadableParameterModel {
                private Integer[] id;
                private Integer value;

                public Impl_ZoneReadableParameterModel(GeneralParameterDescriptor pd) {
                    super(pd);
                    id = new Integer[]{pd.getId()};
                    Impl_ReadablePreset.this.addListener(pla);
                }

                private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
                    ois.defaultReadObject();
                    Impl_ReadablePreset.this.addListener(pla);
                }

                public Integer getValue() throws ParameterUnavailableException {
                    if (value == null)
                        retrieveValue();
                    return value;
                }

                private void retrieveValue() throws ParameterUnavailableException {
                    try {
                        value = getZoneParams(id)[0];
                    } catch (Exception e) {
                        throw new ParameterUnavailableException(id[0]);
                    }
                }

                protected PresetListenerAdapter pla = new PresetListenerAdapter() {
                    public void presetRefreshed(PresetInitializeEvent ev) {
                        value = null;
                        // Impl_ZoneReadableParameterModel.this.fireChanged();
                    }

                    public void zoneChanged(ZoneChangeEvent ev) {
                        if (ev.containsId(id[0])) {
                            value = null;
                            Impl_ZoneReadableParameterModel.this.fireChanged();
                        }
                    }
                };

                public String getToolTipText() {
                    if (tipShowingOwner)
                        try {
                            return "Voice " + (voice.intValue() + 1) + "  Zone " + (zone.intValue() + 1) + " [" + pd.toString() + " = " + getValueString() + " ]";
                        } catch (ParameterException e) {
                            return "Voice " + (voice.intValue() + 1) + "  Zone " + (zone.intValue() + 1);
                        }
                    return super.getToolTipText();
                }

                public void zDispose() {
                    super.zDispose();
                    Impl_ReadablePreset.this.removeListener(pla);
                }
            }
        };
        class Impl_VoiceReadableParameterModel extends AbstractReadableParameterModel {
            private Integer[] id;
            private boolean isFilterId;
            private Integer value;

            public Impl_VoiceReadableParameterModel(GeneralParameterDescriptor pd) {
                super(pd);
                id = new Integer[]{pd.getId()};
                isFilterId = isFilterId();
                if (isFilterId)
                    setFilterType();
                Impl_ReadablePreset.this.addListener(pla);
            }

            private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
                ois.defaultReadObject();
                Impl_ReadablePreset.this.addListener(pla);
            }

            private void setFilterType() {
                try {
                    ((FilterParameterDescriptor) pd).setFilterType(Impl_ReadableVoice.this.getVoiceParams(new Integer[]{IntPool.get(82)})[0]);
                    Impl_VoiceReadableParameterModel.this.fireChanged();
                } catch (EmptyException e) {
                    e.printStackTrace();
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                } catch (NoSuchVoiceException e) {
                    e.printStackTrace();
                } catch (ParameterException e) {
                    e.printStackTrace();
                } catch (PresetException e) {
                    e.printStackTrace();
                }
            }

            protected boolean isFilterId() {
                int idv = id[0].intValue();
                if (idv > 82 && idv < 93)
                    return true;
                return false;
            }

            public Integer getValue() throws ParameterUnavailableException {
                if (value == null)
                    retrieveValue();
                return value;
            }

            private void retrieveValue() throws ParameterUnavailableException {
                try {
                    value = getVoiceParams(id)[0];
                } catch (Exception e) {
                    throw new ParameterUnavailableException(id[0]);
                }
            }

            public String getToolTipText() {
                if (tipShowingOwner)
                    try {
                        return "Voice " + (voice.intValue() + 1) + " [" + pd.toString() + " = " + getValueString() + " ]";
                    } catch (ParameterException e) {
                        return "Voice " + (voice.intValue() + 1);
                    }
                return super.getToolTipText();
            }

            public void zDispose() {
                Impl_ReadablePreset.this.removeListener(pla);
            }

            protected PresetListenerAdapter pla = new PresetListenerAdapter() {
                public void presetRefreshed(PresetInitializeEvent ev) {
                    value = null;
                    //Impl_VoiceReadableParameterModel.this.fireChanged();
                }

                public void voiceChanged(VoiceChangeEvent ev) {
                    if (ev.getVoice().equals(voice)) {
                        if (isFilterId && ev.containsId(IntPool.get(82))) {
                            setFilterType();
                            value = null;
                            return;
                        }
                        int i = ev.indexOfId(id[0]);
                        if (i != -1) {
                            value = ev.getValues()[i];
                            Impl_VoiceReadableParameterModel.this.fireChanged();
                        }
                    }
                }
            };
        }
    };

    public ReadablePreset.ReadableLink getReadableLink(final Integer link) {
        return new Impl_ReadableLink(link);
    }

    class Impl_ReadableLink implements ReadableLink, Comparable {
        protected Integer link;

        public Impl_ReadableLink(Integer link) {
            this.link = link;
        }

        public Integer[] getLinkParams(Integer[] ids) throws PresetException, EmptyException, ParameterException {
            return Impl_ReadablePreset.this.getLinkParams(link, ids);
        }

        public Integer getLinkNumber() {
            return link;
        }

        public void setLinkNumber(Integer link) {
            this.link = link;
        }

        public IsolatedPreset.IsolatedLink getIsolated() throws EmptyException, PresetException {
            return getIsolatedLink(link);
        }

        public ReadablePreset getPreset() {
            return Impl_ReadablePreset.this;
        }

        public Integer getPresetNumber() {
            return preset;
        }

        public ReadableParameterModel getParameterModel(Integer id) throws ParameterException {
            try {
                return new Impl_LinkReadableParameterModel(pc.getDeviceParameterContext().getLinkContext().getParameterDescriptor(id));
            } catch (DeviceException e) {
                throw new ParameterException(e.getMessage());
            }
        }

        public int compareTo(Object o) {
            if (o instanceof ReadableLink) {
                Integer l = ((ReadableLink) o).getLinkNumber();

                if (l.intValue() < link.intValue())
                    return 1;
                else if (l.intValue() > link.intValue())
                    return -1;
            }
            return 0;
        }

        private class Impl_LinkReadableParameterModel extends AbstractReadableParameterModel {
            private Integer[] id;
            private Integer value;

            public Impl_LinkReadableParameterModel(GeneralParameterDescriptor pd) {
                super(pd);
                id = new Integer[]{pd.getId()};
                Impl_ReadablePreset.this.addListener(pla);
            }

            private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
                ois.defaultReadObject();
                Impl_ReadablePreset.this.addListener(pla);
            }

            public Integer getValue() throws ParameterUnavailableException {
                if (value == null)
                    retrieveValue();
                return value;
            }

            private void retrieveValue() throws ParameterUnavailableException {
                try {
                    value = getLinkParams(id)[0];
                } catch (Exception e) {
                    throw new ParameterUnavailableException(id[0]);
                }
            }

            public String getToolTipText() {
                if (tipShowingOwner)
                    try {
                        return "Link " + (link.intValue() + 1) + " [" + pd.toString() + " = " + getValueString() + " ]";
                    } catch (ParameterException e) {
                        return "Link " + (link.intValue() + 1);
                    }
                return super.getToolTipText();
            }

            protected PresetListenerAdapter pla = new PresetListenerAdapter() {

                public void presetRefreshed(PresetInitializeEvent ev) {
                    value = null;
                    // Impl_ReadableLink.Impl_LinkReadableParameterModel.this.fireChanged();
                }

                public void linkChanged(LinkChangeEvent ev) {
                    if (ev.getLink().equals(link)) {
                        if (ev.containsId(id[0])) {
                            value = null;
                            Impl_ReadableLink.Impl_LinkReadableParameterModel.this.fireChanged();
                        }
                    }
                }
            };

            public void zDispose() {
                Impl_ReadablePreset.this.removeListener(pla);
            }
        }
    };

    public int numLinks() throws PresetException, EmptyException {
        try {
            return pc.numLinks(preset);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public int numPresetSamples() throws PresetException, EmptyException {
        try {
            return pc.numPresetSamples(preset);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    // VOICE
    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer voice) throws PresetException, EmptyException {
        try {
            return pc.getIsolatedVoice(preset, voice);
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    // VOICE
    public void refreshVoiceParameters(Integer voice, Integer[] ids) throws PresetException {
        try {
            pc.refreshVoiceParameters(preset, voice, ids).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    // VOICE
    public Integer[] getVoiceIndexesInGroupFromVoice(Integer voice) throws EmptyException, PresetException {
        try {
            return pc.getVoiceIndexesInGroupFromVoice(preset, voice);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public Integer[] getVoiceIndexesInGroup(Integer group) throws EmptyException, PresetException {
        try {
            return pc.getVoiceIndexesInGroup(preset, group);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public int numPresetZones() throws PresetException, EmptyException {
        try {
            return pc.numPresetZones(preset);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public int numVoices() throws PresetException, EmptyException {
        try {
            return pc.numVoices(preset);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public Integer[] getGroupParams(Integer group, Integer[] ids) throws PresetException, EmptyException, ParameterException {
        try {
            return pc.getGroupParams(preset, group, ids);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public int numZones(Integer voice) throws PresetException, EmptyException {
        try {
            return pc.numZones(preset, voice);
        } catch (NoSuchContextException e) {
            throw new PresetException(e.getMessage());
        } catch (ContentUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (DeviceException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public int compareTo(Object o) {
        if (o instanceof ReadablePreset) {
            Integer p = ((ReadablePreset) o).getIndex();

            if (p.intValue() < preset.intValue())
                return 1;
            else if (p.intValue() > preset.intValue())
                return -1;
        }
        return 0;
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return ReadablePreset.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return ReadablePreset.cmdProviderHelper.getSupportedMarkers();
    }

    public void setPresetContext(PresetContext pc) {
        this.pc = pc;
    }

    public void setPreset(Integer p) {
        this.preset = p;
    }

    public PresetContext getPresetContext() {
        return pc;
    }

    public Integer getPreset() {
        return preset;
    }
}

