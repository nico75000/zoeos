package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.ZoneChangeEvent;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 03-May-2003
 * Time: 16:59:02
 * To change this template use Options | File Templates.
 */
class ZoneObject extends Parameterized implements ZDisposable, IsolatedPreset.IsolatedVoice.IsolatedZone, Serializable {
    private Integer preset;
    private Integer voice;
    private Integer zone;

    protected DeviceParameterContext deviceParameterContext;
    protected PresetEventHandler presetEventHandler;

    // new constructor from preset dump
    public ZoneObject(ByteArrayInputStream dis, Integer preset, Integer voice, Integer zone, DeviceParameterContext dpc, PresetEventHandler peh) throws InvalidPresetDumpException {
        super(dpc.getZoneContext(), false);
        this.preset = preset;
        this.voice = voice;
        this.zone = zone;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
        Integer[] idVals = PresetDatabase.parseDumpStream(dis, dpc.getZoneContext());
        initValues(idVals);
    }

    // copy constructor
    public ZoneObject(Integer preset, Integer voice, Integer zone, ZoneObject src, DeviceParameterContext dpc, PresetEventHandler peh) {
        super(src, dpc.getZoneContext());
        this.zone = zone;
        this.preset = preset;
        this.voice = voice;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
    }

    // copy constructor
    public ZoneObject(Integer preset, Integer voice, Integer zone, ZoneObject src) {
        this(preset, voice, zone, src, src.deviceParameterContext, src.presetEventHandler);
    }

    // copy constructor
    public ZoneObject(Integer preset, Integer voice, Integer zone, IsolatedPreset.IsolatedVoice.IsolatedZone src, DeviceParameterContext dpc, PresetEventHandler peh) {
        super(src, dpc.getZoneContext());
        this.zone = zone;
        this.preset = preset;
        this.voice = voice;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
    }

    // new constructor
    public ZoneObject(Integer preset, Integer voice, Integer zone, DeviceParameterContext dpc, PresetEventHandler peh) {
        super(dpc.getZoneContext(), true);
        this.preset = preset;
        this.voice = voice;
        this.zone = zone;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
    }

    public Integer getZone() {
        return zone;
    }

    public void setZone(Integer zone) {
        this.zone = zone;
    }

    public ByteArrayOutputStream getDumpBytes() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Integer[] values = getAllValues();
        if (values.length != deviceParameterContext.getZoneContext().size())
            throw new IllegalStateException("number of zone parameters mismatch");
        for (int i = 0,j = values.length; i < j; i++)
            os.write(SysexHelper.DataOut(values[i]));
        return os;
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

    public DeviceParameterContext getDeviceParameterContext() {
        return deviceParameterContext;
    }

    public void setDeviceParameterContext(DeviceParameterContext deviceParameterContext) {
        this.deviceParameterContext = deviceParameterContext;
    }

    public PresetEventHandler getPresetEventHandler() {
        return presetEventHandler;
    }

    protected Integer constrainValue(GeneralParameterDescriptor pd, Integer val) {
        val = super.constrainValue(pd, val);
        if (pd.getId().intValue() == 38)
            if (val.intValue() == -1 && pd.getMinValue().intValue() == -1)
                val = IntPool.get(0);
        return val;
    }

    public Integer getSample() {
        return (Integer) params.get(ID.sample);

    }

    public void setPresetEventHandler(PresetEventHandler presetEventHandler) {
        this.presetEventHandler = presetEventHandler;
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

    public void setValue(Integer id, Integer value) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        setValues(new Integer[]{id}, new Integer[]{value}, true);
    }

    public void setValues(Integer[] ids, Integer[] values, boolean postEvent) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.setValues(ids, values);
        if (postEvent)
            presetEventHandler.postPresetEvent(new ZoneChangeEvent(this, preset, voice, zone, ids));
    }

    public void setValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        setValues(ids, values, true);
    }

    public void defaultValues(Integer[] ids, boolean postEvent) throws IllegalParameterIdException {
        super.defaultValues(ids);
        if (postEvent)
            presetEventHandler.postPresetEvent(new ZoneChangeEvent(this, preset, voice, zone, ids));
    }

    public void defaultValues(Integer[] ids) throws IllegalParameterIdException {
        defaultValues(ids, true);
    }

    public void offsetValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException {
        super.offsetValues(ids, values);
        presetEventHandler.postPresetEvent(new ZoneChangeEvent(this, preset, voice, zone, ids));
    }

    public void offsetValues(Integer[] ids, Integer[] values, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        offsetValues(ids, values, constrain, true);
    }

    public void offsetValues(Integer[] ids, Integer[] values, boolean constrain, boolean postEvent) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.offsetValues(ids, values, constrain);
        if (postEvent)
            presetEventHandler.postPresetEvent(new ZoneChangeEvent(this, preset, voice, zone, ids));
    }

    public String toString() {
        return ("V" + voice.toString());
    }

    public Integer getOriginalIndex() {
        return zone;
    }

    public Integer getOriginalVoiceIndex() {
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

}


