package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.preset.VoiceAddEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.VoiceChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.VoiceCopyEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.ZoneRemoveEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.requests.VoiceParametersRequestEvent;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.remote.DumpParsingUtilities;
import com.pcmsolutions.device.EMU.E4.remote.SysexHelper;
import com.pcmsolutions.device.EMU.database.events.content.ContentEventHandler;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.SystemErrors;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 16-Aug-2004
 * Time: 16:30:12
 */
class VoiceObject extends Parameterized implements ZDisposable, IsolatedPreset.IsolatedVoice, Serializable, DatabaseVoice {
    private static final int MAX_ZONES = 255;
    //private boolean postingEvents = true;
    // this is a vector because refreshPreset && getPresetRead change preset objects on the fly and may call zDispose while somebody else is reading
    private final Vector zones = new Vector();

    private Integer preset;
    private Integer voice;

    private final static Integer[] zoneSwitchIds = new Integer[]{IntPool.get(38), IntPool.get(44)};
    private final static Integer[] zoneMergeIds = new Integer[]{IntPool.get(39), IntPool.get(40), IntPool.get(42)};
    protected DeviceParameterContext deviceParameterContext;
    protected ContentEventHandler contentEventHandler;

    private boolean postingEvents = true;

    public boolean isPostingEvents() {
        return postingEvents;
    }

    public void setPostingEvents(boolean postingEvents) {
        this.postingEvents = postingEvents;
        for (int i = 0; i < zones.size(); i++)
            ((ZoneObject) zones.get(i)).setPostingEvents(postingEvents);
    }

    // new constructor from preset dump
    public void initDump(ByteArrayInputStream dis, Integer preset, Integer voice, DeviceParameterContext dpc, ContentEventHandler ceh) throws InvalidPresetDumpException, TooManyZonesException {
        super.initNew(dpc.getVoiceContext(), false);
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.voice = voice;
        this.preset = preset;
        byte[] field = new byte[2];

        dis.read(field, 0, 2);
        Integer group = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn(field);

        dis.read(field, 0, 2);
        // sample might be a sample number of 0x3FFF ( multisample )
        int sample = com.pcmsolutions.device.EMU.E4.remote.SysexHelper.DataIn_int(field);

        //this.setValues(new Integer[]{IntPool.get(37), IntPool.get(38)}, new Integer[]{group, IntPool.get(sample)});
        initValues(new Integer[]{IntPool.get(37), group, IntPool.get(38), IntPool.get(sample)});

        ParameterContext vc = dpc.getVoiceContext();
        Set s = vc.getIds();
        s.remove(IntPool.get(37));
        s.remove(IntPool.get(38));
        //if ( sample == -1)
        //    s.remove(IntPool.get(44));  // if multisample no E4_GEN_ORIG_KEY

        Integer[] ids = new Integer[s.size()];
        s.toArray(ids);

        initValues(DumpParsingUtilities.parseDumpStream(dis, ids));

        dis.read(field, 0, 2);
        int numZones = SysexHelper.DataIn_int(field);
        if (numZones > 1)
            for (int n = 0; n < numZones; n++)
                addZoneFromDump(dis);
    }

    // drop constructor
    public void initDrop(Integer preset, Integer voice, IsolatedPreset.IsolatedVoice src, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initDrop(src, dpc.getVoiceContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.voice = voice;
        if (postingEvents) {
            contentEventHandler.postEvent(new VoiceAddEvent(this, preset, voice), false);
            contentEventHandler.postExternalEvent(new VoiceChangeEvent(this, preset, voice, getAllIds(), getAllValues()));
        }
        int znCount = src.numZones();
        for (int n = 0; n < znCount; n++)
            try {
                ZoneObject z = new ZoneObject();
                z.initVoiceDrop(preset, voice, IntPool.get(n), src.getIsolatedZone(IntPool.get(n)), dpc, ceh);
                zones.add(z);
            } catch (PresetException e) {
                e.printStackTrace();
            }
    }

    // preset drop constructor
    public void initPresetDrop(Integer preset, Integer voice, IsolatedPreset.IsolatedVoice src, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initDrop(src, dpc.getVoiceContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.voice = voice;

        int znCount = src.numZones();
        for (int n = 0; n < znCount; n++)
            try {
                ZoneObject z = new ZoneObject();
                z.initPresetDrop(preset, voice, IntPool.get(n), src.getIsolatedZone(IntPool.get(n)), dpc, ceh);
                zones.add(z);
            } catch (PresetException e) {
                e.printStackTrace();
            }
    }

    // new constructor
    public void initNew(Integer preset, Integer voice, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initNew(dpc.getVoiceContext(), true);
        this.preset = preset;
        this.voice = voice;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        if (postingEvents)
            contentEventHandler.postEvent(new VoiceAddEvent(this, preset, voice), false);
    }

    // copy constructor
    public void initCopy(VoiceObject src, Integer preset, Integer voice, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initCopy(src, dpc.getVoiceContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.voice = voice;
        if (postingEvents)
            try {
                contentEventHandler.postEvent(new VoiceCopyEvent(this, preset, voice, src.preset, src.voice, src.getValue(IntPool.get(37))));
            } catch (IllegalParameterIdException e) {
                SystemErrors.internal(e);
            }

        int znCount = src.zones.size();
        for (int n = 0; n < znCount; n++) {
            ZoneObject z = new ZoneObject();
            z.initCopy(preset, voice, IntPool.get(n), (ZoneObject) src.zones.get(n), dpc, ceh);
            zones.add(z);
        }
    }

    // preset copy constructor
    public void initPresetCopy(VoiceObject src, Integer preset, Integer voice, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initCopy(src, dpc.getVoiceContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.voice = voice;

        int znCount = src.zones.size();
        for (int n = 0; n < znCount; n++) {
            ZoneObject z = new ZoneObject();
            z.initCopy(preset, voice, IntPool.get(n), (ZoneObject) src.zones.get(n), dpc, ceh);
            zones.add(z);
        }
        // no event needed
    }

    // copy constructor for same preset database
    public void initCopy(Integer preset, Integer voice, VoiceObject src) {
        initCopy(src, preset, voice, src.deviceParameterContext, src.contentEventHandler);
    }

    // isolated constructor for same preset database
    public void initIsolated(Integer preset, Integer voice, VoiceObject src) {
        super.initCopy(src, src.deviceParameterContext.getVoiceContext());
        this.preset = preset;
        this.deviceParameterContext = src.deviceParameterContext;
        this.contentEventHandler = null;
        this.voice = voice;

        int znCount = src.zones.size();
        for (int n = 0; n < znCount; n++) {
            ZoneObject z = new ZoneObject();
            z.initIsolated(preset, voice, IntPool.get(n), (ZoneObject) src.zones.get(n));
            zones.add(z);
        }
    }

    public ByteArrayOutputStream getDumpBytes() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Integer[] values = getAllValues();
        if (values.length != deviceParameterContext.getVoiceContext().size())
            throw new IllegalStateException("number of voice parameters mismatch");

        // group
        os.write(SysexHelper.DataOut(values[0]));

        // sample
        if (zones.size() > 0)
            os.write(SysexHelper.DataOut(0x3fff));
        else
            os.write(SysexHelper.DataOut(values[1]));

        // VOICE PARAMETERS
        for (int i = 2, j = values.length; i < j; i++)
            os.write(SysexHelper.DataOut(values[i]));

        // ZONES
        if (zones.size() > 0) {
            os.write(SysexHelper.DataOut(zones.size()));
            for (int i = 0, j = zones.size(); i < j; i++)
                ((ZoneObject) zones.get(i)).getDumpBytes().writeTo(os);
        } else
            os.write(SysexHelper.DataOut(1));

        return os;
    }

    public Integer getSample() {
        return (Integer) params.get(IntPool.get(38));
    }

    public void applySingleSample(Integer sample) throws ParameterValueOutOfRangeException {
        Integer sid = IntPool.get(38);
        for (int i = 0, j = zones.size(); i < j; i++) {
            try {
                ((ZoneObject) zones.get(i)).setValue(sid, sample);
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            }
        }
    }

    public void refreshParameters(Integer[] ids) throws IllegalParameterIdException {
        if (!(deviceParameterContext.getVoiceContext().getIds().containsAll(Arrays.asList(ids))))
            throw new IllegalParameterIdException();
        VoiceParametersRequestEvent vpre = new VoiceParametersRequestEvent(this, preset, voice, ids);
        if (contentEventHandler.sendRequest(vpre)) {
            try {
                List<Integer> values = vpre.getRequestedData();
                Integer[] aValues = values.toArray(new Integer[values.size()]);
                putValues(ids, aValues);
                contentEventHandler.postInternalEvent(new VoiceChangeEvent(this, preset, voice, ids, aValues));
            } catch (ParameterValueOutOfRangeException e) {
                SystemErrors.internal(e);
            }
        }
    }

    public IntegerUseMap getReferencedSampleUsage() {
        IntegerUseMap useMap = new IntegerUseMap();
        Integer s = getSample();

        if (s != null & s.intValue() >= 0)
            useMap.addIntegerReference(s);

        for (int i = 0, j = zones.size(); i < j; i++) {
            s = ((DatabaseZone) zones.get(i)).getSample();
            if (s != null & s.intValue() >= 0)
                useMap.addIntegerReference(s);
        }
        return useMap;
    }

    protected Integer constrainValue(GeneralParameterDescriptor pd, Integer val) {
        val = super.constrainValue(pd, val);
        if (pd.getId().intValue() == 38)
            if (val.intValue() == -1 && pd.getMinValue().intValue() == -1)
                val = IntPool.get(0);
        return val;
    }

    protected Integer finalizeValue(Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException {
        try {
            if (id.intValue() == 38) {
                GeneralParameterDescriptor pd = parameterContext.getParameterDescriptor(id);
                if (!pd.isValidValue(value))
                    throw new ParameterValueOutOfRangeException(id);
                if (value.intValue() == -1 && pd.getMinValue().intValue() == -1)
                    value = IntPool.get(0);
                return value;
            } else
                return super.finalizeValue(id, value);

        } catch (ParameterValueOutOfRangeException e) {
            // LFO1 shape or LFO2 shape ( the remote may have returned Random shape which we can't handle)
            if ((id.intValue() == 106 || id.intValue() == 111) && (value.intValue() == 255 || value.intValue() == -1)) {
                GeneralParameterDescriptor pd = parameterContext.getParameterDescriptor(id);
                return pd.getMaxValue();
            } else
                throw e;
        }
    }

    public Integer getPreset() {
        return preset;
    }

    public void setPreset(Integer preset) {
        this.preset = preset;
    }

    public Integer getVoice() {
        return voice;
    }

    public void setVoice(Integer voice) {
        this.voice = voice;
        int znCount = zones.size();
        for (int n = 0; n < znCount; n++) {
            ZoneObject z = (ZoneObject) zones.get(n);
            z.setVoice(voice);
        }
    }

    public DeviceParameterContext getDeviceParameterContext() {
        return deviceParameterContext;
    }

    public void setDeviceParameterContext(DeviceParameterContext dpc) {
        this.deviceParameterContext = dpc;
    }

    public ContentEventHandler getContentEventHandler() {
        return contentEventHandler;
    }

    public void setContentEventHandler(ContentEventHandler contentEventHandler) {
        this.contentEventHandler = contentEventHandler;
        for (int i = 0, j = zones.size(); i < j; i++)
            ((ZoneObject) zones.get(i)).setContentEventHandler(contentEventHandler);
    }

    public Map offsetSampleIndexes(Integer sampleOffset, boolean user) {
        Integer[] sid = new Integer[]{IntPool.get(38)};
        Integer[] sval = new Integer[]{sampleOffset};
        HashMap outMap = new HashMap();
        ZoneObject zobj;
        for (int i = 0, j = zones.size(); i < j; i++) {
            try {
                zobj = (ZoneObject) zones.get(i);
                if ((user && zobj.getValue(ID.sample).intValue() <= DeviceContext.MAX_USER_SAMPLE)
                        || (!user && zobj.getValue(ID.sample).intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                ) {
                    zobj.offsetValues(sid, sval, false);
                    outMap.put(IntPool.get(i), ((ZoneObject) zones.get(i)).getValue(sid[0]));
                }
            } catch (IllegalParameterIdException e) {
            } catch (ParameterValueOutOfRangeException e) {
            }
        }
        return outMap;
    }

    // returned map format is voice(Integer)->new sample value(Integer)
    public Map remapSampleIndexes(Map translationMap, Integer defaultSampleTranslation) {
        Integer sid = IntPool.get(38);
        Integer sval;
        HashMap outMap = new HashMap();
        for (int i = 0, j = zones.size(); i < j; i++) {
            try {
                sval = ((ZoneObject) zones.get(i)).getValue(sid);
                if (translationMap.get(sval) instanceof Integer) {
                    ((DatabaseZone) zones.get(i)).setValue(sid, (Integer) translationMap.get(sval));
                    outMap.put(IntPool.get(i), ((ZoneObject) zones.get(i)).getValue(sid));
                } else if (defaultSampleTranslation != null) {
                    ((DatabaseZone) zones.get(i)).setValue(sid, defaultSampleTranslation);
                    outMap.put(IntPool.get(i), defaultSampleTranslation);
                }
            } catch (IllegalParameterIdException e) {
            } catch (ParameterValueOutOfRangeException e) {
            }
        }
        return outMap;
    }

    public void offsetValue(Integer id, Integer offset, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        int idv = id.intValue();
        if (idv >= 129 && idv <= 182 && (idv - 129) % 3 != 2) {
            GeneralParameterDescriptor pd = getDeviceParameterContext().getParameterDescriptor(id);
            Integer nextVal = getDeviceParameterContext().discontinuousOffset(pd, getValue(id), offset.intValue(), constrain);
            if (nextVal == null)
                throw new ParameterValueOutOfRangeException(id);
            setValue(id, nextVal);
            return;
        }
        super.offsetValue(id, offset, constrain);
        //if (postingEvents)
          //  contentEventHandler.postEvent(new VoiceChangeEvent(this, preset, voice, new Integer[]{id}, new Integer[]{getValue(id)}), false);
    }

    public void setValue(Integer id, Integer val) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        Integer[] ivals;
        int diff;
        ArrayList changedIdList = new ArrayList();
        int idv = id.intValue();
        if (idv >= 129 && idv <= 182) {
            if ((idv - 129) % 3 == 0) {
                // we've got a cord src parameter
                putValue(id, getDeviceParameterContext().getNearestCordSrcValue(val));
            } else if ((idv - 129) % 3 == 1) {
                // we've got a cord dest parameter
                putValue(id, getDeviceParameterContext().getNearestCordDestValue(val));
            } else
                putValue(id, val);

            changedIdList.add(id);
        } else
            switch (idv) {
                default:
                    putValue(id, val);
                    changedIdList.add(id);
                    break;

                    // Key Win Low Key
                case 45:
                    ivals = getValues(new Integer[]{IntPool.get(46), IntPool.get(47), IntPool.get(48)});
                    if (ivals[1].intValue() < val.intValue())
                        putValue(id, ivals[1]);
                    else
                        putValue(id, val);

                    changedIdList.add(id);

                    diff = Math.abs(ivals[1].intValue() - val.intValue());
                    if (ivals[0].intValue() > diff) {
                        putValue(IntPool.get(46), IntPool.get(diff));
                        changedIdList.add(IntPool.get(46));
                    }
                    if (ivals[2].intValue() > diff) {
                        putValue(IntPool.get(48), IntPool.get(diff));
                        changedIdList.add(IntPool.get(48));
                    }
                    break;

                    // Key Win Low Fade
                case 46:
                    ivals = getValues(new Integer[]{IntPool.get(45), IntPool.get(47)});

                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    putValue(id, val);
                    changedIdList.add(id);

                    break;

                    // Key Win High Key
                case 47:
                    ivals = getValues(new Integer[]{IntPool.get(45), IntPool.get(46), IntPool.get(48)});
                    if (ivals[0].intValue() > val.intValue())
                        val = ivals[0];

                    putValue(id, val);
                    changedIdList.add(id);

                    diff = Math.abs(val.intValue() - ivals[0].intValue());

                    if (ivals[1].intValue() > diff) {
                        putValue(IntPool.get(46), IntPool.get(diff));
                        changedIdList.add(IntPool.get(46));
                    }
                    if (ivals[2].intValue() > diff) {
                        putValue(IntPool.get(48), IntPool.get(diff));
                        changedIdList.add(IntPool.get(48));
                    }
                    break;
                    // Key Win High Fade
                case 48:
                    ivals = getValues(new Integer[]{IntPool.get(45), IntPool.get(47)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    putValue(id, val);
                    changedIdList.add(id);
                    break;

                    // Vel Win Low Key
                case 49:
                    ivals = getValues(new Integer[]{IntPool.get(50), IntPool.get(51), IntPool.get(52)});
                    if (ivals[1].intValue() < val.intValue())
                        val = ivals[1];

                    putValue(id, val);
                    changedIdList.add(id);

                    diff = Math.abs(ivals[1].intValue() - val.intValue());
                    if (ivals[0].intValue() > diff) {
                        putValue(IntPool.get(50), IntPool.get(diff));
                        changedIdList.add(IntPool.get(50));
                    }
                    if (ivals[2].intValue() > diff) {
                        putValue(IntPool.get(52), IntPool.get(diff));
                        changedIdList.add(IntPool.get(52));
                    }
                    break;
                    // Vel Win Low Fade
                case 50:
                    ivals = getValues(new Integer[]{IntPool.get(49), IntPool.get(51)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    putValue(id, val);
                    changedIdList.add(id);
                    break;
                    // Vel Win High Key
                case 51:
                    ivals = getValues(new Integer[]{IntPool.get(49), IntPool.get(50), IntPool.get(52)});
                    if (ivals[0].intValue() > val.intValue())
                        val = ivals[0];

                    putValue(id, val);
                    changedIdList.add(id);

                    diff = Math.abs(val.intValue() - ivals[0].intValue());

                    if (ivals[1].intValue() > diff) {
                        putValue(IntPool.get(50), IntPool.get(diff));
                        changedIdList.add(IntPool.get(50));
                    }
                    if (ivals[2].intValue() > diff) {
                        putValue(IntPool.get(52), IntPool.get(diff));
                        changedIdList.add(IntPool.get(52));
                    }
                    break;
                    // Vel Win High Fade
                case 52:
                    ivals = getValues(new Integer[]{IntPool.get(49), IntPool.get(51)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    putValue(id, val);
                    changedIdList.add(id);
                    break;
                    // RT Win Low Key
                case 53:
                    ivals = getValues(new Integer[]{IntPool.get(54), IntPool.get(55), IntPool.get(56)});
                    if (ivals[1].intValue() < val.intValue())
                        val = ivals[1];

                    putValue(id, val);
                    changedIdList.add(id);

                    diff = Math.abs(ivals[1].intValue() - val.intValue());
                    if (ivals[0].intValue() > diff) {
                        putValue(IntPool.get(54), IntPool.get(diff));
                        changedIdList.add(IntPool.get(54));
                    }
                    if (ivals[2].intValue() > diff) {
                        putValue(IntPool.get(56), IntPool.get(diff));
                        changedIdList.add(IntPool.get(56));
                    }
                    break;
                    // RT Win Low Fade
                case 54:
                    ivals = getValues(new Integer[]{IntPool.get(53), IntPool.get(55)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    putValue(id, val);
                    changedIdList.add(id);
                    break;
                    // RT Win High Key
                case 55:
                    ivals = getValues(new Integer[]{IntPool.get(53), IntPool.get(54), IntPool.get(56)});
                    if (ivals[0].intValue() > val.intValue())
                        val = ivals[0];

                    putValue(id, val);
                    changedIdList.add(id);

                    diff = Math.abs(val.intValue() - ivals[0].intValue());

                    if (ivals[1].intValue() > diff) {
                        putValue(IntPool.get(54), IntPool.get(diff));
                        changedIdList.add(IntPool.get(54));
                    }
                    if (ivals[2].intValue() > diff) {
                        putValue(IntPool.get(56), IntPool.get(diff));
                        changedIdList.add(IntPool.get(56));
                    }
                    break;
                    // RT Win High Fade
                case 56:
                    ivals = getValues(new Integer[]{IntPool.get(53), IntPool.get(55)});
                    if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                        val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                    putValue(id, val);
                    changedIdList.add(id);

                    break;
            }
        Integer[] changedIds = (Integer[]) changedIdList.toArray(new Integer[changedIdList.size()]);
        if (postingEvents)
            contentEventHandler.postEvent(new VoiceChangeEvent(this, preset, voice, changedIds, getValues(changedIds)), false);
    }

    private void setVoiceNumber(Integer voice) {
        this.voice = voice;
    }

    public String toString() {
        return ("V" + voice.toString());
    }

    void sortZones(final Integer[] ids) {
        Collections.sort(zones, new Comparator() {
            public int compare(Object o1, Object o2) {
                int c = 0;
                int index = 0;
                while (c == 0 && index < ids.length)
                    try {
                        c = ((ZoneObject) o1).getValue(ids[index]).compareTo(((ZoneObject) o2).getValue(ids[index]));
                        index++;
                    } catch (IllegalParameterIdException e) {
                    }
                return c;
            }
        });
        remapZones(0);        
    }

    public DatabaseZone getZone(Integer z) throws NoSuchZoneException {
        if (z.intValue() >= zones.size())
            throw new NoSuchZoneException();
        return (ZoneObject) zones.get(z.intValue());
    }

    public Integer newZone() throws TooManyZonesException {
        return newZone(false);
        /* int enz = zones.size();
        enz = checkForFirstZone(enz);
        Integer nextIndex = IntPool.get(enz);
        try {
            if (enz >= MAX_ZONES)
                throw new TooManyZonesException();

            ZoneObject z = new ZoneObject();
            z.setPostingEvents(isPostingEvents());
            z.initNew(preset, voice, nextIndex, deviceParameterContext, presetEventHandler);
            zones.add(z);
            try {
                // put values directly so we don't updateModel an event (the transfer of values happens automatically at the remote)
                z.putValues(zoneSwitchIds, ((ZoneObject) getZone(IntPool.get(enz - 1))).getValues(zoneSwitchIds)); // zone sample
            } catch (IllegalParameterIdException e) {
                throw new IllegalStateException(this.getClass().toString() + ":copyDatabaseZones -> setValues returned IllegalParameterIdException - id should be valid!");
            } catch (ParameterValueOutOfRangeException e) {
                e.printStackTrace();
            } catch (NoSuchZoneException e) {
                e.printStackTrace();
            }
        } finally {
            //  presetEventHandler.postPresetEvent(new ZoneAddEvent(this, preset, voice, IntPool.get(enz), 1));
        }

        return IntPool.get(enz);
        */
    }

    Integer newZone(boolean externalOnly) throws TooManyZonesException {
        int enz = zones.size();
        enz = checkForFirstZone(enz, externalOnly);
        Integer nextIndex = IntPool.get(enz);
        if (enz >= MAX_ZONES)
            throw new TooManyZonesException();

        ZoneObject z = new ZoneObject();
        z.setPostingEvents(isPostingEvents());
        if (externalOnly)
            z.initExternalNew(preset, voice, nextIndex, deviceParameterContext, contentEventHandler);
        else
            z.initNew(preset, voice, nextIndex, deviceParameterContext, contentEventHandler);

        zones.add(z);
        try {
            // put values directly so we don't updateModel an event (the transfer of values happens automatically at the remote)
            z.putValues(zoneSwitchIds, ((ZoneObject) getZone(IntPool.get(enz - 1))).getValues(zoneSwitchIds)); // zone sample
        } catch (IllegalParameterIdException e) {
            throw new IllegalStateException(this.getClass().toString() + ":copyDatabaseZones -> setValues returned IllegalParameterIdException - id should be valid!");
        } catch (ParameterValueOutOfRangeException e) {
            e.printStackTrace();
        } catch (NoSuchZoneException e) {
            e.printStackTrace();
        }
        return IntPool.get(enz);
    }

    private int checkForFirstZone(int enz, boolean externalOnly) {
        if (enz == 0) {
            // create default zone for voice
            ZoneObject z = new ZoneObject();
            z.setPostingEvents(isPostingEvents());
            if (externalOnly)
                z.initExternalNew(preset, voice, IntPool.get(enz++), deviceParameterContext, contentEventHandler);
            else
                z.initNew(preset, voice, IntPool.get(enz++), deviceParameterContext, contentEventHandler);
            try {
                z.setValues(zoneSwitchIds, getValues(zoneSwitchIds));
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            } catch (ParameterValueOutOfRangeException e) {
                e.printStackTrace();
            }
            zones.add(z);
        }
        return enz;
    }

    public Integer addZoneFromDump(ByteArrayInputStream dis) throws TooManyZonesException, InvalidPresetDumpException {
        int enz = zones.size();
        if (enz + 1 >= MAX_ZONES)
            throw new TooManyZonesException();
        Integer newIndex = IntPool.get(enz);
        ZoneObject z = new ZoneObject();
        z.setPostingEvents(isPostingEvents());
        z.initDump(dis, preset, voice, newIndex, deviceParameterContext, contentEventHandler);
        zones.add(z);
        return newIndex;
    }

    public Integer dropZone(IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws TooManyZonesException {
        int enz = zones.size();

        enz = checkForFirstZone(enz, false);

        if (enz + 1 >= MAX_ZONES)
            throw new TooManyZonesException();
        Integer newIndex = IntPool.get(enz);
        ZoneObject z = new ZoneObject();
        z.setPostingEvents(isPostingEvents());
        z.initDrop(preset, voice, newIndex, iz, deviceParameterContext, contentEventHandler);
        zones.add(z);
        return newIndex;
    }

    public void rmvZone(Integer zone) throws NoSuchZoneException {
        if (zone.intValue() >= zones.size())
            if (zone.intValue() != 0)
                throw new NoSuchZoneException();
            else
                return;

        ((ZoneObject) zones.remove(zone.intValue())).zDispose();
        if (zones.size() == 1) {
            ZoneObject z = (ZoneObject) getZone(IntPool.get(0));
            try {
                setValues(zoneSwitchIds, z.getValues(zoneSwitchIds));
                offsetValues(zoneMergeIds, z.getValues(zoneMergeIds), true);
                ((ZoneObject) zones.remove(0)).zDispose();
                if (postingEvents) {
                    contentEventHandler.postEvent(new ZoneRemoveEvent(this, preset, voice, zone), false);
                    contentEventHandler.postEvent(new ZoneRemoveEvent(this, preset, voice, IntPool.get(0)), false);
                }
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            } catch (ParameterValueOutOfRangeException e) {
                e.printStackTrace();
            }
        } else {
            remapZones(zone.intValue());
            if (postingEvents)
                contentEventHandler.postEvent(new ZoneRemoveEvent(this, preset, voice, zone), false);
        }
    }

    /* public void purgeZones() {
         for (int i = zones.size() - 1; i > 0; i++)
             try {
                 rmvZone(IntPool.get(i));
             } catch (NoSuchZoneException e) {
                 // should never get here
             }
     }*/

    public int numZones() {
        return zones.size();
    }

    public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer z) throws NoSuchZoneException {
        final String pname = getOriginalPresetName();
        ZoneObject zobj = new ZoneObject() {
            public String getOrginalPresetName() {
                return pname;
            }
        };
        zobj.initIsolated(preset, voice, z, (ZoneObject) zones.get(z.intValue()));
        return zobj;
    }

    public Integer getOriginalIndex() {
        return voice;
    }

    public Integer getOriginalPresetIndex() {
        return preset;
    }

    public String getOriginalPresetName() {
        try {
            return deviceParameterContext.getDeviceContext().getDefaultPresetContext().getString(preset);
        } catch (DeviceException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DeviceContext.UNTITLED_PRESET;
    }

    private void remapZones(int fromZone) {
        int nZones = zones.size();
        for (int n = fromZone; n < nZones; n++) {
            ((ZoneObject) zones.get(n)).setZone(IntPool.get(n));
        }
    }
}
