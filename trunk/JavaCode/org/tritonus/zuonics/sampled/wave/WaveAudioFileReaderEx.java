package org.tritonus.zuonics.sampled.wave;

import org.tritonus.sampled.file.WaveAudioFileReader;
import org.tritonus.sampled.file.WaveTool;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;
import org.tritonus.zuonics.sampled.AbstractChunkyAudioFileReader;
import org.tritonus.zuonics.sampled.AudioChunkParser;
import org.tritonus.zuonics.sampled.ChunkTool;
import org.tritonus.zuonics.sampled.aiff.AiffAudioFileReaderEx;

import javax.sound.sampled.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaveAudioFileReaderEx extends AbstractChunkyAudioFileReader {

    public static final class WaveSmplChunkParser implements AudioChunkParser {
        public int getMagic() {
            return ChunkTool.WAVE_SMPL_MAGIC;
        }

        public AbstractAudioChunk parseChunk(DataInputStream dis,
                                             int chunkLength) throws UnsupportedAudioFileException, IOException {

            if (chunkLength < ChunkTool.MIN_SMPL_CHUNK_LENGTH)
                throw new UnsupportedAudioFileException("corrupt WAVE file: smpl chunk is too small");

            final int manufacturer = readLittleEndianInt(dis);
            final int product = readLittleEndianInt(dis);
            final int samplePeriod = readLittleEndianInt(dis);
            final int midiUnityNote = readLittleEndianInt(dis);
            final int midiPitchFraction = readLittleEndianInt(dis);
            final int SMPTEFormat = readLittleEndianInt(dis);
            final int SMPTEOffset = readLittleEndianInt(dis);
            final int numSampleLoops = readLittleEndianInt(dis);
            final int sizeSamplerData = readLittleEndianInt(dis);
            int extraLength = numSampleLoops * ChunkTool.SMPL_LOOP_STRUCTURE_LENGTH + sizeSamplerData;

            if (extraLength + ChunkTool.MIN_SMPL_CHUNK_LENGTH > chunkLength)
                throw new UnsupportedAudioFileException("corrupt WAVE file: smpl chunk is truncated or invalid");

            final WaveSmplChunk.Loop[] loops = new WaveSmplChunk.Loop[numSampleLoops];
            for (int i = 0; i < numSampleLoops; i++) {
                final int identifier = readLittleEndianInt(dis);
                final int type = readLittleEndianInt(dis);
                final int start = readLittleEndianInt(dis);
                final int end = readLittleEndianInt(dis);
                final int fraction = readLittleEndianInt(dis);
                final int playCount = readLittleEndianInt(dis);
                loops[i] = new WaveSmplChunk.Loop() {
                    public int getIdentifier() {
                        return identifier;
                    }

                    public int getType() {
                        return type;
                    }

                    public int getStart() {
                        return start;
                    }

                    public int getEnd() {
                        return end;
                    }

                    public int getFraction() {
                        return fraction;
                    }

                    public int getPlayCount() {
                        return playCount;
                    }
                };
            }
            final byte[] samplerData = new byte[sizeSamplerData];

            for (int i = 0; i < sizeSamplerData; i++)
                samplerData[i] = dis.readByte();

            ChunkTool.advanceWaveChunk(dis, chunkLength, extraLength + ChunkTool.MIN_SMPL_CHUNK_LENGTH); // Obey interface contract
            return new WaveSmplChunk() {
                public int getManufacturer() {
                    return manufacturer;
                }

                public int getProduct() {
                    return product;
                }

                public int getSamplePeriod() {
                    return samplePeriod;
                }

                public int getMidiUnityNote() {
                    return midiUnityNote;
                }

                public int getMidiPitchFraction() {
                    return midiPitchFraction;
                }

                public int getSMPTEFormat() {
                    return SMPTEFormat;
                }

                public int getSMPTEOffset() {
                    return SMPTEOffset;
                }

                public int getNumSampleLoops() {
                    return numSampleLoops;
                }

                public WaveSmplChunk.Loop[] getSampleLoops() {
                    return (Loop[]) loops.clone();
                }

                public byte[] getSamplerData() {
                    return (byte[]) samplerData.clone();
                }

                public String getAudioFormatPropertyKey() {
                    return WaveSmplChunk.AUDIO_FORMAT_PROPERTIES_KEY;
                }
            };
        }
    }

    public static final class WaveDataChunkParser implements AudioChunkParser {

        public int getMagic() {
            return WaveTool.WAVE_DATA_MAGIC;
        }

        public AbstractAudioChunk parseChunk(DataInputStream dis, final int chunkLength) throws IOException, UnsupportedAudioFileException {
            if (dis.markSupported())
                dis.mark(dis.available());
            ChunkTool.advanceWaveChunk(dis, chunkLength, 0); // Obey interface contract
            return new WaveDataChunk() {
                public int getDataChunkLength() {
                    return chunkLength;
                }

                public String getAudioFormatPropertyKey() {
                    return null;
                }
            };
        }
    }

    public static final class WaveFmtChunkParser implements AudioChunkParser {
        public int getMagic() {
            return WaveTool.WAVE_FMT_MAGIC;
        }

        public AbstractAudioChunk parseChunk(DataInputStream dis,
                                             int chunkLength) throws UnsupportedAudioFileException, IOException {
            String debugAdd = "";

            int read = WaveTool.MIN_FMT_CHUNK_LENGTH;

            if (chunkLength < WaveTool.MIN_FMT_CHUNK_LENGTH) {
                throw new UnsupportedAudioFileException("corrupt WAVE file: format chunk is too small");
            }

            short formatCode = readLittleEndianShort(dis);
            short channelCount = readLittleEndianShort(dis);
            if (channelCount <= 0) {
                throw new UnsupportedAudioFileException("corrupt WAVE file: number of channels must be positive");
            }

            int sampleRate = readLittleEndianInt(dis);
            if (sampleRate <= 0) {
                throw new UnsupportedAudioFileException("corrupt WAVE file: sample rate must be positive");
            }

            int avgBytesPerSecond = readLittleEndianInt(dis);
            int blockAlign = readLittleEndianShort(dis);

            AudioFormat.Encoding encoding;
            int sampleSizeInBits;
            int frameSize = 0;
            float frameRate = (float) sampleRate;

            int cbSize = 0;
            switch (formatCode) {
                case WaveTool.WAVE_FORMAT_PCM:
                    if (chunkLength < WaveTool.MIN_FMT_CHUNK_LENGTH + 2) {
                        throw new UnsupportedAudioFileException("corrupt WAVE file: format chunk is too small");
                    }
                    sampleSizeInBits = readLittleEndianShort(dis);
                    if (sampleSizeInBits <= 0) {
                        throw new UnsupportedAudioFileException("corrupt WAVE file: sample size must be positive");
                    }
                    encoding = (sampleSizeInBits <= 8) ?
                            AudioFormat.Encoding.PCM_UNSIGNED : AudioFormat.Encoding.PCM_SIGNED;
                    if (TDebug.TraceAudioFileReader) {
                        debugAdd += ", wBitsPerSample=" + sampleSizeInBits;
                    }
                    read += 2;
                    break;
                case WaveTool.WAVE_FORMAT_ALAW:
                    sampleSizeInBits = 8;
                    encoding = AudioFormat.Encoding.ALAW;
                    break;
                case WaveTool.WAVE_FORMAT_ULAW:
                    sampleSizeInBits = 8;
                    encoding = AudioFormat.Encoding.ULAW;
                    break;
                case WaveTool.WAVE_FORMAT_GSM610:
                    if (chunkLength < WaveTool.MIN_FMT_CHUNK_LENGTH + 6) {
                        throw new UnsupportedAudioFileException("corrupt WAVE file: extra GSM bytes are missing");
                    }
                    sampleSizeInBits = readLittleEndianShort(dis); // sample Size (is 0 for GSM)
                    cbSize = readLittleEndianShort(dis);
                    if (cbSize < 2) {
                        throw new UnsupportedAudioFileException("corrupt WAVE file: extra GSM bytes are corrupt");
                    }
                    int decodedSamplesPerBlock = readLittleEndianShort(dis) & 0xFFFF; // unsigned
                    if (TDebug.TraceAudioFileReader) {
                        debugAdd += ", wBitsPerSample=" + sampleSizeInBits
                                + ", cbSize=" + cbSize
                                + ", wSamplesPerBlock=" + decodedSamplesPerBlock;
                    }
                    sampleSizeInBits = AudioSystem.NOT_SPECIFIED;
                    encoding = WaveTool.GSM0610;
                    frameSize = blockAlign;
                    frameRate = ((float) sampleRate) / ((float) decodedSamplesPerBlock);
                    read += 6;
                    break;

                case WaveTool.WAVE_FORMAT_IMA_ADPCM:
                    if (chunkLength < WaveTool.MIN_FMT_CHUNK_LENGTH + 2) {
                        throw new UnsupportedAudioFileException("corrupt WAVE file: extra GSM bytes are missing");
                    }
                    sampleSizeInBits = readLittleEndianShort(dis);
                    cbSize = readLittleEndianShort(dis);
                    if (cbSize < 2) {
                        throw new UnsupportedAudioFileException("corrupt WAVE file: extra IMA ADPCM bytes are corrupt");
                    }
                    int samplesPerBlock = readLittleEndianShort(dis) & 0xFFFF; // unsigned
                    if (TDebug.TraceAudioFileReader) {
                        debugAdd += ", wBitsPerSample=" + sampleSizeInBits
                                + ", cbSize=" + cbSize
                                + ", wSamplesPerBlock=" + samplesPerBlock;
                    }
                    sampleSizeInBits = AudioSystem.NOT_SPECIFIED;
                    encoding = WaveTool.GSM0610;
                    frameSize = blockAlign;
                    frameRate = ((float) sampleRate) / ((float) samplesPerBlock);
                    read += 6;
                    break;

                default:
                    throw new UnsupportedAudioFileException("unsupported WAVE file: unknown format code " + formatCode);
            }
            // if frameSize isn't set, calculate it (the default)
            if (frameSize == 0) {
                frameSize = calculateFrameSize(sampleSizeInBits, channelCount);
            }

            if (TDebug.TraceAudioFileReader) {
                TDebug.out("WaveAudioFileReader.readFormatChunk():");
                TDebug.out("  read values: wFormatTag=" + formatCode
                        + ", nChannels=" + channelCount
                        + ", nSamplesPerSec=" + sampleRate
                        + ", nAvgBytesPerSec=" + avgBytesPerSecond
                        + ", nBlockAlign==" + blockAlign
                        + debugAdd);
                TDebug.out("  constructed values: "
                        + "encoding=" + encoding
                        + ", sampleRate=" + ((float) sampleRate)
                        + ", sampleSizeInBits=" + sampleSizeInBits
                        + ", channels=" + channelCount
                        + ", frameSize=" + frameSize
                        + ", frameRate=" + frameRate);
            }

            // go to next chunk
            //advanceChunk(dis, chunkLength, read);
            final AudioFormat.Encoding f_encoding = encoding;
            final int f_sampleSizeInBits = sampleSizeInBits;
            final int f_channelCount = channelCount;
            final int f_frameSize = frameSize;
            final float f_frameRate = frameRate;
            ChunkTool.advanceWaveChunk(dis, chunkLength, read); // Obey interface contract
            return new WaveFmtChunk() {
                public AudioFormat.Encoding getEncoding() {
                    return f_encoding;
                }

                public int getSampleSizeInBits() {
                    return f_sampleSizeInBits;
                }

                public int getChannelCount() {
                    return f_channelCount;
                }

                public int getFrameSize() {
                    return f_frameSize;
                }

                public float getFrameRate() {
                    return f_frameRate;
                }

                public String getAudioFormatPropertyKey() {
                    return null;
                }
            };
        }
    }

    public static final Map<Integer, AudioChunkParser> chunkParserMap;

    static {
        Map<Integer, AudioChunkParser> temp = new HashMap<Integer, AudioChunkParser>();
        ChunkTool.addParser(new WaveFmtChunkParser(), temp);
        ChunkTool.addParser(new WaveDataChunkParser(), temp);
        ChunkTool.addParser(new WaveSmplChunkParser(), temp);
        chunkParserMap = Collections.unmodifiableMap(temp);
    }

    public WaveAudioFileReaderEx() {
        super((useBasic() ? new WaveAudioFileReader() : null));
    }

    static boolean useBasic() {
        return !ChunkTool.getAssumedTrueBooleanProperty(ChunkTool.ENHANCED_WAVE_PARSING_SYSTEM_PROPERTY);
    }

    protected void advanceChunk(DataInputStream dis, long chunkLength, long chunkRead) throws IOException, UnsupportedAudioFileException {
        ChunkTool.advanceWaveChunk(dis, chunkLength, chunkRead);
    }

    protected int readChunkLength(DataInputStream dis) throws IOException {
        return readLittleEndianInt(dis) & 0xFFFFFFFF; // unsigned;
    }

    protected void preliminaryCheck(InputStream is, int readLimit) throws IOException, UnsupportedAudioFileException {
        DataInputStream dis = new DataInputStream(is);
        int magic = dis.readInt();
        if (magic != WaveTool.WAVE_RIFF_MAGIC) {
            throw new UnsupportedAudioFileException("not a WAVE file: wrong header magic");
        }
        long totalLength = readLittleEndianInt(dis) & 0xFFFFFFFF; // unsigned
        magic = dis.readInt();
        if (magic != WaveTool.WAVE_WAVE_MAGIC) {
            throw new UnsupportedAudioFileException("not a WAVE file: wrong header magic");
        }
    }

    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        DataInputStream dis = new DataInputStream(inputStream);
        //if (!dis.markSupported())
        //   throw new UnsupportedAudioFileException("can't parse WAVE file: stream marking not supported");

        int magic = dis.readInt();
        if (magic != WaveTool.WAVE_RIFF_MAGIC) {
            throw new UnsupportedAudioFileException("not a WAVE file: wrong header magic");
        }
        long totalLength = readLittleEndianInt(dis) & 0xFFFFFFFF; // unsigned
        magic = dis.readInt();
        if (magic != WaveTool.WAVE_WAVE_MAGIC) {
            throw new UnsupportedAudioFileException("not a WAVE file: wrong header magic");
        }
        List<AbstractAudioChunk> chunks = parseChunks(dis, chunkParserMap);
        WaveFmtChunk wfc = (WaveFmtChunk) findChunk(chunks, WaveFmtChunk.class);
        WaveDataChunk wdc = (WaveDataChunk) findChunk(chunks, WaveDataChunk.class);
        if (wfc == null || wdc == null) {
            UnsupportedAudioFileException e = new UnsupportedAudioFileException("unsupported WAVE file: required chunk not found.");
            if (TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
            throw e;
        }
        if (dis.markSupported())
            dis.reset(); // resets to beginning of data chunk  (data chunk parser should have set mark!)

        Map<String, Object> m = makeChunkProperties(chunks);
        TAudioFormat format = new TAudioFormat(wfc.getEncoding(),
                wfc.getFrameRate(),
                wfc.getSampleSizeInBits(),
                wfc.getChannelCount(),
                wfc.getFrameSize(),
                wfc.getFrameRate(),
                false, m);
        long frameLength = wdc.getDataChunkLength() / format.getFrameSize();
        return new TAudioFileFormat(AudioFileFormat.Type.WAVE,
                format,
                (int) frameLength,
                (int) (totalLength + WaveTool.CHUNK_HEADER_SIZE));
    }

    public static final void main(String[] args) {
        WaveAudioFileReaderEx reader = new WaveAudioFileReaderEx();
        AiffAudioFileReaderEx aiff_reader = new AiffAudioFileReaderEx();
        try {
            AudioInputStream ais;
            try {
                ais = aiff_reader.getAudioInputStream(new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\tester.wav"));
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //  AudioFileFormat aff = getReader.getAudioFileFormat(new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\tester.wav"));
            ais = reader.getAudioInputStream(new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\tester.wav"));
            //AudioFileFormat aff = getReader.getAudioFileFormat(new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\tester.wav"));
            new WaveAudioFileWriterEx().write(ais, AudioFileFormat.Type.WAVE, new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\tester-proc.wav"));
            Map m = ais.getFormat().properties();
            Object o = m.get(WaveSmplChunk.AUDIO_FORMAT_PROPERTIES_KEY);
            int loops = 0;
            if (o instanceof WaveSmplChunk) {
                loops = ((WaveSmplChunk) o).getNumSampleLoops();
            }
            System.out.println("Num sample loops = " + loops);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
