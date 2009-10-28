package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.E4.gui.GeneralTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.packaging.PackageHeader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Jan-2004
 * Time: 08:44:30
 * To change this template use Options | File Templates.
 */
class PackageHeaderInfoTable extends JTable {
    private PackageHeader header;
    private final DefaultTableModel dtm = new DefaultTableModel();
    private Vector data = new Vector();
    private final Vector columnNames = new Vector();

    {
        columnNames.add("");
        columnNames.add("");
        GeneralTableCellRenderer gr = new GeneralTableCellRenderer() {
            {
                setForeground(UIColors.getTableFirstSectionFG());
                setBackground(UIColors.getTableFirstSectionHeaderBG());
            }
        };
        setDefaultRenderer(Object.class, gr);
        setEnabled(false);
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public PackageHeaderInfoTable(PackageHeader hdr) {
        this.setModel(dtm);
        setHeader(hdr);
        setPreferredSize(new Dimension(300, 250));
        final TableCellRenderer tcr = this.getDefaultRenderer(Object.class);
        this.setDefaultRenderer(Object.class, new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = tcr.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JComponent)
                    ((JComponent) c).setToolTipText(getValueAt(row, column).toString());
                return c;
            }
        });
    }

    public PackageHeader getHeader() {
        return header;
    }

    public final void setHeader(PackageHeader header) {
        this.header = header;
        data = getDataVector();
        dtm.setDataVector(data, columnNames);
    }

    protected static final int DESC_FIELD_LEN = 18;

    private static final String NAME = "Name";
    private static final String NOTES = "Notes";
    private static final String CREATION_DATE = "Creation date";
    private static final String DEVICE_NAME = "Device name";
    private static final String DEVICE_VERSION = "Device version";
    private static final String ZOEOS_VERSION = "ZoeOS version";

    protected Vector getDataVector() {
        Vector data = new Vector();
        Vector row;
        if (header != null) {
            row = new Vector();
            row.add(NAME);
            row.add(header.getName());
            data.add(row);

            row = new Vector();
            row.add(NOTES);
            row.add(header.getNotes());
            data.add(row);

            row = new Vector();
            row.add(CREATION_DATE);
            row.add(header.getCreationDate());
            data.add(row);

            row = new Vector();
            row.add(DEVICE_NAME);
            row.add(header.getDeviceName());
            data.add(row);

            row = new Vector();
            row.add(DEVICE_VERSION);
            row.add(new Double(header.getDeviceVersion()));
            data.add(row);

            row = new Vector();
            row.add(ZOEOS_VERSION);
            row.add(new Double(header.getZoeosVersion()));
            data.add(row);
        }
        return data;
    }
}
