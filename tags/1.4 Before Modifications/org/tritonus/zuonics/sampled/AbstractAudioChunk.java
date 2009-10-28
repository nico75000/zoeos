package org.tritonus.zuonics.sampled;

public interface AbstractAudioChunk {
    // return value of null indicates the chunk should not be exposed as an audio format property (e.g wave 'fmt' and 'data' chunks)
    String getAudioFormatPropertyKey();
}
