package com.pcmsolutions.aspi;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 13-Jan-2004
 * Time: 20:00:06
 * To change this template use Options | File Templates.
 */
public class SCSI {
//***************************************************************************
//                %%% PERIPHERAL DEVICE TYPE DEFINITIONS %%%
//***************************************************************************
    public static final char DTYPE_DASD = 0x00;    // Disk Device
    public static final char DTYPE_SEQD = 0x01;    // Tape Device
    public static final char DTYPE_PRNT = 0x02;    // Printer
    public static final char DTYPE_PROC = 0x03;    // Processor
    public static final char DTYPE_WORM = 0x04;    // Write-once read-multiple
    public static final char DTYPE_CDROM = 0x05;    // CD-ROM device
    public static final char DTYPE_SCAN = 0x06;    // Scanner device
    public static final char DTYPE_OPTI = 0x07;    // Optical memory device
    public static final char DTYPE_JUKE = 0x08;    // Medium Changer device
    public static final char DTYPE_COMM = 0x09;    // Communications device
    public static final char DTYPE_RESL = 0x0A;    // Reserved (low)
    public static final char DTYPE_RESH = 0x1E;    // Reserved (high)
    public static final char DTYPE_UNKNOWN = 0x1F;    // Unknown or no device type

/*
    ;***************************************************************************
    ;                  Commands for all Device Types
    ;***************************************************************************
    SCSI_CHANGE_DEF    EQU 40H   ;Change Definition (Optional)
    SCSI_COMPARE       EQU 39H   ;Compare (optional)
    SCSI_COPY          EQU 18H   ;Copy (optional)
    SCSI_COP_VERIFY    EQU 3AH   ;Copy and Verify (optional)
    SCSI_INQUIRY       EQU 12H   ;Inquiry (MANDATORY)
    SCSI_LOG_SELECT    EQU 4CH   ;Log Select (optional)
    SCSI_LOG_SENSE     EQU 4DH   ;Log Sense (optional)
    SCSI_MODE_SEL6     EQU 15H   ;Mode Select 6-byte (Device Specific)
    SCSI_MODE_SEL10    EQU 55H   ;Mode Select 10-byte (Device Specific)
    SCSI_MODE_SEN6     EQU 1AH   ;Mode Sense 6-byte (Device Specific)
    SCSI_MODE_SEN10    EQU 5AH   ;Mode Sense 10-byte (Device Specific)
    SCSI_READ_BUFF     EQU 3CH   ;Read Buffer (optional)
    SCSI_REQ_SENSE     EQU 03H   ;Request Sense (MANDATORY)
    SCSI_SEND_DIAG     EQU 1DH   ;Send Diagnostic (optional)
    SCSI_TST_U_RDY     EQU 00H   ;Test Unit Ready (MANDATORY)
    SCSI_WRITE_BUFF    EQU 3BH   ;Write Buffer (optional)
*/
    public static final char SCSI_INQUIRY = 0x12;			// Inquiry (MANDATORY)
    public static final char SCSI_TST_U_RDY = 0x00;			// Test Unit Ready (MANDATORY)

}
