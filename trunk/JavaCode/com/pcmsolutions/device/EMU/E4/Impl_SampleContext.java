package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.TempFileManager;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 14:28:02
 */
class Impl_SampleContext implements SampleContext, RemoteObjectStates, Serializable {

    protected String name;

    protected transient Vector listeners = new Vector();
    protected SampleDatabaseProxy sampleDatabaseProxy;
    protected SampleMediator sampleMediator;
    protected DeviceContext device;

    public Impl_SampleContext(DeviceContext device, String name, SampleDatabaseProxy sdbp, SampleMediator sm) {
        this.device = device;
        this.name = name;
        this.sampleDatabaseProxy = sdbp;
        this.sampleMediator = sm;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        listeners = new Vector();
    }

    public boolean equals(Object o) {
        // identity comparison
        if (o == this)
            return true;
        return false;
    }

    public void setDevice(DeviceContext device) {
        this.device = device;
    }

    public String toString() {
        return "Samples";
    }

    public void addSampleContextListener(SampleContextListener pl) {
        listeners.add(pl);
    }

    public void removeSampleContextListener(SampleContextListener pl) {
        listeners.remove(pl);

    }

    public void addSampleListener(SampleListener pl, Integer[] samples) {
        sampleDatabaseProxy.addSampleListener(pl, samples);
    }

    public void removeSampleListener(SampleListener pl, Integer[] samples) {
        sampleDatabaseProxy.removeSampleListener(pl, samples);
    }

    private void fireSamplesAddedToContext(final Integer[] samples) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
                        try {
                            ((SampleContextListener) e.nextElement()).samplesAddedToContext(Impl_SampleContext.this, samples);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void fireSamplesRemovedFromContext(final Integer[] samples) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
                        try {
                            ((SampleContextListener) e.nextElement()).samplesRemovedFromContext(Impl_SampleContext.this, samples);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void fireContextReleased() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
                        try {
                            ((SampleContextListener) e.nextElement()).contextReleased(Impl_SampleContext.this);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public boolean isSampleInitialized(Integer sample) throws NoSuchSampleException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.isSampleInitialized(sample);
        } finally {
            reader.release();
        }
    }

    public int getSampleState(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.getSampleState(this, sample);
        } finally {
            reader.release();
        }
    }

    public String getSampleSummary(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.tryGetSampleSummary(this, sample);
        } finally {
            reader.release();
        }
    }

    public boolean isSampleWriteLocked(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.isSampleWriteLocked(this, sample);
        } finally {
            reader.release();
        }
    }

    public void copySample(Integer srcSample, Integer[] destSamples) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException, IsolatedSampleUnavailableException {
        if (isSampleEmpty(srcSample))
            throw new SampleEmptyException(srcSample);

        //IsolatedSample is = this.getIsolatedSample(srcSample);
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            for (int i = 0; i < destSamples.length; i++) {
                reader.lockSampleWrite(this, destSamples[i]);
                try {
                    //File f = sampleMediator.retrieveSample(new Impl_SampleRetrievalInfo(srcSample));
                    sampleMediator.copySample(srcSample, new Integer[]{destSamples[i]}, new String[]{getSampleName(srcSample)});
                } finally {
                    refreshSample(destSamples[i]);
                    reader.unlockSample(destSamples[i]);
                }
            }
        } catch (SampleMediator.SampleMediationException e) {
            throw new IsolatedSampleUnavailableException(e.getMessage());
        } finally {
            reader.release();
        }
    }

    public void eraseSample(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.changeSampleObject(this, sample, EmptySample.getInstance());
        } finally {
            reader.release();
        }
    }

    public String getSampleName(Integer sample) throws NoSuchSampleException, SampleEmptyException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            String name = reader.getSampleName(sample);
            if (name == DeviceContext.EMPTY_PRESET)
                throw new SampleEmptyException(sample);
            else
                return name;
        } finally {
            reader.release();
        }
    }

    public boolean isSampleWritable(Integer sample) {
        return isSampleInContext(sample);
    }

    public void release() throws NoSuchContextException {
        SDBWriter writer = sampleDatabaseProxy.getDBWrite();
        try {
            writer.releaseContext(this);
        } finally {
            writer.release();
        }
        fireContextReleased();
    }


    private File makeSampleLocalFile(Integer sample) {
        return new File(Zoeos.getHomeDir(), device.getDeviceLocalDir().getPath() + File.separator + AudioUtilities.makeLocalSampleName(sample, AudioUtilities.SAMPLE_NAMING_MODE_SI));
    }

    public boolean hasLocalCopy(Integer sample) throws NoSuchContextException, NoSuchSampleException, SampleEmptyException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.getSampleRead(this, sample);
            try {
                return makeSampleLocalFile(sample).exists();
            } finally {
                reader.unlockSample(sample);
            }

        } finally {
            reader.release();
        }
    }

    public File retrieveLocalCopy(Integer sample, boolean overwrite) throws NoSuchContextException, NoSuchSampleException, SampleEmptyException, SampleRetrievalException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.getSampleRead(this, sample);
            try {
                File sf = makeSampleLocalFile(sample);
                // return sampleMediator.retrieveSample(sample, sf, AudioUtilities.defaultAudioFormat, overwrite);
                return sampleMediator.retrieveSample(new Impl_SampleRetrievalInfo(sample, getSampleName(sample), sf, AudioUtilities.defaultAudioFormat, null, true, true));
            } catch (SampleMediator.SampleMediationException e) {
                throw new SampleRetrievalException(e.getMessage());
            } finally {
                reader.unlockSample(sample);
            }

        } finally {
            reader.release();
        }
    }

    public void eraseLocalCopy(Integer sample) throws NoSuchContextException, NoSuchSampleException, SampleEmptyException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.getSampleRead(this, sample);
            try {
                new File(Zoeos.getHomeDir(), device.getDeviceLocalDir().getPath() + File.separator + AudioUtilities.makeLocalSampleName(sample, AudioUtilities.SAMPLE_NAMING_MODE_SI)).delete();
            } finally {
                reader.unlockSample(sample);
            }

        } finally {
            reader.release();
        }
    }

    public com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor getLocalCopyHeader(Integer sample) {
        return null;
    }

    public File retrieveCustomLocalCopy(SampleRetrievalInfo sri) throws NoSuchContextException, NoSuchSampleException, SampleEmptyException, SampleRetrievalException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.getSampleRead(this, sri.getSample());
            try {
                //sf = new File(f, AudioUtilities.makeLocalSampleName(sample, reader.getSampleName(sample), namingMode));
                // return sampleMediator.retrieveSample(sample, sf, format, overwrite, endOfProcedure);
                return sampleMediator.retrieveSample(sri);
            } catch (SampleMediator.SampleMediationException e) {
                throw new SampleRetrievalException(e.getMessage());
            } finally {
                reader.unlockSample(sri.getSample());
            }

        } finally {
            reader.release();
        }
    }

    // PRESET
    // value between 0 and 1 representing fraction of dump completed
    // value < 0 means no dump in progress
    public double getInitializationStatus(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.getInitializationStatus(this, sample);
        } finally {
            reader.release();
        }
    }

    public void lockSampleRead(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.lockSampleRead(this, sample);
        } finally {
            reader.release();
        }
    }

    public void lockSampleWrite(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.lockSampleWrite(this, sample);
        } finally {
            reader.release();
        }
    }

    public void unlockSample(Integer sample) {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.unlockSample(sample);
        } finally {
            reader.release();
        }
    }

    public SampleContext newContext(String name, Integer[] samples) throws NoSuchSampleException, NoSuchContextException {
        SDBWriter writer = sampleDatabaseProxy.getDBWrite();
        SampleContext rv;
        try {
            rv = writer.newContext(this, name, samples);
        } finally {
            writer.release();
        }
        fireSamplesRemovedFromContext(samples);
        return rv;
    }

    public void assertSampleInitialized(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        if (!isSampleInitialized(sample))
            refreshSample(sample);
    }

    public void assertSampleNamed(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.assertSampleNamed(this, sample);
        } finally {
            reader.release();
        }
    }

    public void expandContext(SampleContext dpc, Integer[] samples) throws NoSuchContextException, NoSuchSampleException {
        if (dpc == this)
            throw new NoSuchContextException("Can't expand self");
        if (!(dpc instanceof Impl_SampleContext))
            throw new NoSuchContextException("Destination context is not compatible");
        Impl_SampleContext real_dpc = (Impl_SampleContext) dpc;
        removeFromContext(samples);
        real_dpc.addToContext(samples);
    }

    private void addToContext(Integer[] samples) throws NoSuchContextException, NoSuchSampleException {
        SDBWriter writer = sampleDatabaseProxy.getDBWrite();
        try {
            writer.addSamplesToContext(this, samples);
        } finally {
            writer.release();
        }
        fireSamplesAddedToContext(samples);
    }

    private void removeFromContext(Integer[] samples) throws NoSuchContextException, NoSuchSampleException {
        SDBWriter writer = sampleDatabaseProxy.getDBWrite();
        try {
            writer.removeSamplesFromContext(this, samples);
        } finally {
            writer.release();
        }
        fireSamplesRemovedFromContext(samples);
    }

    public List expandContextWithEmptySamples(SampleContext dpc, Integer reqd) throws NoSuchContextException {
        List removed;
        if (dpc == this)
            throw new NoSuchContextException();
        SDBWriter writer = sampleDatabaseProxy.getDBWrite();
        try {
            removed = writer.expandContextWithEmptySamples(this, dpc, reqd);
        } finally {
            writer.release();
        }
        for (int n = 0; n < removed.size(); n++)
            fireSamplesRemovedFromContext((Integer[]) removed.toArray());

        return removed;
    }

    public List findEmptySamplesInContext(int reqd) throws NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.findEmptySamples(this, reqd);
        } finally {
            reader.release();
        }
    }

    // looks for empties on or after beginIndex
    public List findEmptySamplesInContext(int reqd, Integer beginIndex, Integer maxIndex) throws NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.findEmptySamples(this, reqd, beginIndex, maxIndex);
        } finally {
            reader.release();
        }
    }

    public Integer firstEmptySampleInContext() throws NoSuchContextException, NoSuchSampleException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            Set s = reader.getSampleIndexesInContext(this);
            for (Iterator i = s.iterator(); i.hasNext();) {
                Integer index = (Integer) i.next();
                if (index.intValue() > 0 && index.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                    try {
                        if (isSampleEmpty(index))
                            return index;
                    } catch (NoSuchSampleException e) {
                    }
            }
        } finally {
            reader.release();
        }
        throw new NoSuchSampleException(IntPool.get(Integer.MIN_VALUE));
    }

    public Integer firstEmptySampleInDatabaseRange(Integer lowSample, Integer highSample) throws NoSuchContextException, NoSuchSampleException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            Set s = reader.getReadableSampleIndexes(this);
            int hi = highSample.intValue();
            int li = lowSample.intValue();
            if (hi < li)
                throw new IllegalArgumentException("lowSample is higher than highSample");
            for (int i = li; i < hi; i++) {
                Integer index = IntPool.get(i);
                if (s.contains(index))
                    if (index.intValue() > 0 && index.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                        try {
                            if (isSampleEmpty(index))
                                return index;
                        } catch (NoSuchSampleException e) {
                        }
            }
        } finally {
            reader.release();
        }
        throw new NoSuchSampleException(IntPool.get(Integer.MIN_VALUE));
    }

    public int numEmpties(Integer[] samples) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            int num = 0;
            for (int i = 0; i < samples.length; i++)
                if (reader.getSampleNameExtended(this, samples[i]).equals(DeviceContext.EMPTY_SAMPLE))
                    num++;
            return num;
        } finally {
            reader.release();
        }
    }

    public int numEmpties(Integer lowSample, int num) throws NoSuchSampleException, NoSuchContextException {
        Integer[] samples = new Integer[num];

        ZUtilities.fillIncrementally(samples, lowSample.intValue());

        return numEmpties(samples);
    }

    public Set getSampleIndexesInContext() throws NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.getSampleIndexesInContext(this);
        } finally {
            reader.release();
        }
    }

    private Object getMostCapableSampleObject(Integer sample) throws NoSuchSampleException {
        SampleModel impl = getSampleImplementation(sample);
        try {
            impl = SampleClassManager.getMostDerivedSampleInstance(impl, getSampleName(sample));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SampleEmptyException e) {
        }
        return impl;
    }

    // returns List of ContextReadableSample/ReadableSample ( e.g FLASH/ROM samples returned as ReadableSample)
    public List getContextSamples() throws NoSuchContextException {
        ArrayList outList = new ArrayList();
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            Set indexes = getSampleIndexesInContext();
            Integer p;
            for (Iterator i = indexes.iterator(); i.hasNext();)
                try {
                    p = (Integer) i.next();
                    outList.add(getMostCapableSampleObject(p));
                } catch (NoSuchSampleException e) {
                }
            return outList;
        } finally {
            reader.release();
        }
    }

    // returns List of ReadableSample or better
    // e.g FLASH/ROM and out of context samples returned as ReadableSample
    // possibly more derived than ReadableSample if sample is in context etc... )
    public List getDatabaseSamples() throws NoSuchContextException {
        ArrayList outList = new ArrayList();
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            Set indexes = reader.getReadableSampleIndexes(this);
            for (Iterator i = indexes.iterator(); i.hasNext();)
                try {
                    outList.add(getMostCapableSampleObject((Integer) i.next()));
                } catch (NoSuchSampleException e) {
                }
            return outList;
        } finally {
            reader.release();
        }
    }

    // set of integers
    public Set getDatabaseIndexes() throws NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.getReadableSampleIndexes(this);
        } finally {
            reader.release();
        }
    }

    public Map getSampleNamesInContext() throws NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.getSampleNamesInContext(this);
        } finally {
            reader.release();
        }
    }

    public Map getUserSampleNamesInContext() throws NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.getUserSampleNamesInContext(this);
        } finally {
            reader.release();
        }
    }

    public boolean isSampleInContext(Integer sample) {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            return reader.hasSample(this, sample);
        } finally {
            reader.release();
        }
    }

    public int size() {
        try {
            return getSampleIndexesInContext().size();
        } catch (NoSuchContextException e) {
            return 0;
        }
    }

    public boolean isSampleEmpty(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        return getSampleState(sample) == RemoteObjectStates.STATE_EMPTY;
    }

    private Impl_ReadableSample getSampleImplementation(Integer sample) throws NoSuchSampleException {
        if (isSampleInContext(sample))
            if (sample.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                return new Impl_ContextEditableSample(this, sample);
            else
                return new Impl_ContextReadableSample(this, sample);
        else {
            SDBReader reader = sampleDatabaseProxy.getDBRead();
            try {
                if (reader.readsSample(this, sample))
                    return new Impl_ReadableSample(this, sample);
                else
                    throw new NoSuchSampleException(sample);
            } finally {
                reader.release();
            }
        }
    }

    /*private Impl_ContextReadableSample getContextSampleImplementation(Integer sample) throws NoSuchSampleException {
        if (isSampleInContext(sample))
            return new Impl_ContextReadableSample(this, sample);

        throw new NoSuchSampleException(sample);
    }

    private Impl_ReadableSample getReadableSampleImplementation(Integer sample) throws NoSuchSampleException {
        SDBReader reader = sdbp.getDBRead();
        try {
            if (reader.readsSample(this, sample))
                return new Impl_ReadableSample(this, sample);
            else
                throw new NoSuchSampleException(sample);
        } finally {
            reader.release();
        }
    } */

    public ContextReadableSample getContextSample(Integer sample) throws NoSuchSampleException {
        Object rv = getSampleImplementation(sample);
        if (rv instanceof ContextReadableSample)
            return (ContextReadableSample) rv;
        else
            throw new NoSuchSampleException(sample);
    }

    public ReadableSample getReadableSample(Integer sample) throws NoSuchSampleException {
        return getSampleImplementation(sample);
    }

    // TODO!! fix semantics of this to handle FLASH samples that cannot be returned as ContextEditableSample
    public ContextEditableSample getEditableSample(Integer sample) throws NoSuchSampleException {
        if (isSampleInContext(sample))
            return new Impl_ContextEditableSample(this, sample);

        throw new NoSuchSampleException(sample);
    }

    public IsolatedSample getIsolatedSample(final Integer sample, final AudioFileFormat.Type format) throws NoSuchSampleException, NoSuchContextException, SampleEmptyException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.getSampleRead(this, sample);
            try {
                return new IsolatedSample() {
                    private final String name = getSampleName(sample);
                    private SampleRetrievalInfo sri = new Impl_SampleRetrievalInfo(sample, format);
                    private File localFile = sri.getFile();
                    private AudioFileFormat.Type aFormat = format;

                    public String getName() {
                        return name;
                    }

                    public Integer getOriginalIndex() {
                        return sample;
                    }

                    public boolean isROMSample() {
                        return sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE;
                    }

                    /*    public AudioInputStream getAudioInputStream() throws IOException, UnsupportedAudioFileException {
                            if (localFile != null)
                                return AudioSystem.getAudioInputStream(localFile);
                            throw new IOException("no File");
                        }
                      */
                    public File getLocalFile() {
                        return localFile;
                    }

                    public AudioFileFormat.Type getFormatType() throws IOException, UnsupportedAudioFileException {
                        //  if ( localFile != null)
                        //    return AudioSystem.getAudioFileFormat(localFile);
                        //  else
                        return aFormat;
                    }

                    public void ZoeAssert() throws IsolatedSampleUnavailableException {
                        if (sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                            return;
                        if (localFile == null)
                            throw new IsolatedSampleUnavailableException("filename not specified");
                        if (!localFile.exists())
                            try {
                                // sampleMediator.retrieveSample(sample, localFile, format, true);
                                sampleMediator.retrieveSample(sri);
                            } catch (SampleMediator.SampleMediationException e) {
                                throw new IsolatedSampleUnavailableException(e.getMessage());
                            }
                    }

                    public void setLocalFile(File f, boolean moveExisting) {
                        if (localFile != null && moveExisting)
                            localFile.renameTo(f);
                        localFile = f;
                        sri = new Impl_SampleRetrievalInfo(sample, f, format, true);
                    }

                    public void zDispose() {
                        if (localFile != null)
                            localFile.delete();
                    }
                };
            } finally {
                reader.unlockSample(sample);
            }
        } finally {
            reader.release();
        }
    }

    public IsolatedSample getIsolatedSample(final Integer sample, final String fileName, final AudioFileFormat.Type format) throws NoSuchSampleException, NoSuchContextException, SampleEmptyException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.getSampleRead(this, sample);
            try {
                //return new Impl_IsolatedSample(this, sampleMediator, sample, fileName);
                return new IsolatedSample() {
                    private final String name = getSampleName(sample);
                    private SampleRetrievalInfo sri = new Impl_SampleRetrievalInfo(sample, new File(TempFileManager.getTempDirectory(), fileName), format, true);
                    private File localFile = sri.getFile();
                    private AudioFileFormat.Type aFormat = format;

                    {
                        if (sri.getFile().exists())
                            sri.getFile().delete();
                    }

                    public String getName() {
                        return name;
                    }

                    public Integer getOriginalIndex() {
                        return sample;
                    }

                    public boolean isROMSample() {
                        return sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE;
                    }

                    /* public AudioInputStream getAudioInputStream() throws IOException, UnsupportedAudioFileException {
                         if (localFile != null)
                             return AudioSystem.getAudioInputStream(localFile);
                         throw new IOException("no File");
                     }*/

                    public File getLocalFile() {
                        return localFile;
                    }

                    public AudioFileFormat.Type getFormatType() throws IOException, UnsupportedAudioFileException {
                        // return AudioSystem.getAudioFileFormat(localFile);
                        return aFormat;
                    }

                    public void ZoeAssert() throws IsolatedSampleUnavailableException {
                        if (sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                            return;
                        if (localFile == null)
                            throw new IsolatedSampleUnavailableException("filename not specified");
                        if (!localFile.exists())
                            try {
                                //sampleMediator.retrieveSample(sample, localFile, format, true);
                                sampleMediator.retrieveSample(sri);
                            } catch (SampleMediator.SampleMediationException e) {
                                throw new IsolatedSampleUnavailableException(e.getMessage());
                            }
                    }

                    public void setLocalFile(File f, boolean moveExisting) {
                        if (localFile != null && moveExisting)
                            localFile.renameTo(f);
                        localFile = f;
                    }

                    public void zDispose() {
                        if (localFile != null)
                            localFile.delete();
                    }
                };
            } finally {
                reader.unlockSample(sample);
            }
        } finally {
            reader.release();
        }
    }

    public IsolatedSample getIsolatedSample(final Integer sample, final File file, final AudioFileFormat.Type format) throws NoSuchSampleException, NoSuchContextException, SampleEmptyException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.getSampleRead(this, sample);
            try {
                // return new Impl_IsolatedSample(this, sampleMediator, sample, File);
                return new IsolatedSample() {
                    private final String name = getSampleName(sample);
                    //private SampleRetrievalInfo sri = new Impl_SampleRetrievalInfo(sample, File, format, true);
                    private File localFile = file;
                    private AudioFileFormat.Type aFormat = format;

                    public String getName() {
                        return name;
                    }

                    public Integer getOriginalIndex() {
                        return sample;
                    }

                    public boolean isROMSample() {
                        return sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE;
                    }

                    /*public AudioInputStream getAudioInputStream() throws IOException, UnsupportedAudioFileException {
                        if (File != null)
                            return AudioSystem.getAudioInputStream(File);
                        throw new IOException("no File");
                    } */

                    public File getLocalFile() {
                        return localFile;
                    }

                    public AudioFileFormat.Type getFormatType() throws IOException, UnsupportedAudioFileException {
                        //return AudioSystem.getAudioFileFormat(localFile);
                        return aFormat;
                    }

                    public void ZoeAssert() throws IsolatedSampleUnavailableException {
                        if (sample.intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                            return;
                        if (localFile == null)
                            throw new IsolatedSampleUnavailableException("filename not specified");
                        if (!localFile.exists())
                            try {
                                // sampleMediator.retrieveSample(sample, localFile, format, true);
                                sampleMediator.retrieveSample(new Impl_SampleRetrievalInfo(sample, localFile, format, true));
                            } catch (SampleMediator.SampleMediationException e) {
                                throw new IsolatedSampleUnavailableException(e.getMessage());
                            }
                    }

                    public void setLocalFile(File f, boolean moveExisting) {
                        if (localFile != null && moveExisting)
                            localFile.renameTo(f);
                        localFile = f;
                    }

                    public void zDispose() {
                        if (localFile != null)
                            localFile.delete();
                    }
                };
            } finally {
                reader.unlockSample(sample);
            }
        } finally {
            reader.release();
        }
    }

    public void newSample(IsolatedSample is, Integer sample, String name) throws NoSuchContextException, NoSuchSampleException, IsolatedSampleUnavailableException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.lockSampleWrite(this, sample);
            try {
                sampleMediator.sendSample(sample, is, name);
                refreshSample(sample);
            } catch (SampleMediator.SampleMediationException e) {
                reader.changeSampleObject(this, sample, new UninitSampleObject(sample));
                device.logCommError(e);
                throw new IsolatedSampleUnavailableException(e.getMessage());
            } finally {
                reader.unlockSample(sample);
            }
        } finally {
            reader.release();
        }
    }

    public void refreshSample(Integer sample) throws NoSuchSampleException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.refreshSample(this, sample);
        } finally {
            reader.release();
        }

    }

    public void setSampleName(Integer sample, String name) throws NoSuchSampleException, SampleEmptyException, NoSuchContextException {
        SDBReader reader = sampleDatabaseProxy.getDBRead();
        try {
            reader.setSampleName(this, sample, name);
        } finally {
            reader.release();
        }
    }

    public String getDeviceString() {
        return "No Device - SampleContext is offline.";
    }

    public DeviceParameterContext getDeviceParameterContext() {
        return device.getDeviceParameterContext();
    }

    public DeviceContext getDeviceContext() {
        return device;
    }

    public PresetContext getRootPresetContext() {
        return sampleDatabaseProxy.getRootPresetContext();
    }
}
