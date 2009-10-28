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
public interface CoupledSmdiTarget extends ScsiTarget {

    public void sendSync(AudioInputStream ais, int sampleNum, String sampleName, int packetSize, ProgressCallback prog) throws SmdiTargetCouplingInvalidException, SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiUnsupportedConversionException, SMDILogic.SMDILogicException, SmdiSampleEmptyException, UnsupportedAudioFileException, IOException, SmdiTransferAbortedException;

    public void recvAsync(SMDIAgent.SampleInputStreamHandler ish, AudioFileFormat.Type fileType,  int sampleNum, int packetSize, ProgressCallback prog) throws SmdiTargetCouplingInvalidException, SmdiFileOpenException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiTransferAbortedException, IOException;

    public SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiTargetCouplingInvalidException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiSampleEmptyException, SmdiNoMemoryException;

    public String getCouplingTag() throws SmdiTargetCouplingInvalidException;

    byte[] sendMidiMessage(MidiMessage m) throws SmdiGeneralException, TargetNotSMDIException;
}
