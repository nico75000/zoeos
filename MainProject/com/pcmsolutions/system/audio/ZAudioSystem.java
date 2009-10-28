package com.pcmsolutions.system.audio;

import com.pcmsolutions.system.preferences.Impl_ZIntPref;
import com.pcmsolutions.system.preferences.ZIntPref;
import org.tritonus.sampled.file.AuAudioOutputStream;
import org.tritonus.share.sampled.AudioSystemShadow;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.zuonics.sampled.aiff.AiffAudioFileReaderEx;
import org.tritonus.zuonics.sampled.aiff.AiffAudioFileWriterEx;
import org.tritonus.zuonics.sampled.aiff.AiffAudioOutputStreamEx;
import org.tritonus.zuonics.sampled.wave.WaveAudioFileReaderEx;
import org.tritonus.zuonics.sampled.wave.WaveAudioFileWriterEx;
import org.tritonus.zuonics.sampled.wave.WaveAudioOutputStreamEx;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 28-May-2004
 * Time: 22:01:08
 */
public class ZAudioSystem {
    private static final AudioFileFormat.Type[] types;
    static {
        types = new AudioFileFormat.Type[]{AudioFileFormat.Type.WAVE, AudioFileFormat.Type.AIFF, AudioFileFormat.Type.AU};
    }

    public static AudioFileFormat.Type[] getAudioTypes() {
        return (AudioFileFormat.Type[]) types.clone();
    }

    private static final Preferences prefs = Preferences.userNodeForPackage(ZAudioSystem.class);
    public static final ZIntPref ZPREF_defaultAudioType = new Impl_ZIntPref(prefs, "defaultAudioFormat", AudioUtilities.getAudioTypeIndexForExtension(AudioUtilities.defaultAudioFormat.getExtension()));

    public static AudioFileFormat.Type getDefaultAudioType() {
        int t = ZPREF_defaultAudioType.getValue();
        if (t < 0)
            t = 0;
        if (t >= types.length)
            t = types.length - 1;
        return types[t];
    }

    public static AudioInputStream getAudioInputStream(File f) throws IOException, UnsupportedAudioFileException {
        try {
            return new WaveAudioFileReaderEx().getAudioInputStream(f);
        } catch (UnsupportedAudioFileException e) {
        }

        try {
            return new AiffAudioFileReaderEx().getAudioInputStream(f);
        } catch (UnsupportedAudioFileException e) {
        }

        return AudioSystem.getAudioInputStream(f);
    }

    public static int write(AudioInputStream ais, AudioFileFormat.Type type, File f) throws IOException {
        try {
            return new WaveAudioFileWriterEx().write(ais, type, f);
        } catch (IllegalArgumentException e) {
        }

        try {
            return new AiffAudioFileWriterEx().write(ais, type, f);
        } catch (IllegalArgumentException e) {
        }
        return AudioSystem.write(ais, type, f);
    }

// TODO: lLengthInBytes actually should be lLengthInFrames (design problem of A.O.S.)
    public static AudioOutputStream getAudioOutputStream(AudioFileFormat.Type type, AudioFormat audioFormat, long lLengthInBytes, TDataOutputStream dataOutputStream) {
        AudioOutputStream audioOutputStream = null;

        if (type.equals(AudioFileFormat.Type.AIFF) ||
                type.equals(AudioFileFormat.Type.AIFF)) {
            audioOutputStream = new AiffAudioOutputStreamEx(audioFormat, type, lLengthInBytes, dataOutputStream);
        } else if (type.equals(AudioFileFormat.Type.AU)) {
            audioOutputStream = new AuAudioOutputStream(audioFormat, lLengthInBytes, dataOutputStream);
        } else if (type.equals(AudioFileFormat.Type.WAVE)) {
            audioOutputStream = new WaveAudioOutputStreamEx(audioFormat, lLengthInBytes, dataOutputStream);
        }
        return audioOutputStream;
    }

    public static AudioOutputStream getAudioOutputStream(AudioFileFormat.Type type, AudioFormat audioFormat, long lLengthInBytes, OutputStream outputStream)
            throws IOException {
        TDataOutputStream dataOutputStream = AudioSystemShadow.getDataOutputStream(outputStream);
        AudioOutputStream audioOutputStream = getAudioOutputStream(type, audioFormat, lLengthInBytes, dataOutputStream);
        return audioOutputStream;
    }

}
