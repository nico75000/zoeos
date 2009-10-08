package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.audio.SampleAuditionPanel;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZStringPref;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class LoadContextSamplesZMTC extends AbstractContextEditableSampleZMTCommand {
    private static JFileChooser fc;
    private static final Preferences prefs = Preferences.userNodeForPackage(LoadContextSamplesZMTC.class);
    public static final ZStringPref ZPREF_lastDir = new Impl_ZStringPref(prefs, "lastLoadSamplesDir", "");

    private static void assertChooser() {
        final String extStr = AudioUtilities.getLegalAudioExtensionsString();
        if (fc == null) {
            fc = new JFileChooser();
            final SampleAuditionPanel sap = new SampleAuditionPanel("Auditioning");
            fc.setAccessory(sap);
            fc.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                        File[] files = fc.getSelectedFiles();
                        if (files.length == 1)
                            sap.setCurrentFile(files[0]);
                        else
                            sap.setCurrentFile(null);
                    } else {
                        sap.setCurrentFile(null);
                    }
                }
            });

            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(true);
            fc.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f.equals(Zoeos.getZoeosLocalDir()))
                        return false;

                    if (f.isDirectory() || (f.isFile() && AudioUtilities.isLegalAudio(f.getName())))
                        return true;

                    return false;
                }

                public String getDescription() {
                    return extStr;
                }
            });
            fc.setAcceptAllFileFilterUsed(false);
        }
        try {
            fc.setCurrentDirectory(new File(ZPREF_lastDir.getValue()));
        } catch (Exception e) {
        }
    }

    public LoadContextSamplesZMTC() {
        super("Load" + ZUtilities.DOT_POSTFIX, "Load samples from a local directory", null, null);
    }

    public String getMenuPathString() {
        return "";
    }

    public int getMaxNumTargets() {
        return 1;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditableSample[] samples = getTargets();

        if (samples.length == 0)
            doLoad(getTarget());
        else
            doLoad(samples[0]);
    }

    protected void doLoad(ContextEditableSample beginSample) throws CommandFailedException {
        File[] files = null;
        synchronized (this.getClass()) {
            assertChooser();
            int retval = fc.showDialog(ZoeosFrame.getInstance(), "Load");
            ((SampleAuditionPanel) fc.getAccessory()).stopClip();
            if (retval == JFileChooser.APPROVE_OPTION) {
                files = fc.getSelectedFiles();
                ZPREF_lastDir.putValue(files[0].getParentFile().getAbsolutePath());
            }
        }
        if (files != null)
            loadFilesToContext(beginSample, files);
    }

    //  public static void preProcessFiles(File[] files){
    //
    // }

    public static void loadFilesToContext(ContextEditableSample beginSample, final File[] files) throws CommandFailedException {
        if (files.length > 0) {
            DecimalFormat df = new DecimalFormat("0000");

            if (beginSample.getSampleNumber().intValue() + files.length > DeviceContext.MAX_USER_SAMPLE) {
                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Not enough room beyond location " + df.format(beginSample.getSampleNumber()) + " to load " + files.length + " samples", "Problem", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int mc = ZUtilities.getFileCountForPattern(files, AudioUtilities.sampleIndexPattern);
            boolean stripIndex = false;
            if (mc > 0) {
                if (mc == files.length)
                    if (mc == 1)
                        stripIndex = UserMessaging.askYesNo("The sample File name appears to have a sample index appendage. Strip?");
                    else
                        stripIndex = UserMessaging.askYesNo("All of the sample File names appear to have a sample index appendage. Strip?");
                else
                    stripIndex = UserMessaging.askYesNo(mc + " of the sample File names appear to have a sample index appendage. Strip?");
            }
            SampleContext sc = beginSample.getSampleContext();
            String opt1;
            String opt2;
            try {
                if (SampleContextMacros.areSampleIndexesEmpty(sc, beginSample.getSampleNumber(), files.length)) {
                    SampleContextMacros.loadSamplesToContext(files, ZUtilities.fillIncrementally(new Integer[files.length], beginSample.getSampleNumber().intValue()), beginSample.getSampleContext(), true, stripIndex);
                    return;
                }
            } catch (NoSuchSampleException e) {
                throw new CommandFailedException("No such sample");
            } catch (NoSuchContextException e) {
                throw new CommandFailedException("Problem with sample context");
            }
            if (files.length > 1) {
                opt1 = "At locations " + df.format(beginSample.getSampleNumber()) + " - " + df.format(beginSample.getSampleNumber().intValue() + files.length - 1);
                opt2 = "At first " + files.length + " empty locations searching from " + df.format(beginSample.getSampleNumber());
            } else {
                opt1 = "At location " + df.format(beginSample.getSampleNumber());
                opt2 = "At first empty location searching from " + df.format(beginSample.getSampleNumber());
            }
            int res = JOptionPane.showOptionDialog(ZoeosFrame.getInstance(), (files.length == 1 ? "Load 1 sample" : "Load " + files.length + " samples "), "Load Samples", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{opt1, opt2}, opt1);
            if (res == 0) {
                int si = beginSample.getSampleNumber().intValue();
                Integer[] indexes = new Integer[files.length];
                for (int i = 0; i < indexes.length; i++) {
                    indexes[i] = IntPool.get(si + i);
                }
                SampleContextMacros.loadSamplesToContext(files, indexes, beginSample.getSampleContext(), true, stripIndex);
            } else if (res == 1) {
                try {
                    List emptyList = sc.findEmptySamplesInContext(files.length, beginSample.getSampleNumber(), IntPool.get(DeviceContext.MAX_USER_SAMPLE));

                    if (emptyList.size() < files.length)
                        throw new CommandFailedException("Could not find " + files.length + " empty user sample locations.");

                    SampleContextMacros.loadSamplesToContext(files, (Integer[]) emptyList.toArray(new Integer[emptyList.size()]), beginSample.getSampleContext(), true, stripIndex);
                } catch (NoSuchContextException e) {
                    throw new CommandFailedException("Problem with sample context");
                }
            }
        }
    }
}

