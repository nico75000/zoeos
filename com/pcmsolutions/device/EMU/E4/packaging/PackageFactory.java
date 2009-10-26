package com.pcmsolutions.device.EMU.E4.packaging;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.*;
import com.pcmsolutions.util.IntegerUseMap;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Dec-2003
 * Time: 00:40:02
 * To change this template use Options | File Templates.
 */
public class PackageFactory {

    public static PresetPackage createPresetPackage(PresetContext pc, Integer[] presets, boolean deep, String name, String notes, boolean incMaster, boolean incMultimode, boolean incSamples, AudioFileFormat.Type format, Map customObjectMap) throws PackageGenerationException {
        // HEADER
        Impl_PresetPackageHeader header = new Impl_PresetPackageHeader();
        header.setCreationDate(new Date());
        header.setIncludingMasterSettings(incMaster);
        header.setIncludingMultimodeSettings(incMultimode);
        header.setNotes(notes);
        header.setName(name);
        header.setDeviceVersion(pc.getDeviceContext().getDeviceVersion());
        header.setDeviceName(pc.getDeviceContext().getName());

        // EVALUATE
        IntegerUseMap presetUsageMap = evaluatePresetPackage(header.getName(), presets, deep, pc);

        // MASTER
        Integer[] masterIds = null;
        Integer[] masterVals = null;
        if (header.includingMasterSettings) {
            Set mIds;
            mIds = pc.getDeviceParameterContext().getMasterContext().getIds();
            try {
                masterVals = pc.getDeviceContext().getMasterContext().getMasterParams((Integer[]) mIds.toArray(new Integer[mIds.size()]));
                masterIds = (Integer[]) mIds.toArray(new Integer[mIds.size()]);
            } catch (IllegalParameterIdException e) {
                throw new PackageGenerationException("could not generate master settings");
            } catch (ZDeviceNotRunningException e) {
                throw new PackageGenerationException("device not running");
            }
        }

        // MULTIMODE
        MultiModeMap multiModeMap = null;
        if (header.includingMultimodeSettings)
            try {
                multiModeMap = pc.getDeviceContext().getMultiModeContext().getMultimodeMap();
            } catch (ZDeviceNotRunningException e) {
                throw new PackageGenerationException("device not running");
            }

        // ISOLATE PRESETS
        IsolatedPreset[] isoPresets = generateIsolatedPresets(header.getName(), presetUsageMap.getIntegers(), pc);

        // SAMPLES
        SamplePackage spkg = null;
        if (incSamples) {
            spkg = createSamplePackage(pc.getRootSampleContext(), isoPresets, notes, name, null, format);
            header.setIncludingSamples(true);
        }

        // FINALIZE PACKAGE
        Impl_PresetPackage pkg = new Impl_PresetPackage(isoPresets);
        header.setPresetCount(isoPresets.length);
        pkg.setHeader(header);

        if (spkg != null) {
            pkg.setSamplePackage((Impl_SamplePackage) spkg);
        }

        if (customObjectMap != null)
            if (ZUtilities.isMapContentsSerializable(customObjectMap))
                pkg.setCustomObjectMap(customObjectMap);
            else
                throw new PackageGenerationException("CustomObjectMap not serializable");

        if (header.includingMasterSettings) {
            header.setIncludingMasterSettings(true);
            pkg.setMasterIds(masterIds);
            pkg.setMasterVals(masterVals);
        }

        if (header.includingMultimodeSettings) {
            header.setIncludingMultimodeSettings(true);
            pkg.setMultiModeMap(multiModeMap);
        }

        return pkg;
    }

    private static IntegerUseMap evaluatePresetPackage(String title, Integer[] presets, boolean deep, PresetContext pc) throws PackageGenerationException {
        final Object po = new Object();
        Zoeos z = Zoeos.getInstance();
        z.beginProgressElement(po, "Evaluating preset package " + ZUtilities.quote(title), presets.length);
        try {
            IntegerUseMap presetUsageMap = new IntegerUseMap();
            if (deep)
                for (int i = 0; i < presets.length; i++) {
                    try {
                        presetUsageMap.addIntegerReference(presets[i]);
                        presetUsageMap.mergeUseMap(pc.presetLinkPresetUsage(presets[i]));
                    } catch (PresetEmptyException e) {
                    } catch (NoSuchPresetException e) {
                        throw new PackageGenerationException("no such preset " + presets[i]);
                    } catch (NoSuchContextException e) {
                        throw new PackageGenerationException("no such context");
                    }
                    z.updateProgressElement(po);
                }
            else
                presetUsageMap.addIntegerReferences(presets);
            return presetUsageMap;
        } finally {
            z.endProgressElement(po);
        }
    }

    private static IsolatedPreset[] generateIsolatedPresets(String progressTitle, Integer[] presets, PresetContext pc) throws PackageGenerationException {
        Arrays.sort(presets);
        final Object po = new Object();
        Zoeos z = Zoeos.getInstance();
        z.beginProgressElement(po, "Writing preset package " + ZUtilities.quote(progressTitle), presets.length);
        try {
            List ip_list = new ArrayList();
            for (int i = 0; i < presets.length; i++) {
                try {
                    //z.updateProgressElementTitle(po, "Retrieving");
                    ip_list.add(pc.getIsolatedPreset(presets[i]));
                } catch (PresetEmptyException e) {
                } catch (NoSuchPresetException e) {
                    throw new PackageGenerationException("no such preset " + presets[i]);
                } catch (NoSuchContextException e) {
                    throw new PackageGenerationException("no such context");
                } finally {
                    z.updateProgressElement(po);
                }
            }
            return (IsolatedPreset[]) ip_list.toArray(new IsolatedPreset[ip_list.size()]);
        } finally {
            z.endProgressElement(po);
        }
    }

    public static SamplePackage createSamplePackage(SampleContext sc, IsolatedPreset[] presets, String notes, String name, Map customObjectMap, AudioFileFormat.Type format) throws PackageGenerationException {
        IntegerUseMap m = PresetContextMacros.getSampleUsage(presets);
        m.removeIntegerReference(IntPool.get(0));
        return createSamplePackage(sc, m.getIntegers(), name, notes, customObjectMap, format);
    }

    public static SamplePackage createSamplePackage(SampleContext sc, Integer[] samples, String name, String notes, Map customObjectMap, AudioFileFormat.Type format) throws PackageGenerationException {
        // HEADER
        Impl_SamplePackageHeader header = new Impl_SamplePackageHeader();
        header.setCreationDate(new Date());
        header.setNotes(notes);
        header.setName(name);
        header.setDeviceVersion(sc.getDeviceContext().getDeviceVersion());
        header.setDeviceName(sc.getDeviceContext().getName());
        // GENERATE ISOLATED SAMPLES
        ArrayList isl = new ArrayList();
        //HashSet usedIndexes = new HashSet();
        for (int i = 0; i < samples.length; i++) {
            //if (!usedIndexes.contains(samples[i]))
            try {
                isl.add(sc.getIsolatedSample(samples[i], (File) null, format));
                //usedIndexes.addDesktopElement(samples[i]);
            } catch (NoSuchSampleException e) {
                throw new PackageGenerationException(e.getMessage());
            } catch (NoSuchContextException e) {
                throw new PackageGenerationException(e.getMessage());
            } catch (SampleEmptyException e) {
                //throw new PackageGenerationException("samples(s) are empty");
            }
        }
        IsolatedSample[] isoSamples = (IsolatedSample[]) isl.toArray(new IsolatedSample[isl.size()]);
        header.setSampleCount(isoSamples.length);
        header.setPhysicalSampleCount(isoSamples.length - romSampleCount(isoSamples));

        Impl_SamplePackage pkg = new Impl_SamplePackage(isoSamples);
        if (customObjectMap != null)
            if (ZUtilities.isMapContentsSerializable(customObjectMap))
                pkg.setCustomObjectMap(customObjectMap);
            else
                throw new PackageGenerationException("CustomObjectMap not serializable");
        pkg.setHeader(header);
        return pkg;
    }

    private static final DecimalFormat df = new DecimalFormat("0000");

    public static String getIndexedFileName(IsolatedSample is) throws IOException, UnsupportedAudioFileException {
        return df.format(is.getOriginalIndex()) + ZUtilities.STRING_FIELD_SEPERATOR + is.getName() + ZUtilities.FILE_EXTENSION + is.getFormatType().getExtension();
    }

    public static void savePresetPackage(PresetPackage pkg, File pkgFile/*, boolean showProgress*/) throws PackageGenerationException {
        if (!(pkg instanceof Impl_PresetPackage))
            throw new PackageGenerationException("invalid preset package object");

        ZipOutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            os = new ZipOutputStream(new FileOutputStream(pkgFile));
            os.setMethod(ZipOutputStream.DEFLATED);
            os.setLevel(Deflater.BEST_COMPRESSION);
            os.putNextEntry(new ZipEntry(PresetPackage.PRESET_PKG_CONTENT_ENTRY));
            oos = new ObjectOutputStream(os);
            oos.writeObject(pkg.getHeader());
            try {
                makeIsolatedPresetsSerializable((Impl_PresetPackage) pkg);
            } catch (NoSuchLinkException e) {
                throw new PackageGenerationException("packaging error");
            } catch (NoSuchZoneException e) {
                throw new PackageGenerationException("packaging error");
            } catch (NoSuchVoiceException e) {
                throw new PackageGenerationException("packaging error");
            }
            oos.writeObject(pkg);
            if (pkg.getSamplePackage() != null) {
                File smplDir = ZUtilities.replaceExtension(pkgFile, SamplePackage.SAMPLE_DIR_EXT);
                try {
                    os.putNextEntry(new ZipEntry(SamplePackage.SAMPLE_PKG_CONTENT_ENTRY));
                } catch (IOException e) {
                    throw new PackageGenerationException(e.getMessage());
                }
                writeSamplePackage(pkg.getSamplePackage(), oos, smplDir);
            }
            oos.close();
        } catch (IOException e) {
            // clean up
            pkgFile.delete();
            throw new PackageGenerationException(e.getMessage());
        }
        //   ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
        // PresetPackage.Header hdr_copy = (PresetPackage.Header)ois.readObject();
        // PresetPackage pkg_copy = (PresetPackage)ois.readObject();
        //  ois.closed();
    }

    public static void saveSamplePackage(SamplePackage pkg, File pkgFile/*, boolean showProgress*/) throws PackageGenerationException {
        ZipOutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            os = new ZipOutputStream(new FileOutputStream(pkgFile));
            os.setMethod(ZipOutputStream.DEFLATED);
            os.setLevel(Deflater.BEST_COMPRESSION);
            os.putNextEntry(new ZipEntry(SamplePackage.SAMPLE_PKG_CONTENT_ENTRY));
            oos = new ObjectOutputStream(os);
            //oos.writeObject(pkg.getHeader());
            //oos.writeObject(pkg);
            // if (pkg.getSamplePackage() != null) {
            File smplDir = ZUtilities.replaceExtension(pkgFile, SamplePackage.SAMPLE_DIR_EXT);
            //try {
            //     os.putNextEntry(new ZipEntry(SamplePackage.SAMPLE_PKG_CONTENT_ENTRY));
            // } catch (IOException e) {
            //     throw new PackageGenerationException(e.getMessage());
            // }
            writeSamplePackage(pkg, oos, smplDir);
            //  }
            oos.close();
        } catch (IOException e) {
            // clean up
            pkgFile.delete();
            throw new PackageGenerationException(e.getMessage());
        }
        //   ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
        // PresetPackage.Header hdr_copy = (PresetPackage.Header)ois.readObject();
        // PresetPackage pkg_copy = (PresetPackage)ois.readObject();
        //  ois.closed();
    }

    private static int romSampleCount(IsolatedSample[] samples) {
        int count = 0;
        for (int i = 0; i < samples.length; i++)
            if (samples[i].isROMSample())
                count++;
        return count;
    }

    private static boolean assertSampleDirectory(File smplDir) throws PackageGenerationException {
        if (smplDir.exists()) {
            File[] contents = smplDir.listFiles();
            if (contents.length > 0)
                for (int i = 0; i < contents.length; i++)
                    if (contents[i].isDirectory())
                        throw new PackageGenerationException("Directory " + smplDir.getName() + " already exists and contains sub-directories.");

            if (contents.length > 0) {
                if (!UserMessaging.askYesNo("Directory " + smplDir.getName() + " already exists and contains some files. Delete these and use directory?", "Directory already exists"))
                    return false;
                for (int i = 0; i < contents.length; i++)
                    if (!contents[i].delete())
                        throw new PackageGenerationException("couldn't delete an existing File");
                //if (!smplDir.delete())
                //  throw new PackageGenerationException("could not delete existing sample directory " + smplDir.getName() + ". Does it contain some files?");
            } else {
                if (!UserMessaging.askYesNo("Directory " + smplDir.getName() + " already exists but is empty. Use it?", "Directory already exists"))
                    return false;
                //if (!smplDir.delete())
                //  throw new PackageGenerationException("could not delete existing sample directory " + smplDir.getName() + ". Does it contain some files?");
            }

        } else {
            if (!smplDir.mkdir())
                throw new PackageGenerationException("could not create sample directory");
        }
        return true;
    }

    private static void writeSamplePackage(SamplePackage pkg, ObjectOutputStream os, File smplDir) throws PackageGenerationException {
        if (!(pkg instanceof Impl_SamplePackage)) { // must be PackageFactory package internal implementation
            throw new PackageGenerationException("illegal SamplePackage class");
        }

        IsolatedSample[] samples = pkg.getSamples();

        if (romSampleCount(samples) != samples.length) {
            assertSampleDirectory(smplDir);
        }
        Zoeos z = Zoeos.getInstance();
        final Object po = new Object();
        z.beginProgressElement(po, "Generating sample package \"" + pkg.getHeader().getName() + "\"", samples.length);
        z.setProgressElementIndeterminate(po, true);
        try {
            for (int i = 0; i < samples.length; i++) {
                if (!samples[i].isROMSample()) {
                    final String isFileName;
                    try {
                        isFileName = getIndexedFileName(samples[i]);
                    } catch (IOException e) {
                        throw new PackageGenerationException(e.getMessage());
                    } catch (UnsupportedAudioFileException e) {
                        throw new PackageGenerationException(e.getMessage());
                    }
                    // adjust location to sample directory and delete original if it exists (shouldn't though)
                    samples[i].setLocalFile(new File(smplDir, isFileName), true);
                    try {
                        // write it by asserting
                        z.updateProgressElementTitle(po, "Writing sample " + ZUtilities.quote(samples[i].getName()));
                        samples[i].ZoeAssert();
                        // now make File location in IsolatedSample relative to package File (without moving the sample)
                        samples[i].setLocalFile(new File(smplDir.getName(), isFileName), false);
                    } catch (IsolatedSampleUnavailableException e) {
                        // clean up
                        for (int s = 0; s < i; s++)
                            samples[s].zDispose();
                        throw new PackageGenerationException("Error retrieving samples");
                    }
                }
            }
            z.updateProgressElementTitle(po, "Writing sample package " + ZUtilities.quote(ZUtilities.quote(pkg.getHeader().getName())));
            makeIsolatedSamplesSerializable((Impl_SamplePackage) pkg);
            try {
                os.writeObject(pkg.getHeader());
                os.writeObject(pkg);
            } catch (IOException e) {
                throw new PackageGenerationException(e.getMessage());
            }
        } finally {
             z.endProgressElement(po);
        }
    }

    private static void makeIsolatedSamplesSerializable(Impl_SamplePackage pkg) {
        IsolatedSample[] samples = pkg.getSamples();
        for (int i = 0; i < samples.length; i++)
            samples[i] = new Impl_SerializableIsolatedSample(samples[i]);
        pkg.setSamples(samples);
    }

    private static void makeIsolatedPresetsSerializable(Impl_PresetPackage pkg) throws NoSuchLinkException, NoSuchZoneException, NoSuchVoiceException {
        IsolatedPreset[] presets = pkg.getPresets();
        for (int i = 0; i < presets.length; i++)
            presets[i] = new Impl_SerializableIsolatedPreset(presets[i]);
        pkg.setPresets(presets);
    }

    public static PresetPackage extractPresetPackage(File f) throws CommandFailedException {
        ObjectInputStream ois = null;
        PresetPackage.Header ppkg_hdr = null;
        SamplePackage.Header spkg_hdr = null;
        PresetPackage ppkg = null;
        SamplePackage spkg = null;
        ZipEntry ze = null;
        try {
            ZipInputStream zis;
            FileInputStream fis;
            fis = new FileInputStream(f);
            zis = new ZipInputStream(fis);
            for (int i = 0; i < 2; i++) {
                try {
                    ze = zis.getNextEntry();
                    if (ze == null)
                        break;
                    if (ois == null)
                        ois = new ObjectInputStream(zis);
                    if (ze.getName().equals(PresetPackage.PRESET_PKG_CONTENT_ENTRY)) {
                        ppkg_hdr = (PresetPackage.Header) ois.readObject();
                        ppkg = (PresetPackage) ois.readObject();
                    } else if (ze.getName().equals(SamplePackage.SAMPLE_PKG_CONTENT_ENTRY)) {
                        spkg_hdr = (SamplePackage.Header) ois.readObject();
                        spkg = (SamplePackage) ois.readObject();
                    } else
                        throw new CommandFailedException("unknown package entry");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            fis.close();
        } catch (IOException e) {
            throw new CommandFailedException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new CommandFailedException(e.getMessage());
        }
        if (ppkg == null)
            throw new CommandFailedException("couldn't read package");

        if (ppkg.getHeader().isIncludingSamples()) {
            if (!(ppkg instanceof Impl_PresetPackage))
                throw new CommandFailedException("unknown preset package format");
            if (spkg instanceof Impl_SamplePackage)
                ((Impl_PresetPackage) ppkg).setSamplePackage((Impl_SamplePackage) spkg);
            else
                throw new CommandFailedException("missing or invalid sample package");
        }
        return ppkg;
    }

    public static SamplePackage extractSamplePackage(File f) throws CommandFailedException {
        ObjectInputStream ois = null;
        try {
            ZipInputStream zis;
            FileInputStream fis;
            fis = new FileInputStream(f);
            zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            ois = new ObjectInputStream(zis);
            SamplePackage.Header hdr = (SamplePackage.Header) ois.readObject();
            SamplePackage pkg = (SamplePackage) ois.readObject();
            fis.close();
            return pkg;
        } catch (IOException e) {
            throw new CommandFailedException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new CommandFailedException(e.getMessage());
        }
    }
}
