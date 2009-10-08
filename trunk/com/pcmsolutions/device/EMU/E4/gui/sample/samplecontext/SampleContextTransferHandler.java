package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.RemoteUnreachableException;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.selections.ContextSampleSelection;
import com.pcmsolutions.device.EMU.E4.selections.DataFlavorGrid;
import com.pcmsolutions.device.EMU.E4.zcommands.LoadContextSamplesZMTC;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 19-May-2003
 * Time: 19:23:57
 * To change this template use Options | File Templates.
 */
public class SampleContextTransferHandler extends TransferHandler implements Transferable {
    public static final DataFlavorGrid sampleContextFlavor = new DataFlavorGrid(ContextSampleSelection.class, "ContextSampleSelection");
    private ContextSampleSelection ss;

    public SampleContextTransferHandler() {
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        super.exportAsDrag(comp, e, action);
    }

    public boolean importData(JComponent comp, Transferable t) {
        if (t.isDataFlavorSupported(sampleContextFlavor)) {
            // do import!!
            if (comp instanceof SampleContextTable) {
                final SampleContextTable sct = (SampleContextTable) comp;
                int row = sct.getSelectedRow();
                try {
                    ContextSampleSelection ips = ((ContextSampleSelection) t.getTransferData(SampleContextTransferHandler.sampleContextFlavor));

                    final ReadableSample[] sourceReadableSamples = ips.getReadableSamples();
                    final Object[] destRowObjects = new Object[sourceReadableSamples.length];
                    for (int i = 0,j = sourceReadableSamples.length; i < j; i++)
                        destRowObjects[i] = sct.getValueAt(row + i, 0);

                    Integer[] destIndexes = new Integer[destRowObjects.length];

                    for (int i = 0; i < destIndexes.length; i++)
                        destIndexes[i] = ((ReadableSample) destRowObjects[i]).getSampleNumber();

                    String confirmStr = SampleContextMacros.getOverwriteConfirmationString(((ReadableSample) destRowObjects[0]).getSampleContext(), destIndexes);

                    int ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), confirmStr, "Confirm Sample Bulk Copy", JOptionPane.YES_NO_OPTION);
                    if (ok == 0)
                        dropIsolatedSamples(ips, destRowObjects);
                    return true;
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            if (comp instanceof SampleContextTable) {
                try {
                    final List files = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
                    final List legalFiles = AudioUtilities.filterLegalAudioFiles(files);
                    if (legalFiles.size() == 0) {
                        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), (files.size() == 1 ? "The File is not in a legal format for ZoeOS" : "None of the files are  in a legal format for ZoeOS"), "Problem", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    if (legalFiles.size() != files.size())
                        if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Not all files are importable, proceed?", "Not all importable", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 1)
                            return false;
                    int r = ((SampleContextTable) comp).getSelectedRow();
                    if (r >= 0) {
                        final Object o = ((SampleContextTable) comp).getValueAt(r, 0);
                        if (o instanceof ContextEditableSample)
                            new ZDBModifyThread("Drop sample File list") {
                                public void run() {
                                    try {
                                        LoadContextSamplesZMTC.loadFilesToContext((ContextEditableSample) o, (File[]) legalFiles.toArray(new File[legalFiles.size()]));
                                    } catch (CommandFailedException e) {
                                        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), e.getMessage(), "Command Failed", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }.start();
                        return true;
                    }
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

    private void dropIsolatedSamples(final ContextSampleSelection ips, final Object[] destRowObjects) {
        final Object progressOwner = new Object();
        final Thread mt = new ZDBModifyThread("D&D: Transfer IsolatedSamples") {
            public void run() {
                final Zoeos z = Zoeos.getInstance();
                z.beginProgressElement(progressOwner, ZUtilities.makeExactLengthString("Copying Samples", progressLabelWidth), destRowObjects.length * 2);
                int errors = 0;
                try {
                    final int j = destRowObjects.length;
                    for (int i = j - 1; i >= 0; i--) {
                        final int f_i = i;
                        final Object sobj = destRowObjects[i];
                        if (sobj instanceof ContextEditableSample) {
                            IsolatedSample is = null;
                            try {
                                if (i == 0)
                                    is = ips.getIsolatedSample(i);
                                else
                                    is = ips.getIsolatedSample(i);
                                is.assert();
                            } catch (IsolatedSampleUnavailableException e) {
                                e.printStackTrace();
                            } finally {
                                z.updateProgressElement(progressOwner);
                                if (is == null) {
                                    z.updateProgressElement(progressOwner);
                                    if (i >= j - 1)
                                        z.endProgressElement(progressOwner);
                                    errors++;
                                    continue;
                                }
                            }
                            final IsolatedSample f_is = is;
                            // new ZDBModifyThread("D&D: New Samples from IsolatedSamples") {
                            //   public void run() {
                            // TODO!! should use a signal here to achieve correct ordering of threads
                            try {
                                z.setProgressElementIndeterminate(progressOwner, true);
                                z.updateProgressElementTitle(progressOwner, "Copying " + f_is.getName() + " to " + ((ContextEditableSample) sobj).getSampleDisplayName());
                                ((ContextEditableSample) sobj).newSample(f_is, f_is.getName());
                                f_is.zDispose();
                            } catch (NoSuchSampleException e) {
                                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), e.getMessage(), "Problem", JOptionPane.ERROR_MESSAGE);
                                errors++;
                                continue;
                            } catch (IsolatedSampleUnavailableException e) {
                                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), e.getMessage(), "Problem", JOptionPane.ERROR_MESSAGE);
                                errors++;
                                continue;
                            } finally {
                                z.setProgressElementIndeterminate(progressOwner, false);
                                z.updateProgressElement(progressOwner);
                                if (f_i >= j - 1)
                                    z.endProgressElement(progressOwner);
                            }
                            //  }
                            // }.stateStart();

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
                        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), (destRowObjects.length > 1 ? "None of the source samples could be copied" : "The source sample could not be copied"), "Problem", JOptionPane.ERROR_MESSAGE);
                    else if (errors > 0)
                        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), errors + " of " + destRowObjects.length + " source samples could not be copied", "Problem", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        mt.start();
    }


    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        if (!(comp instanceof SampleContextTable))
            return false;
        for (int i = 0, n = transferFlavors.length; i < n; i++) {
            if (transferFlavors[i].equals(sampleContextFlavor)) {
                ((SampleContextTable) comp).setDropFeedbackActive(true);
                ((SampleContextTable) comp).setChosenDropFlavor(sampleContextFlavor);
                return true;
            }
            if (transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
                ((SampleContextTable) comp).setDropFeedbackActive(true);
                ((SampleContextTable) comp).setChosenDropFlavor(DataFlavor.javaFileListFlavor);
                return true;
            }
        }

        return false;
    }

    public int getSourceActions(JComponent c) {
        if (c instanceof SampleContextTable)
            return TransferHandler.COPY;
        return 0;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof SampleContextTable) {
            ss = ((SampleContextTable) c).getSelection();
            Integer[] sampleIndexes = ss.getSampleIndexes();
            if (sampleIndexes.length == 0)
                return null;
            sampleContextFlavor.clearGrid();
            sampleContextFlavor.setDefCols(new int[]{0});

            for (int i = 0,j = sampleIndexes.length; i < j; i++)
                sampleContextFlavor.addRow(sampleIndexes[i].intValue());

            //if (c instanceof DragAndDropTable)
            //    ((DragAndDropTable) c).clearSelection();
            return this;
        }
        return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{sampleContextFlavor, DataFlavor.javaFileListFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(sampleContextFlavor) || flavor.equals(DataFlavor.javaFileListFlavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(sampleContextFlavor))
            return ss;
        else if (flavor.equals(DataFlavor.javaFileListFlavor)) {
            ss.setUseTempNames(false);
            ArrayList fl = new ArrayList();
            try {
                for (int i = 0, j = ss.getSampleCount(); i < j; i++) {
                    try {
                        IsolatedSample is;
                        is = ss.getIsolatedSample(i);
                        is.assert();
                        fl.add(is.getLocalFile());
                    } catch (IsolatedSampleUnavailableException e) {
                        return null;
                    }
                }
                return fl;
            } finally {
                try {
                    ss.getSampleContext().getDeviceContext().sampleMemoryDefrag(false);
                } catch (ZDeviceNotRunningException e) {
                } catch (RemoteUnreachableException e) {
                }
            }
        }
        return null;
    }
}
