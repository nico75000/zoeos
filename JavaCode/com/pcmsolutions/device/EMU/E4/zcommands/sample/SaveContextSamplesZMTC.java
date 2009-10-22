package com.pcmsolutions.device.EMU.E4.zcommands.sample;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.gui.ProgressCallbackTree;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.audio.AudioFormatAccessoryPanel;
import com.pcmsolutions.gui.audio.SaveAccessoryPanel;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZStringPref;
import com.pcmsolutions.system.tasking.TicketRunnable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class SaveContextSamplesZMTC extends AbstractContextEditableSampleZMTCommand {
    private static JFileChooser fc;
    private static final Preferences prefs = Preferences.userNodeForPackage(SaveContextSamplesZMTC.class.getClass());
    public static final ZStringPref ZPREF_lastDir = new Impl_ZStringPref(prefs, "lastSaveSamplesDir", "");

    private static AudioFormatAccessoryPanel afap;
    private static SaveAccessoryPanel sap;

    private static void assertChooser() {
        if (fc == null) {
            fc = new JFileChooser();
            Box ab = new Box(BoxLayout.Y_AXIS);
            ab.add(afap = new AudioFormatAccessoryPanel("Sample format"));
            ab.add(sap = new SaveAccessoryPanel(null));
            fc.setAccessory(ab);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() && !f.equals(Zoeos.getZoeosLocalDir());
                }

                public String getDescription() {
                    return "Directories";
                }
            });
            fc.setAcceptAllFileFilterUsed(false);
        }
        try {
            fc.setCurrentDirectory(new File(ZPREF_lastDir.getValue()));
        } catch (Exception e) {
        }
    }

    public String getPresentationString() {
        return "Save" + ZUtilities.DOT_POSTFIX;
    }

    public String getDescriptiveString() {
        return "Save selected samples to a local directory";
    }

    public String getMenuPathString() {
        return "";
    }

    public boolean handleTarget(ContextEditableSample sample, int total, int curr) throws Exception {
        final ContextEditableSample[] samples = getTargets().toArray(new ContextEditableSample[numTargets()]);
        File f = null;
        assertChooser();
        int retval = fc.showDialog(ZoeosFrame.getInstance(), "Choose Directory");
        if (retval == JFileChooser.APPROVE_OPTION) {
            f = fc.getSelectedFile();
            ZPREF_lastDir.putValue(f.getAbsolutePath());

            if (f != null && f.isDirectory()) {
                final File f_f = f;
                samples[0].getDeviceContext().getQueues().zCommandQ().getPostableTicket(new TicketRunnable() {
                    public void run() throws Exception {
                        ProgressCallbackTree prog = new ProgressCallbackTree("Sample save", true) {
                            public String finalizeString(String s) {
                                return samples[0].getDeviceContext().makeDeviceProgressTitle(s);
                            }
                        };
                        int errors = 0;
                        int overwriteMode = sap.getOverwriteMode();
                        try {
                            String name = "";
                            String nameMode;
                            int im = sap.getIndexMode();
                            switch (im) {
                                case SaveAccessoryPanel.MODE_PREFIXED_INDEX:
                                    nameMode = AudioUtilities.SAMPLE_NAMING_MODE_IN;
                                    break;
                                case SaveAccessoryPanel.MODE_POSTFIXED_INDEX:
                                    nameMode = AudioUtilities.SAMPLE_NAMING_MODE_NI;
                                    break;
                                case SaveAccessoryPanel.MODE_NO_INDEX:
                                    nameMode = AudioUtilities.SAMPLE_NAMING_MODE_N;
                                    break;
                                default:
                                    throw new IllegalArgumentException("illegal index mode");
                            }
                            ProgressCallback[] progs = prog.splitTask(samples.length, true);
                            for (int i = 0; i < samples.length; i++) {
                                try {
                                    name = samples[i].getDisplayName();
                                    File sf;
                                    //SampleDownloadDescriptor sdd = SampleDownloadDescriptorFactory.getGeneralDownload(samples[i].getSample(), samples[i].getName(), f_f, afap.getActiveType(), nameMode, true, (overwriteMode == SaveAccessoryPanel.MODE_ALWAYS_OVERWRITE ? true : false));
                                    SampleDownloadDescriptor sdd = SampleDownloadDescriptorFactory.getGeneralDownload(samples[i].getSample(), ZUtilities.getExternalName(samples[i].getName()), f_f, afap.getActiveType(), nameMode, true, true);
                                    try {
                                        IsolatedSample is = samples[i].getIsolated(sdd);
                                        //sf = samples[i].retrieveCustomLocalCopy(sdd, progs[i]);
                                        if (is.getLocalFile().exists() && overwriteMode != SaveAccessoryPanel.MODE_ALWAYS_OVERWRITE) {
                                            if (overwriteMode == SaveAccessoryPanel.MODE_ASK_OVERWRITE) {
                                                //int r = UserMessaging.askYesNoYesAll("File for " + "\"" + name + "\"" + " already exists, overwrite?", "File Already Exists");
                                                int r = UserMessaging.askYesNoYesAllNoAll("File \"" + sdd.getFile().getName() + "\" already exists, overwrite?", "File already exists");
                                                if (r == 2)
                                                    overwriteMode = SaveAccessoryPanel.MODE_ALWAYS_OVERWRITE;
                                                if (r == 3)
                                                    overwriteMode = SaveAccessoryPanel.MODE_NEVER_OVERWRITE;
                                                if (r == 0 || r == 2)
                                                    is.assertSample(progs[i]);
                                                //samples[i].retrieveCustomLocalCopy(SampleDownloadDescriptorFactory.getGeneralDownload(samples[i].getSample(), samples[i].getName(), f_f, afap.getActiveType(), nameMode, true, true), progs[i]);
                                            } else if (overwriteMode == SaveAccessoryPanel.MODE_NEVER_OVERWRITE)
                                                continue;
                                            else
                                                is.assertSample(progs[i]);
                                        } else
                                            is.assertSample(progs[i]);
                                    } finally {
                                    }
                                } catch (SampleException e) {
                                    errors++;
                                } catch (EmptyException e) {
                                    errors++;
                                } catch (IsolatedSampleUnavailableException e) {
                                    errors++;
                                } finally {
                                }
                            }
                        } finally {
                            prog.updateProgress(1);
                        }
                        if (errors == samples.length)
                            UserMessaging.showError(samples.length > 1 ? "None of the source samples could be retrieved" : "The source sample could not be retrieved");
                        else if (errors > 0)
                            UserMessaging.showError(errors + " of " + samples.length + " source samples could not be retrieved");

                    }
                }, getPresentationString()).post();
            } else
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        UserMessaging.flashError(fc, "No valid directory selected for sample save");
                    }
                });
        }
        return false;
    }
}

