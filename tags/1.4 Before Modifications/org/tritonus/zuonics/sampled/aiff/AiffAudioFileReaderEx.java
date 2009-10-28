package org.tritonus.zuonics.sampled.aiff;

import org.tritonus.sampled.file.AiffAudioFileReader;
import org.tritonus.sampled.file.AiffTool;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.Encodings;
import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;
import org.tritonus.zuonics.sampled.AbstractChunkyAudioFileReader;
import org.tritonus.zuonics.sampled.AudioChunkParser;
import org.tritonus.zuonics.sampled.ChunkTool;
import org.tritonus.zuonics.sampled.wave.WaveAudioFileReaderEx;

import javax.sound.sampled.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiffAudioFileReaderEx extends AbstractChunkyAudioFileReader {

    public static final class AiffINSTChunkParser implements AudioChunkParser {
        public int getMagic() {
            return ChunkTool.AIFF_INST_MAGIC;
        }

        public AbstractAudioChunk parseChunk(DataInputStream dis, int chunkLength) throws UnsupportedAudioFileException, IOException {
            if (chunkLength < ChunkTool.INST_CHUNK_LENGTH)
                throw new UnsupportedAudioFileException("corrupt AIFF file: inst chunk is too small");

            final int baseNote = dis.readByte();
            final int detune = dis.readByte();
            final int lowNote = dis.readByte();
            final int highNote = dis.readByte();
            final int lowVelocity = dis.readByte();
            final int highVelocity = dis.readByte();
            final int gain = dis.readShort();

            final int susPlayMode = dis.readShort();
            final int susBeginLoop = dis.readShort();
            final int susEndLoop = dis.readShort();

            final AiffINSTChunk.Loop sustainLoop = new AiffINSTChunk.Loop() {
                public int getPlayMode() {
                    return susPlayMode;
                }

                public int getBeginMarkerId() {
                    return susBeginLoop;
                }

                public int getEndMarkerId() {
                    return susEndLoop;
                }
            };
            final int relPlayMode = dis.readShort();
            final int relBeginLoop = dis.readShort();
            final int relEndLoop = dis.readShort();
            final AiffINSTChunk.Loop releaseLoop = new AiffINSTChunk.Loop() {
                public int getPlayMode() {
                    return relPlayMode;
                }

                public int getBeginMarkerId() {
                    return relBeginLoop;
                }

                public int getEndMarkerId() {
                    return relEndLoop;
                }
            };
            ChunkTool.advanceAiffChunk(dis, chunkLength, 20); // Obey interface contract
            return new AiffINSTChunk() {
                public int getBaseNote() {
                    return baseNote;
                }

                public int getDetune() {
                    return detune;
                }

                public int getLowNote() {
                    return lowNote;
                }

                public int getHighNote() {
                    return highNote;
                }

                public int getlowVelocity() {
                    return lowVelocity;
                }

                public int getHighVelocity() {
                    return highVelocity;
                }

                public int getGain() {
                    return gain;
                }

                public AiffINSTChunk.Loop getSustainLoop() {
                    return sustainLoop;
                }

                public AiffINSTChunk.Loop getReleaseLoop() {
                    return releaseLoop;
                }

                public String getAudioFormatPropertyKey() {
                    return AiffINSTChunk.AUDIO_FORMAT_PROPERTIES_KEY;
                }
            };
        }
    }

    public static final class AiffMARKChunkParser implements AudioChunkParser {
        public int getMagic() {
            return ChunkTool.AIFF_MARK_MAGIC;
        }

        public AbstractAudioChunk parseChunk(DataInputStream dis, int chunkLength) throws UnsupportedAudioFileException, IOException {
            if (chunkLength < ChunkTool.MIN_MARK_CHUNK_LENGTH)
                throw new UnsupportedAudioFileException("corrupt AIFF file: mark chunk is too small");

            final int numMarkers = dis.readShort();
            final AiffMARKChunk.Marker[] markers = new AiffMARKChunk.Marker[numMarkers];
            long read = ChunkTool.MIN_MARK_CHUNK_LENGTH;
            for (int i = 0; i < numMarkers; i++) {
                final int id = dis.readShort();
                final int position = dis.readInt();
                byte strLen = dis.readByte(); // PASCAL style string - first byte is string length
                byte[] strBytes = new byte[strLen];
                dis.read(strBytes);
                read += 7 + strLen;
                if ((strLen + 1) % 2 != 0) {
                    dis.readByte(); // pad byte
                    read++;
                }
                final String name = new String(strBytes);
                markers[i] = new AiffMARKChunk.Marker() {
                    public int getID() {
                        return id;
                    }

                    public int getPosition() {
                        return position;
                    }

                    public String getName() {
                        return name;
                    }
                };
            }
            ChunkTool.advanceAiffChunk(dis, chunkLength, read); // Obey interface contract
            return new AiffMARKChunk() {
                public int getNumMarkers() {
                    return numMarkers;
                }

                public AiffMARKChunk.Marker[] getMarkers() {
                    return (Marker[]) markers.clone();
                }

                public String getAudioFormatPropertyKey() {
                    return AiffMARKChunk.AUDIO_FORMAT_PROPERTIES_KEY;
                }
            };
        }
    }

    public static final class AiffFVERChunkParser implements AudioChunkParser {
        public int getMagic() {
            return AiffTool.AIFF_FVER_MAGIC;
        }

        public AbstractAudioChunk parseChunk(DataInputStream dis, int chunkLength) throws UnsupportedAudioFileException, IOException {
            if (chunkLength < ChunkTool.MIN_FVER_CHUNK_LENGTH) {
                throw new UnsupportedAudioFileException("Corrput AIFF file: FVER chunk too small.");
            }
            final int fVer = dis.readInt();
            /*if (nVer!=AiffTool.AIFF_FVER_TIME_STAMP) {
                throw new UnsupportedAudioFileException(
                    "Unsupported AIFF file: version not known.");
            } */
            ChunkTool.advanceAiffChunk(dis, chunkLength, 4);
            return new AiffFVERChunk() {
                public String getAudioFormatPropertyKey() {
                    return null;
                }

                public int getFormatVersion() {
                    return fVer;
                }
            };
        }
    }

    public static final class AiffCOMMChunkParser implements AudioChunkParser {
        public int getMagic() {
            return AiffTool.AIFF_COMM_MAGIC;
        }

        public AbstractAudioChunk parseChunk(DataInputStream dis, int chunkLength) throws UnsupportedAudioFileException, IOException {
            if (chunkLength < ChunkTool.COMM_CHUNK_LENGTH)
                throw new UnsupportedAudioFileException("Corrput AIFF file: COMM chunk too small.");

            final int numChannels = dis.readShort();
            if (numChannels <= 0) {
                throw new UnsupportedAudioFileException("not an AIFF file: number of channels must be positive");
            }
            if (TDebug.TraceAudioFileReader) {
                TDebug.out("Found " + numChannels + " channels.");
            }
            // ignored: frame count
            dis.readInt();
            int sampleSize = dis.readShort();
            final float sampleRate = (float) readIeeeExtended(dis);
            if (sampleRate <= 0.0) {
                throw new UnsupportedAudioFileException("not an AIFF file: sample rate must be positive");
            }
            if (TDebug.TraceAudioFileReader) {
                TDebug.out("Found framerate " + sampleRate);
            }
            AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
            int nRead = 18;
            if (chunkLength > nRead) {
                int nEncoding = dis.readInt();
                nRead += 4;
                if (nEncoding == AiffTool.AIFF_COMM_PCM) {
                    // PCM - nothing to do
                } else if (nEncoding == AiffTool.AIFF_COMM_ULAW) {
                    // ULAW
                    encoding = AudioFormat.Encoding.ULAW;
                    sampleSize = 8;
                } else if (nEncoding == AiffTool.AIFF_COMM_IMA_ADPCM) {
                    encoding = Encodings.getEncoding("IMA_ADPCM");
                    sampleSize = 4;
                } else {
                    throw new UnsupportedAudioFileException("Encoding 0x" + Integer.toHexString(nEncoding)
                            + " of AIFF file not supported");
                }
            }
            /* In case of IMA ADPCM, frame size is 0.5 bytes (since it is
               always mono). A value of 1 as frame size would be wrong.
               Handling of frame size 0 in defined nowhere. So the best
               solution is to set the frame size to unspecified (-1).
            */
            final int frameSize = (sampleSize == 4) ?
                    AudioSystem.NOT_SPECIFIED :
                    calculateFrameSize(sampleSize, numChannels);
            if (TDebug.TraceAudioFileReader) {
                TDebug.out("calculated frame size: " + frameSize);
            }
            ChunkTool.advanceAiffChunk(dis, chunkLength, nRead);       // Obey interface contract
            AudioFormat format = new AudioFormat(encoding,
                    sampleRate,
                    sampleSize,
                    numChannels,
                    frameSize,
                    sampleRate,
                    true);
            final int f_sampleSize = sampleSize;
            final AudioFormat.Encoding f_encoding = encoding;
            return new AiffCOMMChunk() {
                public String getAudioFormatPropertyKey() {
                    return null;
                }

                public int getNumChannels() {
                    return numChannels;
                }

                public int getFrameSize() {
                    return frameSize;
                }

                public int getSampleSize() {
                    return f_sampleSize;
                }

                public float getSampleRate() {
                    return sampleRate;
                }

                public AudioFormat.Encoding getEncoding() {
                    return f_encoding;
                }
            };
        }
    }

    public static final class AiffSSNDChunkParser implements AudioChunkParser {
        public int getMagic() {
            return AiffTool.AIFF_SSND_MAGIC;
        }

        public AbstractAudioChunk parseChunk(DataInputStream dis, final int chunkLength) throws UnsupportedAudioFileException, IOException {
            if (chunkLength < 8)
                throw new UnsupportedAudioFileException("Corrput AIFF file: SSND chunk too small.");
            final int offset = dis.readInt();
            final int blockSize = dis.readInt();
            if (dis.markSupported())
                dis.mark(dis.available());
            ChunkTool.advanceAiffChunk(dis, chunkLength, 8); // Obey interface contract
            return new AiffSSNDChunk() {
                public String getAudioFormatPropertyKey() {
                    return null;
                }

                public int getOffset() {
                    return offset;
                }

                public int getBlockSize() {
                    return blockSize;
                }

                public int getDataLength() {
                    return (int) (chunkLength - 8);
                }
            };
        }
    }

    public static final Map<Integer, AudioChunkParser> chunkParserMap;

    static {
        Map<Integer, AudioChunkParser> temp = new HashMap<Integer, AudioChunkParser>();
        ChunkTool.addParser(new AiffFVERChunkParser(), temp);
        ChunkTool.addParser(new AiffCOMMChunkParser(), temp);
        ChunkTool.addParser(new AiffSSNDChunkParser(), temp);
        ChunkTool.addParser(new AiffINSTChunkParser(), temp);
        ChunkTool.addParser(new AiffMARKChunkParser(), temp);
        chunkParserMap = Collections.unmodifiableMap(temp);
    }

    public AiffAudioFileReaderEx() {
        super((useBasic() ? new AiffAudioFileReader() : null));
    }

    static boolean useBasic() {
        return !ChunkTool.getAssumedTrueBooleanProperty(ChunkTool.ENHANCED_AIFF_PARSING_SYSTEM_PROPERTY);
    }

    protected void advanceChunk(DataInputStream dataInputStream, long chunkLength, long chunkRead)
            throws IOException, UnsupportedAudioFileException {
        ChunkTool.advanceAiffChunk(dataInputStream, chunkLength, chunkRead);
    }

    protected int readChunkLength(DataInputStream dis) throws IOException {
        return dis.readInt();
    }

    protected void preliminaryCheck(InputStream inputStream, int readLimit) throws IOException, UnsupportedAudioFileException {
        DataInputStream dis = new DataInputStream(inputStream);
        int nMagic = dis.readInt();
        if (nMagic != AiffTool.AIFF_FORM_MAGIC)
            throw new UnsupportedAudioFileException("not an AIFF file: header magic is not FORM");
        dis.readInt();
        nMagic = dis.readInt();
        if (nMagic != AiffTool.AIFF_AIFF_MAGIC && nMagic != AiffTool.AIFF_AIFC_MAGIC)
            throw new UnsupportedAudioFileException("unsupported IFF file: header magic neither AIFF nor AIFC");
    }

    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes) throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) {
            TDebug.out("AiffAudioFileReader.getAudioFileFormat(InputStream, long): begin");
        }
        DataInputStream dis = new DataInputStream(inputStream);
        //if (!dis.markSupported())
        //    throw new UnsupportedAudioFileException("can't parse AIFF file: stream marking not supported");

        int nMagic = dis.readInt();
        if (nMagic != AiffTool.AIFF_FORM_MAGIC) {
            throw new UnsupportedAudioFileException("not an AIFF file: header magic is not FORM");
        }
        int nTotalLength = dis.readInt();
        nMagic = dis.readInt();
        boolean bIsAifc;
        if (nMagic == AiffTool.AIFF_AIFF_MAGIC) {
            bIsAifc = false;
        } else if (nMagic == AiffTool.AIFF_AIFC_MAGIC) {
            bIsAifc = true;
        } else {
            throw new UnsupportedAudioFileException("unsupported IFF file: header magic neither AIFF nor AIFC");
        }
        List<AbstractAudioChunk> chunks = parseChunks(dis, chunkParserMap);

        AiffFVERChunk avc = (AiffFVERChunk) findChunk(chunks, AiffFVERChunk.class);
        AiffCOMMChunk acc = (AiffCOMMChunk) findChunk(chunks, AiffCOMMChunk.class);
        AiffSSNDChunk asc = (AiffSSNDChunk) findChunk(chunks, AiffSSNDChunk.class);

        if ((bIsAifc && avc == null) || acc == null || asc == null) {
            UnsupportedAudioFileException e = new UnsupportedAudioFileException("unsupported AIFF file: required chunk not found.");
            if (TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
            throw e;
        }
        if (bIsAifc && avc.getFormatVersion() != AiffTool.AIFF_FVER_TIME_STAMP) {
            throw new UnsupportedAudioFileException("Unsupported AIFC file: format version not understood.");
        }

        if (dis.markSupported())
            dis.reset(); // resets to beginning of SSND chunk data (SSND chunk parser should have set mark!)

        Map<String, Object> m = makeChunkProperties(chunks);
        TAudioFormat format = new TAudioFormat(acc.getEncoding(),
                acc.getSampleRate(),
                acc.getSampleSize(),
                acc.getNumChannels(),
                acc.getFrameSize(),
                acc.getSampleRate(),
                true, m);
        if (TDebug.TraceAudioFileReader) {
            TDebug.out("AiffAudioFileReader.getAudioFileFormat(InputStream, long): end");
        }
        return new TAudioFileFormat(bIsAifc ? AudioFileFormat.Type.AIFC : AudioFileFormat.Type.AIFF,
                format,
                asc.getDataLength() / format.getFrameSize(),
                nTotalLength + 8);
    }

    public static final void main(String[] args) {
        AiffAudioFileReaderEx reader = new AiffAudioFileReaderEx();
        WaveAudioFileReaderEx wave_reader = new WaveAudioFileReaderEx();
        //  AudioFileFormat aff = getReader.getAudioFileFormat(new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\tester.wav"));
        try {
            System.setProperty(ChunkTool.ENHANCED_AIFF_WRITING_SYSTEM_PROPERTY, "true");
            AudioInputStream ais;
            try {
                ais = wave_reader.getAudioInputStream(new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\dayrush.aif"));
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ais = reader.getAudioInputStream(new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\dayrush.aif"));
            new AiffAudioFileWriterEx().write(ais, AudioFileFormat.Type.AIFF, new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\dayrush-proc.aif"));

            //AudioFileFormat aff = getReader.getAudioFileFormat(new File("C:\\Documents and Settings\\paulmeehan\\My Documents\\tester.wav"));
            Map m = ais.getFormat().properties();
            Object o = m.get(AiffMARKChunk.AUDIO_FORMAT_PROPERTIES_KEY);
            int markers = 0;
            if (o instanceof AiffMARKChunk) {
                markers = ((AiffMARKChunk) o).getNumMarkers();
                for (int i = 0; i < markers; i++)
                    System.out.println("Marker " + (i + 1) + " name: " + ((AiffMARKChunk) o).getMarkers()[i].getName());
            }
            o = m.get(AiffINSTChunk.AUDIO_FORMAT_PROPERTIES_KEY);
            int baseNote = 0;
            if (o instanceof AiffINSTChunk) {
                baseNote = ((AiffINSTChunk) o).getBaseNote();
            }
            System.out.println("Num sample markers = " + markers);
            System.out.println("Base note = " + baseNote);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } finally {

        }
    }
}
