package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.events.preset.requests.PresetParametersRequestEvent;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.remote.DumpParsingUtilities;
import com.pcmsolutions.device.EMU.E4.remote.SysexHelper;
import com.pcmsolutions.device.EMU.database.events.content.ContentEventHandler;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.*;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 13-Aug-2004
 * Time: 19:34:26
 */
class PresetObject extends Parameterized implements Nameable, ZDisposable, IsolatedPreset, Serializable, Comparable, DatabasePreset {
    private static final int MAX_VOICES = 256;
    private static final int MAX_NAME_LENGTH = 16;

    // these are vectors because refreshPreset && getPresetRead change preset objects on the fly and may call zDispose while somebody else is reading
    private final Vector voices = new Vector();
    private final Vector links = new Vector();

    private Integer preset;
    private String name;
    protected String notes = "";

    protected DeviceParameterContext deviceParameterContext;
    protected ContentEventHandler contentEventHandler;

    boolean postingEvents = true;

    public boolean isPostingEvents() {
        return postingEvents;
    }

    public void setPostingEvents(boolean postingEvents) {
        this.postingEvents = postingEvents;
        for (int i = 0; i < voices.size(); i++)
            ((VoiceObject) voices.get(i)).setPostingEvents(postingEvents);
        for (int i = 0; i < links.size(); i++)
            ((LinkObject) links.get(i)).setPostingEvents(postingEvents);
    }

    // new constructor
    public void initNew(Integer preset, String name, ContentEventHandler ceh, DeviceParameterContext dpc) {
        super.initNew(dpc.getPresetContext(), true);
        this.preset = preset;
        this.name = name;
        this.contentEventHandler = ceh;
        this.deviceParameterContext = dpc;
        try {
            newVoices(new Integer[]{IntPool.get(0)});
        } catch (TooManyVoicesException e) {
            e.printStackTrace();  // should never get here
        }
        ceh.postEvent(new PresetNewEvent(this, preset, getDumpBytes(), ProgressCallback.DUMMY));
    }

    // new constructor from preset dump
    public void initDump(ByteArrayInputStream dumpStream, ContentEventHandler ceh, DeviceParameterContext dpc) throws InvalidPresetDumpException, TooManyVoicesException, TooManyZonesException {
        super.initNew(dpc.getPresetContext(), false);
        this.contentEventHandler = ceh;
        this.deviceParameterContext = dpc;
        setDumpBytes(dumpStream);
        ceh.postEvent(new PresetInitializeEvent(this, preset));
    }

    // copy constructor with translation to given ParameterContext
    public void initCopy(PresetObject src, Integer preset, ContentEventHandler ceh, DeviceParameterContext dpc) {
        super.initCopy(src, dpc.getPresetContext());
        this.contentEventHandler = ceh;
        this.name = src.name;
        this.preset = preset;
        this.deviceParameterContext = dpc;

        int len = src.voices.size();

        for (int n = 0; n < len; n++) {
            VoiceObject v = new VoiceObject();
            v.initPresetCopy((VoiceObject) src.voices.get(n), preset, IntPool.get(n), dpc, ceh);
            voices.add(v);
        }
        len = src.links.size();

        for (int n = 0; n < len; n++) {
            LinkObject l = new LinkObject();
            l.initPresetCopy((LinkObject) src.links.get(n), preset, IntPool.get(n), dpc, ceh);
            links.add(l);
        }
        ceh.postEvent(new PresetCopyEvent(this, src.preset, preset));
    }

    // restore constructor with translation to given ParameterContext
    public void initRestore(PresetObject src, Integer preset, ContentEventHandler ceh, DeviceParameterContext dpc) {
        super.initCopy(src, dpc.getPresetContext());
        this.contentEventHandler = ceh;
        this.name = src.name;
        this.preset = preset;
        this.deviceParameterContext = dpc;

        int len = src.voices.size();

        for (int n = 0; n < len; n++) {
            VoiceObject v = new VoiceObject();
            v.initPresetCopy((VoiceObject) src.voices.get(n), preset, IntPool.get(n), dpc, ceh);
            voices.add(v);
        }
        len = src.links.size();

        for (int n = 0; n < len; n++) {
            LinkObject l = new LinkObject();
            l.initPresetCopy((LinkObject) src.links.get(n), preset, IntPool.get(n), dpc, ceh);
            links.add(l);
        }
    }

    // isolated constructor
    public void initIsolated(PresetObject src, Integer preset) {
        super.initCopy(src, src.deviceParameterContext.getPresetContext());
        this.contentEventHandler = null;
        this.name = src.name;
        this.preset = preset;
        this.deviceParameterContext = src.deviceParameterContext;

        int len = src.voices.size();

        for (int n = 0; n < len; n++) {
            VoiceObject v = new VoiceObject();
            v.initIsolated(preset, IntPool.get(n), (VoiceObject) src.voices.get(n));
            voices.add(v);
        }
        len = src.links.size();

        for (int n = 0; n < len; n++) {
            LinkObject l = new LinkObject();
            l.initIsolated(preset, IntPool.get(n), (LinkObject) src.links.get(n));
            links.add(l);
        }
    }

    // drop constructor with translation to given ParameterContext
    public void initDrop(Integer preset, String name, IsolatedPreset src, ContentEventHandler ceh, DeviceParameterContext dpc, ProgressCallback prog) {
        super.initDrop(src, dpc.getPresetContext());
        this.contentEventHandler = ceh;
        this.name = name;
        this.preset = preset;
        this.deviceParameterContext = dpc;

        int len = src.numVoices();
        for (int n = 0; n < len; n++)
            try {
                VoiceObject v = new VoiceObject();
                v.initPresetDrop(preset, IntPool.get(n), src.getIsolatedVoice(IntPool.get(n)), dpc, ceh);
                voices.add(v);
            } catch (PresetException e) {
                e.printStackTrace();
            }
        len = src.numLinks();
        for (int n = 0; n < len; n++)
            try {
                LinkObject l = new LinkObject();
                l.initPresetDrop(preset, IntPool.get(n), src.getIsolatedLink(IntPool.get(n)), dpc, ceh);
                links.add(l);
            } catch (PresetException e) {
                e.printStackTrace();
            }
        if (postingEvents)
            ceh.postEvent(new PresetNewEvent(this, preset, getDumpBytes(), prog));
        else
            prog.updateProgress(1);
    }

    public void offsetGroupValue(Integer group, Integer id, Integer offset, boolean constrain) throws ParameterValueOutOfRangeException, IllegalParameterIdException {
        DatabaseVoice[] voices = this.getVoicesInGroup(group);
        int idv = id.intValue();
        GeneralParameterDescriptor pd = getDeviceParameterContext().getParameterDescriptor(id);
        if (idv >= 129 && idv <= 182 && (idv - 129) % 3 != 2) {
            Integer nextVal = getDeviceParameterContext().discontinuousOffset(pd, voices[0].getValue(id), offset.intValue(), constrain);
            if (nextVal == null)
                throw new ParameterValueOutOfRangeException(id);
            setGroupValue(group, id, nextVal);
            return;
        } else if (constrain)
            setGroupValue(group, id, pd.constrainValue(IntPool.get(voices[0].getValue(id).intValue() + offset.intValue())));
        else
            setGroupValue(group, id, IntPool.get(voices[0].getValue(id).intValue() + offset.intValue()));
    }

    public void setGroupValue(Integer group, Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException {
        DatabaseVoice[] voices = this.getVoicesInGroup(group);
        int idv = id.intValue();
        if (idv >= 129 && idv <= 182) {
            if ((idv - 129) % 3 == 0) {
                // we've got a cord src parameter
                value = getDeviceParameterContext().getNearestCordSrcValue(value);
            } else if ((idv - 129) % 3 == 1) {
                // we've got a cord dest parameter
                value = getDeviceParameterContext().getNearestCordDestValue(value);
            }
        }
        for (int i = 0; i < voices.length; i++)
            ((VoiceObject) voices[i]).putValue(id, value);
        if (postingEvents)
            contentEventHandler.postEvent(new GroupChangeEvent(this, preset, group, this.getVoiceIndexesInGroup(group), new Integer[]{id}, new Integer[]{value}));
    }

    public DeviceParameterContext getDeviceParameterContext() {
        return deviceParameterContext;
    }

    public void setDeviceParameterContext(DeviceParameterContext deviceParameterContext) {
        this.deviceParameterContext = deviceParameterContext;
    }

    public ContentEventHandler getContentEventHandler() {
        return contentEventHandler;
    }

    public void setContentEventHandler(ContentEventHandler contentEventHandler) {
        this.contentEventHandler = contentEventHandler;
        for (int i = 0, j = voices.size(); i < j; i++)
            ((VoiceObject) voices.get(i)).setContentEventHandler(contentEventHandler);
        for (int i = 0, j = links.size(); i < j; i++)
            ((LinkObject) links.get(i)).setContentEventHandler(contentEventHandler);
    }

    public void refreshParameters(Integer[] ids) throws IllegalParameterIdException {
        if (!(deviceParameterContext.getPresetContext().getIds().containsAll((List) Arrays.asList(ids))))
            throw new IllegalParameterIdException();
        PresetParametersRequestEvent ppre = new PresetParametersRequestEvent(this, preset, ids);
        if (contentEventHandler.sendRequest(ppre)) {
            try {
                List<Integer> values = ppre.getRequestedData();
                Integer[] aValues = values.toArray(new Integer[values.size()]);
                putValues(ids, aValues);
                contentEventHandler.postInternalEvent(new PresetChangeEvent(this, preset, ids, aValues));
            } catch (ParameterValueOutOfRangeException e) {
                SystemErrors.internal(e);
            }
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public DatabaseVoice[] getVoicesInGroup(Integer group) {
        ArrayList groupVoices = new ArrayList();
        VoiceObject vobj;
        for (int i = 0, j = voices.size(); i < j; i++) {
            vobj = (VoiceObject) voices.get(i);
            try {
                if (vobj.getValues(new Integer[]{IntPool.get(37)})[0].equals(group))
                    groupVoices.add(vobj);
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            }
        }
        return (VoiceObject[]) groupVoices.toArray(new VoiceObject[groupVoices.size()]);
    }

    public void applySamples(Integer[] samples, int mode) throws TooManyVoicesException, TooManyZonesException, ParameterValueOutOfRangeException {
        switch (mode) {
            case PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICES:
                this.newVoices(samples);
                break;
            case PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICE_AND_ZONES:
                if (samples.length == 1) {
                    this.newVoices(samples);
                } else {
                    Integer vi = this.newVoices(new Integer[]{IntPool.get(0)});
                    try {
                        VoiceObject v = (VoiceObject) getVoice(vi);
                        for (int i = 0; i < samples.length - 1; i++) // -1 because first add yields two zones!
                            v.newZone(true);
                        for (int i = 0; i < samples.length; i++)
                            try {
                                v.getZone(IntPool.get(i)).setValue(ID.sample, samples[i]);
                            } catch (NoSuchZoneException e) {
                                SystemErrors.internal(e);
                            } catch (IllegalParameterIdException e) {
                                SystemErrors.internal(e);
                            }
                    } catch (NoSuchVoiceException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("invalid mode");
        }
    }

    public void applySingleSample(Integer sample, int mode) throws ParameterValueOutOfRangeException, TooManyVoicesException {
        Integer[] sid = new Integer[]{ID.sample};
        Integer[] sval = new Integer[]{sample};

        switch (mode) {
            case PresetContext.MODE_APPLY_SAMPLE_TO_ALL_ZONES:
                for (int i = 0, j = voices.size(); i < j; i++)
                    ((DatabaseVoice) voices.get(i)).applySingleSample(sample);
                break;
            case PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES:
                for (int i = 0, j = voices.size(); i < j; i++) {
                    try {
                        ((VoiceObject) voices.get(i)).setValues(sid, sval);
                    } catch (IllegalParameterIdException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES_AND_ZONES:
                for (int i = 0, j = voices.size(); i < j; i++) {
                    try {
                        ((VoiceObject) voices.get(i)).setValues(sid, sval);
                        ((DatabaseVoice) voices.get(i)).applySingleSample(sample);
                    } catch (IllegalParameterIdException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PresetContext.MODE_APPLY_SAMPLE_TO_NEW_VOICE:
                this.newVoices(new Integer[]{sample});
                break;
            default:
                throw new IllegalArgumentException("invalid mode");
        }
    }

    // returned map format is voice(Integer)->new sample value(Integer)
    public Map offsetSampleIndexes(Integer sampleOffset, boolean user) {
        Integer[] sid = new Integer[]{ID.sample};
        Integer[] sval = new Integer[]{sampleOffset};
        HashMap outMap = new HashMap();
        VoiceObject vobj;
        for (int i = 0, j = voices.size(); i < j; i++) {
            vobj = (VoiceObject) voices.get(i);
            if (vobj.numZones() == 0)       // we don't need to offset a multisample voice
                try {
                    if ((user && vobj.getValue(ID.sample).intValue() <= DeviceContext.MAX_USER_SAMPLE)
                            || (!user && vobj.getValue(ID.sample).intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                    ) {
                        vobj.offsetValues(sid, sval, false);
                        outMap.put(IntPool.get(i), vobj.getValue(sid[0]));
                    }
                } catch (IllegalParameterIdException e) {
                } catch (ParameterValueOutOfRangeException e) {
                }
            else
                outMap.put(IntPool.get(i), vobj.offsetSampleIndexes(sampleOffset, user));
        }
        return outMap;
    }

    private static final Integer[] z_mergeIds = new Integer[]{IntPool.get(39), IntPool.get(40), IntPool.get(42)};
    private static final Integer[] z_rtIds = new Integer[]{IntPool.get(53), IntPool.get(54), IntPool.get(55), IntPool.get(56)};

    public void combineVoices(Integer group) {
        setPostingEvents(false);
        try {
            DatabaseVoice[] vig = getVoicesInGroup(group);
            if (vig.length < 2)
                return;
            Integer[] viig = getVoiceIndexesInGroup(group);
            //int fv_nz = vig[0].numZones();

            IsolatedPreset.IsolatedVoice.IsolatedZone iz;
            int nz; // num zones
            Integer nzi; // new zone index
            ParameterContext zpc = deviceParameterContext.getZoneContext();
            Set z_ids = zpc.getIds();
            Integer[] zoneIds = (Integer[]) z_ids.toArray(new Integer[z_ids.size()]);
            Integer[] vals;
            Integer[] vals2 = null;

            boolean postProcessingReqd = vig[0].numZones() == 0;

            if (postProcessingReqd)
                vals2 = vig[0].getValues(zoneIds);

            for (int i = 1; i < vig.length; i++) {
                nz = vig[i].numZones();
                if (nz == 0) {
                    vals = vig[i].getValues(zoneIds);
                    nzi = vig[0].newZone();
                    vig[0].getZone(nzi).setValues(zoneIds, vals);
                } else
                    for (int j = 0; j < nz; j++) {
                        iz = vig[i].getIsolatedZone(IntPool.get(j));
                        vig[0].getZone(vig[0].dropZone(iz)).offsetValues(z_mergeIds, vig[i].getValues(z_mergeIds));
                    }
            }

            if (postProcessingReqd) {
                vig[0].defaultValues(zoneIds);
                // realtime stuff needs to be defaulted as well
                vig[0].defaultValues(z_rtIds);
                vig[0].getZone(IntPool.get(0)).setValues(zoneIds, vals2);
            }
            Integer[] rmv_viig = new Integer[viig.length - 1];
            System.arraycopy(viig, 1, rmv_viig, 0, viig.length - 1);
            rmvVoices(rmv_viig);
            contentEventHandler.postEvent(new GroupCombineEvent(this, preset, group));
        } catch (NoSuchZoneException e) {
            SystemErrors.internal(e);
        } catch (ParameterValueOutOfRangeException e) {
            SystemErrors.internal(e);
        } catch (IllegalParameterIdException e) {
            SystemErrors.internal(e);
        } catch (TooManyZonesException e) {
            SystemErrors.internal(e);
        } catch (CannotRemoveLastVoiceException e) {
            SystemErrors.internal(e);
        } catch (NoSuchVoiceException e) {
            SystemErrors.internal(e);
        } finally {
            setPostingEvents(true);
        }
    }

    private static final Integer[] exceptIds = new Integer[]{IntPool.get(39), IntPool.get(40), IntPool.get(42)};

    public void expandVoice(final Integer voice) throws TooManyVoicesException {
        setPostingEvents(false);
        try {
            DatabaseVoice v = getVoice(voice);
            if (v.numZones() + numVoices() > MAX_VOICES)
                throw new TooManyVoicesException();
            DatabaseVoice nv;
            Integer addIndex;
            IsolatedPreset.IsolatedVoice.IsolatedZone[] zones = new IsolatedPreset.IsolatedVoice.IsolatedZone[v.numZones()];
            Integer[] voiceVals = v.getAllValues();
            Integer[] voiceIds = v.getAllIds();

            if (zones.length > 0) {
                int nz = v.numZones();
                for (int i = 0, j = nz; i < j; i++)
                    zones[i] = v.getIsolatedZone(IntPool.get(i));

                for (int i = nz - 1; i > 0; i--)
                    v.rmvZone(IntPool.get(i));

                for (int i = 0, j = zones.length; i < j; i++) {
                    addIndex = newVoices(new Integer[]{IntPool.get(0)});
                    nv = getVoice(addIndex);
                    nv.setValues(voiceIds, voiceVals);
                    nv.setValues(zones[i].getAllIdsExcept(exceptIds), zones[i].getAllValuesExcept(exceptIds));
                    nv.offsetValues(exceptIds, zones[i].getValues(exceptIds));  // addDesktopElement zone vol, pan and ftune to corresponding voice values
                }

                rmvVoices(new Integer[]{voice});
                contentEventHandler.postEvent(new VoiceExpandEvent(this, preset, voice));
            }
        } catch (CannotRemoveLastVoiceException e) {
            SystemErrors.internal(e);
        } catch (NoSuchZoneException e) {
            SystemErrors.internal(e);
        } catch (ParameterValueOutOfRangeException e) {
            SystemErrors.internal(e);
        } catch (NoSuchVoiceException e) {
            SystemErrors.internal(e);
        } catch (IllegalParameterIdException e) {
            SystemErrors.internal(e);
        } finally {
            setPostingEvents(true);
        }
    }

    // returned map format is link(Integer)->new link value(Integer)
    public Map offsetLinkIndexes(Integer linkOffset, boolean user) {
        Integer[] lid = new Integer[]{IntPool.get(23)};
        Integer[] lval = new Integer[]{linkOffset};
        HashMap outMap = new HashMap();
        LinkObject lobj;
        for (int i = 0, j = links.size(); i < j; i++) {
            try {
                lobj = (LinkObject) links.get(i);

                if ((user && lobj.getValue(ID.preset).intValue() <= DeviceContext.MAX_USER_PRESET)
                        || (!user && lobj.getValue(ID.preset).intValue() >= DeviceContext.BASE_FLASH_PRESET)
                ) {
                    lobj.offsetValues(lid, lval, false);
                    outMap.put(IntPool.get(i), ((LinkObject) links.get(i)).getValue(lid[0]));
                }
            } catch (IllegalParameterIdException e) {
            } catch (ParameterValueOutOfRangeException e) {
            }
        }
        return outMap;
    }

    public Integer splitVoice(Integer voice, int key) throws NoSuchVoiceException, TooManyVoicesException, ParameterValueOutOfRangeException {
        VoiceObject ov = (VoiceObject) getVoice(voice);

        int env = voices.size();
        int enl = links.size();
        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        VoiceObject v = new VoiceObject();
        v.setPostingEvents(isPostingEvents());
        v.initCopy(preset, IntPool.get(env), ov);
        voices.add(v);
        DatabaseVoice nv = getVoice(IntPool.get(env));
        try {
            ov.setValue(IntPool.get(47), IntPool.get(key - 1)); // key high
            nv.setValue(IntPool.get(45), IntPool.get(key)); // key low
        } catch (IllegalParameterIdException e) {
        }
        return IntPool.get(env);
    }

    // returned map format is voice(Integer)->new sample value(Integer)

    // unmatched rom sample indexes should not appear in translationMap
    public Map remapSampleIndexes(Map translationMap, Integer defaultSampleTranslation) {
        Integer sid = ID.sample;
        Integer sval;
        HashMap outMap = new HashMap();
        VoiceObject vobj;
        for (int i = 0, j = voices.size(); i < j; i++) {
            vobj = (VoiceObject) voices.get(i);
            if (vobj.numZones() == 0)       // we don't need to remap a multisample voice
                try {
                    sval = vobj.getValue(sid);
                    if (translationMap.get(sval) instanceof Integer) {
                        vobj.setValue(sid, (Integer) translationMap.get(sval));
                        outMap.put(IntPool.get(i), vobj.getValue(sid));
                    } else if (defaultSampleTranslation != null) {
                        vobj.setValue(sid, defaultSampleTranslation);
                        outMap.put(IntPool.get(i), defaultSampleTranslation);
                    }
                } catch (IllegalParameterIdException e) {
                } catch (ParameterValueOutOfRangeException e) {
                }
            else
                outMap.put(IntPool.get(i), vobj.remapSampleIndexes(translationMap, defaultSampleTranslation));
        }
        return outMap;
    }

    public Map remapLinkIndexes(Map translationMap) {
        Integer lid = IntPool.get(23);
        Integer lval;
        HashMap outMap = new HashMap();
        for (int i = 0, j = links.size(); i < j; i++) {
            try {
                lval = ((LinkObject) links.get(i)).getValue(lid);
                if (translationMap.get(lval) instanceof Integer) {
                    ((DatabaseLink) links.get(i)).setValue(lid, (Integer) translationMap.get(lval));
                    outMap.put(IntPool.get(i), ((LinkObject) links.get(i)).getValue(lid));
                }
            } catch (IllegalParameterIdException e) {
            } catch (ParameterValueOutOfRangeException e) {
            }
        }
        return outMap;
    }

    public void sortZones(final Integer[] ids) {
        VoiceObject v;
        for (int i = 0, j = voices.size(); i < j; i++) {
            v = ((VoiceObject) voices.get(i));
            v.sortZones(ids);
            if (postingEvents) {
                for (int k = 0, l = v.numZones(); k < l; k++) {
                    try {
                        ZoneObject z = (ZoneObject) v.getZone(IntPool.get(k));
                        contentEventHandler.postEvent(new ZoneChangeEvent(this, preset, IntPool.get(i), IntPool.get(k), z.getAllIds(), z.getAllValues()), false);
                    } catch (Exception e) {
                        SystemErrors.internal(e);
                    }
                }
            }
        }
    }

    public void sortVoices(final Integer[] ids) {
        Collections.sort(voices, new Comparator() {
            public int compare(Object o1, Object o2) {
                int c = 0;
                int index = 0;
                while (c == 0 && index < ids.length)
                    try {
                        c = ((VoiceObject) o1).getValue(ids[index]).compareTo(((VoiceObject) o2).getValue(ids[index]));
                        index++;
                    } catch (IllegalParameterIdException e) {
                    }
                return c;
            }
        });
        remapVoices(0);
        if (postingEvents)
            contentEventHandler.postEvent(new PresetNewEvent(this, preset, getDumpBytes(), ProgressCallback.DUMMY));
    }

    public void sortLinks(final Integer[] ids) {
        Collections.sort(links, new Comparator() {
            public int compare(Object o1, Object o2) {
                int c = 0;
                int index = 0;
                while (c == 0 && index < ids.length)
                    try {
                        c = ((LinkObject) o1).getValue(ids[index]).compareTo(((LinkObject) o2).getValue(ids[index]));
                        index++;
                    } catch (IllegalParameterIdException e) {
                        //SystemErrors.internal(e);
                    }
                return c;
            }
        });
        remapLinks(0);
        if (postingEvents) {
            for (int i = 0, j = numLinks(); i < j; i++) {
                try {
                    LinkObject l = (LinkObject) getLink(IntPool.get(i));
                    contentEventHandler.postEvent(new LinkChangeEvent(this, preset, IntPool.get(i), l.getAllIds(), l.getAllValues()), false);
                } catch (Exception e) {
                    SystemErrors.internal(e);
                }
            }
        }
    }

    public DatabaseVoice getLeadVoiceInGroup(Integer group) {
        VoiceObject vobj;
        for (int i = 0, j = voices.size(); i < j; i++) {
            vobj = (VoiceObject) voices.get(i);
            try {
                if (vobj.getValues(new Integer[]{IntPool.get(37)})[0].equals(group))
                    return vobj;
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Integer[] getVoiceIndexesInGroup(Integer group) {
        ArrayList groupVoices = new ArrayList();
        VoiceObject vobj;
        for (int i = 0, j = voices.size(); i < j; i++) {
            vobj = (VoiceObject) voices.get(i);
            try {
                if (vobj.getValues(new Integer[]{IntPool.get(37)})[0].equals(group))
                    groupVoices.add(IntPool.get(i));
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            }
        }
        return (Integer[]) groupVoices.toArray(new Integer[groupVoices.size()]);
    }

    public Integer getLeadVoiceIndexInGroup(Integer group) {
        VoiceObject vobj;
        for (int i = 0, j = voices.size(); i < j; i++) {
            vobj = (VoiceObject) voices.get(i);
            try {
                if (vobj.getValues(new Integer[]{IntPool.get(37)})[0].equals(group))
                    return (IntPool.get(i));
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getSummary() {
        int links, voices, zones, samples;
        links = numLinks();
        voices = numVoices();
        zones = numZones();
        samples = referencedSampleSet().size();

        TipFieldFormatter ttip = new TipFieldFormatter();

        ttip.add(Integer.toString(voices) + (voices == 1 ? " voice" : " voices"));
        ttip.add(Integer.toString(zones) + (zones == 1 ? " zone" : " zones"));
        ttip.add(Integer.toString(links) + (links == 1 ? " link" : " links"));
        ttip.add(Integer.toString(samples) + (samples == 1 ? " sample" : " samples"));
        //ttip.add("NOTES: " + notes);
        return ttip.toString();
    }

    public ByteArrayInputStream getDumpBytes() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ByteArrayOutputStream os2 = new ByteArrayOutputStream();

            // PRESET NUMBER
            os.write(SysexHelper.DataOut(preset));

            // PARAMETER COUNTS
            os2.write(SysexHelper.DataOut(deviceParameterContext.getPresetContext().size()));

            if (deviceParameterContext.isWeirdPresetDumping())
                os2.write(SysexHelper.DataOut(deviceParameterContext.getLinkContext().size() - 1));
            else
                os2.write(SysexHelper.DataOut(deviceParameterContext.getLinkContext().size()));
            os2.write(SysexHelper.DataOut(deviceParameterContext.getVoiceContext().size()));
            os2.write(SysexHelper.DataOut(deviceParameterContext.getZoneContext().size()));

            // PRESET NAME
            os2.write(SysexHelper.StringOut(new StringBuffer(name)));

            // GLOBAL PARAMS
            Integer[] values;
            values = getAllValues();
            if (values.length != deviceParameterContext.getPresetContext().size())
                throw new IllegalStateException("number of global parameters mismatch");
            for (int i = 0, j = values.length; i < j; i++)
                os2.write(SysexHelper.DataOut(values[i]));

            // NUMBER OF LINKS
            os2.write(SysexHelper.DataOut(links.size()));

            // LINKS
            for (int i = 0, j = links.size(); i < j; i++)
                ((LinkObject) links.get(i)).getDumpBytes().writeTo(os2);

            // NUMBER OF VOICES
            if (!deviceParameterContext.isWeirdPresetDumping())
                os2.write(SysexHelper.DataOut(voices.size()));

            // VOICES
            for (int i = 0, j = voices.size(); i < j; i++)
                ((VoiceObject) voices.get(i)).getDumpBytes().writeTo(os2);

            // DUMP BYTE COUNT
            os.write(SysexHelper.UnsignedLongDataOut_int(os2.size() - 6));

            os2.writeTo(os);
            byte[] thisDump = os.toByteArray();
            /*
             int thisDumpLen = thisDump.length;
             int lastDumpLen = lastDump.length;
             for (int i = 0; i < thisDump.length; i++) {
                 if (i < lastDump.length)
                     if (thisDump[i] != lastDump[i]) {
                         System.out.println("mismatch: " + i);
                        // break;
                     }
             }
             */
            return new ByteArrayInputStream(thisDump);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }

    //static byte[] lastDump;

    public void setDumpBytes(ByteArrayInputStream dis) throws InvalidPresetDumpException, TooManyVoicesException, TooManyZonesException {
        int dc, ds, ngp, nvp, nlp, nzp;
        byte[] dumpField;
        dc = dis.available();

        //lastDump = new byte[dc];
        // dis.read(lastDump, 0, dc);
        // dis.reset();

        dumpField = new byte[2];
// BEGIN DUMP HEADER
        // PRESET NUMBER
        if (dis.read(dumpField, 0, 2) == 2) {
            preset = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn(dumpField);
            // Number of bytes in dump
            dumpField = new byte[4];
            if (dis.read(dumpField, 0, 4) == 4) {
                ds = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.UnsignedLongDataIn_int(dumpField[0], dumpField[1], dumpField[2], dumpField[3]);

                if (dc >= ds) {
                    dumpField = new byte[2];
                    // Number of global parameters
                    dis.read(dumpField, 0, 2);
                    ngp = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn_int(dumpField);
                    // Number of link parameters
                    dis.read(dumpField, 0, 2);
                    nlp = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn_int(dumpField);
                    // Number of voice parameters
                    dis.read(dumpField, 0, 2);
                    nvp = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn_int(dumpField);
                    // Number of zone parameters
                    dis.read(dumpField, 0, 2);
                    nzp = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn_int(dumpField);
                    boolean weirdDump = false;
                    if (nlp == 28) {
                        //   nlp = 29;    // EOS 3.2 bug??
                        weirdDump = true;
                    }
                    if (
                            nlp <= deviceParameterContext.getLinkContext().size()
                            && nvp == deviceParameterContext.getVoiceContext().size()
                            && ngp == deviceParameterContext.getPresetContext().size()
                            && nzp == deviceParameterContext.getZoneContext().size()) {

// END DUMP HEADER
                        // PRESET NAME
                        dumpField = new byte[16];
                        dis.read(dumpField, 0, 16);
                        name = new String(dumpField).trim();

                        // GLOBAL PARAMETERS
                        Integer[] idVals = DumpParsingUtilities.parseDumpStream(dis, deviceParameterContext.getPresetContext());
                        initValues(idVals);

                        // LINKS
                        dumpField = new byte[2];
                        dis.read(dumpField, 0, 2);
                        int nLinks = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn_int(dumpField);
                        // postingEvents = false;
                        //  try {
                        for (int n = 0; n < nLinks; n++)
                            addLinkFromDump(dis, nlp);

                        // VOICES
                        if (weirdDump) {
                            //nVoices = dis.available() / (146 * 2);
                            while (dis.available() > 0)
                                addVoiceFromDump(dis);
                        } else {
                            dis.read(dumpField, 0, 2);
                            int nVoices = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn_int(dumpField);
                            for (int n = 0; n < nVoices; n++)
                                addVoiceFromDump(dis);
                        }
                        //  } finally {
                        //      postingEvents = true;
                        //  }
                        return;
                    }
                }
            }
        }

        throw new InvalidPresetDumpException();
    }

    public void setValue(Integer id, Integer value) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.putValue(id, value);
        if (postingEvents)
            contentEventHandler.postEvent(new PresetChangeEvent(this, preset, new Integer[]{id}, new Integer[]{value}), false);
        if (id.intValue() == 6) {
            Integer val1 = FXDefaults.getFXA_Decay(value);
            Integer val2 = FXDefaults.getFXA_HFDamping(value);
            super.putValue(IntPool.get(7), val1);
            super.putValue(IntPool.get(8), val2);
            if (postingEvents)
                contentEventHandler.postInternalEvent(new PresetChangeEvent(this, preset, new Integer[]{IntPool.get(7), IntPool.get(8)}, new Integer[]{val1, val2}));
        } else if (id.intValue() == 14) {
            Integer val1 = FXDefaults.getFXB_Feedback(value);
            Integer val2 = FXDefaults.getFXB_LFORate(value);
            Integer val3 = FXDefaults.getFXB_DelayTime(value);
            super.putValue(IntPool.get(15), val1);
            super.putValue(IntPool.get(16), val2);
            super.putValue(IntPool.get(17), val3);
            if (postingEvents)
                contentEventHandler.postInternalEvent(new PresetChangeEvent(this, preset, new Integer[]{IntPool.get(15), IntPool.get(16), IntPool.get(17)}, new Integer[]{val1, val2, val3}));
        }
    }

    /*
    public void offsetValue(Integer id, Integer offset, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.offsetValue(id, offset, constrain);
        if (postingEvents)
            contentEventHandler.postEvent(new PresetChangeEvent(this, preset, new Integer[]{id}, new Integer[]{getValue(id)}), false);
    }
    */

    public void setPresetNumber(Integer preset) {
        this.preset = preset;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Integer getOriginalIndex() {
        return preset;
    }

    public void setName(String name) {
        if (name != null) {
            if (name.length() > MAX_NAME_LENGTH)
                this.name = name.substring(0, MAX_NAME_LENGTH);
            else
                this.name = name;

            contentEventHandler.postEvent(new PresetNameChangeEvent(this, preset, this.name));
        }
    }

    public int numZones() {
        int zones = 0;
        int voices = numVoices();
        try {
            for (int n = 0; n < voices; n++)
                zones += getVoice(IntPool.get(n)).numZones();
        } catch (NoSuchVoiceException e) {
            // should not happen!
        }
        return zones;
    }

    public void purgeLinks() {
        for (int i = 0, j = links.size(); i < j; i++)
            try {
                rmvLink(IntPool.get(i));
            } catch (NoSuchLinkException e) {
                // should never get here
            }
    }

    public int numReferencedSamples() {
        return referencedSampleUsage().size();
    }

    public IntegerUseMap referencedSampleUsage() {
        IntegerUseMap useMap = new IntegerUseMap();

        for (int i = 0, j = voices.size(); i < j; i++)
            useMap.mergeUseMap(((DatabaseVoice) voices.get(i)).getReferencedSampleUsage());

        return useMap;
    }

    public Set referencedSampleSet() {
        return referencedSampleUsage().getUseMap().keySet();
    }

    public int numReferencedPresets() {
        return referencedPresetUsage().size();
    }

    public IntegerUseMap referencedPresetUsage() {
        IntegerUseMap useMap = new IntegerUseMap();
        Integer p;
        for (int i = 0, j = links.size(); i < j; i++) {
            p = ((DatabaseLink) links.get(i)).getPreset();
            if (p != null & p.intValue() >= 0)
                useMap.addIntegerReference(p);
        }
        return useMap;
    }

    public Set<Integer> referencedPresetSet() {
        return referencedPresetUsage().getUseMap().keySet();
    }

    public IsolatedPreset getIsolated() {
        PresetObject pobj = new PresetObject() {
            public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer v) throws NoSuchVoiceException {
                return (VoiceObject) getVoice(v);
            }

            public IsolatedPreset.IsolatedLink getIsolatedLink(Integer l) throws NoSuchLinkException {
                return (LinkObject) getLink(l);
            }
        };
        pobj.initIsolated(this, preset);
        return pobj;
    }

    public DatabaseVoice getVoice(Integer v) throws NoSuchVoiceException {
        if (v.intValue() >= voices.size())
            throw new NoSuchVoiceException();
        return (VoiceObject) voices.get(v.intValue());
    }

    public DatabaseLink getLink(Integer l) throws NoSuchLinkException {
        if (l.intValue() >= links.size())
            throw new NoSuchLinkException();

        return (LinkObject) links.get(l.intValue());
    }

    public Integer newVoices(Integer[] sampleNumbers) throws TooManyVoicesException {
        int enl = links.size();
        int env = voices.size();
        int num = sampleNumbers.length;
        Integer nv;
        int addIndex = numVoices();
        try {
            for (int i = 0; i < num; i++) {
                if (enl + env + i >= MAX_VOICES)
                    throw new TooManyVoicesException();
                nv = IntPool.get(env + i);
                VoiceObject v = new VoiceObject();
                v.setPostingEvents(isPostingEvents());
                v.initNew(preset, nv, deviceParameterContext, contentEventHandler);
                voices.add(v);
                try {
                    v.setValues(new Integer[]{ID.sample}, new Integer[]{sampleNumbers[i]}); // voice sample
                } catch (IllegalParameterIdException e) {
                    throw new IllegalStateException(this.getClass().toString() + ":copyVoices -> setValues returned IllegalParameterIdException - id should be valid!");
                } catch (ParameterValueOutOfRangeException e) {
                    throw new IllegalStateException(this.getClass().toString() + ":copyVoices -> setValues returned IllegalParameterIdException - id should have a valid value!");
                }
            }
        } finally {
        }
        return IntPool.get(addIndex);
    }

    public Integer copyDatabaseVoice(DatabaseVoice voice) throws TooManyVoicesException {
        int enl = links.size();
        int env = voices.size();
        Integer nv;
        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();
        nv = IntPool.get(env);
        VoiceObject v = new VoiceObject();
        v.setPostingEvents(isPostingEvents());
        v.initCopy(preset, nv, (VoiceObject) voice);
        voices.add(v);
        return IntPool.get(env);
    }

    Integer addVoiceFromDump(ByteArrayInputStream dis) throws TooManyVoicesException, InvalidPresetDumpException, TooManyZonesException {
        int env = voices.size();
        int enl = links.size();

        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        VoiceObject v = new VoiceObject();
        v.initDump(dis, preset, IntPool.get(env), deviceParameterContext, contentEventHandler);
        voices.add(v);

        return IntPool.get(env);
    }

    public Integer dropVoice(IsolatedPreset.IsolatedVoice iv) throws TooManyVoicesException, TooManyZonesException {
        int env = voices.size();
        int enl = links.size();

        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        VoiceObject v = new VoiceObject();
        v.setPostingEvents(isPostingEvents());
        v.initDrop(preset, IntPool.get(env), iv, deviceParameterContext, contentEventHandler);
        voices.add(v);
        return IntPool.get(env);
    }

    public void rmvVoices(Integer[] voiceIndexes) throws NoSuchVoiceException, CannotRemoveLastVoiceException {
        if (voiceIndexes.length == 0)
            return;
        ///////////////////////////////////////////////////////
        // THIS IS A HACK TO COMPENSATE FOR A SYSEX BUG!!
        ///////////////////////////////////////////////////////
        // delete all the remote links - christ
        if (postingEvents)
            for (int i = numLinks() - 1; i >= 0; i--)
                contentEventHandler.postExternalEvent(new LinkRemoveEvent(this, preset, IntPool.get(i)));
        try {
            // last to first
            Arrays.sort(voiceIndexes);
            for (int i = voiceIndexes.length - 1; i >= 0; i--) {
                if (voiceIndexes[i].intValue() >= voices.size())
                    throw new NoSuchVoiceException();
                if (voices.size() == 1)
                    throw new CannotRemoveLastVoiceException();
                ((VoiceObject) voices.remove(voiceIndexes[i].intValue())).zDispose();
                if (postingEvents)
                    contentEventHandler.postEvent(new VoiceRemoveEvent(this, preset, voiceIndexes[i]), false);
            }
            //remapVoices(voiceIndexes[0].intValue());
            remapVoices(0);
        } finally {
            // create the remote links again - whew
            for (int i = numLinks() - 1; i >= 0; i--) {
                Integer li = IntPool.get(i);
                LinkObject lobj = (LinkObject) links.get(i);
                if (postingEvents) {
                    contentEventHandler.postExternalEvent(new LinkAddEvent(this, preset, li));
                    contentEventHandler.postExternalEvent(new LinkChangeEvent(this, preset, IntPool.get(i), lobj.getAllIds(), lobj.getAllValues()));
                }
            }
        }
    }

    public Integer newLinks(Integer[] presetNumbers) throws TooManyVoicesException {
        int env = voices.size();
        int enl = links.size();
        int num = presetNumbers.length;
        if (enl + env + num > MAX_VOICES)
            throw new TooManyVoicesException();

        Integer nl;
        try {
            for (int i = 0; i < num; i++) {
                if (enl + env + i > MAX_VOICES)
                    throw new TooManyVoicesException();

                nl = IntPool.get(enl + i);
                LinkObject l = new LinkObject();
                l.setPostingEvents(isPostingEvents());
                l.initNew(preset, nl, deviceParameterContext, contentEventHandler);
                links.add(l);
                try {
                    l.setValues(new Integer[]{IntPool.get(23)}, new Integer[]{presetNumbers[i]}); // link preset
                } catch (IllegalParameterIdException e) {
                    SystemErrors.internal(e);
                } catch (ParameterValueOutOfRangeException e) {
                    SystemErrors.internal(e);
                } finally {
                }
            }
        } finally {
        }

        return IntPool.get(enl);
    }

    public Integer copyDatabaseLink(DatabaseLink newLink) throws TooManyVoicesException {
        int env = voices.size();
        int enl = links.size();
        Integer nl;
        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();
        nl = IntPool.get(enl);
        LinkObject l = new LinkObject();
        l.setPostingEvents(isPostingEvents());
        l.initCopy(preset, nl, (LinkObject) newLink);
        links.add(l);

        return IntPool.get(enl);
    }

    public Integer addLinkFromDump(ByteArrayInputStream dis, int numIds) throws TooManyVoicesException, InvalidPresetDumpException {
        int env = voices.size();
        int enl = links.size();

        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        LinkObject l = new LinkObject();
        l.setPostingEvents(isPostingEvents());
        l.initDump(dis, numIds, preset, IntPool.get(enl), deviceParameterContext, contentEventHandler);
        links.add(l);

        //if (postingEvents)
        //    presetEventHandler.postPresetEvent(new LinkAddEvent(this, preset, IntPool.get(enl), 1));

        return IntPool.get(enl);
    }

    public Integer dropLink(IsolatedPreset.IsolatedLink il) throws TooManyVoicesException {
        int env = voices.size();
        int enl = links.size();

        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        LinkObject l = new LinkObject();
        l.setPostingEvents(isPostingEvents());
        l.initDrop(preset, IntPool.get(enl), il, deviceParameterContext, contentEventHandler);
        links.add(l);
        return IntPool.get(enl);
    }

    public void rmvLink(Integer link) throws NoSuchLinkException {
        if (link.intValue() >= links.size())
            throw new NoSuchLinkException();
        ((LinkObject) links.remove(link.intValue())).zDispose();
        remapLinks(0);
        if (postingEvents)
            contentEventHandler.postEvent(new LinkRemoveEvent(this, preset, link), false);
    }

    public int numVoices() {
        return voices.size();
    }

    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer v) throws NoSuchVoiceException {
        if (v.intValue() >= voices.size())
            throw new NoSuchVoiceException();
        VoiceObject vobj = new VoiceObject() {
            public String getOriginalPresetName() {
                return name;
            }

            public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer z) throws NoSuchZoneException {
                return (ZoneObject) getZone(z);
            }
        };
        vobj.initIsolated(preset, v, (VoiceObject) voices.get(v.intValue()));
        return vobj;
    }

    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer l) throws NoSuchLinkException {
        if (l.intValue() >= links.size())
            throw new NoSuchLinkException();
        LinkObject lobj = new LinkObject() {
            public String getOrginalPresetName() {
                return name;
            }
        };
        lobj.initIsolated(preset, l, (LinkObject) links.get(l.intValue()));
        return lobj;
    }

    public int numLinks() {
        return links.size();
    }

    public void remapVoices(int fromVoice) {
        int nVoices = voices.size();
        for (int n = fromVoice; n < nVoices; n++) {
            ((VoiceObject) voices.get(n)).setVoice(IntPool.get(n));
        }
    }

    public void remapLinks(int fromLink) {
        int nLinks = links.size();
        for (int n = fromLink; n < nLinks; n++) {
            ((LinkObject) links.get(n)).setLink(IntPool.get(n));
        }
    }

    public int compareTo(Object o) {
        if (o instanceof DatabasePreset)
            return preset.compareTo(((DatabasePreset) o).getIndex());
        if (o instanceof Integer)
            return preset.compareTo((Integer) o);
        return 0;
    }

    public Integer getIndex() {
        return preset;
    }
}


