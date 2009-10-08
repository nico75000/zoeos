package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.system.TempFileManager;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.audio.AudioUtilities;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 06-Dec-2003
 * Time: 10:34:36
 * To change this template use Options | File Templates.
 */
public class Impl_SampleRetrievalInfo implements SampleRetrievalInfo {
    private Integer sample;
    private File fn;
    private AudioFileFormat.Type format;
    private String namingMode = null;
    private boolean applyingExtension = false;
    private boolean overwriting = false;
    //private boolean endOfAProcedure = false;

    private boolean isTemp;
    // get temp File
    /*  public Impl_SampleRetrievalInfo(Integer sample, File fn, boolean endOfAProcedure) {
          try {
              this(null, sample, fn, AudioUtilities.defaultAudioFormat, null, false, true, endOfAProcedure);
          } catch (SampleEmptyException e) { // shouldn't get these because sc is null
          } catch (NoSuchSampleException e) {
          }
      } */

    // get temp File
    public Impl_SampleRetrievalInfo(Integer sample) {
        this(sample, null, null, AudioUtilities.defaultAudioFormat, null, false, true);
    }

    // tempo File in specified format
    public Impl_SampleRetrievalInfo(Integer sample, AudioFileFormat.Type format) {
        this(sample, null, null, format, null, false, true);
    }


    public Impl_SampleRetrievalInfo(Integer sample, File fn, AudioFileFormat.Type format, boolean overwrite) {
        this(sample, null, fn, format, null, true, overwrite);
    }

    public Impl_SampleRetrievalInfo(Integer sample, String sampleName, File fn, AudioFileFormat.Type format, String namingMode, boolean applyExtension, boolean overwrite) {
        this.sample = sample;

        this.format = format;
        if (this.format == null)
            this.format = AudioUtilities.defaultAudioFormat;

        this.applyingExtension = applyExtension;
        this.overwriting = overwrite;

        this.namingMode = namingMode;
        if (namingMode != null) {
            if (!fn.isDirectory())
                throw new IllegalArgumentException("passed File must be a directory when non-null naming mode specified");
            this.fn = new File(fn, AudioUtilities.makeLocalSampleName(sample, sampleName, namingMode));
        } else if (fn == null) {
            this.fn = TempFileManager.getNewTempFile();
            isTemp = true;
        } else {
            if (fn.isDirectory())
                throw new IllegalArgumentException("passed non-null File cannot be a directory when no naming mode specified");
            this.fn = fn;
        }

        if (applyingExtension)
            this.fn = ZUtilities.replaceExtension(this.fn, format.getExtension());
    }

    public Integer getSample() {
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
