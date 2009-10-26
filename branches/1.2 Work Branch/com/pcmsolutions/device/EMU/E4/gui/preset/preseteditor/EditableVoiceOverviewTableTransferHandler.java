package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceOverviewTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.selections.ContextSampleSelection;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelectionCollection;
import com.pcmsolutions.device.EMU.E4.selections.VoiceSelection;
import com.pcmsolutions.device.EMU.E4.selections.ZoneSelection;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 19-May-2003
 * Time: 19:23:57
 * To change this template use Options | File Templates.
 */
public class EditableVoiceOverviewTableTransferHandler extends VoiceOverviewTableTransferHandler implements Transferable {
    public EditableVoiceOverviewTableTransferHandler() {
    }

    public boolean importData(JComponent comp, Transferable t) {
        if (comp instanceof EditableVoiceOverviewTable) {
            EditableVoiceOverviewTable evot = (EditableVoiceOverviewTable) comp;
            if (t.isDataFlavorSupported(SampleContextTransferHandler.sampleContextFlavor)) {
                int sr = ((EditableVoiceOverviewTable) comp).getSelectedRow();
                try {
                    Integer[] sampleIndexes = ((ContextSampleSelection) t.getTransferData(SampleContextTransferHandler.sampleContextFlavor)).getSampleIndexes();
                    int h = 0,i = 0,j = sampleIndexes.length;
                    EditableVoiceOverviewTableModel evotm = (EditableVoiceOverviewTableModel) evot.getModel();
                    while (i < j) {
                        if (!(sr + h < evot.getRowCount()))
                            break;
                        if (evotm.isRowMultisampleVoice(sr + h)) {
                            h++;
                            continue;
                        }
                        try {
                            Object robj = evotm.getValueAt(sr + h, 0);
                            if (robj instanceof ContextEditablePreset.EditableVoice)
                                ((ContextEditablePreset.EditableVoice) robj).setVoicesParam(ID.sample, sampleIndexes[i]);
                            else if (robj instanceof ContextEditablePreset.EditableVoice.EditableZone)
                                ((ContextEditablePreset.EditableVoice.EditableZone) robj).setZonesParam(ID.sample, sampleIndexes[i]);
                        } catch (NoSuchPresetException e) {
                            e.printStackTrace();
                        } catch (PresetEmptyException e) {
                            e.printStackTrace();
                        } catch (IllegalParameterIdException e) {
                            e.printStackTrace();
                        } catch (ParameterValueOutOfRangeException e) {
                            e.printStackTrace();
                        } catch (NoSuchVoiceException e) {
                            e.printStackTrace();
                        } catch (NoSuchZoneException e) {
                            e.printStackTrace();
                        }
                        h++;
                        i++;
                    }
                    return true;
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (t.isDataFlavorSupported(voiceParameterCollectionFlavor)) {
                try {
                    ((EditableVoiceOverviewTable) comp).setSelection((VoiceParameterSelectionCollection) t.getTransferData(voiceParameterCollectionFlavor));
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (t.isDataFlavorSupported(voiceFlavor) && comp instanceof EditableVoiceOverviewTable.VoiceSelectionAcceptor) {
                EditableVoiceOverviewTable.VoiceSelectionAcceptor vsa = (EditableVoiceOverviewTable.VoiceSelectionAcceptor) comp;
                try {
                    vsa.setSelection((VoiceSelection) t.getTransferData(voiceFlavor));
                    return true;
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (t.isDataFlavorSupported(zoneFlavor) && comp instanceof EditableVoiceOverviewTable.ZoneSelectionAcceptor) {
                EditableVoiceOverviewTable.ZoneSelectionAcceptor zsa = (EditableVoiceOverviewTable.ZoneSelectionAcceptor) comp;
                try {
                    zsa.setSelection((ZoneSelection) t.getTransferData(zoneFlavor));
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
        if (comp instanceof EditableVoiceOverviewTable) {
            for (int i = 0, n = transferFlavors.length; i < n; i++) {
                if (transferFlavors[i].equals(SampleContextTransferHandler.sampleContextFlavor)) {
                    cf = SampleContextTransferHandler.sampleContextFlavor;
                    break;
                }
                if (transferFlavors[i].equals(voiceParameterCollectionFlavor)) {
                    cf = voiceParameterCollectionFlavor;
                    break;
                }
            }
        } else {
            if (comp instanceof EditableVoiceOverviewTable.VoiceSelectionAcceptor) {
                for (int i = 0, n = transferFlavors.length; i < n; i++) {
                    if (transferFlavors[i].equals(voiceFlavor)) {
                        cf = voiceFlavor;
                        break;
                    }
                }
            }
            if (cf == null && comp instanceof EditableVoiceOverviewTable.ZoneSelectionAcceptor) {
                for (int i = 0, n = transferFlavors.length; i < n; i++) {
                    if (transferFlavors[i].equals(zoneFlavor)) {
                        cf = zoneFlavor;
                        break;
                    }
                }
            }
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
}
