package org.tritonus.zuonics.sampled;

import org.tritonus.zuonics.sampled.AudioChunkParser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

public class ChunkTool {
    public static final String ENHANCED_WAVE_PARSING_SYSTEM_PROPERTY = "org.tritonus.enhanced_WAVE_parsing";
    public static final String ENHANCED_WAVE_WRITING_SYSTEM_PROPERTY = "org.tritonus.enhanced_WAVE_writing";
    public static final String ENHANCED_AIFF_PARSING_SYSTEM_PROPERTY = "org.tritonus.enhanced_AIFF_parsing";
    public static final String ENHANCED_AIFF_WRITING_SYSTEM_PROPERTY = "org.tritonus.enhanced_AIFF_writing";

    public static final String WAVE_SMPL_CHUNK_ID = "smpl";
    public static final int WAVE_SMPL_MAGIC = 1936552044;
    public static final int MIN_SMPL_CHUNK_LENGTH = 36;
    public static final int SMPL_LOOP_STRUCTURE_LENGTH = 24;

    public static final String AIFF_INST_CHUNK_ID = "INST";
    public static final int AIFF_INST_MAGIC = 1229869908;
    public static final int INST_CHUNK_LENGTH = 20;

    public static final String AIFF_MARK_CHUNK_ID = "MARK";
    public static final int AIFF_MARK_MAGIC = 1296126539;
    public static final int MIN_MARK_CHUNK_LENGTH = 2;

    public static final int MIN_FVER_CHUNK_LENGTH = 4;

    public static final int COMM_CHUNK_LENGTH = 18;

    public static void advanceAiffChunk(DataInputStream dataInputStream, long chunkLength, long chunkRead) throws UnsupportedAudioFileException, IOException {
        chunkLength -= chunkRead;
        if (chunkLength > 0) {
            dataInputStream.skip(chunkLength + (chunkLength % 2));
        } else if (chunkLength < 0)
            throw new UnsupportedAudioFileException("corrupt AIFF file: specified chunk length is incorrect");
    }

    public static void advanceWaveChunk(DataInputStream dis, long chunkLength, long chunkRead)
            throws IOException, UnsupportedAudioFileException {
        if (chunkLength > 0) {
            dis.skip(((chunkLength + 1) & 0xFFFFFFFE) - chunkRead);
        } else if (chunkLength < 0)
            throw new UnsupportedAudioFileException("corrupt WAVE file: specified chunk length is incorrect");
    }

    public static boolean getAssumedTrueBooleanProperty(String prop) {
        try {
            String ep = System.getProperty(prop);
            if (ep != null && Boolean.parseBoolean(ep) == false)
                return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void addParser(AudioChunkParser parser, Map<Integer, AudioChunkParser> map) {
        map.put(new Integer(parser.getMagic()), parser);
    }
}
