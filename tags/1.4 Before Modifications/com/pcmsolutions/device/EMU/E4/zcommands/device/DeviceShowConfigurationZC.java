package com.pcmsolutions.device.EMU.E4.zcommands.device;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.ZInstanceDialog;
import com.pcmsolutions.gui.ZoeosFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class DeviceShowConfigurationZC extends AbstractDeviceContextZCommand {

    public int getMnemonic() {
        return KeyEvent.VK_C;
    }

    public String getPresentationString() {
        return "Show configuration";
    }

    public String getDescriptiveString() {
        return "Show a report of the devices configuration";
    }

    public int getMaxNumTargets() {
        return 1;
    }

    public boolean handleTarget(final DeviceContext device, int total, int curr) throws Exception {
        final JTable t = new JTable(device.getDeviceConfigTableModel()) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        t.setPreferredSize(new Dimension(350, t.getPreferredSize().height));
        GeneralTableCellRenderer gr = new GeneralTableCellRenderer() {
            {
                setForeground(UIColors.getTableFirstSectionFG());
                setBackground(UIColors.getTableFirstSectionHeaderBG());
            }
        };
        t.setDefaultRenderer(Object.class, gr);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JDialog reportDlg = new ZInstanceDialog(ZoeosFrame.getInstance(), device.toString() + " Configuration", false);
                reportDlg.setContentPane(t);
                reportDlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                reportDlg.pack();
                reportDlg.setVisible(true);
            }
        });
        return true;
    }
}
