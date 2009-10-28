package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.sample.*;
import com.pcmsolutions.device.EMU.E4.events.sample.requests.SampleDumpRequestEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.requests.SampleDumpResult;
import com.pcmsolutions.device.EMU.E4.events.sample.requests.SampleIsolationRequest;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.SampleDownloadDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.SampleDownloadDescriptorFactory;
import com.pcmsolutions.device.EMU.ROMLocation;
import com.pcmsolutions.device.EMU.database.AbstractDBO;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;
import com.pcmsolutions.gui.ProgressCallback;

import javax.sound.sampled.AudioFileFormat;
import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 04:42:59
 */
class SampleDBO extends AbstractDBO<DatabaseSample, IsolatedSample> implements Serializable {
    static final String EMPTY = DeviceContext.EMPTY_SAMPLE;

    public SampleDBO(Integer index, ManageableContentEventHandler ceh) {
        super(index, ceh);
    }

    public Object retrieveName() throws ContentUnavailableException {
        Object o = performRefresh();
        if (o == null)
            throw new ContentUnavailableException();
        return o;
    }

    public void handleNamedAsEmptyEvent() {
        getEventHandler().postEvent(new SampleRefreshEvent(this, getIndex()));
    }

    public void handleNameChangedEvent(String name) {
        getEventHandler().postEvent(new SampleNameChangeEvent(this, getIndex(), name));
    }

    public void handleUninitializeEvent() {
        getEventHandler().postEvent(new SampleRefreshEvent(this, getIndex()));
    }

    public void handleEraseEvent() {
        getEventHandler().postEvent(new SampleEraseEvent(this, getIndex()));
    }

    public Object performRefresh() {
        SampleDumpRequestEvent sre = new SampleDumpRequestEvent(this, getIndex());
        if (getEventHandler().sendRequest(sre)) {
            SampleDumpResult sdr = sre.getRequestedData();
            if (sdr.isEmpty()) {
                getEventHandler().postInternalEvent(new SampleInitializeEvent(this, getIndex()));
                return EMPTY;
            } else if (sdr.onlyNameProvided()) {
                SampleObject s = new SampleObject();
                s.init(getIndex(), sdr.getName().trim(), getEventHandler(), null);
                getEventHandler().postInternalEvent(new SampleInitializeEvent(this, getIndex()));
                return s;
            } else {
                SampleDescriptor sd = sdr.getDescriptor();
                SampleObject s = new SampleObject();
                s.init(getIndex(), sd.getName(), getEventHandler(), sd);
                getEventHandler().postInternalEvent(new SampleInitializeEvent(this, getIndex()));
                return s;
            }
        }
        return null;
    }

    public boolean testEmpty(String str) {
        return str.equals(DeviceContext.EMPTY_SAMPLE);
    }

    public String emptyString() {
        return EMPTY;
    }

    public IsolatedSample acquireIsolated(DatabaseSample databaseSample, Object flags) throws ContentUnavailableException {
        if (flags instanceof SampleDownloadDescriptor) {
            SampleIsolationRequest sir = new SampleIsolationRequest(this, toString(), (SampleDownloadDescriptor) flags);
            if (getEventHandler().sendRequest(sir))
                return sir.getRequestedData();
        } else if (flags instanceof AudioFileFormat.Type) {
            SampleIsolationRequest sir = new SampleIsolationRequest(this, toString(), SampleDownloadDescriptorFactory.getDownloadToTempFile(getIndex(), (AudioFileFormat.Type) flags));
            if (getEventHandler().sendRequest(sir))
                return sir.getRequestedData();
        }
        throw new ContentUnavailableException();
    }

     interface NewSampleContentFlags{
        IsolatedSample getIsolatedSample();
        ProgressCallback getProgressCallback();
    }
    public DatabaseSample synthesizeNewContent(String name, Object flags) throws ContentUnavailableException {
        if (flags instanceof NewSampleContentFlags) {
            return specifyContentAfterDrop(((NewSampleContentFlags)flags).getIsolatedSample(), name, ((NewSampleContentFlags)flags).getProgressCallback());
        } else
            throw  new ContentUnavailableException();
    }

    public DatabaseSample specifyContentAfterDrop(IsolatedSample isolatedSample, String name, final Object flags) throws ContentUnavailableException {
        final SampleObject s = new SampleObject();
        s.init(getIndex(), name, getEventHandler(), null);
        SampleNewEvent sne = new SampleNewEvent(this, getIndex(), isolatedSample, name, (flags instanceof ProgressCallback?(ProgressCallback)flags: null)){
            public void result(Exception e, boolean wasCancelled) {
                if ( flags instanceof ProgressCallback)
                    ((ProgressCallback)flags).updateProgress(1);
                task_refresh(true);
            }
        };
        getEventHandler().postEvent(sne, true);
        //s.initDrop(isolatedSample, getIndex(), name, getEventHandler(), (flags instanceof ProgressCallback?(ProgressCallback)flags: null));
        return s;
    }

    public Object translateContent(Object rawContent) {
        if (rawContent instanceof String)
            return rawContent;
        else if (rawContent instanceof SampleObject) {
            SampleObject s = new SampleObject();
            s.init((SampleObject) rawContent, getIndex(), getEventHandler());
            return s;
        } else if (rawContent instanceof IsolatedSample) {
            IsolatedSample is = (IsolatedSample) rawContent;
            SampleObject s = new SampleObject();
            s.init(getIndex(), is.getName(), getEventHandler(), null);
            return s;
        } else if (rawContent instanceof ROMLocation) {
            if (((ROMLocation) rawContent).getName().equals(pendingString()))
                return pendingString();
            if (((ROMLocation) rawContent).getName().equals(emptyString()))
                return emptyString();
            SampleObject s = new SampleObject();
            s.init(getIndex(), ((ROMLocation) rawContent).getName(), getEventHandler(), null);
            return s;
        } else
            return null;
    }

    public Object provideCopiedContent(DatabaseSample databaseSample, String name) {
        if (databaseSample instanceof SampleObject)
            throw new IllegalArgumentException("must be a SampleObject to perform copy");
        SampleObject s = new SampleObject();
        s.initCopy((SampleObject) databaseSample, getIndex(), name, getEventHandler());
        return s;
    }
}
