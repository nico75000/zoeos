package com.pcmsolutions.device.EMU.E4.packaging;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.gui.ProgressSession;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;
import com.pcmsolutions.system.preferences.ZBoolPref;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.util.IntegerUseMap;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.prefs.Preferences;
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
    public static final String PKG_HEADERS_ENTRY = "zppkg_headers";

    public static class PresetPackageManifest {
        PresetPackage presetPackage;
        SamplePackageManifest samplePackageManifest = null;
        Integer[] sourcePresetIndexes;
        IsolatedPreset[] isolatedPresets;
        boolean missingSamples;
        boolean brokenRomReferences;
        Integer[] referencedSamples;
        String packageLocation;
        Integer[] referencedRomSamples;
        Integer[] referencedUserSamples;

        Integer[] missingUserSamples;
        Integer[] brokenRomSamples;
        Integer[] emptyUserSamples;   // user indexes without an entry in the sample package

        SampleContext sc;

        public PresetPackageManifest(PresetPackage presetPackage, String location, SampleContext sc) {
            this.presetPackage = presetPackage;
            this.packageLocation = location;
            this.sc = sc;
            initManifest();
        }

        void initManifest() {
            SamplePackage spkg = presetPackage.getSamplePackage();
            if (spkg != null)
                samplePackageManifest = new SamplePackageManifest(spkg, packageLocation);
            isolatedPresets = presetPackage.getPresets();
            sourcePresetIndexes = PresetContextMacros.extractPresetIndexes(isolatedPresets);
            IntegerUseMap map = new IntegerUseMap();
            for (int i = 0; i < isolatedPresets.length; i++)
                map.mergeUseMap(isolatedPresets[i].referencedSampleUsage());
            map.removeIntegerReference(IntPool.get(0));
            referencedSamples = map.getIntegers();
            referencedRomSamples = PresetContextMacros.getRomIndexes(referencedSamples);
            referencedUserSamples = SampleContextMacros.extractUserIndexes(referencedSamples);

            // NOTE: preset packages created since 15th May 2004 will always have a sample package included
            // second branch of IF below provided for backward compatibility            
            if (samplePackageManifest != null) {
                brokenRomSamples = SampleContextMacros.extractIndexes(getSamplePackageManifest().getBrokenRomReferences(sc));
                missingUserSamples = SampleContextMacros.extractIndexes(getSamplePackageManifest().getMissingSamples());
                List s = Arrays.asList(SampleContextMacros.extractUserIndexes(samplePackageManifest.getSourceIndexes()));
                ArrayList empty = new ArrayList();
                for (int i = 0; i < referencedUserSamples.length; i++)
                    if (!s.contains(referencedUserSamples[i]))
                        empty.add(referencedUserSamples[i]);
                emptyUserSamples = (Integer[]) empty.toArray(new Integer[empty.size()]);
            } else {
                brokenRomSamples = referencedRomSamples;
                missingUserSamples = referencedUserSamples;
                emptyUserSamples = new Integer[0];
            }
        }

        public SamplePackageManifest getSamplePackageManifest() {
            return samplePackageManifest;
        }

        public Integer[] getEmptyUserSamples() {
            return (Integer[]) emptyUserSamples.clone();
        }

        public Integer[] getMissingUserSamples() {
            return (Integer[]) missingUserSamples.clone();
        }

        public Integer[] getBrokenRomSamples() {
            return (Integer[]) brokenRomSamples.clone();
        }

        public Integer[] getReferencedSamples() {
            return (Integer[]) referencedSamples.clone();
        }

        public Integer[] getReferencedRomSamples() {
            return (Integer[]) referencedRomSamples.clone();
        }

        public Integer[] getReferencedUserSamples() {
            return (Integer[]) referencedUserSamples.clone();
        }

        public Integer[] getSourcePresetIndexes() {
            return (Integer[]) sourcePresetIndexes.clone();
        }

        public PresetPackage getPresetPackage() {
            return presetPackage;
        }
    }

    public static class SamplePackageManifest {
        SamplePackage samplePackage;
        String packageLocation;
        IsolatedSample[] isoSamples;
        Integer[] sourceIndexes;
        File[] files;
        TreeMap file2SrcIndexMap;
        File[] nonNullFiles;
        int nonNullFileCount;
        File[] nonNullValidFiles;
        int nonNullValidFileCount;
        IsolatedSample[] missing;

        public SamplePackageManifest(SamplePackage pkg, String location) {
            this.samplePackage = pkg;
            this.packageLocation = location;
            initManifest();
        }

        void initManifest() {
            isoSamples = samplePackage.getSamples();
            files = SampleContextMacros.extractFiles(isoSamples);
            sourceIndexes = SampleContextMacros.extractIndexes(isoSamples);
            ZUtilities.prefixPath(files, packageLocation); // will ignore nulls

            // some files may be null (typically in the case of ROM samples )
            nonNullFileCount = files.length - ZUtilities.numNulls(files);
            //- TempFileManager.numTmpExtensions(files); // numTmpExtensions required because of a bug that caused IsolatedSamples with ROM indexes to point to temp files - this causes confusion in incoming sample packages

            file2SrcIndexMap = new TreeMap();
            for (int i = 0; i < isoSamples.length; i++)
                if (files[i] != null)
                    file2SrcIndexMap.put(files[i], sourceIndexes[i]);

            nonNullFiles = (File[]) file2SrcIndexMap.keySet().toArray(new File[file2SrcIndexMap.keySet().size()]);
            nonNullValidFiles = ZUtilities.filesThatExist(nonNullFiles);
            nonNullValidFileCount = nonNullValidFiles.length;

            ArrayList missing = new ArrayList();
            for (int i = 0; i < files.length; i++)
                if (files[i] != null && !files[i].exists())
                    missing.add(isoSamples[i]);
            this.missing = (IsolatedSample[]) missing.toArray(new IsolatedSample[missing.size()]);
        }

        public IsolatedSample[] getBrokenRomReferences(SampleContext sc) {
            return SampleContextMacros.unmatchedRomLocations(isoSamples, sc);
        }

        public IsolatedSample[] getMissingSamples() {
            return (IsolatedSample[]) missing.clone();
        }

        public SamplePackage getSamplePackage() {
            return samplePackage;
        }

        public String getPackageLocation() {
            return packageLocation;
        }

        public IsolatedSample[] getIsoSamples() {
            return (IsolatedSample[]) isoSamples.clone();
        }

        public File[] getFiles() {
            return (File[]) files.clone();
        }

        public Map getFile2SrcIndexMap() {
            return (Map) file2SrcIndexMap.clone();
        }

        public File[] getNonNullFiles() {
            return (File[]) nonNullFiles.clone();
        }

        public int getNonNullFileCount() {
            return nonNullFileCount;
        }

        public File[] getNonNullValidFiles() {
            return (File[]) nonNullValidFiles.clone();
        }

        public int getNonNullValidFileCount() {
            return nonNullValidFileCount;
        }

        public Integer[] getSourceIndexes() {
            return (Integer[]) sourceIndexes.clone();
        }
    }

    public static final ZBoolPref ZPREF_serachForAndOnlyUseEmptySamples = new Impl_ZBoolPref(Preferences.userNodeForPackage(PackageFactory.class.getClass()), "searchAndOnyUseEmptySamples", true);
    public static final ZBoolPref ZPREF_serachForAndOnlyUseEmptyPresets = new Impl_ZBoolPref(Preferences.userNodeForPackage(PackageFactory.class.getClass()), "searchAndOnyUseEmptyPresets", true);
    public static final ZBoolPref ZPREF_applyMultimode = new Impl_ZBoolPref(Preferences.userNodeForPackage(PackageFactory.class.getClass()), "applyMultimode", true);
    public static final ZBoolPref ZPREF_applyMaster = new Impl_ZBoolPref(Preferences.userNodeForPackage(PackageFactory.class.getClass()), "applyMaster", true);
    public static final ZBoolPref ZPREF_zeroMissingSamples = new Impl_ZBoolPref(Preferences.userNodeForPackage(PackageFactory.class.getClass()), "zeroMissingSamples", true);
    public static final ZBoolPref ZPREF_zeroBrokenRomReferences = new Impl_ZBoolPref(Preferences.userNodeForPackage(PackageFactory.class.getClass()), "zeroBrokenRomReferences", false);

    public static PresetPackage createPresetPackage(PresetContext pc, Integer[] presets, String name, String notes, boolean incMaster, boolean incMultimode, boolean incSamples, AudioFileFormat.Type format, Map customObjectMap) throws PackageGenerationException {
        try {
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
            //IntegerUseMap presetUsageMap = evaluatePresetsForPackage(presets, deep, pc, ProgressCallback.DUMMY);

            // MASTER
            Integer[] masterIds = null;
            Integer[] masterVals = null;
            if (header.includingMasterSettings) {
                Set mIds;
                mIds = pc.getDeviceParameterContext().getMasterContext().getIds();
                masterVals = pc.getDeviceContext().getMasterContext().getMasterParams((Integer[]) mIds.toArray(new Integer[mIds.size()]));
                masterIds = (Integer[]) mIds.toArray(new Integer[mIds.size()]);
            }

            // MULTIMODE
            MultiModeMap multiModeMap = null;
            if (header.includingMultimodeSettings)
                multiModeMap = new Impl_SerializableMultiModeMap(pc.getDeviceContext().getMultiModeContext().getMultimodeMap().getMapData());

            // ISOLATE PRESETS
            IsolatedPreset[] isoPresets = generateIsolatedPresets(header.getName(), presets/* presetUsageMap.getIntegers()*/, pc);
            // SAMPLES
            SamplePackage spkg = null;
            if (incSamples) {
                spkg = createSamplePackage(pc.getRootSampleContext(), isoPresets, notes, name, null, format);
                header.setIncludingSamples(true);
            } else {
                Integer[] romIndexes = PresetContextMacros.getRomIndexes(PresetContextMacros.getSampleUsage(isoPresets).getIntegers());
                spkg = createSamplePackage(pc.getRootSampleContext(), romIndexes, notes, name, null, format);
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
        } catch (Exception e) {
            throw new PackageGenerationException(e.getMessage());
        }
    }

    public static Integer[] evaluatePresetsForPackage(Integer[] presets, boolean deep, PresetContext pc, ProgressCallback prog) throws PackageGenerationException {
        try {
            Set preset_set;
            if (deep) {
                try {
                    preset_set = pc.getPresetsDeepSet(presets);
                    return (Integer[]) preset_set.toArray(new Integer[preset_set.size()]);
                } catch (Exception e) {
                    throw new PackageGenerationException(e.getMessage());
                } finally {
                }
            } else
                return presets;
        } finally {
            prog.updateProgress(1);
        }
    }

    private static IsolatedPreset[] generateIsolatedPresets(String progressTitle, Integer[] presets, PresetContext pc) throws PackageGenerationException {
        Arrays.sort(presets);
        Zoeos z = Zoeos.getInstance();
        ProgressSession ps = null;
        ps = z.getProgressSession("Writing preset package " + ZUtilities.quote(progressTitle), presets.length);
        try {
            List ip_list = new ArrayList();
            for (int i = 0; i < presets.length; i++) {
                try {
                    //z.updateProgressElementTitle(po, "Retrieving");
                    ip_list.add(pc.getIsolatedPreset(presets[i], false));
                } catch (EmptyException e) {
                } catch (Exception e) {
                    throw new PackageGenerationException(e.getMessage());
                } finally {
                    ps.updateStatus();
                }
            }
            return (IsolatedPreset[]) ip_list.toArray(new IsolatedPreset[ip_list.size()]);
        } finally {
            ps.end();
        }
    }

    public static SamplePackage createSamplePackage(SampleContext sc, IsolatedPreset[] presets, String notes, String name, Map customObjectMap, AudioFileFormat.Type format) throws PackageGenerationException {
        IntegerUseMap m = PresetContextMacros.getSampleUsage(presets);
        m.removeIntegerReference(IntPool.get(0));
        Integer[] samples = m.getIntegers();
        for (int i = 0; i < samples.length; i++)
            try {
                sc.refresh(samples[i]).post();
            } catch (ResourceUnavailableException e) {
                e.printStackTrace();
            }
        return createSamplePackage(sc, m.getIntegers(), name, notes, customObjectMap, format);
    }

    public static SamplePackage createSamplePackage(SampleContext sc, Integer[] samples, String name, String notes, Map customObjectMap, AudioFileFormat.Type format) throws PackageGenerationException {
        try {
            if (!sc.getDeviceContext().isSmdiCoupled() && SampleContextMacros.extractUserIndexes(samples).length > 0)
                throw new PackageGenerationException("Cannot not retrieve samples for package - the device is not SMDI coupled");
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
                    isl.add(sc.getIsolated(samples[i], format));
                    //usedIndexes.addDesktopElement(samples[i]);
                } catch (DeviceException e) {
                    throw new PackageGenerationException(e.getMessage());
                } catch (EmptyException e) {
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
        } catch (Exception e) {
            throw new PackageGenerationException(e.getMessage());
        }
    }

    private static final DecimalFormat df = new DecimalFormat("0000");

    public static String getIndexedFileName(IsolatedSample is) throws IOException, UnsupportedAudioFileException {
        return df.format(is.getOriginalIndex()) + ZUtilities.STRING_FIELD_SEPERATOR + is.getName() + ZUtilities.FILE_EXTENSION + is.getFormatType().getExtension();
    }

    public static void savePresetPackage(PresetPackage pkg, File pkgFile, ProgressCallback prog) throws PackageGenerationException {
        try {
            if (!(pkg instanceof Impl_PresetPackage))
                throw new PackageGenerationException("invalid preset package object");

            ZipOutputStream os = null;
            ObjectOutputStream oos = null;
            try {
                os = new ZipOutputStream(new FileOutputStream(pkgFile));
                os.setMethod(ZipOutputStream.DEFLATED);
                os.setLevel(Deflater.BEST_SPEED);
                os.putNextEntry(new ZipEntry(PresetPackage.PRESET_PKG_CONTENT_ENTRY));
                oos = new ObjectOutputStream(os);
                oos.writeObject(pkg.getHeader());
                try {
                    makeIsolatedPresetsSerializable((Impl_PresetPackage) pkg);
                } catch (PresetException e) {
                    throw new PackageGenerationException("General packaging error");
                }
                oos.writeObject(pkg);
                if (pkg.getSamplePackage() != null) {
                    File smplDir = ZUtilities.replaceExtension(pkgFile, SamplePackage.SAMPLE_DIR_EXT);
                    try {
                        os.putNextEntry(new ZipEntry(SamplePackage.SAMPLE_PKG_CONTENT_ENTRY));
                    } catch (IOException e) {
                        throw new PackageGenerationException(e.getMessage());
                    }
                    writeSamplePackage(pkg.getSamplePackage(), oos, smplDir, prog);
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
        } finally {
            prog.updateProgress(1);
        }
    }

    public static void saveSamplePackage(SamplePackage pkg, File pkgFile, ProgressCallback prog) throws PackageGenerationException {
        ZipOutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            os = new ZipOutputStream(new FileOutputStream(pkgFile));
            os.setMethod(ZipOutputStream.DEFLATED);
            os.setLevel(Deflater.BEST_SPEED);
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
            writeSamplePackage(pkg, oos, smplDir, prog);
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

    public static boolean prepareSampleDirectory(File smplDir, boolean dirRequired) throws PackageGenerationException {
        if (smplDir.exists()) {
            if (!dirRequired) {
                if (!UserMessaging.askYesNo("<html>This particular package does not require a sample directory but the directory " + smplDir.getName() + " already exists.<br><br>To avoid confusion or a potential conflict, delete this directory before proceeding?</html>", "Potential confusion"))
                    return true;
                File[] contents = smplDir.listFiles();
                if (contents.length > 0)
                    for (int i = 0; i < contents.length; i++)
                        if (contents[i].isDirectory())
                            throw new PackageGenerationException("Could not delete the conflicting directory " + smplDir.getName() + "  as it contains sub-directories.");

                for (int i = 0; i < contents.length; i++)
                    if (!contents[i].delete())
                        throw new PackageGenerationException("Couldn't delete a file in the conflicting sample directory");
                if (!smplDir.delete())
                    throw new PackageGenerationException("Could not delete the conflicting sample directory " + smplDir.getName());
            } else {
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
                            throw new PackageGenerationException("Couldn't delete a file in the existing sample directory");
                    //if (!smplDir.delete())
                    //  throw new PackageGenerationException("could not delete existing sample directory " + smplDir.getName() + ". Does it contain some files?");
                } else {
                    if (!UserMessaging.askYesNo("Directory " + smplDir.getName() + " already exists but is empty. Use it?", "Directory already exists"))
                        return false;
                    //if (!smplDir.delete())
                    //  throw new PackageGenerationException("could not delete existing sample directory " + smplDir.getName() + ". Does it contain some files?");
                }
            }
        } else {
            if (dirRequired && !smplDir.mkdir())
                throw new PackageGenerationException("Could not create the sample directory");
        }
        return true;
    }

    private static void writeSamplePackage(SamplePackage pkg, ObjectOutputStream os, File smplDir, ProgressCallback prog) throws PackageGenerationException {
        if (!(pkg instanceof Impl_SamplePackage)) { // must be PackageFactory package internal implementation
            throw new PackageGenerationException("General packaging error");
        }

        IsolatedSample[] samples = pkg.getSamples();

        prepareSampleDirectory(smplDir, romSampleCount(samples) != samples.length);
        // if (romSampleCount(samples) != samples.length) {
        //     prepareSampleDirectory(smplDir);
        // }
        prog.updateLabel("Writing sample package");
        ProgressCallback[] progs = prog.splitTask(SampleContextMacros.userSampleCount(samples), true);
        int pi = 0;
        try {
            for (int i = 0; i < samples.length; i++) {
                if (!samples[i].isROMSample()) {
                    try {
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
                            progs[pi].updateLabel("Writing sample " + ZUtilities.quote(samples[i].getName()));
                            samples[i].assertSample(progs[pi]);
                            // now make File location in IsolatedSample relative to package File (without moving the sample)
                            samples[i].setLocalFile(new File(smplDir.getName(), isFileName), false);
                        } catch (IsolatedSampleUnavailableException e) {
                            // clean up
                            for (int s = 0; s < i; s++)
                                samples[s].zDispose();
                            throw new PackageGenerationException("Could not retrieve samples");
                        }
                    } finally {
                        progs[pi++].updateProgress(1);
                    }
                }
            }
            // ps.updateTitle("Writing sample package " + ZUtilities.quote(ZUtilities.quote(pkg.getHeader().getName())));
            makeIsolatedSamplesSerializable((Impl_SamplePackage) pkg);
            try {
                os.writeObject(pkg.getHeader());
                os.writeObject(pkg);
            } catch (IOException e) {
                throw new PackageGenerationException(e.getMessage());
            }
        } finally {
            prog.updateProgress(1);
        }
    }

    private static void makeIsolatedSamplesSerializable(Impl_SamplePackage pkg) {
        IsolatedSample[] samples = pkg.getSamples();
        for (int i = 0; i < samples.length; i++)
            samples[i] = new Impl_SerializableIsolatedSample(samples[i]);
        pkg.setSamples(samples);
    }

    private static void makeIsolatedPresetsSerializable(Impl_PresetPackage pkg) throws PresetException {
        IsolatedPreset[] presets = pkg.getPresets();
        for (int i = 0; i < presets.length; i++)
            presets[i] = new Impl_SerializableIsolatedPreset(presets[i]);
        pkg.setPresets(presets);
    }

    public static IsolatedPreset makeSerializableIsolatedPreset(IsolatedPreset preset) throws PresetException {
        return new Impl_SerializableIsolatedPreset(preset);
    }

    public static IsolatedSample makeSerializableIsolatedSample(IsolatedSample sample) {
        return new Impl_SerializableIsolatedSample(sample);
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

        // if (ppkg.getHeader().isIncludingSamples()) {
        if (!(ppkg instanceof Impl_PresetPackage))
            throw new CommandFailedException("unknown preset package format");
        if (spkg instanceof Impl_SamplePackage)
            ((Impl_PresetPackage) ppkg).setSamplePackage((Impl_SamplePackage) spkg);
        // else
        //    throw new CommandFailedException("missing or invalid sample package");
        // }
        return ppkg;
    }

    public interface PresetPackageHeaders {
        PresetPackage.Header getPresetPackageHeader();

        SamplePackage.Header getSamplePackageHeader();
    }

    public static PresetPackageHeaders extractPresetPackageHeader(File f) throws CommandFailedException {
        ObjectInputStream ois = null;
        PresetPackage.Header ppkg_hdr = null;
        SamplePackage.Header spkg_hdr = null;
        ZipEntry ze = null;
        try {
            ZipInputStream zis;
            FileInputStream fis;
            fis = new FileInputStream(f);
            try {
                zis = new ZipInputStream(fis);
                for (int i = 0; i < 2; i++) {
                    try {
                        ze = zis.getNextEntry();
                        if (ze == null)
                            break;
                        if (ois == null)
                            ois = new ObjectInputStream(zis);
                        if (ze.getName().equals(PresetPackage.PRESET_PKG_CONTENT_ENTRY))
                            ppkg_hdr = (PresetPackage.Header) ois.readObject();
                        else if (ze.getName().equals(SamplePackage.SAMPLE_PKG_CONTENT_ENTRY))
                            spkg_hdr = (SamplePackage.Header) ois.readObject();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            } finally {
                fis.close();
            }
        } catch (IOException e) {
            throw new CommandFailedException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new CommandFailedException(e.getMessage());
        }
        if (ppkg_hdr == null)
            throw new CommandFailedException("couldn't read package header");

        final PresetPackage.Header f_ppkg_hdr = ppkg_hdr;
        final SamplePackage.Header f_spkg_hdr = spkg_hdr;
        return new PresetPackageHeaders() {
            public PresetPackage.Header getPresetPackageHeader() {
                return f_ppkg_hdr;
            }

            public SamplePackage.Header getSamplePackageHeader() {
                return f_spkg_hdr;
            }
        };
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

    public static SamplePackage.Header extractSamplePackageHeader(File f) throws CommandFailedException {
        ObjectInputStream ois = null;
        try {
            ZipInputStream zis;
            FileInputStream fis;
            fis = new FileInputStream(f);
            try {
                zis = new ZipInputStream(fis);
                zis.getNextEntry();
                ois = new ObjectInputStream(zis);
                SamplePackage.Header hdr = (SamplePackage.Header) ois.readObject();
                return hdr;
            } finally {
                fis.close();
            }
        } catch (IOException e) {
            throw new CommandFailedException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new CommandFailedException(e.getMessage());
        }
    }
}
