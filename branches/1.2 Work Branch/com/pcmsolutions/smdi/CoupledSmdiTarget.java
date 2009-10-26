package com.pcmsolutions.smdi;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Sep-2003
 * Time: 19:05:16
 * To change this template use Options | File Templates.
 */
public interface CoupledSmdiTarget extends ScsiTarget {

    public void sendSync(String fileName, int sampleNum, String sampleName) throws SmdiTargetCouplingInvalidException, SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException;

    public void sendAsync(String fileName, int sampleNum, String sampleName) throws SmdiTargetCouplingInvalidException, SmdiGeneralException, TargetNotSMDIException, SmdiOutOfRangeException;

    public void recvSync(String fileName, int sampleNum) throws SmdiTargetCouplingInvalidException, SmdiFileOpenException, SmdiNoSampleException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException;

    public void recvAsync(String fileName, int sampleNum) throws SmdiTargetCouplingInvalidException, SmdiGeneralException, TargetNotSMDIException, SmdiOutOfRangeException;

    public SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiTargetCouplingInvalidException, SmdiOutOfRangeException, SmdiNoSampleException, SmdiGeneralException, TargetNotSMDIException;

    public SmdiSampleHeader getSampleHeader(String fileName) throws SmdiTargetCouplingInvalidException, SmdiFileOpenException, SmdiUnknownFileFormatException, SmdiGeneralException, TargetNotSMDIException;

    public String getCouplingTag() throws SmdiTargetCouplingInvalidException;
}
