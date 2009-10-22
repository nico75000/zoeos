package org.tritonus.zuonics.sampled;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioFileReader;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.*;
import java.net.URL;
import java.util.*;

abstract public class AbstractChunkyAudioFileReader extends TAudioFileReader {

    private static final int INIT_READ_LIMIT = 1024;

    private final AudioFileReader basicReader;

    public AbstractChunkyAudioFileReader() {
        this(null);
    }

    public AbstractChunkyAudioFileReader(AudioFileReader basicReader) {
        super(INIT_READ_LIMIT, false);
        this.basicReader = basicReader;
    }

    abstract protected void advanceChunk(DataInputStream dis, long chunkLength, long chunkRead) throws IOException, UnsupportedAudioFileException;

    protected final AbstractAudioChunk findChunk(List<AbstractAudioChunk> chunks, Class c) {
        for (Iterator<AbstractAudioChunk> i = chunks.iterator(); i.hasNext();) {
            AbstractAudioChunk chunk = i.next();
            if (c.isInstance(chunk))
                return chunk;
        }
        return null;
    }

    protected final Map<String, Object> makeChunkProperties(List<AbstractAudioChunk> chunks) {
        Map<String, Object> propMaps = new HashMap<String, Object>();
        for (Iterator<AbstractAudioChunk> i = chunks.iterator(); i.hasNext();) {
            AbstractAudioChunk chunk = i.next();
            if (chunk.getAudioFormatPropertyKey() != null)
                propMaps.put(chunk.getAudioFormatPropertyKey(), chunk);
        }
        return propMaps;
    }

    abstract protected int readChunkLength(DataInputStream dis) throws IOException;

    protected final List<AbstractAudioChunk> parseChunks(DataInputStream dis, Map<Integer, AudioChunkParser> chunkParserMap)
            throws UnsupportedAudioFileException, IOException {
        ArrayList<AbstractAudioChunk> parsedChunks = new ArrayList<AbstractAudioChunk>();

        int currKey;
        int chunkLength = 0;
        int chunkRead = 0;
        do {
            if (dis.available() == 0)
                break;
            advanceChunk(dis, chunkLength, chunkRead);
            if (dis.available() == 0)
                break;
            try {
                currKey = dis.readInt();
                if (dis.available() == 0)
                    break;
                chunkLength = readChunkLength(dis);
            } catch (IOException e) {
                break;
            }
            Integer currKeyObj = new Integer(currKey);
            if (chunkParserMap.containsKey(currKeyObj)) {
                parsedChunks.add((chunkParserMap.get(currKeyObj)).parseChunk(dis, chunkLength));
                chunkRead = chunkLength;
            } else
                chunkRead = 0;
        } while (true);
        return parsedChunks;
    }

    protected AudioInputStream getAudioInputStream(InputStream inputStream, long lFileLengthInBytes)
            throws UnsupportedAudioFileException, IOException {
        if (TDebug.TraceAudioFileReader) {
            TDebug.out("AbstractChunkyAudioFileReader.getAudioInputStream(InputStream, long): begin");
        }
        inputStream = new BufferedInputStream(inputStream, INIT_READ_LIMIT);
        inputStream.mark(INIT_READ_LIMIT);
        preliminaryCheck(inputStream, INIT_READ_LIMIT);
        inputStream.reset();
        inputStream = new BufferedInputStream(inputStream, (int) lFileLengthInBytes); // ok buffer it all now - we know we are not wasting our time with this stream
        AudioFileFormat audioFileFormat = getAudioFileFormat(inputStream, lFileLengthInBytes);
        AudioInputStream audioInputStream =
                new AudioInputStream(inputStream,
                        audioFileFormat.getFormat(),
                        audioFileFormat.getFrameLength());
        if (TDebug.TraceAudioFileReader) {
            TDebug.out("AbstractChunkyAudioFileReader.getAudioInputStream(InputStream, long): end");
        }
        return audioInputStream;
    }

    // this is to test the first few magic bytes of the stream to see if it is initially compatible
    // we do this because chunky file readers need to buffer the whole file
    // we can avoid creating uneccessary huge buffered streams from huge files by testing the first few bytes
    protected abstract void preliminaryCheck(InputStream inputStream, int readLimit) throws IOException, UnsupportedAudioFileException;

    public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
        if (basicReader != null)
            return basicReader.getAudioFileFormat(stream);
        return super.getAudioFileFormat(stream);
    }

    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        if (basicReader != null)
            return basicReader.getAudioFileFormat(url);
        return super.getAudioFileFormat(url);
    }

    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        if (basicReader != null)
            return basicReader.getAudioFileFormat(file);
        return super.getAudioFileFormat(file);
    }

    public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
        if (basicReader != null)
            return basicReader.getAudioInputStream(stream);
        return super.getAudioInputStream(stream);
    }

    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        if (basicReader != null)
            return basicReader.getAudioInputStream(url);
        return super.getAudioInputStream(url);
    }

    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        if (basicReader != null)
            return basicReader.getAudioInputStream(file);
        return super.getAudioInputStream(file);
    }
}
