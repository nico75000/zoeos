package org.tritonus.zuonics.sampled.aiff;

import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.sampled.file.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import java.io.IOException;

public class AiffAudioFileWriterEx extends AiffAudioFileWriter{
    protected AudioOutputStream getAudioOutputStream(AudioFormat audioFormat,
                                                     long lLengthInBytes,
                                                     AudioFileFormat.Type fileType,
                                                     TDataOutputStream dataOutputStream) throws IOException {
        return new AiffAudioOutputStreamEx(audioFormat, fileType,
                lLengthInBytes,
                dataOutputStream);
    }
}
