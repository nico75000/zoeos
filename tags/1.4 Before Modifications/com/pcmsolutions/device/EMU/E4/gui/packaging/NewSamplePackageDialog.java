package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.packaging.PackageGenerationException;
import com.pcmsolutions.device.EMU.E4.packaging.SamplePackage;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.zcommands.sample.NewSamplePackageZMTC;
import com.pcmsolutions.gui.*;
import com.pcmsolutions.gui.audio.AudioFormatAccessoryPanel;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.audio.ZAudioSystem;
import com.pcmsolutions.system.callback.ShowCommandFailedCallback;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZStringPref;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 07-May-2004
 * Time: 23:19:15
 */
class NewSamplePackageDialog extends ZDialog {
    private JTextField packageNotesField;
    private JTextField packageNameField;
    private JButton cancelButton;
    private JButton createButton;
    private JList incSamplesList;
    private JPanel mainPanel;
    private JTextField packageSummaryField;

    protected ContextEditableSample[] samples;
    private JScrollPane incSamplesScroll;

    private static JFileChooser fc;
    private static final Preferences prefs = Preferences.userNodeForPackage(NewSamplePackageZMTC.class.getClass());
    public static final ZStringPref ZPREF_lastDir = new Impl_ZStringPref(prefs, "lastSaveSamplePackageDir", Zoeos.getHomeDir().getAbsolutePath());
    private static AudioFormatAccessoryPanel afap;

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

    public NewSamplePackageDialog(final ContextEditableSample[] samples, String suggestedName) throws DeviceException {
        super(ZoeosFrame.getInstance(), "Sample packager", true);
        //Arrays.sort(samples);
        this.samples = SampleContextMacros.getFreshCopies(samples);

        incSamplesList.setListData(this.samples);
        incSamplesList.setBackground(UIColors.getDefaultBG());
        incSamplesList.setCellRenderer(new SampleContextTableCellRenderer());
        incSamplesList.setEnabled(false);
        incSamplesList.setFixedCellHeight(UIColors.getTableRowHeight());
        packageSummaryField.setText(samples.length + (samples.length > 1 ? " samples, total size " : " sample, size ") + AudioUtilities.getFormattedSize(SampleContextMacros.getTotalSizeInBytes(samples)));
        packageSummaryField.setBorder(new LineBorder(UIColors.getTableFirstSectionBG(), 3));
        incSamplesScroll.setBorder(new CompoundBorder(new TitledBorder("Package samples"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
        incSamplesScroll.getVerticalScrollBar().setUnitIncrement(UNIT_SCROLL);
        incSamplesScroll.getVerticalScrollBar().setBlockIncrement(BLOCK_SCROLL);
        packageNameField.setText(suggestedName);
        packageNameField.selectAll();
        packageNameField.requestFocusInWindow();
        //audioFormatParentPanel.setLayout(new BorderLayout());
        // audioFormatParentPanel.add(new AudioFormatAccessoryPanel("Audio format"));
        ActionListener cancelAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        ActionListener createAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    samples[0].getDeviceContext().getQueues().presetContextQ().getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
                            createAndSaveSamplePackage(samples[0].getSampleContext(), SampleContextMacros.extractSampleIndexes(samples), packageNameField.getText(), packageNotesField.getText());
                        }
                    }, "New sample package").post(ShowCommandFailedCallback.INSTANCE);
                } catch (ResourceUnavailableException e1) {
                    UserMessaging.showCommandFailed(e1.getMessage());
                }
                dispose();
            }
        };
        createButton.addActionListener(createAction);
        cancelButton.addActionListener(cancelAction);
        setContentPane(mainPanel);
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
    }

    private static final int UNIT_SCROLL = 30;
    private static final int BLOCK_SCROLL = 60;

    private void createAndSaveSamplePackage(SampleContext sc, Integer[] indexes, String name, String notes) throws CommandFailedException, PackageGenerationException {
        try {
            if (!sc.getDeviceContext().isSmdiCoupled())
                throw new CommandFailedException("Cannot retrieve samples for the package- the device is not SMDI coupled.");
        } catch (DeviceException e) {
            throw new CommandFailedException(e.getMessage());
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
        SamplePackage pkg = null;
        try {
            pkg = PackageFactory.createSamplePackage(sc, indexes, name, notes, null, ZAudioSystem.getDefaultAudioType());
        } catch (PackageGenerationException e) {
            throw new CommandFailedException("Error saving sample package: " + e.getMessage());
        }
        if (extFile != null) {
            final SamplePackage f_pkg = pkg;
            final File f_extFile = extFile;
            ProgressCallbackTree prog = new ProgressCallbackTree("Writing sample package", false);
            try {
                PackageFactory.saveSamplePackage(f_pkg, f_extFile, prog);
            } finally {
                prog.updateProgress(1);
            }
        }
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// !!! IMPORTANT !!!
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * !!! IMPORTANT !!!
     * DO NOT edit this method OR call it in your code!
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        incSamplesScroll = new JScrollPane();
        incSamplesScroll.setVerticalScrollBarPolicy(22);
        mainPanel.add(incSamplesScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, 150), null));
        incSamplesList = new JList();
        incSamplesList.setEnabled(true);
        incSamplesList.setSelectionMode(0);
        incSamplesList.setToolTipText("Sample to be included in this package");
        incSamplesScroll.setViewportView(incSamplesList);
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setToolTipText("Cancel creation of the package");
        panel1.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        createButton = new JButton();
        createButton.setText("Create");
        createButton.setToolTipText("Create and save the preset package");
        panel1.add(createButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        packageSummaryField = new JTextField();
        packageSummaryField.setEditable(false);
        packageSummaryField.setEnabled(true);
        packageSummaryField.setToolTipText("Included sample summary");
        mainPanel.add(packageSummaryField, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Name:");
        panel2.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        packageNameField = new JTextField();
        packageNameField.setToolTipText("Name for this package");
        panel2.add(packageNameField, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));
        final JLabel label2 = new JLabel();
        label2.setText("Notes:");
        panel2.add(label2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        packageNotesField = new JTextField();
        packageNotesField.setToolTipText("Notes associated with this package");
        panel2.add(packageNotesField, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
    }
}
