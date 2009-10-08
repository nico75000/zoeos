package com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTransferHandler;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.selections.ContextPresetSelection;
import com.pcmsolutions.device.EMU.E4.selections.ContextSampleSelection;
import com.pcmsolutions.device.EMU.E4.selections.DataFlavorGrid;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;

public class PresetContextTransferHandler extends TransferHandler implements Transferable {
    public static final DataFlavorGrid presetContextFlavor = new DataFlavorGrid(ContextPresetSelection.class, "ContextPresetSelection");
    private ContextPresetSelection ips;

    public PresetContextTransferHandler() {
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        super.exportAsDrag(comp, e, action);
    }

    private static final String PREF_askJustVoicesForSampleDropOnPreset = "askJustVoicesForSampleDropOnPreset";
    private static final String PREF_justVoicesForSampleDropOnPreset = "justVoicesForSampleDropOnPreset";

    public boolean importData(JComponent comp, Transferable t) {
        if (t.isDataFlavorSupported(SampleContextTransferHandler.sampleContextFlavor)) {
            // do import!!
            if (comp instanceof PresetContextTable) {
                try {
                    final ContextSampleSelection sel = (ContextSampleSelection) t.getTransferData(SampleContextTransferHandler.sampleContextFlavor);
                    final PresetContextTable pct = (PresetContextTable) comp;

                    final int selRow = pct.getSelectedRow();
                    final Integer[] sampleIndexes = sel.getSampleIndexes();

                    Object o = pct.getValueAt(selRow, 0);
                    if (o instanceof ContextEditablePreset)
                        PresetContextMacros.applySamplesToPreset(((ContextEditablePreset) o).getPresetContext(), ((ContextEditablePreset) o).getPresetNumber(), sampleIndexes);
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParameterValueOutOfRangeException e) {
                    e.printStackTrace();
                } catch (PresetEmptyException e) {
                    e.printStackTrace();
                } catch (NoSuchContextException e) {
                    e.printStackTrace();
                } catch (TooManyVoicesException e) {
                    e.printStackTrace();
                } catch (NoSuchPresetException e) {
                    e.printStackTrace();
                } catch (ZDeviceNotRunningException e) {
                    e.printStackTrace();
                } catch (NoSuchSampleException e) {
                    e.printStackTrace();
                } catch (TooManyZonesException e) {
                    e.printStackTrace();
                }
            }
        } else if (t.isDataFlavorSupported(presetContextFlavor)) {
            // do import!!
            if (comp instanceof PresetContextTable) {
                final PresetContextTable pct = (PresetContextTable) comp;
                int row = pct.getSelectedRow();
                try {
                    ContextPresetSelection ips = ((ContextPresetSelection) t.getTransferData(PresetContextTransferHandler.presetContextFlavor));

                    final ReadablePreset[] sourceReadablePresets = ips.getReadablePresets();
                    final Object[] destRowObjects = new Object[sourceReadablePresets.length];
                    for (int i = 0,j = sourceReadablePresets.length; i < j; i++)
                        destRowObjects[i] = pct.getValueAt(row + i, 0);

                    /*Arrays.sort(sourceReadablePresets);
                    ArrayList destPresetObjs = new ArrayList();
                    for (int i = 0,j = sourceReadablePresets.length; i < j; i++) {
                        if (!(row + i < pct.getRowCount()))
                            break;
                        destPresetObjs.addDesktopElement(pct.getValueAt(row + i, 0));
                    }
                    final ReadablePreset[] dpos = new ReadablePreset[destPresetObjs.size()];
                    destPresetObjs.toArray(dpos);
                    */
                    Integer[] destIndexes = new Integer[destRowObjects.length];

                    for (int i = 0; i < destIndexes.length; i++)
                        destIndexes[i] = ((ReadablePreset) destRowObjects[i]).getPresetNumber();

                    String confirmStr = PresetContextMacros.getOverwriteConfirmationString(((ReadablePreset) destRowObjects[0]).getPresetContext(), destIndexes);

                    int ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), confirmStr, "Confirm Preset Bulk Copy", JOptionPane.YES_NO_OPTION);
                    if (ok == 0)
                        if (pct.getPresetContext() == ips.getPresetContext())
                            dropContextLocalPresets(destRowObjects, sourceReadablePresets, pct);
                        else
                            dropIsolatedPresets(ips, destRowObjects);
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static int progressLabelWidth = 72;

    private void dropIsolatedPresets(final ContextPresetSelection ips, final Object[] destRowObjects) {
        final Object progressOwner = new Object();
        final Thread mt = new ZDBModifyThread("D&D: Transfer IsolatedPresets") {
            public void run() {
                final Zoeos z = Zoeos.getInstance();
                z.beginProgressElement(progressOwner, ZUtilities.makeExactLengthString("Copying Presets", progressLabelWidth), destRowObjects.length * 2);
                int errors = 0;
                try {
                    final int j = destRowObjects.length;
                    for (int i = 0; i < j; i++) {
                        final int f_i = i;
                        final Object pobj = destRowObjects[i];
                        if (pobj instanceof ContextEditablePreset) {
                            final IsolatedPreset ip = ips.getIsolatedPreset(i);
                            z.updateProgressElement(progressOwner);
                            if (ip == null) {
                                z.updateProgressElement(progressOwner);
                                if (i >= j - 1)
                                    z.endProgressElement(progressOwner);
                                errors++;
                                continue;
                            }
                            new ZDBModifyThread("D&D: New Presets from IsolatedPresets") {
                                public void run() {
                                    // TODO!! should use a signal here to achieve correct ordering of threads
                                    try {
                                        z.updateProgressElementTitle(progressOwner, "Copying " + ip.getName() + " to " + ((ContextEditablePreset) pobj).getPresetDisplayName());
                                        ((ContextEditablePreset) pobj).newPreset(((ContextEditablePreset) pobj).getPresetNumber(), ip.getName(), ip);
                                    } catch (NoSuchPresetException e) {
                                        e.printStackTrace();
                                    } finally {
                                        z.updateProgressElement(progressOwner);
                                        if (f_i >= j - 1)
                                            z.endProgressElement(progressOwner);
                                    }
                                }
                            }.start();
                        } else {
                            z.updateProgressElement(progressOwner);
                            z.updateProgressElement(progressOwner);
                            if (i >= j - 1)
                                z.endProgressElement(progressOwner);
                            errors++;
                        }
                    }
                } finally {
                    if (errors == destRowObjects.length)
                        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), (destRowObjects.length > 1 ? "None of the source presets could be copied" : "The source preset could not be copied"), "Problem", JOptionPane.ERROR_MESSAGE);
                    else if (errors > 0)
                        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), errors + " of " + destRowObjects.length + " source presets could not be copied", "Problem", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        mt.start();
    }

    private void dropContextLocalPresets(final Object[] destRowObjects, final ReadablePreset[] readablePresets, final PresetContextTable pct) {
        new ZDBModifyThread("D&D: Copy Presets") {
            public void run() {
                final Zoeos z = Zoeos.getInstance();
                z.beginProgressElement(this, ZUtilities.makeExactLengthString("Copying Presets", progressLabelWidth), destRowObjects.length);
                int errors = 0;
                for (int i = readablePresets.length - 1; i >= 0; i--) {
                    try {
                        if (destRowObjects[i] instanceof ReadablePreset) {
                            z.updateProgressElementTitle(this, "Copying " + readablePresets[i].getPresetName() + " to " + ((ReadablePreset) destRowObjects[i]).getPresetDisplayName());
                            pct.getPresetContext().copyPreset(readablePresets[i].getPresetNumber(), ((ReadablePreset) destRowObjects[i]).getPresetNumber());
                        }
                    } catch (NoSuchPresetException e) {
                        errors++;
                        e.printStackTrace();
                    } catch (PresetEmptyException e) {
                        errors++;
                        e.printStackTrace();
                    } catch (NoSuchContextException e) {
                        errors++;
                        e.printStackTrace();
                    } finally {
                        z.updateProgressElement(this);
                    }
                }
                z.endProgressElement(this);
                if (errors == readablePresets.length)
                    JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), (destRowObjects.length > 1 ? "None of the source presets could be copied" : "The source preset could not be copied"), "Problem", JOptionPane.ERROR_MESSAGE);
                else if (errors > 0)
                    JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), errors + " of " + destRowObjects.length + " source presets could not be copied", "Problem", JOptionPane.ERROR_MESSAGE);
            }
        }.start();
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        if (!(comp instanceof PresetContextTable))
            return false;
        for (int i = 0, n = transferFlavors.length; i < n; i++)
            if (transferFlavors[i].equals(presetContextFlavor)) {
                ((PresetContextTable) comp).setDropFeedbackActive(true);
                ((PresetContextTable) comp).setChosenDropFlavor(presetContextFlavor);
                return true;
            } else if (transferFlavors[i].equals(SampleContextTransferHandler.sampleContextFlavor) /*&& SampleContextTransferHandler.sampleContextFlavor.numRows() == 1*/) {
                ((PresetContextTable) comp).setDropFeedbackActive(true);
                ((PresetContextTable) comp).setChosenDropFlavor(SampleContextTransferHandler.sampleContextFlavor);
                return true;
            }
        return false;
    }

    public int getSourceActions(JComponent c) {
        if (c instanceof PresetContextTable)
            return TransferHandler.COPY;
        return 0;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof PresetContextTable) {
            ips = ((PresetContextTable) c).getSelection();
            ReadablePreset[] readablePresets = ips.getReadablePresets();
            if (readablePresets.length == 0)
                return null;
            presetContextFlavor.clearGrid();
            presetContextFlavor.setDefCols(new int[]{0});

            for (int i = 0,j = readablePresets.length; i < j; i++)
                presetContextFlavor.addRow(readablePresets[i].getPresetNumber().intValue());

            //if (c instanceof DragAndDropTable)
            //    ((DragAndDropTable) c).clearSelection();

            return this;
        }
        return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{presetContextFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(presetContextFlavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor))
            return ips;
        return null;
    }
}
