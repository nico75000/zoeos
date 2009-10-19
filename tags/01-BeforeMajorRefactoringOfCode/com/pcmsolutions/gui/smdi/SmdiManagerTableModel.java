package com.pcmsolutions.gui.smdi;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.smdi.*;
import com.pcmsolutions.system.DeviceNames;
import com.pcmsolutions.system.ZExternalDevice;
import com.pcmsolutions.system.Zoeos;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Sep-2003
 * Time: 15:01:26
 * To change this template use Options | File Templates.
 */
public class SmdiManagerTableModel extends AbstractRowHeaderedAndSectionedTableModel implements SMDIAgent.SmdiListener {

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 150, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[5];
        columnData[0] = new ColumnData("SMDI", 50, JLabel.LEFT, 0, String.class, null, null);
        columnData[1] = new ColumnData("Host Adapter Id", 80, JLabel.LEFT, 0, String.class, null, null);
        columnData[2] = new ColumnData("SCSI Id", 50, JLabel.LEFT, 0, String.class, null, null);
        columnData[3] = new ColumnData("Manufacturer", 150, JLabel.LEFT, 0, String.class, null, null);
        columnData[4] = new ColumnData("Coupled Midi Device", 200, JLabel.LEFT, 0, String.class, null, null);
        sectionData = new SectionData[]{new SectionData(UIColors.getTableFirstSectionBG(), UIColors.getTableFirstSectionFG(), 530, "")};
    }

    protected void doRefresh() {
        if (!SMDIAgent.isSmdiAvailable())
            return;
        SMDIAgent.removeSmdiListener(this);
        SMDIAgent.addSmdiListener(this);
        ScsiTarget[] devices = new ScsiTarget[0];
        try {
            devices = SMDIAgent.getDevices();
            for (int i = 0, n = devices.length; i < n; i++) {
                final ScsiTarget st = devices[i];
                tableRowObjects.add(new ColumnValueProvider() {
                    public Object getValueAt(int col) {
                        switch (col) {
                            case 0:
                                return st;
                            case 1:
                                return (st.isSMDI() ? "Yes" : "no");
                            case 2:
                                return String.valueOf(st.getHA_Id());
                            case 3:
                                return String.valueOf(st.getSCSI_Id());
                            case 4:
                                return st.getDeviceManufacturer();
                            case 5:
                                if (st instanceof SmdiTarget)
                                    if (((SmdiTarget) st).isCoupled())
                                        try {
                                            String prefix;
                                            ZExternalDevice d = Zoeos.getInstance().getDeviceManager().getDeviceMatchingIdentityMessageString(((SmdiTarget) st).getCouplingString());
                                            if (d != null)
                                                prefix = d.getName() + "   ";
                                            else
                                                prefix = DeviceNames.getNameForDevice(((SmdiTarget) st).getCouplingString(), "") + "   ";    // getCouplingString() should be toString() of identity message, so this alternative procedure may work

                                            return prefix + "[" + ((SmdiTarget) st).getCouplingString() + " ]";
                                        } catch (SmdiTargetNotCoupledException e) {
                                            e.printStackTrace();
                                        }
                        }
                        return "";
                    }

                    public void zDispose() {
                    }
                });
            }
        } catch (SmdiUnavailableException e) {
            e.printStackTrace();
        }
    }

    protected void doPreRefresh() {
    }

    protected void doPostRefresh() {
    }

    public void zDispose() {
        super.zDispose();
        SMDIAgent.removeSmdiListener(this);
    }

    public void SmdiChanged() {
        refresh(true);
    }
}
