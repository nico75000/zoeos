package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.system.IntPool;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Aug-2003
 * Time: 14:51:49
 * To change this template use Options | File Templates.
 */
public class DataFlavorGrid extends DataFlavor {
    protected final TreeMap grid = new TreeMap();
    protected int lowRow = Integer.MAX_VALUE;
    protected int lowCol = Integer.MAX_VALUE;
    protected int[] defCols;
    protected final List normalizedRows = new ArrayList();

    public void setDefCols(int[] defCols) {
        this.defCols = defCols;
    }

    // returns thew actual index of the first row in source
    public int getLowRow() {
        return lowRow;
    }

    public void clearGrid() {
        grid.clear();
        normalizedRows.clear();
        lowRow = Integer.MAX_VALUE;
        lowCol = Integer.MAX_VALUE;
    }

    /*public void addCell(int row, int col) {
        addRow(row, new int[]{col});
    } */

    // row is 0 indexed
    public void addRow(int row) {
        if (defCols == null)
            throw new IllegalArgumentException("no default columns set for DataFlavorGrid");
        addRow(row, defCols);
    }

    // row is 0 indexed
    public void addRow(int row, int[] cols) {
        if (row < lowRow)
            lowRow = row;

        List l = (List) grid.get(IntPool.get(row));
        if (l == null) {
            l = new ArrayList();
            grid.put(IntPool.get(row), l);
        }
        normalizedRows.add(IntPool.get(row));

        for (int i = 0,j = cols.length; i < j; i++) {
            l.add(IntPool.get(cols[i]));

            if (cols[i] < lowRow)
                lowCol = cols[i];
        }
    }

    public int numRows() {
        return grid.size();
    }

    /*protected int getNormalizedRow(int row) {
        return normalizedRows.indexOf(IntPool.get(row));
    } */

    // row relative to first entered row index
    public boolean isRowPresent(int row) {
        return grid.containsKey(IntPool.get(row));
    }

    public int[] getRowOffsets() {
        int[] roffs = new int[grid.size()];
        int index = 0;
        for (Iterator i = grid.keySet().iterator(); i.hasNext();)
            roffs[index++] = ((Integer) i.next()).intValue() - lowRow;

        return roffs;
    }

    // row is 0 indexed ( typically used for in-place order)
    public boolean isCellPresent(int row, int col) {
        //if ( row>=0 && )
        List l = (List) grid.get(IntPool.get(row + lowRow));

        if (l != null && l.contains(IntPool.get(col)))
            return true;

        return false;
    }

    // row is 0 indexed and normalized
    public boolean isRowNormalizedCellPresent(int row, int col) {
        if (row >= 0 && row < normalizedRows.size()) {
            List l = (List) grid.get(normalizedRows.get(row));

            if (l != null && l.contains(IntPool.get(col)))
                return true;
        }
        return false;
    }

    // row is 0 indexed and normalized
    public boolean isRowNormalizedAndColumnOffsettedCellPresent(int row, int col) {
        if (row >= 0 && row < normalizedRows.size()) {
            List l = (List) grid.get(normalizedRows.get(row));

            if (l != null && l.contains(IntPool.get(col - lowCol)))
                return true;
        }
        return false;
    }

    public DataFlavorGrid() {
    }

    public DataFlavorGrid(String mimeType) throws ClassNotFoundException {
        super(mimeType);
    }

    public DataFlavorGrid(String mimeType, String humanPresentableName) {
        super(mimeType, humanPresentableName);
    }

    public DataFlavorGrid(String mimeType, String humanPresentableName, ClassLoader classLoader) throws ClassNotFoundException {
        super(mimeType, humanPresentableName, classLoader);
    }

    public DataFlavorGrid(Class representationClass, String humanPresentableName) {
        super(representationClass, humanPresentableName);
    }
}
