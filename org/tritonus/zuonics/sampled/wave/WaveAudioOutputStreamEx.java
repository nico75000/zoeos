package org.tritonus.zuonics.sampled.wave;

import org.tritonus.sampled.file.WaveTool;
import org.tritonus.sampled.file.WaveAudioOutputStream;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TDataOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.tritonus.zuonics.sampled.AbstractChunkyAudioOutputStream;
import org.tritonus.zuonics.sampled.AudioChunkGenerator;
import org.tritonus.zuonics.sampled.AudioChunkGenerator;
import org.tritonus.zuonics.sampled.ChunkTool;
import org.tritonus.zuonics.*;
import org.tritonus.zuonics.sampled.ChunkTool;

public class WaveAudioOutputStreamEx extends AbstractChunkyAudioOutputStream {
    public static int getFormatChunkAdd(int formatCode) {
        int formatChunkAdd = 0;
        if (formatCode == WaveTool.WAVE_FORMAT_GSM610) {
            // space for extra fields
            formatChunkAdd += 2;
        }
        return formatChunkAdd;
    }

    public static int getDecodedSamplesPerBlock(AudioFormat format) {
        int decodedSamplesPerBlock = 1;
        if (WaveTool.getFormatCode(format) == WaveTool.WAVE_FORMAT_GSM610) {
            if (format.getFrameSize() == 33) {
                decodedSamplesPerBlock = 320;
            } else if (format.getFrameSize() == 65) {
                decodedSamplesPerBlock = 320;
            } else {
                // how to retrieve this value here ?
                decodedSamplesPerBlock = (int) (format.getFrameSize() * (320.0f / 65.0f));
            }
        }
        return decodedSamplesPerBlock;
    }

    public static final class WaveRIFFChunkGenerator implements AudioChunkGenerator {
        public void generateChunk(AudioFileFormat.Type fileFormat, AudioFormat format, long lLength, TDataOutputStream dos) throws IOException {
            int formatCode = WaveTool.getFormatCode(format);
            int formatChunkAdd = getFormatChunkAdd(formatCode);
            int dataOffset = WaveTool.DATA_OFFSET + formatChunkAdd;
            if (formatCode != WaveTool.WAVE_FORMAT_PCM) {
                // space for fact chunk
                dataOffset += 4 + WaveTool.CHUNK_HEADER_SIZE;
            }

            // if patching the header, and the length has not been known at first
            // writing of the header, just truncate the size fields, don't throw an exception
            if (lLength != AudioSystem.NOT_SPECIFIED
                    && lLength + dataOffset > 0xFFFFFFFFl) {
                lLength = 0xFFFFFFFFl - dataOffset;
            }

            // chunks must be on word-boundaries
            long lDataChunkSize = lLength + (lLength % 2);

            dos.writeInt(WaveTool.WAVE_RIFF_MAGIC);
            dos.writeLittleEndian32((int) ((lDataChunkSize + dataOffset - WaveTool.CHUNK_HEADER_SIZE)
                    & 0xFFFFFFFF));
            dos.writeInt(WaveTool.WAVE_WAVE_MAGIC);
        }
    }

    public static final class WaveFmtChunkGenerator implements AudioChunkGenerator {
        public void generateChunk(AudioFileFormat.Type fileFormat, AudioFormat format, long lLength, TDataOutputStream dos) throws IOException {
            int formatCode = WaveTool.getFormatCode(format);
            int formatChunkAdd = getFormatChunkAdd(formatCode);
            int formatChunkSize = WaveTool.FMT_CHUNK_SIZE + formatChunkAdd;

            short sampleSizeInBits;
            if (formatCode == WaveTool.WAVE_FORMAT_GSM610)
                sampleSizeInBits = 0; // MS standard
            else
                sampleSizeInBits = (short) format.getSampleSizeInBits();

            int decodedSamplesPerBlock = getDecodedSamplesPerBlock(format);

            int avgBytesPerSec = ((int) format.getSampleRate()) / decodedSamplesPerBlock * format.getFrameSize();
            dos.writeInt(WaveTool.WAVE_FMT_MAGIC);
            dos.writeLittleEndian32(formatChunkSize);
            dos.writeLittleEndian16((short) formatCode);             // wFormatTag
            dos.writeLittleEndian16((short) format.getChannels());   // nChannels
            dos.writeLittleEndian32((int) format.getSampleRate());   // nSamplesPerSec
            dos.writeLittleEndian32(avgBytesPerSec);                 // nAvgBytesPerSec
            dos.writeLittleEndian16((short) format.getFrameSize());  // nBlockalign
            dos.writeLittleEndian16(sampleSizeInBits);               // wBitsPerSample
            dos.writeLittleEndian16((short) formatChunkAdd);         // cbSize

            if (formatCode == WaveTool.WAVE_FORMAT_GSM610) {
                dos.writeLittleEndian16((short) decodedSamplesPerBlock); // wSamplesPerBlock
            }
        }
    }

    public static final class WaveFactChunkGenerator implements AudioChunkGenerator {
        public void generateChunk(AudioFileFormat.Type fileFormat, AudioFormat format, long lLength, TDataOutputStream dos) throws IOException {
            int formatCode = WaveTool.getFormatCode(format);
            if (formatCode != WaveTool.WAVE_FORMAT_PCM) {
                int decodedSamplesPerBlock = getDecodedSamplesPerBlock(format);
                // write "fact" chunk: number of samples
                // todo: add this as an attribute or property
                // in AudioOutputStream or AudioInputStream
                long samples = 0;
                if (lLength != AudioSystem.NOT_SPECIFIED) {
                    samples = lLength / format.getFrameSize() * decodedSamplesPerBlock;
                }
                // saturate sample count
                if (samples > 0xFFFFFFFFl) {
                    samples = (0xFFFFFFFFl / decodedSamplesPerBlock) * decodedSamplesPerBlock;
                }
                dos.writeInt(WaveTool.WAVE_FACT_MAGIC);
                dos.writeLittleEndian32(4);
                dos.writeLittleEndian32((int) (samples & 0xFFFFFFFF));
            }
        }
    }

    public static final class WaveSmplChunkGenerator implements AudioChunkGenerator {
        public void generateChunk(AudioFileFormat.Type fileFormat, AudioFormat format, long lLength, TDataOutputStream dos) throws IOException {
            Object smpl = format.properties().get(WaveSmplChunk.AUDIO_FORMAT_PROPERTIES_KEY);
            if (smpl instanceof WaveSmplChunk) {
                WaveSmplChunk wsc = (WaveSmplChunk) smpl;

                byte[] samplerData = wsc.getSamplerData();
                int numLoops = wsc.getNumSampleLoops();
                int smplChunkSize = ChunkTool.MIN_SMPL_CHUNK_LENGTH + numLoops * ChunkTool.SMPL_LOOP_STRUCTURE_LENGTH + (samplerData.length + samplerData.length % 2);

                dos.writeInt(ChunkTool.WAVE_SMPL_MAGIC);
                dos.writeLittleEndian32(smplChunkSize);

                dos.writeLittleEndian32(wsc.getManufacturer());
                dos.writeLittleEndian32(wsc.getProduct());
                dos.writeLittleEndian32(wsc.getSamplePeriod());
                dos.writeLittleEndian32(wsc.getMidiUnityNote());
                dos.writeLittleEndian32(wsc.getMidiPitchFraction());
                dos.writeLittleEndian32(wsc.getSMPTEFormat());
                dos.writeLittleEndian32(wsc.getSMPTEOffset());
                dos.writeLittleEndian32(wsc.getNumSampleLoops());
                dos.writeLittleEndian32(samplerData.length);

                WaveSmplChunk.Loop[] loops = wsc.getSampleLoops();
                if (loops.length != numLoops)
                    throw new IllegalArgumentException("Invalid WaveSmplChunk: number of loops doesn't match number of loops provided");
                for (int i = 0; i < numLoops; i++) {
                    dos.writeLittleEndian32(loops[i].getIdentifier());
                    dos.writeLittleEndian32(loops[i].getType());
                    dos.writeLittleEndian32(loops[i].getStart());
                    dos.writeLittleEndian32(loops[i].getEnd());
                    dos.writeLittleEndian32(loops[i].getFraction());
                    dos.writeLittleEndian32(loops[i].getPlayCount());
                }
                for (int i = 0; i < samplerData.length; i++)
                    dos.writeByte(samplerData[i]);
                if (samplerData.length % 2 != 0)
                    dos.writeByte(0);
            }
        }
    }

    public static final class WaveDataChunkGenerator implements AudioChunkGenerator {
        public void generateChunk(AudioFileFormat.Type fileFormat, AudioFormat audioFormat, long lLength, TDataOutputStream dos) throws IOException {
            dos.writeInt(WaveTool.WAVE_DATA_MAGIC);
            dos.writeLittleEndian32((lLength != AudioSystem.NOT_SPECIFIED) ? ((int) lLength) : LENGTH_NOT_KNOWN);
        }
    }

    public static final List<AudioChunkGenerator> chunkGeneratorList;

    static {
        List<AudioChunkGenerator> temp = new ArrayList<AudioChunkGenerator>();
        temp.add(new WaveRIFFChunkGenerator());
        temp.add(new WaveFmtChunkGenerator());
        temp.add(new WaveFactChunkGenerator());
        temp.add(new WaveSmplChunkGenerator());
        temp.add(new WaveDataChunkGenerator());
        chunkGeneratorList = Collections.unmodifiableList(temp);
    }

    public WaveAudioOutputStreamEx(AudioFormat audioFormat, long lLength, TDataOutputStream dataOutputStream) {
        super(AudioFileFormat.Type.WAVE, audioFormat, lLength, dataOutputStream, chunkGeneratorList, (useBasic() ? new WaveAudioOutputStream(audioFormat, lLength, dataOutputStream) : null));
    }

    static boolean useBasic() {
        return !ChunkTool.getAssumedTrueBooleanProperty(ChunkTool.ENHANCED_WAVE_WRITING_SYSTEM_PROPERTY);
    }
}
