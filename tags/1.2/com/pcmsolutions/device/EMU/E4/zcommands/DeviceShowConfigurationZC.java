package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.gui.ZInstanceDialog;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;

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

    public DeviceShowConfigurationZC() {
        super("Show Configuration", "Show a report of the devices configuration", null, null);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        return null;
    }

    public int getMnemonic() {
        return KeyEvent.VK_C;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (getTarget() == null)
            throw new CommandFailedException();

//        tp.setText(getTarget().getDeviceConfigReport());
        //JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), getTarget().getDeviceConfigReport(), "Device Configuration", JOptionPane.INFORMATION_MESSAGE);

        /*final String[][] report = getTarget().getTabularDeviceConfigReport();
        Object[] colNames = new Object[report[0].length];
        Arrays.fill(colNames, "");
        */
        final JTable t = new JTable(getTarget().getDeviceConfigTableModel()) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        t.setPreferredSize(new Dimension(350, t.getPreferredSize().height));
        GeneralTableCellRenderer gr = new GeneralTableCellRenderer() {
            protected void setupLook(JTable table, Object value, boolean isSelected, int row, int column) {
                super.setupLook(table, value, isSelected, row, column);
                setForeground(UIColors.getTableFirstSectionFG());
                setBackground(UIColors.getTableFirstSectionBG());
            }
        };
        t.setDefaultRenderer(Object.class, gr);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JDialog reportDlg = new ZInstanceDialog(ZoeosFrame.getInstance(), getTarget().toString() + " Configuration", false);
                reportDlg.setContentPane(t);
                ZUtilities.applyHideButton(reportDlg, true, true);
            }
        });
    }

    public ZDialog generateVerificationDialog() {
        return null;
    }

    public Icon getIcon() {
        return null;
        //return new ImageIcon("toolbarButtonGraphics/general/information16.gif");
    }
}
