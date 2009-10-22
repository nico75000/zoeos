package com.pcmsolutions.smdi;

import org.tritonus.zuonics.sampled.aiff.AiffINSTChunk;
import org.tritonus.zuonics.sampled.wave.WaveSmplChunk;
import com.pcmsolutions.aspi.ASPILogic;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.audio.AudioConverter;
import com.pcmsolutions.system.audio.AudioUtilities;

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

    public static int extractAddMsgLen(byte[] arr) {
        if (arr.length < 11)
            throw new IllegalArgumentException("not a valid SMDI message");
        /*int v = 0;
        v += arr[8] << 16;
        v += arr[9] << 8;
        v += arr[10];
        return v;*/
        return ZUtilities.extractUnsignedInt(arr, 8, 3);
    }

    public static class SMDIMsgException extends Exception {

    }

    public static byte[] stripHeader(byte[] msg) {
        if (msg.length < 11) throw new IllegalArgumentException("not a valid SMDI msg");
        byte[] out = new byte[msg.length - 11];
        System.arraycopy(msg, 11, out, 0, msg.length - 11);
        return out;
    }

    private static abstract class BaseSMDIMsg {
        protected int mid = 0x00000000;
        protected byte[] data = new byte[0];

        protected BaseSMDIMsg() {
        }

        protected byte[] checkAddMsgLen(byte[] data) throws SMDIMsgException {
            if (extractAddMsgLen(data) > data.length - 11)
                throw new SMDIMsgException();
            return data;
        }

        public byte[] dispatch(int haid, int id) throws ASPILogic.ASPILogicException, ASPILogic.CommandFailedException, SMDIMsgException {
            byte[] msg = checkAddMsgLen(getMessage());
            return ASPILogic.performSMDI(haid, id, msg, 256);
        }

        public final byte[] getMessage() {
            byte[] msg = new byte[data.length + 11];
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
            data = new byte[4];
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


        int size;

        public SendNextPacket setPacket(int packet, int packetSize) {
            data = new byte[3];
            this.size = packetSize + 16;// +16 to accomodate 11 byte header
            ZUtilities.applyBytes(data, packet, 0, 3);
            return this;
        }

        public byte[] dispatch(int haid, int id) throws ASPILogic.ASPILogicException, ASPILogic.CommandFailedException, SMDIMsgException {
            byte[] msg = checkAddMsgLen(getMessage());
            return ASPILogic.performSMDI(haid, id, msg, size);
        }
    }

    public static class EOP extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_EOP;
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

        public static byte[] getData(byte[] data) {
            int aml = extractAddMsgLen(data);
            byte[] out = new byte[aml - 3];
            for (int i = 0; i < aml - 3; i++)
                out[i] = (byte) data[i + 14];
            return out;
        }

        public DataPacket setPacketData(int packet, byte[] bytes) {
            data = new byte[3 + bytes.length];
            ZUtilities.applyBytes(data, packet, 0, 3);
            System.arraycopy(bytes, 0, data, 3, bytes.length);
            return this;
        }
    }

    public static class SampleHeaderRequest extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_HDR_REQ;
        }

        public SampleHeaderRequest setSample(int sample) {
            data = new byte[3];
            ZUtilities.applyBytes(data, sample, 0, 3);
            return this;
        }
    }

    public static class SampleHeader extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_HDR;
        }

        public static AudioFormat getAudioFormat(byte[] data, SMDIRecvInstance ri) {
            float sr = (int) (1000000000.0F / getPeriodInNS(data));
            int chnls = getChannels(data);
            int bpw = getBitsPerWord(data);

            return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sr, bpw, chnls, AudioConverter.calculatePCMFrameSize(chnls, bpw), sr, true, ri.getPropertiesForSampleHeader(getSampleHeader(data)));
            //   return new AudioFormat(1000000000.0F / getPeriodInNS(data), getBitsPerWord(data), getChannels(data), true, true);
        }

        public static Impl_SmdiSampleHeader getSampleHeader(byte[] data) {
            byte bpw = (byte) SMDIMsg.SampleHeader.getBitsPerWord(data);
            byte chnls = (byte) SMDIMsg.SampleHeader.getChannels(data);
            byte lc = (byte) SMDIMsg.SampleHeader.getLoopControl(data);
            int period = SMDIMsg.SampleHeader.getPeriodInNS(data);
            int length = SMDIMsg.SampleHeader.getSampleLength(data);
            int ls = SMDIMsg.SampleHeader.getLoopStart(data);
            int le = SMDIMsg.SampleHeader.getLoopEnd(data);
            short pitch = (short) SMDIMsg.SampleHeader.getSamplePitch(data);
            short pitchf = (short) SMDIMsg.SampleHeader.getSamplePitchFraction(data);
            String name = SMDIMsg.SampleHeader.getName(data);
            return new Impl_SmdiSampleHeader(true, bpw, chnls, lc, (byte) name.length(), period, length, ls, le, pitch, pitchf, name, 0);
        }

        public static int getSampleNumber(byte[] data) {
            return ZUtilities.extractUnsignedInt(data, 0, 3);
        }

        public static int getChannels(byte[] data) {
            return (int) data[4];
        }

        public static int getBitsPerWord(byte[] data) {
            return (int) data[3];
        }

        public static int getPeriodInNS(byte[] data) {
            return ZUtilities.extractUnsignedInt(data, 5, 3);
        }

        public static int getSampleLength(byte[] data) {
            return ZUtilities.extractUnsignedInt(data, 8, 4);
        }

        public static int getLoopStart(byte[] data) {
            return ZUtilities.extractUnsignedInt(data, 12, 4);
        }

        public static int getLoopEnd(byte[] data) {
            return ZUtilities.extractUnsignedInt(data, 16, 4);
        }

        public static int getSamplePitch(byte[] data) {
            return ZUtilities.extractUnsignedInt(data, 21, 2);
        }

        public static int getSamplePitchFraction(byte[] data) {
            return ZUtilities.extractUnsignedInt(data, 23, 2);
        }

        public static int getLoopControl(byte[] data) {
            return data[20];
        }

        public static String getName(byte[] data) {
            return new String(ZUtilities.extractChars(data, 26, data[25]));
        }

        public SampleHeader setSample(int sample, String name, long length, AudioFormat f) {
            data = new byte[26 + name.length()];

            //if (length > Integer.MAX_VALUE)
            //    throw new IllegalArgumentException("frame length too long for SMDI");

            // sample number
            ZUtilities.applyBytes(data, sample, 0, 3);

            // bits per word
            data[3] = (byte) f.getSampleSizeInBits();

            // channels
            data[4] = (byte) f.getChannels();

            // period in nanoseconds
            int pns = (int) Math.round(1000000000.0 / f.getSampleRate());
            ZUtilities.applyBytes(data, pns, 5, 3);

            // sample length
            ZUtilities.applyBytes(data, (int) length, 8, 4);

            AudioUtilities.AudioSampleLoop asl = AudioUtilities.getFirstLoop(f, (int)length);

            // loop start/end
            ZUtilities.applyBytes(data, asl.getLoopStart(), 12, 4);
            ZUtilities.applyBytes(data, asl.getLoopEnd(), 16, 4);

            // loop control
            data[20] = (byte)asl.getLoopControl();

            // sample pitch
            ZUtilities.applyBytes(data, 60, 21, 2);

            // sample pitch fraction
            ZUtilities.applyBytes(data, 0, 23, 2);

            // name length
            data[25] = (byte) name.length();

            // name
            System.arraycopy(ZUtilities.applyToByteArray(name.toCharArray(), 0, name.length()), 0, data, 26, name.length());

            return this;
        }
    }

    public static class TransferBegin extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_TRANSFER_BEGIN;
        }

        public TransferBegin setSample(int sample, int packetLength) {
            data = new byte[6];
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
            data = new byte[6];
            ZUtilities.applyBytes(data, sample, 0, 3);
            ZUtilities.applyBytes(data, packetLength, 3, 3);
            return this;
        }

        public static int getSample(byte[] msg) {
            return ZUtilities.extractUnsignedInt(msg, 11, 3);
        }

        public static int getPacketLength(byte[] msg) {
            return ZUtilities.extractUnsignedInt(msg, 14, 3);
        }
    }

    public static class SampleName extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_NAME;
        }

        public SampleName setName(int sample, String name) {
            data = new byte[4 + name.length()];
            ZUtilities.applyBytes(data, sample, 0, 3);
            data[3] = (byte) name.length();
            System.arraycopy(name.toCharArray(), 0, data, 4, name.length());
            return this;
        }
    }

    public static class DeleteSample extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_DELETE;
        }

        public DeleteSample setSample(int sample) {
            data = new byte[3];
            ZUtilities.applyBytes(data, sample, 0, 3);
            return this;
        }
    }

    public static class Midi extends BaseSMDIMsg {
        {
            mid = SMDI.SMDI_MSG_MIDI;
        }

        public Midi setMidi(byte[] midi) {
            data = new byte[midi.length];
            System.arraycopy(midi, 0, data, 0, midi.length);
            return this;
        }
    }

    public static final void main(String[] args) {

        System.out.println();
    }

}
