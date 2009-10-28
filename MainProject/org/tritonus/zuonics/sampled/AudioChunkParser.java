package org.tritonus.zuonics.sampled;

import org.tritonus.zuonics.sampled.AbstractAudioChunk;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.DataInputStream;
import java.io.IOException;

public interface AudioChunkParser {
    int getMagic();

    // CONTRACT: method should read chunkLength bytes from stream, if it reads less than chunkLength, it should skip remaining
    // if it reads more than chunkLength, an UnsupportedAudioFormatException should be thrown
    AbstractAudioChunk parseChunk(DataInputStream dis,
                                          int chunkLength) throws UnsupportedAudioFileException, IOException;
}
