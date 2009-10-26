package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 08:51:55
 * To change this template use Options | File Templates.
 */
abstract public class AbstractRowHeaderedAndSectionedTableModel extends AbstractTableModel implements ColumnAndSectionDataProvider, ZDisposable {
    protected List tableRowObjects = new LinkedList();
    protected static int DEF_COL_WIDTH = 45;

    protected ColumnData[] columnData;
    protected ColumnData rowHeaderColumnData;
    protected SectionData[] sectionData;
    protected Vector csdListeners = new Vector();

    public AbstractRowHeaderedAndSectionedTableModel() {
    }

    public AbstractRowHeaderedAndSectionedTableModel init() {
        buildColumnAndSectionData();
        refresh(true);
        return this;
    }

    abstract protected void buildColumnAndSectionData();

    public void addColumnAndSectionDataListener(ColumnAndSectionDataListener cdsl) {
        csdListeners.add(cdsl);
    }

    public void removeColumnAndSectionDataListener(ColumnAndSectionDataListener cdsl) {
        csdListeners.remove(cdsl);
    }

    protected void fireColumnAndSectionDataChanged(Object source) {
        for (int i = 0,j = csdListeners.size(); i < j; i++)
            try {
                ((ColumnAndSectionDataListener) csdListeners.get(i)).columnAndSectionDataChanged(source);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    protected void clearRows() {
        ZUtilities.zDisposeCollection(tableRowObjects);
        tableRowObjects.clear();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public final void refresh(final boolean structural) {
        doPreRefresh();
        clearRows();
        doRefresh();
        doPostRefresh();
        //SwingUtilities.invokeLater(new Runnable() {
        // public void run() {
        if (structural) {
            fireTableStructureChanged();
        } else
            fireTableDataChanged();
        //   }
        // });
    }

    abstract protected void doRefresh();

    abstract protected void doPreRefresh();

    abstract protected void doPostRefresh();

    public int getRowCount() {
        if (tableRowObjects != null)
            return tableRowObjects.size();
        else
            return 0;
    }

    public final int getColumnCount() {
        return columnData.length + 1;       // +1 for row header
    }

    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (rowIndex < tableRowObjects.size()) {
            Object o = tableRowObjects.get(rowIndex);
            if (o instanceof ColumnValueProvider)
                try {
                    return ((ColumnValueProvider) o).getValueAt(columnIndex);
                } catch (Exception e) {
                }
        }
        return "";
    }

    protected interface ColumnValueProvider extends ZDisposable {
        public Object getValueAt(int col);
    }

    protected interface ComparableColumnValueProvider extends Comparable {
    }

    public void zDispose() {
        clearRows();
        csdListeners.clear();
        csdListeners = null;
        tableRowObjects = null;
        for (int i = 0; i < columnData.length; i++)
            columnData[i].zDispose();
        columnData = null;
        rowHeaderColumnData = null;
        sectionData = null;
    }

    public String getColumnName(int column) {
        if (column == 0)
            return rowHeaderColumnData.title;
        else
            return columnData[column - 1].title;
    }

    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return rowHeaderColumnData.columnClass;
        else
            return columnData[columnIndex - 1].columnClass;
    }

    public ColumnData[] getColumnData() {
        return columnData;
    }

    public SectionData[] getSectionData() {
        return sectionData;
    }

    public ColumnData getRowHeaderColumnData() {
        return rowHeaderColumnData;
    }
}
