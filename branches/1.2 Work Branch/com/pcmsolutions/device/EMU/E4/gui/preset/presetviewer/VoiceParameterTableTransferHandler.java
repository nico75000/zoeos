package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.VoiceParameterSelectionAcceptor;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.selections.CordParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.DataFlavorGrid;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;


public class VoiceParameterTableTransferHandler extends TransferHandler implements Transferable {
    protected static VoiceParameterTableTransferHandler INSTANCE = new VoiceParameterTableTransferHandler();

    public static VoiceParameterTableTransferHandler getInstance() {
        return INSTANCE;
    }


    public static class VoiceParameterDataFlavor extends DataFlavor {
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
            return ((that instanceof VoiceParameterDataFlavor) && ((VoiceParameterDataFlavor) that).getCategory() == category);
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

        public VoiceParameterDataFlavor() {
            this(VoiceParameterSelection.VOICE_GENERAL, new Integer[]{});
        }

        public VoiceParameterDataFlavor(int category, Integer[] ids) {
            super(VoiceParameterSelection.class, "VoiceParameterSelection");
            this.category = category;
            this.ids = ids;
        }
    }

    protected static VoiceParameterDataFlavor voiceParameterFlavor = new VoiceParameterDataFlavor();
    protected static DataFlavorGrid cordParameterFlavor = new DataFlavorGrid(CordParameterSelection.class, "CordParameterSelection");
    protected static DataFlavor activeDataFlavor;

    protected VoiceParameterSelection vps;

    private VoiceParameterTableTransferHandler() {
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        super.exportAsDrag(comp, e, action);
    }

    public boolean importData(JComponent comp, Transferable t) {
        if (comp instanceof VoiceParameterSelectionAcceptor) {
            if (t.isDataFlavorSupported(voiceParameterFlavor)) {
                // do import!!
                try {
                    ((VoiceParameterSelectionAcceptor) comp).setSelection((VoiceParameterSelection) t.getTransferData(voiceParameterFlavor));
                    return true;
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (t.isDataFlavorSupported(cordParameterFlavor)) {
                // do import!!
                try {
                    ((VoiceParameterSelectionAcceptor) comp).setSelection((CordParameterSelection) t.getTransferData(cordParameterFlavor));
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
        if (!(comp instanceof VoiceParameterSelectionAcceptor))
            return false;
        DataFlavor cf = null;
        for (int i = 0, n = transferFlavors.length; i < n; i++)
            if (transferFlavors[i] instanceof VoiceParameterDataFlavor && ((VoiceParameterSelectionAcceptor) comp).willAcceptCategory(((VoiceParameterDataFlavor) transferFlavors[i]).getCategory())
                    || transferFlavors[i].equals(cordParameterFlavor) && ((VoiceParameterSelectionAcceptor) comp).willAcceptCategory(VoiceParameterSelection.VOICE_CORDS)
            ) {
                cf = transferFlavors[i];
                break;
            }
        if (cf != null) {
            if (comp instanceof DragAndDropTable) {
                ((DragAndDropTable) comp).setDropFeedbackActive(true);
                ((DragAndDropTable) comp).setChosenDropFlavor(cf);
            }
            return true;
        }

        return false;
    }

    public int getSourceActions(JComponent c) {
        if (c instanceof DragAndDropTable && c instanceof VoiceParameterSelectionProvider)
            return TransferHandler.COPY;
        return 0;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof VoiceParameterSelectionProvider) {
            vps = ((VoiceParameterSelectionProvider) c).getSelection();
            if (vps instanceof CordParameterSelection) {
                int[] selCols = ((CordParameterSelection) vps).getSelCols();
                int[] selRows = ((CordParameterSelection) vps).getSelRows();
                cordParameterFlavor.clearGrid();
                cordParameterFlavor.setDefCols(selCols);
                for (int i = 0,j = selRows.length; i < j; i++)
                    cordParameterFlavor.addRow(selRows[i]);
                activeDataFlavor = cordParameterFlavor;

            } else {
                voiceParameterFlavor.setIds(vps.getIds());
                voiceParameterFlavor.setCategory(vps.getCategory());
                activeDataFlavor = voiceParameterFlavor;
            }
            //if (c instanceof DragAndDropTable)
            //  ((DragAndDropTable) c).clearSelection();
            return this;
        }
        return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{activeDataFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(activeDataFlavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor))
            return vps;
        return null;
    }
}
