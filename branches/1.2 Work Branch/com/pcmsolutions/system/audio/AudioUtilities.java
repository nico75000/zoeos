package com.pcmsolutions.system.audio;

import com.pcmsolutions.system.ZUtilities;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Dec-2003
 * Time: 13:10:49
 * To change this template use Options | File Templates.
 */
public class AudioUtilities {
   // public static final ZStringPref ZPREF_defaultPlaybackDevice = new Impl_ZStringPref(preferences.userNodeForPackage(AudioUtilities.class), "defaultPlaybackDevice", "");
    public static AudioFileFormat.Type defaultAudioFormat = AudioFileFormat.Type.WAVE;

    public static final int maxClipLen = 1024 * 1204 * 16;

    public static final String SAMPLE_NAMING_MODE_SIN = "SIN";
    public static final String SAMPLE_NAMING_MODE_IN = "IN";
    public static final String SAMPLE_NAMING_MODE_SI = "SI";
    public static final String SAMPLE_NAMING_MODE_I = "I";
    public static final String SAMPLE_NAMING_MODE_NI = "NI";
    public static final String SAMPLE_NAMING_MODE_NSI = "NSI";
    public static final String SAMPLE_NAMING_MODE_N = "N";

    public static final Pattern sampleIndexPattern = Pattern.compile("((_[0-9]{4})|([0-9]{4}_))");

    public static final String sampleIndexPrefix = "s";
    private static final DecimalFormat snFormatter = new DecimalFormat("0000");

    private static final AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();

    public static String makeLocalSampleName(Integer sample, String mode) {
        if (mode.equals(SAMPLE_NAMING_MODE_SI))
            return sampleIndexPrefix + snFormatter.format(sample);
        else if (mode.equals(SAMPLE_NAMING_MODE_I))
            return snFormatter.format(sample);
        else
            throw new IllegalArgumentException("illegal format");
    }

    public static boolean isLegalAudio(String name) {
        return isLegalAudioExtension(ZUtilities.getExtension(name));
    }

   /* public static int getDefaultMixerIndex() {
        return ZUtilities.getIndexForString(AudioSystem.getMixerInfo(), ZPREF_defaultPlaybackDevice.getValue());
    }

    public static Mixer.Info getDefaultMixer() {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        int i = getDefaultMixerIndex();

        if (i != -1)
            return infos[i];

        if (infos.length > 0)
            return AudioSystem.getMixerInfo()[0];

        return null;
    }
     */
    public static boolean isLegalAudioExtension(String ext) {
        for (int i = 0; i < types.length; i++)
            if (types[i].getExtension().toLowerCase().equals(ext.toLowerCase()))
                return true;
        return false;
    }

    public static String getLegalAudioExtensionsString() {
        return getLegalAudioExtensionsString(",");
    }

    public static List filterLegalAudioFiles(final List files) {
        final List legalFiles = new ArrayList();
        File f;
        for (Iterator i = files.iterator(); i.hasNext();) {
            f = (File) i.next();
            if (isLegalAudio(f.getName()))
                legalFiles.add(f);
        }
        return legalFiles;
    }

    public static String getLegalAudioExtensionsString(String sep) {
        String outStr = "";
        for (int i = 0; i < types.length; i++)
            if (outStr.equals(""))
                outStr = types[i].getExtension();
            else
                outStr += sep + " " + types[i].getExtension();
        return outStr;
    }

    public static String makeLocalSampleName(Integer sample, String name, String mode) {
        if (mode.equals(SAMPLE_NAMING_MODE_N))
            return name;
        else if (mode.equals(SAMPLE_NAMING_MODE_SIN))
            return sampleIndexPrefix + snFormatter.format(sample) + ZUtilities.STRING_FIELD_SEPERATOR + name;
        else if (mode.equals(SAMPLE_NAMING_MODE_NI))
            return name + ZUtilities.STRING_FIELD_SEPERATOR + snFormatter.format(sample);
        else if (mode.equals(SAMPLE_NAMING_MODE_NSI))
            return name + ZUtilities.STRING_FIELD_SEPERATOR + sampleIndexPrefix + snFormatter.format(sample);
        else if (mode.equals(SAMPLE_NAMING_MODE_IN))
            return snFormatter.format(sample) + ZUtilities.STRING_FIELD_SEPERATOR + name;
        else
            return makeLocalSampleName(sample, mode);
    }

    public static int getAudioTypeIndexForExtension(String ext) {
        for (int i = 0; i < types.length; i++)
            if (types[i].getExtension().equals(ext))
                return i;
        return -1;
    }

    public static AudioFileFormat.Type getAudioTypeForExtension(String ext) {
        for (int i = 0; i < types.length; i++)
            if (types[i].getExtension().equals(ext))
                return types[i];
        return null;
    }
}
