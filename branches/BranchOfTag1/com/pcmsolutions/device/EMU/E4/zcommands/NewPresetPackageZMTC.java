package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.RemoteUnreachableException;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.packaging.PackageGenerationException;
import com.pcmsolutions.device.EMU.E4.packaging.PresetPackage;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZBoolPref;
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
 * Date: 29-Sep-2003
 * Time: 13:53:18
 * To change this template use Options | File Templates.
 */
public class NewPresetPackageZMTC extends AbstractReadablePresetZMTCommand implements E4ReadablePresetZCommandMarker {

    protected static final Preferences prefs = Preferences.userNodeForPackage(NewPresetPackageZMTC.class).node("Preset packaging");
    protected static final ZBoolPref ZPREF_incSamples = new Impl_ZBoolPref(prefs, "incSamples", true);
    protected static final ZBoolPref ZPREF_incMultiMode = new Impl_ZBoolPref(prefs, "incMultiMode", false);
    protected static final ZBoolPref ZPREF_incMaster = new Impl_ZBoolPref(prefs, "incMaster", false);

    protected PresetContext pc;
    protected Integer[] presetIndexes;

    protected Exception err = null;

    protected boolean done = false;
    protected boolean cancelled = false;

    public NewPresetPackageZMTC() {
        init("Create New" + ZUtilities.DOT_POSTFIX, "Create a new preset package", null, null);
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ReadablePreset[] presets = getTargets();

        if (!PresetContextMacros.areAllSameContext(presets))
            throw new CommandFailedException("Presets must be all from same context");

        presetIndexes = PresetContextMacros.extractUniquePresetIndexes(presets);
        pc = presets[0].getPresetContext();

        synchronized (this) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new CreatePresetPackageDialog(ZoeosFrame.getInstance(), "New Preset Package");
                }
            });
            while (true) {
                try {
                    wait();
                    if (done)
                        break;
                } catch (InterruptedException e) {
                }
            }
        }

        if (err != null)
            throw new CommandFailedException(err.getMessage());
    }

    private static JFileChooser fc;
    public static final ZStringPref ZPREF_lastDir = new Impl_ZStringPref(prefs, "lastPresetPackageDir", Zoeos.getHomeDir().getAbsolutePath());

    private static void assertChooser() {
        if (fc == null) {
            fc = new JFileChooser();
            fc.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f.equals(Zoeos.getZoeosLocalDir()))
                        return false;

                    if (f.isDirectory() || (f.isFile() && (ZUtilities.hasExtension(f.getName(), PresetPackage.PRESET_PKG_EXT))))
                        return true;

                    return false;
                }

                public String getDescription() {
                    return "Preset Package";
                }
            });
            fc.setAcceptAllFileFilterUsed(false);
            try {
                fc.setCurrentDirectory(new File(ZPREF_lastDir.getValue()));
            } catch (Exception e) {
            }
        }
    }

    private void createAndSavePresetPackage(PresetContext pc, Integer[] indexes, String name, String notes, boolean is, AudioFileFormat.Type format, boolean im, boolean imm) throws CommandFailedException {
        PresetPackage pkg = null;
        try {
            pkg = PackageFactory.createPresetPackage(pc, indexes, true, name, notes, im, imm, is, format, null);
        } catch (PackageGenerationException e) {
            throw new CommandFailedException("Error saving preset package: " + e.getMessage());
        }
        File extFile = null;
        synchronized (this.getClass()) {
            assertChooser();
            fc.setSelectedFile(new File(name));
            int retval = fc.showSaveDialog(ZoeosFrame.getInstance());
            if (retval == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                //File extFile = new File(f.getAbsolutePath() + "." + PresetPackage.PRESET_PKG_EXT);
                extFile = ZUtilities.replaceExtension(f, PresetPackage.PRESET_PKG_EXT);

                if (extFile.exists() && JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Overwrite " + extFile.getName() + " ?", "File Already Exists", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 1)
                    return;

                ZPREF_lastDir.putValue(extFile.getAbsolutePath());
            }
        }
        if (extFile != null) {
            try {
                PackageFactory.savePresetPackage(pkg, extFile);
            } catch (PackageGenerationException e) {
                throw new CommandFailedException("Error saving preset package: " + e.getMessage());
            } finally {
                try {
                    pc.getDeviceContext().sampleMemoryDefrag(false);
                } catch (ZDeviceNotRunningException e) {
                } catch (RemoteUnreachableException e) {
                }
            }
        }
    }

    public String getMenuPathString() {
        return ";Packaging";
    }

    private class CreatePresetPackageDialog extends ZDialog {

        private JCheckBox incSamples;
        private JCheckBox incMM;
        private JCheckBox incMaster;
        //private JCheckBox compress;
        private Box checkBox;

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

        public CreatePresetPackageDialog(Frame owner, String title) throws HeadlessException {
            super(owner, title, true);

            incSamples = new JCheckBox(new AbstractAction("Include user samples") {
                public void actionPerformed(ActionEvent e) {
                    ZPREF_incSamples.putValue(((JCheckBox) e.getSource()).isSelected());
                }
            });
            incSamples.setSelected(ZPREF_incSamples.getValue());

            incMM = new JCheckBox(new AbstractAction("Include multi-mode settings") {
                public void actionPerformed(ActionEvent e) {
                    ZPREF_incMultiMode.putValue(((JCheckBox) e.getSource()).isSelected());
                }
            });
            incMM.setSelected(ZPREF_incMultiMode.getValue());

            incMaster = new JCheckBox(new AbstractAction("Include master settings") {
                public void actionPerformed(ActionEvent e) {
                    ZPREF_incMaster.putValue(((JCheckBox) e.getSource()).isSelected());
                }
            });
            incMaster.setSelected(ZPREF_incMaster.getValue());

            /*compress = new JCheckBox(new AbstractAction("Compress non-sample data") {
                public void actionPerformed(ActionEvent e) {
                    sun.putBoolean(PREF_compress, ((JCheckBox) e.getSource()).isSelected());
                }
            });
            compress.setSelected(sun.getBoolean(PREF_compress, false));
            */

            checkBox = new Box(BoxLayout.Y_AXIS);
            checkBox.add(incSamples);
            checkBox.add(incMM);
            checkBox.add(incMaster);
            //checkBox.addDesktopElement(compress);

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
            mainBox.add(checkBox);
            //mainBox.addDesktopElement(incMM);
            // mainBox.addDesktopElement(incMaster);
            //mainBox.addDesktopElement(progressBox);

            buttPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            createButt = new JButton(new AbstractAction("Create & Save") {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                    new ZDBModifyThread("Preset packaging") {
                        public void run() {
                            synchronized (NewPresetPackageZMTC.this) {
                                try {
                                    // TODO!! get his from user
                                    AudioFileFormat.Type format = AudioUtilities.defaultAudioFormat;
                                    createAndSavePresetPackage(pc, presetIndexes, nameField.getText(), notesField.getText(), incSamples.isSelected(), format, incMaster.isSelected(), incMM.isSelected());
                                } catch (CommandFailedException e1) {
                                    err = e1;
                                } finally {
                                    done = true;
                                    NewPresetPackageZMTC.this.notify();
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
