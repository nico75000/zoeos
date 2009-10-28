package com.pcmsolutions.system.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.util.Map;

/**
 * User: paulmeehan
 * Date: 24-Feb-2004
 * Time: 21:56:04
 */
public class AudioConverter {
    private static final float DELTA = 1E-9F;
    private static boolean DEBUG = true;

    private static void out(String strMessage) {
        System.out.println(strMessage);
    }

    public static AudioFormat getWaveCompatibleAudioFormat(AudioFormat af){
        return new AudioFormat((af.getSampleSizeInBits() == 8) ?
                        AudioFormat.Encoding.PCM_UNSIGNED :
                        AudioFormat.Encoding.PCM_SIGNED,af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels(), af.getFrameSize(), af.getFrameRate(), false, af.properties());
    }
     public static AudioFormat getAiffCompatibleAudioFormat(AudioFormat af){
        return new AudioFormat((af.getSampleSizeInBits() == 8) ?
                        AudioFormat.Encoding.PCM_UNSIGNED :
                        AudioFormat.Encoding.PCM_SIGNED,af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels(), af.getFrameSize(), af.getFrameRate(), true, af.properties());
    }
    public static AudioInputStream convertStreamToWaveCompatible(AudioInputStream stream) throws AudioConversionException {
         AudioFormat af = stream.getFormat();
        return convertStream(stream,getWaveCompatibleAudioFormat(af));
    }

    public static AudioInputStream convertStreamToAiffCompatible(AudioInputStream stream) throws AudioConversionException {
            AudioFormat af = stream.getFormat();
           return convertStream(stream,getAiffCompatibleAudioFormat(af));
       }

    public static AudioInputStream convertStream(AudioInputStream stream, AudioFormat desiredFormat) throws AudioConversionException {
        try {
            AudioFormat format = stream.getFormat();

            /* Step 1: convert to PCM, if necessary.
            */
            System.out.println(AudioSystem.isConversionSupported(desiredFormat, stream.getFormat()));
            if (!isPCM(format.getEncoding())) {
                if (DEBUG) out("converting to PCM...");
                /* The following is a heuristics: normally (but not always),
                   8 bit audio data are unsigned, while 16 bit data are signed.
                */
                AudioFormat.Encoding targetEncoding =
                        (format.getSampleSizeInBits() == 8) ?
                        AudioFormat.Encoding.PCM_UNSIGNED :
                        AudioFormat.Encoding.PCM_SIGNED;
                stream = convertEncoding(targetEncoding, stream);
                if (DEBUG) out("stream: " + stream);
                if (DEBUG) out("format: " + stream.getFormat());
            }

            /* Step 2: convert number of channels, if necessary.
            */
            if (stream.getFormat().getChannels() != desiredFormat.getChannels()) {
                if (DEBUG) out("converting channels...");
                stream = convertChannels(desiredFormat.getChannels(), stream);
                if (DEBUG) out("stream: " + stream);
                if (DEBUG) out("format: " + stream.getFormat());
            }

            /* Step 3: convert sample size and endianess, if necessary.
            */
            boolean bDoConvertSampleSize =
                    (stream.getFormat().getSampleSizeInBits() != desiredFormat.getSampleSizeInBits());
            boolean bDoConvertEndianess =
                    (stream.getFormat().isBigEndian() != desiredFormat.isBigEndian());
            if (bDoConvertSampleSize || bDoConvertEndianess) {
                if (DEBUG && bDoConvertSampleSize) out("converting sample size ...");
                if (DEBUG && bDoConvertEndianess) out("converting endianess ...");
                stream = convertPCMSampleSizeAndEndianess(desiredFormat.getSampleSizeInBits(), desiredFormat.getEncoding(),
                        desiredFormat.isBigEndian(), stream);
                if (DEBUG) out("stream: " + stream);
                if (DEBUG) out("format: " + stream.getFormat());
            }

            /* Step 4: convert sample rate, if necessary.
            */
            if (!equals(stream.getFormat().getSampleRate(), desiredFormat.getSampleRate())) {
                if (DEBUG) out("converting sample rate...");
                stream = convertSampleRate(desiredFormat.getSampleRate(), stream);
                if (DEBUG) out("stream: " + stream);
                if (DEBUG) out("format: " + stream.getFormat());
            }
             return new AudioInputStream(stream, desiredFormat, stream.getFrameLength());
            //return stream;
        } catch (Exception e) {
            throw new AudioConversionException(e.getMessage());
        }
    }

    private static boolean isPCM(AudioFormat.Encoding afe) {
        return afe.equals(AudioFormat.Encoding.PCM_SIGNED) || afe.equals(AudioFormat.Encoding.PCM_UNSIGNED);
    }

    private static AudioInputStream convertEncoding(
            AudioFormat.Encoding targetEncoding,
            AudioInputStream sourceStream) {
        return AudioSystem.getAudioInputStream(targetEncoding,
                sourceStream);
    }


    private static AudioInputStream convertChannels(
            int nChannels,
            AudioInputStream sourceStream) {
        AudioFormat sourceFormat = sourceStream.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                sourceFormat.getEncoding(),
                sourceFormat.getSampleRate(),
                sourceFormat.getSampleSizeInBits(),
                nChannels,
                calculatePCMFrameSize(nChannels,
                        sourceFormat.getSampleSizeInBits()),
                sourceFormat.getFrameRate(),
                sourceFormat.isBigEndian(), sourceFormat.properties());
        return AudioSystem.getAudioInputStream(targetFormat,
                sourceStream);
    }


    private static AudioInputStream convertPCMSampleSizeAndEndianess(
            int nSampleSizeInBits,
            AudioFormat.Encoding enc,
            boolean bBigEndian,
            AudioInputStream sourceStream) {
        AudioFormat sourceFormat = sourceStream.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                enc
                /*sourceFormat.getEncoding()*/,
                sourceFormat.getSampleRate(),
                nSampleSizeInBits,
                sourceFormat.getChannels(),
                calculatePCMFrameSize(sourceFormat.getChannels(),
                        nSampleSizeInBits),
                sourceFormat.getFrameRate(),
                bBigEndian,sourceFormat.properties());
        return AudioSystem.getAudioInputStream(targetFormat,
                sourceStream);
    }


    private static AudioInputStream convertSampleRate(
            float fSampleRate,
            AudioInputStream sourceStream) {
        AudioFormat sourceFormat = sourceStream.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                sourceFormat.getEncoding(),
                fSampleRate,
                sourceFormat.getSampleSizeInBits(),
                sourceFormat.getChannels(),
                sourceFormat.getFrameSize(),
                fSampleRate,
                sourceFormat.isBigEndian(),sourceFormat.properties());
        return AudioSystem.getAudioInputStream(targetFormat,
                sourceStream);
    }


    public static int calculatePCMFrameSize(int nChannels, int nSampleSizeInBits) {
        return ((nSampleSizeInBits + 7) / 8) * nChannels;
    }


    /** Compares two float values for equality.
     */
    private static boolean equals(float f1, float f2) {
        return (Math.abs(f1 - f2) < DELTA);
    }
}
