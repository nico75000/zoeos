package org.tritonus.zuonics.sampled.wave;

import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.sampled.file.WaveAudioFileWriter;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

public class WaveAudioFileWriterEx extends WaveAudioFileWriter {
    protected AudioOutputStream getAudioOutputStream(AudioFormat audioFormat,
                                                     long lLengthInBytes,
                                                     AudioFileFormat.Type fileType,
                                                     TDataOutputStream dataOutputStream) throws IOException {
        return new WaveAudioOutputStreamEx(audioFormat,
                lLengthInBytes,
                dataOutputStream);
    }
}
