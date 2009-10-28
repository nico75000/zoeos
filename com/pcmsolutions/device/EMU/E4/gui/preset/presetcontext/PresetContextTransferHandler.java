package com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext;

import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceParameterTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.VoiceParameterSelectionAcceptor;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.selections.ContextPresetSelection;
import com.pcmsolutions.device.EMU.E4.selections.ContextSampleSelection;
import com.pcmsolutions.device.EMU.E4.selections.DataFlavorGrid;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.gui.ProgressSession;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

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

    public boolean importData(JComponent comp, Transferable t) {
        if (t.isDataFlavorSupported(SampleContextTransferHandler.sampleContextFlavor)) {
            // do import!!
            if (comp instanceof PresetContextTable) {
                try {
                    final ContextSampleSelection sel = (ContextSampleSelection) t.getTransferData(SampleContextTransferHandler.sampleContextFlavor);
                    final PresetContextTable pct = (PresetContextTable) comp;

                    final int selRow = pct.getSelectedRow();
                    final Integer[] sampleIndexes = sel.getSampleIndexes();

                    final Object o = pct.getValueAt(selRow, 0);
                    if (o instanceof ContextEditablePreset)
                        try {
                            PresetContextMacros.applySamplesToPreset(((ContextEditablePreset) o).getPresetContext(), ((ContextEditablePreset) o).getIndex(), sampleIndexes);
                        } catch (ResourceUnavailableException e) {
                            e.printStackTrace();
                        }
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (t.isDataFlavorSupported(presetContextFlavor)) {
            // do import!!
            if (comp instanceof PresetContextTable) {
                final PresetContextTable pct = (PresetContextTable) comp;
                int row = pct.getSelectedRow();
                try {
                    final ContextPresetSelection ips = ((ContextPresetSelection) t.getTransferData(PresetContextTransferHandler.presetContextFlavor));

                    final ReadablePreset[] sourceReadablePresets = ips.getReadablePresets();
                    final Object[] destRowObjects = new Object[sourceReadablePresets.length];
                    for (int i = 0, j = sourceReadablePresets.length; i < j; i++)
                        destRowObjects[i] = pct.getValueAt(row + i, 0);

                    final Integer[] destIndexes = new Integer[destRowObjects.length];
                    for (int i = 0; i < destIndexes.length; i++)
                        try {
                            destIndexes[i] = ((ReadablePreset) destRowObjects[i]).getIndex();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

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
                    //    Impl_ZThread.ddTQ.postTask(new Impl_ZThread.Task(){
                    //      public void doTask() {
                    String confirmStr = PresetContextMacros.getOverwriteConfirmationString(((ReadablePreset) destRowObjects[0]).getPresetContext(), destIndexes);
                    int ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), confirmStr, "Confirm Preset Bulk Copy", JOptionPane.YES_NO_OPTION);
                    if (ok == 0)
                        if (pct.getContext() == ips.getPresetContext())
                            dropContextLocalPresets(destRowObjects, sourceReadablePresets, pct);
                        else
                            dropIsolatedPresets(ips, destRowObjects);
                    //       }
                    // });
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
        try {
            ips.getPresetContext().getDeviceContext().getQueues().ddQ().getPostableTicket(new TicketRunnable() {
                public void run() throws Exception {
                    final Zoeos z = Zoeos.getInstance();
                    final ProgressSession ps = z.getProgressSession(ZUtilities.makeExactLengthString("Copying Presets", progressLabelWidth), destRowObjects.length * 2);
                    int errors = 0;
                    try {
                        final int j = destRowObjects.length;
                        for (int i = 0; i < j; i++) {
                            final int f_i = i;
                            final Object pobj = destRowObjects[i];
                            if (pobj instanceof ContextEditablePreset) {
                                final IsolatedPreset ip = ips.getIsolatedPreset(i);
                                ps.updateStatus();
                                if (ip == null) {
                                    ps.updateStatus();
                                    if (i >= j - 1)
                                        ps.end();
                                    errors++;
                                    continue;
                                }
                                // TODO!! should use a signal here to achieve correct ordering of threads
                                try {
                                    ps.updateTitle("Copying " + ip.getName() + " to " + ((ContextEditablePreset) pobj).getDisplayName());
                                    ((ContextEditablePreset) pobj).newPreset(((ContextEditablePreset) pobj).getIndex(), ip.getName(), ip);
                                } catch (PresetException e) {
                                    e.printStackTrace();
                                } finally {
                                    ps.updateStatus();
                                    if (f_i >= j - 1)
                                        ps.end();
                                }
                            } else {
                                ps.updateStatus();
                                ps.updateStatus();
                                if (i >= j - 1)
                                    ps.end();
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
            }, "dropIsolatedPresets").post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void dropContextLocalPresets(final Object[] destRowObjects, final ReadablePreset[] readablePresets, final PresetContextTable pct) {
        final Zoeos z = Zoeos.getInstance();
        ProgressSession ps = null;
        ps = z.getProgressSession(ZUtilities.makeExactLengthString("Copying Presets", progressLabelWidth), destRowObjects.length);
        int errors = 0;
        try {
            for (int i = readablePresets.length - 1; i >= 0; i--) {
                try {
                    if (destRowObjects[i] instanceof ReadablePreset) {
                        ps.updateTitle("Copying " + readablePresets[i].getName() + " to " + ((ReadablePreset) destRowObjects[i]).getDisplayName());
                        pct.getContext().copy(readablePresets[i].getIndex(), ((ReadablePreset) destRowObjects[i]).getIndex()).post();
                        if (readablePresets.length == 1) {
                            ReadablePreset p = (ReadablePreset) destRowObjects[0];
                            if (p.getDeviceContext().getDevicePreferences().ZPREF_askToOpenAfterPresetCopy.getValue())
                                if (UserMessaging.askYesNo("Open '" + p.getDisplayName() + "' now?"))
                                    try {
                                        p.getDeviceContext().getViewManager().openPreset(p, true).post();
                                    } catch (ResourceUnavailableException e) {
                                        e.printStackTrace();
                                    }
                        }
                    }
                } catch (Exception e) {
                    errors++;
                    e.printStackTrace();
                } finally {
                    ps.updateStatus();
                }
            }
        } finally {
            ps.end();
        }
        if (errors == readablePresets.length)
            JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), (destRowObjects.length > 1 ? "None of the source presets could be copied" : "The source preset could not be copied"), "Problem", JOptionPane.ERROR_MESSAGE);
        else if (errors > 0)
            JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), errors + " of " + destRowObjects.length + " source presets could not be copied", "Problem", JOptionPane.ERROR_MESSAGE);
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
            } else if (transferFlavors[i].equals(SampleContextTransferHandler.sampleContextFlavor) /*&& SampleContextTransferHandler.sampleContextFlavor.numRows() == 1*/) {
                ((PresetContextTable) comp).setDropFeedbackActive(true);
                ((PresetContextTable) comp).setChosenDropFlavor(SampleContextTransferHandler.sampleContextFlavor);
                return true;
            }

        // if (transferFlavors[i] instanceof VoiceParameterTableTransferHandler.VoiceParameterDataFlavor && ((VoiceParameterSelectionAcceptor) comp).willAcceptCategory(((VoiceParameterTableTransferHandler.VoiceParameterDataFlavor) transferFlavors[i]).getCategory())
          //          || transferFlavors[i].equals(cordParameterFlavor) && ((VoiceParameterSelectionAcceptor) comp).willAcceptCategory(VoiceParameterSelection.VOICE_CORDS)

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

            for (int i = 0, j = readablePresets.length; i < j; i++)
                presetContextFlavor.addRow(readablePresets[i].getIndex().intValue());

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
