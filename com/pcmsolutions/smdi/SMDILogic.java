package com.pcmsolutions.smdi;

import com.pcmsolutions.aspi.ASPILogic;
import com.pcmsolutions.aspi.SCSI;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.audio.AudioConversionException;
import com.pcmsolutions.system.audio.AudioConverter;
import com.pcmsolutions.system.threads.Impl_ZThread;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 14-Jan-2004
 * Time: 02:06:20
 * To change this template use Options | File Templates.
 */
public class SMDILogic {

    private static boolean checkWait(char[] reply, long interval, long timeout) {
        return false;
    }

    private static int checkReject(byte[] reply) throws SMDILogicException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiOutOfRangeException {
        try {
            int rmid = ZUtilities.extractUnsignedInt(reply, 4);
            if (rmid == SMDI.SMDI_MSG_REJECT) {
                int rcode = ZUtilities.extractUnsignedInt(reply, 11);
                switch (rcode) {
                    case SMDI.SMDI_REJECT_BPW:
                        throw new SMDILogicException("Unsupported number of bits per sample word");
                    case SMDI.SMDI_REJECT_BUSY:
                        throw new SMDILogicException("Device busy");
                    case SMDI.SMDI_REJECT_CHNLS:
                        throw new SMDILogicException("Unsupported number of audio channels");
                    case SMDI.SMDI_REJECT_EMPTY:
                        throw new SmdiSampleEmptyException("Empty sample");
                    case SMDI.SMDI_REJECT_HDR_MISMATCH:
                        throw new SMDILogicException("Sample header mismatch");
                    case SMDI.SMDI_REJECT_INAPPROPIATE:
                        throw new SMDILogicException("Inappropiate SMDI message");
                    case SMDI.SMDI_REJECT_MEMORY:
                        throw new SmdiNoMemoryException("Insufficient sample memory");
                    case SMDI.SMDI_REJECT_OUTOFRANGE:
                        throw new SmdiOutOfRangeException("Sample out of range");
                    case SMDI.SMDI_REJECT_PACKET_LENGTH:
                        throw new SMDILogicException("Unsupported packet length");
                    case SMDI.SMDI_REJECT_PACKET_MISMATCH:
                        throw new SMDILogicException("Packet number mismatch");
                    case SMDI.SMDI_REJECT_PARAM_MEMORY:
                        throw new SmdiNoMemoryException("Insufficient parameter memory");
                    case SMDI.SMDI_REJECT_UNSUPPORTED:
                        throw new SMDILogicException("Unsupported command");
                    default:
                        throw new SMDILogicException("Unknown SMDI problem: reject code = " + rcode);
                }
            }
            return rmid;
        } catch (IllegalArgumentException e) {
            throw new SMDILogicException("SMDI messaging problem");
        }
    }

    public static class SMDILogicException extends Exception {
        public SMDILogicException(String message) {
            super(message);
        }
    }

    public static void deleteSample(int haid, int id, int sample) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException {
        SMDIMsg.DeleteSample msg = new SMDIMsg.DeleteSample().setSample(sample);
        try {
            byte[] reply = msg.dispatch(haid, id);
            int rm = checkReject(reply);
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }
    }

    public static void nameSample(int haid, int id, int sample, String name) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException {
        SMDIMsg.SampleName msg = new SMDIMsg.SampleName().setName(sample, name);
        try {
            byte[] reply = msg.dispatch(haid, id);
            int rm = checkReject(reply);
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }
    }

    public interface DeviceInfo extends ASPILogic.DeviceInfo {
        public boolean isSMDI();
    }

    // may return null to indicate no device
    public static DeviceInfo inquireDevice(int haid, int id) throws SMDILogicException {
        try {
            final ASPILogic.DeviceInfo di;
            try {
                di = ASPILogic.inquireDevice(haid, id);
            } catch (ASPILogic.ASPINoDeviceException e) {
                return null;
            }
            if (di == null)
                return null;
            boolean smdiSlave = false;
            try {
                smdiSlave = masterIdentify(haid, id);
            } catch (SMDILogicException e) {
                throw new SMDILogicException(e.getMessage());
            } catch (SmdiOutOfRangeException e) {
                throw new SMDILogicException(e.getMessage());
            } catch (SmdiSampleEmptyException e) {
                throw new SMDILogicException(e.getMessage());
            } catch (SmdiNoMemoryException e) {
                throw new SMDILogicException(e.getMessage());
            }
            final boolean isSMDI = (di.getDeviceType() == SCSI.DTYPE_PROC && smdiSlave);
            return new DeviceInfo() {
                public boolean isSMDI() {
                    return isSMDI;
                }

                public String getManufacturer() {
                    return di.getManufacturer();
                }

                public String getName() {
                    return di.getName();
                }

                public int getHaId() {
                    return di.getHaId();
                }

                public int getScsiId() {
                    return di.getScsiId();
                }

                public int getDeviceType() {
                    return di.getDeviceType();
                }

                public char[] getResultBuffer() {
                    return di.getResultBuffer();
                }
            };
        } catch (ASPILogic.CommandFailedException e) {
            // throw new SMDILogicException(e.getMessage());
            return null;
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        }
    }

    public static boolean masterIdentify(int haid, int id) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException {
        SMDIMsg.MasterIdentify msg = new SMDIMsg.MasterIdentify();
        try {
            byte[] reply = msg.dispatch(haid, id);
            int rm = checkReject(reply);
            return rm == SMDI.SMDI_MSG_SLAVE;
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }
    }

    public static byte[] sendMidi(int haid, int id, byte[] midiData) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException {
        SMDIMsg.Midi msg = new SMDIMsg.Midi().setMidi(midiData);
        try {
            byte[] reply = msg.dispatch(haid, id);
            int rm = checkReject(reply);
            return SMDIMsg.stripHeader(reply);
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }
    }

    public static Impl_SmdiSampleHeader getSampleHeader(int haid, int id, int sample) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException {
        byte[] reply = SMDIMsg.stripHeader(IN_reqSampleHeader(haid, id, sample).getReply());
        masterIdentify(haid, id);
        //tryAbort(haid, id);
        return SMDIMsg.SampleHeader.getSampleHeader(reply);
    }

    private static int GC_BYTE_THRESHOLD = 1024 * 1204 * 8;

    public static AudioInputStream recvSampleAsync(final SMDIRecvInstance ri) throws SmdiOutOfRangeException, SmdiNoMemoryException, SMDILogicException, SmdiSampleEmptyException, SmdiGeneralException {
        final byte[] reply = SMDIMsg.stripHeader(IN_reqSampleHeader(ri.getHAID(), ri.getID(), ri.getSample()).getReply());
        final AudioFormat af = SMDIMsg.SampleHeader.getAudioFormat(reply, ri);
        final int len = SMDIMsg.SampleHeader.getSampleLength(reply);
        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos;
        try {
            pos = new PipedOutputStream(pis);
        } catch (IOException e) {
            throw new SmdiGeneralException();
        }
        AudioInputStream ais = new AudioInputStream(pis, af, len);
        new Impl_ZThread() {
            public void runBody() {
                ProgressCallback prog = ri.getProgressCallback();
                try {
                    SMDITransactionReport rep;
                    int ps = ri.getPacketSizeInBytes();
                    prog.updateProgress(0);
                    try {
                        int bytesOutstanding = len * (af.getSampleSizeInBits() / 8) * af.getChannels();
                        rep = IN_beginTransfer(ri.getHAID(), ri.getID(), ri.getSample(), ps);
                        double tot = bytesOutstanding;
                        double gc_pass_tot = 0;
                        // System.out.println("Total length = " + tot);
                        int packet = 0;
                        while (bytesOutstanding > 0) {
                            try {
                                rep = IN_sendNextPacket(ri.getHAID(), ri.getID(), ri.getSample(), packet++, ps);
                                byte[] packetData = SMDIMsg.DataPacket.getData(rep.getReply());
                                // System.out.println("Packet length = " + packetData.length);
                                // System.out.println("bytes outstanding = " + bytesOutstanding);
                                pos.write(packetData, 0, packetData.length);
                                bytesOutstanding -= packetData.length;
                                gc_pass_tot += packetData.length;
                                if (prog.isCancelled()) {
                                    tryAbort(ri.getHAID(), ri.getID());
                                    throw new SmdiTransferAbortedException();
                                }
                                prog.updateProgress((tot - bytesOutstanding) / tot);
                            } finally {
                                if (gc_pass_tot > GC_BYTE_THRESHOLD) {
                                    System.gc();
                                    gc_pass_tot = 0;
                                }
                            }
                        }
                        pos.close();
                    } catch (SmdiOutOfRangeException e) {
                        e.printStackTrace();
                    } catch (SmdiTransferAbortedException e) {
                        e.printStackTrace();
                    } catch (SmdiNoMemoryException e) {
                        e.printStackTrace();
                    } catch (SMDILogicException e) {
                        e.printStackTrace();
                    } catch (SmdiSampleEmptyException e) {
                        e.printStackTrace();
                    } finally {
                        prog.updateProgress(1);
                        //System.gc();
                        //System.gc();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return ais;
    }

    /*
    public static AudioOutputStream recvSample(SMDIRecvInstance ri) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiTransferAbortedException, IOException, SmdiGeneralException {
        SMDITransactionReport rep;
        final ProgressCallback prog = ri.getProgressCallback();

        final byte[] reply = SMDIMsg.stripHeader(IN_reqSampleHeader(ri.getHAID(), ri.getID(), ri.getSample()).getReply());
        final AudioFormat af = SMDIMsg.SampleHeader.getAudioFormat(reply, ri);

        AudioOutputStream aos = null;
        int len = SMDIMsg.SampleHeader.getSampleLength(reply);
        int ps = ri.getPacketSizeInBytes();
        prog.updateProgress(0);

        try {
            int bytesOutstanding = len * (af.getSampleSizeInBits() / 8) * af.getChannels();
            try {
                aos = ZAudioSystem.getAudioOutputStream(ri.getFileType(), af, bytesOutstanding, ri.getOutputStream());
            } catch (Exception e) {
                throw new SmdiGeneralException(e.getMessage());
            }
            rep = IN_beginTransfer(ri.getHAID(), ri.getID(), ri.getSample(), ps);
            double tot = bytesOutstanding;
            // System.out.println("Total length = " + tot);
            int packet = 0;
            while (bytesOutstanding > 0) {
                rep = IN_sendNextPacket(ri.getHAID(), ri.getID(), ri.getSample(), packet++, ps);
                byte[] packetData = SMDIMsg.DataPacket.getData(rep.getReply());
                // System.out.println("Packet length = " + packetData.length);
                // System.out.println("bytes outstanding = " + bytesOutstanding);
                aos.write(packetData, 0, packetData.length);
                bytesOutstanding -= packetData.length;
                if (prog.isCancelled()) {
                    tryAbort(ri.getHAID(), ri.getID());
                    throw new SmdiTransferAbortedException();
                }
                prog.updateProgress((tot - bytesOutstanding) / tot);
            }
        } finally {
            prog.updateProgress(1);
            System.gc();
        }
        return aos;
    }
     */
    public static void sendSample(SMDISendInstance si) throws SMDILogicException, IOException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiTransferAbortedException {
        SMDITransactionReport rep;
        AudioInputStream ais = si.getAudioInputStream();
        ProgressCallback prog = si.getProgressCallback();
        try {
            prog.updateProgress(0);
            rep = OUT_sendHeader(si.getHAID(), si.getID(), si.getSample(), si.getSampleName(), ais.getFrameLength(), ais.getFormat());
            int ps = SMDIMsg.TransferAck.getPacketLength(rep.getReply());
            if (ps < si.getPacketSizeInBytes())
                ps = si.getPacketSizeInBytes();
            long tot = ais.getFrameLength() * ais.getFormat().getFrameSize();
            // System.out.println("Total length = " + tot);
            double runTot = 0;
            rep = OUT_beginTransfer(si.getHAID(), si.getID(), si.getSample(), ps);
            int packet = 0;
            //int gc_pass_tot = 0;
            do {
                try {
                    rep = OUT_sendNextPacket(si.getHAID(), si.getID(), packet++, ais, ps);
                    runTot += rep.getAudioBytesTransferred();
                    //gc_pass_tot += rep.getAudioBytesTransferred();
                    //System.out.println("Bytes transferred = " + runTot);
                    if (prog.isCancelled()) {
                        tryAbort(si.getHAID(), si.getID());
                        throw new SmdiTransferAbortedException();
                    }
                    prog.updateProgress(runTot / tot);
                } finally {
                    /*
                    if (gc_pass_tot > GC_BYTE_THRESHOLD) {
                        System.gc();
                        gc_pass_tot = 0;
                    }
                    */
                }
            } while (rep.getMID() != SMDI.SMDI_MSG_EOP);
        } catch (SMDILogicException e) {
            tryAbort(si.getHAID(), si.getID());
            throw e;
        } catch (IOException e) {
            tryAbort(si.getHAID(), si.getID());
            throw e;
        } finally {
            prog.updateProgress(1);
            //System.gc();
        }
    }

    private static boolean tryAbort(int haid, int id) {
        SMDIMsg.Abort abort = new SMDIMsg.Abort();
        try {
            if (checkReject(abort.dispatch(haid, id)) == SMDI.SMDI_MSG_ACK)
                return true;
        } catch (ASPILogic.ASPILogicException e1) {
        } catch (ASPILogic.CommandFailedException e1) {
        } catch (SMDILogicException e) {
        } catch (SmdiSampleEmptyException e) {
        } catch (SmdiNoMemoryException e) {
        } catch (SmdiOutOfRangeException e) {
        } catch (SMDIMsg.SMDIMsgException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static SMDITransactionReport IN_reqSampleHeader(int haid, int id, int sample) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException {
        SMDIMsg.SampleHeaderRequest msg = new SMDIMsg.SampleHeaderRequest().setSample(sample);
        int mid;
        byte[] reply;
        try {
            reply = msg.dispatch(haid, id);
            mid = checkReject(reply);
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }

        final byte[] f_reply = reply;
        final int f_mid = mid;
        return new SMDITransactionReport() {
            public byte[] getReply() {
                return f_reply;
            }

            public int getAudioBytesTransferred() {
                return 0;
            }

            public int getMID() {
                return f_mid;
            }
        };
    }

    private static SMDITransactionReport IN_beginTransfer(int haid, int id, int sample, int packetSize) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiTransferAbortedException {
        SMDIMsg.TransferBegin tb = new SMDIMsg.TransferBegin();
        int mid;
        tb.setSample(sample, packetSize);
        byte[] reply;
        try {
            reply = tb.dispatch(haid, id);
            mid = checkReject(reply);
            if (mid == SMDI.SMDI_MSG_ABORT)
                throw new SmdiTransferAbortedException();
            else if (mid != SMDI.SMDI_MSG_TRANSFER_ACK)
                throw new SMDILogicException("Unexpected response from slave device");
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }
        final byte[] f_reply = reply;
        return new SMDITransactionReport() {
            public byte[] getReply() {
                return f_reply;
            }

            public int getAudioBytesTransferred() {
                return 0;
            }

            public int getMID() {
                return SMDI.SMDI_MSG_TRANSFER_ACK;
            }
        };
    }

    private static SMDITransactionReport IN_sendNextPacket(int haid, int id, int sample, int packet, int packetSize) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiTransferAbortedException {
        int mid;
        SMDIMsg.SendNextPacket snp = new SMDIMsg.SendNextPacket();
        snp.setPacket(packet, packetSize);
        byte[] reply;
        try {
            reply = snp.dispatch(haid, id);
            mid = checkReject(reply);
            if (mid == SMDI.SMDI_MSG_ABORT)
                throw new SmdiTransferAbortedException();
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }
        final byte[] f_reply = reply;
        final int f_mid = mid;
        return new SMDITransactionReport() {
            public byte[] getReply() {
                return f_reply;
            }

            public int getAudioBytesTransferred() {
                return 0;
            }

            public int getMID() {
                return f_mid;
            }
        };
    }

    private static final int WAIT_INTERVAL = 100;

    private static SMDITransactionReport OUT_sendHeader(int haid, int id, int sample, String name, long length, AudioFormat f) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException {
        SMDIMsg.SampleHeader h = new SMDIMsg.SampleHeader();
        h.setSample(sample, name, length, f);
        byte[] reply;
        int mid;
        try {
            reply = h.dispatch(haid, id);
            mid = checkReject(reply);
            if (mid != SMDI.SMDI_MSG_TRANSFER_ACK) {
                if (mid == SMDI.SMDI_MSG_WAIT) {
                    try {
                        Thread.sleep(WAIT_INTERVAL);
                    } catch (InterruptedException e) {
                    }
                } else
                    throw new SMDILogicException("Unexpected response from slave device");
                return OUT_sendHeader(haid, id, sample, name, length, f);
            }
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }
        final byte[] f_reply = reply;
        final int f_mid = mid;
        return new SMDITransactionReport() {
            public byte[] getReply() {
                return f_reply;
            }

            public int getAudioBytesTransferred() {
                return 0;
            }

            public int getMID() {
                return f_mid;
            }
        };
    }

    private static SMDITransactionReport OUT_beginTransfer(int haid, int id, int sample, int pl) throws SMDILogicException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException {
        SMDIMsg.TransferBegin tb = new SMDIMsg.TransferBegin();
        tb.setSample(sample, pl);
        byte[] reply;
        int mid;
        try {
            reply = tb.dispatch(haid, id);
            mid = checkReject(reply);
            if (mid != SMDI.SMDI_MSG_NEXT) {
                if (mid == SMDI.SMDI_MSG_WAIT) {
                    try {
                        Thread.sleep(WAIT_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else
                    throw new SMDILogicException("Unexpected response from slave device");
                return OUT_beginTransfer(haid, id, sample, pl);
            }
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }
        final byte[] f_reply = reply;
        final int f_mid = mid;
        return new SMDITransactionReport() {
            public byte[] getReply() {
                return f_reply;
            }

            public int getAudioBytesTransferred() {
                return 0;
            }

            public int getMID() {
                return f_mid;
            }
        };
    }

    private static SMDITransactionReport OUT_sendNextPacket(int haid, int id, int packet, AudioInputStream ais, int pl) throws SMDILogicException, IOException, SmdiOutOfRangeException, SmdiSampleEmptyException, SmdiNoMemoryException {
        SMDIMsg.DataPacket dp = new SMDIMsg.DataPacket();
        byte[] reply;
        int mid;
        try {
            ais.mark(ais.available());
            if (ais.available() < pl)
                pl = ais.available();
            byte[] data = new byte[pl];
            ais.read(data, 0, pl);
            dp.setPacketData(packet, data);
            reply = dp.dispatch(haid, id);
            mid = checkReject(reply);
            if (mid != SMDI.SMDI_MSG_NEXT) {
                if (mid == SMDI.SMDI_MSG_WAIT) {
                    System.out.println("SMDI WAIT");
                    try {
                        Thread.sleep(WAIT_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ais.reset();
                    return OUT_sendNextPacket(haid, id, packet, ais, pl);
                }
                if (mid != SMDI.SMDI_MSG_EOP)
                    throw new SMDILogicException("Unexpected response from slave device");
            }
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDIMsg.SMDIMsgException e) {
            throw new SMDILogicException(e.getMessage());
        }
        final byte[] f_reply = reply;
        final int f_pl = pl;
        final int f_mid = mid;
        return new SMDITransactionReport() {
            public byte[] getReply() {
                return f_reply;
            }

            public int getAudioBytesTransferred() {
                return f_pl;
            }

            public int getMID() {
                return f_mid;
            }
        };
    }

    private static interface SMDITransactionReport {
        public byte[] getReply();

        public int getAudioBytesTransferred();

        public int getMID();
    }

    private static class Converter {
        public static AudioInputStream prepareAudioStream(AudioInputStream ais, float maxRate) throws SmdiUnsupportedConversionException, AudioConversionException {
            AudioFormat af = ais.getFormat();
            if (af.getChannels() > 2)
                throw new SmdiUnsupportedConversionException("Too many channels in audio data");

            float rate = af.getSampleRate();
            if (rate > maxRate)
                rate = maxRate;
            return AudioConverter.convertStream(ais, new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, af.getChannels(), af.getFrameSize(), af.getFrameRate(), true, af.properties()));
        }
    }

    public static void main(String[] args) {

    }
}
