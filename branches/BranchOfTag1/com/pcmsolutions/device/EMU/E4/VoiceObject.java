package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.PresetRefreshEvent;
import com.pcmsolutions.device.EMU.E4.events.VoiceChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.ZoneAddEvent;
import com.pcmsolutions.device.EMU.E4.events.ZoneRemoveEvent;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 03-May-2003
 * Time: 16:34:47
 * To change this template use Options | File Templates.
 */
class VoiceObject extends Parameterized implements ZDisposable, IsolatedPreset.IsolatedVoice, Serializable {
    private boolean postingEvents = true;
    private static final int MAX_ZONES = 255;

    // this is a vector because refreshPreset && getPresetRead change preset objects on the fly and may call zDispose while somebody else is reading
    private final Vector zones = new Vector();

    private Integer preset;
    private Integer voice;

    private final static Integer[] zoneSwitchIds = new Integer[]{IntPool.get(38), IntPool.get(44)};
    private final static Integer[] zoneMergeIds = new Integer[]{IntPool.get(39), IntPool.get(40), IntPool.get(42)};
    protected DeviceParameterContext deviceParameterContext;
    protected PresetEventHandler presetEventHandler;

    public boolean isPostingEvents() {
        return postingEvents;
    }

    public void setPostingEvents(boolean postingEvents) {
        this.postingEvents = postingEvents;
    }

    // new constructor from preset dump
    public VoiceObject(ByteArrayInputStream dis, Integer preset, Integer voice, DeviceParameterContext dpc, PresetEventHandler peh) throws InvalidPresetDumpException, TooManyZonesException {
        super(dpc.getVoiceContext(), false);
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
        this.voice = voice;
        this.preset = preset;
        byte[] field = new byte[2];

        dis.read(field, 0, 2);
        Integer group = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn(field);

        dis.read(field, 0, 2);
        // sample might be a sample number of 0x3FFF ( multisample )
        int sample = com.pcmsolutions.device.EMU.E4.SysexHelper.DataIn_int(field);

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

        initValues(PresetDatabase.parseDumpStream(dis, ids));

        dis.read(field, 0, 2);
        int numZones = SysexHelper.DataIn_int(field);
        postingEvents = false;
        if (numZones > 1)
            for (int n = 0; n < numZones; n++)
                addZone(dis);
        postingEvents = true;
    }

    // copy constructor
    public VoiceObject(VoiceObject src, Integer preset, Integer voice, DeviceParameterContext dpc, PresetEventHandler peh) {
        super(src, dpc.getVoiceContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
        this.voice = voice;

        int znCount = src.zones.size();
        for (int n = 0; n < znCount; n++)
            zones.add(new ZoneObject(preset, voice, IntPool.get(n), (ZoneObject) src.zones.get(n), dpc, peh));
    }

    // copy constructor
    public VoiceObject(Integer preset, Integer voice, IsolatedPreset.IsolatedVoice src, DeviceParameterContext dpc, PresetEventHandler peh) {
        super(src, dpc.getVoiceContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
        this.voice = voice;

        int znCount = src.numZones();
        for (int n = 0; n < znCount; n++)
            try {
                zones.add(new ZoneObject(preset, voice, IntPool.get(n), src.getIsolatedZone(IntPool.get(n)), dpc, peh));
            } catch (NoSuchZoneException e) {
                e.printStackTrace();
                // Should never get here
            }
    }

    // new constructor
    public VoiceObject(Integer preset, Integer voice, DeviceParameterContext dpc, PresetEventHandler peh) {
        super(dpc.getVoiceContext(), true);
        this.preset = preset;
        this.voice = voice;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
    }

    // copy constructor for same preset database
    public VoiceObject(Integer preset, Integer voice, VoiceObject src) {
        this(src, preset, voice, src.deviceParameterContext, src.presetEventHandler);
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
        for (int i = 2,j = values.length; i < j; i++)
            os.write(SysexHelper.DataOut(values[i]));

        // ZONES
        if (zones.size() > 0) {
            os.write(SysexHelper.DataOut(zones.size()));
            for (int i = 0,j = zones.size(); i < j; i++)
                ((ZoneObject) zones.get(i)).getDumpBytes().writeTo(os);
        } else
            os.write(SysexHelper.DataOut(1));

        return os;
    }

    public Integer getSample() {
        return (Integer) params.get(IntPool.get(38));
    }

    public void applySingleSample(Integer sample) throws ParameterValueOutOfRangeException {
        Integer[] sid = new Integer[]{IntPool.get(38)};
        Integer[] sval = new Integer[]{sample};
        for (int i = 0,j = zones.size(); i < j; i++) {
            try {
                ((ZoneObject) zones.get(i)).setValues(sid, sval);
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            }
        }
    }

    public IntegerUseMap getReferencedSampleUsage() {
        IntegerUseMap useMap = new IntegerUseMap();
        Integer s = getSample();

        if (s != null & s.intValue() >= 0)
            useMap.addIntegerReference(s);

        for (int i = 0,j = zones.size(); i < j; i++) {
            s = ((ZoneObject) zones.get(i)).getSample();
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

    protected Integer checkValue(Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException {
        if (id.intValue() == 38) {
            GeneralParameterDescriptor pd = parameterContext.getParameterDescriptor(id);
            if (!pd.isValidValue(value))
                throw new ParameterValueOutOfRangeException();
            if (value.intValue() == -1 && pd.getMinValue().intValue() == -1)
                value = IntPool.get(0);
            return value;
        } else
            return super.checkValue(id, value);
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
    }

    public DeviceParameterContext getDeviceParameterContext() {
        return deviceParameterContext;
    }

    public void setDeviceParameterContext(DeviceParameterContext dpc) {
        this.deviceParameterContext = dpc;
    }

    public PresetEventHandler getPresetEventHandler() {
        return presetEventHandler;
    }

    public void setPresetEventHandler(PresetEventHandler presetEventHandler) {
        this.presetEventHandler = presetEventHandler;
          for (int i = 0,j = zones.size(); i < j; i++)
            ((ZoneObject) zones.get(i)).setPresetEventHandler(presetEventHandler);
    }

    public Map offsetSampleIndexes(Integer sampleOffset, boolean user) {
        Integer[] sid = new Integer[]{IntPool.get(38)};
        Integer[] sval = new Integer[]{sampleOffset};
        HashMap outMap = new HashMap();
        ZoneObject zobj;
        for (int i = 0,j = zones.size(); i < j; i++) {
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
        for (int i = 0,j = zones.size(); i < j; i++) {
            try {
                sval = ((ZoneObject) zones.get(i)).getValue(sid);
                if (translationMap.get(sval) instanceof Integer) {
                    ((ZoneObject) zones.get(i)).setValue(sid, (Integer) translationMap.get(sval));
                    outMap.put(IntPool.get(i), ((ZoneObject) zones.get(i)).getValue(sid));
                } else if (defaultSampleTranslation != null) {
                    ((ZoneObject) zones.get(i)).setValue(sid, defaultSampleTranslation);
                    outMap.put(IntPool.get(i), defaultSampleTranslation);
                }
            } catch (IllegalParameterIdException e) {
            } catch (ParameterValueOutOfRangeException e) {
            }
        }
        return outMap;
    }

    public void setValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.setValues(ids, values);
        if (postingEvents)
            presetEventHandler.postPresetEvent(new VoiceChangeEvent(this, preset, voice, ids));
    }

    public void offsetValues(Integer[] ids, Integer[] values, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.offsetValues(ids, values, constrain);
        if (postingEvents)
            presetEventHandler.postPresetEvent(new VoiceChangeEvent(this, preset, voice, ids));
    }

    public void setValue(Integer id, Integer value) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        setValues(new Integer[]{id}, new Integer[]{value});
    }

    public void offsetValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException {
        super.offsetValues(ids, values);
        if (postingEvents)
            presetEventHandler.postPresetEvent(new VoiceChangeEvent(this, preset, voice, ids));
    }

    private void setVoiceNumber(Integer voice) {
        this.voice = voice;
    }

    public String toString() {
        return ("V" + voice.toString());
    }

    public void sortZones(final Integer[] ids) {
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
        if (postingEvents)
            presetEventHandler.postPresetEvent(new PresetRefreshEvent(this, preset));
    }

    public void defaultValues(Integer[] ids) throws IllegalParameterIdException {
        super.defaultValues(ids);
        if (postingEvents)
            presetEventHandler.postPresetEvent(new VoiceChangeEvent(this, preset, voice, ids));
    }

    public ZoneObject getZone(Integer z) throws NoSuchZoneException {
        if (z.intValue() >= zones.size())
            throw new NoSuchZoneException();
        return (ZoneObject) zones.get(z.intValue());
    }

    public Integer addZones(Integer n) throws TooManyZonesException {
        int enz = zones.size();
        int nnz = n.intValue();
        Integer nextIndex;

        int added = 0;

        enz = checkForFirstZone(enz);
        try {
            for (int i = 0; i < nnz; i++) {
                if (enz + i >= MAX_ZONES)
                    throw new TooManyZonesException();

                nextIndex = IntPool.get(enz + i);
                ZoneObject z = new ZoneObject(preset, voice, nextIndex, deviceParameterContext, presetEventHandler);
                zones.add(z);
                added++;
                try {
                    z.setValues(zoneSwitchIds, getZone(IntPool.get(enz + i - 1)).getValues(zoneSwitchIds), false); // zone sample
                } catch (IllegalParameterIdException e) {
                    throw new IllegalStateException(this.getClass().toString() + ":addZones -> setValues returned IllegalParameterIdException - id should be valid!");
                } catch (ParameterValueOutOfRangeException e) {
                    e.printStackTrace();
                } catch (NoSuchZoneException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            if (added > 0)
                if (postingEvents)
                    presetEventHandler.postPresetEvent(new ZoneAddEvent(this, preset, voice, IntPool.get(enz), added));
        }

        return IntPool.get(enz);
    }

    private int checkForFirstZone(int enz) {
        if (enz == 0) {
            // create zone for voice
            ZoneObject z = new ZoneObject(preset, voice, IntPool.get(enz++), deviceParameterContext, presetEventHandler);
            try {
                z.setValues(zoneSwitchIds, getValues(zoneSwitchIds));
                //super.setValues(new Integer[]{IntPool.get(38)}, new Integer[]{IntPool.get(-1)});
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            } catch (ParameterValueOutOfRangeException e) {
                e.printStackTrace();
            }
            zones.add(z);
            if (postingEvents)
                presetEventHandler.postPresetEvent(new ZoneAddEvent(this, preset, voice, IntPool.get(0), 1));
        }
        return enz;
    }

    public Integer addZones(ZoneObject[] newZones, boolean check) throws TooManyZonesException {
        int nnz = newZones.length;
        int enz = zones.size();
        Integer nextIndex;

        int added = 0;
        if (check)
            enz = checkForFirstZone(enz);
        try {
            for (int i = 0; i < nnz; i++) {
                if (enz + i >= MAX_ZONES)
                    throw new TooManyZonesException();
                nextIndex = IntPool.get(enz + i);
                zones.add(new ZoneObject(preset, voice, nextIndex, newZones[i]));
                added++;
            }
        } finally {
            if (added > 0)
                if (postingEvents)
                    presetEventHandler.postPresetEvent(new ZoneAddEvent(this, preset, voice, IntPool.get(enz), added));
        }
        return IntPool.get(enz);
    }

    public Integer addZone(ByteArrayInputStream dis) throws TooManyZonesException, InvalidPresetDumpException {
        int enz = zones.size();
        if (enz + 1 >= MAX_ZONES)
            throw new TooManyZonesException();
        Integer newIndex = IntPool.get(enz);
        zones.add(new ZoneObject(dis, preset, voice, newIndex, deviceParameterContext, presetEventHandler));

        if (postingEvents)
            presetEventHandler.postPresetEvent(new ZoneAddEvent(this, preset, voice, IntPool.get(enz), 1));

        return newIndex;
    }

    public Integer addZone(IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws TooManyZonesException {
        int enz = zones.size();

        enz = checkForFirstZone(enz);

        if (enz + 1 >= MAX_ZONES)
            throw new TooManyZonesException();
        Integer newIndex = IntPool.get(enz);
        zones.add(new ZoneObject(preset, voice, newIndex, iz, deviceParameterContext, presetEventHandler));

        if (postingEvents)
            presetEventHandler.postPresetEvent(new ZoneAddEvent(this, preset, voice, IntPool.get(enz), 1));

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
            ZoneObject z = getZone(IntPool.get(0));
            try {
                setValues(zoneSwitchIds, z.getValues(zoneSwitchIds));
                offsetValues(zoneMergeIds, z.getValues(zoneMergeIds), true);
                ((ZoneObject) zones.remove(0)).zDispose();
                if (postingEvents) {
                    presetEventHandler.postPresetEvent(new ZoneRemoveEvent(this, preset, voice, zone));
                    presetEventHandler.postPresetEvent(new ZoneRemoveEvent(this, preset, voice, IntPool.get(0)));
                }
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            } catch (ParameterValueOutOfRangeException e) {
                e.printStackTrace();
            }
        } else {
            remapZones(zone.intValue());
            if (postingEvents)
                presetEventHandler.postPresetEvent(new ZoneRemoveEvent(this, preset, voice, zone));
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
        ZoneObject zobj = new ZoneObject(preset, voice, z, (ZoneObject) zones.get(z.intValue())) {
            public String getOrginalPresetName() {
                return pname;
            }
        };
        //zobj.setDeviceParameterContext(null);
        zobj.setPresetEventHandler(null);
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
            return deviceParameterContext.getDeviceContext().getDefaultPresetContext().getPresetName(preset);
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        } catch (PresetEmptyException e) {
            e.printStackTrace();
        } catch (ZDeviceNotRunningException e) {
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
