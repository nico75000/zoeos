package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.preset.PresetEraseEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializationStatusChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetNameChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.requests.PresetDumpRequestEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.requests.PresetDumpResult;
import com.pcmsolutions.device.EMU.E4.events.preset.requests.PresetNameRequestEvent;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.device.EMU.database.AbstractDBO;
import com.pcmsolutions.device.EMU.database.AbstractDatabase;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.events.content.ContentEventHandler;
import com.pcmsolutions.gui.ProgressCallback;

import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 13-Aug-2004
 * Time: 19:13:20
 */
class PresetDBO extends AbstractDBO<DatabasePreset, IsolatedPreset> implements AbstractDatabase.DBO<DatabasePreset, IsolatedPreset>, Serializable {
    static final String EMPTY = DeviceContext.EMPTY_PRESET;
    DeviceParameterContext dpc;

    public PresetDBO(Integer index, ContentEventHandler ceh, DeviceParameterContext dpc) {
        super(index, ceh);
        this.dpc = dpc;
    }

    protected Object provideCopiedContent(DatabasePreset dp, String name) {
        if (!(dp instanceof PresetObject))
            throw new IllegalArgumentException("must be a PresetObject to perform copy");

        PresetObject p = new PresetObject();
        p.initCopy((PresetObject) dp, getIndex(), getEventHandler(), dpc);
        p.setName(name);
        return p;
    }

    protected Object translateContent(Object rawContent) {
        if (rawContent instanceof String)
            return rawContent;
        else if (rawContent instanceof PresetObject) {
            PresetObject p = new PresetObject();
            p.initRestore((PresetObject) rawContent, getIndex(), getEventHandler(), dpc);
            return p;
        } else if (rawContent instanceof IsolatedPreset) {
            IsolatedPreset ip = (IsolatedPreset) rawContent;
            PresetObject p = new PresetObject();
            p.postingEvents = false; // so initDrop won't try to dump the preset to the remote
            p.initDrop(getIndex(), ip.getName(), ip, getEventHandler(), dpc, ProgressCallback.DUMMY);
            p.postingEvents = true;
            return p;
        } else
            return null;
    }

    protected String emptyString() {
        return EMPTY;
    }

    protected boolean testEmpty(String str) {
        return str.equals(DeviceContext.EMPTY_PRESET);
    }

    protected String retrieveName() throws ContentUnavailableException {
        PresetNameRequestEvent pnre = new PresetNameRequestEvent(this, getIndex());
        if (getEventHandler().sendRequest(pnre))
            return pnre.getRequestedData();
        else
            throw new ContentUnavailableException("Remote name unavailable");
    }

    protected void handleNamedAsEmptyEvent() {
        getEventHandler().postInternalEvent(new PresetInitializeEvent(this, getIndex()));
    }

    protected void handleNameChangedEvent(String name) {
        getEventHandler().postInternalEvent(new PresetNameChangeEvent(this, getIndex(), name));
    }

    protected void handleEraseEvent() {
        getEventHandler().postEvent(new PresetEraseEvent(this, getIndex()));
    }

    protected void handleUninitializeEvent() {
        getEventHandler().postEvent(new PresetInitializeEvent(this, getIndex()));
    }

    protected IsolatedPreset acquireIsolated(DatabasePreset p, Object flags) {
        return p.getIsolated();
    }

    protected DatabasePreset synthesizeNewContent(String name, Object flags) {
        PresetObject p = new PresetObject();
        p.initNew(getIndex(), name, getEventHandler(), dpc);
        return p;
    }

    protected DatabasePreset specifyContentAfterDrop(IsolatedPreset ip, String name, Object flags) {
        PresetObject p = new PresetObject();
        p.initDrop(getIndex(), name, ip, getEventHandler(), dpc, (flags instanceof ProgressCallback ? (ProgressCallback) flags : ProgressCallback.DUMMY));
        return p;
    }

    protected Object performRefresh() {
        getEventHandler().postEvent(new PresetInitializationStatusChangedEvent(this, getIndex(), 0));
        PresetDumpRequestEvent pdre = new PresetDumpRequestEvent(this, getIndex());
        if (getEventHandler().sendRequest(pdre))
            return finalizeRefresh(pdre.getRequestedData());
        else
            return null;
    }

    private Object finalizeRefresh(PresetDumpResult dump) {
        try {
            if (dump.isEmpty()) {
                getEventHandler().postEvent(new PresetInitializeEvent(this, getIndex()));
                return EMPTY;
            } else {
                PresetObject p = new PresetObject();
                try {
                    p.initDump(dump.getDump(), getEventHandler(), dpc);
                    return p;
                } catch (InvalidPresetDumpException e) {
                    e.printStackTrace();
                } catch (TooManyVoicesException e) {
                    e.printStackTrace();
                } catch (TooManyZonesException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            getEventHandler().postEvent(new PresetInitializationStatusChangedEvent(this, getIndex()));
        }
        return null;
    }
}