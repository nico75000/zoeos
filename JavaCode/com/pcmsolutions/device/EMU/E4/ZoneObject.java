package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.events.preset.ZoneAddEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.ZoneChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.ZoneAddEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.ZoneChangeEvent;
import com.pcmsolutions.device.EMU.E4.remote.SysexHelper;
import com.pcmsolutions.device.EMU.E4.remote.DumpParsingUtilities;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;
import com.pcmsolutions.device.EMU.database.events.content.ContentEventHandler;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;

import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * User: paulmeehan
 * Date: 16-Aug-2004
 * Time: 16:31:13
 */
class ZoneObject extends Parameterized implements ZDisposable, IsolatedPreset.IsolatedVoice.IsolatedZone, Serializable, DatabaseZone {
    private Integer preset;
    private Integer voice;
    private Integer zone;

    protected DeviceParameterContext deviceParameterContext;
    protected ContentEventHandler contentEventHandler;

    private boolean postingEvents = true;

    public boolean isPostingEvents() {
        return postingEvents;
    }

    public void setPostingEvents(boolean postingEvents) {
        this.postingEvents = postingEvents;
    }

    // new constructor from preset dump
    public void initDump(ByteArrayInputStream dis, Integer preset, Integer voice, Integer zone, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initNew(dpc.getZoneContext(), false);
        this.preset = preset;
        this.voice = voice;
        this.zone = zone;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        Integer[] idVals = DumpParsingUtilities.parseDumpStream(dis, dpc.getZoneContext());
        initValues(idVals);
        // no need to fire events
    }

    // copy constructor
    public void initCopy(Integer preset, Integer voice, Integer zone, ZoneObject src, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initCopy(src, dpc.getZoneContext());
        this.zone = zone;
        this.preset = preset;
        this.voice = voice;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        // no need to fire events  - usually used by a preset copy or voice copy - no such thing as remote zone copy
    }

    // copy constructor
    public void initCopy(Integer preset, Integer voice, Integer zone, ZoneObject src) {
        initCopy(preset, voice, zone, src, src.deviceParameterContext, src.contentEventHandler);
    }

    // drop constructor
    public void initDrop(Integer preset, Integer voice, Integer zone, IsolatedPreset.IsolatedVoice.IsolatedZone src, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initDrop(src, dpc.getZoneContext());
        this.zone = zone;
        this.preset = preset;
        this.voice = voice;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        if (postingEvents) {
            if (zone.intValue() != 0)// first zone is created automatically at remote
                contentEventHandler.postEvent(new ZoneAddEvent(this, preset, voice, zone),false);
            else
                contentEventHandler.postInternalEvent(new ZoneAddEvent(this, preset, voice, zone));

            contentEventHandler.postExternalEvent(new ZoneChangeEvent(this, preset, voice, zone, getAllIds(), getAllValues()));
        }
    }

    // voice drop constructor
    public void initVoiceDrop(Integer preset, Integer voice, Integer zone, IsolatedPreset.IsolatedVoice.IsolatedZone src, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initDrop(src, dpc.getZoneContext());
        this.zone = zone;
        this.preset = preset;
        this.voice = voice;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        if (postingEvents) {
            contentEventHandler.postExternalEvent(new ZoneAddEvent(this, preset, voice, zone));
            contentEventHandler.postExternalEvent(new ZoneChangeEvent(this, preset, voice, zone, getAllIds(), getAllValues()));
        }
    }

    // preset drop constructor
    public void initPresetDrop(Integer preset, Integer voice, Integer zone, IsolatedPreset.IsolatedVoice.IsolatedZone src, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initDrop(src, dpc.getZoneContext());
        this.zone = zone;
        this.preset = preset;
        this.voice = voice;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        // no need for events
    }

    // new constructor
    public void initNew(Integer preset, Integer voice, Integer zone, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initNew(dpc.getZoneContext(), true);
        this.preset = preset;
        this.voice = voice;
        this.zone = zone;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        if (postingEvents)
            contentEventHandler.postEvent(new ZoneAddEvent(this, preset, voice, zone),false);
    }

    // external new
    public void initExternalNew(Integer preset, Integer voice, Integer zone, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initNew(dpc.getZoneContext(), true);
        this.preset = preset;
        this.voice = voice;
        this.zone = zone;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        if (postingEvents)
            contentEventHandler.postExternalEvent(new ZoneAddEvent(this, preset, voice, zone));
    }

    // isolated constructor
    public void initIsolated(Integer preset, Integer voice, Integer zone, ZoneObject src) {
        super.initCopy(src, src.deviceParameterContext.getZoneContext());
        this.zone = zone;
        this.preset = preset;
        this.voice = voice;
        this.deviceParameterContext = src.deviceParameterContext;
        this.contentEventHandler = null;
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
        for (int i = 0, j = values.length; i < j; i++)
            os.write(SysexHelper.DataOut(values[i]));
        return os;
    }

    protected Integer finalizeValue(Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException {
        if (id.intValue() == 38) {
            GeneralParameterDescriptor pd = parameterContext.getParameterDescriptor(id);
            if (!pd.isValidValue(value))
                throw new ParameterValueOutOfRangeException(id);
            if (value.intValue() == -1 && pd.getMinValue().intValue() == -1)
                value = IntPool.get(0);
            return value;
        } else
            return super.finalizeValue(id, value);
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

    public void setContentEventHandler(ContentEventHandler contentEventHandler) {
        this.contentEventHandler = contentEventHandler;
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

    public void setValue(Integer id, Integer val) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        Integer[] ivals;
        int diff;
        ArrayList changedIdList = new ArrayList();
        switch (id.intValue()) {
            default:
                super.putValue(id, val);
                changedIdList.add(id);
                break;
                // Key Win Low Key
            case 45:
                ivals = getValues(new Integer[]{IntPool.get(46), IntPool.get(47), IntPool.get(48)});
                if (ivals[1].intValue() < val.intValue())
                    val = ivals[1];

                changedIdList.add(id);
                super.putValue(id, val);

                diff = Math.abs(ivals[1].intValue() - val.intValue());
                if (ivals[0].intValue() > diff) {
                    super.putValue(IntPool.get(46), IntPool.get(diff));
                    changedIdList.add(IntPool.get(46));
                }
                if (ivals[2].intValue() > diff) {
                    super.putValue(IntPool.get(48), IntPool.get(diff));
                    changedIdList.add(IntPool.get(48));
                }
                break;
                // Key Win Low Fade
            case 46:
                ivals = getValues(new Integer[]{IntPool.get(45), IntPool.get(47)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                super.putValue(id, val);
                changedIdList.add(id);
                break;
                // Key Win High Key
            case 47:
                ivals = getValues(new Integer[]{IntPool.get(45), IntPool.get(46), IntPool.get(48)});

                if (ivals[0].intValue() > val.intValue())
                    val = ivals[0];

                super.putValue(id, val);
                changedIdList.add(id);

                diff = Math.abs(val.intValue() - ivals[0].intValue());

                if (ivals[1].intValue() > diff) {
                    super.putValue(IntPool.get(46), IntPool.get(diff));
                    changedIdList.add(IntPool.get(46));
                }
                if (ivals[2].intValue() > diff) {
                    super.putValue(IntPool.get(48), IntPool.get(diff));
                    changedIdList.add(IntPool.get(48));
                }
                break;
                // Key Win High Fade
            case 48:
                ivals = super.getValues(new Integer[]{IntPool.get(45), IntPool.get(47)});

                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                super.putValue(id, val);
                changedIdList.add(id);
                break;

                // Vel Win Low Key
            case 49:
                ivals = getValues(new Integer[]{IntPool.get(50), IntPool.get(51), IntPool.get(52)});

                if (ivals[1].intValue() < val.intValue())
                    val = ivals[1];

                super.putValue(id, val);
                changedIdList.add(id);

                diff = Math.abs(ivals[1].intValue() - val.intValue());
                if (ivals[0].intValue() > diff) {
                    super.putValue(IntPool.get(50), IntPool.get(diff));
                    changedIdList.add(IntPool.get(50));
                }
                if (ivals[2].intValue() > diff) {
                    super.putValue(IntPool.get(52), IntPool.get(diff));
                    changedIdList.add(IntPool.get(52));
                }
                break;
                // Vel Win Low Fade
            case 50:
                ivals = super.getValues(new Integer[]{IntPool.get(49), IntPool.get(51)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                super.putValue(id, val);
                changedIdList.add(id);

                break;
                // Vel Win High Key
            case 51:
                ivals = getValues(new Integer[]{IntPool.get(49), IntPool.get(50), IntPool.get(52)});
                if (ivals[0].intValue() > val.intValue())
                    val = ivals[0];

                super.putValue(id, val);
                changedIdList.add(id);

                diff = Math.abs(val.intValue() - ivals[0].intValue());

                if (ivals[1].intValue() > diff) {
                    super.putValue(IntPool.get(50), IntPool.get(diff));
                    changedIdList.add(IntPool.get(50));
                }
                if (ivals[2].intValue() > diff) {
                    super.putValue(IntPool.get(52), IntPool.get(diff));
                    changedIdList.add(IntPool.get(50));
                }
                break;

                // Vel Win High Fade
            case 52:
                ivals = getValues(new Integer[]{IntPool.get(49), IntPool.get(51)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                super.putValue(id, val);
                changedIdList.add(id);

                break;
        }
        Integer[] changedIds = (Integer[]) changedIdList.toArray(new Integer[changedIdList.size()]);
        if (postingEvents)
            contentEventHandler.postEvent(new ZoneChangeEvent(this, preset, voice, zone, changedIds, getValues(changedIds)),false);
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
            return deviceParameterContext.getDeviceContext().getDefaultPresetContext().getString(preset);
        } catch (DeviceException e) {
            e.printStackTrace();
        } 
        return DeviceContext.UNTITLED_PRESET;
    }
}
