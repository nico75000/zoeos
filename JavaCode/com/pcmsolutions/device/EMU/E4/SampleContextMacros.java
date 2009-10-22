package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.gui.ProgressCallbackTree;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.callback.CompleteProgressOnCancelledCallback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.util.ClassUtility;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
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

    public static ReadableSample[] getFreshCopies(ReadableSample[] samples) throws DeviceException {
        TreeSet outSet = new TreeSet();
        for (int i = 0; i < samples.length; i++)
            outSet.add(samples[i].getSampleContext().getReadableSample(samples[i].getIndex()));
        return (ReadableSample[]) outSet.toArray(new ReadableSample[outSet.size()]);
    }

    public static ContextEditableSample[] getFreshCopies(ContextEditableSample[] samples) throws DeviceException {
        TreeSet outSet = new TreeSet();
        for (int i = 0; i < samples.length; i++)
            outSet.add(samples[i].getSampleContext().getEditableSample(samples[i].getIndex()));
        return (ContextEditableSample[]) outSet.toArray(new ContextEditableSample[outSet.size()]);
    }

    public static List<ReadableSample> findEmptySamples(ReadableSample[] samples, int searchIndex) {
        ArrayList<ReadableSample> empties = new ArrayList<ReadableSample>();
        for (int i = searchIndex; i < samples.length; i++)
            try {
                if (samples[i].isEmpty())
                    empties.add(samples[i]);
            } catch (SampleException e) {
                e.printStackTrace();
            }

        return empties;
    }

    public static int getTotalSizeInBytes(ContextEditableSample[] samples) {
        int tot = 0;
        for (int i = 0; i < samples.length; i++) {
            try {
                SampleDescriptor sd = samples[i].getSampleDescriptor();
                if (sd != null)
                    tot += sd.getSizeInBytes();
            } catch (EmptyException e) {
            } catch (SampleException e) {
            }
        }
        return tot;
    }

    public static int numEmpties(SampleContext sc, Integer[] indexes) throws DeviceException {
        return indexes.length - getNonEmpty(sc, indexes).length;
    }

    public static Integer[] getNonEmpty(SampleContext sc, Integer[] indexes) throws DeviceException {
        ArrayList ne = new ArrayList();
        for (int i = 0; i < indexes.length; i++)
            if (sc.isEmpty(indexes[i]))
                continue;
            else
                ne.add(indexes[i]);
        return (Integer[]) ne.toArray(new Integer[ne.size()]);
    }

    public static ReadableSample[] getNonEmpty(ReadableSample[] samples) throws SampleException {
        ArrayList<ReadableSample> ne = new ArrayList<ReadableSample>();
        for (int i = 0; i < samples.length; i++) {
            samples[i].assertInitialized();
            if (samples[i].isEmpty())
                continue;
            else
                ne.add(samples[i]);
        }
        return ne.toArray(new ReadableSample[ne.size()]);
    }

    public static IsolatedSample[] unmatchedRomLocations(IsolatedSample[] samples, SampleContext sc) {
        return unmatchedLocations(samples, sc, DeviceContext.BASE_ROM_SAMPLE);
    }

    public static IsolatedSample[] unmatchedLocations(IsolatedSample[] samples, SampleContext sc, int baseIndex) {
        ArrayList ul = new ArrayList();
        for (int i = 0; i < samples.length; i++) {
            if (samples[i].getOriginalIndex().intValue() >= baseIndex)
                try {
                    if (!samples[i].getName().equals(sc.getName(samples[i].getOriginalIndex())))
                        ul.add(samples[i]);
                } catch (EmptyException e) {
                    ul.add(samples[i]);
                } catch (DeviceException e) {
                    ul.add(samples[i]);
                } catch (ContentUnavailableException e) {
                    ul.add(samples[i]);
                }
        }
        return (IsolatedSample[]) ul.toArray(new IsolatedSample[ul.size()]);
    }

    public static Integer[] extractRomIndexes(Integer[] indexes) {
        ArrayList rom = new ArrayList();
        for (int i = 0; i < indexes.length; i++)
            if (indexes[i].intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                rom.add(indexes[i]);
        return (Integer[]) rom.toArray(new Integer[rom.size()]);
    }

    public static Integer[] extractUserIndexes(Integer[] indexes) {
        ArrayList rom = new ArrayList();
        for (int i = 0; i < indexes.length; i++)
            if (indexes[i].intValue() < DeviceContext.BASE_ROM_SAMPLE)
                rom.add(indexes[i]);
        return (Integer[]) rom.toArray(new Integer[rom.size()]);
    }

    public static int userSampleCount(IsolatedSample[] samples) {
        int count = 0;
        if (samples.length != 0)
            for (int i = 0; i < samples.length; i++)
                if (samples[i].getOriginalIndex().intValue() < DeviceContext.BASE_ROM_SAMPLE)
                    count++;
        return count;
    }

    public static ReadableSample[] getReadablePresets(SampleContext sc, Integer[] indexes) throws DeviceException {
        ReadableSample[] samples = new ReadableSample[indexes.length];
        for (int i = 0; i < indexes.length; i++)
            samples[i] = sc.getReadableSample(indexes[i]);
        return samples;
    }

    public static boolean areSampleIndexesEmpty(SampleContext sc, Integer lowSample, int count) throws DeviceException {
        for (int i = 0; i < count; i++)
            if (!sc.isEmpty(IntPool.get(lowSample.intValue() + i)))
                return false;
        return true;
    }

    public static boolean areSampleIndexesEmpty(SampleContext sc, Integer[] samples) throws DeviceException {
        for (int i = 0; i < samples.length; i++)
            if (!sc.isEmpty(samples[i]))
                return false;
        return true;
    }

    public static void loadSamplesToContext(final File[] files, final Integer[] indexes, final SampleContext sc, final ProgressCallback prog, final boolean stripIndex) throws CommandFailedException, DeviceException {
        if (!sc.getDeviceContext().isSmdiCoupled())
            throw new CommandFailedException("Cannot perform sample load - the device is not SMDI coupled.");
        if (!ZUtilities.filesExist(files)) {
            int nec = files.length - ZUtilities.howManyFilesExist(files);
            if (nec == files.length)
                throw new CommandFailedException("All the sample files are missing.");
            else if (nec == 1)
                throw new CommandFailedException("One of the sample files is missing.");
            else
                throw new CommandFailedException(nec + " of the sample files are missing.");
        }

        ProgressCallback[] progs = null;
        progs = prog.splitTask(files.length, false);
        for (int i = 0; i < files.length; i++) {
            if (prog.isCancelled()) {
                return;
            }
            final int f_i = i;
            final String name = (stripIndex ? ZUtilities.removeFirstPattern(ZUtilities.stripExtension(files[f_i].getName()), AudioUtilities.sampleIndexPattern) :
                    ZUtilities.stripExtension(files[f_i].getName()));
            try {
                sc.newContent(new IsolatedSample() {
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

                    public void assertSample(ProgressCallback prog) throws IsolatedSampleUnavailableException {
                        try {
                            if (!files[f_i].exists())
                                throw new IsolatedSampleUnavailableException("File doesn't exist");
                        } finally {
                            prog.updateProgress(1);
                        }
                    }

                    public void setLocalFile(File f, boolean moveExisting) {
                    }

                    public void zDispose() {
                    }

                }, indexes[i], name, progs[i]).post(new CompleteProgressOnCancelledCallback((i == files.length - 1 ? prog : progs[i])));
            } catch (Exception e) {
                prog.updateProgress(1);
                throw new CommandFailedException(e.getMessage());
            } finally {
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
        } catch (NoSuchContextException e) {
        } catch (DeviceException e) {
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
                name = new ContextLocation(indexes[0], sc.getName(indexes[0])).toString();
                return "OK to overwrite " + name + " ?";
            } catch (EmptyException e) {
                e.printStackTrace();
            } catch (DeviceException e) {
                e.printStackTrace();
            } catch (ContentUnavailableException e) {
                e.printStackTrace();
            }
            return "OK to overwrite non-empty sample at location " + new DecimalFormat("0000").format(indexes[0]) + " ?";
        }
    }

    public static int copySamples(SampleContext sc, Integer[] srcSamples, Integer[] destSamples, boolean copyEmpties, final boolean provideFeedback, String feedbackTitle) throws DeviceException {
        int notCopied = 0;

        if (srcSamples.length != destSamples.length)
            throw new IllegalArgumentException("number of src/dest samples mismatch");

        ProgressCallback prog = null;
        ProgressCallback[] progs = null;
        if (provideFeedback) {
            String confirmStr = SampleContextMacros.getOverwriteConfirmationString(sc, destSamples);
            if (confirmStr != null && !UserMessaging.askYesNo(confirmStr, "Confirm " + feedbackTitle))
                return srcSamples.length;
            prog = new ProgressCallbackTree("Sample block copy", false);
            progs = prog.splitTask(srcSamples.length, true);
        }

        Arrays.sort(srcSamples);
        Arrays.sort(destSamples);
        try {
            for (int i = 0; i < srcSamples.length; i++) {
                try {
                    if (sc.isEmpty(srcSamples[i])) {
                        if (copyEmpties)
                            try {
                                sc.erase(destSamples[i]).post();
                            } catch (ResourceUnavailableException e1) {
                                notCopied++;
                            }
                        else
                            notCopied++;
                    } else
                        try {
                            sc.copy(srcSamples[i], new Integer[]{destSamples[i]}, (provideFeedback ? progs[i] : ProgressCallback.DUMMY)).post(new CompleteProgressOnCancelledCallback((provideFeedback ? progs[i] : ProgressCallback.DUMMY)));
                        } catch (Exception e) {
                            if (provideFeedback)
                                progs[i].updateProgress(1);
                            notCopied++;
                        } finally {
                        }
                } catch (DeviceException e) {
                    prog.updateProgress(1);
                    throw e;
                }
            }
        } finally {
        }
        if (provideFeedback) {
            if (notCopied == srcSamples.length)
                UserMessaging.showError((srcSamples.length > 1 ? "None of the source samples could be copied" : "The source sample could not be copied"));
            else if (notCopied > 0)
                UserMessaging.showError(notCopied + " of " + srcSamples.length + " source samples could not be copied");
        }
        return notCopied;
    }

    public static void copySample(final SampleContext sc, Integer srcSample, Integer[] destSamples, boolean copyEmpty, final boolean provideFeedback, String feedbackTitle) throws DeviceException {
        if (provideFeedback) {
            String confirmStr = SampleContextMacros.getOverwriteConfirmationString(sc, destSamples);
            if (confirmStr != null && !UserMessaging.askYesNo(confirmStr, "Confirm " + feedbackTitle))
                return;
        }
        Arrays.sort(destSamples);
        try {

            if (sc.isEmpty(srcSample)) {
                if (copyEmpty)
                    for (int i = 0; i < destSamples.length; i++)
                        try {
                            sc.erase(destSamples[i]).post();
                        } catch (ResourceUnavailableException e1) {
                            e1.printStackTrace();
                        }
            } else if (provideFeedback) {
                final ProgressCallbackTree prog = new ProgressCallbackTree("Sample range copy", false) {
                    public String finalizeString(String s) {
                        return sc.getDeviceContext().makeDeviceProgressTitle(s);
                    }
                };
                try {
                    sc.copy(srcSample, destSamples, prog).post(new CompleteProgressOnCancelledCallback(prog));
                } catch (Exception e) {
                    prog.updateProgress(1);
                    e.printStackTrace();
                } finally {
                }
            } else {
                try {
                    sc.copy(srcSample, destSamples, ProgressCallback.DUMMY).post();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
        }
    }

    public static String getSampleDisplayName(SampleContext pc, Integer sample) {
        String name = "";
        try {
            name = new com.pcmsolutions.device.EMU.database.ContextLocation(sample, pc.getName(sample)).toString();
        } catch (DeviceException e) {
        } catch (EmptyException e) {
            name = DeviceContext.EMPTY_SAMPLE;
        } catch (ContentUnavailableException e) {
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

    public static int userSampleCount(ReadableSample[] samples) {
        int count = 0;
        if (samples.length != 0)
            for (int i = 0; i < samples.length; i++)
                if (samples[i].getIndex().intValue() < DeviceContext.BASE_ROM_SAMPLE)
                    count++;
        return count;
    }

    public static ReadableSample[] extractReadableSamples(Object[] objects) {
        Object[] rp1 = ClassUtility.extractInstanceOf(objects, ReadableSample.class);
        ReadableSample[] rp2 = new ReadableSample[rp1.length];
        System.arraycopy(rp1, 0, rp2, 0, rp1.length);
        return rp2;
    }

    public static ContextEditableSample[] extractContextEditableSamples(Object[] objects) {
        Object[] rp1 = ClassUtility.extractInstanceOf(objects, ContextEditableSample.class);
        ContextEditableSample[] rp2 = new ContextEditableSample[rp1.length];
        System.arraycopy(rp1, 0, rp2, 0, rp1.length);
        return rp2;
    }

    public static Integer[] extractSampleIndexes(ReadableSample[] samples) {
        Integer[] indexes = new Integer[samples.length];
        for (int i = 0; i < samples.length; i++)
            indexes[i] = samples[i].getIndex();
        return indexes;
    }

    public static ReadableSample[] extractRomSamples(ReadableSample[] samples) {
        ArrayList roms = new ArrayList();
        for (int i = 0; i < samples.length; i++)
            if (samples[i].getIndex().intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                roms.add(samples[i]);
        return (ReadableSample[]) roms.toArray(new ReadableSample[roms.size()]);
    }

    public static Integer[] extractUniqueSampleIndexes(ReadableSample[] samples) {
        Set s = new HashSet();
        for (int i = 0; i < samples.length; i++)
            s.add(samples[i].getIndex());
        return (Integer[]) s.toArray(new Integer[s.size()]);
    }

    public static File[] extractFiles(IsolatedSample[] samples) {
        ArrayList<File> files = new ArrayList<File>();
        for (int i = 0; i < samples.length; i++)
            if (samples[i].isROMSample())
                files.add(null);
            else
                files.add(samples[i].getLocalFile());

        return files.toArray(new File[files.size()]);
    }

    public static Integer[] extractIndexes(IsolatedSample[] samples) {
        Integer[] indexes = new Integer[samples.length];
        for (int i = 0; i < samples.length; i++)
            indexes[i] = samples[i].getOriginalIndex();
        return indexes;
    }
}