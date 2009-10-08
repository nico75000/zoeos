package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.RemoteMessagingException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 14:28:24
 */
class Impl_OnlineSampleContext extends Impl_SampleContext implements RemoteAssignable {
    protected transient Remotable remote;

    public Impl_OnlineSampleContext(DeviceContext device, String name, SampleDatabaseProxy sdbp, SampleMediator sm, Remotable remote) {
        super(device, name, sdbp, sm);
        this.remote = remote;
    }

    public void refreshSample(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        super.refreshSample(sample);
        /*try {
            this.retrieveLocalCopy(sample, true);
        } catch (NotSMDICoupledException e) {
            e.printStackTrace();
        } catch (SmdiNoSampleException e) {
            e.printStackTrace();
        } catch (SmdiOutOfRangeException e) {
            e.printStackTrace();
        } catch (SmdiGeneralException e) {
            e.printStackTrace();
        } catch (TargetNotSMDIException e) {
            e.printStackTrace();
        } catch (SmdiFileOpenException e) {
            e.printStackTrace();
        } catch (NoSuchContextException e) {
            e.printStackTrace();
        } catch (NoSuchSampleException e) {
            e.printStackTrace();
        } catch (SampleEmptyException e) {
            e.printStackTrace();
        } */
    }

    public void eraseSample(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            super.eraseSample(sample);
            remote.getSampleContext().cmd_delete(sample);
        } catch (RemoteUnreachableException e) {
            reader.changeSampleObject(this, sample, new UninitSampleObject(sample));
            device.logCommError(e);
        } catch (RemoteMessagingException e) {
            reader.changeSampleObject(this, sample, new UninitSampleObject(sample));
            device.logCommError(e);
        } finally {
            reader.release();
        }
    }

    public void setSampleName(Integer sample, String name) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            super.setSampleName(sample, name);
            remote.getSampleContext().edit_name(sample, name);
        } catch (RemoteUnreachableException e) {
            reader.changeSampleObject(this, sample, new UninitSampleObject(sample));
            device.logCommError(e);
        } catch (RemoteMessagingException e) {
            reader.changeSampleObject(this, sample, new UninitSampleObject(sample));
            device.logCommError(e);
        } finally {
            reader.release();
        }
    }

    public void setRemote(Remotable r) {
        remote = r;
    }
}
