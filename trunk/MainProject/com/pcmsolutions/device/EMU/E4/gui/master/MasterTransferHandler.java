package com.pcmsolutions.device.EMU.E4.gui.master;

import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.PresetParameterTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.selections.MasterParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.PresetParameterSelection;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;


public class MasterTransferHandler extends TransferHandler implements Transferable {

    protected static final MasterTransferHandler INSTANCE = new MasterTransferHandler();

    public static class MasterDataFlavor extends DataFlavor {
        protected int category;
        protected Integer[] ids;

        public boolean containsId(Integer id) {
            if (id != null)
                for (int i = 0,j = ids.length; i < j; i++)
                    if (ids[i].equals(id))
                        return true;
            return false;
        }

        public boolean equals(DataFlavor that) {
            return ((that instanceof MasterDataFlavor) && ((MasterDataFlavor) that).getCategory() == category);
        }

        public int getCategory() {
            return category;
        }

        public void setCategory(int category) {
            this.category = category;
        }

        public Integer[] getIds() {
            return (Integer[]) ids.clone();
        }

        public void setIds(Integer[] ids) {
            this.ids = ids;
        }

        public MasterDataFlavor() {
            this(MasterParameterSelection.MASTER_GENERAL, new Integer[]{});
        }

        public MasterDataFlavor(int category, Integer[] ids) {
            super(MasterParameterSelection.class, "MasterParameterSelection");
            this.category = category;
            this.ids = ids;
        }
    }

    public static MasterDataFlavor masterFlavor = new MasterDataFlavor();

    protected MasterParameterSelection ms;

    private MasterTransferHandler() {
    }

    public static MasterTransferHandler getInstance() {
        return INSTANCE;
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        super.exportAsDrag(comp, e, action);
    }

    public boolean importData(JComponent comp, Transferable t) {
        if (comp instanceof MasterParameterSelectionAcceptor) {
            if (t.isDataFlavorSupported(masterFlavor)) {
                // do import!!
                try {
                    ((MasterParameterSelectionAcceptor) comp).setSelection((MasterParameterSelection) t.getTransferData(masterFlavor));
                    return true;
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (t.isDataFlavorSupported(PresetParameterTableTransferHandler.presetParameterFlavor)) {
                // do import!!
                try {
                    PresetParameterSelection pps = (PresetParameterSelection) t.getTransferData(PresetParameterTableTransferHandler.presetParameterFlavor);
                    ((MasterParameterSelectionAcceptor) comp).setSelection(new MasterParameterSelection(pps.getSrcDevice(), pps));
                    return true;
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        if (!(comp instanceof MasterParameterSelectionAcceptor))
            return false;
        DataFlavor cf = null;
        for (int i = 0, n = transferFlavors.length; i < n; i++)
            if (transferFlavors[i] instanceof MasterDataFlavor && ((MasterParameterSelectionAcceptor) comp).willAcceptCategory(((MasterDataFlavor) transferFlavors[i]).getCategory())) {
                cf = transferFlavors[i];
                break;
            } else if (transferFlavors[i] instanceof PresetParameterTableTransferHandler.PresetParameterDataFlavor && ((MasterParameterSelectionAcceptor) comp).willAcceptCategory(((PresetParameterTableTransferHandler.PresetParameterDataFlavor) transferFlavors[i]).getCategory())) {
                cf = transferFlavors[i];
                break;
            }

        if (cf != null) {
            if (comp instanceof DragAndDropTable) {
                ((MasterParameterTable) comp).setDropFeedbackActive(true);
                ((MasterParameterTable) comp).setChosenDropFlavor(cf);
            }
            return true;
        }

        return false;
    }

    public int getSourceActions(JComponent c) {
        if (c instanceof MasterParameterTable)
            return TransferHandler.COPY;
        return 0;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof MasterParameterTable) {
            ms = ((MasterParameterTable) c).getSelection();
            masterFlavor.setIds(ms.getIds());
            masterFlavor.setCategory(ms.getCategory());
            //if (c instanceof DragAndDropTable)
            //  ((DragAndDropTable) c).clearSelection();
            return this;
        }
        return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{masterFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(masterFlavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor))
            return ms;
        return null;
    }
}
