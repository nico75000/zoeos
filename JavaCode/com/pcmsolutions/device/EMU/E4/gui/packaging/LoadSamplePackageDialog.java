package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.FuzzyLineBorder;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.preferences.ZBoolPref;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
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
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 29-Apr-2004
 * Time: 07:31:03
 */
class LoadSamplePackageDialog extends ZDialog {
    public static final ZBoolPref ZPREF_addLoadedSamplesToContextFilter = new Impl_ZBoolPref(Preferences.userNodeForPackage(PackageFactory.class.getClass()), "addLoadedSamplesToContextFilter", true);

    private JButton resetButton;
    private JButton cancelButton;
    private JList targetLocationList;
    private JList baseLocationList;
    private JButton proceedButton;
    private JPanel packageHeaderPanel;
    private JCheckBox useEmptyCheck;
    private JPanel mainPanel;
    private JScrollPane baseLocationScroll;
    private JScrollPane targetLocationScroll;
    private JTextField statusTextField;
    private JCheckBox addToFilterCheck;

    public LoadSamplePackageDialog(PackageFactory.SamplePackageManifest pkg, ContextEditableSample sample) throws DeviceException {
        super(ZoeosFrame.getInstance(), "Load sample package finalizer", true);
        this.pkgManifest = pkg;
        this.baseSample = sample;
        init();
        setupComponents();
        updateTargets();
        ActionListener cancelAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                srcIndex2DestIndexMap = null;
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

    ContextEditableSample baseSample;
    ContextEditableSample[] contextSamples;
    Integer[] destIndexes;
    Map<Integer, Integer> srcIndex2DestIndexMap;
    private PackageFactory.SamplePackageManifest pkgManifest;

    public Map<Integer, Integer> getSrcIndex2DestIndexMap() {
        return srcIndex2DestIndexMap;
    }

    public Integer[] getDestIndexes() {
        return destIndexes;
    }

    void init() throws DeviceException {
        java.util.List<ContextEditableSample> samples = baseSample.getSampleContext().getContextEditableSamples();
        contextSamples = samples.toArray(new ContextEditableSample[samples.size()]);
        srcIndex2DestIndexMap = new HashMap<Integer, Integer>();
        Integer[] srcIndexes = pkgManifest.getSourceIndexes();
        // initialize index2IndexMap
        for (int i = 0; i < srcIndexes.length; i++)
            srcIndex2DestIndexMap.put(srcIndexes[i], srcIndexes[i]);
    }

    private static final int UNIT_SCROLL = 30;
    private static final int BLOCK_SCROLL = 60;

    void setupComponents() {
        packageHeaderPanel.setLayout(new BorderLayout());
        packageHeaderPanel.setBorder(new CompoundBorder(new TitledBorder("Package Header"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
        packageHeaderPanel.add(new SamplePackageHeaderInfoTable(pkgManifest.getSamplePackage().getHeader()), BorderLayout.CENTER);
        baseLocationList.setListData(contextSamples);
        baseLocationList.setSelectedValue(baseSample, true);
        baseLocationList.setFixedCellHeight(UIColors.getTableRowHeight());
        baseLocationList.setBackground(UIColors.getDefaultBG());
        baseLocationList.setCellRenderer(new SampleContextTableCellRenderer());
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
        // statusTextField.setBorder(new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true));
        statusTextField.setBorder(new LineBorder(UIColors.getTableFirstSectionBG(), 3));
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
        addToFilterCheck.setSelected(ZPREF_addLoadedSamplesToContextFilter.getValue());
        addToFilterCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZPREF_addLoadedSamplesToContextFilter.putValue(addToFilterCheck.isSelected());
                //updateTargets();
            }
        });
    }

    static final DecimalFormat df = new DecimalFormat("0000");

    private static final String LOADING = "LOADING [";
    private static final String NOT_LOADING = "Cannot perform load";

    void setStatus(boolean valid) {
        if (valid)
            try {
                StringBuffer sb = new StringBuffer(LOADING);
                sb.append(destIndexes.length);
                sb.append (destIndexes.length > 0 ? " samples at " + df.format(destIndexes[0]) : " samples");
                sb.append( ", ");
                sb.append( (destIndexes.length - SampleContextMacros.numEmpties(baseSample.getSampleContext(), destIndexes)));
                sb.append(" overwrites");

                int missing = pkgManifest.getMissingSamples().length;
                if (missing > 0) {
                    sb.append(" (");
                    sb.append(missing);
                    sb.append(" missing) ]");
                } else
                    sb.append(" ]");
                statusTextField.setText(sb.toString());
            } catch (Exception e) {
                statusTextField.setText("Error");
            }
        else
            statusTextField.setText(NOT_LOADING);

        proceedButton.setEnabled(valid && pkgManifest.getNonNullValidFileCount() > 0);
        resetButton.setEnabled(pkgManifest.getNonNullValidFileCount() > 0);
    }

    int getUserSampleLoadCount() {
        return pkgManifest.getNonNullValidFileCount();
    }

    void updateTargets() {
        ArrayList targets = new ArrayList();
        if (!useEmptyCheck.isSelected()) {
            int si = Arrays.asList(contextSamples).indexOf(baseSample);
            destIndexes = new Integer[pkgManifest.getNonNullValidFileCount()];
            if (si + destIndexes.length >= contextSamples.length)
                setStatus(false);
            else {
                for (int i = 0; i < destIndexes.length; i++) {
                    destIndexes[i] = contextSamples[si + i].getIndex();
                    targets.add(contextSamples[si + i]);
                }
                setStatus(true);
                updateIndex2IndexMap(srcIndex2DestIndexMap, pkgManifest.getFile2SrcIndexMap(), destIndexes, pkgManifest.getNonNullValidFiles());
            }

        } else {
            java.util.List emptyList = SampleContextMacros.findEmptySamples(contextSamples, Arrays.asList(contextSamples).indexOf(baseSample));
            if (emptyList.size() < pkgManifest.getNonNullValidFileCount()) {
                setStatus(false);
            } else {
                emptyList = emptyList.subList(0, pkgManifest.getNonNullValidFileCount());
                destIndexes = SampleContextMacros.extractSampleIndexes((ContextEditableSample[]) emptyList.toArray(new ContextEditableSample[emptyList.size()]));
                targets.addAll(emptyList);
                setStatus(true);
                updateIndex2IndexMap(srcIndex2DestIndexMap, pkgManifest.getFile2SrcIndexMap(), destIndexes, pkgManifest.getNonNullValidFiles());
            }
        }
        targetLocationList.setListData(targets.toArray());
    }

    static void updateIndex2IndexMap(Map index2IndexMap, Map file2IndexMap, Integer[] newIndexes, File[] files) {
        for (int i = 0; i < files.length; i++) {
            Integer ind = (Integer) file2IndexMap.get(files[i]);
            if (ind == null)
                throw new IllegalArgumentException("missing index mapping");
            index2IndexMap.put(ind, newIndexes[i]);
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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        packageHeaderPanel = new JPanel();
        packageHeaderPanel.setEnabled(false);
        mainPanel.add(packageHeaderPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(250, 150), null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        statusTextField = new JTextField();
        statusTextField.setEditable(false);
        statusTextField.setEnabled(true);
        panel1.add(statusTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));
        useEmptyCheck = new JCheckBox();
        useEmptyCheck.setText("Search for and only use empty samples");
        useEmptyCheck.setToolTipText("r");
        mainPanel.add(useEmptyCheck, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(3, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
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
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        baseLocationScroll = new JScrollPane();
        baseLocationScroll.setHorizontalScrollBarPolicy(31);
        baseLocationScroll.setVerticalScrollBarPolicy(22);
        panel3.add(baseLocationScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, -1), null));
        baseLocationList = new JList();
        baseLocationList.setSelectionMode(0);
        baseLocationScroll.setViewportView(baseLocationList);
        targetLocationScroll = new JScrollPane();
        targetLocationScroll.setHorizontalScrollBarPolicy(31);
        targetLocationScroll.setVerticalScrollBarPolicy(22);
        panel3.add(targetLocationScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, -1), null));
        targetLocationList = new JList();
        targetLocationList.setEnabled(false);
        targetLocationList.setSelectionMode(0);
        targetLocationScroll.setViewportView(targetLocationList);
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(5, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        addToFilterCheck = new JCheckBox();
        addToFilterCheck.setText("Add loaded samples to sample filter");
        mainPanel.add(addToFilterCheck, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
    }
}
