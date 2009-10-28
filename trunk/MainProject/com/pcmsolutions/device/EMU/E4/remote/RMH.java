package com.pcmsolutions.device.EMU.E4.remote;

import com.pcmsolutions.comms.*;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.*;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.util.ClassUtility;

import javax.sound.midi.SysexMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.DecimalFormat;

/**
 * User: paulmeehan
 * Date: 25-Aug-2004
 * Time: 09:35:24
 */
class RMH {
    private final static int HDR_SIZE = 6;
    private final static byte[] cmdTail = new byte[]{(byte) 0xF7};
    private final static byte[] cmdCSTail = new byte[]{(byte) 0x7F, (byte) 0xF7};

    private final static byte PARAMETER_VALUE_EDIT_CMD = 0x01;
    private final static byte PARAMETER_VALUE_REQ = 0x02;
    private final static byte PARAMETER_MMD_REQ = 0x03;
    private final static byte PRESET_NAME_CMD = 0x05;
    private final static byte PRESET_NAME_REQ = 0x06;
    private final static byte SAMPLE_NAME_CMD = 0x09;
    private final static byte SAMPLE_NAME_REQ = 0x0A;
    private final static byte NEW_PRESET_DUMP_CMD = 0x0d;
    private final static byte NEW_PRESET_DUMP_HEADER_SUB_CMD = 0x03;
    private final static byte NEW_PRESET_DUMP_REQ_SUB_CMD = 0x05;
    private final static byte NEW_PRESET_DUMP_DATA_SUB_CMD = 0x04;
    private final static byte PRESET_MEMORY_REQ = 0x10;
    private final static byte SAMPLE_MEMORY_REQ = 0x12;
    private final static byte CONFIG_REQ = 0x14;
    private final static byte PRESET_VOICES_REQ = 0x16;
    private final static byte PRESET_LINKS_REQ = 0x18;
    private final static byte PRESET_ZONES_REQ = 0x1a;
    private final static byte VOICE_ZONES_REQ = 0x1c;
    private final static byte EX_CONFIG_REQ = 0x1e;
    private final static byte NEW_VOICE_CMD = 0x20;
    private final static byte DELETE_VOICE_CMD = 0x21;
    private final static byte COPY_VOICE_CMD = 0x22;
    private final static byte NEW_SAMPLE_ZONE_CMD = 0x30;
    private final static byte GET_MULTISAMPLE_CMD = 0x31;
    private final static byte DELETE_ZONE_CMD = 0x32;
    private final static byte COMBINE_VOICES_CMD = 0x33;
    private final static byte EXPAND_VOICE_CMD = 0x34;
    private final static byte NEW_LINK_CMD = 0x40;
    private final static byte DELETE_LINK_CMD = 0x41;
    private final static byte COPY_LINK_CMD = 0x42;
    private final static byte DELETE_SAMPLE_CMD = 0x50;
    private final static byte SAMPLE_DEFRAG_CMD = 0x52;
    private final static byte COPY_PRESET_CMD = 0x70;
    private final static byte DELETE_PRESET_CMD = 0x71;
    private final static byte MULTI_MODE_DUMP_CMD = 0x72;
    private final static byte MULTI_MODE_DUMP_REQ = 0x73;
    private final static byte ERASE_RAM_BANK_CMD = 0x74;
    private final static byte ERASE_RAM_PRESETS_CMD = 0x75;
    private final static byte ERASE_RAM_SAMPLES_CMD = 0x76;
    private final static byte NACK_CMD = 0x79;
    private final static byte ACK_CMD = 0x7a;
    private final static byte EOF_CMD = 0x7b;
    private final static byte WAIT_CMD = 0x7c;
    private final static byte CANCEL_CMD = 0x7d;

    static final int PEPTALK_HDR_SIZE = 5;
    static final byte PEPTALK_NOTE_ON = 0x20;
    static final byte PEPTALK_NOTE_OFF = 0x21;
    static final byte PEPTALK_OPEN = 0x10;
    static final byte PEPTALK_CLOSE = 0x11;
    static final byte PEPTALK_BUTTON_EVENT = 0x40;

    private static final byte PEPTALK_BUTTON_PRESS = 0x01;
    private static final byte PEPTALK_BUTTON_RELEASE = 0x00;

    // BUTTON IDS
    /*
        Preset Manage               0x58
       Sample Manage               0x59
       Preset Edit                 0x5A
       Sample Edit                 0x5B
       Master                      0x5C
       Disk                        0x5D
       Exit                        0x5E
       Assignable Key 1            0x5F
       Assignable Key 2            0x60

       Softkey F1                  0x62
       Assignable Key 3            0x63
       Softkey F2                  0x64
       Audition                    0x65
       Softkey F3                  0x66

       Softkey F4                  0x68
       Page Prev                   0x69
       Softkey F5                  0x6A
       Page Next                   0x6B
       Softkey F6                  0x6C
       Enter                       0x6D
       Up Arrow                    0x6E
       Left Arrow                  0x6F
       Right Arrow                 0x70
       Down Arrow                  0x71
       Dec                         0x72
       Inc                         0x73
       Numeric Keypad 1            0x74
       Numeric Keypad 2            0x75
       Numeric Keypad 3            0x76
       Numeric Keypad 4            0x77
       Numeric Keypad 5            0x78
       Numeric Keypad 6            0x79
       Numeric Keypad 7            0x7A
       Numeric Keypad 8            0x7B
       Numeric Keypad 9            0x7C
       Numeric Keypad +/-          0x7D
       Numeric Keypad 0            0x7E
       Numeric Keypad . (dot)      0x7F
    */
    static final byte PEPTALK_EXIT = 0x5E;
    static final byte PEPTALK_SAMPLE_EDIT = 0x5B;
    static final byte PEPTALK_RIGHT_ARROW = 0x70;

    private final static int WAIT_TIME = 200;

    // HELPERS
    private static ByteArrayOutputStream genHeader(byte[] hdr, byte cmd) throws RemoteMessagingException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(HDR_SIZE);
        try {
            os.write(hdr);
            os.write(cmd);
        } catch (IOException e) {
            throw new RemoteMessagingException(e.getMessage());
        }
        return os;
    }

    private static SysexMessage wrapCmd(ByteArrayOutputStream os) throws RemoteMessagingException {
        SysexMessage m = new SysexMessage();
        try {
            os.write(cmdTail);
            m.setMessage(os.toByteArray(), os.size());
            os.close();
        } catch (Exception e) {
            throw new RemoteMessagingException(e.getMessage());
        }
        return m;

    }

    private static SysexMessage wrapChecksummedCmd(ByteArrayOutputStream os) throws RemoteMessagingException {
        SysexMessage m = new SysexMessage();
        try {
            os.write(cmdCSTail);
            m.setMessage(os.toByteArray(), os.size());
            os.close();
        } catch (Exception e) {
            throw new RemoteMessagingException(e.getMessage());
        }
        return m;

    }

    private static SysexMessage wrapChecksummedCmd(ByteArrayOutputStream os, byte cs) throws RemoteMessagingException {
        SysexMessage m = new SysexMessage();
        try {
            //if ( cs < 0)
            //  cs+=128;
            os.write(cs);
            os.write(cmdTail);
            m.setMessage(os.toByteArray(), os.size());
            os.close();
        } catch (Exception e) {
            throw new RemoteMessagingException(e.getMessage());
        }
        return m;

    }

    private static void dispatchCmd(SysexMessage m, RawDevice d, long pause) throws IllegalStateException, RemoteUnreachableException {
        synchronized (d) {
            d.outlet.dispatch(m, 0, pause);
        }
    }

    private static Object dispatchCmdReply(SysexMessage m, RawDevice d, Filterable f) throws RemoteUnreachableException, RemoteDeviceDidNotRespondException, RemoteMessagingException {
        synchronized (d) {
            return d.inlet.dispatchAndWaitForReply(d.outlet, m, f);
        }
    }

    private static Object[] dispatchCmdLongReply(SysexMessage m, RawDevice d, Filterable f) throws RemoteUnreachableException, RemoteDeviceDidNotRespondException, RemoteMessagingException {
        synchronized (d) {
            return d.inlet.dispatchAndWaitForLongReply(d.outlet, m, f);
        }
    }

    // private static Object waitForReply(RawDevice d, Filterable f) throws RemoteMessagingException, RemoteDeviceDidNotRespondException {
    //   synchronized (d) {
    //     return d.inlet.waitForReply(f);
    //   }
    // }

    private static Object[] dispatchCmdReplies(SysexMessage m, RawDevice d, Filterable f, int numReplies) throws RemoteUnreachableException, RemoteDeviceDidNotRespondException, RemoteMessagingException {
        synchronized (d) {
            return d.inlet.dispatchAndWaitForReplies(d.outlet, m, f, numReplies);
        }
    }

    private static Object[] dispatchCmdLongReplies(SysexMessage m, RawDevice d, Filterable f, int numReplies) throws RemoteUnreachableException, RemoteDeviceDidNotRespondException, RemoteMessagingException {
        synchronized (d) {
            return d.inlet.dispatchAndWaitForLongReplies(d.outlet, m, f, numReplies);
        }
    }

    private static ByteArrayOutputStream putInteger(ByteArrayOutputStream os, Integer intv) throws RemoteMessagingException {
        try {
            os.write(SysexHelper.DataOut(intv.shortValue()));
        } catch (Exception e) {
            throw new RemoteMessagingException("Midi message construction error");
        }
        return os;
    }

    private static ByteArrayOutputStream putByte(ByteArrayOutputStream os, byte bytev) throws RemoteMessagingException {
        try {
            os.write(bytev);
        } catch (Exception e) {
            throw new RemoteMessagingException("Midi message construction error");
        }
        return os;
    }

    private static ByteArrayOutputStream putName(ByteArrayOutputStream os, String name) throws RemoteMessagingException {
        try {
            //StringBuffer buf = new StringBuffer(name);
            //buf.setLength(16);
            //name = ;
            os.write(ZUtilities.makeExactLengthString(name, 16, ' ', true).getBytes("US-ASCII"));
        } catch (Exception e) {
            throw new RemoteMessagingException("Midi message construction error");
        }
        return os;
    }

    private static ByteArrayOutputStream putStream(ByteArrayOutputStream os, ByteArrayInputStream is) throws RemoteMessagingException {
        try {
            int cnt = is.available();
            byte[] data = new byte[cnt];
            if (is.read(data, 0, cnt) == cnt) {
                os.write(data, 0, cnt);
            } else
                throw new RemoteMessagingException("Midi message construction error");

        } catch (Exception e) {
            throw new RemoteMessagingException("Midi message construction error");
        }
        return os;
    }

    private static ByteArrayOutputStream putIntegers(ByteArrayOutputStream os, Integer[] ints) throws RemoteMessagingException {
        try {
            int len = ints.length;
            os.write((byte) (len));
            for (int n = 0; n < len; n++) {
                os.write(SysexHelper.DataOut(ints[n].shortValue()));
            }
        } catch (Exception e) {
            throw new RemoteMessagingException("Midi message construction error");
        }
        return os;
    }

    private static ByteArrayOutputStream putBytes(ByteArrayOutputStream os, byte[] bytes, boolean putLength) throws RemoteMessagingException {
        try {
            if (putLength)
                os.write((byte) (bytes.length / 2));
            os.write(bytes);
        } catch (Exception e) {
            throw new RemoteMessagingException("Midi message construction error");
        }
        return os;
    }

    private static ByteArrayOutputStream putBytes(ByteArrayOutputStream os, byte[] bytes, boolean putLength, int count) throws RemoteMessagingException {
        try {
            if (putLength)
                os.write((byte) (bytes.length / 2));
            os.write(bytes, 0, count);
        } catch (Exception e) {
            throw new RemoteMessagingException("Midi message construction error");
        }
        return os;
    }

    // REQUESTS

    public static Remotable.DeviceConfig reqConfig(final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (Remotable.DeviceConfig) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), CONFIG_REQ)), d, d.filterConfigReq);
    }

    public static Remotable.DeviceExConfig reqExConfig(final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (Remotable.DeviceExConfig) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), EX_CONFIG_REQ)), d, d.filterExConfigReq);
    }

    public static Integer reqPresetVoices(Integer preset, final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (Integer) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_VOICES_REQ), preset)), d, d.filterPresetVoicesReq);
    }

    public static Integer reqPresetZones(Integer preset, final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (Integer) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_ZONES_REQ), preset)), d, d.filterPresetZonesReq);
    }

    public static Integer reqVoiceZones(Integer preset, Integer voice, final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (Integer) dispatchCmdReply(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), VOICE_ZONES_REQ), preset), voice)), d, d.filterVoiceZonesReq);
    }

    public static Integer reqPresetLinks(Integer preset, final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (Integer) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_LINKS_REQ), preset)), d, d.filterPresetLinksReq);
    }

    public static Remotable.PresetMemory reqPresetMemory(final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (Remotable.PresetMemory) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), PRESET_MEMORY_REQ)), d, d.filterPresetMemoryReq);
    }

    public static Remotable.SampleMemory reqSampleMemory(final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (Remotable.SampleMemory) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), SAMPLE_MEMORY_REQ)), d, d.filterSampleMemoryReq);
    }

    private static boolean pepopen = false;

    public static String reqPresetName(Integer preset, final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        // 0xf0,0x18, 0x7f(peptalk id), 0x01(midiDevId), 0x00(masterID), 0x10(peptalk cmd), 0xf7
        /*  try {
              byte[] openPeptalk = new byte[]{Remotable.BOX, Remotable.EMU_ID, 0x7f, 0x00, 0x07, 0x10, EOX, 0, 0, (byte)0xe4};
              byte[] closePeptalk = new byte[]{Remotable.BOX, Remotable.EMU_ID, 0x7f, 0x00, 0x07, 0x11, EOX};
              byte[] masterVolReq = new byte[]{Remotable.BOX, Remotable.EMU_ID, 0x7f, 0x00, 0x07, 0x62, EOX};
              byte[] ledStateReq = new byte[]{Remotable.BOX, Remotable.EMU_ID, 0x7f, 0x00, 0x07, 0x44, EOX};
              byte[] fullDisplayReq = new byte[]{Remotable.BOX, Remotable.EMU_ID, 0x7f, 0x00, 0x07, 0x51, EOX};
              byte[] delVoice = wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), DELETE_VOICE_CMD), IntPool.get(0)), IntPool.get(1))).getMessage();

              //byte[] reply = SMDILogic.sendMidi(0, 2, masterVolReq);
              byte[] reply = null;

              if (!pepopen) {
              //    reply = SMDILogic.sendMidi(0, 2, closePeptalk);
                  reply = SMDILogic.sendMidi(0, 2, openPeptalk);
                  System.out.println(new String(reply));
                  pepopen = true;
              }
              reply = SMDILogic.sendMidi(0, 2, fullDisplayReq);
              //reply = SMDILogic.sendMidi(0, 2, delVoice);
              System.out.println(new String(reply));
              //reply = SMDILogic.sendMidi(0, 2, wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_NAME_REQ), preset)).getMessage());
              //reply = SMDILogic.sendMidi(0, 2, new byte[]{0,1,2,3,4});
              //System.out.println(new String(reply));
              System.out.println(reply.length);
          } catch (SMDILogic.SMDILogicException e) {
              e.printStackTrace();
          } catch (SmdiOutOfRangeException e) {
              e.printStackTrace();
          } catch (SmdiNoMemoryException e) {
              e.printStackTrace();
          } catch (SmdiEmptyException e) {
              e.printStackTrace();
          }
        */
        return (String) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PRESET_NAME_REQ), preset)), d, d.filterPresetNameReq);
        //return "test";
    }

    public static String reqSampleName(Integer sample, final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (String) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), SAMPLE_NAME_REQ), sample)), d, d.filterSampleNameReq);
    }

    public static MinMaxDefault reqMMD(Integer id, final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (MinMaxDefault) dispatchCmdReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), PARAMETER_MMD_REQ), id)), d, d.filterMMDReq);
    }

    final static int multiParamChunkSize = 8;
    public static Integer[] reqParameterValues(Integer[] parameters, final RawDevice d, boolean returnIdVals) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        ArrayList<Integer> returnVals = new ArrayList<Integer>();
        Integer[] currParams;
        int ptr = 0;
        while (ptr < parameters.length) {
            if (ptr + multiParamChunkSize < parameters.length) {
                currParams = new Integer[multiParamChunkSize];
                System.arraycopy(parameters, ptr, currParams, 0, multiParamChunkSize);
                ptr += multiParamChunkSize;
            } else {
                currParams = new Integer[parameters.length - ptr];
                System.arraycopy(parameters, ptr, currParams, 0, parameters.length - ptr);
                ptr += parameters.length - ptr;
            }
            returnVals.addAll(Arrays.asList(task_reqParameterValues(currParams, d,returnIdVals)));
        }
        return returnVals.toArray(new Integer[returnVals.size()]);
    }

     static Integer[] task_reqParameterValues(Integer[] parameters, final RawDevice d, boolean returnIdVals) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        int num = parameters.length;
        Object[] replies = dispatchCmdReplies(wrapChecksummedCmd(putIntegers(genHeader(d.getRemoteHeader(), PARAMETER_VALUE_REQ), parameters)), d, d.filterParameterValuesReq, num);
        int numIdVals = num * 2;
        Object reply;
        if (replies != null && replies.length >= num) {
            Integer[] output;
            if (returnIdVals)
                output = new Integer[numIdVals];
            else
                output = new Integer[numIdVals / 2];

            for (int n = 0; n < numIdVals; n += 2) {
                reply = replies[n / 2];
                if (reply != null && reply instanceof Integer[] && ((Integer[]) reply).length > 1) {
                    // E4 sysex spec states that it will return one parameter edit message for each requested parameter
                    // so there should not be more than one parameter/value in each message
                    // so only access 2 integers from output - there shouldn't be any more!!
                    if (returnIdVals) {
                        output[n] = ((Integer[]) reply)[0];
                        output[n + 1] = ((Integer[]) reply)[1];
                    } else
                        output[n / 2] = ((Integer[]) reply)[1];
                } else
                    return null;
            }
            return output;
        } else
            throw new RemoteMessagingException("Not enough replies ( or no reply ) to a request for parameter values");
    }

    public static MultiModeMap reqMultiModeDump(final RawDevice d) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        return (MultiModeMap) dispatchCmdReply(wrapCmd(genHeader(d.getRemoteHeader(), MULTI_MODE_DUMP_REQ)), d, d.filterMultiModeMapReq);
    }

    public static void editPresetDump(final RawDevice d, ByteArrayInputStream dump, DumpMonitor mon) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException, IOException {
        Object[] headerReplies;
        int lastDataPacketSize = -1;
        synchronized (d) {
            ZMidiSystem.midiLock.access();
            try {
                byte[] leadBytes = new byte[14];
                dump.read(leadBytes);
                headerReplies = dispatchCmdLongReply(wrapCmd(putBytes(putByte(genHeader(d.getRemoteHeader(), NEW_PRESET_DUMP_CMD), NEW_PRESET_DUMP_HEADER_SUB_CMD), leadBytes, false)), d, d.filterPresetDumpReq);
                int i = ClassUtility.firstIndexOfClass(headerReplies, RawDevice.NewDumpStatus.class, false);
                if (i != -1 && ((RawDevice.NewDumpStatus) headerReplies[i]).getStatus() == RawDevice.NewDumpStatus.NEW_ACK) {
                    if (mon != null)
                        mon.setStatus(0);
                    doEditDumpLoop(d, (RawDevice.NewDumpStatus) headerReplies[i], dump, lastDataPacketSize, mon);
                } else if (i != -1 && ((RawDevice.NewDumpStatus) headerReplies[i]).getStatus() == RawDevice.NewDumpStatus.WAIT) {
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mon != null)
                        mon.setStatus(0);
                    doEditDumpLoop(d, (RawDevice.NewDumpStatus) headerReplies[i], dump, lastDataPacketSize, mon);
                } else {
                    cmdCancel(d);
                    throw new RemoteMessagingException("Incorrect response to preset dump header");
                }
            } finally {
                ZMidiSystem.midiLock.unlock();
                if (mon != null)
                    mon.setStatus(0);
            }
        }
    }

    private static void doEditDumpLoop(RawDevice d, RawDevice.NewDumpStatus reply, ByteArrayInputStream dump, int lastDataPacketSize, DumpMonitor mon) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException {
        byte[] data = new byte[244];
        Object[] robjs;
        int packetCount = 1;
        int nackCount = 0;
        byte checksum;
        dump.mark(0);
        double dump_tot = dump.available();
        while (true) {
            switch (reply.getStatus()) {
                case RawDevice.NewDumpStatus.NEW_ACK:
                    //nackCount = 0;
                    dump.mark(0);
                    checksum = 0;
                    int av = dump.available();
                    if (av == 0) {
                        cmdEof(d);
                        return;
                    } else if (av < 244) {
                        dump.read(data, 0, av);
                        for (int i = 0, j = av; i < j; i++)
                                //checksum += (data[i] < 0 ? (~data[i] + 128) : ~data[i]);
                            checksum += data[i];
                    } else {
                        dump.read(data, 0, 244);
                        for (int i = 0, j = 244; i < j; i++)
                                //checksum += (data[i] < 0 ? (~data[i] + 128) : ~data[i]);
                            checksum += data[i];
                    }
                    checksum = (byte) ~checksum;
                    if (checksum < 0)
                        checksum += 128;
                    robjs = dispatchCmdLongReply(wrapChecksummedCmd(putBytes(putInteger(putByte(genHeader(d.getRemoteHeader(), NEW_PRESET_DUMP_CMD), NEW_PRESET_DUMP_DATA_SUB_CMD), IntPool.get(packetCount++)), data, false, (av < 244 ? av : 244)), (byte) checksum), d, d.filterPresetDumpReq);
                    int i = ClassUtility.firstIndexOfClass(robjs, RawDevice.NewDumpStatus.class, false);
                    if (i == -1) {
                        cmdCancel(d);
                        throw new RemoteMessagingException("Incorrect reply during preset dump loop");
                    }
                    reply = (RawDevice.NewDumpStatus) robjs[i];
                    break;
                case RawDevice.NewDumpStatus.NEW_NACK:
                    if (nackCount++ < 5) {
                        dump.reset();
                        packetCount--;
                        final int pc = packetCount;
                        reply = new RawDevice.NewDumpStatus() {
                            public int getStatus()   // returns one of "NEW ACK", "NEW NACK", "WAIT", "CANCEL", "EOF"
                            {
                                return NEW_ACK;
                            }

                            public int getPacketNumber() {
                                return pc;
                            }
                        };
                        break;
                    }
                    cmdCancel(d);
                    throw new RemoteMessagingException("Too many NACKS");
                case RawDevice.NewDumpStatus.WAIT:
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final int pn = reply.getPacketNumber();
                    reply = new RawDevice.NewDumpStatus() {
                        public int getStatus()   // returns one of "NEW ACK", "NEW NACK", "WAIT", "CANCEL", "EOF"
                        {
                            return NEW_ACK;
                        }

                        public int getPacketNumber() {
                            return pn;
                        }
                    };
                    break;
                    //System.out.println("CANCELLED BECAUSE OF WAIT");
                default:
                    //System.out.println("CANCELLED BECAUSE OF UNKNOWN");
                    // CANCEL, EOF OR UNKNOWN
                    cmdCancel(d);
                    throw new RemoteMessagingException("Incorrect reply during preset dump loop");
            }
            if (mon != null)
                mon.setStatus(-(dump_tot - dump.available()) / dump_tot);
        }
    }

    public static ByteArrayInputStream reqPresetDump(Integer preset, final RawDevice d, DumpMonitor mon) throws RemoteMessagingException, RemoteDeviceDidNotRespondException, RemoteUnreachableException, EmptyException {
        Object[] replies;
        ArrayList dataPackets = new java.util.ArrayList();
        synchronized (d) {
            int totalBytes;
            ZMidiSystem.midiLock.access();
            try {
                replies = dispatchCmdLongReply(wrapCmd(putInteger(putByte(genHeader(d.getRemoteHeader(), NEW_PRESET_DUMP_CMD), NEW_PRESET_DUMP_REQ_SUB_CMD), preset)), d, d.filterPresetDumpReq);
                int i = ClassUtility.firstIndexOfClass(replies, RawDevice.NewDumpHeader.class, false);
                if (i != -1) {
                    dataPackets.add(replies[i]);
                    totalBytes = ((RawDevice.NewDumpHeader) replies[i]).getNumDataBytes();
                    if (mon != null)
                        mon.setStatus(0);
                } else {
                    i = ClassUtility.firstIndexOfClass(replies, RawDevice.NewDumpStatus.class, false);
                    if (i != -1 && ((RawDevice.NewDumpStatus) replies[i]).getStatus() == RawDevice.NewDumpStatus.CANCEL)
                        throw new EmptyException(preset);
                    else {
                        cmdCancel(d);
                        throw new RemoteMessagingException("First replies from dump request was not a dump header");
                    }
                }
                doMainDumpLoop(d, mon, totalBytes, dataPackets);
                return dumpPacketsToInputStream(dataPackets);
            } finally {
                ZMidiSystem.midiLock.unlock();
                mon.setStatus(0);
            }
        }
    }

    private static void doMainDumpLoop(RawDevice d, DumpMonitor mon, int totalBytes, ArrayList dataPackets) throws RemoteUnreachableException, RemoteMessagingException, RemoteDeviceDidNotRespondException {
        Object[] replies;
        boolean done = false;
        int rpc = 0;
        int runningDataByteCount = 0;
        while (!done) {
            replies = cmdAck(IntPool.get(rpc++), d);
            if (replies == null) {
                cmdCancel(d);
                throw new RemoteMessagingException("No replies to packet acknowledge");
            }
            int i = ClassUtility.firstIndexOfClass(replies, RawDevice.NewDumpData.class, false);

            if (i != -1) {
                dataPackets.add(replies[i]);
                runningDataByteCount += ((RawDevice.NewDumpData) replies[i]).getByteCount();
                if (mon != null)
                    mon.setStatus((double) runningDataByteCount / (double) totalBytes);
            } else {
                i = ClassUtility.firstIndexOfClass(replies, RawDevice.NewDumpStatus.class, false);
                if (i != -1)
                    switch (((RawDevice.NewDumpStatus) replies[i]).getStatus()) {
                        case RawDevice.NewDumpStatus.EOF:
                            done = true;
                            break;
                        case RawDevice.NewDumpStatus.CANCEL:
                            throw new RemoteMessagingException("Dump cbCancelled by remote");
                        default:
                            cmdCancel(d);
                            throw new RemoteMessagingException("Invalid dump response");
                    }
                else
                    throw new RemoteMessagingException("Invalid dump response");
            }
        }
    }

    private static ByteArrayInputStream dumpPacketsToInputStream(ArrayList dataPackets) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int num = dataPackets.size();
        if (num > 2) {      // must have header + at least one data packet
            try {
                os.write(((RawDevice.NewDumpHeader) dataPackets.get(0)).getHeaderBytes());

                for (int n = 1; n < num; n++)
                    os.write(((RawDevice.NewDumpData) dataPackets.get(n)).getData());

                return new ByteArrayInputStream(os.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Object[] cmdAck(Integer packet, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException, RemoteDeviceDidNotRespondException {
        return dispatchCmdLongReply(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), ACK_CMD), packet)), d, d.filterPresetDumpReq);
    }

    // COMMANDS

    public static void cmdEditParameters(Integer[] idVals, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapChecksummedCmd(putIntegers(genHeader(d.getRemoteHeader(), PARAMETER_VALUE_EDIT_CMD), idVals)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    /*
     public static void cmdEditMultiModeDump(MultiModeMap map, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
         dispatchCmd(wrapChecksummedCmd(putBytes(genHeader(d.getRemoteHeader(), MULTI_MODE_DUMP_CMD), map.getByteStream().)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
     }
     */
    static void cmdEditParametersBytes(byte[] idValBytes, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapChecksummedCmd(putBytes(genHeader(d.getRemoteHeader(), PARAMETER_VALUE_EDIT_CMD), idValBytes, true)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    static void cmdPresetName(Integer preset, String name, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putName(putInteger(genHeader(d.getRemoteHeader(), PRESET_NAME_CMD), preset), name)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void cmdSampleName(Integer sample, String name, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putName(putInteger(genHeader(d.getRemoteHeader(), SAMPLE_NAME_CMD), sample), name)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void cmdNewZone(Integer preset, Integer voice, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), NEW_SAMPLE_ZONE_CMD), preset), voice)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
    }

    public static void cmdNewVoice(Integer preset, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), NEW_VOICE_CMD), preset)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
    }

    public static void cmdNewLink(Integer preset, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), NEW_LINK_CMD), preset)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void cmdMultiModeDump(ByteArrayInputStream is, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putStream(genHeader(d.getRemoteHeader(), MULTI_MODE_DUMP_CMD), is)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void cmdCancel(RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), CANCEL_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void cmdEof(RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), EOF_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void cmdGetMultiSample(Integer srcPreset, Integer srcVoice, Integer destPreset, Integer destVoice, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(putInteger(putInteger(putInteger(genHeader(d.getRemoteHeader(), GET_MULTISAMPLE_CMD), srcPreset), srcVoice), destPreset), destVoice)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void cmdDeleteZone(Integer preset, Integer voice, Integer zone, final RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(putInteger(putInteger(genHeader(d.getRemoteHeader(), DELETE_ZONE_CMD), preset), voice), zone)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void cmdSampleDefrag(RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), SAMPLE_DEFRAG_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 100);
    }

    public static void cmdEraseBank(RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), ERASE_RAM_BANK_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 100);
    }

    public static void cmdEraseRAMPresets(RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), ERASE_RAM_PRESETS_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 50);
    }

    public static void cmdEraseRAMSamples(RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(genHeader(d.getRemoteHeader(), ERASE_RAM_SAMPLES_CMD)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 50);
    }

    public static void cmdCopyLink(Integer srcPreset, Integer link, Integer destPreset, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(putInteger(putInteger(genHeader(d.getRemoteHeader(), COPY_LINK_CMD), srcPreset), link), destPreset)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
    }

    public static void cmdCopyVoice(Integer srcPreset, Integer voice, Integer destPreset, Integer destGroup, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putByte(putInteger(putInteger(putInteger(genHeader(d.getRemoteHeader(), COPY_VOICE_CMD), srcPreset), voice), destPreset), (byte) (destGroup.intValue() - 1))), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
    }

    public static void cmdDeleteLink(Integer preset, Integer link, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), DELETE_LINK_CMD), preset), link)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 2);
    }

    public static void cmdCombineVoices(Integer preset, Integer group, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), COMBINE_VOICES_CMD), preset), IntPool.get(group.intValue() - 1))), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 5);
    }

    public static void cmdExpandVoice(Integer preset, Integer voice, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), EXPAND_VOICE_CMD), preset), voice)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 5);
    }

    public static void cmdDeleteVoice(Integer preset, Integer voice, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), DELETE_VOICE_CMD), preset), voice)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
    }

    public static void cmdCopyPreset(Integer srcPreset, Integer destPreset, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(putInteger(genHeader(d.getRemoteHeader(), COPY_PRESET_CMD), srcPreset), destPreset)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 3);
    }

    public static void cmdDeletePreset(Integer preset, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), DELETE_PRESET_CMD), preset)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 5);
    }

    public static void cmdDeleteSample(Integer sample, RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putInteger(genHeader(d.getRemoteHeader(), DELETE_SAMPLE_CMD), sample)), d, d.getRemotePreferences().ZPREF_commPause.getValue() * 5);
    }

    public static void peptalkOpenSession(RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(genHeader(d.getPeptalkHeader(), PEPTALK_OPEN)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void peptalkCloseSession(RawDevice d) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(genHeader(d.getPeptalkHeader(), PEPTALK_CLOSE)), d, d.getRemotePreferences().ZPREF_commPause.getValue());
    }

    public static void peptalkNoteOn(RawDevice d, byte note, byte vel, byte track) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putByte(putByte(putByte(genHeader(d.getPeptalkHeader(), PEPTALK_NOTE_ON), note), vel), track)), d, 0/* d.getRemotePreferences().ZPREF_commPause.getValue()*/);
    }

    public static void peptalkNoteOff(RawDevice d, byte note, byte vel, byte track) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putByte(putByte(putByte(genHeader(d.getPeptalkHeader(), PEPTALK_NOTE_OFF), note), vel), track)), d, 0/*d.getRemotePreferences().ZPREF_commPause.getValue()*/);
    }

    public static void peptalkButtonEvent(RawDevice d, byte lowButtonID, byte highButtonID, boolean down) throws RemoteMessagingException, RemoteUnreachableException {
        dispatchCmd(wrapCmd(putByte(putByte(putByte(genHeader(d.getPeptalkHeader(), PEPTALK_BUTTON_EVENT), lowButtonID), highButtonID), (down ? PEPTALK_BUTTON_PRESS : PEPTALK_BUTTON_RELEASE))), d, 0/*d.getRemotePreferences().ZPREF_commPause.getValue()*/);
    }

    // FILTERS
    static Remotable.PresetMemory filterPresetMemoryReq(ByteStreamable o, byte[] rmtHeader) {
        Remotable.PresetMemory robj = null;
        ByteArrayInputStream is = o.getByteStream();
        byte[] header = new byte[rmtHeader.length];

        if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x11) {
            byte[] data = new byte[4];  // big enough to accomodate both 2 byte fields

            if (is.read(data, 0, 4) == 4) {
                final Integer presetMemory = SysexHelper.DataIn(data[0], data[1]);
                final Integer presetFreeMemory = SysexHelper.DataIn(data[2], data[3]);
                robj = new Remotable.PresetMemory() {
                    public String toString() {
                        return "Preset Memory   " + presetMemory + " KB (free:" + presetFreeMemory + " KB)";
                    }
                    /*public String toString() {
                        return new RMF("PRESET_MEMORY", LOCAL_TAG).toString();
                    } */

                    public Integer getPresetMemory() {
                        return presetMemory;
                    }

                    public Integer getPresetFreeMemory() {
                        return presetFreeMemory;
                    }
                };
            }
        }
        return robj;
    }

    static Remotable.SampleMemory filterSampleMemoryReq(ByteStreamable o, byte[] rmtHeader) {
        Remotable.SampleMemory robj = null;
        ByteArrayInputStream is = o.getByteStream();
        byte[] header = new byte[rmtHeader.length];

        if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x13) {
            byte[] data = new byte[4];  // big enough to accomodate both 2 byte fields

            if (is.read(data, 0, 4) == 4) {
                final Integer sampleMemory = SysexHelper.DataIn(data[0], data[1]);
                final Integer sampleFreeMemory = SysexHelper.DataIn(data[2], data[3]);
                robj = new Remotable.SampleMemory() {
                    public String toString() {
                        return "Sample Memory   " + sampleMemory + " MB (free:" + IntPool.get(sampleFreeMemory.intValue() * 10) + " KB)";
                    }
                    /*public String toString() {
                        return new RMF("SAMPLE_MEMORY", LOCAL_TAG).toString();
                    } */

                    public Integer getSampleMemory() {
                        return sampleMemory;
                    }

                    public Integer getSampleFreeMemory() {
                        return sampleFreeMemory;
                    }
                };
            }
        }
        return robj;
    }

    static String filterPresetNameReq(ByteStreamable o, byte[] rmtHeader) {
        String rstr = null;
        ByteArrayInputStream is = o.getByteStream();
        byte[] header = new byte[rmtHeader.length];

        if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x05) {
            byte[] data = new byte[16];  // big enough to accomodate name

            if (is.read(data, 0, 2) == 2) {
                if (/*preset == (int)SysexHelper.DataIn(data[0], data[1]) &&*/ is.read(data, 0, 16) == 16)
                    return new String(data).trim();
            }
        }
        return rstr;
    }

    static String filterSampleNameReq(ByteStreamable o, byte[] rmtHeader) {
        String rstr = null;
        ByteArrayInputStream is = o.getByteStream();
        byte[] header = new byte[rmtHeader.length];

        if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x09) {
            byte[] data = new byte[16];  // big enough to accomodate name

            if (is.read(data, 0, 2) == 2) {
                if (/*sample.equals(SysexHelper.DataIn(data[0], data[1])) &&*/ is.read(data, 0, 16) == 16)
                    return new String(data);
            }
        }
        return rstr;
    }

    static MultiModeMap filterMultiModeMapReq(ByteStreamable o, byte[] rmtHeader) {
        ByteArrayInputStream is = o.getByteStream();
        byte[] header = new byte[rmtHeader.length];
        if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x72) {
            byte[] first16 = new byte[128];  // to accomodate first 16 midi channels
            byte[] all32 = new byte[256];  // to accomodate 32 midi channels available
            if (is.read(first16, 0, 128) == 128) {
                if (is.read(all32, 128, 128) == 128) {
                    System.arraycopy(first16, 0, all32, 0, 128);
                    return new Impl_MultiModeMap(all32);
                } else
                    return new Impl_MultiModeMap(first16);
            }
        }
        return null;
    }

    static Integer filterPresetVoicesReq(ByteStreamable o, byte[] rmtHeader) {
        Integer rint = null;
        ByteArrayInputStream is = o.getByteStream();
        byte[] data = new byte[rmtHeader.length];

        if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x17) {
            if (is.read(data, 0, 2) == 2)
                return SysexHelper.DataIn(data[0], data[1]);
        }
        return rint;
    }

    static Integer filterPresetLinksReq(ByteStreamable o, byte[] rmtHeader) {
        Integer rint = null;
        ByteArrayInputStream is = o.getByteStream();
        byte[] data = new byte[rmtHeader.length];

        if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x19) {
            if (is.read(data, 0, 2) == 2)
                return SysexHelper.DataIn(data[0], data[1]);
        }
        return rint;
    }

    static Integer filterVoiceZonesReq(ByteStreamable o, byte[] rmtHeader) {
        Integer rint = null;
        ByteArrayInputStream is = o.getByteStream();
        byte[] data = new byte[rmtHeader.length];

        if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x1d) {
            if (is.read(data, 0, 2) == 2)
                return SysexHelper.DataIn(data[0], data[1]);
        }
        return rint;
    }

    static Integer filterPresetZonesReq(ByteStreamable o, byte[] rmtHeader) {
        Integer rint = null;
        ByteArrayInputStream is = o.getByteStream();
        byte[] data = new byte[rmtHeader.length];

        if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x1b) {
            if (is.read(data, 0, 2) == 2)
                return SysexHelper.DataIn(data[0], data[1]);
        }
        return rint;
    }

    static Remotable.DeviceConfig filterConfigReq(ByteStreamable o, byte[] rmtHeader) {
        ByteArrayInputStream is = o.getByteStream();
        byte[] data = new byte[rmtHeader.length];
        if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x15) {
            final byte[] info = new byte[3];
            if (is.read(info, 0, 3) == 3)
                return new Remotable.DeviceConfig() {
                    public String toString() {
                        return new RawDevice.RMF("CONFIG_REPLY", RawDevice.LOCAL_TAG).toString();
                    }

                    public Integer getVoices() {
                        if ((info[0] & 1) != 0) return IntPool.get(128); else return IntPool.get(64);
                    }

                    public boolean hasFX() {
                        if ((info[0] & 2) != 0) return true; else return false;
                    }

                    public boolean hasMidi() {
                        if ((info[0] & 4) != 0) return true; else return false;
                    }

                    public boolean hasOctopus() {
                        if ((info[0] & 8) != 0) return true; else return false;
                    }

                    public boolean hasDigitalIO() {
                        if ((info[0] & 16) != 0) return true; else return false;
                    }

                    public Integer getSampleRAM() {
                        return SysexHelper.DataIn(info[1], info[2]);
                    }
                };
        }
        return null;
    }

    static Remotable.DeviceExConfig filterExConfigReq(ByteStreamable o, byte[] rmtHeader) {

        ByteArrayInputStream is = o.getByteStream();
        byte[] data = new byte[rmtHeader.length];
        if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x1f) {
            final byte[] info = new byte[10];
            if (is.read(info, 0, 10) == 10)
                return new Remotable.DeviceExConfig() {

                    public Integer getVoices() {
                        if ((info[0] & 1) != 0) return IntPool.get(128); else return IntPool.get(64);
                    }

                    public boolean hasFX() {
                        if ((info[0] & 2) != 0) return true; else return false;
                    }

                    public boolean hasMidi() {
                        if ((info[0] & 4) != 0) return true; else return false;
                    }

                    public boolean hasOctopus() {
                        if ((info[0] & 8) != 0) return true; else return false;
                    }

                    public boolean hasDigitalIO() {
                        if ((info[0] & 16) != 0) return true; else return false;
                    }

                    public boolean hasPresetFlash() {
                        if ((info[0] & 32) != 0) return true; else return false;
                    }

                    public boolean hasADAT() {
                        if ((info[0] & 64) != 0) return true; else return false;
                    }

                    public Integer getSampleRAM() {
                        return SysexHelper.DataIn(info[2], info[3]);
                    }

                    public Integer getSampleROM() {
                        return IntPool.get((int) info[4]);
                    }

                    public Integer getSampleFlash() {
                        return IntPool.get((int) info[5]);
                    }

                    public String toString() {
                        String ls = Zoeos.getLineSeperator();
                        DecimalFormat df3 = new DecimalFormat("##0");
                        //DecimalFormat df2 = new DecimalFormat("#0");
                        return ls
                                + "Voices          " + getVoices() + ls
                                + (hasFX() ? "Legacy FX       Present" : "No Legacy FX    Present") + ls
                                + "Midi Channels   " + (hasMidi() ? IntPool.get(32) : IntPool.get(16)) + ls
                                + (hasDigitalIO() ? "Digital IO      Present" : "No Digital IO   Present") + ls
                                + (hasPresetFlash() ? "Preset Flash    Present" : "No Preset Flash Present") + ls
                                + (hasADAT() ? "ADAT            Present" : "No ADAT         Present") + ls
                                + "Sample RAM      " + df3.format(getSampleRAM()) + " MB" + ls
                                + "Sample ROM      " + df3.format(getSampleROM()) + " MB" + ls
                                + "Sample Flash    " + df3.format(getSampleFlash()) + " MB" + ls;
                    }
                };
            // 4 reserved bytes for future expansion not handled
        }
        return null;
    }

    static Integer[] filterParameterValuesReq(ByteStreamable o, byte[] rmtHeader) {
        ByteArrayInputStream is = o.getByteStream();
        byte[] header = new byte[rmtHeader.length];

        if (is.read(header, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, header) && is.read() == 0x01) {
            int bc = is.read() * 2;
            byte[] data = new byte[bc];
            if (is.read(data, 0, bc) == bc) {
                Integer[] output = new Integer[bc / 2];
                int i = 0;
                for (int n = 0; n < bc; n += 2)
                    output[i++] = SysexHelper.DataIn(data[n], data[n + 1]);

                return output;
            }
        }
        return null;
    }

    static Object filterPresetDumpReq(ByteStreamable o, byte[] rmtHeader) {
        Object robj = null;
        final ByteArrayInputStream is = o.getByteStream();
        byte[] data = new byte[rmtHeader.length];
        final int msgSize = is.available();
        if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data)) {
            int c = is.read();
            switch (c) {
                // NEW DUMP HEADER or NEW DUMP DATA
                case 0x0d:
                    {
                        int subCommand = is.read();
                        // NEW DUMP HEADER
                        if (subCommand == NEW_PRESET_DUMP_HEADER_SUB_CMD) {
                            //System.out.println("NEW DUMP HEADER" + " " + d);
                            final byte[] words = new byte[14];
                            if (is.read(words, 0, 14) == 14) {
                                return new RawDevice.NewDumpHeader() {
                                    public int getPreset() {
                                        return SysexHelper.DataIn(words[0], words[1]).intValue();
                                    }

                                    public int getNumDataBytes() {
                                        return SysexHelper.UnsignedLongDataIn_int(words[2], words[3], words[4], words[5]);
                                    }

                                    public int getNumGlobalParams() {
                                        return SysexHelper.DataIn(words[6], words[7]).intValue();
                                    }

                                    public int getNumLinkParams() {
                                        return SysexHelper.DataIn(words[8], words[9]).intValue();
                                    }

                                    public int getNumVoiceParams() {
                                        return SysexHelper.DataIn(words[10], words[11]).intValue();
                                    }

                                    public int getNumZoneParams() {
                                        return SysexHelper.DataIn(words[12], words[13]).intValue();
                                    }

                                    public byte[] getHeaderBytes() {
                                        return words;
                                    }
                                };
                            }
                        }
                        // NEW DUMP DATA
                        else if (subCommand == NEW_PRESET_DUMP_DATA_SUB_CMD) {
                            final byte[] pc = new byte[2];
                            int nDataBytes = msgSize - 11; // 5 bytes for header, 2 command bytes,2 packet bytes, 1 checksum byte and 1 EOX byte
                            final byte[] dataBytes = new byte[nDataBytes];
                            if (is.read(pc, 0, 2) == 2 && is.read(dataBytes, 0, nDataBytes) == nDataBytes) {
                                //  System.out.println(NEW_DUMP_DATA_REPLY + " " + d + " " + SysexHelper.DataIn(pc).intValue());
                                return new RawDevice.NewDumpData() {
                                    public int getPacketCount() {
                                        return SysexHelper.DataIn(pc).intValue();
                                    }

                                    public byte[] getData() {
                                        return dataBytes;
                                    }

                                    public int getByteCount() {
                                        return dataBytes.length;
                                    }

                                    public int compareTo(Object o) {
                                        if (o instanceof RawDevice.NewDumpData) {
                                            RawDevice.NewDumpData ndd = (RawDevice.NewDumpData) o;
                                            if (ndd.getPacketCount() < getPacketCount())
                                                return 1;
                                            else if (ndd.getPacketCount() > getPacketCount())
                                                return -1;
                                            else
                                                return 0;
                                        }
                                        return 1;
                                    }
                                };
                            }
                        }
                        break;
                    }

                    // NEW ACK
                case ACK_CMD:
                    {
                        final byte[] packet = new byte[2];
                        // System.out.println(NEW_ACK_REPLY);
                        return new RawDevice.NewDumpStatus() {
                            public int getStatus() {
                                return NEW_ACK;
                            }

                            public int getPacketNumber() {
                                is.read(packet, 0, 2);
                                return (int) SysexHelper.DataIn(packet).intValue();
                            }
                        };
                    }
                    // NEW NACK
                case NACK_CMD:
                    {
                        final byte[] packet = new byte[2];
                        // System.out.println(NEW_NACK_REPLY);
                        return new RawDevice.NewDumpStatus() {
                            public int getStatus() {
                                return NEW_NACK;
                            }

                            public int getPacketNumber() {
                                is.read(packet, 0, 2);
                                return SysexHelper.DataIn(packet).intValue();
                            }
                        };
                    }
                    // WAIT
                case WAIT_CMD:
                    // System.out.println(WAIT_REPLY);
                    return new RawDevice.NewDumpStatus() {
                        public int getStatus() {
                            return WAIT;
                        }

                        public int getPacketNumber() {
                            return -1;
                        }
                    };
                    // CANCEL
                case CANCEL_CMD:
                    // System.out.println(CANCEL_REPLY);
                    return new RawDevice.NewDumpStatus() {
                        public int getStatus() {
                            return CANCEL;
                        }

                        public int getPacketNumber() {
                            return -1;
                        }
                    };
                    // EOF
                case EOF_CMD:
                    // System.out.println(EOF_REPLY);
                    return new RawDevice.NewDumpStatus() {
                        public int getStatus() {
                            return EOF;
                        }

                        public int getPacketNumber() {
                            return -1;
                        }
                    };
            }
        }
        return robj;
    }

    static MinMaxDefault filterMMDReq(ByteStreamable o, byte[] rmtHeader) {
        ByteArrayInputStream is = o.getByteStream();
        byte[] data = new byte[rmtHeader.length];
        if (is.read(data, 0, rmtHeader.length) == rmtHeader.length && Arrays.equals(rmtHeader, data) && is.read() == 0x04) {
            final byte[] info = new byte[8];
            if (is.read(info, 0, 8) == 8)
                return new MinMaxDefault() {
                    public int id = getID().intValue();
                    public int min = getMin().intValue();
                    public int max = getMax().intValue();
                    public int def = getDefault().intValue();

                    public boolean equals(Object obj) {
                        if (obj instanceof MinMaxDefault) {
                            MinMaxDefault mmd = (MinMaxDefault) obj;
                            return (mmd.getID().equals(getID()) && mmd.getMin().equals(getMin()) && mmd.getMax().equals(getMax()) && mmd.getDefault().equals(getDefault()));
                        }
                        return false;
                    }

                    public Integer getID() {
                        return SysexHelper.DataIn(info[0], info[1]);
                    }

                    public Integer getMin() {
                        return SysexHelper.DataIn(info[2], info[3]);
                    }

                    public Integer getMax() {
                        return SysexHelper.DataIn(info[4], info[5]);
                    }

                    public Integer getDefault() {
                        return SysexHelper.DataIn(info[6], info[7]);
                    }

                    public String toString() {
                        RawDevice.RMF s = new RawDevice.RMF("PARAMETER_MMD", RawDevice.LOCAL_TAG).addLine(String.valueOf(id));
                        s.addLine("MIN =" + getMin().toString());
                        s.addLine("MAX =" + getMax().toString());
                        s.addLine("DEF =" + getDefault().toString());
                        return s.toString();
                    }
                };
        }
        return null;
    }
}

