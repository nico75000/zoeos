package com.pcmsolutions.aspi;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 13-Jan-2004
 * Time: 20:01:02
 * To change this template use Options | File Templates.
 */
public class ASPI {
    public static final char SENSE_LEN = 14;			// Default sense buffer length
    public static final char SRB_DIR_SCSI = 0x00;		// Direction determined by SCSI 															// command
    public static final char SRB_DIR_IN = 0x08;		// Transfer from SCSI target to 															// host
    public static final char SRB_DIR_OUT = 0x10;		// Transfer from host to SCSI 															// target
    public static final char SRB_POSTING = 0x01;		// Enable ASPI posting
    public static final char SRB_EVENT_NOTIFY = 0x40;        // Enable ASPI event notification
    public static final char SRB_ENABLE_RESIDUAL_COUNT = 0x04;		// Enable residual byte count 															// reporting
    public static final char SRB_DATA_SG_LIST = 0x02;		// Data buffer points to 																	// scatter-gather list
    //public final int WM_ASPIPOST = 0x4D42 ;		// ASPI Post message


//***************************************************************************
//						 %%% ASPI Command Definitions %%%
//***************************************************************************
    public static final char SC_HA_INQUIRY = 0x00;		// Host adapter inquiry
    public static final char SC_GET_DEV_TYPE = 0x01;		// Get device type
    public static final char SC_EXEC_SCSI_CMD = 0x02;		// Execute SCSI command
    public static final char SC_ABORT_SRB = 0x03;		// Abort an SRB
    public static final char SC_RESET_DEV = 0x04;		// SCSI bus device reset
    public static final char SC_GET_DISK_INFO = 0x06;		// Get Disk information
    public static final char SC_RESCAN_SCSI_BUS = 0x07;		// Rescan SCSI bus

//***************************************************************************
//								  %%% SRB Status %%%
//***************************************************************************
    public static final char SS_PENDING = 0x00;	// SRB being processed
    public static final char SS_COMP = 0x01;	// SRB completed without error
    public static final char SS_ABORTED = 0x02;	// SRB aborted
    public static final char SS_ABORT_FAIL = 0x03;	// Unable to abort SRB
    public static final char SS_ERR = 0x04;	// SRB completed with error

    public static final char SS_INVALID_CMD = 0x80;		// Invalid ASPI command
    public static final char SS_INVALID_HA = 0x81;		// Invalid host adapter number
    public static final char SS_NO_DEVICE = 0x82;	// SCSI device not installed

    public static final char SS_INVALID_SRB = 0xE0;		// Invalid parameter set in SRB
    public static final char SS_FAILED_INIT = 0xE4;		// ASPI for windows failed stateInitial
    public static final char SS_ASPI_IS_BUSY = 0xE5;		// No resources available to execute cmd
    public static final char SS_BUFFER_TO_BIG = 0xE6;		// Buffer size to big to handle!

//***************************************************************************
//							%%% Host Adapter Status %%%
//***************************************************************************
    public static final char HASTAT_OK = 0x00;	// Host adapter did not detect an 															// error
    public static final char HASTAT_SEL_TO = 0x11;	// Selection Timeout
    public static final char HASTAT_DO_DU = 0x12;	// Data overrun data underrun
    public static final char HASTAT_BUS_FREE = 0x13;	// Unexpected bus free
    public static final char HASTAT_PHASE_ERR = 0x14;	// Target bus phase sequence 																// failure
    public static final char HASTAT_TIMEOUT = 0x09;	// Timed out while SRB was 																	waiting to beprocessed.
    public static final char HASTAT_COMMAND_TIMEOUT = 0x0B;	// While processing the SRB, the
    // adapter timed out.
    public static final char HASTAT_MESSAGE_REJECT = 0x0D;	// While processing SRB, the 																// adapter received a MESSAGE 															// REJECT.
    public static final char HASTAT_BUS_RESET = 0x0E;// A bus reset was detected.
    public static final char HASTAT_PARITY_ERROR = 0x0F;	// A parity error was detected.
    public static final char HASTAT_REQUEST_SENSE_FAILED = 0x10;	// The adapter failed in issuing
    //   REQUEST SENSE. 
}
