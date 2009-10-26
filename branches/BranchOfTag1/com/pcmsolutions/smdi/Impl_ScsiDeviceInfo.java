package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-Sep-2003
 * Time: 20:07:38
 * To change this template use Options | File Templates.
 */
class Impl_ScsiDeviceInfo {
    private boolean isSMDI;
    private String name;
    private String manufacturer;
    private byte deviceType;

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

    public void setName(String name) {
        this.name = name;
    }    /*typedef struct SCSI_DevInfo
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
