package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.RemoteUnreachableException;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.packaging.PackageGenerationException;
import com.pcmsolutions.device.EMU.E4.packaging.SamplePackage;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.audio.AudioFormatAccessoryPanel;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZStringPref;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.sound.sampled.AudioFileFormat;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class NewSamplePackageZMTC extends AbstractContextEditableSampleZMTCommand {
    private static JFileChooser fc;
    private static final Preferences prefs = Preferences.userNodeForPackage(NewSamplePackageZMTC.class);
    public static final ZStringPref ZPREF_lastDir = new Impl_ZStringPref(prefs, "lastSaveSamplePackageDir", Zoeos.getHomeDir().getAbsolutePath());
    private static AudioFormatAccessoryPanel afap;

    protected Exception err = null;

    protected SampleContext sc;
    protected Integer[] sampleIndexes;

    private static void assertChooser() {
        if (fc == null) {
            fc = new JFileChooser();
            afap = new AudioFormatAccessoryPanel("Sample format");
            fc.setAccessory(afap);
            fc.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f.equals(Zoeos.getZoeosLocalDir()))
                        return false;

                    if (f.isDirectory() || (f.isFile() && (ZUtilities.hasExtension(f.getName(), SamplePackage.SAMPLE_PKG_EXT))))
                        return true;

                    return false;
                }

                public String getDescription() {
                    return "Sample Package";
                }
            });
            fc.setAcceptAllFileFilterUsed(false);
        }
        try {
            fc.setCurrentDirectory(new File(ZPREF_lastDir.getValue()));
        } catch (Exception e) {
        }
    }

    public NewSamplePackageZMTC() {
        init("Create New" + ZUtilities.DOT_POSTFIX, "Create a new sample package", null, null);
    }

    public String getMenuPathString() {
        return ";Packaging";
    }

    protected boolean done = false;
    protected ContextEditableSample[] samples;

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        samples = getTargets();

        if (!SampleContextMacros.areAllSameContext(samples))
            throw new CommandFailedException("Samples must be all from same context");

        sampleIndexes = SampleContextMacros.extractUniqueSampleIndexes(samples);
        sc = samples[0].getSampleContext();
        synchronized (this) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new NewSamplePackageZMTC.CreateSamplePackageDialog(ZoeosFrame.getInstance(), "New Sample Package");
                }
            });

            while (!done) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }

        if (err != null)
            throw new CommandFailedException(err.getMessage());
    }

    private void createAndSaveSamplePackage(SampleContext sc, Integer[] indexes, String name, String notes, AudioFileFormat.Type format) throws CommandFailedException {
        SamplePackage pkg = null;
        try {
            pkg = PackageFactory.createSamplePackage(sc, indexes, name, notes, null, format);
        } catch (PackageGenerationException e) {
            throw new CommandFailedException("Error saving sample package: " + e.getMessage());
        }
        File extFile = null;
        synchronized (this.getClass()) {
            assertChooser();
            fc.setSelectedFile(new File(name));
            int retval = fc.showSaveDialog(ZoeosFrame.getInstance());
            if (retval == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                //File extFile = new File(f.getAbsolutePath() + "." + SamplePackage.SAMPLE_PKG_EXT);
                extFile = ZUtilities.replaceExtension(f, SamplePackage.SAMPLE_PKG_EXT);

                if (extFile.exists() && JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Overwrite " + extFile.getName() + " ?", "File Already Exists", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 1)
                    return;

                ZPREF_lastDir.putValue(extFile.getAbsolutePath());

            }
        }
        if (extFile != null) {
            try {
                PackageFactory.saveSamplePackage(pkg, extFile);
            } catch (PackageGenerationException e) {
                throw new CommandFailedException("Error saving sample package: " + e.getMessage());
            } finally {
                try {
                    sc.getDeviceContext().sampleMemoryDefrag(false);
                } catch (ZDeviceNotRunningException e) {
                } catch (RemoteUnreachableException e) {
                }
            }
        }
    }

    private class CreateSamplePackageDialog extends ZDialog {

        private JTextField nameField;
        private JTextField notesField;

        private Box progressBox;
        private JProgressBar progressBar;
        private JLabel progressLabel;

        private JButton createButt;
        private JButton cancelButt;

        private Box mainBox;
        private JPanel buttPanel;
        private JPanel notesPanel;
        private JPanel namePanel;

        private static final String DEF_NAME = "Untitled";
        private static final String DEF_NOTES = "";

        public CreateSamplePackageDialog(Frame owner, String title) throws HeadlessException {
            super(owner, title, true);

            nameField = new JTextField(DEF_NAME, 48);
            notesField = new JTextField(DEF_NOTES, 48);
            namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            namePanel.add(new JLabel("Name"));
            namePanel.add(nameField);
            notesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            notesPanel.add(new JLabel("Notes"));
            notesPanel.add(notesField);

            progressBox = new Box(BoxLayout.X_AXIS);
            progressBar = new JProgressBar(0, 1000);
            progressLabel = new JLabel(ZUtilities.makeExactLengthString("", 48));
            progressBox.add(progressLabel);
            progressBox.add(progressBar);

            mainBox = new Box(BoxLayout.Y_AXIS);
            mainBox.setAlignmentY(Component.LEFT_ALIGNMENT);
            mainBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainBox.add(namePanel);
            mainBox.add(notesPanel);
            buttPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            createButt = new JButton(new AbstractAction("Create & Save") {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                    new ZDBModifyThread("Sample packaging") {
                        public void run() {
                            synchronized (NewSamplePackageZMTC.this) {
                                try {
                                    // TODO!! get his from user
                                    AudioFileFormat.Type format = AudioUtilities.defaultAudioFormat;
                                    createAndSaveSamplePackage(sc, sampleIndexes, nameField.getText(), notesField.getText(), format);
                                } catch (CommandFailedException e1) {
                                    err = e1;
                                } finally {
                                    done = true;
                                    NewSamplePackageZMTC.this.notify();
                                }
                            }
                        }
                    }.start();
                }
            });

            cancelButt = new JButton(new AbstractAction("Cancel") {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            buttPanel.add(createButt);
            buttPanel.add(cancelButt);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(mainBox, BorderLayout.CENTER);
            getContentPane().add(buttPanel, BorderLayout.SOUTH);

            setResizable(false);

            getRootPane().setDefaultButton(createButt);

            pack();
            show();
        }
    }
}

