package com.pcmsolutions.gui.license;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.license.LicenseKeyManager;
import com.pcmsolutions.system.IntPool;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Sep-2003
 * Time: 15:01:26
 * To change this template use Options | File Templates.
 */
public class LicenseManagerTableModel extends AbstractRowHeaderedAndSectionedTableModel implements LicenseKeyManager.LicenseKeyListener {

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 30, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[5];
        columnData[0] = new ColumnData("Product", 100, JLabel.LEFT, 0, String.class, null, null);
        columnData[1] = new ColumnData("Type", 120, JLabel.LEFT, 0, String.class, null, null);
        columnData[2] = new ColumnData("Load", 50, JLabel.LEFT, 0, String.class, null, null);
        columnData[3] = new ColumnData("Licensee", 180, JLabel.LEFT, 0, String.class, null, null);
        columnData[4] = new ColumnData("Key String", 200, JLabel.LEFT, 0, String.class, null, null);
        sectionData = new SectionData[]{new SectionData(UIColors.getTableFirstSectionBG(), UIColors.getTableFirstSectionFG(), 650, "")};
    }

    protected void doRefresh() {
        LicenseKeyManager.removeLicenseKeyListener(this);
        LicenseKeyManager.addLicenseKeyListener(this);
        LicenseKeyManager.LicenseKey[] keys = new LicenseKeyManager.LicenseKey[0];
        try {
            keys = LicenseKeyManager.getAllKeys();
            for (int i = 0, n = keys.length; i < n; i++) {
                final LicenseKeyManager.LicenseKey key = keys[i];
                final int f_i = i;
                tableRowObjects.add(new ColumnValueProvider() {
                    public Object getValueAt(int col) {
                        switch (col) {
                            case 0:
                                return IntPool.get(f_i + 1);
                            case 1:
                                return key.getProduct();
                            case 2:
                                return key.getType();
                            case 3:
                                return IntPool.get(key.getLoad());
                            case 4:
                                return key.getRegName();
                            case 5:
                                return key.toString();
                        }
                        return "";
                    }

                    public void zDispose() {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doPreRefresh() {
    }

    protected void doPostRefresh() {
    }

    public void zDispose() {
        super.zDispose();
        LicenseKeyManager.removeLicenseKeyListener(this);
    }

    public void licenseKeysChanged() {
        refresh(true);
    }
}
