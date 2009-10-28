package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.packaging.PackagingGUIFactory;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.selections.ContextSampleSelection;
import com.pcmsolutions.device.EMU.E4.selections.DataFlavorGrid;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.gui.ProgressCallbackTree;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
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
                    final ContextSampleSelection ips = ((ContextSampleSelection) t.getTransferData(SampleContextTransferHandler.sampleContextFlavor));

                    final ReadableSample[] sourceReadableSamples = ips.getReadableSamples();
                    final Object[] destRowObjects = new Object[sourceReadableSamples.length];
                    for (int i = 0, j = sourceReadableSamples.length; i < j; i++)
                        destRowObjects[i] = sct.getValueAt(row + i, 0);

                    final Integer[] destIndexes = new Integer[destRowObjects.length];

                    for (int i = 0; i < destIndexes.length; i++)
                        destIndexes[i] = ((ReadableSample) destRowObjects[i]).getIndex();

                    String confirmStr = SampleContextMacros.getOverwriteConfirmationString(((ReadableSample) destRowObjects[0]).getSampleContext(), destIndexes);

                    int ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), confirmStr, "Confirm sample bulk copy", JOptionPane.YES_NO_OPTION);
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
                        JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), (files.size() == 1 ? "The file is not in a legal format for ZoeOS" : "None of the files are  in a legal format for ZoeOS"), "Problem", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    if (legalFiles.size() != files.size())
                        if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Not all files are importable, proceed?", "Not all importable", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 1)
                            return false;
                    int r = ((SampleContextTable) comp).getSelectedRow();
                    if (r >= 0) {
                        final Object o = ((SampleContextTable) comp).getValueAt(r, 0);
                        if (o instanceof ContextEditableSample)
                            try {
                                PackagingGUIFactory.loadSampleFiles((File[]) legalFiles.toArray(new File[legalFiles.size()]), (ContextEditableSample) o);
                            } catch (Exception e) {
                                e.printStackTrace();
                                UserMessaging.showError(e.getMessage());
                            }
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

    private void dropIsolatedSamples(final ContextSampleSelection ips, final Object[] destRowObjects) {
        try {
            ips.getSampleContext().getDeviceContext().getQueues().ddQ().getPostableTicket(new TicketRunnable() {
                public void run() throws Exception {
                    final List<ContextEditableSample> validSamples = new ArrayList<ContextEditableSample>();
                    for (Object o : destRowObjects)
                        if (o instanceof ContextEditableSample)
                            validSamples.add((ContextEditableSample) o);
                    if (validSamples.size() < 1)
                        return;
                    int errors = destRowObjects.length - validSamples.size();
                    if ( SampleContextMacros.getNonEmpty(ips.getReadableSamples()).length <ips.getReadableSamples().length ){
                        UserMessaging.showCommandFailed("Cannot drop empty samples.");
                        return;
                    }
                    ProgressCallback prog = new ProgressCallbackTree("Sample drop", false);
                    ProgressCallback[] progs = prog.splitTask(validSamples.size() * 2, false);
                    int pi = 0;
                    try {
                        for (IsolatedSample is : ips.getIsolatedSamples()) {
                            if ( prog.isCancelled()){
                                errors = 0;
                                return;
                            }
                            try {
                                is.assertSample(progs[pi]);
                            } catch (Exception e) {
                                is = null;
                                e.printStackTrace();
                                errors++;
                            } finally {
                                progs[pi++].updateProgress(1);
                            }
                        }
                        int i = 0;
                        for (IsolatedSample is : ips.getIsolatedSamples()) {
                            if ( prog.isCancelled()){
                                errors = 0;
                                return;
                            }
                            try {
                                validSamples.get(i++).newContent(is, is.getName(), progs[pi++]);
                            } catch (Exception e) {
                                e.printStackTrace();
                                errors++;
                            }
                        }
                    } catch (Exception e) {
                        UserMessaging.showCommandFailed(e.getMessage());
                        prog.updateProgress(1);
                    } finally {
                        if (errors == destRowObjects.length)
                            UserMessaging.showCommandFailed((destRowObjects.length > 1 ? "None of the source samples could be copied" : "The source sample could not be copied"));
                        else if (errors > 0)
                            UserMessaging.showCommandFailed(errors + " of " + destRowObjects.length + " source samples could not be copied");
                    }
                }
            }, "sampleDrop").post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
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
            for (int i = 0, j = sampleIndexes.length; i < j; i++)
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
            if (SampleContextMacros.extractRomIndexes(ss.getSampleIndexes()).length > 0)
                return null;
            ss.setUseTempNames(false);
            return new AbstractList() {
                // FlashMsg msg;

                public Object get(int index) {
                    // if (index == 0)
                    //   msg = new FlashMsg(ZoeosFrame.getInstance(), FlashMsg.colorInfo, "Downloading samples to dekstop", Integer.MAX_VALUE);
                    ProgressCallbackTree prog = new ProgressCallbackTree("Sample download to desktop", false);
                    try {
                        IsolatedSample is;
                        is = ss.getIsolatedSample(index);
                        is.assertSample(prog);
                        return is.getLocalFile();
                    } catch (IsolatedSampleUnavailableException e) {
                        return null;
                    } finally {
                        prog.updateProgress(1);
                        // if (index == size() - 1)
                        //   msg.terminate();
                    }
                }

                public int size() {
                    return ss.getSampleCount();
                }
            };
            /*
            ArrayList fl = new ArrayList();
            ProgressCallbackTree prog = new ProgressCallbackTree("Sample download to desktop", false);
            try {
                ProgressCallback[] progs = prog.splitTask(ss.getSampleCount(), false);
                for (int i = 0, j = ss.getSampleCount(); i < j; i++) {
                    try {
                        IsolatedSample is;
                        is = ss.getIsolatedSample(i);
                        is.assertSample(progs[i]);
                        fl.add(is.getLocalFile());
                        //List x = new AbstractList(){};
                    } catch (IsolatedSampleUnavailableException e) {
                        return null;
                    } finally {
                        progs[i].updateProgress(1);
                    }
                }
           } finally {
                prog.updateProgress(1);
            }
        */
        }
        return null;
    }
}
