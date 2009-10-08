package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.AggRemoteName;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.gui.ProgressMultiBox;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.util.ClassUtility;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 11-Oct-2003
 * Time: 09:44:29
 * To change this template use Options | File Templates.
 */
public class SampleContextMacros {
    public static ReadableSample[] getContextUserSamples(SampleContext sc) throws NoSuchContextException {
        List l = sc.getContextSamples();
        List ul = new ArrayList();
        for (int i = 0; i < l.size(); i++) {
            if (((ReadableSample) l.get(i)).getSampleNumber().intValue() > 0 && ((ReadableSample) l.get(i)).getSampleNumber().intValue() <= DeviceContext.MAX_USER_SAMPLE)
                ul.add(l.get(i));
        }
        return (ReadableSample[]) ul.toArray(new ReadableSample[ul.size()]);
    }

    public static IsolatedSample[] unmatchedRomLocations(IsolatedSample[] samples, SampleContext sc) throws NoSuchSampleException {
        return unmatchedLocations(samples, sc, DeviceContext.BASE_ROM_SAMPLE);
    }

    public static IsolatedSample[] unmatchedLocations(IsolatedSample[] samples, SampleContext sc, int baseIndex) throws NoSuchSampleException {
        ArrayList ul = new ArrayList();
        for (int i = 0; i < samples.length; i++) {
            if (samples[i].getOriginalIndex().intValue() >= baseIndex)
                try {
                    if (!samples[i].getName().equals(sc.getSampleName(samples[i].getOriginalIndex())))
                        ul.add(samples[i]);
                } catch (SampleEmptyException e) {
                    ul.add(samples[i]);
                }
        }
        return (IsolatedSample[]) ul.toArray(new IsolatedSample[ul.size()]);
    }

    public static boolean areSampleIndexesEmpty(SampleContext sc, Integer lowSample, int count) throws NoSuchSampleException, NoSuchContextException {
        for (int i = 0; i < count; i++)
            if (!sc.isSampleEmpty(IntPool.get(lowSample.intValue() + i)))
                return false;
        return true;
    }

    public static boolean areSampleIndexesEmpty(SampleContext sc, Integer[] samples) throws NoSuchSampleException, NoSuchContextException {
        for (int i = 0; i < samples.length; i++)
            if (!sc.isSampleEmpty(samples[i]))
                return false;
        return true;
    }

    public static void loadSamplesToContext(final File[] files, final Integer[] indexes, final SampleContext sc, boolean showProgress, final boolean stripIndex) throws CommandFailedException {
        if (!ZUtilities.filesExist(files)) {
            int nec = files.length - ZUtilities.howManyFilesExist(files);
            if (nec == files.length)
                throw new CommandFailedException("All the sample files are missing.");
            else if (nec == 1)
                throw new CommandFailedException("One of the sample files is missing.");
            else
                throw new CommandFailedException(nec + " of the sample files are missing.");
        }

        DecimalFormat df = new DecimalFormat("0000");
        Zoeos z = Zoeos.getInstance();
        final Object pobj = new Object();
        if (showProgress)
            z.beginProgressElement(pobj, ZUtilities.makeExactLengthString("Loading samples...", 88), files.length);
        try {
            for (int i = 0; i < files.length; i++) {
                if (showProgress) {
                    z.setProgressElementIndeterminate(pobj, false);
                    z.updateProgressElementTitle(pobj, "Loading sample \"" + files[i].getName() + "\" to " + df.format(indexes[i]));
                    z.setProgressElementIndeterminate(pobj, true);
                }
                final int f_i = i;
                final String name = (stripIndex ? ZUtilities.removeFirstPattern(ZUtilities.stripExtension(files[f_i].getName()), AudioUtilities.sampleIndexPattern) :
                        ZUtilities.stripExtension(files[f_i].getName()));
                try {
                    sc.newSample(new IsolatedSample() {
                        public String getName() {
                            return name;
                        }

                        public Integer getOriginalIndex() {
                            return IntPool.get(Integer.MIN_VALUE);
                        }

                        public boolean isROMSample() {
                            return false;
                        }

                        public File getLocalFile() {
                            return files[f_i];
                        }

                        public AudioFileFormat.Type getFormatType() throws IOException, UnsupportedAudioFileException {
                            return AudioSystem.getAudioFileFormat(files[f_i]).getType();
                        }

                        public void ZoeAssert() throws IsolatedSampleUnavailableException {
                            if (!files[f_i].exists())
                                throw new IsolatedSampleUnavailableException("File doesn't exist");
                        }

                        public void setLocalFile(File f, boolean moveExisting) {
                        }

                        /* public SampleDescriptor getSampleDescriptor() throws IsolatedSampleUnavailableException {
                            try {
                                return new Impl_SampleDescriptor(SMDIAgent.getSampleHeader(files[f_i].getAbsolutePath()));
                            } catch (SmdiFileOpenException e) {
                                throw new IsolatedSampleUnavailableException(e.getMessage());
                            } catch (SmdiUnknownFileFormatException e) {
                                throw new IsolatedSampleUnavailableException(e.getMessage());
                            } catch (SmdiGeneralException e) {
                                throw new IsolatedSampleUnavailableException(e.getMessage());
                            } catch (SmdiUnavailableException e) {
                                throw new IsolatedSampleUnavailableException(e.getMessage());
                            }
                        }*/

                        public void zDispose() {
                        }

                    }, indexes[i], name);
                } catch (NoSuchContextException e) {
                    throw new CommandFailedException("Problem with sample context");
                } catch (NoSuchSampleException e) {
                    throw new CommandFailedException("Could not find sample location " + indexes[i]);
                } catch (IsolatedSampleUnavailableException e) {
                    throw new CommandFailedException(e.getMessage());
                } finally {
                    if (showProgress)
                        z.updateProgressElement(pobj);
                }
            }
        } finally {
            if (showProgress) {
                z.updateProgressElement(pobj, ProgressMultiBox.PROGRESS_DONE_TITLE);
                z.endProgressElement(pobj);
            }
        }
    }

    public static String getOverwriteConfirmationString(SampleContext sc, Integer destIndex, int num) {
        Integer[] indexes = new Integer[num];
        ZUtilities.fillIncrementally(indexes, destIndex.intValue());
        return getOverwriteConfirmationString(sc, indexes);
    }

    public static String getOverwriteConfirmationString(SampleContext sc, Integer[] indexes) {
        int numEmpty = 0;
        int num = indexes.length;
        try {
            numEmpty = sc.numEmpties(indexes);
        } catch (NoSuchSampleException e) {
        } catch (NoSuchContextException e) {
        }
        if (num != 1) {
            if (num == numEmpty)
                return "OK to overwrite " + num + " empty sample locations ?";
            else
                return "OK to overwrite " + num + " sample locations (" + (num - numEmpty) + " non-empty) ?";
        } else if (numEmpty == 1)
            return "OK to overwrite empty sample location ?";
        else {
            String name = null;
            try {
                name = new AggRemoteName(indexes[0], sc.getSampleName(indexes[0])).toString();
                return "OK to overwrite " + name + " ?";
            } catch (NoSuchSampleException e) {
                e.printStackTrace();
            } catch (SampleEmptyException e) {
                e.printStackTrace();
            }
            return "OK to overwrite non-empty sample at location " + new DecimalFormat("0000").format(indexes[0]) + " ?";
        }
    }

    public static int copySamples(SampleContext sc, Integer[] srcSamples, Integer[] destSamples, boolean copyEmpties, final boolean provideFeedback, String feedbackTitle) throws NoSuchSampleException, NoSuchContextException, IsolatedSampleUnavailableException {
        int notCopied = 0;

        if (srcSamples.length != destSamples.length)
            throw new IllegalArgumentException("number of src/dest samples mismatch");

        Zoeos z = Zoeos.getInstance();
        final Object po = new Object();
        if (provideFeedback) {
            String confirmStr = SampleContextMacros.getOverwriteConfirmationString(sc, destSamples);
            if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), confirmStr, "Confirm " + feedbackTitle, JOptionPane.YES_NO_OPTION) != 0)
                return srcSamples.length;
            z.beginProgressElement(po, ZUtilities.makeExactLengthString("Sample Copy", 60), srcSamples.length);
        }

        Arrays.sort(srcSamples);
        Arrays.sort(destSamples);
        try {
            for (int i = 0; i < srcSamples.length; i++) {
                try {
                    if (provideFeedback)
                        z.setProgressElementIndeterminate(po, true);
                    sc.copySample(srcSamples[i], new Integer[]{destSamples[i]});
                } catch (SampleEmptyException e) {
                    // source is empty
                    if (copyEmpties)
                        try {
                            sc.eraseSample(destSamples[i]);
                        } catch (SampleEmptyException e1) {
                        }
                    else
                        notCopied++;
                } finally {
                    if (provideFeedback) {
                        z.setProgressElementIndeterminate(po, false);
                        z.updateProgressElement(po, "Copied " + getSampleDisplayName(sc, srcSamples[i]));
                    }
                }
            }
        } finally {
            if (provideFeedback)
                z.endProgressElement(po);
            try {
                sc.getDeviceContext().sampleMemoryDefrag(false);
            } catch (ZDeviceNotRunningException e) {
            } catch (RemoteUnreachableException e) {
            }
        }
        if (provideFeedback) {
            if (notCopied == srcSamples.length)
                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), (srcSamples.length > 1 ? "None of the source samples could be copied" : "The source sample could not be copied"), "Problem", JOptionPane.ERROR_MESSAGE);
            else if (notCopied > 0)
                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), notCopied + " of " + srcSamples.length + " source samples could not be copied", "Problem", JOptionPane.ERROR_MESSAGE);
        }
        return notCopied;
    }

    public static String getSampleDisplayName(SampleContext pc, Integer sample) {
        String name = "";
        try {
            name = new com.pcmsolutions.device.EMU.E4.preset.AggRemoteName(sample, pc.getSampleName(sample)).toString();
        } catch (NoSuchSampleException e) {
        } catch (SampleEmptyException e) {
            name = DeviceContext.EMPTY_SAMPLE;
        }
        return name;
    }

    public static boolean areAllSameContext(ReadableSample[] samples) {
        if (samples.length != 0) {
            SampleContext sc = samples[0].getSampleContext();
            for (int i = 1; i < samples.length; i++) {
                if (!samples[i].getSampleContext().equals(sc))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static ReadableSample[] extractReadableSamples(Object[] objects) {
        Object[] rp1 = ClassUtility.extractInstanceOf(objects, ReadableSample.class);
        ReadableSample[] rp2 = new ReadableSample[rp1.length];
        System.arraycopy(rp1, 0, rp2, 0, rp1.length);
        return rp2;
    }

    public static Integer[] extractSampleIndexes(ReadableSample[] samples) {
        Integer[] indexes = new Integer[samples.length];
        for (int i = 0; i < samples.length; i++)
            indexes[i] = samples[i].getSampleNumber();
        return indexes;
    }

    public static Integer[] extractUniqueSampleIndexes(ReadableSample[] samples) {
        Set s = new HashSet();
        for (int i = 0; i < samples.length; i++)
            s.add(samples[i].getSampleNumber());
        return (Integer[]) s.toArray(new Integer[s.size()]);
    }

    public static File[] extractFiles(IsolatedSample[] samples) {
        File[] files = new File[samples.length];
        for (int i = 0; i < samples.length; i++)
            files[i] = samples[i].getLocalFile();
        return files;
    }

    public static Integer[] extractIndexes(IsolatedSample[] samples) {
        Integer[] indexes = new Integer[samples.length];
        for (int i = 0; i < samples.length; i++)
            indexes[i] = samples[i].getOriginalIndex();
        return indexes;
    }
}