package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.PresetChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.PresetInitializeEvent;
import com.pcmsolutions.device.EMU.E4.events.PresetRefreshEvent;
import com.pcmsolutions.device.EMU.E4.events.VoiceChangeEvent;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.DesktopEditingMediator;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.PresetIcon;
import com.pcmsolutions.device.EMU.E4.multimode.IllegalMidiChannelException;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadablePresetZCommandMarker;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.threads.ZDefaultThread;
import com.pcmsolutions.util.IntegerUseMap;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;


class Impl_ReadablePreset implements PresetModel, ReadablePreset, IconAndTipCarrier, Comparable, ZCommandProvider {
    private static final int iconWidth = 10;
    private static final int iconHeight = 10;

    private static final Icon flashPresetIcon = new PresetIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetFlashIcon());
    private static final Icon pendingFlashPresetIcon = new PresetIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetFlashIcon(), UIColors.getPresetPendingIcon());
    private static final Icon emptyFlashPresetIcon = new PresetIcon(iconWidth, iconHeight, UIColors.getPresetFlashIcon(), Color.white, true);

    private static final Icon initializingPresetIcon = new PresetIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetInitializingIcon());

    private static final Icon pendingPresetIcon = new PresetIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetPendingIcon());
    private static final Icon namedPresetIcon = new PresetIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetInitializedIcon(), UIColors.getPresetPendingIcon());
    private static final Icon initializedPresetIcon = new PresetIcon(iconWidth, iconHeight, Color.white, UIColors.getPresetInitializedIcon());
    private static final Icon emptyPresetIcon = new PresetIcon(iconWidth, iconHeight, UIColors.getPresetInitializedIcon(), Color.white, true);

    private static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ReadablePresetZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.RefreshPresetZMTC;com.pcmsolutions.device.EMU.E4.zcommands.ReadablePresetToMultiModeZMTC;com.pcmsolutions.device.EMU.E4.zcommands.OpenPresetZMTC;com.pcmsolutions.device.EMU.E4.zcommands.NewPresetPackageZMTC;");

    private static final String TIP_ERROR = "== NO INFO ==";

    protected DesktopEditingMediator dem;
    protected Integer preset;
    protected PresetContext pc;
    protected boolean stringFormatExtended = true;

    static {
        PresetClassManager.addPresetClass(Impl_ReadablePreset.class, null, "Database Preset");
    }

    public Impl_ReadablePreset(PresetContext pc, Integer preset, DesktopEditingMediator dem) {
        this.preset = preset;
        this.pc = pc;
        this.dem = dem;
    }

    public Impl_ReadablePreset(PresetContext pc, Integer preset) {
        this(pc, preset, null);
    }

    public boolean equals(Object o) {
        ReadablePreset p;
        if (o instanceof ReadablePreset) {
            p = (ReadablePreset) o;
            if (p.getPresetNumber().equals(preset) && p.getPresetContext().equals(pc))
                return true;
        } else    // try and compare using just preset number
            if (o instanceof Integer && o.equals(preset))
                return true;

        return false;
    }

    public String toString() {
        String name;
        try {
            name = getPresetName();
        } catch (PresetEmptyException e) {
            name = DeviceContext.EMPTY_PRESET;
        } catch (NoSuchPresetException e) {
            name = "Unknown Preset";
        }
        if (stringFormatExtended)
            return " " + new DecimalFormat("0000").format(preset) + "  " + name;
        else
            return name;
    }

    public ReadablePreset getReadablePresetDowngrade() {
        Impl_ReadablePreset np = new Impl_ReadablePreset(pc, preset);
        np.dem = dem;
        np.stringFormatExtended = stringFormatExtended;
        return np;
    }

    public ReadablePreset getMostCapableNonContextEditablePresetDowngrade() {
        return this;
    }

    public void performDefaultAction() {
        performOpenAction();
    }

    public void performOpenAction() {
        new ZDefaultThread() {
            public void run() {
                try {
                    assertPresetInitialized();
                    getDeviceContext().getViewManager().openPreset(Impl_ReadablePreset.this);
                } catch (NoSuchPresetException e) {
                }
            }
        }.start();
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

    public void assertPresetRemote() throws NoSuchPresetException {
        try {
            pc.assertPresetRemote(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void assertPresetInitialized() throws NoSuchPresetException {
        try {
            pc.assertPresetInitialized(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    // VIEW
    public Icon getIcon() {
        int st;
        try {
            st = this.getPresetState();
            switch (st) {

                case RemoteObjectStates.STATE_PENDING:
                    if (preset.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                        return pendingFlashPresetIcon;
                    else
                        return pendingPresetIcon;

                case RemoteObjectStates.STATE_INITIALIZING:
                    return initializingPresetIcon;

                case RemoteObjectStates.STATE_NAMED:
                    if (preset.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                        return pendingFlashPresetIcon;
                    else
                        return namedPresetIcon;

                case RemoteObjectStates.STATE_INITIALIZED:
                    if (preset.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                        return flashPresetIcon;
                    else
                        return initializedPresetIcon;

                case RemoteObjectStates.STATE_EMPTY:
                    if (preset.intValue() >= DeviceContext.BASE_FLASH_PRESET)
                        return emptyFlashPresetIcon;
                    else
                        return emptyPresetIcon;

            }
        } catch (NoSuchPresetException e) {
        }
        return null;
    }

    // PRESET
    public void refreshPreset() throws NoSuchPresetException {
        try {
            pc.refreshPreset(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void unlockPreset() {
        pc.unlockPreset(preset);
    }

    public boolean isPresetInitialized() throws NoSuchPresetException {
        return pc.isPresetInitialized(preset);
    }

    public int getPresetState() throws NoSuchPresetException {
        try {
            return pc.getPresetState(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public double getInitializationStatus() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.getInitializationStatus(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public boolean isPresetWriteLocked() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.isPresetWriteLocked(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public String getToolTipText() {
        try {
            return pc.getPresetSummary(preset);
        } catch (NoSuchPresetException e) {
        } catch (NoSuchContextException e) {
        }
        return TIP_ERROR;
    }

    public Integer[] getLinkParams(Integer link, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException {
        try {
            return pc.getLinkParams(preset, link, ids);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public String getPresetName() throws NoSuchPresetException, PresetEmptyException {
        return pc.getPresetName(preset);
    }

    public String getPresetDisplayName() throws NoSuchPresetException {
        try {
            return "P" + new AggRemoteName(preset, getPresetName()).toString();
        } catch (PresetEmptyException e) {
            return "P" + new AggRemoteName(preset, DeviceContext.EMPTY_PRESET).toString();
        }
    }

    public Integer getPresetNumber() {
        return preset;
    }

    public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException {
        return new Impl_PresetReadableParameterModel(pc.getDeviceParameterContext().getPresetContext().getParameterDescriptor(id));
    }

    public ReadableParameterModel[] getAllParameterModels() {
        List pds = pc.getDeviceParameterContext().getPresetContext().getAllParameterDescriptors();
        ReadableParameterModel[] outModels = new ReadableParameterModel[pds.size()];
        for (int i = 0,n = pds.size(); i < n; i++)
            outModels[i] = new Impl_PresetReadableParameterModel((GeneralParameterDescriptor) pds.get(i));
        return outModels;
    }

    class Impl_PresetReadableParameterModel extends AbstractReadableParameterModel {
        protected Integer[] id;

        public Impl_PresetReadableParameterModel(GeneralParameterDescriptor pd) {
            super(pd);
            id = new Integer[]{pd.getId()};
            addPresetListener(pla);
        }

        public Integer getValue() throws ParameterUnavailableException {
            try {
                return getPresetParams(new Integer[]{pd.getId()})[0];
            } catch (Exception e) {
                throw new ParameterUnavailableException();
            }
        }

        public String getToolTipText() {
            try {
                return getValueString();
            } catch (ParameterUnavailableException e) {
            }
            return super.getToolTipText();
        }

        public void zDispose() {
            super.zDispose();
            removePresetListener(pla);
        }

        protected PresetListenerAdapter pla = new PresetListenerAdapter() {
            public void presetInitialized(PresetInitializeEvent ev) {
                fireChanged();
            }

            public void presetRefreshed(PresetRefreshEvent ev) {
                fireChanged();
            }

            public void presetChanged(PresetChangeEvent ev) {
                if (ev.getPreset().equals(preset) && ev.containsId(id[0]))
                    fireChanged();
            }
        };
    }

    // EVENTS
    public void addPresetListener(PresetListener pl) {
        pc.addPresetListener(pl, new Integer[]{preset});
    }

    public void removePresetListener(PresetListener pl) {
        pc.removePresetListener(pl, new Integer[]{preset});
    }

    public DeviceParameterContext getDeviceParameterContext() {
        return pc.getDeviceParameterContext();
    }

    public DeviceContext getDeviceContext() {
        return pc.getDeviceContext();
    }

    public void sendToMultiMode(Integer ch) throws IllegalMidiChannelException {
        try {
            getDeviceContext().getMultiModeContext().setPreset(ch, preset);
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        }
    }

    public void setToStringFormatExtended(boolean extended) {
        this.stringFormatExtended = extended;
    }

    // PRESET
    public Set getPresetSet() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.getPresetSet(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    // PRESET
    public IntegerUseMap presetSampleUsage() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.presetSampleUsage(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public IntegerUseMap presetLinkPresetUsage() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.presetLinkPresetUsage(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    // PRESET
    public IsolatedPreset getIsolated() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.getIsolatedPreset(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Integer[] getPresetParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException {
        try {
            return pc.getPresetParams(preset, ids);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Integer[] getVoiceParams(Integer voice, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException {
        try {
            return pc.getVoiceParams(preset, voice, ids);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    // LINK
    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer link) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException {
        try {
            return pc.getIsolatedLink(preset, link);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer voice, Integer zone) throws NoSuchZoneException, NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
        try {
            return pc.getIsolatedZone(preset, voice, zone);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Integer[] getZoneParams(Integer voice, Integer zone, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException {
        try {
            return pc.getZoneParams(preset, voice, zone, ids);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
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

        public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer zone) throws NoSuchZoneException, NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
            return Impl_ReadablePreset.this.getIsolatedZone(voice, zone);
        }

        public Integer[] getVoiceParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException {
            return Impl_ReadablePreset.this.getVoiceParams(voice, ids);
        }

        public Integer getVoiceNumber() {
            return voice;
        }

        public void setVoiceNumber(Integer voice) {
            this.voice = voice;
        }

        public void performOpenAction() {
            new ZDefaultThread() {
                public void run() {
                    try {
                        assertPresetInitialized();
                         if (getDeviceContext().getDevicePreferences().ZPREF_useTabbedVoicePanel.getValue())
                            getDeviceContext().getViewManager().openTabbedVoice(Impl_ReadablePreset.Impl_ReadableVoice.this, getDeviceContext().getDevicePreferences().ZPREF_groupEnvelopesWhenVoiceTabbed.getValue(), true);
                        else
                            getDeviceContext().getViewManager().openVoice(Impl_ReadablePreset.Impl_ReadableVoice.this, true);
                    } catch (NoSuchPresetException e) {
                    }
                }
            }.start();
        }

        public Integer getPresetNumber() {
            return preset;
        }

        public IsolatedPreset.IsolatedVoice getIsolated() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException {
            return getIsolatedVoice(voice);
        }

        public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException {
            return new Impl_VoiceReadableParameterModel(pc.getDeviceParameterContext().getVoiceContext().getParameterDescriptor(id));
        }

        public Integer[] getVoiceIndexesInGroup() throws PresetEmptyException, NoSuchContextException, NoSuchVoiceException, NoSuchPresetException {
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

        public int numZones() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException {
            return Impl_ReadablePreset.this.numZones(voice);
        }

        public ZCommand[] getZCommands() {
            return cmdProviderHelper.getCommandObjects(this);
        }

        class Impl_ReadableZone implements ReadableZone, Comparable {
            protected Integer zone;

            public Impl_ReadableZone(Integer zone) {
                this.zone = zone;
            }

            public Integer[] getZoneParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException {
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

            public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolated() throws PresetEmptyException, NoSuchZoneException, NoSuchVoiceException, NoSuchPresetException {
                return getIsolatedZone(zone);
            }

            public void setZoneNumber(Integer zone) {
                this.zone = zone;
            }

            public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException {
                return new Impl_ZoneReadableParameterModel(pc.getDeviceParameterContext().getZoneContext().getParameterDescriptor(id));
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

                public Impl_ZoneReadableParameterModel(GeneralParameterDescriptor pd) {
                    super(pd);
                    id = new Integer[]{pd.getId()};
                }

                public Integer getValue() throws ParameterUnavailableException {
                    try {
                        return getZoneParams(id)[0];
                    } catch (Exception e) {
                        throw new ParameterUnavailableException();
                    }
                }

                public String getToolTipText() {
                    if (tipShowingOwner)
                        try {
                            return "Voice " + (voice.intValue() + 1) + "  Zone " + (zone.intValue() + 1) + " [" + pd.toString() + " = " + getValueString() + " ]";
                        } catch (ParameterUnavailableException e) {
                            return "Voice " + (voice.intValue() + 1) + "  Zone " + (zone.intValue() + 1);
                        }
                    return super.getToolTipText();
                }
            }
        };
        class Impl_VoiceReadableParameterModel extends AbstractReadableParameterModel {
            private Integer[] id;
            private boolean isFilterId;

            public Impl_VoiceReadableParameterModel(GeneralParameterDescriptor pd) {
                super(pd);
                id = new Integer[]{pd.getId()};
                isFilterId = isFilterId();
                if (isFilterId)
                    setFilterType();
                Impl_ReadablePreset.this.addPresetListener(pla);
            }

            private void setFilterType() {
                try {
                    ((FilterParameterDescriptor) pd).setFilterType(Impl_ReadableVoice.this.getVoiceParams(new Integer[]{IntPool.get(82)})[0]);
                    Impl_VoiceReadableParameterModel.this.fireChanged();
                } catch (NoSuchPresetException e) {
                    e.printStackTrace();
                } catch (PresetEmptyException e) {
                    e.printStackTrace();
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                } catch (NoSuchVoiceException e) {
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
                try {
                    return getVoiceParams(id)[0];
                } catch (Exception e) {
                    throw new ParameterUnavailableException();
                }
            }

            public String getToolTipText() {
                if (tipShowingOwner)
                    try {
                        return "Voice " + (voice.intValue() + 1) + " [" + pd.toString() + " = " + getValueString() + " ]";
                    } catch (ParameterUnavailableException e) {
                        return "Voice " + (voice.intValue() + 1);
                    }
                return super.getToolTipText();
            }

            public void zDispose() {
                Impl_ReadablePreset.this.removePresetListener(pla);
            }

            protected PresetListenerAdapter pla = new PresetListenerAdapter() {
                public void presetInitialized(PresetInitializeEvent ev) {
                    Impl_VoiceReadableParameterModel.this.fireChanged();
                }

                public void presetRefreshed(PresetRefreshEvent ev) {
                    Impl_VoiceReadableParameterModel.this.fireChanged();
                }

                public void voiceChanged(VoiceChangeEvent ev) {
                    if (ev.getVoice().equals(voice)) {
                        if (isFilterId && ev.containsId(IntPool.get(82))) {
                            setFilterType();
                            return;
                        } else if (ev.containsId(id[0]))
                            Impl_VoiceReadableParameterModel.this.fireChanged();
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

        public Integer[] getLinkParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException {
            return Impl_ReadablePreset.this.getLinkParams(link, ids);
        }

        public Integer getLinkNumber() {
            return link;
        }

        public void setLinkNumber(Integer link) {
            this.link = link;
        }

        public IsolatedPreset.IsolatedLink getIsolated() throws PresetEmptyException, NoSuchPresetException, NoSuchLinkException {
            return getIsolatedLink(link);
        }

        public ReadablePreset getPreset() {
            return Impl_ReadablePreset.this;
        }

        public Integer getPresetNumber() {
            return preset;
        }

        public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException {
            return new Impl_LinkReadableParameterModel(pc.getDeviceParameterContext().getLinkContext().getParameterDescriptor(id));
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

            public Impl_LinkReadableParameterModel(GeneralParameterDescriptor pd) {
                super(pd);
                id = new Integer[]{pd.getId()};
            }

            public Integer getValue() throws ParameterUnavailableException {
                try {
                    return getLinkParams(id)[0];
                } catch (Exception e) {
                    throw new ParameterUnavailableException();
                }
            }

            public String getToolTipText() {
                if (tipShowingOwner)
                    try {
                        return "Link " + (link.intValue() + 1) + " [" + pd.toString() + " = " + getValueString() + " ]";
                    } catch (ParameterUnavailableException e) {
                        return "Link " + (link.intValue() + 1);
                    }
                return super.getToolTipText();
            }
        }
    };

    public int numLinks() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.numLinks(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public int numPresetSamples() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.numPresetSamples(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    // VOICE
    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
        try {
            return pc.getIsolatedVoice(preset, voice);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    // VOICE
    public void refreshVoiceParameters(Integer voice, Integer[] ids) throws NoSuchContextException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException, ParameterValueOutOfRangeException, IllegalParameterIdException {
        try {
            pc.refreshVoiceParameters(preset, voice, ids);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    // VOICE
    public Integer[] getVoiceIndexesInGroupFromVoice(Integer voice) throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException {
        try {
            return pc.getVoiceIndexesInGroupFromVoice(preset, voice);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Integer[] getVoiceIndexesInGroup(Integer group) throws PresetEmptyException, NoSuchPresetException, NoSuchGroupException {
        try {
            return pc.getVoiceIndexesInGroup(preset, group);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public int numPresetZones() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.numPresetZones(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public int numVoices() throws NoSuchPresetException, PresetEmptyException {
        try {
            return pc.numVoices(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Integer[] getGroupParams(Integer group, Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException {
        try {
            return pc.getGroupParams(preset, group, ids);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public int numZones(Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
        try {
            return pc.numZones(preset, voice);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public int compareTo(Object o) {
        if (o instanceof ReadablePreset) {
            Integer p = ((ReadablePreset) o).getPresetNumber();

            if (p.intValue() < preset.intValue())
                return 1;
            else if (p.intValue() > preset.intValue())
                return -1;
        }
        return 0;
    }

    public ZCommand[] getZCommands() {
        return cmdProviderHelper.getCommandObjects(this);
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

    public void setPresetEditingMediator(DesktopEditingMediator pem) {
        this.dem = pem;
    }

    public DesktopEditingMediator getPresetEditingMediator() {
        return dem;
    }
}

