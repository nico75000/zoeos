package com.pcmsolutions.smdi;

import com.pcmsolutions.aspi.SCSI;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-Sep-2003
 * Time: 20:07:38
 * To change this template use Options | File Templates.
 */
class Impl_ScsiDeviceInfo {
    public static final String NO_DEVICE = "No Device";
    public static final String NO_MANUFACTURER = " ";
    private boolean isSMDI;
    private String name = NO_DEVICE;
    private String manufacturer = NO_MANUFACTURER;
    private byte deviceType = (byte) SCSI.DTYPE_UNKNOWN;

    public Impl_ScsiDeviceInfo() {
    }

    public Impl_ScsiDeviceInfo(SMDILogic.DeviceInfo di) {
        isSMDI = di.isSMDI();
        name = di.getName();
        manufacturer = di.getManufacturer();
        deviceType = (byte) di.getDeviceType();
    }

    public byte getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(byte deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isSMDI() {
        return isSMDI;
    }

    public void setSMDI(boolean SMDI) {
        isSMDI = SMDI;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getName() {
        return name;
    }

    public boolean isDevice() {
        return name != NO_DEVICE && manufacturer != NO_MANUFACTURER;
    }

    public void setName(String name) {
        this.name = name;
    }
    /*typedef struct SCSI_DevInfo
    {
      DWORD dwStructSize;                   // (00)
      BOOL bSMDI;                           // (04)
      BYTE DevType;                         // (08)
      BYTE Rsvd1;                           // (09)
      BYTE Rsvd2;                           // (10)
      BYTE Rsvd3;                           // (11)
      char cName[20];                       // (12)
      char cManufacturer[12];               // (32)
    } SCSI_DevInfo;
      */
}
