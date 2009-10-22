package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.device.EMU.E4.Impl_SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.SampleDownloadDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.SampleDownloadDescriptorFactory;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.smdi.*;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.audio.AudioConversionException;
import com.pcmsolutions.system.audio.AudioConverter;
import com.pcmsolutions.system.audio.ZAudioSystem;
import com.pcmsolutions.system.preferences.ZEnumPref;
import com.pcmsolutions.system.preferences.ZIntPref;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 11:38:03
 */
class Impl_SampleMediator implements SampleMediator {
    Remotable remote;
    ZEnumPref packetSizePref;
    ZIntPref maxSampleRatePref;

    public Impl_SampleMediator(Remotable remote, ZEnumPref packetSizePref, ZIntPref maxSampleRatePref) {
        this.remote = remote;
        this.packetSizePref = packetSizePref;
        this.maxSampleRatePref = maxSampleRatePref;
    }

    public File retrieveSample(final SampleDownloadDescriptor sri, final ProgressCallback prog) throws SampleMediator.SampleMediationException, DeviceException {
        if (sri.getFile().exists() && !sri.isOverwriting())
            return null;
        try {
            prog.updateLabel("Retrieving sample " + ZUtilities.formatIndex(sri.getIndex()));
            prog.updateProgress(0);
            String excStr;
            File fn = sri.getFile();
           // synchronized (remote) {
                try {
                    if (sri.getFormat().equals(AudioFileFormat.Type.AIFF) || sri.getFormat().equals(AudioFileFormat.Type.AIFC)) {
                        SMDIAgent.SampleInputStreamHandler sih = new SMDIAgent.SampleInputStreamHandler() {
                            public void handleStream(AudioInputStream ais) throws Exception {
                                ais = AudioConverter.convertStreamToAiffCompatible(ais);
                                ZAudioSystem.write(ais, sri.getFormat(), sri.getFile());
                            }
                        };
                        remote.getSmdiContext().recvSync(sih, sri.getFormat(), sri.getIndex().intValue(), Integer.valueOf(packetSizePref.getValue()).intValue(), prog);

                    } else if (sri.getFormat().equals(AudioFileFormat.Type.WAVE)) {
                        SMDIAgent.SampleInputStreamHandler sih = new SMDIAgent.SampleInputStreamHandler() {
                            public void handleStream(AudioInputStream ais) throws Exception {
                                ais = AudioConverter.convertStreamToWaveCompatible(ais);
                                ZAudioSystem.write(ais, sri.getFormat(), sri.getFile());
                            }
                        };
                        remote.getSmdiContext().recvSync(sih, sri.getFormat(), sri.getIndex().intValue(), Integer.valueOf(packetSizePref.getValue()).intValue(), prog);
                    } else {
                        SMDIAgent.SampleInputStreamHandler sih = new SMDIAgent.SampleInputStreamHandler() {
                            public void handleStream(AudioInputStream ais) throws Exception {
                                ZAudioSystem.write(ais, sri.getFormat(), sri.getFile());
                            }
                        };
                        remote.getSmdiContext().recvSync(sih, sri.getFormat(), sri.getIndex().intValue(), Integer.valueOf(packetSizePref.getValue()).intValue(), prog);
                    }
                    return fn;
                } catch (SmdiOutOfRangeException e) {
                    excStr = e.getMessage();
                } catch (SmdiGeneralException e) {
                    excStr = e.getMessage();
                } catch (TargetNotSMDIException e) {
                    excStr = e.getMessage();
                } catch (SmdiSampleEmptyException e) {
                    excStr = e.getMessage();
                } catch (SmdiNoMemoryException e) {
                    excStr = e.getMessage();
                } catch (SmdiUnavailableException e) {
                    excStr = e.getMessage();
                } finally {
                }
          //  }
            throw new SampleMediator.SampleMediationException(excStr);
        } finally {
            prog.updateProgress(1);
        }
    }

    public void sendSampleMulti(File fn, Integer[] destSamples, String[] destNames, ProgressCallback prog) throws SampleMediator.SampleMediationException, SmdiSampleEmptyException, SmdiTransferAbortedException, DeviceException {
        try {
            prog.updateProgress(0);
            prog.updateLabel("Sending local file " + fn.getName());
            final ProgressCallback[] progs = prog.splitTask(destSamples.length, true);
            for (int i = 0; i < destSamples.length; i++)
                sendSample(destSamples[i], fn, destNames[i], true, progs[i]);
        } finally {
            prog.updateProgress(1);
        }
    }

    public void sendSample(IsolatedSample is, Integer[] destSamples, String[] destNames, ProgressCallback prog) throws SampleMediator.SampleMediationException, IsolatedSampleUnavailableException, SmdiSampleEmptyException, SmdiTransferAbortedException, DeviceException {
        try {
            prog.updateProgress(0);
            final ProgressCallback[] progs = prog.splitTask(destSamples.length + 1, true);
            is.assertSample(progs[0]);
            for (int i = 0; i < destSamples.length; i++)
                sendSample(destSamples[i], is.getLocalFile(), destNames[i], true, progs[i + 1]);
        } finally {
            prog.updateProgress(1);
        }
    }

    public void copySample(Integer srcSample, Integer[] destSamples, String[] destNames, ProgressCallback prog) throws SampleMediator.SampleMediationException, SmdiSampleEmptyException, SmdiTransferAbortedException, DeviceException {
        final SampleDownloadDescriptor sri = SampleDownloadDescriptorFactory.getDownloadToTempFile(srcSample);
        prog.updateProgress(0);
        final ProgressCallback[] progs = prog.splitTask(destSamples.length + 1, true);
        try {
            retrieveSample(sri, progs[0]);
            for (int i = 0; i < destSamples.length; i++)
                if (srcSample.intValue() != destSamples[i].intValue())
                    sendSample(destSamples[i], sri.getFile(), destNames[i], false, progs[i + 1]);
                else
                    progs[i + 1].updateProgress(1);
        } finally {
            prog.updateProgress(1);
        }
    }

    public void sendSample(Integer sample, IsolatedSample is, String sampleName, ProgressCallback prog) throws SampleMediator.SampleMediationException, IsolatedSampleUnavailableException, SmdiTransferAbortedException, DeviceException {
        sendSample(sample, is.getLocalFile(), sampleName, true, prog);        
    }

    public void sendSample(Integer sample, File fn, String sampleName, boolean obeyMaxSampleRate, ProgressCallback prog) throws SampleMediator.SampleMediationException, SmdiTransferAbortedException, DeviceException {
        try {
            prog.updateLabel("Sending " + fn.getName() + " to " + new ContextLocation(sample, sampleName));
            prog.updateProgress(0);
            if (fn != null) {
                try {
                    AudioInputStream ais = SendConverter.prepareAudioStream(ZAudioSystem.getAudioInputStream(fn), (obeyMaxSampleRate ? (float) maxSampleRatePref.getValue() : 48000.0F));
                    try {
                       // synchronized (remote) {
                            remote.getSmdiContext().sendSync(ais, sample.intValue(), sampleName, Integer.valueOf(packetSizePref.getValue()).intValue(), prog);
                       // }
                    } finally {
                        ais.close();
                    }
                    System.out.println(fn.getAbsolutePath() + " --> " + sample.intValue());
                    return;
                } catch (IOException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (UnsupportedAudioFileException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (SmdiFileOpenException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (SmdiOutOfRangeException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (SmdiGeneralException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (TargetNotSMDIException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (SmdiUnknownFileFormatException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (SmdiUnsupportedSampleBitsException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (SmdiNoMemoryException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (SmdiUnsupportedConversionException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (SmdiSampleEmptyException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (AudioConversionException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } catch (SmdiUnavailableException e) {
                    throw new SampleMediator.SampleMediationException(e.getMessage());
                } finally {
                }
            }
        } finally {
            prog.updateProgress(1);
        }
    }

    public SampleDescriptor getSampleDescriptor(Integer sample) throws SampleMediator.SampleMediationException, SmdiSampleEmptyException {
        String excStr;
        try {
           // synchronized (remote) {
                return new Impl_SampleDescriptor(remote.getSmdiContext().getSampleHeader(sample.intValue()));
           // }
        } catch (SmdiOutOfRangeException e) {
            excStr = e.getMessage();
        } catch (SmdiGeneralException e) {
            excStr = e.getMessage();
        } catch (TargetNotSMDIException e) {
            excStr = e.getMessage();
        } catch (SmdiNoMemoryException e) {
            excStr = e.getMessage();
        } catch (SmdiUnavailableException e) {
            excStr = e.getMessage();
        }
        throw new SampleMediator.SampleMediationException(excStr);
    }
}

