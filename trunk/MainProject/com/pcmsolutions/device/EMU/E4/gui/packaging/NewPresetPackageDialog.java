package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.packaging.PackageGenerationException;
import com.pcmsolutions.device.EMU.E4.packaging.PresetPackage;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.gui.*;
import com.pcmsolutions.gui.audio.AudioFormatAccessoryPanel;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.audio.ZAudioSystem;
import com.pcmsolutions.system.callback.ShowCommandFailedCallback;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZBoolPref;
import com.pcmsolutions.system.preferences.ZStringPref;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;
import com.pcmsolutions.system.threads.Impl_ZThread;
import com.pcmsolutions.util.IntegerUseMap;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 09-May-2004
 * Time: 14:46:06
 */
class NewPresetPackageDialog extends ZDialog {

    private static final Preferences prefs = Preferences.userNodeForPackage(NewPresetPackageDialog.class.getClass());
    ZBoolPref ZPREF_incMaster = new Impl_ZBoolPref(prefs, "incMasterSettings", false);
    ZBoolPref ZPREF_incMultimode = new Impl_ZBoolPref(prefs, "incMultimodeSettings", false);
    ZBoolPref ZPREF_deepPackage = new Impl_ZBoolPref(prefs, "deepPackage", true);
    ZBoolPref ZPREF_incSamples = new Impl_ZBoolPref(prefs, "incSamples", true);

    private JPanel mainPanel;
    private JScrollPane incSamplesScroll;
    private JList incSamplesList;
    private JScrollPane incPresetsScroll;
    private JList incPresetsList;
    private JButton createButton;
    private JButton cancelButton;
    private JTextField packageNotesField;
    private JTextField packageNameField;

    private static JFileChooser fc;
    public static final ZStringPref ZPREF_lastDir = new Impl_ZStringPref(Preferences.userNodeForPackage(NewPresetPackageDialog.class.getClass()), "lastPresetPackageDir", Zoeos.getHomeDir().getAbsolutePath());
    private JCheckBox incMasterCheck;
    private JCheckBox incMultimodeCheck;
    private JTextField packageSampleSummaryField;
    private JTextField packagePresetSummaryField;
    private JCheckBox incLinksCheck;
    private JCheckBox incSamplesCheck;
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

    private static final int UNIT_SCROLL = 30;
    private static final int BLOCK_SCROLL = 60;

    ReadablePreset[] topLevelPresets;
    ReadablePreset[] incPresets;
    ReadableSample[] refSamples;
    ReadableSample[] incSamples;
    PresetContext pc;

    Impl_ZThread evalThread = null;

    public NewPresetPackageDialog(final ReadablePreset[] presets, String suggestedName) {
        super(ZoeosFrame.getInstance(), "Preset packager", true);
        topLevelPresets = presets;
        pc = presets[0].getPresetContext();

        setupComponents(suggestedName);
        setupActions();

        setContentPane(mainPanel);
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        this.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
                evaluatePackage();
            }

            public void componentHidden(ComponentEvent e) {
            }
        });
    }

    private void setupComponents(String suggestedName) {
        incPresetsList.setBackground(UIColors.getDefaultBG());
        incPresetsList.setCellRenderer(new SampleContextTableCellRenderer());
        incPresetsList.setEnabled(false);
        incPresetsList.setFixedCellHeight(UIColors.getTableRowHeight());

        incSamplesList.setBackground(UIColors.getDefaultBG());
        incSamplesList.setCellRenderer(new SampleContextTableCellRenderer());
        incSamplesList.setEnabled(false);
        incSamplesList.setFixedCellHeight(UIColors.getTableRowHeight());

        packagePresetSummaryField.setBorder(new LineBorder(UIColors.getTableFirstSectionBG(), 3));
        packageSampleSummaryField.setBorder(new LineBorder(UIColors.getTableFirstSectionBG(), 3));

        incPresetsScroll.setBorder(new CompoundBorder(new TitledBorder("Package presets"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
        incPresetsScroll.getVerticalScrollBar().setUnitIncrement(UNIT_SCROLL);
        incPresetsScroll.getVerticalScrollBar().setBlockIncrement(BLOCK_SCROLL);

        incSamplesScroll.setBorder(new CompoundBorder(new TitledBorder("Package samples"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
        incSamplesScroll.getVerticalScrollBar().setUnitIncrement(UNIT_SCROLL);
        incSamplesScroll.getVerticalScrollBar().setBlockIncrement(BLOCK_SCROLL);

        packageNameField.setText(suggestedName);
        packageNameField.selectAll();
        //packageNameField.requestFocusInWindow();

        incLinksCheck.setSelected(ZPREF_deepPackage.getValue());
        incMultimodeCheck.setSelected(ZPREF_incMultimode.getValue());
        incMasterCheck.setSelected(ZPREF_incMaster.getValue());
        incSamplesCheck.setSelected(ZPREF_incSamples.getValue());
    }

    private void setupActions() {
        ActionListener cancelAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        cancelButton.addActionListener(cancelAction);

        ActionListener createAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    topLevelPresets[0].getDeviceContext().getQueues().presetContextQ().getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
                            createAndSavePresetPackage();
                        }
                    }, "New preset package").post(ShowCommandFailedCallback.INSTANCE);
                } catch (ResourceUnavailableException e1) {
                    UserMessaging.showCommandFailed(e1.getMessage());
                }
                dispose();
            }
        };
        createButton.addActionListener(createAction);

        incMasterCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZPREF_incMaster.putValue(incMasterCheck.isSelected());
            }
        });
        incMultimodeCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZPREF_incMultimode.putValue(incMultimodeCheck.isSelected());
            }
        });
        incSamplesCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZPREF_incSamples.putValue(incSamplesCheck.isSelected());
                evaluatePackage();
            }
        });
        incLinksCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZPREF_deepPackage.putValue(incLinksCheck.isSelected());
                evaluatePackage();
            }
        });
    }

    public void dispose() {
        if (evalThread != null)
            evalThread.stopThreadSafely();
        super.dispose();    //To change body of overridden methods use File | Settings | File Templates.
    }

    void evaluatePackage() {
        packagePresetSummaryField.setText("...WAIT...evaluating presets");
        packageSampleSummaryField.setText("...WAIT...evaluating samples");

        createButton.setEnabled(false);
        incMasterCheck.setEnabled(false);
        incMultimodeCheck.setEnabled(false);
        incLinksCheck.setEnabled(false);
        incSamplesCheck.setEnabled(false);
        packageNameField.setEnabled(false);
        packageNotesField.setEnabled(false);

        final Integer[] presetIndexes = PresetContextMacros.extractPresetIndexes(topLevelPresets);
        incSamples = new ReadableSample[0];
        evalThread = new Impl_ZThread() {
            public void runBody() {
                try {
                    Integer[] pset = PackageFactory.evaluatePresetsForPackage(presetIndexes, ZPREF_deepPackage.getValue(), pc, ProgressCallback.DUMMY);
                    if (!shouldRun)
                        return;
                    incPresets = PresetContextMacros.getReadablePresets(pc, pset);
                    IntegerUseMap s_map = PresetContextMacros.getSampleUsage(pc, pset);
                    s_map.removeIntegerReference(IntPool.get(0));
                    refSamples = SampleContextMacros.getReadablePresets(pc.getRootSampleContext(), s_map.getIntegers());
                    if (ZPREF_incSamples.getValue())
                        incSamples = refSamples;
                    else
                        incSamples = SampleContextMacros.extractRomSamples(refSamples);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (NewPresetPackageDialog.this.isVisible()) {
                                incPresetsList.setListData(incPresets);
                                incSamplesList.setListData(incSamples);
                                if (SampleContextMacros.userSampleCount(refSamples) == 0)
                                    incSamplesCheck.setEnabled(false);
                                else
                                    incSamplesCheck.setEnabled(true);

                                packagePresetSummaryField.setText(incPresets.length + (incPresets.length > 1 ? " presets" : " preset"));
                                packageSampleSummaryField.setText(incSamples.length + (incSamples.length == 1 ? " sample, retrieval size " : " samples, retrieval size ") + AudioUtilities.getFormattedSize(SampleContextMacros.getTotalSizeInBytes(SampleContextMacros.extractContextEditableSamples(incSamples))));

                                createButton.setEnabled(true);
                                incMasterCheck.setEnabled(true);
                                incMultimodeCheck.setEnabled(true);
                                try {
                                    incLinksCheck.setEnabled(!PresetContextMacros.noLinksInPresets(topLevelPresets));
                                } catch (PresetException e) {
                                    e.printStackTrace();
                                }

                                //incSamplesCheck.setEnabled(true);
                                packageNameField.setEnabled(true);
                                packageNotesField.setEnabled(true);
                            }
                        }
                    });
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handleCriticalError();
            }
        };
        evalThread.start();
    }

    private void createAndSavePresetPackage() throws PackageGenerationException {
        PresetPackage pkg = null;
        //ProgressCallbackTree prog = new ProgressCallbackTree("Creating preset package", false);
        //ProgressCallback[] progs = prog.splitTask(2, false);
        File extFile = null;
        synchronized (this.getClass()) {
            assertChooser();
            fc.setSelectedFile(new File(packageNameField.getText()));
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
            ProgressSession sess = ZoeosFrame.getInstance().getProgressSession("Creating preset package", 100, false);
            sess.setIndeterminate(true);
            try {
                pkg = PackageFactory.createPresetPackage(pc, PresetContextMacros.extractPresetIndexes(incPresets), packageNameField.getText(), packageNotesField.getText(), ZPREF_incMaster.getValue(), ZPREF_incMultimode.getValue(), incSamplesCheck.isEnabled() && ZPREF_incSamples.getValue(), ZAudioSystem.getDefaultAudioType(), null);
            } finally {
                sess.end();
            }
            PackageFactory.savePresetPackage(pkg, extFile, new ProgressCallbackTree("Saving preset package", false).splitTask(1, false)[0]);
        }
    }

    void handleCriticalError() {
        UserMessaging.showError("Error evaluating the preset package");
        dispose();
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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(9, 4, new Insets(0, 0, 0, 0), -1, -1));
        incPresetsScroll = new JScrollPane();
        incPresetsScroll.setVerticalScrollBarPolicy(22);
        mainPanel.add(incPresetsScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, 150), null));
        incPresetsList = new JList();
        incPresetsList.setEnabled(true);
        incPresetsList.setSelectionMode(0);
        incPresetsList.setToolTipText("Presets to be included in this package");
        incPresetsScroll.setViewportView(incPresetsList);
        incSamplesScroll = new JScrollPane();
        incSamplesScroll.setVerticalScrollBarPolicy(22);
        mainPanel.add(incSamplesScroll, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, 150), null));
        incSamplesList = new JList();
        incSamplesList.setEnabled(true);
        incSamplesList.setSelectionMode(0);
        incSamplesList.setToolTipText("Samples to be included in this package");
        incSamplesScroll.setViewportView(incSamplesList);
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        packageSampleSummaryField = new JTextField();
        packageSampleSummaryField.setEditable(false);
        packageSampleSummaryField.setEnabled(true);
        packageSampleSummaryField.setToolTipText("Summary of included samples");
        mainPanel.add(packageSampleSummaryField, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(6, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        packagePresetSummaryField = new JTextField();
        packagePresetSummaryField.setEditable(false);
        packagePresetSummaryField.setToolTipText("Summary of included presets");
        mainPanel.add(packagePresetSummaryField, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(8, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setToolTipText("Cancel creation of the package");
        panel1.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        createButton = new JButton();
        createButton.setText("Create");
        createButton.setToolTipText("Create and save the preset package");
        panel1.add(createButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
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
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(5, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        incMasterCheck = new JCheckBox();
        incMasterCheck.setText("Include master");
        incMasterCheck.setToolTipText("Include current master settings with this package");
        panel3.add(incMasterCheck, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        incMultimodeCheck = new JCheckBox();
        incMultimodeCheck.setText("Include multimode");
        incMultimodeCheck.setToolTipText("Include current multimode settings with this package");
        panel3.add(incMultimodeCheck, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        incLinksCheck = new JCheckBox();
        incLinksCheck.setText("Include links");
        incLinksCheck.setToolTipText("Include linked presets in this package");
        panel3.add(incLinksCheck, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        incSamplesCheck = new JCheckBox();
        incSamplesCheck.setText("Include user samples");
        incSamplesCheck.setToolTipText("Include user samples in the package");
        panel3.add(incSamplesCheck, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer7 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer7, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer8 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer8, new com.intellij.uiDesigner.core.GridConstraints(4, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer9 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer9, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer10 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer10, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null));
    }
}
