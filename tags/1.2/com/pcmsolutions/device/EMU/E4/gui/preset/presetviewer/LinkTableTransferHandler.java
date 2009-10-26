package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.selections.LinkParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.LinkParameterSelectionCollection;
import com.pcmsolutions.device.EMU.E4.selections.LinkSelection;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 19-May-2003
 * Time: 19:23:57
 * To change this template use Options | File Templates.
 */
public class LinkTableTransferHandler extends TransferHandler implements Transferable {
    public static final DataFlavor linkFlavor = new DataFlavor(LinkSelection.class, "LinkSelection");
    protected static LinkSelection ls;

    public static final LinkParameterCollectionDataFlavor linkParameterCollectionFlavor = new LinkParameterCollectionDataFlavor();
    protected static LinkParameterSelectionCollection lpsc;


    public static class LinkParameterCollectionDataFlavor extends DataFlavor {
        protected int category;
        protected Integer[] ids;
        protected int count;

        public boolean containsId(Integer id) {
            if (id != null)
                for (int i = 0,j = ids.length; i < j; i++)
                    if (ids[i].equals(id))
                        return true;
            return false;
        }

        public int getCount() {
            return count;
        }

        protected void setCount(int count) {
            this.count = count;
        }

        public boolean equals(DataFlavor that) {
            return ((that instanceof LinkTableTransferHandler.LinkParameterCollectionDataFlavor) && ((LinkTableTransferHandler.LinkParameterCollectionDataFlavor) that).getCategory() == category);
        }

        public int getCategory() {
            return category;
        }

        protected void setCategory(int category) {
            this.category = category;
        }

        public Integer[] getIds() {
            return (Integer[]) ids.clone();
        }

        protected void setIds(Integer[] ids) {
            this.ids = ids;
        }

        public LinkParameterCollectionDataFlavor() {
            this(LinkParameterSelection.LINK_GENERAL, new Integer[]{}, 0);
        }

        public LinkParameterCollectionDataFlavor(int category, Integer[] ids, int count) {
            super(LinkParameterSelectionCollection.class, "LinkParameterSelectionCollection");
            this.category = category;
            this.ids = ids;
            this.count = count;
        }
    }


    protected DataFlavor activeFlavor;
    protected Object activeSelection;

    public LinkTableTransferHandler() {
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        super.exportAsDrag(comp, e, action);
    }

    public boolean importData(JComponent comp, Transferable t) {
        return false;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        return false;
    }

    public int getSourceActions(JComponent c) {
        if (c instanceof LinkTable || c instanceof LinkTable.LinkSelectionProvider)
            return TransferHandler.COPY;
        return 0;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof LinkTable.LinkSelectionProvider) {
            activeFlavor = linkFlavor;
            activeSelection = ls = ((LinkTable.LinkSelectionProvider) c).getSelection();
            if (c instanceof DragAndDropTable)
                ((DragAndDropTable) c).clearSelection();
            return this;
        }
        if (c instanceof LinkTable) {
            activeSelection = lpsc = ((LinkTable) c).getSelection();
            activeFlavor = linkParameterCollectionFlavor;
            linkParameterCollectionFlavor.setCategory(lpsc.getCategory());
            linkParameterCollectionFlavor.setIds(lpsc.getIds());
            linkParameterCollectionFlavor.setCount(lpsc.getCount());
            //if (c instanceof DragAndDropTable)
            //  ((DragAndDropTable) c).clearSelection();
            return this;
        }
        return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{activeFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(activeFlavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor))
            return activeSelection;
        return null;
    }
}
