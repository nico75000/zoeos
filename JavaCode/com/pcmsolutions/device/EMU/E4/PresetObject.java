package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.Nameable;
import com.pcmsolutions.system.TipFieldFormatter;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

class PresetObject extends Parameterized implements Nameable, ZDisposable, IsolatedPreset, Serializable, Comparable {
    private static final int MAX_VOICES = 256;
    private static final int MAX_NAME_LENGTH = 16;
    private boolean postingEvents = true;

    // these are vectors because refreshPreset && getPresetRead change preset objects on the fly and may call zDispose while somebody else is reading
    private final Vector voices = new Vector();
    private final Vector links = new Vector();

    private Integer preset;
    private String name;
    protected String notes = "";

    protected DeviceParameterContext deviceParameterContext;
    protected PresetEventHandler presetEventHandler;

    public boolean isPostingEvents() {
        return postingEvents;
    }

    public void setPostingEvents(boolean postingEvents) {
        this.postingEvents = postingEvents;
    }

    // copy constructor same preset database
    public PresetObject(Integer preset, PresetObject src) {
        this(src, preset, src.presetEventHandler, src.deviceParameterContext);
    }

    // new constructor
    public PresetObject(Integer preset, String name, PresetEventHandler peh, DeviceParameterContext dpc) {
        super(dpc.getPresetContext(), true);
        this.preset = preset;
        this.name = name;
        this.presetEventHandler = peh;
        this.deviceParameterContext = dpc;
        try {
            addVoices(new VoiceObject[]{new VoiceObject(preset, IntPool.get(0), dpc, peh)});
        } catch (TooManyVoicesException e) {
            e.printStackTrace();  // should never get here
        }
    }

    // new constructor from preset dump
    public PresetObject(ByteArrayInputStream dumpStream, PresetEventHandler peh, DeviceParameterContext dpc) throws InvalidPresetDumpException, TooManyVoicesException, TooManyZonesException {
        super(dpc.getPresetContext(), false);
        this.presetEventHandler = peh;
        this.deviceParameterContext = dpc;
        setDumpBytes(dumpStream);
    }

    // copy constructor with translation to given ParameterContext
    public PresetObject(PresetObject src, Integer preset, PresetEventHandler peh, DeviceParameterContext dpc) {
        super(src, dpc.getPresetContext());
        this.presetEventHandler = peh;
        this.name = src.name;
        this.preset = preset;
        this.deviceParameterContext = dpc;

        int len = src.voices.size();

        for (int n = 0; n < len; n++)
            voices.add(new VoiceObject((VoiceObject) src.voices.get(n), preset, IntPool.get(n), dpc, peh));
        len = src.links.size();

        for (int n = 0; n < len; n++)
            links.add(new LinkObject((LinkObject) src.links.get(n), preset, IntPool.get(n), dpc, peh));
    }

    // copy constructor with translation to given ParameterContext
    public PresetObject(Integer preset, String name, IsolatedPreset src, PresetEventHandler peh, DeviceParameterContext dpc) {
        super(src, dpc.getPresetContext());
        this.presetEventHandler = peh;
        this.name = name;
        this.preset = preset;
        this.deviceParameterContext = dpc;

        int len = src.numVoices();
        for (int n = 0; n < len; n++)
            try {
                voices.add(new VoiceObject(preset, IntPool.get(n), src.getIsolatedVoice(IntPool.get(n)), dpc, peh));
            } catch (NoSuchVoiceException e) {
                e.printStackTrace();
            }
        len = src.numLinks();
        for (int n = 0; n < len; n++)
            try {
                links.add(new LinkObject(preset, IntPool.get(n), src.getIsolatedLink(IntPool.get(n)), dpc, peh));
            } catch (NoSuchLinkException e) {
                e.printStackTrace();
            }
    }

    public DeviceParameterContext getDeviceParameterContext() {
        return deviceParameterContext;
    }

    public void setDeviceParameterContext(DeviceParameterContext deviceParameterContext) {
        this.deviceParameterContext = deviceParameterContext;
    }

    public PresetEventHandler getPresetEventHandler() {
        return presetEventHandler;
    }

    public void setPresetEventHandler(PresetEventHandler presetEventHandler) {
        this.presetEventHandler = presetEventHandler;
        for (int i = 0,j = voices.size(); i < j; i++)
            ((VoiceObject) voices.get(i)).setPresetEventHandler(presetEventHandler);
        for (int i = 0,j = links.size(); i < j; i++)
            ((LinkObject) links.get(i)).setPresetEventHandler(presetEventHandler);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public VoiceObject[] getVoicesInGroup(Integer group) {
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
                this.addVoices(IntPool.get(samples.length), samples);
                break;
            case PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICE_AND_ZONES:
                Integer vi = this.addVoices(IntPool.get(1), new Integer[]{IntPool.get(0)});
                try {
                    VoiceObject v = getVoice(vi);
                    v.setPostingEvents(false);
                    try {
                        Integer zi = v.addZones(IntPool.get(samples.length - 1));
                        v.setPostingEvents(false);
                        for (int i = 0; i < samples.length; i++)
                            try {
                                getVoice(vi).getZone(IntPool.get(i)).setValue(ID.sample, samples[i]);
                            } catch (NoSuchZoneException e) {
                                e.printStackTrace();
                            } catch (IllegalParameterIdException e) {
                                e.printStackTrace();
                            }
                    } finally {
                        v.setPostingEvents(true);
                    }
                } catch (NoSuchVoiceException e) {
                    e.printStackTrace();
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
                for (int i = 0,j = voices.size(); i < j; i++)
                    ((VoiceObject) voices.get(i)).applySingleSample(sample);
                break;
            case PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES:
                for (int i = 0,j = voices.size(); i < j; i++) {
                    try {
                        ((VoiceObject) voices.get(i)).setValues(sid, sval);
                    } catch (IllegalParameterIdException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES_AND_ZONES:
                for (int i = 0,j = voices.size(); i < j; i++) {
                    try {
                        ((VoiceObject) voices.get(i)).setValues(sid, sval);
                        ((VoiceObject) voices.get(i)).applySingleSample(sample);
                    } catch (IllegalParameterIdException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PresetContext.MODE_APPLY_SAMPLE_TO_NEW_VOICE:
                this.addVoices(IntPool.get(1), new Integer[]{sample});
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
        for (int i = 0,j = voices.size(); i < j; i++) {
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

    // returned map format is link(Integer)->new link value(Integer)
    public Map offsetLinkIndexes(Integer linkOffset, boolean user) {
        Integer[] lid = new Integer[]{IntPool.get(23)};
        Integer[] lval = new Integer[]{linkOffset};
        HashMap outMap = new HashMap();
        LinkObject lobj;
        for (int i = 0,j = links.size(); i < j; i++) {
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
        VoiceObject ov = getVoice(voice);

        int env = voices.size();
        int enl = links.size();
        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        voices.add(new VoiceObject(preset, IntPool.get(env), ov));
        if (postingEvents)
            presetEventHandler.postPresetEvent(new VoiceAddEvent(this, preset, IntPool.get(env), 1));

        VoiceObject nv = getVoice(IntPool.get(env));
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
        for (int i = 0,j = voices.size(); i < j; i++) {
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
        for (int i = 0,j = links.size(); i < j; i++) {
            try {
                lval = ((LinkObject) links.get(i)).getValue(lid);
                if (translationMap.get(lval) instanceof Integer) {
                    ((LinkObject) links.get(i)).setValue(lid, (Integer) translationMap.get(lval));
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
        for (int i = 0,j = voices.size(); i < j; i++) {
            v = ((VoiceObject) voices.get(i));
            v.setPostingEvents(false);
            v.sortZones(ids);
            v.setPostingEvents(true);
        }
        if (postingEvents)
            presetEventHandler.postPresetEvent(new PresetRefreshEvent(this, preset));
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
        if (postingEvents)
            presetEventHandler.postPresetEvent(new PresetRefreshEvent(this, preset));
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
                    }
                return c;
            }
        });
        if (postingEvents)
            presetEventHandler.postPresetEvent(new PresetRefreshEvent(this, preset));
    }

    public VoiceObject getLeadVoiceInGroup(Integer group) {
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

    public void defaultValues(Integer[] ids) throws IllegalParameterIdException {
        super.defaultValues(ids);
        if (postingEvents)
            presetEventHandler.postPresetEvent(new PresetChangeEvent(this, preset, ids));
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
        ttip.add("NOTES: " + notes);
        return ttip.toString();
    }

    /*public ByteArrayInputStream getDumpBytes() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        return is;
    } */

    public ByteArrayOutputStream getDumpBytes() throws IOException {
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
        for (int i = 0,j = values.length; i < j; i++)
            os2.write(SysexHelper.DataOut(values[i]));

        // NUMBER OF LINKS
        os2.write(SysexHelper.DataOut(links.size()));

        // LINKS
        for (int i = 0,j = links.size(); i < j; i++)
            ((LinkObject) links.get(i)).getDumpBytes().writeTo(os2);

        // NUMBER OF VOICES
        if (!deviceParameterContext.isWeirdPresetDumping())
            os2.write(SysexHelper.DataOut(voices.size()));

        // VOICES
        for (int i = 0,j = voices.size(); i < j; i++)
            ((VoiceObject) voices.get(i)).getDumpBytes().writeTo(os2);

        // DUMP BYTE COUNT
        os.write(SysexHelper.UnsignedLongDataOut_int(os2.size() - 6));

        os2.writeTo(os);
        return os;
    }

    public void setDumpBytes(ByteArrayInputStream dis) throws InvalidPresetDumpException, TooManyVoicesException, TooManyZonesException {
        int dc,ds, ngp, nvp, nlp, nzp;
        byte[] dumpField;

        dc = dis.available();

        dumpField = new byte[2];
// BEGIN DUMP HEADER
        // PRESET NUMBER
        if (dis.read(dumpField, 0, 2) == 2) {
            preset = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn(dumpField);
            // Number of bytes in dump
            dumpField = new byte[4];
            if (dis.read(dumpField, 0, 4) == 4) {
                ds = com.pcmsolutions.device.EMU.E4.SysexHelper.UnsignedLongDataIn_int(dumpField[0], dumpField[1], dumpField[2], dumpField[3]);

                if (dc >= ds) {
                    dumpField = new byte[2];
                    // Number of global parameters
                    dis.read(dumpField, 0, 2);
                    ngp = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn_int(dumpField);
                    // Number of link parameters
                    dis.read(dumpField, 0, 2);
                    nlp = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn_int(dumpField);
                    // Number of voice parameters
                    dis.read(dumpField, 0, 2);
                    nvp = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn_int(dumpField);
                    // Number of zone parameters
                    dis.read(dumpField, 0, 2);
                    nzp = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn_int(dumpField);
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
                        Integer[] idVals = PresetDatabase.parseDumpStream(dis, deviceParameterContext.getPresetContext());
                        initValues(idVals);

                        // LINKS
                        dumpField = new byte[2];
                        dis.read(dumpField, 0, 2);
                        int nLinks = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn_int(dumpField);
                        postingEvents = false;
                        try {
                            for (int n = 0; n < nLinks; n++)
                                addLink(dis, nlp);

                            // VOICES
                            if (weirdDump) {
                                //nVoices = dis.available() / (146 * 2);
                                while (dis.available() > 0)
                                    addVoice(dis);
                            } else {
                                dis.read(dumpField, 0, 2);
                                int nVoices = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn_int(dumpField);
                                for (int n = 0; n < nVoices; n++)
                                    addVoice(dis);
                            }
                        } finally {
                            postingEvents = true;
                        }
                        return;
                    }
                }
            }
        }

        throw new InvalidPresetDumpException();
    }

    public void setValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.setValues(ids, values);
        if (postingEvents)
            presetEventHandler.postPresetEvent(new PresetChangeEvent(this, preset, ids));
    }

    /*public void setValues(Integer[] idVals, boolean postEvent) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.setValues(idVals);
        if (postEvent)
            peh.postPresetEvent(new PresetChangeEvent(this, preset, ids));
    }

    public void setValues(Integer[] idVals) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        setValues(idVals, true);
    } */

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
        if (name.length() > MAX_NAME_LENGTH)
            this.name = name.substring(0, MAX_NAME_LENGTH);
        else
            this.name = name;

        if (postingEvents)
            presetEventHandler.postPresetEvent(new PresetNameChangeEvent(this, preset));
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

    /* public void purgeZones() {
         for (int i = 0, j = voices.size(); i < j; i++)
             ((VoiceObject) voices.get(i)).purgeZones();
     }*/

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

        for (int i = 0,j = voices.size(); i < j; i++)
            useMap.mergeUseMap(((VoiceObject) voices.get(i)).getReferencedSampleUsage());

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
        for (int i = 0,j = links.size(); i < j; i++) {
            p = ((LinkObject) links.get(i)).getPreset();
            if (p != null & p.intValue() >= 0)
                useMap.addIntegerReference(p);
        }
        return useMap;
    }

    public Set referencedPresetSet() {
        return referencedPresetUsage().getUseMap().keySet();
    }

    public VoiceObject getVoice(Integer v) throws NoSuchVoiceException {
        if (v.intValue() >= voices.size())
            throw new NoSuchVoiceException();
        return (VoiceObject) voices.get(v.intValue());
    }

    public LinkObject getLink(Integer l) throws NoSuchLinkException {
        if (l.intValue() >= links.size())
            throw new NoSuchLinkException();

        return (LinkObject) links.get(l.intValue());
    }

    public Integer addVoices(Integer n, Integer[] sampleNumbers) throws TooManyVoicesException {
        if (sampleNumbers.length != n.intValue())
            throw new IllegalArgumentException(this.getClass().toString() + ":addVoices -> Number of specified sample numbers does not match number of voices being added!");

        int enl = links.size();
        int env = voices.size();
        int num = n.intValue();
        Integer nv;
        int added = 0;
        try {
            for (int i = 0; i < num; i++) {
                if (enl + env + i >= MAX_VOICES)
                    throw new TooManyVoicesException();
                nv = IntPool.get(env + i);
                VoiceObject v = new VoiceObject(preset, nv, deviceParameterContext, presetEventHandler);
                voices.add(v);
                added++;
                try {
                    v.setPostingEvents(false);
                    v.setValues(new Integer[]{ID.sample}, new Integer[]{sampleNumbers[i]}); // voice sample
                } catch (IllegalParameterIdException e) {
                    throw new IllegalStateException(this.getClass().toString() + ":addVoices -> setValues returned IllegalParameterIdException - id should be valid!");
                } catch (ParameterValueOutOfRangeException e) {
                    throw new IllegalStateException(this.getClass().toString() + ":addVoices -> setValues returned IllegalParameterIdException - id should have a valid value!");
                } finally {
                    v.setPostingEvents(true);
                }
            }
        } finally {
            if (added > 0) {
                if (postingEvents)

                    presetEventHandler.postPresetEvent(new VoiceAddEvent(this, preset, IntPool.get(env), added));
            }
        }

        return IntPool.get(env);
    }

    public Integer addVoices(VoiceObject[] newVoices) throws TooManyVoicesException {
        int enl = links.size();
        int env = voices.size();
        int nnv = newVoices.length;
        Integer nv;
        int added = 0;
        try {
            for (int i = 0; i < nnv; i++) {
                if (enl + env + i >= MAX_VOICES)
                    throw new TooManyVoicesException();
                nv = IntPool.get(env + i);
                VoiceObject v = new VoiceObject(preset, nv, newVoices[i]);
                voices.add(v);
                added++;
            }
        } finally {
            if (added > 0)
                if (postingEvents)

                    presetEventHandler.postPresetEvent(new VoiceAddEvent(this, preset, IntPool.get(env), added));
        }

        return IntPool.get(env);
    }

    public Integer addVoice(ByteArrayInputStream dis) throws TooManyVoicesException, InvalidPresetDumpException, TooManyZonesException {
        int env = voices.size();
        int enl = links.size();

        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        voices.add(new VoiceObject(dis, preset, IntPool.get(env), deviceParameterContext, presetEventHandler));

        if (postingEvents)
            presetEventHandler.postPresetEvent(new VoiceAddEvent(this, preset, IntPool.get(env), 1));

        return IntPool.get(env);
    }

    public Integer addVoice(IsolatedPreset.IsolatedVoice iv) throws TooManyVoicesException, TooManyZonesException {
        int env = voices.size();
        int enl = links.size();

        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        voices.add(new VoiceObject(preset, IntPool.get(env), iv, deviceParameterContext, presetEventHandler));

        if (postingEvents)
            presetEventHandler.postPresetEvent(new VoiceAddEvent(this, preset, IntPool.get(env), 1));

        return IntPool.get(env);
    }

    public void rmvVoice(Integer voice) throws NoSuchVoiceException, CannotRemoveLastVoiceException {
        if (voice.intValue() >= voices.size())
            throw new NoSuchVoiceException();
        if (voices.size() == 1)
            throw new CannotRemoveLastVoiceException();
        ((VoiceObject) voices.remove(voice.intValue())).zDispose();
        remapVoices(voice.intValue());
        presetEventHandler.postPresetEvent(new VoiceRemoveEvent(this, preset, voice));
    }

    public Integer addLinks(Integer n, Integer[] presetNumbers) throws TooManyVoicesException {
        if (presetNumbers.length != n.intValue())
            throw new IllegalArgumentException(this.getClass().toString() + ":addLinks -> Number of specified preset numbers does not match number of links being added!");

        int env = voices.size();
        int enl = links.size();
        int num = n.intValue();

        if (enl + env + num > MAX_VOICES)
            throw new TooManyVoicesException();

        Integer nl;
        int added = 0;
        try {
            for (int i = 0; i < num; i++) {
                if (enl + env + i > MAX_VOICES)
                    throw new TooManyVoicesException();

                nl = IntPool.get(enl + i);
                LinkObject l = new LinkObject(preset, nl, deviceParameterContext, presetEventHandler);
                links.add(l);
                added++;
                l.setPostingEvents(false);
                try {
                    l.setValues(new Integer[]{IntPool.get(23)}, new Integer[]{presetNumbers[i]}); // link preset
                } catch (IllegalParameterIdException e) {
                    throw new IllegalStateException(this.getClass().toString() + ":addLinks -> setValues returned IllegalParameterIdException - id should be valid!");
                } catch (ParameterValueOutOfRangeException e) {
                    throw new IllegalStateException(this.getClass().toString() + ":addLinks -> setValues returned IllegalParameterIdException - id should have a valid value!");
                } finally {
                    l.setPostingEvents(true);
                }
            }
        } finally {
            if (added > 0)
                if (postingEvents)
                    presetEventHandler.postPresetEvent(new LinkAddEvent(this, preset, IntPool.get(enl), added));
        }

        return IntPool.get(enl);
    }

    public Integer addLinks(LinkObject[] newLinks) throws TooManyVoicesException {
        int env = voices.size();
        int nnl = newLinks.length;
        int enl = links.size();
        Integer nl;

        int added = 0;
        try {
            for (int i = 0; i < nnl; i++) {
                if (enl + env + i > MAX_VOICES)
                    throw new TooManyVoicesException();
                nl = IntPool.get(enl + i);
                links.add(new LinkObject(preset, nl, newLinks[i]));
                added++;
            }
        } finally {
            if (added > 0)
                if (postingEvents)
                    presetEventHandler.postPresetEvent(new LinkAddEvent(this, preset, IntPool.get(enl), added));
        }

        return IntPool.get(enl);
    }

    public Integer addLink(ByteArrayInputStream dis, int numIds) throws TooManyVoicesException, InvalidPresetDumpException {
        int env = voices.size();
        int enl = links.size();

        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        links.add(new LinkObject(dis, numIds, preset, IntPool.get(enl), deviceParameterContext, presetEventHandler));

        if (postingEvents)
            presetEventHandler.postPresetEvent(new LinkAddEvent(this, preset, IntPool.get(enl), 1));

        return IntPool.get(enl);
    }

    public Integer addLink(IsolatedPreset.IsolatedLink il) throws TooManyVoicesException {
        int env = voices.size();
        int enl = links.size();

        if (enl + env + 1 > MAX_VOICES)
            throw new TooManyVoicesException();

        links.add(new LinkObject(preset, IntPool.get(enl), il, deviceParameterContext, presetEventHandler));

        if (postingEvents)
            presetEventHandler.postPresetEvent(new LinkAddEvent(this, preset, IntPool.get(enl), 1));

        return IntPool.get(enl);
    }

    public void rmvLink(Integer link) throws NoSuchLinkException {
        if (link.intValue() >= links.size())
            throw new NoSuchLinkException();
        ((LinkObject) links.remove(link.intValue())).zDispose();
        remapVoices(link.intValue());
        if (postingEvents)
            presetEventHandler.postPresetEvent(new LinkRemoveEvent(this, preset, link));
    }

    // IsolatedPreset stuff
    public int numVoices() {
        return voices.size();
    }

    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer v) throws NoSuchVoiceException {
        VoiceObject vobj = new VoiceObject(preset, v, (VoiceObject) voices.get(v.intValue())) {
            public String getOriginalPresetName() {
                return name;
            }
        };
        //vobj.setDeviceParameterContext(null);
        vobj.setPresetEventHandler(null);
        return vobj;
    }

    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer l) throws NoSuchLinkException {
        LinkObject lobj = new LinkObject(preset, l, (LinkObject) links.get(l.intValue())) {
            public String getOrginalPresetName() {
                return name;
            }
        };
        //lobj.setDeviceParameterContext(null);
        lobj.setPresetEventHandler(null);
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
        int nLinks = voices.size();
        for (int n = fromLink; n < nLinks; n++) {
            ((LinkObject) links.get(n)).setLink(IntPool.get(n));
        }
    }

    public int compareTo(Object o) {
        //GJP
//        if (o instanceof PresetObject)
          return preset.compareTo(((PresetObject) o).preset);
  //      return preset.compareTo(o);
    }
}


