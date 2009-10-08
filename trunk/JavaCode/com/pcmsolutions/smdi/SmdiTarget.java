package com.pcmsolutions.smdi;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 19:05:16
 * To change this template use Options | File Templates.
 */
public interface SmdiTarget extends ScsiTarget {

    public void sendSync(String fileName, int sampleNum, String sampleName) throws SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException;

    public void sendAsync(String fileName, int sampleNum, String sampleName) throws SmdiGeneralException, TargetNotSMDIException, SmdiOutOfRangeException;

    public void recvSync(String fileName, int sampleNum) throws SmdiFileOpenException, SmdiNoSampleException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException;

    public void recvAsync(String fileName, int sampleNum) throws SmdiGeneralException, TargetNotSMDIException, SmdiOutOfRangeException;

    //public boolean deleteSample(int sampleNum);

    public SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiOutOfRangeException, SmdiNoSampleException, SmdiGeneralException, TargetNotSMDIException;

    public SmdiSampleHeader getSampleHeader(String fileName) throws SmdiFileOpenException, SmdiUnknownFileFormatException, SmdiGeneralException, TargetNotSMDIException;

    public boolean isCoupled();

    public String getCouplingString() throws SmdiTargetNotCoupledException;

    public void setCouplingString(String str) throws TargetNotSMDIException, SmdiUnavailableException;
}
