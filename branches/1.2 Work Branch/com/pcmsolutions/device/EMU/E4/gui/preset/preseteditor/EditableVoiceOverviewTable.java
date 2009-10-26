package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceOverviewTable;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceOverviewTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.selections.*;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 08-Jun-2003
 * Time: 21:51:27
 * To change this template use Options | File Templates.
 */
public class EditableVoiceOverviewTable extends VoiceOverviewTable {
    //protected ContextEditablePreset preset;

    protected static EditableVoiceOverviewTableTransferHandler editableVoiceOverviewTableTransferHandler = new EditableVoiceOverviewTableTransferHandler();

    public EditableVoiceOverviewTable(ContextEditablePreset p) throws ZDeviceNotRunningException {
        super(new EditableVoiceOverviewTableModel(p, p.getDeviceContext().getDeviceParameterContext()));
        this.preset = p;
        this.setTransferHandler(editableVoiceOverviewTableTransferHandler);
        setDropChecker(new DragAndDropTable.DropChecker() {
            public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {
                if (chosenDropFlavor instanceof DataFlavorGrid) {
                    if (row >= dropRow)
                    // check it's not a multi-sample voice
                        if (!((EditableVoiceOverviewTableModel) EditableVoiceOverviewTable.this.getModel()).isRowMultisampleVoice(row)) {
                            // adjust row to skip multisample voices
                            row -= getNumberOfMultisampleVoicesInRange(dropRow, row);
                            return ((DataFlavorGrid) chosenDropFlavor).isRowNormalizedAndColumnOffsettedCellPresent(row - dropRow, col - 1);
                        }
                } else if (chosenDropFlavor instanceof VoiceOverviewTableTransferHandler.VoiceParameterCollectionDataFlavor) {
                    VoiceOverviewTableTransferHandler.VoiceParameterCollectionDataFlavor cf = (VoiceOverviewTableTransferHandler.VoiceParameterCollectionDataFlavor) chosenDropFlavor;
                    if (row >= dropRow && row < dropRow + cf.getCount())
                        if (value instanceof ReadableParameterModel && cf.containsId(((ReadableParameterModel) value).getParameterDescriptor().getId()))
                            return true;
                }
                return false;
            }
        });
        ParameterModelUtilities.registerTableForEditableParameterModelShortcuts(this);
    }

    protected void setupDropOverExtent() {
        if (chosenDropFlavor instanceof VoiceOverviewTableTransferHandler.VoiceParameterCollectionDataFlavor)
            dropOverExtent = ((VoiceOverviewTableTransferHandler.VoiceParameterCollectionDataFlavor) chosenDropFlavor).getCount();
        else
            super.setupDropOverExtent();
    }

    protected void updateCellsInDropExtent(int row) {
        if (chosenDropFlavor instanceof DataFlavorGrid)
            dropOverExtentDisplacement = getNumberOfMultisampleVoicesInRange(row, row + dropOverExtent);

        super.updateCellsInDropExtent(row);
    }

    protected int getNumberOfMultisampleVoicesInRange(int dropRow, int row) {
        int count = 0;
        for (int k = dropRow,l = row; k <= l; k++)
            if (((EditableVoiceOverviewTableModel) EditableVoiceOverviewTable.this.getModel()).isRowMultisampleVoice(k))
                count++;

        return count;
    }

    public void setInPlaceGridDrop(boolean inPlaceGridDrop) {
        super.setInPlaceGridDrop(false);
    }

    public void setSelection(VoiceParameterSelectionCollection vpsc) {
        int row = getSelectedRow();
        Object val;
        VoiceParameterSelection[] sels = vpsc.getSelections();
        for (int i = 0,j = sels.length; i < j; i++) {
            if (row + i < getRowCount()) {
                val = getModel().getValueAt(row + i, 0);
                if (val instanceof ContextEditablePreset.EditableVoice)
                    sels[i].render(new ContextEditablePreset.EditableVoice[]{(ContextEditablePreset.EditableVoice) val});
                else if (val instanceof ContextEditablePreset.EditableVoice.EditableZone) {
                    sels[i].render(new ContextEditablePreset.EditableVoice.EditableZone[]{(ContextEditablePreset.EditableVoice.EditableZone) val});
                }
            } else
                break;
        }
    }

    public interface VoiceSelectionAcceptor {
        public void setSelection(VoiceSelection vs);
    }

    public interface ZoneSelectionAcceptor {
        public void setSelection(ZoneSelection zs);
    }

    public boolean editCellAt(int row, int column, EventObject e) {
        if (ParameterModelTableCellEditor.testCellEditable(e) && ParameterModelTableCellEditor.tryToggleCellAt(this, row, column))
            return false;
        return super.editCellAt(row, column, e);
    }

    protected DragAndDropTable generateRowHeaderTable() {
        EditableVoiceOverviewTable.EditableVoiceOverviewTableRowHeader t = new EditableVoiceOverviewTable.EditableVoiceOverviewTableRowHeader(editableVoiceOverviewTableTransferHandler, popupName, null, null) {
            public void zDispose() {
            }

            protected JMenuItem[] getCustomMenuItems() {
                return customRowHeaderMenuItems;
            }

            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (!e.isConsumed() && e.getClickCount() == 2)
                    this.clearSelection();
            }
        };
        return t;
    }

    protected class EditableVoiceOverviewTableRowHeader extends VoiceOverviewTable.VoiceOverviewTableRowHeader implements EditableVoiceOverviewTable.VoiceSelectionAcceptor, EditableVoiceOverviewTable.ZoneSelectionAcceptor {
        {
            setDropChecker(new DropChecker() {
                public boolean isCellDropTarget(int dropRow, int dropCol, int row, int col, Object value) {
                    if (chosenDropFlavor.equals(VoiceOverviewTableTransferHandler.zoneFlavor)) {
                        Object dropValueAt = getValueAt(dropRow, 0);
                        Integer dropVoice;
                        if (dropValueAt instanceof ReadablePreset.ReadableVoice)
                            dropVoice = ((ReadablePreset.ReadableVoice) dropValueAt).getVoiceNumber();
                        else
                            dropVoice = ((ReadablePreset.ReadableVoice.ReadableZone) dropValueAt).getVoiceNumber();

                        Object rowValueAt = getValueAt(row, 0);
                        Integer rowVoice;
                        if (rowValueAt instanceof ReadablePreset.ReadableVoice)
                            rowVoice = ((ReadablePreset.ReadableVoice) rowValueAt).getVoiceNumber();
                        else
                            rowVoice = ((ReadablePreset.ReadableVoice.ReadableZone) rowValueAt).getVoiceNumber();

                        return dropVoice.equals(rowVoice);
                    }
                    return true;
                }
            });
        }

        protected void setupDropOverExtent() {
            this.dropOverExtent = -1;
        }

        public EditableVoiceOverviewTableRowHeader(String popupName, Color popupBG, Color popupFG) {
            super(popupName, popupBG, popupFG);
        }

        public EditableVoiceOverviewTableRowHeader(TransferHandler t, String popupName, Color popupBG, Color popupFG) {
            super(t, popupName, popupBG, popupFG);
        }

        public void zDispose() {
        }

        public void setSelection(VoiceSelection vs) {
            IsolatedPreset.IsolatedVoice[] voices;
            voices = vs.getIsolatedVoices();
            for (int i = 0,j = voices.length; i < j; i++) {
                if (voices[i] != null)
                    try {
                        ((ContextEditablePreset)preset).newVoice(voices[i]);
                    } catch (NoSuchPresetException e) {
                        e.printStackTrace();
                    } catch (TooManyZonesException e) {
                        e.printStackTrace();
                    } catch (TooManyVoicesException e) {
                        e.printStackTrace();
                    } catch (PresetEmptyException e) {
                        e.printStackTrace();
                    }
            }
        }

        public void setSelection(ZoneSelection zs) {
            Integer voice = null;
            int selRow = getSelectedRow();
            Object valueAt = getValueAt(selRow, 0);
            if (valueAt instanceof ReadablePreset.ReadableVoice)
                voice = ((ReadablePreset.ReadableVoice) valueAt).getVoiceNumber();
            else
                voice = ((ReadablePreset.ReadableVoice.ReadableZone) valueAt).getVoiceNumber();

            IsolatedPreset.IsolatedVoice.IsolatedZone[] zones;
            zones = zs.getIsolatedZones();
            for (int i = 0,j = zones.length; i < j; i++) {
                if (zones[i] != null)
                    try {
                        ((ContextEditablePreset) preset).newZone(voice, zones[i]);
                    } catch (NoSuchPresetException e) {
                        e.printStackTrace();
                    } catch (TooManyZonesException e) {
                        e.printStackTrace();
                    } catch (PresetEmptyException e) {
                        e.printStackTrace();
                    } catch (NoSuchVoiceException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    protected ReadablePreset convertPassThroughPreset(ReadablePreset preset) {
        return preset;
    }

    protected ReadableSample convertPassThroughSample(ReadableSample sample) {
        return sample;
    }
    /*protected JMenuItem[] getCustomMenuItems() {
       JMenuItem smi = null;
       Object[] selObjs = this.getSelObjects();
       try {
           SampleContext sc = preset.getDeviceContext().getDefaultSampleContext();
           if (ClassUtility.areAllInstanceOf(selObjs, ReadableParameterModel.class)) {
               ReadableParameterModel[] models = (ReadableParameterModel[]) Arrays.asList(selObjs).toArray(new ReadableParameterModel[selObjs.length]);
               if (ParameterModelUtilities.areAllOfId(models, 38) && !ParameterModelUtilities.areAllOfValue(models, 0)) {
                   Set s = ParameterModelUtilities.getValueSet(models);
                   ReadableSample[] samples = new ReadableSample[s.size()];
                   int si = 0;
                   for (Iterator i = s.iterator(); i.hasNext();)
                       samples[si++] = sc.getReadableSample((Integer) i.next());
                   String mstr;
                   if (samples.length == 1)
                       mstr = samples[0].getSampleDisplayName();
                   else
                       mstr = "Selected samples";
                   smi = ZCommandInvocationHelper.getMenu(samples, null, null, mstr);
               }
           }
       } catch (ZDeviceNotRunningException e) {
           e.printStackTrace();
       } catch (NoSuchSampleException e) {
           e.printStackTrace();
       } catch (ParameterUnavailableException e) {
           e.printStackTrace();
       }
       try {
           ArrayList menuItems = new ArrayList();
           menuItems.addDesktopElement(ZCommandInvocationHelper.getMenu(new Object[]{preset}, null, null, preset.getPresetDisplayName()));
           if (smi != null)
               menuItems.addDesktopElement(smi);
           if (customAction != null)
               menuItems.addDesktopElement(new JMenuItem(customAction));
           return (JMenuItem[]) menuItems.toArray(new JMenuItem[menuItems.size()]);
       } catch (NoSuchPresetException e) {
           e.printStackTrace();
       }
       return null;
   }
   */
}
