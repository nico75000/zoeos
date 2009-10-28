package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.system.TempFileManager;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.audio.AudioUtilities;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;

/**
 * User: paulmeehan
 * Date: 31-Aug-2004
 * Time: 16:11:25
 */
public class SampleDownloadDescriptorFactory {
    public static SampleDownloadDescriptor getDownloadToTempFile(Integer sample) {
        return new Impl_SampleDownloadDescriptor(sample, null, null, AudioUtilities.defaultAudioFormat, null, false, true);
    }

    public static SampleDownloadDescriptor getDownloadToTempFile(Integer sample, AudioFileFormat.Type format) {
        return new Impl_SampleDownloadDescriptor(sample, null, null, format, null, false, true);
    }

    public static SampleDownloadDescriptor getDownloadToSpecificFile(Integer sample, File fn, AudioFileFormat.Type format, boolean overwrite) {
        return new Impl_SampleDownloadDescriptor(sample, null, fn, format, null, true, overwrite);
    }

    public static SampleDownloadDescriptor getGeneralDownload(Integer sample, String sampleName, File fn, AudioFileFormat.Type format, String namingMode, boolean applyExtension, boolean overwrite) {
        return new Impl_SampleDownloadDescriptor(sample, sampleName, fn, format, namingMode, applyExtension, overwrite);
    }

    private static class Impl_SampleDownloadDescriptor implements SampleDownloadDescriptor {
        protected File fn;
        protected AudioFileFormat.Type format;
        protected String namingMode = null;
        protected boolean applyingExtension = false;
        protected boolean overwriting = false;
        protected boolean isTemp = false;
        private Integer sample;

        // get temp File
        public Impl_SampleDownloadDescriptor(Integer sample) {
            this(sample, null, null, AudioUtilities.defaultAudioFormat, null, false, true);
        }

        // tempo File in specified format
        public Impl_SampleDownloadDescriptor(Integer sample, AudioFileFormat.Type format) {
            this(sample, null, null, format, null, false, true);
        }

        public Impl_SampleDownloadDescriptor(Integer sample, File fn, AudioFileFormat.Type format, boolean overwrite) {
            this(sample, null, fn, format, null, true, overwrite);
        }

        public Impl_SampleDownloadDescriptor(Integer sample, String sampleName, File fn, AudioFileFormat.Type format, String namingMode, boolean applyExtension, boolean overwrite) {
            this.sample = sample;
            this.format = format;
            this.applyingExtension = applyExtension;
            this.overwriting = overwrite;
            this.namingMode = namingMode;

            if (this.format == null)
                this.format = AudioUtilities.defaultAudioFormat;
            if (namingMode != null) {
                if (!fn.isDirectory())
                    throw new IllegalArgumentException("file argument must be a directory when a naming mode is specified");
                this.fn = new File(fn, AudioUtilities.makeLocalSampleName(sample, sampleName, namingMode));
            } else if (fn == null) {
                this.fn = TempFileManager.getNewTempFile();
                isTemp = true;
            } else {
                if (fn.isDirectory())
                    throw new IllegalArgumentException("file argument cannot be a directory when naming mode unspecified");
                this.fn = fn;
            }

            if (applyingExtension)
                this.fn = ZUtilities.replaceExtension(this.fn, format.getExtension());
        }

        public Integer getIndex() {
            return sample;
        }

        public File getFile() {
            return fn;
        }

        public String getNamingMode() {
            return namingMode;
        }

        public AudioFileFormat.Type getFormat() {
            return format;
        }

        public boolean isApplyingExtension() {
            return applyingExtension;
        }

        public boolean isOverwriting() {
            return overwriting;
        }

        public void zDispose() {
            if (isTemp)
                fn.delete();
        }
    }
}