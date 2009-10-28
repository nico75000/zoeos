package org.tritonus.zuonics.sampled.wave;

import org.tritonus.zuonics.sampled.AbstractAudioChunk;

public interface WaveSmplChunk extends AbstractAudioChunk{
    String AUDIO_FORMAT_PROPERTIES_KEY = "wave_smpl_chunk";

    int SMPTE_FORMAT_NONE = 0;
    int SMPTE_FORMAT_24_FPS = 24;
    int SMPTE_FORMAT_25_FPS = 25;
    int SMPTE_FORMAT_30_FPS_WITH_DROPPING = 29;
    int SMPTE_FORMAT_30_FPS = 30;

    int getManufacturer();

    int getProduct();

    int getSamplePeriod();

    int getMidiUnityNote();

    int getMidiPitchFraction();

    int getSMPTEFormat();

    int getSMPTEOffset();

    int getNumSampleLoops();

    Loop[] getSampleLoops();

    byte[] getSamplerData();

    public interface Loop {
        int TYPE_LOOP_FORWARD = 0;
        int TYPE_LOOP_ALTERNATING = 1;
        int TYPE_LOOP_BACKWARD = 2;

        int getIdentifier();

        int getType();

        int getStart();

        int getEnd();

        int getFraction();

        int getPlayCount();
    }
}
