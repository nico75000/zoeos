package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.RemoteDeviceDidNotRespondException;
import com.pcmsolutions.comms.RemoteMessagingException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.sample.*;
import com.pcmsolutions.device.EMU.E4.events.sample.requests.SampleDumpRequestEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.requests.SampleDumpResult;
import com.pcmsolutions.device.EMU.E4.events.sample.requests.SampleIsolationRequest;
import com.pcmsolutions.device.EMU.E4.events.sample.requests.SampleRequestEvent;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.remote.Remotable;
import com.pcmsolutions.device.EMU.E4.remote.RemoteFactory;
import com.pcmsolutions.device.EMU.E4.remote.SampleMediator;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.SampleDownloadDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.SampleDownloadDescriptorFactory;
import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.smdi.SmdiSampleEmptyException;
import com.pcmsolutions.smdi.SmdiTransferAbortedException;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 07:09:43
 */
public class RemoteSampleSynchronizer implements Serializable, ManageableContentEventHandler.ExternalHandler<SampleEvent, SampleListener>, ManageableContentEventHandler.RequestHandler<SampleRequestEvent>, RemoteAssignable {
    private transient Remotable remote;
    private E4Device device;
    private ManageableContentEventHandler ceh;
    private transient SampleMediator sampleMediator;

    public RemoteSampleSynchronizer(E4Device device, Remotable remote) {
        setRemote(remote);
        this.device = device;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

    public boolean handleEvent(SampleEvent event) {
        // SAMPLE NAME CHANGE
        if (event instanceof SampleNameChangeEvent) {
            SampleNameChangeEvent snce = (SampleNameChangeEvent) event;
            try {
                remote.getSampleContext().edit_name(snce.getIndex(), snce.getName());
            } catch (RemoteMessagingException e) {
                device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                device.logCommError(e);
            }
        } else if (event instanceof SampleEraseEvent) {
            try {
                remote.getSampleContext().cmd_delete(event.getIndex());
            } catch (RemoteMessagingException e) {
                device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                device.logCommError(e);
            }
        }
        // SAMPLE NEW
        else if (event instanceof SampleNewEvent) {
            SampleNewEvent sne = (SampleNewEvent) event;
            IsolatedSample is = sne.getIsolatedSample();
            ProgressCallback prog = null;
            try {
                prog = sne.getProgressCallback();
                sampleMediator.sendSample(event.getIndex(), is, sne.getName(), (prog != null ? prog : ProgressCallback.DUMMY));
                return true;
            } catch (SampleMediator.SampleMediationException e) {
                e.printStackTrace();
            } catch (IsolatedSampleUnavailableException e) {
                e.printStackTrace();
            } catch (SmdiTransferAbortedException e) {
                e.printStackTrace();
            } catch (DeviceException e) {
                e.printStackTrace();
            } finally {
                if (prog != null)
                    prog.updateProgress(1);
            }
        }
        // SAMPLE COPY
        else if (event instanceof SampleCopyEvent) {
            SampleCopyEvent sce = (SampleCopyEvent) event;
            try {
                sampleMediator.copySample(sce.getSrcIndex(), new Integer[]{sce.getIndex()}, new String[]{sce.getDestName()}, ProgressCallback.DUMMY);
                return true;
            } catch (SampleMediator.SampleMediationException e) {
                e.printStackTrace();
            } catch (SmdiSampleEmptyException e) {
                e.printStackTrace();
            } catch (SmdiTransferAbortedException e) {
                e.printStackTrace();
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Object handleRequest(SampleRequestEvent event) {
        // SAMPLE DUMP REQUEST
        if (event.getClass().equals(SampleDumpRequestEvent.class)) {
            Integer index = event.getIndex();
            if (remote.isSmdiCoupled() && index.intValue() >= DeviceContext.FIRST_USER_SAMPLE && index.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                try {
                    final SampleDescriptor sd = sampleMediator.getSampleDescriptor(index);
                    // System.out.println("smdi sample name");
                    return new SampleDumpResult() {
                        public SampleDescriptor getDescriptor() {
                            return sd;
                        }

                        public boolean isEmpty() {
                            return false;
                        }

                        public boolean onlyNameProvided() {
                            return false;
                        }

                        public String getName() {
                            return sd.getName();
                        }
                    };
                } catch (SmdiSampleEmptyException e) {
                    // System.out.println("smdi empty sample: " + event.getIndex());
                    return new SampleDumpResult() {
                        public SampleDescriptor getDescriptor() {
                            return null;
                        }

                        public boolean isEmpty() {
                            return true;
                        }

                        public boolean onlyNameProvided() {
                            return true;
                        }

                        public String getName() {
                            return DeviceContext.EMPTY_SAMPLE;
                        }
                    };
                } catch (SampleMediator.SampleMediationException e) {
                    //device.logCommError(e);
                    e.printStackTrace();
                }
            try {
                final String name = remote.getSampleContext().req_name(index);
                return new SampleDumpResult() {
                    public SampleDescriptor getDescriptor() {
                        return null;
                    }

                    public boolean isEmpty() {
                        return name.trim().equals(DeviceContext.EMPTY_SAMPLE);
                    }

                    public boolean onlyNameProvided() {
                        return true;
                    }

                    public String getName() {
                        return name;
                    }
                };
            } catch (RemoteDeviceDidNotRespondException e) {
                device.logCommError(e);
            } catch (RemoteMessagingException e) {
                device.logCommError(e);
            } catch (RemoteUnreachableException e) {
                device.logCommError(e);
            }
        }
        // SAMPLE ISOLATION
        else if (event.getClass().equals(SampleIsolationRequest.class)) {
            final SampleIsolationRequest sie = (SampleIsolationRequest) event;
            return new IsolatedSample() {
                private final String name = sie.getName();
                private SampleDownloadDescriptor sdd = sie.getSampleDownloadDescriptor();//new Impl_SampleDownloadDescriptor(sie.getIndex(), sie.getType());
                private File localFile;

                {
                    if (sdd.getIndex().intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                        localFile = sdd.getFile();
                }

                public String getName() {
                    return name;
                }

                public Integer getOriginalIndex() {
                    return sdd.getIndex();
                }

                public boolean isROMSample() {
                    return sdd.getIndex().intValue() >= DeviceContext.BASE_ROM_SAMPLE;
                }

                public File getLocalFile() {
                    return localFile;
                }

                public AudioFileFormat.Type getFormatType() {
                    return sdd.getFormat();
                }

                public void assertSample(ProgressCallback prog) throws IsolatedSampleUnavailableException {
                    try {
                        if (sdd.getIndex().intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                            throw new IsolatedSampleUnavailableException("rom sample");
                        if (localFile == null && (localFile = sdd.getFile()) == null)
                            throw new IsolatedSampleUnavailableException("filename not specified");
                        if (!localFile.exists())
                            try {
                                sampleMediator.retrieveSample(sdd, prog);
                            } catch (Exception e) {
                                throw new IsolatedSampleUnavailableException(e.getMessage());
                            }
                    } finally {
                        prog.updateProgress(1);
                    }
                }

                public void setLocalFile(File f, boolean moveExisting) {
                    if (localFile != null && moveExisting)
                        localFile.renameTo(f);
                    localFile = f;
                    sdd = SampleDownloadDescriptorFactory.getDownloadToSpecificFile(sdd.getIndex(), f, sdd.getFormat(), sdd.isOverwriting());
                }

                public void zDispose() {
                    if (localFile != null)
                        localFile.delete();
                }
            };
        }
        return null;
    }

    public void setEventHandler(ManageableContentEventHandler ceh) {
        this.ceh = ceh;
    }

    public void setRemote(Remotable r) {
        remote = r;
        sampleMediator = RemoteFactory.createSampleMediator(remote);
    }
}
