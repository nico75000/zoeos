package org.tritonus.zuonics.sampled;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

abstract public class AbstractChunkyAudioOutputStream extends TAudioOutputStream {
    private final AudioOutputStream basicWriter;
    private List<AudioChunkGenerator> chunkGeneratorList;
    private AudioFileFormat.Type fileType;

    public AbstractChunkyAudioOutputStream(AudioFileFormat.Type fileType, AudioFormat format, long l, TDataOutputStream tDataOutputStream, List<AudioChunkGenerator> chunkGeneratorList, AudioOutputStream basicWriter) {
        super(format, l, tDataOutputStream, l == AudioSystem.NOT_SPECIFIED && tDataOutputStream.supportsSeek());
        this.basicWriter = basicWriter;
        this.chunkGeneratorList = chunkGeneratorList;
        this.fileType = fileType;
    }

    public AbstractChunkyAudioOutputStream(AudioFileFormat.Type fileType,AudioFormat format, long l, TDataOutputStream tDataOutputStream, List<AudioChunkGenerator> chunkGeneratorList) {
        this(fileType,format, l, tDataOutputStream, chunkGeneratorList, null);
    }

    public int write(byte[] bytes, int i, int i1) throws IOException {
        if (basicWriter != null)
            return basicWriter.write(bytes, i, i1);
        return super.write(bytes, i, i1);
    }

    public AudioFormat getFormat() {
        if (basicWriter != null)
            return basicWriter.getFormat();
        return super.getFormat();
    }

    public long getLength() {
        if (basicWriter != null)
            return basicWriter.getLength();
        return super.getLength();
    }

    protected final void writeHeader() throws IOException {
        if (TDebug.TraceAudioOutputStream) {
            TDebug.out("AbstractChunkyAudioOutputStream.writeHeader()");
        }
        for (Iterator<AudioChunkGenerator> i = chunkGeneratorList.iterator(); i.hasNext();)
            i.next().generateChunk(fileType, getFormat(), getLength(), getDataOutputStream());
    }

    protected void patchHeader()
            throws IOException {
        TDataOutputStream tdos = getDataOutputStream();
        tdos.seek(0);
        setLengthFromCalculatedLength();
        writeHeader();
    }

    public void close() throws IOException {
        if (basicWriter != null)
            basicWriter.close();
        else {
            long nBytesWritten = getCalculatedLength();

            if ((nBytesWritten % 2) == 1) {
                if (TDebug.TraceAudioOutputStream) {
                    TDebug.out("AbstractChunkyAudioOutputStream.referencedClose(): adding padding byte");
                }
                // extra byte for to align on word boundaries
                TDataOutputStream tdos = getDataOutputStream();
                tdos.writeByte(0);
                // DON'T adjust calculated length !
            }
            super.close();
        }
    }

    public static void writeIeeeExtended(TDataOutputStream	dos, float sampleRate) throws IOException {
        // currently, only integer sample rates are written
        // TODO: real conversion
        // I don't know exactly how much I have to shift left the mantisse for normalisation
        // now I do it so that there are any bits set in the first 5 bits
        int nSampleRate=(int) sampleRate;
        short ieeeExponent=0;
        while ((nSampleRate!=0) && (nSampleRate & 0x80000000)==0) {
            ieeeExponent++;
            nSampleRate<<=1;
        }
        dos.writeShort(16414-ieeeExponent); // exponent
        dos.writeInt(nSampleRate);          // mantisse high double word
        dos.writeInt(0);                    // mantisse low double word
    }

}
