package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.events.preset.LinkAddEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.LinkChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.LinkCopyEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.remote.SysexHelper;
import com.pcmsolutions.device.EMU.E4.remote.DumpParsingUtilities;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;
import com.pcmsolutions.device.EMU.database.events.content.ContentEventHandler;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.DeviceException;

import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * User: paulmeehan
 * Date: 16-Aug-2004
 * Time: 16:31:37
 */
class LinkObject extends Parameterized implements ZDisposable, IsolatedPreset.IsolatedLink, Serializable, DatabaseLink {
    private Integer preset;
    private Integer link;

    protected DeviceParameterContext deviceParameterContext;
    protected ContentEventHandler contentEventHandler;

    private boolean postingEvents = true;

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
    public void initNew(Integer preset, Integer link, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initNew(dpc.getLinkContext(), true);
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.link = link;
        if (postingEvents)
            ceh.postEvent(new LinkAddEvent(this, preset, link), false);
    }

    // new constructor from preset dump
    public void initDump(ByteArrayInputStream dis, int numIds, Integer preset, Integer link, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initNew(dpc.getLinkContext(), false);
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.link = link;
        Integer[] idVals = DumpParsingUtilities.parseDumpStream(dis, dpc.getLinkContext(), numIds);
        initValues(idVals);
    }

    // copy constructor
    public void initCopy(LinkObject src, Integer preset, Integer link, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initCopy(src, dpc.getLinkContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.link = link;
        if (postingEvents)
            ceh.postEvent(new LinkCopyEvent(this, preset, link, src.preset, src.link),false);
    }

    // preset copy constructor
    public void initPresetCopy(LinkObject src, Integer preset, Integer link, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initCopy(src, dpc.getLinkContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.link = link;
        // no event needed
    }

    // isolated constructor
    public void initIsolated(Integer preset, Integer link, LinkObject src) {
        super.initCopy(src, src.deviceParameterContext.getLinkContext());
        this.preset = preset;
        this.deviceParameterContext = src.deviceParameterContext;
        this.contentEventHandler = null;
        this.link = link;
    }

    // drop constructor
    public void initDrop(Integer preset, Integer link, IsolatedPreset.IsolatedLink src, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initDrop(src, dpc.getLinkContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.link = link;
        if (postingEvents) {
            contentEventHandler.postEvent(new LinkAddEvent(this, preset, link),false);
            contentEventHandler.postExternalEvent(new LinkChangeEvent(this, preset, link, getAllIds(), getAllValues()));
        }
    }

    // preset drop constructor
    public void initPresetDrop(Integer preset, Integer link, IsolatedPreset.IsolatedLink src, DeviceParameterContext dpc, ContentEventHandler ceh) {
        super.initDrop(src, dpc.getLinkContext());
        this.preset = preset;
        this.deviceParameterContext = dpc;
        this.contentEventHandler = ceh;
        this.link = link;
        // no need for events
    }

    // copy constructor
    public void initCopy(Integer preset, Integer link, LinkObject src) {
        initCopy(src, preset, link, src.deviceParameterContext, src.contentEventHandler);
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

        for (int i = 0, j = values.length; i < j; i++)
            os.write(SysexHelper.DataOut(values[i]));
        return os;
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
    }

    public void setValue(Integer id, Integer val) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
        Integer[] ivals;
        int diff;
        ArrayList changedIdList = new ArrayList();
        switch (id.intValue()) {
            default:
                putValue(id, val);
                changedIdList.add(id);
                break;
                // Key Win Low Key
            case 28:
                ivals = getValues(new Integer[]{IntPool.get(29), IntPool.get(30), IntPool.get(31)});
                if (ivals[1].intValue() < val.intValue())
                    val = ivals[1];

                putValue(id, val);
                changedIdList.add(id);

                diff = Math.abs(ivals[1].intValue() - val.intValue());
                if (ivals[0].intValue() > diff) {
                    putValue(IntPool.get(29), IntPool.get(diff));
                    changedIdList.add(IntPool.get(29));
                }
                if (ivals[2].intValue() > diff) {
                    putValue(IntPool.get(31), IntPool.get(diff));
                    changedIdList.add(IntPool.get(31));
                }
                break;
                // Key Win Low Fade
            case 29:
                ivals = getValues(new Integer[]{IntPool.get(28), IntPool.get(30)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));
                putValue(id, val);
                changedIdList.add(id);

                break;
                // Key Win High Key
            case 30:
                ivals = getValues(new Integer[]{IntPool.get(28), IntPool.get(29), IntPool.get(31)});
                if (ivals[0].intValue() > val.intValue())
                    val = ivals[0];
                putValue(id, val);
                changedIdList.add(id);

                diff = Math.abs(val.intValue() - ivals[0].intValue());

                if (ivals[1].intValue() > diff) {
                    putValue(IntPool.get(29), IntPool.get(diff));
                    changedIdList.add(IntPool.get(29));
                }
                if (ivals[2].intValue() > diff) {
                    putValue(IntPool.get(31), IntPool.get(diff));
                    changedIdList.add(IntPool.get(31));
                }
                break;
                // Key Win High Fade
            case 31:
                ivals = getValues(new Integer[]{IntPool.get(28), IntPool.get(30)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));
                putValue(id, val);
                changedIdList.add(id);

                break;

                // Vel Win Low Key
            case 32:
                ivals = getValues(new Integer[]{IntPool.get(33), IntPool.get(34), IntPool.get(35)});
                if (ivals[1].intValue() < val.intValue())
                    val = ivals[1];

                putValue(id, val);
                changedIdList.add(id);

                diff = Math.abs(ivals[1].intValue() - val.intValue());
                if (ivals[0].intValue() > diff) {
                    putValue(IntPool.get(33), IntPool.get(diff));
                    changedIdList.add(IntPool.get(33));
                }
                if (ivals[2].intValue() > diff) {
                    putValue(IntPool.get(35), IntPool.get(diff));
                    changedIdList.add(IntPool.get(35));
                }
                break;
                // Vel Win Low Fade
            case 33:
                ivals = getValues(new Integer[]{IntPool.get(32), IntPool.get(34)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));

                putValue(id, val);
                changedIdList.add(id);

                break;
                // Vel Win High Key
            case 34:
                ivals = getValues(new Integer[]{IntPool.get(32), IntPool.get(33), IntPool.get(35)});
                if (ivals[0].intValue() > val.intValue())
                    val = ivals[0];
                putValue(id, val);
                changedIdList.add(id);

                diff = Math.abs(val.intValue() - ivals[0].intValue());

                if (ivals[1].intValue() > diff) {
                    putValue(IntPool.get(33), IntPool.get(diff));
                    changedIdList.add(IntPool.get(33));
                }
                if (ivals[2].intValue() > diff) {
                    putValue(IntPool.get(35), IntPool.get(diff));
                    changedIdList.add(IntPool.get(35));
                }
                break;
                // Vel Win High Fade
            case 35:
                ivals = getValues(new Integer[]{IntPool.get(32), IntPool.get(34)});
                if (val.intValue() > Math.abs(ivals[1].intValue() - ivals[0].intValue()))
                    val = IntPool.get(Math.abs(ivals[1].intValue() - ivals[0].intValue()));
                putValue(id, val);
                changedIdList.add(id);

                break;
        }
        Integer[] changedIds = (Integer[]) changedIdList.toArray(new Integer[changedIdList.size()]);
        if (postingEvents)
            contentEventHandler.postEvent(new LinkChangeEvent(this, preset, link, changedIds, getValues(changedIds)),false);
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
            return deviceParameterContext.getDeviceContext().getDefaultPresetContext().getString(preset);
        } catch (DeviceException e) {
            e.printStackTrace();
        }
        return DeviceContext.UNTITLED_PRESET;
    }

}
