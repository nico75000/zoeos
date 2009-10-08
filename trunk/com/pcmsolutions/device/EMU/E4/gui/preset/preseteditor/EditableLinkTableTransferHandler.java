package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext.PresetContextTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.LinkTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.selections.ContextPresetSelection;
import com.pcmsolutions.device.EMU.E4.selections.LinkParameterSelectionCollection;
import com.pcmsolutions.device.EMU.E4.selections.LinkSelection;
import com.pcmsolutions.system.IntPool;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


public class EditableLinkTableTransferHandler extends LinkTableTransferHandler implements Transferable {

    public EditableLinkTableTransferHandler() {
    }

    public boolean importData(JComponent comp, Transferable t) {
        if (comp instanceof EditableLinkTable) {
            EditableLinkTable elt;
            elt = (EditableLinkTable) comp;

            if (t.isDataFlavorSupported(PresetContextTransferHandler.presetContextFlavor)) {
                int sr = ((EditableLinkTable) comp).getSelectedRow();
                try {
                    ReadablePreset[] readablePresets = ((ContextPresetSelection) t.getTransferData(PresetContextTransferHandler.presetContextFlavor)).getReadablePresets();
                    for (int i = 0,j = readablePresets.length; i < j; i++) {
                        if (sr + i >= elt.getRowCount())
                            break;
                        try {
                            Object link = elt.getModel().getValueAt(sr + i, 0);
                            if (link instanceof ContextEditablePreset.EditableLink)
                                ((ContextEditablePreset.EditableLink) link).setLinksParam(IntPool.get(23), readablePresets[i].getPresetNumber());
                        } catch (NoSuchPresetException e) {
                            e.printStackTrace();
                        } catch (PresetEmptyException e) {
                            e.printStackTrace();
                        } catch (IllegalParameterIdException e) {
                            e.printStackTrace();
                        } catch (NoSuchLinkException e) {
                            e.printStackTrace();
                        } catch (ParameterValueOutOfRangeException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (t.isDataFlavorSupported(linkParameterCollectionFlavor)) {
                try {
                    ((EditableLinkTable) comp).setSelection((LinkParameterSelectionCollection) t.getTransferData(linkParameterCollectionFlavor));
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (comp instanceof EditableLinkTable.LinkSelectionAcceptor) {
            if (t.isDataFlavorSupported(linkFlavor)) {
                try {
                    LinkSelection ils = (LinkSelection) t.getTransferData(linkFlavor);
                    ((EditableLinkTable.LinkSelectionAcceptor) comp).setSelection(ils);
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
        DataFlavor cf = null;
        if (comp instanceof EditableLinkTable) {
            for (int i = 0, n = transferFlavors.length; i < n; i++) {
                if (transferFlavors[i].equals(PresetContextTransferHandler.presetContextFlavor)) {
                    cf = PresetContextTransferHandler.presetContextFlavor;
                    break;
                }
                if (transferFlavors[i].equals(linkParameterCollectionFlavor)) {
                    cf = linkParameterCollectionFlavor;
                    break;
                }
            }
        } else if (cf == null && comp instanceof EditableLinkTable.LinkSelectionAcceptor) {
            for (int i = 0, n = transferFlavors.length; i < n; i++)
                if (transferFlavors[i].equals(linkFlavor)) {
                    cf = linkFlavor;
                    break;
                }
        }
        boolean rv = false;
        if (cf != null) {
            rv = true;
            if (comp instanceof DragAndDropTable) {
                ((DragAndDropTable) comp).setDropFeedbackActive(true);
                ((DragAndDropTable) comp).setChosenDropFlavor(cf);
                return true;
            }
        }
        return rv;
    }
}
