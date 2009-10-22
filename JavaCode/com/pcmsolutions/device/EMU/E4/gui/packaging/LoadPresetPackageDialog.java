package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext.PresetContextTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.master.MasterContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ContextReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.gui.*;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.callback.ShowCommandFailedCallback;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;
import com.pcmsolutions.system.preferences.ZBoolPref;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

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
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 09-May-2004
 * Time: 14:29:31
 */
class LoadPresetPackageDialog extends ZDialog {
    public static final ZBoolPref ZPREF_addLoadedPresetsToContextFilter = new Impl_ZBoolPref(Preferences.userNodeForPackage(PackageFactory.class.getClass()), "addLoadedPresetsToContextFilter", true);

    private JPanel mainPanel;
    private JPanel packageHeaderPanel;
    private JButton cancelButton;
    private JButton resetButton;
    private JButton proceedButton;
    private JTextField statusTextField;

    private JList targetPresetLocationList;
    private JList baseSampleLocationList;
    private JList targetSampleLocationList;
    private JList basePresetLocationList;
    private JScrollPane basePresetLocationScroll;
    private JScrollPane targetPresetLocationScroll;
    private JScrollPane baseSampleLocationScroll;
    private JScrollPane targetSampleLocationScroll;
    private JCheckBox applyMultimodeCheck;
    private JCheckBox applyMasterCheck;
    private JCheckBox zeroMissingCheck;
    private JCheckBox zeroBrokenRomCheck;
    private JCheckBox useEmptyPresetsCheck;
    private JCheckBox useEmptySamplesCheck;
    private JCheckBox addToFilterCheck;

    PackageFactory.PresetPackageManifest ppkgManifest;
    ContextEditablePreset[] contextPresets;
    ContextEditablePreset basePreset;
    ContextEditableSample[] contextSamples;
    ContextEditableSample baseSample;

    Integer[] destPresetIndexes;
    Integer[] destSampleIndexes;
    Integer[] srcSampleIndexes;
    Map<Integer, Integer> sampleSrcIndex2DestIndexMap;
    Integer[] missingUserSampleSrcIndexes;
    Integer[] brokenRomSampleIndexes;
    Integer[] emptyUserSampleIndexes;

    public LoadPresetPackageDialog(PackageFactory.PresetPackageManifest pkg, ContextEditablePreset basePreset) throws DeviceException {
        super(ZoeosFrame.getInstance(), "Load preset package finalizer", true);
        this.ppkgManifest = pkg;
        this.basePreset = basePreset;
        init();
        setupComponents();
        setupActions();
        setContentPane(mainPanel);
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        updateTargets();
        pack();
    }

    private void setupActions() {
        ActionListener cancelAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        ActionListener proceedAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    basePreset.getDeviceContext().getQueues().generalQ().getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
                            try {
                                finalizeSampleIndexMap();

                                int nonEmptySampleCount = 0;
                                int nonEmptyPresetCount = 0;

                                if (sampleSrcIndex2DestIndexMap != null)
                                    for (Integer index : sampleSrcIndex2DestIndexMap.values())
                                        if ( index.intValue() < DeviceContext.BASE_ROM_SAMPLE && !baseSample.getSampleContext().isEmpty(index))
                                            nonEmptySampleCount++;

                                if (destPresetIndexes != null)
                                    for (Integer index : destPresetIndexes)
                                        if (!basePreset.getPresetContext().isEmpty(index))
                                            nonEmptyPresetCount++;

                                if (nonEmptySampleCount > 0 || nonEmptyPresetCount > 0) {
                                    if (nonEmptySampleCount == 0) {
                                        if (!UserMessaging.askYesNo(nonEmptyPresetCount + " preset" + (nonEmptyPresetCount != 1 ? "s" : "") + " will be overwritten. Continue?"))
                                            return;
                                    } else if (nonEmptyPresetCount == 0) {
                                        if (!UserMessaging.askYesNo(nonEmptySampleCount + " sample" + (nonEmptySampleCount != 1 ? "s" : "") + " will be overwritten. Continue?"))
                                            return;
                                    } else {
                                        if (!UserMessaging.askYesNo(nonEmptyPresetCount + " preset" + (nonEmptyPresetCount != 1 ? "s" : "")
                                                + " and " + nonEmptySampleCount + " sample" + (nonEmptySampleCount != 1 ? "s" : "")
                                                + " will be overwritten. Continue?"))
                                            return;
                                    }
                                }

                                if (getUserSampleLoadCount() > 0) {
                                    if (basePreset.getDeviceContext().isSmdiCoupled()) {
                                        final ProgressCallbackTree sampleProg = new ProgressCallbackTree("Sample upload", false);
                                        SampleContextMacros.loadSamplesToContext(ppkgManifest.getSamplePackageManifest().getNonNullValidFiles(), destSampleIndexes, baseSample.getSampleContext(), sampleProg, true);
                                    } else {
                                        if (!UserMessaging.askYesNo("<html>This preset package contains samples that need to be uploaded - but the device is not SMDI coupled.<br><br>Do you want to load the presets anyway (user sample indexes will be set to zero)?</html>"))
                                            return;
                                        for (Iterator i = sampleSrcIndex2DestIndexMap.entrySet().iterator(); i.hasNext();) {
                                            Map.Entry e = (Map.Entry) i.next();
                                            Integer index = (Integer) e.getKey();
                                            if (index.intValue() < DeviceContext.BASE_FLASH_PRESET && index.intValue() > 0)
                                                e.setValue(IntPool.zero);
                                        }
                                    }
                                }
                                if (destPresetIndexes.length == 1) {
                                    if (UserMessaging.askYesNo("Open uploaded preset now?")) {
                                        ContextReadablePreset p = basePreset.getPresetContext().getContextPreset(destPresetIndexes[0]);
                                        basePreset.getDeviceContext().getViewManager().openPreset(p, true).post();
                                    }
                                } else if (destPresetIndexes.length > 1 && destPresetIndexes.length <= 16) {
                                    if (UserMessaging.askYesNo("Open " + destPresetIndexes.length + " uploaded presets now?")) {
                                        if (UserMessaging.askYesNo("Clear workspace first?")) {
                                            basePreset.getDeviceContext().getViewManager().clearDeviceWorkspace().send(0);
                                        }

                                        for (int i = 0; i < destPresetIndexes.length; i++) {
                                            ContextReadablePreset p = basePreset.getPresetContext().getContextPreset(destPresetIndexes[i]);
                                            basePreset.getDeviceContext().getViewManager().openPreset(p, i == 0).post();
                                        }
                                    }
                                }
                                final ProgressCallbackTree presetProg = new ProgressCallbackTree("Preset package upload", false);
                                PresetContextMacros.loadPresetsToContext(ppkgManifest.getPresetPackage().getPresets(), destPresetIndexes, basePreset.getPresetContext(), sampleSrcIndex2DestIndexMap, presetProg);

                                if (ZPREF_addLoadedPresetsToContextFilter.getValue())
                                    adjustPresetContextFilter(destPresetIndexes);
                                if (ppkgManifest.getPresetPackage().getHeader().isIncludingMasterSettings() && PackageFactory.ZPREF_applyMaster.getValue())
                                    applyMaster();
                                if (ppkgManifest.getPresetPackage().getHeader().isIncludingMultimodeSettings() && PackageFactory.ZPREF_applyMultimode.getValue())
                                    applyMultimode();
                            } finally {
                            }
                        }
                    }, "Preset package upload").post(ShowCommandFailedCallback.INSTANCE);
                } catch (ResourceUnavailableException e1) {
                    UserMessaging.showCommandFailed(e1.getMessage());
                }
                dispose();
            }
        };
        ActionListener resetAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                baseSample = contextSamples[0];
                baseSampleLocationList.setSelectedValue(baseSample, true);
                LoadPresetPackageDialog.this.basePreset = contextPresets[0];
                basePresetLocationList.setSelectedValue(LoadPresetPackageDialog.this.basePreset, true);
                updateTargets();
            }
        };
        proceedButton.addActionListener(proceedAction);
        cancelButton.addActionListener(cancelAction);
        resetButton.addActionListener(resetAction);
    }

    void adjustPresetContextFilter(Integer[] presets) {
        try {
            basePreset.getDeviceContext().getViewManager().addPresetsToPresetContextFilter(presets).post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
    }

    void stripMissingSamples() {

    }

    void finalizeSampleIndexMap() {
        if (brokenRomSampleIndexes.length != 0)
            if (PackageFactory.ZPREF_zeroBrokenRomReferences.getValue()) {
                for (int i = 0; i < brokenRomSampleIndexes.length; i++)
                    this.sampleSrcIndex2DestIndexMap.put(brokenRomSampleIndexes[i], IntPool.zero);
            }

        if (missingUserSampleSrcIndexes.length != 0)
            if (PackageFactory.ZPREF_zeroMissingSamples.getValue()) {
                for (int i = 0; i < missingUserSampleSrcIndexes.length; i++)
                    this.sampleSrcIndex2DestIndexMap.put(missingUserSampleSrcIndexes[i], IntPool.zero);
            }
        if (emptyUserSampleIndexes.length != 0)
            for (int i = 0; i < emptyUserSampleIndexes.length; i++)
                this.sampleSrcIndex2DestIndexMap.put(emptyUserSampleIndexes[i], IntPool.zero);
    }

    private void applyMultimode() {
        try {
            MultiModeContext mc = basePreset.getDeviceContext().getMultiModeContext();
            MultiModeMap m = ppkgManifest.getPresetPackage().getMultiModeMap();

            java.util.List<Integer> srcPresets = Arrays.asList(PresetContextMacros.extractPresetIndexes(ppkgManifest.getPresetPackage().getPresets()));
            for (int i = 1, j = (m.has32() ? 32 : 16); i <= j; i++) {
                try {
                    Integer ch = IntPool.get(i);
                    Integer mp = m.getPreset(ch);
                    int ind = srcPresets.indexOf(mp);
                    if (ind != -1 && ind < destPresetIndexes.length)
                        m.setPreset(ch, destPresetIndexes[ind]);
                    else {
                        m.setPreset(ch, IntPool.minus_one);
                        //m.setVolume(ch, IntPool.zero);
                        //m.setPan(ch, IntPool.zero);
                        //m.setSubmix(ch, IntPool.minus_one);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mc.setMultimodeMap(m).post();
        } catch (Exception e) {
            UserMessaging.showError(e.getMessage());
        }
    }

    private void applyMaster() {
        try {
            MasterContext mc = basePreset.getDeviceContext().getMasterContext();
            Integer[] ids = ppkgManifest.getPresetPackage().getMasterIds();
            Integer[] vals = ppkgManifest.getPresetPackage().getMasterVals();
            for (int i = 0; i < ids.length; i++)
                try {
                    mc.setMasterParam(ids[i], vals[i]).post();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
        } catch (Exception e1) {
            UserMessaging.showError(e1.getMessage());
        }
    }

    private static final int UNIT_SCROLL = 5;
    private static final int BLOCK_SCROLL = 60;

    void init() throws DeviceException {
        contextPresets = basePreset.getPresetContext().getEditablePresets();
        java.util.List<ContextEditableSample> samples = basePreset.getPresetContext().getRootSampleContext().getContextEditableSamples();
        contextSamples = samples.toArray(new ContextEditableSample[samples.size()]);
        //java.util.List<ReadableSample> empties = SampleContextMacros.findEmptySamples(contextSamples, 0);
        //if (empties.size() == 0)
            baseSample = contextSamples[0];
        //else
        //    baseSample = (ContextEditableSample) empties.get(0);

        sampleSrcIndex2DestIndexMap = ZUtilities.createIdentityMap(ppkgManifest.getReferencedSamples());
        srcSampleIndexes = ppkgManifest.getReferencedSamples();

        missingUserSampleSrcIndexes = ppkgManifest.getMissingUserSamples();
        brokenRomSampleIndexes = ppkgManifest.getBrokenRomSamples();
        emptyUserSampleIndexes = ppkgManifest.getEmptyUserSamples();
    }

    void setupComponents() {
        packageHeaderPanel.setLayout(new BorderLayout());
        packageHeaderPanel.setBorder(new CompoundBorder(new TitledBorder("Package Header"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
        PresetPackageHeaderInfoTable table = new PresetPackageHeaderInfoTable(ppkgManifest.getPresetPackage().getHeader());
        if (ppkgManifest.getPresetPackage().getSamplePackage() != null)
            table.setSampleHeader(ppkgManifest.getPresetPackage().getSamplePackage().getHeader());
        packageHeaderPanel.add(table, BorderLayout.CENTER);

        // LISTS
        basePresetLocationList.setListData(contextPresets);
        basePresetLocationList.setSelectedValue(basePreset, true);
        basePresetLocationList.setBackground(UIColors.getDefaultBG());
        basePresetLocationList.setFixedCellHeight(UIColors.getTableRowHeight());
        basePresetLocationList.setCellRenderer(new PresetContextTableCellRenderer());
        basePresetLocationList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                basePreset = (ContextEditablePreset) basePresetLocationList.getSelectedValue();
                updateTargets();
            }
        });
        targetPresetLocationList.setCellRenderer(new PresetContextTableCellRenderer());
        targetPresetLocationList.setBackground(UIColors.getDefaultBG());
        targetPresetLocationList.setFixedCellHeight(UIColors.getTableRowHeight());

        if (getUserSampleLoadCount() > 0) {
            baseSampleLocationList.setListData(contextSamples);
            baseSampleLocationList.setSelectedValue(baseSample, true);
        } else
            baseSampleLocationList.setListData(new Object[]{"No samples loading"});

        baseSampleLocationList.setBackground(UIColors.getDefaultBG());
        baseSampleLocationList.setCellRenderer(new SampleContextTableCellRenderer());
        baseSampleLocationList.setFixedCellHeight(UIColors.getTableRowHeight());
        baseSampleLocationList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                baseSample = (ContextEditableSample) baseSampleLocationList.getSelectedValue();
                updateTargets();
            }
        });
        if (getUserSampleLoadCount() == 0)
            targetSampleLocationList.setListData(new Object[]{"No samples loading"});

        targetSampleLocationList.setCellRenderer(new SampleContextTableCellRenderer());
        targetSampleLocationList.setBackground(UIColors.getDefaultBG());
        targetSampleLocationList.setFixedCellHeight(UIColors.getTableRowHeight());

        // SCROLL BARS
        basePresetLocationScroll.setBorder(new CompoundBorder(new TitledBorder("Base preset location"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
        basePresetLocationScroll.getVerticalScrollBar().setUnitIncrement(UNIT_SCROLL);
        basePresetLocationScroll.getVerticalScrollBar().setBlockIncrement(BLOCK_SCROLL);
        targetPresetLocationScroll.getVerticalScrollBar().setUnitIncrement(UNIT_SCROLL);
        targetPresetLocationScroll.getVerticalScrollBar().setBlockIncrement(BLOCK_SCROLL);
        targetPresetLocationScroll.setBorder(new CompoundBorder(new TitledBorder("Target preset locations"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));

        baseSampleLocationScroll.setBorder(new CompoundBorder(new TitledBorder("Base user sample location"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
        baseSampleLocationScroll.getVerticalScrollBar().setUnitIncrement(UNIT_SCROLL);
        baseSampleLocationScroll.getVerticalScrollBar().setBlockIncrement(BLOCK_SCROLL);
        targetSampleLocationScroll.getVerticalScrollBar().setUnitIncrement(UNIT_SCROLL);
        targetSampleLocationScroll.getVerticalScrollBar().setBlockIncrement(BLOCK_SCROLL);
        targetSampleLocationScroll.setBorder(new CompoundBorder(new TitledBorder("Target user sample locations"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));

        // TEXT FIELD
        statusTextField.setBorder(new LineBorder(UIColors.getTableFirstSectionBG(), 3));

        // CHECKS
        applyMultimodeCheck.setSelected(PackageFactory.ZPREF_applyMultimode.getValue());
        applyMultimodeCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PackageFactory.ZPREF_applyMultimode.putValue(applyMultimodeCheck.isSelected());
            }
        });
        applyMultimodeCheck.setEnabled(ppkgManifest.getPresetPackage().getHeader().isIncludingMultimodeSettings());

        applyMasterCheck.setSelected(PackageFactory.ZPREF_applyMaster.getValue());
        applyMasterCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PackageFactory.ZPREF_applyMaster.putValue(applyMasterCheck.isSelected());
            }
        });
        applyMasterCheck.setEnabled(ppkgManifest.getPresetPackage().getHeader().isIncludingMasterSettings());

        zeroMissingCheck.setSelected(PackageFactory.ZPREF_zeroMissingSamples.getValue());
        zeroMissingCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PackageFactory.ZPREF_zeroMissingSamples.putValue(zeroMissingCheck.isSelected());
                //updateTargets();
            }
        });

        zeroBrokenRomCheck.setSelected(PackageFactory.ZPREF_zeroBrokenRomReferences.getValue());
        zeroBrokenRomCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PackageFactory.ZPREF_zeroBrokenRomReferences.putValue(zeroBrokenRomCheck.isSelected());
                // updateTargets();
            }
        });

        useEmptyPresetsCheck.setSelected(PackageFactory.ZPREF_serachForAndOnlyUseEmptyPresets.getValue());
        useEmptyPresetsCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PackageFactory.ZPREF_serachForAndOnlyUseEmptyPresets.putValue(useEmptyPresetsCheck.isSelected());
                updateTargets();
            }
        });
        useEmptySamplesCheck.setSelected(PackageFactory.ZPREF_serachForAndOnlyUseEmptySamples.getValue());
        useEmptySamplesCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PackageFactory.ZPREF_serachForAndOnlyUseEmptySamples.putValue(useEmptySamplesCheck.isSelected());
                updateTargets();
            }
        });

        addToFilterCheck.setSelected(ZPREF_addLoadedPresetsToContextFilter.getValue());
        addToFilterCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZPREF_addLoadedPresetsToContextFilter.putValue(addToFilterCheck.isSelected());
                //updateTargets();
            }
        });

        //Integer[] refSamples = ppkgManifest.getReferencedSamples();
        // Integer[] romSamples = PresetContextMacros.getRomIndexes(refSamples);
        // Integer[] userSamples = SampleContextMacros.extractUserIndexes(refSamples);


        // NOTE: preset packages created since 15th May 2004 will always have a sample package included
        // first branch of IF below provided for backward compatibility
        if (getUserSampleLoadCount() == 0) {
            useEmptySamplesCheck.setEnabled(false);
            baseSampleLocationList.setEnabled(false);
            targetSampleLocationList.setEnabled(false);
        }

        zeroBrokenRomCheck.setEnabled(brokenRomSampleIndexes.length > 0);
        zeroMissingCheck.setEnabled(missingUserSampleSrcIndexes.length > 0);
    }

    static final DecimalFormat df = new DecimalFormat("0000");
    private static final String LOADING = "LOADING  [ ";
    private static final String NOT_LOADING = "Cannot perform load";

    int getUserSampleLoadCount() {
        return Math.max(0, ppkgManifest.getReferencedUserSamples().length - missingUserSampleSrcIndexes.length - emptyUserSampleIndexes.length);
    }

    void setStatus(boolean valid) {
        if (valid)
            try {
                int overwrites = (destPresetIndexes.length - PresetContextMacros.numEmpties(basePreset.getPresetContext(), destPresetIndexes));
                String s =
                        LOADING
                        + destPresetIndexes.length
                        + (destPresetIndexes.length != 1 ? " presets, " : " preset, ")
                        + overwrites
                        + " overwrites ]";

                if (getUserSampleLoadCount() > 0)
                    overwrites = (destSampleIndexes.length - SampleContextMacros.numEmpties(baseSample.getSampleContext(), destSampleIndexes));
                else
                    overwrites = 0;
                s = s
                        + " [ "
                        + getUserSampleLoadCount()
                        + (getUserSampleLoadCount() != 1 ? " user samples, " : " user sample, ")
                        + overwrites
                        + " overwrites"
                        + (missingUserSampleSrcIndexes.length > 0 ? " (" + missingUserSampleSrcIndexes.length + " missing) ]" : " ]");

                statusTextField.setText(s);
            } catch (Exception e) {
                statusTextField.setText("error");
            }

        else
            statusTextField.setText(NOT_LOADING);

        proceedButton.setEnabled(valid);
    }

    public Map getSampleSrcIndex2DestIndexMap() {
        return sampleSrcIndex2DestIndexMap;
    }

    void updateTargets() {
        boolean sampleStatus = true;
        boolean presetStatus = true;
        // SAMPLE
        java.util.List targets = new ArrayList();
        if (getUserSampleLoadCount() > 0) {
            if (!useEmptySamplesCheck.isSelected()) {
                int si = Arrays.asList(contextSamples).indexOf(baseSample);
                destSampleIndexes = new Integer[ppkgManifest.getSamplePackageManifest().getNonNullValidFileCount()];
                if (si + destSampleIndexes.length >= contextSamples.length)
                    sampleStatus = false;
                else {
                    targets = new ArrayList();
                    for (int i = 0; i < destSampleIndexes.length; i++) {
                        destSampleIndexes[i] = contextSamples[si + i].getIndex();
                        targets.add(contextSamples[si + i]);
                    }
                    sampleStatus = true;
                    updateIndex2IndexMap(sampleSrcIndex2DestIndexMap, ppkgManifest.getSamplePackageManifest().getFile2SrcIndexMap(), destSampleIndexes, ppkgManifest.getSamplePackageManifest().getNonNullValidFiles());
                }
            } else {
                targets = SampleContextMacros.findEmptySamples(contextSamples, Arrays.asList(contextSamples).indexOf(baseSample));
                if (targets.size() < ppkgManifest.getSamplePackageManifest().getNonNullValidFileCount()) {
                    sampleStatus = false;
                } else {
                    targets = targets.subList(0, ppkgManifest.getSamplePackageManifest().getNonNullValidFileCount());
                    destSampleIndexes = SampleContextMacros.extractSampleIndexes((ContextEditableSample[]) targets.toArray(new ContextEditableSample[targets.size()]));
                    //targets.addAll(emptyList);
                    sampleStatus = true;
                    updateIndex2IndexMap(sampleSrcIndex2DestIndexMap, ppkgManifest.getSamplePackageManifest().getFile2SrcIndexMap(), destSampleIndexes, ppkgManifest.getSamplePackageManifest().getNonNullValidFiles());
                }
            }
            targetSampleLocationList.setListData(targets.toArray());
        }
        // PRESET
        targets = new ArrayList();
        Integer[] srcIndexes = ppkgManifest.getSourcePresetIndexes();
        if (!useEmptyPresetsCheck.isSelected()) {
            int si = Arrays.asList(contextPresets).indexOf(basePreset);
            destPresetIndexes = new Integer[srcIndexes.length];
            if (si + destPresetIndexes.length >= contextPresets.length)
                presetStatus = false;
            else {
                for (int i = 0; i < destPresetIndexes.length; i++) {
                    destPresetIndexes[i] = contextPresets[si + i].getIndex();
                    targets.add(contextPresets[si + i]);
                }
                presetStatus = true;
            }
        } else {
            targets = PresetContextMacros.findEmptyPresets(contextPresets, Arrays.asList(contextPresets).indexOf(basePreset));
            if (targets.size() < srcIndexes.length) {
                presetStatus = false;
            } else {
                targets = targets.subList(0, srcIndexes.length);
                destPresetIndexes = PresetContextMacros.extractPresetIndexes((ReadablePreset[]) targets.toArray(new ReadablePreset[targets.size()]));
                presetStatus = true;
            }
        }
        targetPresetLocationList.setListData(targets.toArray());
        setStatus(sampleStatus && presetStatus);
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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(10, 5, new Insets(0, 0, 0, 0), -1, -1));
        packageHeaderPanel = new JPanel();
        packageHeaderPanel.setEnabled(false);
        mainPanel.add(packageHeaderPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(300, 150), null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        basePresetLocationScroll = new JScrollPane();
        basePresetLocationScroll.setVerticalScrollBarPolicy(22);
        panel1.add(basePresetLocationScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 120), null));
        basePresetLocationList = new JList();
        basePresetLocationList.setFixedCellHeight(-1);
        basePresetLocationScroll.setViewportView(basePresetLocationList);
        targetPresetLocationScroll = new JScrollPane();
        targetPresetLocationScroll.setVerticalScrollBarPolicy(22);
        panel1.add(targetPresetLocationScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 120), null));
        targetPresetLocationList = new JList();
        targetPresetLocationList.setEnabled(false);
        targetPresetLocationList.setFixedCellHeight(-1);
        targetPresetLocationScroll.setViewportView(targetPresetLocationList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        baseSampleLocationScroll = new JScrollPane();
        baseSampleLocationScroll.setHorizontalScrollBarPolicy(31);
        baseSampleLocationScroll.setVerticalScrollBarPolicy(22);
        panel2.add(baseSampleLocationScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 120), null));
        baseSampleLocationList = new JList();
        baseSampleLocationList.setFixedCellHeight(-1);
        baseSampleLocationList.setSelectionMode(0);
        baseSampleLocationScroll.setViewportView(baseSampleLocationList);
        targetSampleLocationScroll = new JScrollPane();
        targetSampleLocationScroll.setHorizontalScrollBarPolicy(31);
        targetSampleLocationScroll.setVerticalScrollBarPolicy(22);
        panel2.add(targetSampleLocationScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 120), null));
        targetSampleLocationList = new JList();
        targetSampleLocationList.setEnabled(false);
        targetSampleLocationList.setFixedCellHeight(-1);
        targetSampleLocationList.setSelectionMode(0);
        targetSampleLocationScroll.setViewportView(targetSampleLocationList);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        useEmptyPresetsCheck = new JCheckBox();
        useEmptyPresetsCheck.setText("Search for and only use empty presets");
        panel3.add(useEmptyPresetsCheck, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        addToFilterCheck = new JCheckBox();
        addToFilterCheck.setText("Add loaded presets to preset filter");
        panel3.add(addToFilterCheck, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(3, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(9, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        proceedButton = new JButton();
        proceedButton.setText("Proceed");
        panel4.add(proceedButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        resetButton = new JButton();
        resetButton.setText("Reset");
        panel4.add(resetButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        cancelButton = new JButton();
        cancelButton.setHorizontalTextPosition(2);
        cancelButton.setText("Cancel");
        panel4.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        statusTextField = new JTextField();
        statusTextField.setEditable(false);
        statusTextField.setEnabled(true);
        mainPanel.add(statusTextField, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        zeroMissingCheck = new JCheckBox();
        zeroMissingCheck.setText("Zero missing user samples");
        panel5.add(zeroMissingCheck, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        zeroBrokenRomCheck = new JCheckBox();
        zeroBrokenRomCheck.setText("Zero unmatched ROM samples");
        panel5.add(zeroBrokenRomCheck, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(5, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer7 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer7, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel6, new com.intellij.uiDesigner.core.GridConstraints(6, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        applyMultimodeCheck = new JCheckBox();
        applyMultimodeCheck.setText("Apply multimode settings");
        panel6.add(applyMultimodeCheck, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        applyMasterCheck = new JCheckBox();
        applyMasterCheck.setText("Apply master settings");
        panel6.add(applyMasterCheck, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        useEmptySamplesCheck = new JCheckBox();
        useEmptySamplesCheck.setText("Search for and only use empty samples");
        useEmptySamplesCheck.setToolTipText("r");
        mainPanel.add(useEmptySamplesCheck, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer8 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer8, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
    }
}
