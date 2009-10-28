package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.zcommands.sample.LoadContextSamplesZMTC;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.FuzzyLineBorder;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.audio.AudioUtilities;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * User: paulmeehan
 * Date: 30-Apr-2004
 * Time: 16:02:03
 */
class LoadContextSamplesDialog extends ZDialog {
    private JScrollPane targetLocationScroll;
    private JScrollPane baseLocationScroll;
    private JList targetLocationList;
    private JList baseLocationList;
    private JButton cancelButton;
    private JButton resetButton;
    private JButton proceedButton;
    private JTextField statusTextField;
    private JPanel mainPanel;
    private JCheckBox stripAppendages;
    private JCheckBox useEmptyCheck;
    private JCheckBox addToFilterCheck;

    public LoadContextSamplesDialog(ContextEditableSample sample, final File[] files) throws HeadlessException, DeviceException {
        super(ZoeosFrame.getInstance(), (files.length > 1 ? "Load samples finalizer" : "Load sample finalizer"), true);
        this.baseSample = sample;
        this.files = files;
        init();
        setupComponents();
        updateTargets();

        ActionListener cancelAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                targetIndexes = null;
                dispose();
            }
        };
        ActionListener proceedAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        ActionListener resetAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                baseSample = contextSamples[0];
                baseLocationList.setSelectedValue(baseSample, true);
                updateTargets();
            }
        };
        proceedButton.addActionListener(proceedAction);
        cancelButton.addActionListener(cancelAction);
        resetButton.addActionListener(resetAction);

        setContentPane(mainPanel);
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
    }

    void init() throws DeviceException {
        java.util.List<ContextEditableSample> samples = baseSample.getSampleContext().getContextEditableSamples();
        contextSamples = samples.toArray(new ContextEditableSample[samples.size()]);
    }

    public Integer[] getTargetIndexes() {
        return targetIndexes;
    }

    private static final int UNIT_SCROLL = 30;
    private static final int BLOCK_SCROLL = 60;

    void setupComponents() {
        baseLocationList.setListData(contextSamples);
        baseLocationList.setSelectedValue(baseSample, true);
        baseLocationList.setBackground(UIColors.getDefaultBG());
        baseLocationList.setCellRenderer(new SampleContextTableCellRenderer());
        baseLocationList.setFixedCellHeight(UIColors.getTableRowHeight());
        baseLocationList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                baseSample = (ContextEditableSample) baseLocationList.getSelectedValue();
                updateTargets();
            }
        });
        baseLocationScroll.setBorder(new CompoundBorder(new TitledBorder("Base location"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
        baseLocationScroll.getVerticalScrollBar().setUnitIncrement(UNIT_SCROLL);
        baseLocationScroll.getVerticalScrollBar().setBlockIncrement(BLOCK_SCROLL);
        targetLocationScroll.getVerticalScrollBar().setUnitIncrement(UNIT_SCROLL);
        targetLocationScroll.getVerticalScrollBar().setBlockIncrement(BLOCK_SCROLL);
        //statusTextField.setBorder(new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true));
        targetLocationList.setCellRenderer(new SampleContextTableCellRenderer());
        targetLocationList.setBackground(UIColors.getDefaultBG());
        targetLocationList.setFixedCellHeight(UIColors.getTableRowHeight());
        targetLocationScroll.setBorder(new CompoundBorder(new TitledBorder("Target location(s)"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
        useEmptyCheck.setSelected(PackageFactory.ZPREF_serachForAndOnlyUseEmptySamples.getValue());
        useEmptyCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PackageFactory.ZPREF_serachForAndOnlyUseEmptySamples.putValue(useEmptyCheck.isSelected());
                updateTargets();
            }
        });
        stripAppendages.setSelected(LoadContextSamplesZMTC.stripSampleAppendages.getValue());
        stripAppendages.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoadContextSamplesZMTC.stripSampleAppendages.putValue(stripAppendages.isSelected());
            }
        });
        int mc = ZUtilities.getFileCountForPattern(files, AudioUtilities.sampleIndexPattern);
        if (mc == 0)
            stripAppendages.setEnabled(false);

        addToFilterCheck.setSelected(LoadSamplePackageDialog.ZPREF_addLoadedSamplesToContextFilter.getValue());
        addToFilterCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoadSamplePackageDialog.ZPREF_addLoadedSamplesToContextFilter.putValue(addToFilterCheck.isSelected());
            }
        });
    }

    static final DecimalFormat df = new DecimalFormat("0000");
    private static final String READY = "LOADING ";
    private static final String NOT_READY = "Cannot perform load";

    void setStatus(boolean valid) {
        if (valid)
            try {
                statusTextField.setText(READY + targetIndexes.length + " at " + df.format(targetIndexes[0]) + "  (" + (targetIndexes.length - SampleContextMacros.numEmpties(baseSample.getSampleContext(), targetIndexes)) + " overwrites)");
            } catch (Exception e) {
                statusTextField.setText(READY + targetIndexes.length + " at " + df.format(targetIndexes[0]));
            }
        else
            statusTextField.setText(NOT_READY);

        proceedButton.setEnabled(valid);
    }

    void updateTargets() {
        ArrayList targets = new ArrayList();
        if (!useEmptyCheck.isSelected()) {
            int si = Arrays.asList(contextSamples).indexOf(baseSample);
            targetIndexes = new Integer[files.length];
            if (si + targetIndexes.length >= contextSamples.length)
                setStatus(false);
            else {
                for (int i = 0; i < targetIndexes.length; i++) {
                    targetIndexes[i] = contextSamples[si + i].getIndex();
                    targets.add(contextSamples[si + i]);
                }
                setStatus(true);
            }

        } else {
            java.util.List emptyList = SampleContextMacros.findEmptySamples(contextSamples, Arrays.asList(contextSamples).indexOf(baseSample));
            if (emptyList.size() < files.length) {
                setStatus(false);
            } else {
                emptyList = emptyList.subList(0, files.length);
                targetIndexes = SampleContextMacros.extractSampleIndexes((ContextEditableSample[]) emptyList.toArray(new ContextEditableSample[emptyList.size()]));
                targets.addAll(emptyList);
                setStatus(true);
            }
        }
        targetLocationList.setListData(targets.toArray());
    }

    ContextEditableSample baseSample;
    ContextEditableSample[] contextSamples;
    Integer[] targetIndexes;
    File[] files;

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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 4, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        baseLocationScroll = new JScrollPane();
        baseLocationScroll.setHorizontalScrollBarPolicy(31);
        baseLocationScroll.setVerticalScrollBarPolicy(22);
        panel1.add(baseLocationScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, -1), null));
        baseLocationList = new JList();
        baseLocationList.setSelectionMode(0);
        baseLocationScroll.setViewportView(baseLocationList);
        targetLocationScroll = new JScrollPane();
        targetLocationScroll.setHorizontalScrollBarPolicy(31);
        targetLocationScroll.setVerticalScrollBarPolicy(22);
        panel1.add(targetLocationScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, -1), null));
        targetLocationList = new JList();
        targetLocationList.setEnabled(false);
        targetLocationList.setSelectionMode(0);
        targetLocationScroll.setViewportView(targetLocationList);
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        statusTextField = new JTextField();
        statusTextField.setEditable(false);
        statusTextField.setEnabled(true);
        mainPanel.add(statusTextField, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        proceedButton = new JButton();
        proceedButton.setText("Proceed");
        panel2.add(proceedButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        resetButton = new JButton();
        resetButton.setText("Reset");
        panel2.add(resetButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        cancelButton = new JButton();
        cancelButton.setHorizontalTextPosition(2);
        cancelButton.setText("Cancel");
        panel2.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        stripAppendages = new JCheckBox();
        stripAppendages.setText("Strip sample name appendages");
        stripAppendages.setToolTipText("r");
        panel3.add(stripAppendages, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        addToFilterCheck = new JCheckBox();
        addToFilterCheck.setText("Add loaded samples to sample filter");
        panel3.add(addToFilterCheck, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        useEmptyCheck = new JCheckBox();
        useEmptyCheck.setText("Search for and only use empties");
        useEmptyCheck.setToolTipText("r");
        mainPanel.add(useEmptyCheck, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
    }
}
