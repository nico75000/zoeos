package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

class Impl_ContextEditablePreset extends Impl_ContextBasicEditablePreset implements ContextEditablePreset, ZCommandProvider, IconAndTipCarrier, Comparable {

    static {
        PresetClassManager.addPresetClass(Impl_ContextEditablePreset.class, null, "Editable Preset");
    }

    public Impl_ContextEditablePreset(PresetContext pc, Integer preset) {
        super(pc, preset);
    }

    public ReadablePreset getMostCapableNonContextEditablePreset() {
        return this.getContextBasicEditablePresetDowngrade();
    }

    public void performOpenAction(final boolean activate) {
        try {
            if (pc.isInitialized(preset))
                try {
                    pc.refreshIfEmpty(preset).post(new Callback() {
                        public void result(Exception e, boolean wasCancelled) {
                            try {
                                getDeviceContext().getViewManager().openPreset(Impl_ContextEditablePreset.this, activate).post(new Callback() {
                                    public void result(Exception e, boolean wasCancelled) {
                                        if (!wasCancelled)
                                            PresetContextMacros.optionToNewEmptyPreset(Impl_ContextEditablePreset.this);
                                    }
                                });
                            } catch (ResourceUnavailableException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            else
                pc.assertInitialized(preset, true).post(new Callback() {
                    public void result(Exception e, boolean wasCancelled) {
                        try {
                            if (!wasCancelled) {
                                getDeviceContext().getViewManager().openPreset(Impl_ContextEditablePreset.this, activate).send(0);
                                PresetContextMacros.optionToNewEmptyPreset(Impl_ContextEditablePreset.this);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    public void newPreset(Integer preset, String name) throws PresetException {
        try {
            pc.newContent(preset, name).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void newPreset(Integer preset, String name, IsolatedPreset ip) throws PresetException {
        try {
            pc.dropContent(ip, preset, name, ProgressCallback.DUMMY).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void sortVoices(Integer[] ids) throws PresetException {
        try {
            pc.sortVoices(preset, ids).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void sortLinks(Integer[] ids) throws PresetException {
        try {
            pc.sortLinks(preset, ids).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void sortZones(Integer[] ids) throws PresetException {
        try {
            pc.sortZones(preset, ids).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void splitVoice(Integer voice, int splitKey) throws PresetException {
        try {
            pc.splitVoice(preset, voice, splitKey).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public boolean equals(Object o) {
        ContextEditablePreset p;
        if (o instanceof ContextEditablePreset) {
            p = (ContextEditablePreset) o;
            if (p.getIndex().equals(preset) && p.getPresetContext().equals(pc))
                return true;
        } else    // try and compare using just preset number
            if (o instanceof Integer) {
                if (o.equals(preset))
                    return true;
            }

        return false;
    }

    public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException {
        try {
            return new Impl_PresetEditableParameterModel(pc.getDeviceParameterContext().getPresetContext().getParameterDescriptor(id));
        } catch (DeviceException e) {
            throw new ParameterException(e.getMessage());
        }
    }

    // LINK
    public void newLink(IsolatedPreset.IsolatedLink il) throws PresetException {
        try {
            pc.newLink(preset, il).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    class Impl_PresetEditableParameterModel extends AbstractPresetEditableParameterModel {
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

        public Impl_PresetEditableParameterModel(GeneralParameterDescriptor pd) {
            super(pd);
            id = new Integer[]{pd.getId()};
            addListener(pla);
        }

        public void setValue(Integer value) throws ParameterUnavailableException {
            try {
                this.value = null;
                setPresetParam(pd.getId(), value);
                return;
            } catch (PresetException e) {
            }
            throw new ParameterUnavailableException(pd.getId());
        }

        public void offsetValue(Integer offset) throws ParameterUnavailableException {
            try {
                this.value = null;
                offsetPresetParam(pd.getId(), offset);
                return;
            } catch (PresetException e) {
                throw new ParameterUnavailableException(pd.getId());
            }
        }

        public void offsetValue(Double offsetAsFOR) throws ParameterUnavailableException {
            try {
                this.value = null;
                offsetPresetParam(pd.getId(), offsetAsFOR);
                return;
            } catch (PresetException e) {
                throw new ParameterUnavailableException(pd.getId());
            }
        }

        public void zDispose() {
            super.zDispose();
            removeListener(pla);
        }

        public String getToolTipText() {
            try {
                return getValueString();
            } catch (ParameterException e) {
            }
            return super.getToolTipText();
        }

        protected PresetListenerAdapter pla = new PresetListenerAdapter() {

            public void presetRefreshed(PresetInitializeEvent ev) {
                value = null;
                fireChanged();
            }

            public void presetChanged(PresetChangeEvent ev) {
                if (ev.getIndex().equals(preset) && ev.containsId(id[0])) {
                    value = null;
                    fireChanged();
                }
            }
        };
    }

    public void combineVoices(Integer group) throws EmptyException, PresetException {
        try {
            pc.combineVoices(preset, group).post();
        } catch (Exception e) {
            throw new PresetException(e.getMessage());
        } 
    }

    public void purgeZones() throws PresetException {
        try {
            pc.purgeZones(preset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void purgeLinks() throws PresetException {
        try {
            pc.purgeLinks(preset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(preset, e.getMessage());
        }
    }

    public void purgeEmpties() throws PresetException {
        try {
            pc.purgeEmpties(preset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void applySampleToPreset(Integer sample, int mode) throws PresetException {
        try {
            pc.applySampleToPreset(preset, sample, mode).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void combineVoiceGroup(Integer voice) throws EmptyException, PresetException, ParameterException {
        Integer group = null;
        try {
            group = pc.getVoiceParams(preset, voice, new Integer[]{IntPool.get(37)})[0];
            pc.combineVoices(preset, group).post();
        } catch (Exception e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void newVoice(IsolatedPreset.IsolatedVoice iv) throws PresetException {
        try {
            pc.newVoice(preset, iv).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void copyLink(Integer srcLink) throws PresetException {
        try {
            pc.copyLink(preset, srcLink, preset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void copyVoice(Integer srcVoice, Integer group) throws PresetException {
        try {
            pc.copyVoice(preset, srcVoice, preset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void expandVoice(Integer voice) throws PresetException {
        try {
            pc.expandVoice(preset, voice).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void newLinks(Integer[] presetNums) throws PresetException {
        try {
            pc.newLinks(preset, presetNums).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void newVoices(Integer num, Integer[] sampleNums) throws PresetException {
        try {
            pc.newVoices(preset, sampleNums).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void newZones(Integer voice, Integer num) throws PresetException {
        try {
            pc.newZones(preset, voice, num).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void rmvLinks(Integer[] links) throws PresetException {
        try {
            pc.rmvLinks(preset, links).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void rmvVoices(Integer[] voices) throws PresetException {
        try {
            pc.rmvVoices(preset, voices).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void rmvZones(Integer voice, Integer[] zones) throws PresetException {
        try {
            pc.rmvZones(preset, voice, zones).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void setLinkParam(Integer link, Integer id, Integer value) throws PresetException {
        try {
            pc.setLinkParam(preset, link, id, value).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetLinkParam(Integer link, Integer id, Integer offset) throws PresetException {
        try {
            pc.offsetLinkParam(preset, link, id, offset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetLinkParam(Integer link, Integer id, Double offsetAsFOR) throws PresetException {
        try {
            pc.offsetLinkParam(preset, link, id, offsetAsFOR).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (IllegalParameterIdException e) {
            throw new PresetException(e.getMessage());
        }
    }

    // ZONE
    public void newZone(Integer voice, IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws PresetException {
        try {
            pc.newZone(preset, voice, iz).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public ContextBasicEditablePreset getContextBasicEditablePresetDowngrade() {
        Impl_ContextBasicEditablePreset np = new Impl_ContextBasicEditablePreset(pc, preset);
        np.stringFormatExtended = stringFormatExtended;
        return np;
    }

    public void offsetLinkIndexes(Integer offset, boolean user) throws PresetException {
        try {
            pc.offsetLinkIndexes(preset, offset, user).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetSampleIndexes(Integer offset, boolean user) throws PresetException {
        try {
            pc.offsetSampleIndexes(preset, offset, user).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void remapLinkIndexes(Map translationMap) throws PresetException {
        try {
            pc.remapLinkIndexes(preset, translationMap).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void remapSampleIndexes(Map translationMap) throws PresetException {
        try {
            pc.remapSampleIndexes(preset, translationMap, null).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void setPresetParam(Integer id, Integer value) throws PresetException {
        try {
            pc.setPresetParam(preset, id, value).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetPresetParam(Integer id, Integer offset) throws PresetException {
        try {
            pc.offsetPresetParam(preset, id, offset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetPresetParam(Integer id, Double offsetAsFOR) throws PresetException {
        try {
            pc.offsetPresetParam(preset, id, offsetAsFOR).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (IllegalParameterIdException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void setVoiceParam(Integer voice, Integer id, Integer value) throws PresetException {
        try {
            pc.setVoiceParam(preset, voice, id, value).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetVoiceParam(Integer voice, Integer id, Integer offset) throws PresetException {
        try {
            pc.offsetVoiceParam(preset, voice, id, offset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetVoiceParam(Integer voice, Integer id, Double offsetAsFOR) throws PresetException {
        try {
            pc.offsetVoiceParam(preset, voice, id, offsetAsFOR).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (IllegalParameterIdException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void setGroupParamFromVoice(Integer voice, Integer id, Integer value) throws PresetException {
        try {
            pc.setGroupParamFromVoice(preset, voice, id, value).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetGroupParamFromVoice(Integer voice, Integer id, Integer offset) throws PresetException {
        try {
            pc.offsetGroupParamFromVoice(preset, voice, id, offset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetGroupParamFromVoice(Integer voice, Integer id, Double offsetAsFOR) throws PresetException {
        try {
            pc.offsetGroupParamFromVoice(preset, voice, id, offsetAsFOR).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void setGroupParam(Integer group, Integer id, Integer value) throws PresetException {
        try {
            pc.setGroupParam(preset, group, id, value).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void setZoneParam(Integer voice, Integer zone, Integer id, Integer value) throws PresetException {
        try {
            pc.setZoneParam(preset, voice, zone, id, value).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetZoneParam(Integer voice, Integer zone, Integer id, Integer offset) throws PresetException {
        try {
            pc.offsetZoneParam(preset, voice, zone, id, offset).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        }
    }

    public void offsetZoneParam(Integer voice, Integer zone, Integer id, Double offsetAsFOR) throws PresetException {
        try {
            pc.offsetZoneParam(preset, voice, zone, id, offsetAsFOR).post();
        } catch (ResourceUnavailableException e) {
            throw new PresetException(e.getMessage());
        } catch (IllegalParameterIdException e) {
            throw new PresetException(e.getMessage());
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
            try {
                assertInitialized(true);
                if (getDeviceContext().getDevicePreferences().ZPREF_usePartitionedVoiceEditing.getValue())
                    getDeviceContext().getViewManager().openTabbedVoice(Impl_ContextEditablePreset.Impl_EditableVoice.this, getDeviceContext().getDevicePreferences().ZPREF_groupEnvelopesWhenVoiceTabbed.getValue(), true).post();
                else
                    getDeviceContext().getViewManager().openVoice(Impl_ContextEditablePreset.Impl_EditableVoice.this, true).post();
            } catch (ResourceUnavailableException e) {
                e.printStackTrace();
            } catch (PresetException e) {
                e.printStackTrace();
            }
        }

        public void setGroupMode(boolean groupMode) {
            this.groupMode = groupMode;
        }

        public boolean getGroupMode() {
            return groupMode;
        }

        public void trySetOriginalKeyFromSampleName() throws ParameterException, PresetException {
            try {
                String name = pc.getRootSampleContext().getName(pc.getVoiceParams(preset, voice, new Integer[]{ID.sample})[0]);
                pc.trySetOriginalKeyFromName(preset, voice, name).post();
            } catch (EmptyException e) {
            } catch (ContentUnavailableException e) {
                throw new PresetException(e.getMessage());
            } catch (DeviceException e) {
                throw new PresetException(e.getMessage());
            } catch (ResourceUnavailableException e) {
                throw new PresetException(e.getMessage());
            }
        }

        public void setVoiceParam(Integer id, Integer value) throws PresetException {
            if (groupMode)
                Impl_ContextEditablePreset.this.setGroupParamFromVoice(voice, id, value);
            else
                Impl_ContextEditablePreset.this.setVoiceParam(voice, id, value);
        }

        public void offsetVoiceParam(Integer id, Integer offset) throws PresetException {
            if (groupMode)
                Impl_ContextEditablePreset.this.offsetGroupParamFromVoice(voice, id, offset);
            else
                Impl_ContextEditablePreset.this.offsetVoiceParam(voice, id, offset);
        }

        public void offsetVoiceParam(Integer id, Double offsetAsFOR) throws PresetException {
            if (groupMode)
                Impl_ContextEditablePreset.this.offsetGroupParamFromVoice(voice, id, offsetAsFOR);
            else
                Impl_ContextEditablePreset.this.offsetVoiceParam(voice, id, offsetAsFOR);
        }

        public int numZones() throws EmptyException, PresetException {
            return Impl_ContextEditablePreset.this.numZones(voice);
        }

        public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException {
            try {
                return new Impl_VoiceEditableParameterModel(pc.getDeviceParameterContext().getVoiceContext().getParameterDescriptor(id));
            } catch (DeviceException e) {
                throw new ParameterException(e.getMessage());
            }
        }

        public void removeVoice() throws PresetException {
            Impl_ContextEditablePreset.this.rmvVoices(new Integer[]{voice});
        }

        public void splitVoice(int splitKey) throws PresetException {
            Impl_ContextEditablePreset.this.splitVoice(voice, splitKey);
        }

        public void expandVoice() throws PresetException {
            Impl_ContextEditablePreset.this.expandVoice(voice);
        }

        public void combineVoiceGroup() throws EmptyException, ParameterException, PresetException {
            Impl_ContextEditablePreset.this.combineVoiceGroup(voice);
        }

        public void copyVoice() throws EmptyException, PresetException, ParameterException {
            try {
                Impl_ContextEditablePreset.this.copyVoice(voice, this.getVoiceParams(new Integer[]{IntPool.get(37)})[0]);
            } catch (IllegalParameterIdException e) {
                Impl_ContextEditablePreset.this.copyVoice(voice, IntPool.get(0));
            } catch (NoSuchVoiceException e) {
                throw new PresetException(e.getMessage());
            }
        }

        public void newZones(Integer num) throws PresetException {
            Impl_ContextEditablePreset.this.newZones(voice, num);
        }

        public PresetContext getPresetContext() {
            return Impl_ContextEditablePreset.this.getPresetContext();
        }

        public Integer[] getVoiceParams(Integer[] ids) throws EmptyException, ParameterException, PresetException {
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

            public void setZoneParam(Integer id, Integer value) throws EmptyException, ParameterException, PresetException {
                Impl_ContextEditablePreset.this.setZoneParam(voice, zone, id, value);
            }

            public void offsetZoneParam(Integer id, Integer offset) throws PresetException {
                Impl_ContextEditablePreset.this.offsetZoneParam(voice, zone, id, offset);
            }

            public void offsetZoneParam(Integer id, Double offsetAsFOR) throws PresetException {
                Impl_ContextEditablePreset.this.offsetZoneParam(voice, zone, id, offsetAsFOR);
            }

            public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException {
                try {
                    return new Impl_ZoneEditableParameterModel(pc.getDeviceParameterContext().getVoiceContext().getParameterDescriptor(id));
                } catch (DeviceException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void trySetOriginalKeyFromSampleName() throws EmptyException, PresetException, ParameterException {
                try {
                    String name = pc.getRootSampleContext().getName(pc.getZoneParams(preset, voice, zone, new Integer[]{ID.sample})[0]);
                    pc.trySetOriginalKeyFromName(preset, voice, zone, name).post();
                } catch (EmptyException e) {
                } catch (ContentUnavailableException e) {
                    throw new PresetException(e.getMessage());
                } catch (DeviceException e) {
                    throw new PresetException(e.getMessage());
                } catch (ResourceUnavailableException e) {
                    throw new PresetException(e.getMessage());
                }
            }

            public void removeZone() throws PresetException {
                Impl_ContextEditablePreset.this.rmvZones(voice, new Integer[]{zone});
            }

            public Integer[] getZoneParams(Integer[] ids) throws EmptyException, ParameterException, PresetException {
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

            public ZCommand[] getZCommands(Class markerClass) {
                return EditableZone.cmdProviderHelper.getCommandObjects(markerClass, this);
            }

            public Class[] getZCommandMarkers() {
                return EditableZone.cmdProviderHelper.getSupportedMarkers();
            }

            class Impl_ZoneEditableParameterModel extends AbstractPresetEditableParameterModel {
                private Integer[] id;
                private volatile Integer value;

                public Impl_ZoneEditableParameterModel(GeneralParameterDescriptor pd) {
                    super(pd);
                    id = new Integer[]{pd.getId()};
                    addListener(pla);
                }

                private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
                    ois.defaultReadObject();
                    Impl_ContextEditablePreset.this.addListener(pla);
                }


                public void setValue(Integer value) throws ParameterException {
                    try {
                        this.value = null;
                        setZoneParam(id[0], value);
                    } catch (Exception e) {
                        throw new ParameterException(e.getMessage(), id[0]);
                    }
                }

                public void offsetValue(Integer offset) throws ParameterException {
                    try {
                        this.value = null;
                        offsetZoneParam(id[0], offset);
                    } catch (Exception e) {
                        throw new ParameterException(e.getMessage(), id[0]);
                    }
                }

                public void offsetValue(Double offsetAsFOR) throws ParameterException {
                    try {
                        this.value = null;
                        offsetZoneParam(id[0], offsetAsFOR);
                    } catch (Exception e) {
                        throw new ParameterException(e.getMessage(), id[0]);
                    }
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
                        // Impl_ZoneEditableParameterModel.this.fireChanged();
                    }

                    public void zoneChanged(ZoneChangeEvent ev) {
                        if (ev.containsId(id[0])) {
                            value = null;
                            Impl_ZoneEditableParameterModel.this.fireChanged();
                        }
                    }
                };

                public void zDispose() {
                    super.zDispose();
                    removeListener(pla);
                }

                public String getToolTipText() {
                    if (tipShowingOwner)
                        try {
                            return "Voice " + (voice.intValue() + 1) + "  Zone " + (zone.intValue() + 1) + " [" + pd.toString() + " = " + getValueString() + " ]";
                        } catch (ParameterException e) {
                            return "Voice " + (voice.intValue() + 1) + "  Zone " + (zone.intValue() + 1);
                        }
                    return super.getToolTipText();
                }
            }

        };

        public ZCommand[] getZCommands(Class markerClass) {
            return EditableVoice.cmdProviderHelper.getCommandObjects(markerClass, this);
        }

        public Class[] getZCommandMarkers() {
            return EditableVoice.cmdProviderHelper.getSupportedMarkers();
        }

        class Impl_VoiceEditableParameterModel extends AbstractPresetEditableParameterModel {
            private Integer[] id;
            private boolean isFilterId;
            private volatile Integer value;

            public Impl_VoiceEditableParameterModel(GeneralParameterDescriptor pd) {
                super(pd);
                id = new Integer[]{pd.getId()};
                isFilterId = isFilterId();
                if (isFilterId)
                    setFilterType();
                addListener(pla);
            }

            private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
                ois.defaultReadObject();
                Impl_ContextEditablePreset.this.addListener(pla);
            }

            private void setFilterType() {
                try {
                    ((FilterParameterDescriptor) pd).setFilterType(Impl_EditableVoice.this.getVoiceParams(new Integer[]{IntPool.get(82)})[0]);
                    Impl_ContextEditablePreset.Impl_EditableVoice.Impl_VoiceEditableParameterModel.this.fireChanged();
                } catch (EmptyException e) {
                } catch (IllegalParameterIdException e) {
                } catch (NoSuchVoiceException e) {
                } catch (ParameterException e) {
                } catch (PresetException e) {
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
                    this.value = null;
                    setVoiceParam(id[0], value);
                } catch (Exception e) {
                    throw new ParameterUnavailableException(id[0]);
                }
            }

            public void offsetValue(Integer offset) throws ParameterException {
                try {
                    this.value = null;
                    offsetVoiceParam(id[0], offset);
                } catch (Exception e) {
                    throw new ParameterUnavailableException(id[0]);
                }
            }

            public void offsetValue(Double offsetAsFOR) throws ParameterException {
                try {
                    this.value = null;
                    offsetVoiceParam(id[0], offsetAsFOR);
                } catch (Exception e) {
                    throw new ParameterUnavailableException(id[0]);
                }
            }

            public void zDispose() {
                super.zDispose();
                removeListener(pla);
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
                else {
                    if (getGroupMode()) {
                        HashSet s = new HashSet();
                        try {
                            Integer[] voices = pc.getVoiceIndexesInGroupFromVoice(preset, voice);
                            s.addAll(Arrays.asList(pc.getVoicesParam(preset, voices, id[0])));
                            StringBuffer sb = new StringBuffer();
                            sb.append(super.getToolTipText()).append("  (").append(+s.size()).append(" distinct group value").append((s.size() == 1 ? "" : "s")).append(")");
                            return sb.toString();
                        } catch (EmptyException e) {
                        } catch (NoSuchContextException e) {
                        } catch (DeviceException e) {
                        } catch (IllegalParameterIdException e) {
                        } catch (ParameterException e) {
                        } catch (ContentUnavailableException e) {
                        }
                    }
                    return super.getToolTipText();
                }
            }

            protected PresetListenerAdapter pla = new PresetListenerAdapter() {
                public void presetRefreshed(PresetInitializeEvent ev) {
                    value = null;
                    //Impl_ContextEditablePreset.Impl_EditableVoice.Impl_VoiceEditableParameterModel.this.fireChanged();
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
                            value = null;
                            Impl_VoiceEditableParameterModel.this.fireChanged();
                        }
                    }
                }
            };

            private Integer getGroup() throws EmptyException, ParameterException, PresetException {
                return Impl_EditableVoice.this.getVoiceParams(new Integer[]{IntPool.get(37)})[0];
            }

            private Integer[] getVoiceIndexesInGroup() {
                try {
                    return Impl_EditableVoice.this.getVoiceIndexesInGroup();
                } catch (EmptyException e) {
                } catch (PresetException e) {
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
    }

    class Impl_EditableLink extends Impl_ReadableLink implements EditableLink {
        public Impl_EditableLink(Integer link) {
            super(link);
        }

        public void setLinkParam(Integer id, Integer value) throws PresetException {
            Impl_ContextEditablePreset.this.setLinkParam(link, id, value);
        }

        public void offsetLinkParam(Integer id, Integer offset) throws PresetException {
            Impl_ContextEditablePreset.this.offsetLinkParam(link, id, offset);
        }

        public void offsetLinkParam(Integer id, Double offsetAsFOR) throws PresetException {
            Impl_ContextEditablePreset.this.offsetLinkParam(link, id, offsetAsFOR);
        }

        public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException {
            try {
                return new Impl_LinkEditableParameterModel(pc.getDeviceParameterContext().getVoiceContext().getParameterDescriptor(id));
            } catch (DeviceException e) {
                throw new ParameterException(e.getMessage());
            }
        }

        public PresetContext getPresetContext() {
            return Impl_ContextEditablePreset.this.getPresetContext();
        }

        public void removeLink() throws PresetException {
            Impl_ContextEditablePreset.this.rmvLinks(new Integer[]{link});
        }

        public void copyLink() throws EmptyException, PresetException {
            Impl_ContextEditablePreset.this.copyLink(link);
        }

        public Integer[] getLinkParams(Integer[] ids) throws EmptyException, PresetException, ParameterException {
            return Impl_ContextEditablePreset.this.getLinkParams(link, ids);
        }

        public Integer getLinkNumber() {
            return link;
        }

        public ZCommand[] getZCommands(Class markerClass) {
            return EditableLink.cmdProviderHelper.getCommandObjects(markerClass, this);
        }

        public Class[] getZCommandMarkers() {
            return EditableLink.cmdProviderHelper.getSupportedMarkers();
        }

        class Impl_LinkEditableParameterModel extends AbstractPresetEditableParameterModel {
            private Integer[] id;
            private volatile Integer value;

            public Impl_LinkEditableParameterModel(GeneralParameterDescriptor pd) {
                super(pd);
                id = new Integer[]{pd.getId()};
                addListener(pla);
            }

            private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
                ois.defaultReadObject();
                Impl_ContextEditablePreset.this.addListener(pla);
            }

            private Integer getPreset() {
                return preset;
            }

            private Integer getLink() {
                return link;
            }

            public void setValue(Integer value) throws ParameterException {
                try {
                    this.value = null;
                    setLinkParam(id[0], value);
                } catch (Exception e) {
                    throw new ParameterUnavailableException(id[0]);
                }
            }

            public void offsetValue(Integer offset) throws ParameterException {
                try {
                    this.value = null;
                    offsetLinkParam(id[0], offset);
                } catch (Exception e) {
                    throw new ParameterUnavailableException(id[0]);
                }
            }

            public void offsetValue(Double offsetAsFOR) throws ParameterException {
                try {
                    this.value = null;
                    offsetLinkParam(id[0], offsetAsFOR);
                } catch (Exception e) {
                    throw new ParameterUnavailableException(id[0]);
                }
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

            protected PresetListenerAdapter pla = new PresetListenerAdapter() {
                public void presetRefreshed(PresetInitializeEvent ev) {
                    value = null;
                    //  Impl_EditableLink.Impl_LinkEditableParameterModel.this.fireChanged();
                }

                public void linkChanged(LinkChangeEvent ev) {
                    if (ev.getLink().equals(link)) {
                        if (ev.containsId(id[0])) {
                            value = null;
                            Impl_EditableLink.Impl_LinkEditableParameterModel.this.fireChanged();
                        }
                    }
                }
            };

            public String getToolTipText() {
                if (tipShowingOwner)
                    try {
                        return "Link " + (link.intValue() + 1) + " [" + pd.toString() + " = " + getValueString() + " ]";
                    } catch (ParameterException e) {
                        return "Link " + (link.intValue() + 1);
                    }
                return super.getToolTipText();
            }

            public void zDispose() {
                super.zDispose();
                removeListener(pla);
            }
        }
    };

    public ZCommand[] getZCommands(Class markerClass) {
        return ContextEditablePreset.cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    // most capable/super first
    public Class[] getZCommandMarkers() {
        return ContextEditablePreset.cmdProviderHelper.getSupportedMarkers();
    }
}

