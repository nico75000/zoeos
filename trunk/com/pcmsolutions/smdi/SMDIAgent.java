package com.pcmsolutions.smdi;

import com.pcmsolutions.system.IntPool;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Mar-2003
 * Time: 20:20:01
 * To change this template use Options | File Templates.
 */
public class SMDIAgent {
    // SMDI message IDs
    private static final int SMDIM_ENDOFPROCEDURE = 0x01040000;
    private static final int SMDIM_ERROR = 0x00000000;
    private static final int SMDIM_SAMPLEHEADER = 0x01210000;

    // SMDI errors
    private static final int SMDIE_OUTOFRANGE = 0x00200000;
    private static final int SMDIE_NOSAMPLE = 0x00200002;
    private static final int SMDIE_NOMEMORY = 0x00200004;
    private static final int SMDIE_UNSUPPSAMBITS = 0x00200006;

    // File errors
    private static final int FE_OPENERROR = 0x00010001;      // Couldn't open the File
    private static final int FE_UNKNOWNFORMAT = 0x00010002; // Unsupported File format

    // File types
    private static final int FT_WAV = 0x00000001;   // RIFF WAVE

    private static byte MAX_ID = 16;
    private static int numAdapters = 0;
    private static String versionStr;
    private static int version;

    private static final String PREF_NODE_COUPLINGS = "couplings";
    private static final String EMPTY_COUPLING = "";

    private static Impl_ScsiDeviceInfo[] deviceInfos;
    private static String[] deviceCouplings;

    private static Vector smdiListeners = new Vector();

    private static native int smdiGetVersion();

    private static native byte smdiInit();

    private static native void smdiGetDeviceInfo(byte ha_id, byte scsi_id, Impl_ScsiDeviceInfo sdi);

    private static native int smdiSendFile(byte HA_ID, byte SCSI_ID, int sampleNum, String fileName, String sampleName, boolean async);

    private static native int smdiRecvFile(byte HA_ID, byte SCSI_ID, int sampleNum, String fileName, boolean async);

    private static native int getSampleHeader(byte HA_ID, byte SCSI_ID, int sampleNum, Impl_SmdiSampleHeader hdr);

    private static native int getFileSampleHeader(String fileName, Impl_SmdiSampleHeader hdr);

    private static native void masterIdentify(byte ha_id, byte scsi_id);

    public static final String WAV_EXTENSION = "wav";

    private static volatile boolean smdiDllLoaded = false;

    private static PreferenceChangeListener pcl = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            SMDIAgent.refresh();
        }
    };

    static {
        Preferences.userNodeForPackage(SMDIAgent.class).node(PREF_NODE_COUPLINGS).addPreferenceChangeListener(pcl);
    }

    static {
        try {
            assertSMDI();
        } catch (SmdiUnavailableException e) {
        }
    }

    public static synchronized boolean isSmdiAvailable() {
        try {
            assertSMDI();
        } catch (SmdiUnavailableException e) {
            return false;
        }
        return true;
    }

    public static interface SmdiListener {
        public void SmdiChanged();
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
                        ((SmdiListener) i.next()).SmdiChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    public synchronized static void refresh() {
        if (smdiDllLoaded) {
            numAdapters = smdiInit();
            version = smdiGetVersion();
            DecimalFormat df = new DecimalFormat("###.##");
            versionStr = (df.format((double) version / 100.0));

            deviceInfos = new Impl_ScsiDeviceInfo[numAdapters * MAX_ID];
            deviceCouplings = new String[numAdapters * MAX_ID];
            for (byte ha = 0; ha < numAdapters; ha++) {
                for (byte id = 0; id < MAX_ID; id++) {
                    deviceInfos[ha * MAX_ID + id] = new Impl_ScsiDeviceInfo();
                    SMDIAgent.smdiGetDeviceInfo(ha, id, deviceInfos[ha * MAX_ID + id]);
                    deviceCouplings[ha * MAX_ID + id] = Preferences.userNodeForPackage(SMDIAgent.class).node(PREF_NODE_COUPLINGS).get(makePreferenceKey(ha, id), EMPTY_COUPLING);
                }
            }
            fireSmdiChanged();
        }
    }

    private static void assertSMDI() throws SmdiUnavailableException {
        if (!smdiDllLoaded) {
            try {
                System.loadLibrary("pcmsmdi");
                smdiDllLoaded = true;
                refresh();
            } catch (UnsatisfiedLinkError e) {
                smdiDllLoaded = false;
                throw new SmdiUnavailableException();
            }
        }
    }

    public synchronized static ScsiTarget[] getDevices() throws SmdiUnavailableException {
        assertSMDI();
        ArrayList outDevs = new ArrayList();
        Impl_ScsiDeviceInfo sdi;
        for (int i = 0; i < deviceInfos.length; i++) {
            sdi = deviceInfos[i];
            if (sdi.getManufacturer() != null && !sdi.getManufacturer().trim().equals(""))
                if (sdi.isSMDI())
                    outDevs.add(new Impl_SmdiTarget((byte) (i / MAX_ID), (byte) (i % MAX_ID), sdi));
                else
                    outDevs.add(new Impl_ScsiTarget((byte) (i / MAX_ID), (byte) (i % MAX_ID), sdi));
        }
        return (ScsiTarget[]) outDevs.toArray(new ScsiTarget[outDevs.size()]);
    }

    public static interface ScsiLocation {
        public byte getHA_Id();

        public byte getSCSI_Id();
    }

    public synchronized static ScsiTarget getScsiTarget(ScsiLocation loc) throws NoSuchSCSITargetException, SmdiUnavailableException {
        return getScsiTarget(loc.getHA_Id(), loc.getSCSI_Id());
    }

    private static void clearAnyCouplingForIdentityMessage(Object identityMessage) {
        for (int i = 0; i < deviceCouplings.length; i++)
            if (deviceCouplings[i].equals(identityMessage.toString()))
                deviceCouplings[i] = EMPTY_COUPLING;
    }

    public synchronized static void setSmdiTargetCoupling(byte HA_ID, byte SCSI_ID, Object identityMessage) throws TargetNotSMDIException, SmdiUnavailableException {
        assertSMDI();
        if (!deviceInfos[HA_ID * MAX_ID + SCSI_ID].isSMDI())
            throw new TargetNotSMDIException();
        clearAnyCouplingForIdentityMessage(identityMessage);
        Preferences.userNodeForPackage(SMDIAgent.class).node(PREF_NODE_COUPLINGS).put(makePreferenceKey(HA_ID, SCSI_ID), identityMessage.toString());
    }

    public static synchronized SmdiTarget getSmdiTargetForIdentityMessage(Object identityMessage) throws DeviceNotCoupledToSmdiException, SmdiUnavailableException {
        assertSMDI();
        for (int i = 0; i < deviceCouplings.length; i++)
            if (deviceCouplings[i].equals(identityMessage.toString()))
                if (deviceInfos[i].isSMDI())
                    return new Impl_SmdiTarget((byte) (i / MAX_ID), (byte) (i % MAX_ID), deviceInfos[i]);

        throw new DeviceNotCoupledToSmdiException();
    }

    public synchronized static ScsiTarget getScsiTarget(byte HA_ID, byte SCSI_ID) throws NoSuchSCSITargetException, SmdiUnavailableException {
        assertSMDI();
        try {
            Impl_ScsiDeviceInfo sdi = deviceInfos[HA_ID * MAX_ID + SCSI_ID];
            if (sdi.isSMDI())
                return new Impl_SmdiTarget(HA_ID, SCSI_ID, sdi);
            else
                return new Impl_ScsiTarget(HA_ID, SCSI_ID, sdi);
        } catch (Exception e) {
            throw new NoSuchSCSITargetException();
        }

    }

    public static byte getMAX_ID() {
        return MAX_ID;
    }

    public static int getNumAdapters() {
        return numAdapters;
    }

    private static boolean isScsiLocationSmdi(ScsiLocation loc) {
        return isScsiLocationSmdi(loc.getHA_Id(), loc.getSCSI_Id());
    }

    private static boolean isScsiLocationSmdi(byte HA_ID, byte SCSI_ID) {
        try {
            return deviceInfos[HA_ID * MAX_ID + SCSI_ID].isSMDI();
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized static void clearCouplings() throws SmdiUnavailableException {
        assertSMDI();
        try {
            Preferences.userNodeForPackage(SMDIAgent.class).node(PREF_NODE_COUPLINGS).clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /*public synchronized static String getAssociationTagForSmdiTarget(ScsiLocation sl) throws SmdiTargetNotCoupledException {
        return getAssociationTagForSmdiTarget(sl.getHA_Id(), sl.getSCSI_Id());
    }

    public synchronized static String getAssociationTagForSmdiTarget(byte HA_ID, byte SCSI_ID) throws SmdiTargetNotCoupledException {
        try {
            if (!(deviceAssociations[HA_ID * MAX_ID + SCSI_ID].equals("")))
                return deviceAssociations[HA_ID * MAX_ID + SCSI_ID];
        } catch (Exception e) {
        }
        throw new SmdiTargetNotCoupledException();
    }
      */

    public static CoupledSmdiTarget getCoupledSmdiTargetForIdentityMessageString(String ims) throws SmdiUnavailableException {
        assertSMDI();
        synchronized (SMDIAgent.class) {
            for (int i = 0; i < deviceCouplings.length; i++)
                if (deviceCouplings[i].equals(ims))
                    try {
                        ScsiTarget st = getScsiTarget((byte) (i / MAX_ID), (byte) (i % MAX_ID));
                        if (st instanceof SmdiTarget)
                            return new Impl_CoupledSmdiTarget(ims, (SmdiTarget) st);
                    } catch (NoSuchSCSITargetException e) {
                    }
        }
        return null;
    }

    public static SmdiSampleHeader getSampleHeader(String fileName) throws SmdiFileOpenException, SmdiUnknownFileFormatException, SmdiGeneralException, SmdiUnavailableException {
        assertSMDI();
        synchronized (SMDIAgent.class) {
            Impl_SmdiSampleHeader hdr = new Impl_SmdiSampleHeader();
            int retval = SMDIM_ERROR;
            try {
                retval = SMDIAgent.getFileSampleHeader(fileName, hdr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (retval) {
                case FT_WAV:
                    return hdr;
                case FE_OPENERROR:
                    throw new SmdiFileOpenException();
                case FE_UNKNOWNFORMAT:
                    throw new SmdiUnknownFileFormatException();
                default:
                    throw new SmdiGeneralException();
            }
        }
    }

    private static String makePreferenceKey(byte HA_ID, byte SCSI_ID) {
        return "HA_ID = " + String.valueOf(HA_ID) + " SCSI_ID = " + String.valueOf(SCSI_ID);
    }

    private static String makePreferenceKey(ScsiLocation sl) {
        return makePreferenceKey(sl.getHA_Id(), sl.getSCSI_Id());
    }

    public static String getVersionStr() {
        return versionStr;
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

        public Impl_ScsiTarget(byte HA_ID, byte SCSI_ID, Impl_ScsiDeviceInfo info) {
            this.HA_ID = HA_ID;
            this.SCSI_ID = SCSI_ID;
            deviceManufacturer = info.getManufacturer();
            deviceName = info.getName();
            deviceType = info.getDeviceType();
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

        public int getDeviceType() {
            return deviceType;
        }

        public boolean isSMDI() {
            synchronized (SMDIAgent.class) {
                try {
                    return deviceInfos[HA_ID * MAX_ID + SCSI_ID].isSMDI();
                } catch (Exception e) {
                    return false;
                }
            }
        }
    }

    private static class Impl_SmdiTarget extends Impl_ScsiTarget implements SmdiTarget {
        public Impl_SmdiTarget(byte HA_ID, byte SCSI_ID, Impl_ScsiDeviceInfo info) {
            super(HA_ID, SCSI_ID, info);
            if (deviceType != 3)
                throw new IllegalArgumentException("SCSI device not a SMDI target!");
        }

        private void assert() throws TargetNotSMDIException {
            if (!isSMDI())
                throw new TargetNotSMDIException();
        }

        public void sendSync(String fileName, int sampleNum, String sampleName) throws SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException {
            if (sampleNum == 0)
                throw new SmdiOutOfRangeException("sample 0");
            synchronized (SMDIAgent.class) {
                assert();

                int retval = SMDIM_ERROR;

                try {
                    retval = smdiSendFile(HA_ID, SCSI_ID, sampleNum, fileName, sampleName, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                switch (retval) {
                    case SMDIM_ENDOFPROCEDURE:
                        break;
                    case SMDIE_OUTOFRANGE:
                        throw new SmdiOutOfRangeException();
                    case SMDIE_NOMEMORY:
                        throw new SmdiNoMemoryException();
                    case SMDIE_UNSUPPSAMBITS:
                        throw new SmdiUnsupportedSampleBitsException();
                    case FE_OPENERROR:
                        throw new SmdiFileOpenException();
                    case FE_UNKNOWNFORMAT:
                        throw new SmdiUnknownFileFormatException();
                    default:
                        refresh();
                        throw new SmdiGeneralException();
                }
            }
        }

        public void sendAsync(String fileName, int sampleNum, String sampleName) throws SmdiGeneralException, TargetNotSMDIException, SmdiOutOfRangeException {
            if (sampleNum == 0)
                throw new SmdiOutOfRangeException("sample 0");
            synchronized (SMDIAgent.class) {
                assert();
                int retval = SMDIM_ERROR;
                try {
                    retval = smdiSendFile(HA_ID, SCSI_ID, sampleNum, fileName, sampleName, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (retval != -1) { //0xffffffff
                    refresh();
                    throw new SmdiGeneralException();
                }
            }
        }

        public void recvSync(String fileName, int sampleNum) throws SmdiFileOpenException, SmdiNoSampleException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException {
            if (sampleNum == 0)
                throw new SmdiOutOfRangeException("Illegal sample index 0");
            synchronized (SMDIAgent.class) {
                assert();
                int retval = SMDIM_ERROR;
                try {
                    retval = smdiRecvFile(HA_ID, SCSI_ID, sampleNum, fileName, false);
                    //SMDIAgent.masterIdentify(HA_ID, SCSI_ID);
                } catch (Exception e) {
                    throw new SmdiGeneralException(e.getMessage());
                }
                switch (retval) {
                    case SMDIM_ENDOFPROCEDURE:
                        break;
                    case SMDIE_OUTOFRANGE:
                        throw new SmdiOutOfRangeException();
                    case SMDIE_NOSAMPLE:
                        throw new SmdiNoSampleException();
                    case FE_OPENERROR:
                        throw new SmdiFileOpenException();
                    case FE_UNKNOWNFORMAT:
                    default:
                        refresh();
                        throw new SmdiGeneralException();
                }
            }
        }

        public void recvAsync(String fileName, int sampleNum) throws SmdiGeneralException, TargetNotSMDIException, SmdiOutOfRangeException {
            if (sampleNum == 0)
                throw new SmdiOutOfRangeException("sample 0");
            synchronized (SMDIAgent.class) {
                assert();
                int retval = SMDIM_ERROR;
                try {
                    retval = smdiRecvFile(HA_ID, SCSI_ID, sampleNum, fileName, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (retval != -1) {            //0xffffffff
                    refresh();
                    throw new SmdiGeneralException();
                }
            }
        }

        public SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiOutOfRangeException, SmdiNoSampleException, SmdiGeneralException, TargetNotSMDIException {
            synchronized (SMDIAgent.class) {
                assert();
                Impl_SmdiSampleHeader hdr = new Impl_SmdiSampleHeader();

                int retval = SMDIM_ERROR;

                try {
                    retval = SMDIAgent.getSampleHeader(HA_ID, SCSI_ID, sampleNum, hdr);
                    SMDIAgent.masterIdentify(HA_ID, SCSI_ID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                switch (retval) {
                    case SMDIM_SAMPLEHEADER:
                        return hdr;
                    case SMDIE_OUTOFRANGE:
                        throw new SmdiOutOfRangeException();
                    case SMDIE_NOSAMPLE:
                        throw new SmdiNoSampleException();
                    default:
                        refresh();
                        throw new SmdiGeneralException();
                }
            }
        }

        public SmdiSampleHeader getSampleHeader(String fileName) throws SmdiFileOpenException, SmdiUnknownFileFormatException, SmdiGeneralException, TargetNotSMDIException {
            synchronized (SMDIAgent.class) {
                assert();
                Impl_SmdiSampleHeader hdr = new Impl_SmdiSampleHeader();
                int retval = SMDIM_ERROR;
                try {
                    retval = SMDIAgent.getFileSampleHeader(fileName, hdr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                switch (retval) {
                    case FT_WAV:
                        return hdr;
                    case FE_OPENERROR:
                        throw new SmdiFileOpenException();
                    case FE_UNKNOWNFORMAT:
                        throw new SmdiUnknownFileFormatException();
                    default:
                        refresh();
                        throw new SmdiGeneralException();
                }
            }
        }

        public boolean isCoupled() {
            synchronized (SMDIAgent.class) {
                try {
                    return (!deviceCouplings[HA_ID * MAX_ID + SCSI_ID].equals(EMPTY_COUPLING));
                } catch (Exception e) {
                    return false;
                }
            }
        }

        public String getCouplingString() throws SmdiTargetNotCoupledException {
            synchronized (SMDIAgent.class) {
                if (!isCoupled())
                    throw new SmdiTargetNotCoupledException();
                return deviceCouplings[HA_ID * MAX_ID + SCSI_ID];
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

        private void assert() throws SmdiTargetCouplingInvalidException {
            try {
                if (coupling.equals(deviceCouplings[st.getHA_Id() * MAX_ID + st.getSCSI_Id()]))
                    return;
            } catch (Exception e) {
            }
            throw new SmdiTargetCouplingInvalidException();

        }

        public void sendSync(String fileName, int sampleNum, String sampleName) throws SmdiTargetCouplingInvalidException, SmdiUnknownFileFormatException, SmdiFileOpenException, SmdiUnsupportedSampleBitsException, SmdiNoMemoryException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException {
            synchronized (SMDIAgent.class) {
                assert();
                st.sendSync(fileName, sampleNum, sampleName);
            }
        }

        public void sendAsync(String fileName, int sampleNum, String sampleName) throws SmdiTargetCouplingInvalidException, SmdiGeneralException, TargetNotSMDIException, SmdiOutOfRangeException {
            synchronized (SMDIAgent.class) {
                assert();
                st.sendAsync(fileName, sampleNum, sampleName);
            }
        }

        public void recvSync(String fileName, int sampleNum) throws SmdiTargetCouplingInvalidException, SmdiFileOpenException, SmdiNoSampleException, SmdiOutOfRangeException, SmdiGeneralException, TargetNotSMDIException {
            synchronized (SMDIAgent.class) {
                assert();
                st.recvSync(fileName, sampleNum);
            }
        }

        public void recvAsync(String fileName, int sampleNum) throws SmdiTargetCouplingInvalidException, SmdiGeneralException, TargetNotSMDIException, SmdiOutOfRangeException {
            synchronized (SMDIAgent.class) {
                assert();
                st.recvAsync(fileName, sampleNum);
            }
        }

        public SmdiSampleHeader getSampleHeader(int sampleNum) throws SmdiTargetCouplingInvalidException, SmdiOutOfRangeException, SmdiNoSampleException, SmdiGeneralException, TargetNotSMDIException {
            synchronized (SMDIAgent.class) {
                assert();
                return st.getSampleHeader(sampleNum);
            }
        }

        public SmdiSampleHeader getSampleHeader(String fileName) throws SmdiTargetCouplingInvalidException, SmdiFileOpenException, SmdiUnknownFileFormatException, SmdiGeneralException, TargetNotSMDIException {
            synchronized (SMDIAgent.class) {
                assert();
                return st.getSampleHeader(fileName);
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