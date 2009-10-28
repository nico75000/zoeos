package org.tritonus.zuonics.sampled;

import org.tritonus.share.sampled.file.TDataOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import java.io.IOException;

public interface AudioChunkGenerator {
    int LENGTH_NOT_KNOWN = -1;
    void generateChunk(AudioFileFormat.Type fileType, AudioFormat audioFormat, long lLength, TDataOutputStream dos) throws IOException;
}
