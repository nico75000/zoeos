package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.master.MasterTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.EditablePresetParameterTable;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.selections.MasterParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.PresetParameterSelection;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;


public class PresetParameterTableTransferHandler extends TransferHandler implements Transferable {

    public static class PresetParameterDataFlavor extends DataFlavor {
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
            return ((that instanceof PresetParameterDataFlavor) && ((PresetParameterDataFlavor) that).getCategory() == category);
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

        public PresetParameterDataFlavor() {
            this(MasterParameterSelection.MASTER_GENERAL, new Integer[]{});
        }

        public PresetParameterDataFlavor(int category, Integer[] ids) {
            super(PresetParameterSelection.class, "PresetParameterSelection");
            this.category = category;
            this.ids = ids;
        }
    }

    public static PresetParameterDataFlavor presetParameterFlavor = new PresetParameterDataFlavor();
    protected PresetParameterSelection pps;

    public PresetParameterTableTransferHandler() {
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        super.exportAsDrag(comp, e, action);
    }

    public boolean importData(JComponent comp, Transferable t) {
        if (comp instanceof EditablePresetParameterTable) {
            if (t.isDataFlavorSupported(presetParameterFlavor)) {
                try {
                    ((EditablePresetParameterTable) comp).setSelection((PresetParameterSelection) t.getTransferData(presetParameterFlavor));
                    return true;
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (t.isDataFlavorSupported(MasterTransferHandler.masterFlavor)) {
                // do import!!
                try {
                    ((EditablePresetParameterTable) comp).setSelection((MasterParameterSelection) t.getTransferData(MasterTransferHandler.masterFlavor));
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
        if (!(comp instanceof EditablePresetParameterTable))
            return false;
        DataFlavor cf = null;
        for (int i = 0, n = transferFlavors.length; i < n; i++)
            if (transferFlavors[i] instanceof PresetParameterDataFlavor && ((PresetParameterDataFlavor) transferFlavors[i]).getCategory() == PresetParameterSelection.convertPresetCategoryString(((EditablePresetParameterTable) comp).getCategory())) {
                cf = transferFlavors[i];
                break;
            } else {
                if (transferFlavors[i] instanceof MasterTransferHandler.MasterDataFlavor) {
                    MasterTransferHandler.MasterDataFlavor mpdf = ((MasterTransferHandler.MasterDataFlavor) transferFlavors[i]);
                    if ((mpdf.getCategory() == MasterParameterSelection.MASTER_FX_A && PresetParameterSelection.convertPresetCategoryString(((EditablePresetParameterTable) comp).getCategory()) == PresetParameterSelection.PRESET_FX_A) ||
                            (mpdf.getCategory() == MasterParameterSelection.MASTER_FX_B && PresetParameterSelection.convertPresetCategoryString(((EditablePresetParameterTable) comp).getCategory()) == PresetParameterSelection.PRESET_FX_B)) {
                        cf = transferFlavors[i];
                        break;
                    }
                }
            }

        if (cf != null) {
            ((DragAndDropTable) comp).setDropFeedbackActive(true);
            ((DragAndDropTable) comp).setChosenDropFlavor(cf);
            return true;
        }

        return false;
    }

    public int getSourceActions(JComponent c) {
        if (c instanceof PresetParameterTable)
            return TransferHandler.COPY;
        return 0;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof PresetParameterTable) {
            pps = ((PresetParameterTable) c).getSelection();
            presetParameterFlavor.setIds(pps.getIds());
            presetParameterFlavor.setCategory(pps.getCategory());
            if (c instanceof DragAndDropTable)
                ((DragAndDropTable) c).clearSelection();
            return this;
        }
        return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{presetParameterFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(presetParameterFlavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor))
            return pps;
        return null;
    }
}
