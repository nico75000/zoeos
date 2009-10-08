package com.pcmsolutions.aspi;

import com.excelsior.xFunction.*;
import com.pcmsolutions.system.ZUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 11-Jan-2004
 * Time: 12:23:18
 * To change this template use Options | File Templates.
 */
public class ASPIMsg {
    public static Exception lastCriticalASPIException = null;

    public static xFunction aspiSend = null;

    static {
        try {
            aspiSend = new xFunction("wnaspi32", "int SendASPI32Command(int*)");
            //com.pcmsolutions.aspi.VoidStruct*
        } catch (LibraryNotFoundException e) {
            e.printStackTrace();
        } catch (FunctionNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalSignatureException e) {
            e.printStackTrace();
        }
    }

    public static xFunction aspiGetSupportInfo = null;

    static {
        try {
            aspiGetSupportInfo = new xFunction("wnaspi32", "int GetASPI32SupportInfo()");
        } catch (LibraryNotFoundException e) {
            e.printStackTrace();
        } catch (FunctionNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalSignatureException e) {
            e.printStackTrace();
        }
    }

    public static class ASPIUnavailableException extends Exception {
        public ASPIUnavailableException(String message) {
            super(message);
        }
    }

    public static class ASPIWrapperException extends Exception {
        public ASPIWrapperException(String message) {
            super(message);
        }
    }

    public abstract static class SRB extends Structure {
        // WRITE
        private char srb_cmd = 0;
        // READ
        char srb_status = 0;
        // WRITE
        private char srb_haid = 0;
        // READ
        private char srb_flags = ASPI.SRB_DIR_SCSI;    // 0

        private int srb_hdr_rsvd = 0;

        SRB() {
        }

        void init(char cmd, char haid) {
            srb_cmd = cmd;
            srb_haid = haid;
        }

        void init(char cmd, char haid, char flags) {
            srb_flags = flags;
            init(cmd, haid);
        }

        char getSrb_flags() {
            return srb_flags;
        }

        void setSrb_flags(char srb_flags) {
            this.srb_flags = srb_flags;
        }

        public String defineLayout() {
            return "char srb_cmd, char srb_status, char srb_haid, char srb_flags, int srb_hdr_rsvd";
        }

        public Result execute() throws ASPIUnavailableException, ASPIWrapperException {
            if (aspiSend == null)
                throw new ASPIUnavailableException((lastCriticalASPIException == null ? "unknown problem" : lastCriticalASPIException.getMessage()));
            try {
                final Pointer p = Pointer.createPointerTo(this).cast("int*");
                final int rv = ((Integer) aspiSend.invoke(p)).intValue();
                final SRB f_srb = (SRB) p.cast(this.getClass().getName() + "*").deref();
                return new Result() {
                    public int getReturnValue() {
                        return rv;
                    }

                    public SRB getReturnedStruct() {
                        return f_srb;
                    }
                };
            } catch (WrongArgumentNumberException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (IncompatibleArgumentTypeException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (IllegalStructureException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (IllegalSignatureException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (NullDereferenceException e) {
                throw new ASPIWrapperException(e.getMessage());
            }
        }
    }

//***************************************************************************
//			 %%% SRB - HOST ADAPTER INQUIRY - SC_HA_INQUIRY %%%
//***************************************************************************
    /*typedef struct {
        BYTE	SRB_Cmd;				// ASPI command code = SC_HA_INQUIRY
        BYTE	SRB_Status;				// ASPI command status byte
        BYTE	SRB_HaId;				// ASPI host adapter number
        BYTE	SRB_Flags;				// ASPI request flags
        DWORD	SRB_Hdr_Rsvd;			// Reserved, MUST = 0
        BYTE	HA_Count;				// Number of host adapters present
        BYTE	HA_SCSI_ID;				// SCSI ID of host adapter
        BYTE	HA_ManagerId[16];		// String describing the manager
        BYTE	HA_Identifier[16];		// String describing the host adapter
        BYTE	HA_Unique[16];			// Host Adapter Unique parameters
        WORD	HA_Rsvd1;
    } SRB_HAInquiry, *PSRB_HAInquiry;
    */


    public static class SRB_HAInquiry extends SRB {
        // READ
        public char srb_ha_count = 0;
        public char srb_ha_scsi_id = 0;
        public char[] srb_ha_managerid = new char[16];
        public char[] srb_ha_identifier = new char[16];
        public char[] srb_ha_unique = new char[16];

        short srb_ha_rsvd1 = 0;

        SRB_HAInquiry() {
        }

        void init(char haid) {
            super.init(ASPI.SC_HA_INQUIRY, haid);
        }

        public String defineLayout() {
            return super.defineLayout() + ",char srb_ha_count, char srb_ha_scsi_id, char[16] srb_ha_managerid, char[16] srb_ha_identifier, char[16] srb_ha_unique, short srb_ha_rsvd1";
        }
    }


// Rescan SCSI Port
    /*
    typedef struct
    {
        BYTE	SRB_Cmd;				// ASPI code = SC_RESCAN_SCSI_BUS
        BYTE	SRB_Status;				// ASPI command status byte
        BYTE	SRB_HaId;				// ASPI host adapter number
        BYTE	SRB_Flags;				// ASPI request flags
        DWORD	SRB_Hdr_Rsvd;			// Reserved, MUST = 0
    } SRB_RescanPort, *PSRB_RescanPort;
    */

    public static class SRB_RescanPort extends SRB {
        SRB_RescanPort() {
        }

        void init(char haid) {
            super.init(ASPI.SC_RESCAN_SCSI_BUS, haid);
        }
    }

//***************************************************************************
//			  %%% SRB - GET DEVICE TYPE - SC_GET_DEV_TYPE %%%
//***************************************************************************
    /*
    typedef struct {

        BYTE	SRB_Cmd;				// ASPI command code = SC_GET_DEV_TYPE
        BYTE	SRB_Status;				// ASPI command status byte
        BYTE	SRB_HaId;				// ASPI host adapter number
        BYTE	SRB_Flags;				// Reserved
        DWORD	SRB_Hdr_Rsvd;			// Reserved
        BYTE	SRB_Target;				// Target's SCSI ID
        BYTE	SRB_Lun;				// Target's LUN number
        BYTE	SRB_DeviceType;			// Target's peripheral device type
        BYTE	SRB_Rsvd1;

    } SRB_GDEVBlock, *PSRB_GDEVBlock;
    */
    public static class SRB_GDEVBlock extends SRB {
        // WRITE
        private char srb_target = 0;
        private char srb_lun = 0;

        // READ
        public char srb_devicetype = 0;

        private char srb_ha_rsvd1 = 0;

        SRB_GDEVBlock() {
        }

        void init(char haid, char target, char lun) {
            super.init(ASPI.SC_GET_DEV_TYPE, haid);
            srb_target = target;
            srb_lun = lun;
        }

        public String defineLayout() {
            return super.defineLayout() + ", char srb_target, char srb_lun, char srb_devicetype, char srb_ha_rsvd1";
        }
    }


//***************************************************************************
//		  %%% SRB - EXECUTE SCSI COMMAND - SC_EXEC_SCSI_CMD %%%
//***************************************************************************
    /*
    typedef struct {
        BYTE	SRB_Cmd;				// ASPI command code = SC_EXEC_SCSI_CMD
        BYTE	SRB_Status;				// ASPI command status byte
        BYTE	SRB_HaId;				// ASPI host adapter number
        BYTE	SRB_Flags;				// ASPI request flags
        DWORD	SRB_Hdr_Rsvd;			// Reserved
        BYTE	SRB_Target;				// Target's SCSI ID
        BYTE	SRB_Lun;				// Target's LUN number
        WORD 	SRB_Rsvd1;				// Reserved for Alignment
        DWORD	SRB_BufLen;				// Data Allocation Length
        BYTE	*SRB_BufPointer;		// Data Buffer Pointer
        BYTE	SRB_SenseLen;			// Sense Allocation Length
        BYTE	SRB_CDBLen;				// CDB Length
        BYTE	SRB_HaStat;				// Host Adapter Status
        BYTE	SRB_TargStat;			// Target Status
        void	*SRB_PostProc;			// Post routine
        void	*SRB_Rsvd2;				// Reserved
        BYTE	SRB_Rsvd3[16];			// Reserved for alignment
        BYTE	CDBByte[16];			// SCSI CDB
        BYTE	SenseArea[SENSE_LEN+2];	// Request Sense buffer

    } SRB_ExecSCSICmd, *PSRB_ExecSCSICmd;
    */

    private static final int CDB_LEN = 16;

    public static class SRB_ExecSCSICmd extends SRB {
        private static final SRB_ExecSCSICmd INSTANCE = new SRB_ExecSCSICmd();

        // WRITE
        private char srb_target = 0;
        private char srb_lun = 0;
        private short srb_rsvd1 = 0;
        private int srb_buflen;
        private Pointer srb_bufpointer;
        private char srb_senselen = ASPI.SENSE_LEN + 2;
        private char srb_cdblen;

        // READ
        public char srb_hastat = 0;
        public char srb_targstat = 0;

        private ASPICallback srb_postproc = new ASPICallback();

        private ASPICallback srb_rsvd2 = null;
        private char[] srb_rsvd3 = new char[16];
        private char[] cdbbyte;
        private char[] sensearea = new char[ASPI.SENSE_LEN + 2];

        public String defineLayout() {
            return super.defineLayout() + ", char srb_target, char srb_lun, short srb_rsvd1, int srb_buflen, char* srb_bufpointer, char srb_senselen,char srb_cdblen, char srb_hastat, char srb_targstat, com.pcmsolutions.aspi.ASPICallback srb_postproc, com.pcmsolutions.aspi.ASPICallback srb_rsvd2, char[16] srb_rsvd3, char[" + CDB_LEN + "] cdbbyte, char[" + (ASPI.SENSE_LEN + 2) + "] sensearea";
        }

        protected void finalize() {
            srb_postproc.free();
        }

        SRB_ExecSCSICmd() {

        }

        public static SRB_ExecSCSICmd getInstance(char haid, char target, char lun, char[] buf, char[] cdb, char flags) throws ASPIWrapperException {
            INSTANCE.init(haid, target, lun, buf, cdb, flags);
            return INSTANCE;
        }

        void init(char haid, char target, char lun, char[] buf, char[] cdb) throws ASPIWrapperException {
            init(ASPI.SC_EXEC_SCSI_CMD, haid, target, buf, cdb, (char) 0);
        }

        void init(char haid, char target, char lun, char[] buf, char[] cdb, char flags) throws ASPIWrapperException {
            super.init(ASPI.SC_EXEC_SCSI_CMD, haid, (char) (flags | ASPI.SRB_POSTING));
            srb_target = target;
            srb_lun = lun;
            try {
                srb_bufpointer = (Pointer) Argument.create("char*", buf);
            } catch (IllegalSignatureException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (TypesDifferentException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (IllegalStringConversionException e) {
                throw new ASPIWrapperException(e.getMessage());
            }
            srb_buflen = buf.length;
            if (cdb.length > CDB_LEN)
                throw new IllegalArgumentException("illegal command descriptor block length");

            cdbbyte = new char[CDB_LEN];
            System.arraycopy(cdb, 0, cdbbyte, 0, cdb.length);
            srb_cdblen = (char) cdb.length;
        }

        public char[] getBuffer() throws ASPIWrapperException {
            try {
                //System.out.println(srb_bufpointer.deref().getClass().getName());
                return (char[])srb_bufpointer.createArray(srb_buflen);
            } catch (NullDereferenceException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (IllegalDimensionException e) {
                throw new ASPIWrapperException(e.getMessage());
            }
        }

        public Result execute() throws ASPIUnavailableException, ASPIWrapperException {
            srb_postproc.reset();
            Pointer p;
            int rv;
            try {
                p = new Argument(this).createPointer();
                synchronized (srb_postproc) {
                    rv = ((Integer) aspiSend.invoke(p.cast("int*"))).intValue();
                    while (srb_postproc.getHits() == 0) {
                        try {
                            // System.out.println("waiting on " + srb_postproc.toString());
                            srb_postproc.wait();
                            // System.out.println("waiting finished on " + srb_postproc.toString());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                final SRB f_srb = (SRB) p.deref();
                final int f_rv = rv;
                return new Result() {
                    public int getReturnValue() {
                        return f_rv;
                    }

                    public SRB getReturnedStruct() throws ASPIWrapperException {
                        return f_srb;
                    }
                };

            } catch (IllegalStructureException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (IllegalSignatureException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (WrongArgumentNumberException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (IncompatibleArgumentTypeException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (NullDereferenceException e) {
                throw new ASPIWrapperException(e.getMessage());
            }
        }
    }

//***************************************************************************
//				  %%% SRB - ABORT AN SRB - SC_ABORT_SRB %%%
//***************************************************************************
    /*
        typedef struct {

        BYTE	SRB_Cmd;				// ASPI command code = SC_EXEC_SCSI_CMD
        BYTE	SRB_Status;				// ASPI command status byte
        BYTE	SRB_HaId;				// ASPI host adapter number
        BYTE	SRB_Flags;				// Reserved
        DWORD	SRB_Hdr_Rsvd;			// Reserved
        void	*SRB_ToAbort;			// Pointer to SRB to abort

    } SRB_Abort, *PSRB_Abort;
    */
    public static class SRB_Abort extends SRB {
        // WRITE
        private Pointer srb_toabort;

        SRB_Abort() {

        }

        void init(char haid, SRB toAbort) throws IllegalStructureException {
            super.init(ASPI.SC_ABORT_SRB, haid);
            srb_toabort = Pointer.createPointerTo(toAbort);
        }

        public String defineLayout() {
            return super.defineLayout() + ", char* srb_toabort";
        }
    }
//***************************************************************************
//				%%% SRB - BUS DEVICE RESET - SC_RESET_DEV %%%
//***************************************************************************
/*
    typedef struct {

        BYTE	SRB_Cmd;				// ASPI command code = SC_EXEC_SCSI_CMD
        BYTE	SRB_Status;				// ASPI command status byte
        BYTE	SRB_HaId;				// ASPI host adapter number
        BYTE	SRB_Flags;				// Reserved
        DWORD	SRB_Hdr_Rsvd;			// Reserved
        BYTE	SRB_Target;				// Target's SCSI ID
        BYTE	SRB_Lun;				// Target's LUN number
        BYTE 	SRB_Rsvd1[12];			// Reserved for Alignment
        BYTE	SRB_HaStat;				// Host Adapter Status
        BYTE	SRB_TargStat;			// Target Status
        void 	*SRB_PostProc;			// Post routine
        void	*SRB_Rsvd2;				// Reserved
        BYTE	SRB_Rsvd3[16];			// Reserved
        BYTE	CDBByte[16];			// SCSI CDB

    } SRB_BusDeviceReset, *PSRB_BusDeviceReset;
*/
    public static class SRB_BusDeviceReset extends SRB {
        private static final SRB_BusDeviceReset INSTANCE = new SRB_BusDeviceReset();

        // WRITE
        private char srb_target = 0;
        private char srb_lun = 0;

        private char[] srb_rsvd1 = new char[12];

        // READ
        public char srb_hastat = 0;
        public char srb_targstat = 0;

        private ASPICallback srb_postproc = new ASPICallback();
        private ASPICallback srb_rsvd2 = null;
        private char[] srb_rsvd3 = new char[16];
        private char[] cdbbyte = new char[16];  // ??

        SRB_BusDeviceReset() {

        }

        public static SRB_BusDeviceReset getInstance(char haid, char target, char lun) {
            INSTANCE.init(haid, target, lun);
            return INSTANCE;
        }

        void init(char haid, char target, char lun) {
            super.init(ASPI.SC_RESET_DEV, haid, ASPI.SRB_POSTING);
            srb_target = target;
            srb_lun = lun;
        }

        public String defineLayout() {
            return super.defineLayout() + ", char srb_target, char srb_lun, char[12] srb_rsvd1, char srb_hastat, char srb_targstat, BusDeviceResetCallback* srb_postproc, ReservedCallback* srb_rsvd2, char[16] srb_rsvd3, char[16] cdbbyte ";
        }

        public Result execute() throws ASPIUnavailableException, ASPIWrapperException {
            srb_postproc.reset();
            Pointer p;
            int rv;
            try {
                p = new Argument(this).createPointer();
                synchronized (srb_postproc) {
                    rv = ((Integer) aspiSend.invoke(p.cast("int*"))).intValue();
                    while (srb_postproc.getHits() == 0) {
                        try {
                            // System.out.println("waiting on " + srb_postproc.toString());
                            srb_postproc.wait();
                            // System.out.println("waiting finished on " + srb_postproc.toString());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                final SRB f_srb = (SRB) p.deref();
                final int f_rv = rv;
                return new Result() {
                    public int getReturnValue() {
                        return f_rv;
                    }

                    public SRB getReturnedStruct() throws ASPIWrapperException {
                        return f_srb;
                    }
                };

            } catch (IllegalStructureException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (IllegalSignatureException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (WrongArgumentNumberException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (IncompatibleArgumentTypeException e) {
                throw new ASPIWrapperException(e.getMessage());
            } catch (NullDereferenceException e) {
                throw new ASPIWrapperException(e.getMessage());
            }
        }
    }


//***************************************************************************
//				%%% SRB - GET DISK INFORMATION - SC_GET_DISK_INFO %%%
//***************************************************************************
/*
    typedef struct {

        BYTE	SRB_Cmd;				// ASPI command code = SC_EXEC_SCSI_CMD
        BYTE	SRB_Status;				// ASPI command status byte
        BYTE	SRB_HaId;				// ASPI host adapter number
        BYTE	SRB_Flags;				// Reserved
        DWORD	SRB_Hdr_Rsvd;			// Reserved
        BYTE	SRB_Target;				// Target's SCSI ID
        BYTE	SRB_Lun;				// Target's LUN number
        BYTE 	SRB_DriveFlags;			// Driver flags
        BYTE	SRB_Int13HDriveInfo;	// Host Adapter Status
        BYTE	SRB_Heads;				// Preferred number of heads translation
        BYTE	SRB_Sectors;			// Preferred number of sectors translation
        BYTE	SRB_Rsvd1[10];			// Reserved
    } SRB_GetDiskInfo, *PSRB_GetDiskInfo;
*/

    public static class SRB_GetDiskInfo extends SRB {
        // WRITE
        private char srb_target = 0;
        private char srb_lun = 0;

        // READ
        public char srb_driveflags = 0;
        public char srb_int13hdriveinfo = 0;
        public char srb_heads = 0;
        public char srb_sectors = 0;

        private char[] srb_rsvd1 = new char[10];

        SRB_GetDiskInfo() {

        }

        void init(char haid, char target, char lun) {
            super.init(ASPI.SC_GET_DISK_INFO, haid);
            srb_target = target;
            srb_lun = lun;
        }

        public String defineLayout() {
            return super.defineLayout() + ", char srb_target,char srb_lun, char srb_driveflags, char srb_int13hdriveinfo, char srb_heads, char srb_sectors, char[10] srb_rsvd1 ";
        }
    }

    public static final void main(String[] args) {
        int rv;
        int adapters = 0;
        try {
            rv = ((Integer) aspiGetSupportInfo.invoke()).intValue();
            adapters = ZUtilities.lobyte(ZUtilities.loword(rv));
        } catch (WrongArgumentNumberException e) {
            e.printStackTrace();
        } catch (IncompatibleArgumentTypeException e) {
            e.printStackTrace();
        }

        SRB_HAInquiry inq;
        Result res;
        ASPIMsg.SRB_HAInquiry r_inq;
        SRB_GDEVBlock info;
        for (int a = 0; a < adapters; a++) {
            inq = new SRB_HAInquiry();
            inq.init((char) a);
            try {
                res = inq.execute();
                r_inq = (ASPIMsg.SRB_HAInquiry) res.getReturnedStruct();
            } catch (ASPIUnavailableException e) {
                e.printStackTrace();
            } catch (ASPIWrapperException e) {
                e.printStackTrace();
            }
            for (int d = 0; d < 8; d++) {
                info = new SRB_GDEVBlock();
                info.init((char) a, (char) d, (char) 0);
                try {
                    res = info.execute();
                    info = (SRB_GDEVBlock) res.getReturnedStruct();
                    if (res.getReturnValue() == ASPI.SS_COMP) {
                        System.out.println((int) info.srb_devicetype);
                    } else
                        System.out.println("error: " + res.getReturnValue());
                } catch (ASPIUnavailableException e) {
                    e.printStackTrace();
                } catch (ASPIWrapperException e) {
                    e.printStackTrace();
                }
            }
        }
        /* DInfo.SRB_Cmd = SC_GET_DEV_TYPE;
         DInfo.SRB_HaId = ha_id;
         DInfo.SRB_Flags = 0;
         DInfo.SRB_Hdr_Rsvd = 0;
         DInfo.SRB_Target = id;
         DInfo.SRB_Lun = 0;

         SendASPI32Command((LPSRB) & DInfo);

         while (DInfo.SRB_Status == SS_PENDING) dummyFunc(DInfo.SRB_Status);

         return DInfo.SRB_DeviceType;
         */
    }
}
