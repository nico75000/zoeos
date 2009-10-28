package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.device.EMU.E4.selections.DataFlavorGrid;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.TooManyListenersException;

abstract public class DragAndDropTable extends EditingTable implements DropTargetListener, DropCellCheckerTable {
    protected int dropRow = Integer.MIN_VALUE;
    protected int dropCol = Integer.MIN_VALUE;
    protected DropChecker dropChecker;
    protected boolean inPlaceGridDrop = false;
    protected boolean usingGridDrop = true;
    protected int dropOverExtent = 0;
    protected int dropOverExtentDisplacement = 0;
    protected boolean dropFeedbackActive = false;
    protected DataFlavor chosenDropFlavor;

    public final DragAndDropTable.DropChecker defaultDropGridChecker = new DragAndDropTable.DropChecker() {
        public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {
            if (chosenDropFlavor == null)
                return false;
            if (chosenDropFlavor instanceof DataFlavorGrid) {
                if (row >= dropRow)
                    if (DragAndDropTable.this.isInPlaceGridDrop())
                        return ((DataFlavorGrid) chosenDropFlavor).isCellPresent(row - dropRow, col);
                    else
                        return ((DataFlavorGrid) chosenDropFlavor).isRowNormalizedCellPresent(row - dropRow, col);
            } else if (row == dropRow)
                return true;
            return false;
        }
    };

    public boolean isDropFeedbackActive() {
        return dropFeedbackActive;
    }

    public void setDropFeedbackActive(boolean dropFeedbackActive) {
        this.dropFeedbackActive = dropFeedbackActive;
        //updateCellsInDropExtent(0);
    }

    public DataFlavor getChosenDropFlavor() {
        return chosenDropFlavor;
    }

    public void setChosenDropFlavor(DataFlavor chosenDropFlavor) {
        this.chosenDropFlavor = chosenDropFlavor;
        if (chosenDropFlavor instanceof DataFlavorGrid)
            usingGridDrop = true;
        else
            usingGridDrop = false;
    }

    public interface DropChecker {
        public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value);
    }

    public DropChecker getDropChecker() {
        return dropChecker;
    }

    public void setDropChecker(DropChecker dropChecker) {
        this.dropChecker = dropChecker;
    }

    public boolean isUsingGridDrop() {
        return usingGridDrop;
    }

    public boolean isInPlaceGridDrop() {
        return inPlaceGridDrop;
    }

    public void setInPlaceGridDrop(boolean inPlaceGridDrop) {
        this.inPlaceGridDrop = inPlaceGridDrop;
    }

    public void setTransferHandler(TransferHandler newHandler) {
        if (getDropTarget() != null)
            getDropTarget().removeDropTargetListener(this);
        super.setTransferHandler(newHandler);
        if (newHandler != null)
            try {
                getDropTarget().addDropTargetListener(this);
            } catch (TooManyListenersException e) {
                e.printStackTrace();
            }
    }

    public DragAndDropTable(String popupName, Color popupBG, Color popupFG) {
        super(popupName, popupBG, popupFG);
    }

    public DragAndDropTable(TransferHandler t, String popupName, Color popupBG, Color popupFG) {
        super(popupName, popupBG, popupFG);
        setDragEnabled(true);
        setTransferHandler(t);
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        if (dropFeedbackActive) {
            updateCellsInDropExtent(dropRow);
            //if (getTransferHandler() != null) {
            dropRow = this.rowAtPoint(dtde.getLocation());
            dropCol = this.columnAtPoint(dtde.getLocation());
            setupDropOverExtent();
            updateCellsInDropExtent(dropRow);
            //}
        }
    }

    protected void setupDropOverExtent() {
        dropOverExtent = 0;
        if (chosenDropFlavor instanceof DataFlavorGrid && usingGridDrop)
            if (inPlaceGridDrop) {
                int[] offsets = ((DataFlavorGrid) chosenDropFlavor).getRowOffsets();
                dropOverExtent = 0;
                for (int k = 0,l = offsets.length; k < l; k++)
                    if (offsets[k] > dropOverExtent)
                        dropOverExtent = offsets[k];
            } else {
                dropOverExtent = ((DataFlavorGrid) chosenDropFlavor).numRows();
            }
    }

    public void dragOver(DropTargetDragEvent dtde) {
        if (dropFeedbackActive) {
            int ndr = this.rowAtPoint(dtde.getLocation());
            if (ndr != dropRow) {
                updateCellsInDropExtent(dropRow);
                dropRow = ndr;
                updateCellsInDropExtent(dropRow);
            }
            dropCol = this.columnAtPoint(dtde.getLocation());
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
        if (dropFeedbackActive) {
            int oldDropRow = dropRow;
            dropCol = Integer.MIN_VALUE;
            dropRow = Integer.MIN_VALUE;
            updateCellsInDropExtent(oldDropRow);
            dropOverExtent = 0;
            dropOverExtentDisplacement = 0;
            dropFeedbackActive = false;
            chosenDropFlavor = null;
        }
    }

    protected void updateCellsInDropExtent(int row) {
        if (row >= 0)
            if (dropOverExtent == -1)
            // updateTitleData whole table
                this.tableChanged(new TableModelEvent(getModel()));
            else
            // updateTitleData in drop extent
                this.tableChanged(new TableModelEvent(getModel(), row, row + dropOverExtent + dropOverExtentDisplacement));
    }

    public void drop(DropTargetDropEvent dtde) {
        if (dropFeedbackActive) {
            int oldDropRow = dropRow;
            dropRow = Integer.MIN_VALUE;
            dropCol = Integer.MIN_VALUE;
            updateCellsInDropExtent(oldDropRow);
            dropOverExtent = 0;
            dropOverExtentDisplacement = 0;
            dropFeedbackActive = false;
            chosenDropFlavor = null;
        }
    }

    public boolean isCellDropTarget(int row, int col, Object value) {
        if (dropFeedbackActive) {
            if (dropChecker != null)
                return dropChecker.isCellDropTarget(dropRow, dropCol, row, col, value);
        }
        return false;
    }

    public void zDispose() {
        super.zDispose();
        setDropTarget(null);
        dropChecker = null;
    }
}
