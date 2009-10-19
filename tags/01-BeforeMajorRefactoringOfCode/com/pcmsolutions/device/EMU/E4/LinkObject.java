package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.LinkChangeEvent;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
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
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 03-May-2003
 * Time: 16:08:09
 * To change this template use Options | File Templates.
 */
class LinkObject extends Parameterized implements ZDisposable, IsolatedPreset.IsolatedLink, Serializable {
    private boolean postingEvents = true;

    private Integer preset;
    private Integer link;

    protected DeviceParameterContext deviceParameterContext;
    protected PresetEventHandler presetEventHandler;

    public boolean isPostingEvents() {
        return postingEvents;
    }

    public void setPostingEvents(boolean postingEvents) {
        this.postingEvents = postingEvents;
    }

    public Integer getLink() {
        return link;
    }

    public void setLink(Integer link) {
        this.link = link;
    }

    // new constructor
    public LinkObject(Integer preset, Integer link, DeviceParameterContext dpc, PresetEventHandler peh) {
        super(dpc.getLinkContext(), true);
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
        this.link = link;
    }

    // new constructor from preset dump
    public LinkObject(ByteArrayInputStream dis, int numIds, Integer preset, Integer link, DeviceParameterContext dpc, PresetEventHandler peh) throws InvalidPresetDumpException {
        super(dpc.getLinkContext(), false);
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
        this.link = link;
        Integer[] idVals = PresetDatabase.parseDumpStream(dis, dpc.getLinkContext(), numIds);
        initValues(idVals);
    }

    // copy constructor
    public LinkObject(Parameterized src, Integer preset, Integer link, DeviceParameterContext dpc, PresetEventHandler peh) {
        super(src, dpc.getLinkContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
        this.link = link;
    }

    // copy constructor
    public LinkObject(Integer preset, Integer link, IsolatedPreset.IsolatedLink src, DeviceParameterContext dpc, PresetEventHandler peh) {
        super(src, dpc.getLinkContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.presetEventHandler = peh;
        this.link = link;
    }

    // copy constructor
    public LinkObject(Integer preset, Integer link, LinkObject src) {
        this(src, preset, link, src.deviceParameterContext, src.presetEventHandler);
    }

    public ByteArrayOutputStream getDumpBytes() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Integer[] values = getAllValues();
        if (deviceParameterContext.isWeirdPresetDumping()) {
            List l = Arrays.asList(values);
            l.subList(0, l.size() - 1);
            values = (Integer[]) l.toArray(new Integer[l.size()]);
        } else {
            if (values.length != deviceParameterContext.getLinkContext().size())
                throw new IllegalStateException("number of link parameters mismatch");
        }

        for (int i = 0,j = values.length; i < j; i++)
            os.write(SysexHelper.DataOut(values[i]));
        return os;
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
    }

    public void setValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.setValues(ids, values);
        if (postingEvents)
            presetEventHandler.postPresetEvent(new LinkChangeEvent(this, preset, link, ids));
    }

    public void setValue(Integer id, Integer value) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        setValues(new Integer[]{id}, new Integer[]{value});
    }

    public void offsetValues(Integer[] ids, Integer[] values) throws IllegalParameterIdException {
        super.offsetValues(ids, values);
        presetEventHandler.postPresetEvent(new LinkChangeEvent(this, preset, link, ids));
    }

    public void offsetValues(Integer[] ids, Integer[] values, boolean constrain) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        super.offsetValues(ids, values, constrain);
        if (postingEvents)
            presetEventHandler.postPresetEvent(new LinkChangeEvent(this, preset, link, ids));
    }

    public void defaultValues(Integer[] ids) throws IllegalParameterIdException {
        super.defaultValues(ids);
        if (postingEvents)
            presetEventHandler.postPresetEvent(new LinkChangeEvent(this, preset, link, ids));
    }

    public Integer getPreset() {
        return (Integer) params.get(IntPool.get(23));
    }

    public String toString() {
        return "L" + link.toString();
    }

    public Integer getOriginalIndex() {
        return link;
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