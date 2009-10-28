package com.pcmsolutions.aspi;

import com.excelsior.xFunction.IllegalStructureException;
import com.pcmsolutions.system.ZUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 12-Jan-2004
 * Time: 14:50:14
 * To change this template use Options | File Templates.
 */
public class ASPILogic {

    public static class ASPILogicException extends Exception {
        public ASPILogicException(String message) {
            super(message);
        }
    }

    public static class ASPINoDeviceException extends Exception {
        public ASPINoDeviceException(String message) {
            super(message);
        }
    }

    public static class CommandFailedException extends Exception {
        private int srbStatus;

        public CommandFailedException(String message, int srbStatus) {
            super(message);
            this.srbStatus = srbStatus;
        }
    }

    private static void checkResult(Result r) throws CommandFailedException, ASPINoDeviceException {
        if (r.getReturnValue() == ASPI.SS_NO_DEVICE)
            throw new ASPINoDeviceException("No device present");
        if (r.getReturnedStruct().srb_status != ASPI.SS_COMP) {
            throw new CommandFailedException("ASPI Error", r.getReturnedStruct().srb_status);
        }
    }

    private static void abort(int haid, ASPIMsg.SRB abortSRB) throws ASPILogicException {
        try {
            ASPIMsg.SRB_Abort exec = new ASPIMsg.SRB_Abort();
            exec.init((char) haid, abortSRB);
            if (exec.execute().getReturnValue() != ASPI.SS_COMP)
                throw new ASPILogicException("command failed");
        } catch (ASPIMsg.ASPIUnavailableException e) {
            throw new ASPILogicException(e.getMessage());
        } catch (ASPIMsg.ASPIWrapperException e) {
            throw new ASPILogicException(e.getMessage());
        } catch (IllegalStructureException e) {
            throw new ASPILogicException(e.getMessage());
        }
    }

    public static void rescanPort(int haid) throws ASPILogicException, CommandFailedException {
        ASPIMsg.SRB_RescanPort exec = new ASPIMsg.SRB_RescanPort();
        exec.init((char) haid);
        try {
            Result r = exec.execute();
            if (r.getReturnValue() != ASPI.SS_COMP)
                throw new CommandFailedException("ASPI Error", r.getReturnValue());
        } catch (ASPIMsg.ASPIUnavailableException e) {
            throw new ASPILogicException(e.getMessage());
        } catch (ASPIMsg.ASPIWrapperException e) {
            throw new ASPILogicException(e.getMessage());
        }
    }

    // private static final char[] defaultRecvBuffer = new char[512];

    public static byte[] performSMDI(int haid, int id, byte[] bytes, int recvLenInBytes) throws ASPILogicException, CommandFailedException {
        if (recvLenInBytes % 4 != 0)
            throw new IllegalArgumentException();

        char[] cdb = new char[6];

        cdb[0] = 0x0a; /* SEND */
        ZUtilities.applyBytesAsChar(cdb, bytes.length, 2, 3);

        try {
            ASPIMsg.SRB_ExecSCSICmd exec;
            exec = ASPIMsg.SRB_ExecSCSICmd.getSendInstance((char) haid, (char) id, (char) 0, bytes, cdb);
            Result r = exec.execute();

            int st = ((ASPIMsg.SRB_ExecSCSICmd) r.getReturnedStruct()).srb_status;
            if (st != ASPI.SS_COMP) {
                throw new CommandFailedException("ASPI Error", st);
            }

            cdb[0] = 0x08; /* RECV */
            ZUtilities.applyBytesAsChar(cdb, recvLenInBytes, 2, 3);
            exec = ASPIMsg.SRB_ExecSCSICmd.getRecvInstance((char) haid, (char) id, (char) 0, recvLenInBytes, cdb);
            r = exec.execute();
            exec = (ASPIMsg.SRB_ExecSCSICmd) r.getReturnedStruct();
            st = ((ASPIMsg.SRB_ExecSCSICmd) r.getReturnedStruct()).srb_status;
            if (st != ASPI.SS_COMP) {
                throw new CommandFailedException("ASPI Error", st);
            }
            return ((ASPIMsg.SRB_ExecSCSICmd) r.getReturnedStruct()).getBuffer();
            //  return buf;
        } catch (ASPIMsg.ASPIWrapperException e) {
            throw new ASPILogicException(e.getMessage());
        } catch (ASPIMsg.ASPIUnavailableException e) {
            throw new ASPILogicException(e.getMessage());
        } finally {
        }
    }

    public static boolean isDeviceProcessor(int haid, int id) throws ASPILogicException, CommandFailedException, ASPINoDeviceException {
        return getDeviceType(haid, id) == SCSI.DTYPE_PROC;
    }

    // -1 for no device
    public static int getDeviceType(int haid, int id) throws ASPILogicException, CommandFailedException, ASPINoDeviceException {
        ASPIMsg.SRB_GDEVBlock exec = new ASPIMsg.SRB_GDEVBlock();
        exec.init((char) haid, (char) id, (char) 0);
        try {
            Result r = exec.execute();
            checkResult(r);
            return ((ASPIMsg.SRB_GDEVBlock) r.getReturnedStruct()).srb_devicetype;
            /*if (r.getReturnValue() == ASPI.SS_COMP)
                return ((ASPIMsg.SRB_GDEVBlock) r.getReturnedStruct()).srb_devicetype;
            else {
                throw new CommandFailedException("ASPI Error", r.getReturnValue());
            } */
        } catch (ASPIMsg.ASPIUnavailableException e) {
            throw new ASPILogicException(e.getMessage());
        } catch (ASPIMsg.ASPIWrapperException e) {
            throw new ASPILogicException(e.getMessage());
        }
    }

    public static interface AdapterInfo {
        public int getAdapterCount();

        public int getAdapterSCSIId();

        public String getAdapterManagerIdentifier();

        public String getAdapterIdentifier();

        public char[] getAdapterUnique();
    }

    public static AdapterInfo adapterInquiry(int haid) throws ASPILogicException, CommandFailedException {
        ASPIMsg.SRB_HAInquiry exec = new ASPIMsg.SRB_HAInquiry();
        exec.init((char) haid);
        try {
            Result r = exec.execute();
            final ASPIMsg.SRB_HAInquiry f_struct = (ASPIMsg.SRB_HAInquiry) r.getReturnedStruct();
            if (r.getReturnValue() != ASPI.SS_COMP)
                throw new CommandFailedException("ASPI Error", r.getReturnValue());
            return new AdapterInfo() {
                public int getAdapterCount() {
                    return f_struct.srb_ha_count;
                }

                public int getAdapterSCSIId() {
                    return f_struct.srb_ha_scsi_id;
                }

                public String getAdapterManagerIdentifier() {
                    return new String(f_struct.srb_ha_managerid).trim();
                }

                public String getAdapterIdentifier() {
                    return new String(f_struct.srb_ha_identifier).trim();
                }

                public char[] getAdapterUnique() {
                    return (char[]) f_struct.srb_ha_unique.clone();
                }

                public String toString() {
                    return "Identifier = " + getAdapterIdentifier() + ", Manager ID = " + getAdapterManagerIdentifier() + ", SCSI ID = " + getAdapterSCSIId() + ", Adapter Count = " + getAdapterCount();
                }
            };
        } catch (ASPIMsg.ASPIUnavailableException e) {
            throw new ASPILogicException(e.getMessage());
        } catch (ASPIMsg.ASPIWrapperException e) {
            throw new ASPILogicException(e.getMessage());
        }
    }

    public static interface DeviceInfo {
        public String getManufacturer();

        public String getName();

        public int getHaId();

        public int getScsiId();

        public int getDeviceType();

        public char[] getResultBuffer();
    }

    // may return null to indicate no device
    public static DeviceInfo inquireDevice(final int haid, final int id) throws CommandFailedException, ASPILogicException, ASPINoDeviceException {
        // win32 cdb is always 16 bytes
        char[] cdb = new char[6];
        cdb[0] = SCSI.SCSI_INQUIRY;
        cdb[4] = 32;
        cdb[5] = 0x12;//??
        try {
            ASPIMsg.SRB_ExecSCSICmd exec;
            exec = ASPIMsg.SRB_ExecSCSICmd.getRecvInstance((char) haid, (char) id, (char) 0, 96, cdb);
            Result r = exec.execute();
            checkResult(r);
            byte[] byteBuf = ((ASPIMsg.SRB_ExecSCSICmd) r.getReturnedStruct()).getBuffer();
            final char[] charBuf = new char[byteBuf.length];
            ZUtilities.applyToCharArray(charBuf, byteBuf, 0);
            final String manu = new String(charBuf, 8, 8);
            final String name = new String(charBuf, 16, 16);
            return new DeviceInfo() {
                public String getManufacturer() {
                    return manu;
                }

                public String getName() {
                    return name;
                }

                public int getHaId() {
                    return haid;
                }

                public int getScsiId() {
                    return id;
                }

                public int getDeviceType() {
                    return charBuf[0] & 0x1F;
                }

                public char[] getResultBuffer() {
                    return (char[]) charBuf.clone();
                }
            };
        } catch (ASPIMsg.ASPIUnavailableException e) {
            throw new ASPILogicException(e.getMessage());
        } catch (ASPIMsg.ASPIWrapperException e) {
            throw new ASPILogicException(e.getMessage());
        }
    }

    public static boolean testUnitReady(int haid, int id) throws ASPILogicException, CommandFailedException {
        // win32 cdb is always 16 bytes
        char[] cdb = new char[6];
        int[] buf = new int[1];
        cdb[0] = SCSI.SCSI_TST_U_RDY;
        try {
            ASPIMsg.SRB_ExecSCSICmd exec;
            exec = ASPIMsg.SRB_ExecSCSICmd.getRecvInstance((char) haid, (char) id, (char) 0, 4, cdb);
            Result r = exec.execute();
            int rv = r.getReturnedStruct().srb_status;
            if (rv != ASPI.SS_COMP)
                throw new CommandFailedException("ASPI Error", rv);
        } catch (ASPIMsg.ASPIUnavailableException e) {
            throw new ASPILogicException(e.getMessage());
        } catch (ASPIMsg.ASPIWrapperException e) {
            throw new ASPILogicException(e.getMessage());
        }
        if ((buf[0] & 0x3f) == 0)
            return true;
        return false;
    }

    public static final void main(String[] args) {
        /* try {
             abort(0);
         } catch (SMDILogicException e) {
             e.printStackTrace();
         }
         */
        try {
            System.out.println(testUnitReady(0, 2));
            rescanPort(2);
            System.out.println(adapterInquiry(0));
        } catch (ASPILogicException e) {
            e.printStackTrace();
        } catch (CommandFailedException e) {
            e.printStackTrace();
        }
    }
}
