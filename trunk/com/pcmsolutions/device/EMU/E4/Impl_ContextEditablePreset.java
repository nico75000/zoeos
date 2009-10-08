package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.PresetChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.PresetInitializeEvent;
import com.pcmsolutions.device.EMU.E4.events.PresetRefreshEvent;
import com.pcmsolutions.device.EMU.E4.events.VoiceChangeEvent;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.DesktopEditingMediator;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.threads.ZDefaultThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Impl_ContextEditablePreset extends Impl_ContextBasicEditablePreset implements ContextEditablePreset, IconAndTipCarrier, Comparable {

    static {
        PresetClassManager.addPresetClass(Impl_ContextEditablePreset.class, null, "Editable Preset");
    }

    public Impl_ContextEditablePreset(PresetContext pc, Integer preset) {
        super(pc, preset);
    }

    public Impl_ContextEditablePreset(PresetContext pc, Integer preset, DesktopEditingMediator dem) {
        super(pc, preset, dem);
    }

    public ReadablePreset getMostCapableNonContextEditablePresetDowngrade() {
        return this.getContextBasicEditablePresetDowngrade();
    }

    public void performOpenAction() {
        new ZDefaultThread() {
            public void run() {
                try {
                    assertPresetInitialized();
                    getDeviceContext().getViewManager().openPreset(Impl_ContextEditablePreset.this);
                } catch (NoSuchPresetException e) {
                }
            }
        }.start();
    }

    public void newPreset(Integer preset, String name) throws NoSuchPresetException {
        try {
            pc.newPreset(preset, name);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void newPreset(Integer preset, String name, IsolatedPreset ip) throws NoSuchPresetException {
        try {
            pc.newPreset(ip, preset, name);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }


    public void sortVoices(Integer[] ids) throws PresetEmptyException, NoSuchPresetException {
        try {
            pc.sortVoices(preset, ids);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void sortLinks(Integer[] ids) throws PresetEmptyException, NoSuchPresetException {
        try {
            pc.sortLinks(preset, ids);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void sortZones(Integer[] ids) throws PresetEmptyException, NoSuchPresetException {
        try {
            pc.sortZones(preset, ids);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void splitVoice(Integer voice, int splitKey) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException, ParameterValueOutOfRangeException, NoSuchVoiceException {
        try {
            pc.splitVoice(preset, voice, splitKey);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public boolean equals(Object o) {
        ContextEditablePreset p;
        if (o instanceof ContextEditablePreset) {
            p = (ContextEditablePreset) o;
            if (p.getPresetNumber().equals(preset) && p.getPresetContext().equals(pc))
                return true;
        } else    // try and compare using just preset number
            if (o instanceof Integer) {
                if (o.equals(preset))
                    return true;
            }

        return false;
    }

    public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException {
        return new Impl_PresetEditableParameterModel(pc.getDeviceParameterContext().getPresetContext().getParameterDescriptor(id));
    }

    // LINK
    public Integer newLink(IsolatedPreset.IsolatedLink il) throws NoSuchPresetException, TooManyVoicesException, NoSuchContextException, PresetEmptyException {
        try {
            return pc.newLink(preset, il);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public EditableParameterModel[] getAllEditableParameterModels() {
        List pds = pc.getDeviceParameterContext().getPresetContext().getAllParameterDescriptors();
        EditableParameterModel[] outModels = new EditableParameterModel[pds.size()];
        for (int i = 0,n = pds.size(); i < n; i++)
            outModels[i] = new Impl_PresetEditableParameterModel((GeneralParameterDescriptor) pds.get(i));
        return outModels;
    }

    class Impl_PresetEditableParameterModel extends AbstractPresetEditableParameterModel {
        protected Integer[] id;

        public Impl_PresetEditableParameterModel(GeneralParameterDescriptor pd) {
            super(pd);
            id = new Integer[]{pd.getId()};
            addPresetListener(pla);
        }

        private Integer getPreset() {
            return preset;
        }

        public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
            try {
                setPresetParams(new Integer[]{pd.getId()}, new Integer[]{value});
                return;
            } catch (NoSuchPresetException e) {
            } catch (PresetEmptyException e) {
            } catch (IllegalParameterIdException e) {
            }
            throw new ParameterUnavailableException();
        }

        public void zDispose() {
            super.zDispose();
            removePresetListener(pla);
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

    public void combineVoices(Integer group) throws NoSuchPresetException, PresetEmptyException {
        try {
            pc.combineVoices(preset, group);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void purgeZones() throws PresetEmptyException, NoSuchPresetException {
        try {
            pc.purgeZones(preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void applySampleToPreset(Integer sample, int mode) throws NoSuchPresetException, PresetEmptyException, ParameterValueOutOfRangeException, TooManyVoicesException {
        try {
            pc.applySampleToPreset(preset, sample, mode);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void combineVoiceGroup(Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
        Integer group = null;
        try {
            group = pc.getVoiceParams(preset, voice, new Integer[]{IntPool.get(37)})[0];
            pc.combineVoices(preset, group);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        } catch (IllegalParameterIdException e) {
            throw new NoSuchVoiceException();
        }
    }

    public Integer newVoice(IsolatedPreset.IsolatedVoice iv) throws NoSuchPresetException, TooManyZonesException, TooManyVoicesException, PresetEmptyException {
        try {
            return pc.newVoice(preset, iv);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void copyLink(Integer srcLink) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException {
        try {
            pc.copyLink(preset, srcLink, preset);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void copyVoice(Integer srcVoice, Integer group) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyVoicesException {
        try {
            pc.copyVoice(preset, srcVoice, preset, group);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void expandVoice(Integer voice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyVoicesException {
        try {
            pc.expandVoice(preset, voice);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void getVoiceMultiSample(Integer srcVoice, Integer destVoice) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
        pc.getVoiceMultiSample(preset, srcVoice, destVoice, preset);
    }

    public Integer newLinks(Integer num, Integer[] presetNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException {
        try {
            return pc.newLinks(preset, num, presetNums);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Integer newVoices(Integer num, Integer[] sampleNums) throws NoSuchPresetException, PresetEmptyException, TooManyVoicesException {
        try {
            return pc.newVoices(preset, num, sampleNums);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Integer newZones(Integer voice, Integer num) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyZonesException {
        try {
            return pc.newZones(preset, voice, num);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void rmvLinks(Integer[] links) throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException {
        try {
            pc.rmvLinks(preset, links);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void rmvVoices(Integer[] voices) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, CannotRemoveLastVoiceException {
        try {
            pc.rmvVoices(preset, voices);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void rmvZones(Integer voice, Integer[] zones) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchZoneException {
        try {
            pc.rmvZones(preset, voice, zones);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void setLinksParam(Integer[] links, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, ParameterValueOutOfRangeException {
        try {
            pc.setLinksParam(preset, links, id, values);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    // ZONE
    public Integer newZone(Integer voice, IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws NoSuchPresetException, TooManyZonesException, PresetEmptyException, NoSuchVoiceException {
        try {
            return pc.newZone(preset, voice, iz);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public ContextBasicEditablePreset getContextBasicEditablePresetDowngrade() {
        Impl_ContextBasicEditablePreset np = new Impl_ContextBasicEditablePreset(pc, preset);
        np.dem = dem;
        np.stringFormatExtended = stringFormatExtended;
        return np;
    }

    public Map offsetLinkIndexes(Integer offset, boolean user) throws PresetEmptyException, NoSuchPresetException {
        try {
            return pc.offsetLinkIndexes(preset, offset, user);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Map offsetSampleIndexes(Integer offset, boolean user) throws PresetEmptyException, NoSuchPresetException {
        try {
            return pc.offsetSampleIndexes(preset, offset, user);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Map remapLinkIndexes(Map translationMap) throws PresetEmptyException, NoSuchPresetException {
        try {
            return pc.remapLinkIndexes(preset, translationMap);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public Map remapSampleIndexes(Map translationMap) throws PresetEmptyException, NoSuchPresetException {
        try {
            return pc.remapSampleIndexes(preset, translationMap, null);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void setPresetParams(Integer[] ids, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, ParameterValueOutOfRangeException {
        try {
            pc.setPresetParams(preset, ids, values);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void setVoicesParam(Integer[] voices, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, ParameterValueOutOfRangeException {
        try {
            pc.setVoicesParam(preset, voices, id, values);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void setGroupParamFromVoice(Integer voice, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, ParameterValueOutOfRangeException {
        try {
            pc.setGroupParamFromVoice(preset, voice, id, value);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void setGroupParam(Integer group, Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchGroupException, ParameterValueOutOfRangeException {
        try {
            pc.setGroupParam(preset, group, id, value);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public void setZonesParam(Integer voice, Integer[] zones, Integer id, Integer[] values) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, ParameterValueOutOfRangeException {
        try {
            pc.setZonesParam(preset, voice, zones, id, values);
        } catch (NoSuchContextException e) {
            throw new NoSuchPresetException(preset);
        }
    }

    public ContextEditablePreset.EditableVoice getEditableVoice(final Integer voice) {
        return new Impl_EditableVoice(voice);
    }

    private class Impl_EditableVoice extends Impl_ReadableVoice implements EditableVoice {
        protected boolean groupMode = false;

        public Impl_EditableVoice(Integer voice) {
            super(voice);
        }

        public ContextEditablePreset.EditableVoice duplicate() {
            return new Impl_EditableVoice(voice);
        }

        public void performOpenAction() {
            new ZDefaultThread() {
                public void run() {
                    try {
                        assertPresetInitialized();
                        if (getDeviceContext().getDevicePreferences().ZPREF_useTabbedVoicePanel.getValue())
                            getDeviceContext().getViewManager().openTabbedVoice(Impl_ContextEditablePreset.Impl_EditableVoice.this, getDeviceContext().getDevicePreferences().ZPREF_groupEnvelopesWhenVoiceTabbed.getValue(), true);
                        else
                            getDeviceContext().getViewManager().openVoice(Impl_ContextEditablePreset.Impl_EditableVoice.this, true);
                    } catch (NoSuchPresetException e) {
                    }
                }
            }.start();
        }

        public void setGroupMode(boolean groupMode) {
            this.groupMode = groupMode;
        }

        public boolean getGroupMode() {
            return groupMode;
        }

        public boolean trySetOriginalKeyFromSampleName() throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
            try {
                String name = pc.getRootSampleContext().getSampleName(pc.getVoiceParams(preset, voice, new Integer[]{ID.sample})[0]);
                return pc.trySetOriginalKeyFromName(preset, voice, name);
            } catch (NoSuchContextException e) {
                throw new NoSuchPresetException(preset);
            } catch (IllegalParameterIdException e) {
            } catch (NoSuchSampleException e) {
            } catch (SampleEmptyException e) {
            }
            return false;
        }

        public void setVoicesParam(Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, ParameterValueOutOfRangeException {
            if (groupMode)
                Impl_ContextEditablePreset.this.setGroupParamFromVoice(voice, id, value);
            else
                Impl_ContextEditablePreset.this.setVoicesParam(new Integer[]{voice}, id, new Integer[]{value});
        }

        public int numZones() throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
            return Impl_ContextEditablePreset.this.numZones(voice);
        }

        public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException {
            return new Impl_ContextEditablePreset.Impl_EditableVoice.Impl_VoiceEditableParameterModel(pc.getDeviceParameterContext().getVoiceContext().getParameterDescriptor(id));
        }

        public void removeVoice() throws NoSuchPresetException, NoSuchVoiceException, PresetEmptyException, CannotRemoveLastVoiceException {
            Impl_ContextEditablePreset.this.rmvVoices(new Integer[]{voice});
        }

        public void splitVoice(int splitKey) throws NoSuchContextException, NoSuchPresetException, PresetEmptyException, TooManyVoicesException, ParameterValueOutOfRangeException, NoSuchVoiceException {
            Impl_ContextEditablePreset.this.splitVoice(voice, splitKey);
        }

        public void expandVoice() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException, TooManyVoicesException {
            Impl_ContextEditablePreset.this.expandVoice(voice);
        }

        public void combineVoiceGroup() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException, TooManyVoicesException {
            Impl_ContextEditablePreset.this.combineVoiceGroup(voice);
        }

        public void copyVoice() throws NoSuchPresetException, NoSuchVoiceException, PresetEmptyException, CannotRemoveLastVoiceException, TooManyVoicesException {
            try {
                Impl_ContextEditablePreset.this.copyVoice(voice, this.getVoiceParams(new Integer[]{IntPool.get(37)})[0]);
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
                Impl_ContextEditablePreset.this.copyVoice(voice, IntPool.get(0));
            }
        }

        public void newZones(Integer num) throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, TooManyZonesException {
            Impl_ContextEditablePreset.this.newZones(voice, num);
        }

        public PresetContext getPresetContext() {
            return Impl_ContextEditablePreset.this.getPresetContext();
        }

        public Integer[] getVoiceParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException {
            return Impl_ContextEditablePreset.this.getVoiceParams(voice, ids);
        }

        public Integer getVoiceNumber() {
            return voice;
        }

        public EditableZone getEditableZone(final Integer zone) {
            return new Impl_EditableZone(zone);
        }

        class Impl_EditableZone extends Impl_ReadableZone implements EditableZone {
            public Impl_EditableZone(Integer zone) {
                super(zone);
            }

            public void setZonesParam(Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException, ParameterValueOutOfRangeException {
                Impl_ContextEditablePreset.this.setZonesParam(voice, new Integer[]{zone}, id, new Integer[]{value});
            }

            public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException {
                return new Impl_EditableZone.Impl_ZoneEditableParameterModel(pc.getDeviceParameterContext().getVoiceContext().getParameterDescriptor(id));
            }

            public boolean trySetOriginalKeyFromSampleName() throws NoSuchPresetException, PresetEmptyException, NoSuchVoiceException, NoSuchZoneException {
                try {
                    String name = pc.getRootSampleContext().getSampleName(pc.getZoneParams(preset, voice, zone, new Integer[]{ID.sample})[0]);
                    return pc.trySetOriginalKeyFromName(preset, voice, zone, name);
                } catch (NoSuchContextException e) {
                    throw new NoSuchPresetException(preset);
                } catch (IllegalParameterIdException e) {
                } catch (NoSuchSampleException e) {
                } catch (SampleEmptyException e) {
                }
                return false;
            }

            public void removeZone() throws NoSuchPresetException, NoSuchVoiceException, PresetEmptyException, NoSuchZoneException {
                Impl_ContextEditablePreset.this.rmvZones(voice, new Integer[]{zone});
            }

            public Integer[] getZoneParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException {
                return Impl_ContextEditablePreset.this.getZoneParams(voice, zone, ids);
            }

            public Integer getVoiceNumber() {
                return voice;
            }

            public Integer getZoneNumber() {
                return zone;
            }

            public PresetContext getPresetContext() {
                return Impl_ContextEditablePreset.this.getPresetContext();
            }


            public ZCommand[] getZCommands() {
                return cmdProviderHelper.getCommandObjects(this);
            }

            class Impl_ZoneEditableParameterModel extends AbstractPresetEditableParameterModel {
                private Integer[] id;

                public Impl_ZoneEditableParameterModel(GeneralParameterDescriptor pd) {
                    super(pd);
                    id = new Integer[]{pd.getId()};
                }

                private Integer getPreset() {
                    return preset;
                }

                private Integer getVoice() {
                    return voice;
                }

                private Integer getZone() {
                    return zone;
                }

                public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
                    try {
                        setZonesParam(id[0], value);
                    } catch (NoSuchPresetException e) {
                        throw new ParameterUnavailableException();
                    } catch (PresetEmptyException e) {
                        throw new ParameterUnavailableException();
                    } catch (IllegalParameterIdException e) {
                        throw new ParameterUnavailableException();
                    } catch (NoSuchVoiceException e) {
                        throw new ParameterUnavailableException();
                    } catch (NoSuchZoneException e) {
                        throw new ParameterUnavailableException();
                    }
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

        public ZCommand[] getZCommands() {
            return ContextEditablePreset.EditableVoice.cmdProviderHelper.getCommandObjects(this);
        }

        class Impl_VoiceEditableParameterModel extends AbstractPresetEditableParameterModel {
            private Integer[] id;
            private boolean isFilterId;

            public Impl_VoiceEditableParameterModel(GeneralParameterDescriptor pd) {
                super(pd);
                id = new Integer[]{pd.getId()};
                isFilterId = isFilterId();
                if (isFilterId)
                    setFilterType();
                addPresetListener(pla);
            }

            private Integer getPreset() {
                return preset;
            }

            private boolean getGroupMode() {
                return groupMode;
            }

            private Integer getVoice() {
                return voice;
            }

            private void setFilterType() {
                try {
                    ((FilterParameterDescriptor) pd).setFilterType(Impl_EditableVoice.this.getVoiceParams(new Integer[]{IntPool.get(82)})[0]);
                    Impl_ContextEditablePreset.Impl_EditableVoice.Impl_VoiceEditableParameterModel.this.fireChanged();
                } catch (NoSuchPresetException e) {
                    // e.printStackTrace();
                } catch (PresetEmptyException e) {
                    // e.printStackTrace();
                } catch (IllegalParameterIdException e) {
                    // e.printStackTrace();
                } catch (NoSuchVoiceException e) {
                    //  e.printStackTrace();
                }
            }

            protected boolean isFilterId() {
                int idv = id[0].intValue();
                if (idv > 82 && idv < 93)
                    return true;
                return false;
            }

            public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
                try {
                    setVoicesParam(id[0], value);
                } catch (NoSuchPresetException e) {
                    throw new ParameterUnavailableException();
                } catch (PresetEmptyException e) {
                    throw new ParameterUnavailableException();
                } catch (NoSuchVoiceException e) {
                    throw new ParameterUnavailableException();
                } catch (IllegalParameterIdException e) {
                    throw new ParameterUnavailableException();
                }
            }

            public void zDispose() {
                super.zDispose();
                removePresetListener(pla);
            }

            public Integer getValue() throws ParameterUnavailableException {
                try {
                    return getVoiceParams(id)[0];
                } catch (Exception e) {
                    //e.printStackTrace();
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

            /*public boolean isEditChainableWith(Object o) {
                if (groupMode)
                    return false;
                else
                    return super.isEditChainableWith(o);
            } */

            protected PresetListenerAdapter pla = new PresetListenerAdapter() {
                public void presetInitialized(PresetInitializeEvent ev) {
                    Impl_ContextEditablePreset.Impl_EditableVoice.Impl_VoiceEditableParameterModel.this.fireChanged();
                }

                public void presetRefreshed(PresetRefreshEvent ev) {
                    Impl_ContextEditablePreset.Impl_EditableVoice.Impl_VoiceEditableParameterModel.this.fireChanged();
                }

                public void voiceChanged(VoiceChangeEvent ev) {
                    if (ev.getVoice().equals(voice)) {
                        if (isFilterId && ev.containsId(IntPool.get(82))) {
                            setFilterType();
                            return;
                        } else if (ev.containsId(id[0]))
                            Impl_ContextEditablePreset.Impl_EditableVoice.Impl_VoiceEditableParameterModel.this.fireChanged();
                    }
                }
            };

            private Integer getGroup() throws PresetEmptyException, NoSuchPresetException, IllegalParameterIdException, NoSuchVoiceException {
                return Impl_EditableVoice.this.getVoiceParams(new Integer[]{IntPool.get(37)})[0];
            }

            private Integer[] getVoiceIndexesInGroup() {
                try {
                    return Impl_EditableVoice.this.getVoiceIndexesInGroup();
                } catch (PresetEmptyException e) {
                } catch (NoSuchContextException e) {
                } catch (NoSuchVoiceException e) {
                } catch (NoSuchPresetException e) {
                }
                return new Integer[0];
            }
            /*public Object[] getWrappedObjects() {
                if ( groupMode){
                         Integer[] vig = getVoiceIndexesInGroup();
                }
                return new Object[]{this};
            } */
        }
    };

    public ContextEditablePreset.EditableLink getEditableLink(final Integer link) {
        return new Impl_EditableLink(link);
    }

    private abstract class AbstractPresetEditableParameterModel extends AbstractEditableParameterModel {
        public AbstractPresetEditableParameterModel(GeneralParameterDescriptor pd) {
            super(pd);
        }

        public boolean isEditChainableWith(Object o) {
            if (o instanceof AbstractPresetEditableParameterModel)
                return true;
            else if (o instanceof EditableParameterModelGroup && ((EditableParameterModelGroup) o).getWrappedObjects()[0] instanceof AbstractPresetEditableParameterModel)
                return true;
            return false;
        }

        public void setValue(EditChainValueProvider ecvp, EditableParameterModel[] modelChain) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
            try {
                setDiversePresetParams(modelChain, ecvp);
            } catch (NoSuchContextException e) {
                throw new ParameterUnavailableException(e.getMessage());
            } catch (NoSuchPresetException e) {
                throw new ParameterUnavailableException(e.getMessage());
            } catch (PresetEmptyException e) {
                throw new ParameterUnavailableException(e.getMessage());
            } catch (IllegalParameterIdException e) {
                throw new ParameterUnavailableException(e.getMessage());
            } catch (NoSuchVoiceException e) {
                throw new ParameterUnavailableException(e.getMessage());
            } catch (NoSuchLinkException e) {
                throw new ParameterUnavailableException(e.getMessage());
            } catch (NoSuchZoneException e) {
                throw new ParameterUnavailableException(e.getMessage());
            } catch (NoSuchGroupException e) {
                throw new ParameterUnavailableException(e.getMessage());
            }
        }

    }

    private static PresetContext.AbstractPresetParameterProfile[] getParameterProfiles(final EditableParameterModel[] models, final EditableParameterModel.EditChainValueProvider ecvp) {
        ArrayList profs = new ArrayList();
        for (int i = 0; i < models.length; i++) {
            if (models[i] instanceof ParameterModelWrapper)
                profs.addAll(taskGetParameterProfiles(ParameterModelUtilities.extractEditableParameterModels(((ParameterModelWrapper) models[i]).getWrappedObjects()), ecvp));
            else
                profs.addAll(taskGetParameterProfiles(new EditableParameterModel[]{models[i]}, ecvp));
        }
        return (PresetContext.AbstractPresetParameterProfile[]) profs.toArray(new PresetContext.AbstractPresetParameterProfile[profs.size()]);
    }

    private static ArrayList taskGetParameterProfiles(final EditableParameterModel[] models, final EditableParameterModel.EditChainValueProvider ecvp) {
        ArrayList profs = new ArrayList();
        for (int i = 0; i < models.length; i++) {
            final int f_i = i;
            if (models[i] instanceof Impl_PresetEditableParameterModel) {
                try {
                    final Integer preset = ((Impl_PresetEditableParameterModel) models[f_i]).getPreset();
                    final Integer id = ((Impl_PresetEditableParameterModel) models[f_i]).id[0];
                    final Integer val = ecvp.getValue(models[f_i], models[0]);

                    profs.add(new PresetContext.PresetParameterProfile() {

                        public Integer getPreset() {
                            return preset;
                        }

                        public Integer getId() {
                            return id;
                        }

                        public Integer getValue() {
                            return val;
                        }
                    });
                } catch (Exception e) {
                    continue;
                }
            } else if (models[i] instanceof Impl_EditableVoice.Impl_VoiceEditableParameterModel) {
                final Impl_EditableVoice.Impl_VoiceEditableParameterModel voc = (Impl_EditableVoice.Impl_VoiceEditableParameterModel) models[f_i];
                if (voc.getGroupMode() == true) {
                    try {
                        final Integer preset = voc.getPreset();
                        final Integer group = voc.getGroup();
                        final Integer id = voc.id[0];
                        final Integer val = ecvp.getValue(models[f_i], models[0]);
                        profs.add(new PresetContext.GroupParameterProfile() {

                            public Integer getPreset() {
                                return preset;
                            }

                            public Integer getId() {
                                return id;
                            }

                            public Integer getValue() {
                                return val;
                            }

                            public Integer getGroup() {
                                return group;
                            }
                        });
                    } catch (Exception e) {
                        continue;
                    }
                } else
                    try {
                        final Integer preset = voc.getPreset();
                        final Integer voice = voc.getVoice();
                        final Integer id = voc.id[0];
                        final Integer val = ecvp.getValue(models[f_i], models[0]);
                        profs.add(new PresetContext.VoiceParameterProfile() {

                            public Integer getPreset() {
                                return preset;
                            }

                            public Integer getId() {
                                return id;
                            }

                            public Integer getValue() {
                                return val;
                            }

                            public Integer getVoice() {
                                return voice;
                            }
                        });
                    } catch (Exception e) {
                        continue;
                    }
            } else if (models[i] instanceof Impl_EditableLink.Impl_LinkEditableParameterModel) {
                try {
                    final Integer preset = ((Impl_EditableLink.Impl_LinkEditableParameterModel) models[f_i]).getPreset();
                    final Integer link = ((Impl_EditableLink.Impl_LinkEditableParameterModel) models[f_i]).getLink();
                    final Integer[] ids = ((Impl_EditableLink.Impl_LinkEditableParameterModel) models[f_i]).id;
                    final Integer val = ecvp.getValue(models[f_i], models[0]);
                    profs.add(new PresetContext.LinkParameterProfile() {

                        public Integer getPreset() {
                            return preset;
                        }

                        public Integer getId() {
                            return ids[0];
                        }

                        public Integer getValue() {
                            return val;
                        }

                        public Integer getLink() {
                            return link;
                        }
                    });
                } catch (Exception e) {
                    continue;
                }
            } else if (models[i] instanceof Impl_EditableVoice.Impl_EditableZone.Impl_ZoneEditableParameterModel) {
                try {
                    final Integer preset = ((Impl_EditableVoice.Impl_EditableZone.Impl_ZoneEditableParameterModel) models[f_i]).getPreset();
                    final Integer voice = ((Impl_EditableVoice.Impl_EditableZone.Impl_ZoneEditableParameterModel) models[f_i]).getVoice();
                    final Integer zone = ((Impl_EditableVoice.Impl_EditableZone.Impl_ZoneEditableParameterModel) models[f_i]).getZone();
                    final Integer[] ids = ((Impl_EditableVoice.Impl_EditableZone.Impl_ZoneEditableParameterModel) models[f_i]).id;
                    final Integer val = ecvp.getValue(models[f_i], models[0]);
                    profs.add(new PresetContext.ZoneParameterProfile() {

                        public Integer getPreset() {
                            return preset;
                        }

                        public Integer getId() {
                            return ids[0];
                        }

                        public Integer getValue() {
                            return val;
                        }

                        public Integer getVoice() {
                            return voice;
                        }

                        public Integer getZone() {
                            return zone;
                        }
                    });
                } catch (Exception e) {
                    continue;
                }
            } else
                throw new IllegalArgumentException("illegal EditableParameterModel type");
        }
        return profs;
    }

    public void setDiversePresetParams(EditableParameterModel[] models, EditableParameterModel.EditChainValueProvider ecvp) throws NoSuchContextException, NoSuchPresetException, ParameterValueOutOfRangeException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchLinkException, NoSuchZoneException, ParameterUnavailableException, NoSuchGroupException {
        pc.setDiversePresetParams(getParameterProfiles(models, ecvp));
    }

    class Impl_EditableLink extends Impl_ReadableLink implements EditableLink {
        public Impl_EditableLink(Integer link) {
            super(link);
        }

        public void setLinksParam(Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, ParameterValueOutOfRangeException {
            Impl_ContextEditablePreset.this.setLinksParam(new Integer[]{link}, id, new Integer[]{value});
        }

        public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException {
            return new Impl_ContextEditablePreset.Impl_EditableLink.Impl_LinkEditableParameterModel(pc.getDeviceParameterContext().getVoiceContext().getParameterDescriptor(id));
        }

        public PresetContext getPresetContext() {
            return Impl_ContextEditablePreset.this.getPresetContext();
        }

        public void removeLink() throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException {
            Impl_ContextEditablePreset.this.rmvLinks(new Integer[]{link});
        }

        public void copyLink() throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException {
            Impl_ContextEditablePreset.this.copyLink(link);
        }

        public Integer[] getLinkParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException {
            return Impl_ContextEditablePreset.this.getLinkParams(link, ids);
        }

        public Integer getLinkNumber() {
            return link;
        }

        public ZCommand[] getZCommands() {
            return cmdProviderHelper.getCommandObjects(this);
        }

        class Impl_LinkEditableParameterModel extends AbstractPresetEditableParameterModel {
            private Integer[] id;

            public Impl_LinkEditableParameterModel(GeneralParameterDescriptor pd) {
                super(pd);
                id = new Integer[]{pd.getId()};
            }

            private Integer getPreset() {
                return preset;
            }

            private Integer getLink() {
                return link;
            }

            public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
                try {
                    setLinksParam(id[0], value);
                } catch (NoSuchPresetException e) {
                    throw new ParameterUnavailableException();
                } catch (PresetEmptyException e) {
                    throw new ParameterUnavailableException();
                } catch (NoSuchLinkException e) {
                    throw new ParameterUnavailableException();
                } catch (IllegalParameterIdException e) {
                    throw new ParameterUnavailableException();
                }
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

    public ZCommand[] getZCommands() {
        return ZUtilities.concatZCommands(super.getZCommands(), cmdProviderHelper.getCommandObjects(this));
    }
}

