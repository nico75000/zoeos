package com.pcmsolutions.smdi;

import com.pcmsolutions.gui.ProgressCallback;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.midi.MidiMessage;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 19:05:16
 * To change this template use Options | File Templates.
 */
public interface SmdiTarget extends ScsiTarget {

    public void sendSync(AudioInputStream ais, int sampleNum, String sampleName, int packetSize,ProgressCallback prog) throws SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, UnsupportedAudioFileException, IOException, SmdiUnsupportedConversionException,  SmdiSampleEmptyException, SmdiTransferAbortedException;

    //public void recvSync(OutputStream os,AudioFileFormat.Type fileType, int sampleNum,int packetSize,  ProgressCallback prog) throws SmdiFileOpenException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiTransferAbortedException, IOException;

    public void recvSync(SMDIAgent.SampleInputStreamHandler ish,AudioFileFormat.Type fileType, int sampleNum,int packetSize,  ProgressCallback prog) throws SmdiOutOfRangeException, TargetNotSMDIException, SmdiGeneralException, SmdiNoMemoryException, SmdiSampleEmptyException;

    public byte[] sendMidiMessage(MidiMessage m) throws TargetNotSMDIException, SmdiGeneralException;

    //public boolean deleteSample(int sampleNum);

    public SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiSampleEmptyException, SmdiNoMemoryException;

    public boolean isCoupled();

    public String getCouplingString() throws SmdiTargetNotCoupledException;

    public void setCouplingString(String str) throws TargetNotSMDIException, SmdiUnavailableException;
}
