package com.pcmsolutions.smdi;

import com.pcmsolutions.aspi.ASPILogic;
import com.pcmsolutions.aspi.ASPIMsg;
import com.pcmsolutions.aspi.SCSI;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.audio.AudioUtilities;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;

import javax.sound.midi.MidiMessage;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Mar-2003
 * Time: 20:20:01
 * To change this template use Options | File Templates.
 */
public class SMDIAgent {
    private static byte MAX_IDS = 8;
    private static int numAdapters = 0;

    private static final String PREF_NODE_COUPLINGS = "couplings";
    private static final String EMPTY_COUPLING = "";

    private static Impl_ScsiDeviceInfo[] deviceInfos = new Impl_ScsiDeviceInfo[0];
    private static String[] deviceCouplings = new String[0];

    private static Vector smdiListeners = new Vector();

    private static boolean smdiUnavailable = false;

    static {
        try {
            assertSMDI();
            refresh();
        } catch (SmdiUnavailableException e) {
            smdiUnavailable = true;
        }
    }

    public synchronized static boolean isSmdiAvailable() {
        try {
            assertSMDI();
        } catch (SmdiUnavailableException e) {
            return false;
        }
        return !smdiUnavailable;
    }

    public static interface SmdiListener {
        public void smdiChanged();
    }

    public static void addSmdiListener(SmdiListener sl) {
        smdiListeners.add(sl);
    }

    public static void removeSmdiListener(SmdiListener sl) {
        smdiListeners.remove(sl);
    }

    private static void fireSmdiChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Vector slc = (Vector) smdiListeners.clone();
                for (Iterator i = slc.iterator(); i.hasNext();)
                    try {
                        ((SmdiListener) i.next()).smdiChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    public synchronized static void refresh() throws SmdiUnavailableException {
        if (smdiUnavailable)
            throw new SmdiUnavailableException();
        try {
            System.out.println("Refreshing SMDI");
            numAdapters = ASPIMsg.numAdapters();
            for (int i = 0; i < numAdapters; i++)
                try {
                    ASPILogic.rescanPort(i);
                } catch (ASPILogic.ASPILogicException e) {
                    e.printStackTrace();
                } catch (ASPILogic.CommandFailedException e) {
                    e.printStackTrace();
                }
            System.out.println("SMDI adapters = " + numAdapters);
            deviceInfos = new Impl_ScsiDeviceInfo[numAdapters * MAX_IDS];
            deviceCouplings = new String[numAdapters * MAX_IDS];
            for (byte ha = 0; ha < numAdapters; ha++) {
                for (byte id = 0; id < MAX_IDS; id++) {
                    System.out.println("Refeshing HAID = " + ha + ", ID = " + id);
                    SMDILogic.DeviceInfo di = null;
                    try {
                        int type = ASPILogic.getDeviceType(ha, id);
                        System.out.println("Device type = " + type);
                        if (type == SCSI.DTYPE_PROC)
                        //if (type >= SCSI.DTYPE_DASD && type <=SCSI.DTYPE_COMM)
                            di = SMDILogic.inquireDevice(ha, id);
                    } catch (ASPILogic.ASPILogicException e) {
                        e.printStackTrace();
                    } catch (ASPILogic.CommandFailedException e) {
                        //  e.printStackTrace();
                    } catch (ASPILogic.ASPINoDeviceException e) {
                        // e.printStackTrace();
                    } catch (SMDILogic.SMDILogicException e) {
                        e.printStackTrace();
                    }
                    if (di == null)
                        deviceInfos[ha * MAX_IDS + id] = new Impl_ScsiDeviceInfo();
                    else
                        deviceInfos[ha * MAX_IDS + id] = new Impl_ScsiDeviceInfo(di);
                    deviceCouplings[ha * MAX_IDS + id] = Preferences.userNodeForPackage(SMDIAgent.class.getClass()).node(PREF_NODE_COUPLINGS).get(makePreferenceKey(ha, id), EMPTY_COUPLING);
                }
            }
            System.out.println("SMDI completed refresh");
            fireSmdiChanged();
            return;
        } catch (ASPIMsg.ASPIUnavailableException e) {
            smdiUnavailable = true;
        } finally {
        }
        // error pathology
        deviceInfos = new Impl_ScsiDeviceInfo[0];
        deviceCouplings = new String[0];
        fireSmdiChanged();
    }

    private static void assertSMDI() throws SmdiUnavailableException {
        if (smdiUnavailable)
            throw new SmdiUnavailableException();
        try {
            ASPIMsg.assertASPI();
        } catch (UnsatisfiedLinkError e) {
            smdiUnavailable = true;
            e.printStackTrace();
            throw new SmdiUnavailableException();
        } catch (ASPIMsg.ASPIUnavailableException e) {
            smdiUnavailable = true;
            e.printStackTrace();
            throw new SmdiUnavailableException();
        } catch (Exception e) {
            smdiUnavailable = true;
            e.printStackTrace();
            throw new SmdiUnavailableException();
        }
    }

    public synchronized static ScsiTarget[] getDevices() throws SmdiUnavailableException {
        assertSMDI();
        ArrayList outDevs = new ArrayList();
        Impl_ScsiDeviceInfo sdi;
        for (int i = 0; i < deviceInfos.length; i++) {
            sdi = deviceInfos[i];
            //if (sdi.getManufacturer() != null && !sdi.getManufacturer().equals(""))
            if (sdi.isSMDI())
                outDevs.add(new Impl_SmdiTarget((byte) (i / MAX_IDS), (byte) (i % MAX_IDS), sdi));
            else
                outDevs.add(new Impl_ScsiTarget((byte) (i / MAX_IDS), (byte) (i % MAX_IDS), sdi));
        }
        return (ScsiTarget[]) outDevs.toArray(new ScsiTarget[outDevs.size()]);
    }

    public static interface ScsiLocation {
        public byte getHA_Id();

        public byte getSCSI_Id();
    }

    public synchronized static ScsiTarget getScsiTarget(ScsiLocation loc) throws NoSuchSCSITargetException {
        return getScsiTarget(loc.getHA_Id(), loc.getSCSI_Id());
    }

    private static void clearAnyCouplingForIdentityMessage(Object identityMessage) {
        for (int i = 0; i < deviceCouplings.length; i++)
            if (deviceCouplings[i].equals(identityMessage.toString()))
                deviceCouplings[i] = EMPTY_COUPLING;
    }

    public synchronized static void setSmdiTargetCoupling(byte HA_ID, byte SCSI_ID, Object identityMessage) throws TargetNotSMDIException, SmdiUnavailableException {
        if (!deviceInfos[HA_ID * MAX_IDS + SCSI_ID].isSMDI())
            throw new TargetNotSMDIException();
        clearAnyCouplingForIdentityMessage(identityMessage);
        Preferences.userNodeForPackage(SMDIAgent.class.getClass()).node(PREF_NODE_COUPLINGS).put(makePreferenceKey(HA_ID, SCSI_ID), identityMessage.toString());
        refresh();
    }

    public synchronized static SmdiTarget getSmdiTargetForIdentityMessage(Object identityMessage) throws DeviceNotCoupledToSmdiException {
        // refresh();
        for (int i = 0; i < deviceCouplings.length; i++)
            if (deviceCouplings[i].equals(identityMessage.toString()))
                if (deviceInfos[i].isSMDI())
                    return new Impl_SmdiTarget((byte) (i / MAX_IDS), (byte) (i % MAX_IDS), deviceInfos[i]);

        throw new DeviceNotCoupledToSmdiException();
    }

    public synchronized static ScsiTarget getScsiTarget(byte HA_ID, byte SCSI_ID) throws NoSuchSCSITargetException {
        // refresh();
        try {
            Impl_ScsiDeviceInfo sdi = deviceInfos[HA_ID * MAX_IDS + SCSI_ID];
            if (sdi.isSMDI())
                return new Impl_SmdiTarget(HA_ID, SCSI_ID, sdi);
            else
                return new Impl_ScsiTarget(HA_ID, SCSI_ID, sdi);
        } catch (Exception e) {
            throw new NoSuchSCSITargetException();
        }
    }

    public static byte getMAX_IDS() {
        return MAX_IDS;
    }

    public static int getNumAdapters() {
        return numAdapters;
    }

    private static boolean isScsiLocationSmdi(ScsiLocation loc) {
        return isScsiLocationSmdi(loc.getHA_Id(), loc.getSCSI_Id());
    }

    private static boolean isScsiLocationSmdi(byte HA_ID, byte SCSI_ID) {
        try {
            return deviceInfos[HA_ID * MAX_IDS + SCSI_ID].isSMDI();
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized static void clearCouplings() throws SmdiUnavailableException {
        try {
            Preferences.userNodeForPackage(SMDIAgent.class.getClass()).node(PREF_NODE_COUPLINGS).clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        refresh();
    }

    public static synchronized CoupledSmdiTarget getCoupledSmdiTargetForIdentityMessageString(String ims) throws SmdiUnavailableException {
        refresh();
        synchronized (SMDIAgent.class) {
            for (int i = 0; i < deviceCouplings.length; i++)
                if (deviceCouplings[i].equals(ims))
                    try {
                        Impl_ScsiTarget st = (Impl_ScsiTarget) getScsiTarget((byte) (i / MAX_IDS), (byte) (i % MAX_IDS));
                        if (st instanceof Impl_SmdiTarget) {
                            return new Impl_CoupledSmdiTarget(ims, (SmdiTarget) st);
                        }
                    } catch (NoSuchSCSITargetException e) {
                    }
        }
        return null;
    }

    private static String makePreferenceKey(byte HA_ID, byte SCSI_ID) {
        return "HA_ID = " + String.valueOf(HA_ID) + " SCSI_ID = " + String.valueOf(SCSI_ID);
    }

    private static String makePreferenceKey(ScsiLocation sl) {
        return makePreferenceKey(sl.getHA_Id(), sl.getSCSI_Id());
    }

    public static void main(String args[]) {
        try {
            System.out.println(Thread.currentThread());
//Impl_ScsiDeviceInfo sdi = new Impl_ScsiDeviceInfo();
//Impl_SmdiSampleHeader sh = new Impl_SmdiSampleHeader();

/*      for (int lun = 0; lun < 2; lun++) {
          for (int id = 0; id < 8; id++) {
              SMDIAgent.smdiGetDeviceInfo((byte) lun, (byte) id, sdi);
              System.out.println("LUN: " + lun + " ID: " + id);
              System.out.println("TYPE: " + sdi.getDeviceType());
              System.out.println("SMDI: " + sdi.isSMDI());
              System.out.println("MANUFACTURER: " + sdi.getManufacturer());
              System.out.println("NAME: " + sdi.getName());
          }
      }
      */

/* for (Iterator i = devices.keySet().iterator(); i.hasNext();) {
     ScsiTarget st = (ScsiTarget) i.next();
     System.out.println("HA: " + st.getHA_Id() + " ID: " + st.getSCSI_Id());
     System.out.println("TYPE: " + st.getDeviceType());
     System.out.println("SMDI: " + st.isSMDI());
     System.out.println("MANUFACTURER: " + st.getDeviceManufacturer());
     System.out.println("NAME: " + st.getDeviceName());
 }*/


//sa.smdiGetDeviceInfo((byte)1,(byte)2,sdi);
//System.out.println(sdi.getDeviceType());
/*System.out.println(SMDIAgent.getFileSampleHeader("e:\\PENEMY2.wav", sh));
System.out.println(SMDIAgent.getSampleHeader((byte) 1, (byte) 2, 1, sh));
System.out.println(SMDIAgent.getVersionStr());
//System.out.println(sa.smdiSendFile((byte) 0, (byte) 3, (short) 1, "e:\\sooth2.wav", "tester", false));
// System.out.println(sa.smdiSendFile((byte) 1, (byte) 2, (short) 1, "e:\\PENEMY.wav", "tester", false));
System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 2, "e:\\PENEMY2.wav", false));
  */
//System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 1, "e:\\sooth2.wav", "tester", false));
//System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 2, "e:\\sooth2.wav", "tester", false));
// System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 3, "e:\\sooth2.wav", "tester", false));
//System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 4, "e:\\sooth2.wav", "tester", false));

//System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 1, "e:\\PENEMY2.wav", "tester", false));
//System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 2, "e:\\PENEMY2.wav", "tester", false));


/* System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 1, new String("e:\\PENEMY2.wav"), new String("tester"), false));
 System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 2, new String("e:\\PENEMY2.wav"), new String("tester"), false));
 System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 3, new String("e:\\PENEMY2.wav"), new String("tester"), false));
 System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 4, new String("e:\\PENEMY2.wav"), new String("tester"), false));
 */
// System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 5, new String("e:\\PENEMY2.wav"), new String("tester"), false));
// System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 6, new String("e:\\PENEMY2.wav"), new String("tester"), false));
// System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 7, new String("e:\\PENEMY2.wav"), new String("tester"), false));
// System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 8, new String("e:\\PENEMY2.wav"), new String("tester"), false));



/* System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY3.wav", false));
 System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 2, "e:\\PENEMY4.wav", false));
 System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 3, "e:\\PENEMY5.wav", false));
 System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 4, "e:\\PENEMY6.wav", false));

 System.out.println("farting");
 */
//System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 2, "e:\\PENEMY4.wav", false));
//System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 3, "e:\\PENEMY5.wav", false));

//SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 2, "e:\\PENEMY2.wav", "tester", false);
// System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 3, "e:\\sooth2.wav", "tester", false));
//System.out.println(SMDIAgent.smdiSendFile((byte) 1, (byte) 2, (short) 4, "e:\\sooth2.wav", "tester", false));

//System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY2.wav", false));
/*  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY3.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY4.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY5.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY6.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY7.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY8.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY9.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY0.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 1, "e:\\PENEMY10.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 2, "e:\\PENEMY11.wav", false));
  System.out.println(SMDIAgent.smdiRecvFile((byte) 1, (byte) 2, 2, "e:\\PENEMY12.wav", false));
  */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Impl_ScsiTarget implements ScsiTarget {
        protected byte HA_ID;
        protected byte SCSI_ID;
        protected String deviceName;
        protected String deviceManufacturer;
        protected int deviceType;
        protected boolean isDevice;

        public Impl_ScsiTarget(byte HA_ID, byte SCSI_ID, Impl_ScsiDeviceInfo info) {
            this.HA_ID = HA_ID;
            this.SCSI_ID = SCSI_ID;
            deviceManufacturer = info.getManufacturer();
            deviceName = info.getName();
            deviceType = info.getDeviceType();
            isDevice = info.isDevice();
        }

        public String toString() {
            return getDeviceName();
        }

        public int compareTo(Object o) {
            if (!(o instanceof ScsiTarget))
                return 0;
            ScsiTarget st = (ScsiTarget) o;

            int res = IntPool.get(getHA_Id()).compareTo(IntPool.get(st.getHA_Id()));

            if (res == 0)
                res = IntPool.get(getSCSI_Id()).compareTo(IntPool.get(st.getSCSI_Id()));

            return res;
        }

        public byte getHA_Id() {
            return HA_ID;
        }

        public byte getSCSI_Id() {
            return SCSI_ID;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getDeviceManufacturer() {
            return deviceManufacturer;
        }

        public boolean isDevice() {
            return isDevice;
        }

        public int getDeviceType() {
            return deviceType;
        }

        public boolean isSMDI() {
            synchronized (SMDIAgent.class) {
                try {
                    return deviceInfos[HA_ID * MAX_IDS + SCSI_ID].isSMDI();
                } catch (Exception e) {
                    return false;
                }
            }
        }
    }

    public interface SampleInputStreamHandler{
        void handleStream(AudioInputStream ais) throws Exception;
    }

    private static class Impl_SmdiTarget extends Impl_ScsiTarget implements SmdiTarget {
        public Impl_SmdiTarget(byte HA_ID, byte SCSI_ID, Impl_ScsiDeviceInfo info) {
            super(HA_ID, SCSI_ID, info);
            if (deviceType != 3)
                throw new IllegalArgumentException("SCSI device not a SMDI target!");
        }

        private void assertSMDI() throws TargetNotSMDIException {
            if (!isSMDI())
                throw new TargetNotSMDIException();
        }

        public void sendSync(final AudioInputStream ais, final int sampleNum, final String sampleName, final int packetSize, final ProgressCallback prog) throws SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, UnsupportedAudioFileException, IOException, SmdiUnsupportedConversionException, SmdiSampleEmptyException, SmdiTransferAbortedException {
            if (sampleNum == 0)
                throw new SmdiOutOfRangeException("sample 0");
            synchronized (SMDIAgent.class) {
                assertSMDI();
                SMDISendInstance si = new SMDISendInstance() {
                    final private ProgressCallback pc;

                    {
                        pc = (prog == null ? ProgressCallback.DUMMY : prog);

                    }

                    public AudioInputStream getAudioInputStream() {
                        return ais;
                    }

                    public String getSampleName() {
                        return sampleName;
                    }

                    public int getPacketSizeInBytes() {
                        return packetSize * 1024;
                    }

                    public int getHAID() {
                        return HA_ID;
                    }

                    public int getID() {
                        return SCSI_ID;
                    }

                    public int getSample() {
                        return sampleNum;
                    }

                    public ProgressCallback getProgressCallback() {
                        return pc;
                    }
                };
                try {
                    SMDILogic.sendSample(si);
                } catch (SMDILogic.SMDILogicException e) {
                    throw new SmdiGeneralException(e.getMessage());
                }
            }
        }

        public void recvSync(SampleInputStreamHandler isf, final AudioFileFormat.Type fileType, final int sampleNum, final int packetSize, final ProgressCallback prog) throws SmdiOutOfRangeException, TargetNotSMDIException, SmdiGeneralException, SmdiNoMemoryException, SmdiSampleEmptyException {
            if (sampleNum == 0)
                throw new SmdiOutOfRangeException("Illegal sample index 0");
            synchronized (SMDIAgent.class) {
                assertSMDI();
                SMDIRecvInstance ri = new SMDIRecvInstance() {
                    final private ProgressCallback pc;

                    {
                        pc = (prog == null ? ProgressCallback.DUMMY : prog);
                    }

                    public int getPacketSizeInBytes() {
                        return packetSize * 1024;
                    }

                    public int getHAID() {
                        return HA_ID;
                    }

                    public int getID() {
                        return SCSI_ID;
                    }

                    public int getSample() {
                        return sampleNum;
                    }

                    public ProgressCallback getProgressCallback() {
                        return pc;
                    }

                    public OutputStream getOutputStream() {
                        return null;
                    }

                    public AudioFileFormat.Type getFileType() {
                        return fileType;
                    }

                    public Map<String, Object> getPropertiesForSampleHeader(SmdiSampleHeader hdr) {
                        AbstractAudioChunk[] chunks = AudioUtilities.getSMDIHeaderChunks(hdr, fileType);
                        return AudioUtilities.getChunkPropertiesMap(chunks);
                    }
                };
                try {
                   isf.handleStream(SMDILogic.recvSampleAsync(ri));
                } catch (SMDILogic.SMDILogicException e) {
                    throw new SmdiGeneralException(e.getMessage());
                }catch(Exception e){
                    throw new SmdiGeneralException(e.getMessage());
                }
            }
        }

        public byte[] sendMidiMessage(MidiMessage m) throws TargetNotSMDIException, SmdiGeneralException {
            synchronized (SMDIAgent.class) {
                assertSMDI();
                try {
                    return SMDILogic.sendMidi(HA_ID, SCSI_ID, m.getMessage());
                } catch (SMDILogic.SMDILogicException e) {
                    throw new SmdiGeneralException(e.getMessage());
                } catch (SmdiOutOfRangeException e) {
                    throw new SmdiGeneralException(e.getMessage());
                } catch (SmdiNoMemoryException e) {
                    throw new SmdiGeneralException(e.getMessage());
                } catch (SmdiSampleEmptyException e) {
                    throw new SmdiGeneralException(e.getMessage());
                }
            }
        }

        public SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiSampleEmptyException, SmdiNoMemoryException {
            synchronized (SMDIAgent.class) {
                assertSMDI();
                try {
                    return SMDILogic.getSampleHeader(HA_ID, SCSI_ID, sampleNum);
                } catch (SMDILogic.SMDILogicException e) {
                    throw new SmdiGeneralException(e.getMessage());
                }
            }
        }

        public boolean isCoupled() {
            synchronized (SMDIAgent.class) {
                try {
                    return (!deviceCouplings[HA_ID * MAX_IDS + SCSI_ID].equals(EMPTY_COUPLING));
                } catch (Exception e) {
                    return false;
                }
            }
        }

        public String getCouplingString() throws SmdiTargetNotCoupledException {
            synchronized (SMDIAgent.class) {
                if (!isCoupled())
                    throw new SmdiTargetNotCoupledException();
                return deviceCouplings[HA_ID * MAX_IDS + SCSI_ID];
            }
        }

        public void setCouplingString(String str) throws TargetNotSMDIException, SmdiUnavailableException {
            setSmdiTargetCoupling(HA_ID, SCSI_ID, str);
        }
    }

    private static class Impl_CoupledSmdiTarget implements CoupledSmdiTarget {
        private SmdiTarget st;
        private String coupling;

        public Impl_CoupledSmdiTarget(String coupling, SmdiTarget st) {
            this.coupling = coupling;
            this.st = st;
        }

        private void assertCoupling() throws SmdiTargetCouplingInvalidException {
            try {
                if (coupling.equals(deviceCouplings[st.getHA_Id() * MAX_IDS + st.getSCSI_Id()]))
                    return;
            } catch (Exception e) {
            }
            throw new SmdiTargetCouplingInvalidException();

        }

        public void sendSync(AudioInputStream ais, int sampleNum, String sampleName, int packetSize, ProgressCallback prog) throws SmdiTargetCouplingInvalidException, SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiUnsupportedConversionException, SMDILogic.SMDILogicException, SmdiSampleEmptyException, UnsupportedAudioFileException, IOException, SmdiTransferAbortedException {
            synchronized (SMDIAgent.class) {
                assertCoupling();
                st.sendSync(ais, sampleNum, sampleName, packetSize, prog);
            }
        }

        public void recvAsync(SampleInputStreamHandler ish, AudioFileFormat.Type fileType, int sampleNum, int packetSize, ProgressCallback prog) throws SmdiTargetCouplingInvalidException, SmdiFileOpenException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiSampleEmptyException, SmdiNoMemoryException, SmdiTransferAbortedException, IOException {
            synchronized (SMDIAgent.class) {
                assertCoupling();
                st.recvSync(ish, fileType, sampleNum, packetSize, prog);
            }
        }

        public byte[] sendMidiMessage(MidiMessage m) throws SmdiGeneralException, TargetNotSMDIException {
            return st.sendMidiMessage(m);
        }

        public SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiTargetCouplingInvalidException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException, SmdiSampleEmptyException, SmdiNoMemoryException {
            synchronized (SMDIAgent.class) {
                assertCoupling();
                return st.getSampleHeader(sampleNum);
            }
        }

        public String getCouplingTag() throws SmdiTargetCouplingInvalidException {
            return coupling;
        }

        public byte getHA_Id() {
            return st.getHA_Id();
        }

        public byte getSCSI_Id() {
            return st.getSCSI_Id();
        }

        public String getDeviceName() {
            return st.getDeviceName();
        }

        public boolean isDevice() {
            return st.isDevice();
        }

        public String getDeviceManufacturer() {
            return st.getDeviceManufacturer();
        }

        public int getDeviceType() {
            return st.getDeviceType();
        }

        public boolean isSMDI() {
            return st.isSMDI();
        }

        public int compareTo(Object o) {
            return st.compareTo(o);
        }
    }
}