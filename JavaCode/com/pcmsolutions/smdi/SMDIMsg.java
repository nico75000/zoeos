package com.pcmsolutions.smdi;

import com.pcmsolutions.aspi.ASPILogic;
import com.pcmsolutions.system.ZUtilities;

import javax.sound.sampled.AudioFormat;


/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 13-Jan-2004
 * Time: 23:19:25
 * To change this template use Options | File Templates.
 */
class SMDIMsg {
    private static final int MAX_ADD_MSG_LEN = 1013;

    private static abstract class BaseSMDIMsg {
        protected int mid = 0x00000000;
        protected char[] data = new char[0];

        protected BaseSMDIMsg() {
        }

        public char[] dispatch(int haid, int id) throws ASPILogic.ASPILogicException, ASPILogic.CommandFailedException {
            return ASPILogic.performSMDI(haid, id, getMessage(), 256);
        }

        public final char[] getMessage() {
            char[] msg = new char[data.length + 11];
            msg[0] = 'S';
            msg[1] = 'M';
            msg[2] = 'D';
            msg[3] = 'I';
            ZUtilities.applyBytes(msg, mid, 4, 4);
            ZUtilities.applyBytes(msg, data.length, 8, 3);
            System.arraycopy(data, 0, msg, 11, data.length);
            return msg;
        }
    }

    public static class NoMessage extends BaseSMDIMsg {
    }

    public static class MasterIdentify extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_MASTER;
        }
    }

    public static class SlaveIdentify extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_SLAVE;
        }
    }

    public static class MessageReject extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_REJECT;
        }

        public MessageReject setRejectCodes(int rc, int rsc) {
            data = new char[4];
            ZUtilities.applyBytes(data, rc, 0, 2);
            ZUtilities.applyBytes(data, rsc, 2, 2);
            return this;
        }
    }

    public static class Ack extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_ACK;
        }
    }

    public static class Nack extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_NACK;
        }
    }

    public static class Wait extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_WAIT;
        }
    }

    public static class SendNextPacket extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_NEXT;
        }


        public SendNextPacket setPacket(int packet) {
            data = new char[3];
            ZUtilities.applyBytes(data, packet, 0, 3);
            return this;
        }
    }

    public static class EOP extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_EOP;
            ;
        }
    }

    public static class Abort extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_ABORT;
        }
    }

    public static class DataPacket extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_DATA;
        }

        public DataPacket setPacketData(int packet, byte[] bytes) {
            data = new char[3 + bytes.length];
            ZUtilities.applyBytes(data, packet, 0, 3);
            ZUtilities.applyByteArray(data, bytes, 3);
            return this;
        }
    }

    public static class SampleHeaderRequest extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_HDR_REQ;
        }

        public SampleHeaderRequest setSample(int sample) {
            data = new char[3];
            ZUtilities.applyBytes(data, sample, 0, 3);
            return this;
        }
    }

    public static class SampleHeader extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_HDR;
        }

        public static AudioFormat getAudioFormat(char[] data) {
            return new AudioFormat(1000000000.0F / getPeriodInNS(data), getBitsPerWord(data), getChannels(data), true, true);
        }

        public static int getSampleNumber(char[] data) {
            return ZUtilities.extractInt(data, 0, 3);
        }

        public static int getChannels(char[] data) {
            return (int) data[4];
        }

        public static int getBitsPerWord(char[] data) {
            return (int) data[3];
        }

        public static int getPeriodInNS(char[] data) {
            return ZUtilities.extractInt(data, 5, 3);
        }

        public static int getSampleLength(char[] data) {
            return ZUtilities.extractInt(data, 8, 4);
        }

        public static int getLoopStart(char[] data) {
            return ZUtilities.extractInt(data, 12, 4);
        }

        public static int getLoopEnd(char[] data) {
            return ZUtilities.extractInt(data, 16, 4);
        }

        public static int getSamplePitch(char[] data) {
            return ZUtilities.extractInt(data, 21, 2);
        }

        public static int getSamplePitchFraction(char[] data) {
            return ZUtilities.extractInt(data, 23, 2);
        }

        public static int getLoopConrtol(char[] data) {
            return data[20];
        }

        public static String getName(char[] data) {
            char[] name = new char[data[25]];
            System.arraycopy(data, 26, name, 0, data[25]);
            return new String(name);
        }

        public SampleHeader setSample(int sample, String name, long length, AudioFormat f) {
            data = new char[26 + name.length()];

            if (length > Integer.MAX_VALUE)
                throw new IllegalArgumentException("frame length too long for SMDI");

            // sample number
            ZUtilities.applyBytes(data, sample, 0, 3);

            // bits per word
            data[3] = 16;//(char) f.getSampleSizeInBits();

            // channels
            data[4] = (char) f.getChannels();

            // period in nanoseconds
            int pns = (int) Math.round(1000000000.0 / f.getSampleRate());
            ZUtilities.applyBytes(data, pns, 5, 3);

            // sample length
            ZUtilities.applyBytes(data, (int) length, 8, 4);

            // loop stateStart/end
            ZUtilities.applyBytes(data, 0, 12, 4);
            ZUtilities.applyBytes(data, (int) length, 16, 4);

            // loop control
            data[20] = 0;

            // sample pitch
            ZUtilities.applyBytes(data, 60, 21, 2);

            // sample pitch fraction
            ZUtilities.applyBytes(data, 0, 23, 2);

            // name length
            data[25] = (char) name.length();

            // name
            System.arraycopy(name.toCharArray(), 0, data, 26, name.length());

            return this;
        }
    }

    public static class TransferBegin extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_TRANSFER_BEGIN;
        }

        public TransferBegin setSample(int sample, int packetLength) {
            data = new char[6];
            ZUtilities.applyBytes(data, sample, 0, 3);
            ZUtilities.applyBytes(data, packetLength, 3, 3);
            return this;
        }
    }

    public static class TransferAck extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_TRANSFER_ACK;
        }

        public TransferAck setSample(int sample, int packetLength) {
            data = new char[6];
            ZUtilities.applyBytes(data, sample, 0, 3);
            ZUtilities.applyBytes(data, packetLength, 3, 3);
            return this;
        }

        public static int getSample(char[] msg) {
            return ZUtilities.extractInt(msg, 11, 3);
        }

        public static int getPacketLength(char[] msg) {
            return ZUtilities.extractInt(msg, 14, 3);
        }
    }

    public static class SampleName extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_NAME;
        }

        public SampleName setName(int sample, String name) {
            data = new char[4 + name.length()];
            ZUtilities.applyBytes(data, sample, 0, 3);
            data[3] = (char) name.length();
            System.arraycopy(name.toCharArray(), 0, data, 4, name.length());
            return this;
        }
    }

    public static class DeleteSample extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_DELETE;
        }

        public DeleteSample setSample(int sample) {
            data = new char[3];
            ZUtilities.applyBytes(data, sample, 0, 3);
            return this;
        }
    }

    public static class Midi extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_MIDI;
        }

        public Midi setMidi(byte[] midi) {
            data = new char[midi.length];
            ZUtilities.applyByteArray(data, midi, 0);
            return this;
        }
    }

    public static final void main(String[] args) {

        System.out.println();
    }

}
