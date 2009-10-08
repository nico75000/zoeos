package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.RemoteUnreachableException;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.gui.ProgressMultiBox;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.audio.AudioFormatAccessoryPanel;
import com.pcmsolutions.gui.audio.SaveAccessoryPanel;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZStringPref;

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
    private static final Preferences prefs = Preferences.userNodeForPackage(SaveContextSamplesZMTC.class);
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

    public SaveContextSamplesZMTC() {
        super("Save" + ZUtilities.DOT_POSTFIX, "Save selected samples to a local directory", null, null);
    }

    public String getMenuPathString() {
        // return ";Save";
        return "";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditableSample[] samples = getTargets();

        if (samples.length == 0)
            doLocalCopy(new ContextEditableSample[]{getTarget()}, "Saving");
        else
            doLocalCopy(samples, "Saving");
    }

    protected void doLocalCopy(ContextEditableSample[] samples, String prefix) {
        File f = null;

        synchronized (this.getClass()) {
            assertChooser();
            int retval = fc.showDialog(ZoeosFrame.getInstance(), "Choose Directory");
            if (retval == JFileChooser.APPROVE_OPTION) {
                f = fc.getSelectedFile();
                ZPREF_lastDir.putValue(f.getAbsolutePath());
            }
        }

        if (f != null) {
            int errors = 0;
            Zoeos z = Zoeos.getInstance();
            z.beginProgressElement(this, ZUtilities.makeExactLengthString(prefix + " Samples", 80), samples.length);
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
                for (int i = 0; i < samples.length; i++) {
                    try {
                        name = samples[i].getSampleDisplayName();
                        z.setProgressElementIndeterminate(this, true);
                        File sf;
                        SampleRetrievalInfo sri = new Impl_SampleRetrievalInfo(samples[i].getSample(), samples[i].getSampleName(), f, afap.getActiveType(), nameMode, true, (overwriteMode == SaveAccessoryPanel.MODE_ALWAYS_OVERWRITE ? true : false));
                        z.updateProgressElementTitle(this, prefix + " " + name + " to \"" + sri.getFile().getName() + "\"");
                        try {
                            sf = samples[i].retrieveCustomLocalCopy(sri);

                            if (sf == null && (overwriteMode == SaveAccessoryPanel.MODE_ASK_OVERWRITE)) {
                                //int r = UserMessaging.askYesNoYesAll("File for " + "\"" + name + "\"" + " already exists, overwrite?", "File Already Exists");
                                int r = UserMessaging.askYesNoYesAllNoAll("File \"" + sri.getFile().getName() + "\" already exists, overwrite?", "File Already Exists");

                                if (r == 2)
                                    overwriteMode = SaveAccessoryPanel.MODE_ALWAYS_OVERWRITE;
                                if (r == 3)
                                    overwriteMode = SaveAccessoryPanel.MODE_NEVER_OVERWRITE;
                                if (r == 0 && r == 2)
                                    samples[i].retrieveCustomLocalCopy(new Impl_SampleRetrievalInfo(samples[i].getSample(), samples[i].getSampleName(), f, afap.getActiveType(), nameMode, true, true));
                            }
                        } finally {
                            z.setProgressElementIndeterminate(this, false);
                            z.updateProgressElement(this, "Finished " + name);
                        }
                    } catch (NoSuchSampleException e) {
                        errors++;
                    } catch (SampleEmptyException e) {
                        errors++;
                    } catch (SampleRetrievalException e) {
                        errors++;
                    } finally {
                    }
                }
            } finally {
                try {
                    if (samples.length > 0)
                        samples[0].getDeviceContext().sampleMemoryDefrag(false);
                } catch (ZDeviceNotRunningException e) {
                } catch (RemoteUnreachableException e) {
                }
                z.updateProgressElement(this, ProgressMultiBox.PROGRESS_DONE_TITLE);
                z.endProgressElement(this);
            }
            if (errors == samples.length)
                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), (samples.length > 1 ? "None of the source samples could be retrieved" : "The source sample could not be retrieved"), "Problem", JOptionPane.ERROR_MESSAGE);
            else if (errors > 0)
                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), errors + " of " + samples.length + " source samples could not be retrieved", "Problem", JOptionPane.ERROR_MESSAGE);
        }
    }
}

