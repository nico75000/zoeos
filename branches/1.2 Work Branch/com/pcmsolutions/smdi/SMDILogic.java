package com.pcmsolutions.smdi;

import com.pcmsolutions.aspi.ASPILogic;
import com.pcmsolutions.gui.ProgressUpdater;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.preferences.Impl_ZDoublePref;
import com.pcmsolutions.system.preferences.Impl_ZIntPref;
import com.pcmsolutions.system.preferences.ZDoublePref;
import com.pcmsolutions.system.preferences.ZIntPref;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;


/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 14-Jan-2004
 * Time: 02:06:20
 * To change this template use Options | File Templates.
 */
class SMDILogic {
    private static int extractAddMsgLen(char[] arr) {
        if (arr.length < 11)
            throw new IllegalArgumentException("not a valid SMDI message");
        int v = 0;
        v += arr[8] << 16;
        v += arr[9] << 8;
        v += arr[10];
        return v;
    }

    private static boolean checkWait(char[] reply, long interval, long timeout) throws SMDILogicException, SMDISampleEmptyException {
        return false;
    }

    private static int checkReject(char[] reply) throws SMDILogicException, SMDISampleEmptyException {
        try {
            int rmid = ZUtilities.extractInt(reply, 4);
            if (rmid == SMDI.SMDI_MSG_REJECT) {
                int rcode = ZUtilities.extractInt(reply, 11);
                switch (rcode) {
                    case SMDI.SMDI_REJECT_BPW:
                        throw new SMDILogicException("Unsupported number of bits per sample word");
                    case SMDI.SMDI_REJECT_BUSY:
                        throw new SMDILogicException("Device busy");
                    case SMDI.SMDI_REJECT_CHNLS:
                        throw new SMDILogicException("Unsupported number of audio channels");
                    case SMDI.SMDI_REJECT_EMPTY:
                        throw new SMDISampleEmptyException("Empty");
                    case SMDI.SMDI_REJECT_HDR_MISMATCH:
                        throw new SMDILogicException("Sample header mismatch");
                    case SMDI.SMDI_REJECT_INAPPROPIATE:
                        throw new SMDILogicException("Inappropiate SMDI message");
                    case SMDI.SMDI_REJECT_MEMORY:
                        throw new SMDILogicException("Insufficient sample memory");
                    case SMDI.SMDI_REJECT_OUTOFRANGE:
                        throw new SMDILogicException("Sample out of range");
                    case SMDI.SMDI_REJECT_PACKET_LENGTH:
                        throw new SMDILogicException("Unsupported packet length");
                    case SMDI.SMDI_REJECT_PACKET_MISMATCH:
                        throw new SMDILogicException("Packet number mismatch");
                    case SMDI.SMDI_REJECT_PARAM_MEMORY:
                        throw new SMDILogicException("Insufficient parameter memory");
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

    public static class SMDISampleEmptyException extends Exception {
        public SMDISampleEmptyException(String message) {
            super(message);
        }
    }

    public static void deleteSample(int haid, int id, int sample) throws SMDILogicException, SMDISampleEmptyException {
        SMDIMsg.DeleteSample msg = new SMDIMsg.DeleteSample().setSample(sample);
        try {
            char[] reply = msg.dispatch(haid, id);
            int rm = checkReject(reply);
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        }
    }

    public static void nameSample(int haid, int id, int sample, String name) throws SMDILogicException, SMDISampleEmptyException {
        SMDIMsg.SampleName msg = new SMDIMsg.SampleName().setName(sample, name);
        try {
            char[] reply = msg.dispatch(haid, id);
            int rm = checkReject(reply);
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        }
    }

    public static void masterIdentify(int haid, int id) throws SMDILogicException, SMDISampleEmptyException {
        SMDIMsg.MasterIdentify msg = new SMDIMsg.MasterIdentify();
        try {
            char[] reply = msg.dispatch(haid, id);
            int rm = checkReject(reply);
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        }
    }

    public static synchronized byte[] sendMidi(int haid, int id, byte[] midiData) throws SMDILogicException, SMDISampleEmptyException {
        SMDIMsg.Midi msg = new SMDIMsg.Midi().setMidi(midiData);
        try {
            char[] reply = msg.dispatch(haid, id);
            int rm = checkReject(reply);
            byte[] arr = ZUtilities.extractByteArray(reply, 11, extractAddMsgLen(reply));
            return arr;
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        }
    }

    private static SMDITransactionReport IN_reqSampleHeader(int haid, int id, int sample) throws SMDILogicException, SMDISampleEmptyException {
        SMDIMsg.SampleHeaderRequest msg = new SMDIMsg.SampleHeaderRequest().setSample(sample);
        int mid;
        char[] reply;
        try {
            reply = msg.dispatch(haid, id);
            mid = checkReject(reply);
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        }

        final char[] f_reply = reply;
        final int f_mid = mid;
        return new SMDITransactionReport() {
            public char[] getReply() {
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

    public static void recvSample(int haid, int id, int sample, OutputStream os, ProgressUpdater prog) throws SMDISampleEmptyException, SMDILogicException {
        SMDITransactionReport rep;
        rep = IN_reqSampleHeader(haid, id, sample);


         //ByteArrayInputStream bais = new ByteArrayInputStream();

        //AudioSystem.write();
        //AudioInputStream ais = new AudioInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        rep = IN_beginTransfer(haid, id, sample);
    }

    private static SMDITransactionReport IN_beginTransfer(int haid, int id, int sample) throws SMDILogicException {
        SMDIMsg.TransferBegin tb = new SMDIMsg.TransferBegin();
        tb.setSample(sample, ZPREF_smdiPacketSizeKb.getValue() * 1024);
        char[] reply;
        try {
            reply = tb.dispatch(haid, id);
            if (checkReject(reply) != SMDI.SMDI_MSG_TRANSFER_ACK)
                throw new SMDILogicException("Unexpected response from slave device");
        } catch (ASPILogic.ASPILogicException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (ASPILogic.CommandFailedException e) {
            throw new SMDILogicException(e.getMessage());
        } catch (SMDISampleEmptyException e) {
            throw new SMDILogicException(e.getMessage());
        }
        final char[] f_reply = reply;
        return new SMDITransactionReport() {
            public char[] getReply() {
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

    private static final int WAIT_INTERVAL = 50;

    private static SMDITransactionReport OUT_sendHeader(int haid, int id, int sample, String name, long length, AudioFormat f) throws SMDILogicException {
        SMDIMsg.SampleHeader h = new SMDIMsg.SampleHeader();
        h.setSample(sample, name, length, f);
        char[] reply;
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
        } catch (SMDISampleEmptyException e) {
            throw new SMDILogicException(e.getMessage());
        }
        final char[] f_reply = reply;
        final int f_mid = mid;
        return new SMDITransactionReport() {
            public char[] getReply() {
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

    private static SMDITransactionReport OUT_beginTransfer(int haid, int id, int sample, int pl) throws SMDILogicException {
        SMDIMsg.TransferBegin tb = new SMDIMsg.TransferBegin();
        tb.setSample(sample, pl);
        char[] reply;
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
        } catch (SMDISampleEmptyException e) {
            throw new SMDILogicException(e.getMessage());
        }
        final char[] f_reply = reply;
        final int f_mid = mid;
        return new SMDITransactionReport() {
            public char[] getReply() {
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

    private static SMDITransactionReport OUT_sendNextPacket(int haid, int id, int packet, AudioInputStream ais, int pl) throws SMDILogicException, IOException {
        SMDIMsg.DataPacket dp = new SMDIMsg.DataPacket();
        char[] reply;
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
        } catch (SMDISampleEmptyException e) {
            throw new SMDILogicException(e.getMessage());
        }
        final char[] f_reply = reply;
        final int f_pl = pl;
        final int f_mid = mid;
        return new SMDITransactionReport() {
            public char[] getReply() {
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
        public char[] getReply();

        public int getAudioBytesTransferred();

        public int getMID();
    }

    // optional audio format conversion, null is ok
    public static final ZIntPref ZPREF_smdiPacketSizeKb = new Impl_ZIntPref(Preferences.userNodeForPackage(SMDILogic.class), "smdiPacketSizeKb", 64);

    private static class AudioConverter {
        public static final ZDoublePref ZPREF_maxRate = new Impl_ZDoublePref(Preferences.userNodeForPackage(SMDILogic.class), "smdiMaxRate", 48000);

        public static AudioInputStream prepareAudioStream(AudioInputStream ais) throws SmdiUnsupportedConversionException {
            AudioFormat af = ais.getFormat();
            if (af.getChannels() > 2)
                throw new SmdiUnsupportedConversionException("Too many channels in audio data");

            AudioFormat naf;
            if (!af.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) || !af.isBigEndian()) {
                naf = new AudioFormat(af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels(), true, true);
                if (AudioSystem.isConversionSupported(naf, af))
                    ais = AudioSystem.getAudioInputStream(naf, ais);
                else
                    throw new SmdiUnsupportedConversionException("Unsupported audio conversion");
            }

            float mr = (float) ZPREF_maxRate.getValue();
            if (af.getSampleRate() > mr || af.getSampleSizeInBits() != 16) {
                float rate = Math.min(af.getSampleRate(), mr);
                naf = new AudioFormat(rate, 16, af.getChannels(), true, true);
                if (AudioSystem.isConversionSupported(naf, af))
                    ais = AudioSystem.getAudioInputStream(naf, ais);
                else
                    throw new SmdiUnsupportedConversionException("Unsupported audio conversion");
            }
            return ais;
        }
    }

    public static void sendSample(int haid, int id, int sample, String name, AudioInputStream ais, ProgressUpdater prog) throws SMDILogicException, IOException, SmdiUnsupportedConversionException {
        ais = AudioConverter.prepareAudioStream(ais);
        SMDITransactionReport rep;
        try {
            prog.setProgress(0);
            rep = OUT_sendHeader(haid, id, sample, name, ais.getFrameLength(), ais.getFormat());
            int ps = SMDIMsg.TransferAck.getPacketLength(rep.getReply());
            if (ps < ZPREF_smdiPacketSizeKb.getValue() * 1024)
                ps = ZPREF_smdiPacketSizeKb.getValue() * 1024;
            long tot = ais.getFrameLength() * ais.getFormat().getFrameSize();
            long runTot = 0;
            rep = OUT_beginTransfer(haid, id, sample, ps);
            int packet = 0;
            do {
                rep = OUT_sendNextPacket(haid, id, packet++, ais, ps);
                runTot += rep.getAudioBytesTransferred();
                prog.setProgress(runTot / (double) tot);
            } while (rep.getMID() != SMDI.SMDI_MSG_EOP);
        } catch (SMDILogicException e) {
            tryAbort(haid, id);
            throw e;
        } catch (IOException e) {
            tryAbort(haid, id);
            throw e;
        } finally {
            // ais.closed();
            prog.setProgress(1);
        }
    }
    /* public static void recvSample(int haid, int id, int sample, AudioOutputStream aos) throws SMDILogicException, IOException {
         OutputStream o;
         //AudioInputStream ais = new AudioInputStream(o.);

     }*/

    private static boolean tryAbort(int haid, int id) {
        SMDIMsg.Abort abort = new SMDIMsg.Abort();
        try {
            if (checkReject(abort.dispatch(haid, id)) == SMDI.SMDI_MSG_ACK)
                return true;
        } catch (ASPILogic.ASPILogicException e1) {
        } catch (ASPILogic.CommandFailedException e1) {
        } catch (SMDILogicException e) {
        } catch (SMDISampleEmptyException e) {
        }
        return false;
    }

    public static final void main(String[] args) {
        try {
            //AudioInputStream ais = AudioSystem.getAudioInputStream(new File("c://whine.wav"));
            //AudioInputStream ais = AudioSystem.getAudioInputStream(new File("c://starter1.wav"));
            // AudioInputStream ais = AudioSystem.getAudioInputStream(new File("c://dayrush.wav"));
            //AudioInputStream ais = AudioSystem.getAudioInputStream(new File("c://test24bit.wav"));
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File("c://test32.wav"));
            sendSample(0, 2, 10, "farts", ais, new ProgressUpdater() {
                // fraction 0..1
                // < 0 signifies inactive
                public void setProgress(double p) {
                    System.out.println(new DecimalFormat("00.00").format(p * 100) + " %");
                }
            });

            //sendSample(0, 2, 1, "farts", AudioSystem.getAudioInputStream(new File("c://starter1.wav")));
            //sendSample(0, 2, 1, "farts", AudioSystem.getAudioInputStream(new File("c://dayrush.wav")));
        } catch (SMDILogicException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SmdiUnsupportedConversionException e) {
            e.printStackTrace();
        }
    }
}

/*  try {
              System.out.println(ASPIMsg.aspiGetSupportInfo.invoke());
          } catch (WrongArgumentNumberException e) {
              e.printStackTrace();
          } catch (IncompatibleArgumentTypeException e) {
              e.printStackTrace();
          }*/
/*
         try {
             System.out.println(ASPILogic.testUnitReady(0, 2));
         } catch (ASPILogic.ASPILogicException e) {
             e.printStackTrace();
         } catch (ASPILogic.CommandFailedException e) {
             e.printStackTrace();
         }
         byte[] reply = null;
         try {
             reply = sendMidi(0, 2, new IdentityRequest((byte) 127).getMessage());
         } catch (SMDILogicException e) {
             e.printStackTrace();
         } catch (SMDISampleEmptyException e) {
             e.printStackTrace();
         }
         for (int i = 1; i < 1000; i++) {
             try {
                 reqSampleHeader(0, 2, i);
                 masterIdentify(0, 2);
             } catch (SMDILogicException e) {
                 e.printStackTrace();
             } catch (SMDISampleEmptyException e) {
                 System.out.println("Empty sample");
             }
             System.out.println(i);
         }
         for (int i = 1; i < 20; i++) {
             try {
                 nameSample(0, 2, i, String.valueOf(i));
                 //deleteSample(0, 2, i);
                 //masterIdentify(0, 2);
             } catch (SMDILogicException e) {
                 e.printStackTrace();
             } catch (SMDISampleEmptyException e) {
                 System.out.println("Empty sample");
             }
             System.out.println(i);
         }
         //deleteSample(0, 2, 2);
         */
