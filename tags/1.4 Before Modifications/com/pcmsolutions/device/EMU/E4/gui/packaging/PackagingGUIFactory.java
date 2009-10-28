package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.packaging.PresetPackage;
import com.pcmsolutions.device.EMU.E4.packaging.SamplePackage;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.zcommands.sample.LoadContextSamplesZMTC;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.gui.FuzzyLineBorder;
import com.pcmsolutions.gui.ProgressCallbackTree;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.audio.SampleAuditionPanel;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZStringPref;
import com.pcmsolutions.system.tasking.ManageableTicketedQ;
import com.pcmsolutions.system.tasking.QueueFactory;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * User: paulmeehan
 * Date: 03-Aug-2004
 * Time: 04:13:03
 */
public final class PackagingGUIFactory {
    static ManageableTicketedQ packageGUIQ = QueueFactory.createTicketedQueue(PackagingGUIFactory.class.getClass(), "packageGUIQ", 6);
    static ManageableTicketedQ packageWorkerQ = QueueFactory.createTicketedQueue(PackagingGUIFactory.class.getClass(), "packageWorkerQ", 6);

    static {
        packageGUIQ.start();
        packageWorkerQ.start();
    }

    public static void newPresetPackage(final ReadablePreset[] presets, final String suggName) throws ResourceUnavailableException {
        packageGUIQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                NewPresetPackageDialog dlg = new NewPresetPackageDialog(presets, suggName);
                dlg.setVisible(true);
            }
        }, "newPresetPackage").post();
    }

    public static void newSamplePackage(final ContextEditableSample[] samples, final String suggName) throws ResourceUnavailableException {
        packageGUIQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                NewSamplePackageDialog dlg = new NewSamplePackageDialog(samples, suggName);
                dlg.setVisible(true);
            }
        }, "newSamplePackage").post();
    }

    static JFileChooser ppkgFileChooser;
    static final ZStringPref ZPREF_lastPPKGDir = new Impl_ZStringPref(Preferences.userNodeForPackage(PackagingGUIFactory.class.getClass()), "lastLoadPresetPackageDir", Zoeos.getHomeDir().getAbsolutePath());

    static void assertPresetPackageLoadChooser() {
        if (ppkgFileChooser == null) {
            ppkgFileChooser = new JFileChooser();
            final PresetPackageHeaderInfoTable phip = new PresetPackageHeaderInfoTable(null);

            JPanel p = new JPanel(new BorderLayout());
            p.add(phip, BorderLayout.CENTER);
            p.setBorder(new CompoundBorder(new TitledBorder("Package Header"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));

            ppkgFileChooser.setAccessory(p);
            ppkgFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                        PackageFactory.PresetPackageHeaders hdrs = null;
                        File sf = ppkgFileChooser.getSelectedFile();

                        if (sf != null && !sf.isDirectory())
                            try {
                                hdrs = PackageFactory.extractPresetPackageHeader(ppkgFileChooser.getSelectedFile());
                                phip.setHeader(hdrs.getPresetPackageHeader());
                                phip.setSampleHeader(hdrs.getSamplePackageHeader());
                            } catch (CommandFailedException e) {
                                e.printStackTrace();
                                phip.setHeader(null);
                                // phip.setText("error");
                            }
                        else
                            phip.setHeader(null);
                    } else {
                        //if (evt.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                        phip.setHeader(null);
                    }
                }
            });

            ppkgFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            ppkgFileChooser.setFileFilter(new FileFilter() {
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
            ppkgFileChooser.setAcceptAllFileFilterUsed(false);
        }
        try {
            ppkgFileChooser.setCurrentDirectory(new File(ZPREF_lastPPKGDir.getValue()));
        } catch (Exception e) {
        }
    }

    public static void loadPresetPackage(final ContextEditablePreset preset) throws ResourceUnavailableException {
        packageGUIQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                File f = null;
                assertPresetPackageLoadChooser();
                ppkgFileChooser.setSelectedFile(null);
                int retval = ppkgFileChooser.showDialog(ZoeosFrame.getInstance(), "Load");
                if (retval == JFileChooser.APPROVE_OPTION) {
                    f = ppkgFileChooser.getSelectedFile();
                    ZPREF_lastPPKGDir.putValue(f.getParentFile().getAbsolutePath());
                }

                if (f != null) {
                    final File f_f = f;
                    try {
                        PresetPackage ppkg = PackageFactory.extractPresetPackage(f_f);
                        try {
                            PackageFactory.PresetPackageManifest mani = new PackageFactory.PresetPackageManifest(ppkg, f_f.getParent(), preset.getPresetContext().getRootSampleContext());
                            LoadPresetPackageDialog dlg = new LoadPresetPackageDialog(mani, preset);
                            dlg.setVisible(true);
                        } catch (Exception e) {
                            UserMessaging.showCommandFailed(e.getMessage());
                        }
                    } catch (CommandFailedException e) {
                        UserMessaging.showCommandFailed(e.getMessage());
                    }
                }
            }
        }, "loadPresetPackage").post();
    }

    private static JFileChooser spkgFileChooser;
    public static final ZStringPref ZPREF_lastSPKGDir = new Impl_ZStringPref(Preferences.userNodeForPackage(PackagingGUIFactory.class.getClass()), "lastLoadSamplePackageDir", Zoeos.getHomeDir().getAbsolutePath());

    private static void assertSamplePackageLoadChooser() {
        if (spkgFileChooser == null) {
            spkgFileChooser = new JFileChooser();
            final PackageHeaderInfoTable phip = new SamplePackageHeaderInfoTable(null);

            JPanel p = new JPanel(new BorderLayout()) {
                /*   public Color getBackground() {
                       return UIColors.getDefaultBG();
                   }
                   public Color getForeground() {
                       return UIColors.getDefaultFG();
                   }
                   */
            };
            p.add(phip, BorderLayout.CENTER);
            p.setBorder(new CompoundBorder(new TitledBorder("Package Header"), new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true)));
            spkgFileChooser.setAccessory(p);
            spkgFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                        File sf = spkgFileChooser.getSelectedFile();
                        if (sf != null && !sf.isDirectory())
                            try {
                                phip.setHeader(PackageFactory.extractSamplePackageHeader(spkgFileChooser.getSelectedFile()));
                            } catch (CommandFailedException e) {
                                e.printStackTrace();
                                phip.setHeader(null);
                                //phip.setText("error");
                            }
                        else
                            phip.setHeader(null);
                    } else {
                        phip.setHeader(null);
                    }
                }
            });
            spkgFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            spkgFileChooser.setFileFilter(new FileFilter() {
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
            spkgFileChooser.setAcceptAllFileFilterUsed(false);
        }
        try {
            spkgFileChooser.setCurrentDirectory(new File(ZPREF_lastSPKGDir.getValue()));
        } catch (Exception e) {
        }
    }

    public static void loadSamplePackage(final ContextEditableSample sample) throws ResourceUnavailableException {
        packageGUIQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                File f = null;
                assertSamplePackageLoadChooser();
                int retval = spkgFileChooser.showDialog(ZoeosFrame.getInstance(), "Load");
                if (retval == JFileChooser.APPROVE_OPTION) {
                    f = spkgFileChooser.getSelectedFile();
                    ZPREF_lastSPKGDir.putValue(f.getParentFile().getAbsolutePath());
                }
                if (f != null) {
                    final File f_f = f;
                    try {
                        task_loadSamplePackage(PackageFactory.extractSamplePackage(f_f), f_f.getParent(), sample);
                    } catch (Exception e) {
                        UserMessaging.showCommandFailed(e.getMessage());
                    }
                }
            }
        }, "loadSamplePackage").post();
    }

    // returns Integer -> Integer
    // returns null if op cbCancelled
    private static void task_loadSamplePackage(final SamplePackage pkg, final String location, final ContextEditableSample sample) throws ResourceUnavailableException {
        packageWorkerQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                if (!sample.getDeviceContext().isSmdiCoupled())
                    throw new CommandFailedException("Cannot load the package samples - the device is not SMDI coupled.");
                LoadSamplePackageDialog dlg = null;
                PackageFactory.SamplePackageManifest mani = new PackageFactory.SamplePackageManifest(pkg, location);
                if (mani.getNonNullFileCount() == 0)
                    return;
                else if (mani.getNonNullValidFileCount() == 0) {
                    UserMessaging.showWarning("Nothing to load. All the samples in the package are missing.");
                    return;
                }
                try {
                    dlg = new LoadSamplePackageDialog(mani, sample);
                } catch (NoSuchContextException e) {
                    throw new CommandFailedException(e.getMessage());
                }
                dlg.setVisible(true);
                Map<Integer, Integer> m = dlg.getSrcIndex2DestIndexMap();

                if (m != null) {
                    int nonEmptyCount = 0;
                    //SampleContextMacros.numEmpties(sample.getSampleContext(), m.values().toArray(new Integer[m.size()]));
                    for (Integer index : m.values())
                        if (index.intValue() < DeviceContext.BASE_ROM_SAMPLE && !sample.getSampleContext().isEmpty(index))
                            nonEmptyCount++;
                    if (nonEmptyCount > 0)
                        if (!UserMessaging.askYesNo(nonEmptyCount + " sample" + (nonEmptyCount != 1 ? "s" : "") + " will be overwritten. Continue?"))
                            return;
                    ProgressCallbackTree prog = new ProgressCallbackTree("Sample package upload", true) {
                        public String finalizeString(String s) {
                            return sample.getSampleContext().getDeviceContext().makeDeviceProgressTitle(s);
                        }
                    };
                    SampleContextMacros.loadSamplesToContext(mani.getNonNullValidFiles(), dlg.getDestIndexes(), sample.getSampleContext(), prog, true);
                    if (LoadSamplePackageDialog.ZPREF_addLoadedSamplesToContextFilter.getValue())
                        try {
                            sample.getDeviceContext().getViewManager().addSamplesToSampleContextFilter(dlg.getDestIndexes()).post();
                        } catch (ResourceUnavailableException e) {
                            e.printStackTrace();
                        }
                }
            }
        }, "task_loadSamplePackage").post();
    }

    private static void updateIndexMap(Map index2IndexMap, Map file2IndexMap, Integer[] newIndexes, File[] files) {
        for (int i = 0; i < files.length; i++) {
            Integer ind = (Integer) file2IndexMap.get(files[i]);
            if (ind == null)
                throw new IllegalArgumentException("missing index mapping");
            index2IndexMap.put(ind, newIndexes[i]);
        }
    }

    private static JFileChooser loadSamplesFileChooser;
    public static final ZStringPref ZPREF_lastDir = new Impl_ZStringPref(Preferences.userNodeForPackage(PackagingGUIFactory.class.getClass()), "lastLoadSamplesDir", "");

    private static void assertLoadSamplesChooser() {
        final String extStr = AudioUtilities.getLegalAudioExtensionsString();
        if (loadSamplesFileChooser == null) {
            loadSamplesFileChooser = new JFileChooser();
            final SampleAuditionPanel sap = new SampleAuditionPanel("Auditioning", loadSamplesFileChooser);
            loadSamplesFileChooser.setAccessory(sap);
            loadSamplesFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                        File[] files = loadSamplesFileChooser.getSelectedFiles();
                        if (files.length == 1)
                            sap.setCurrentFile(files[0]);
                        else
                            sap.setCurrentFile(null);
                    } else {
                        sap.setCurrentFile(null);
                    }
                }
            });

            loadSamplesFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            loadSamplesFileChooser.setMultiSelectionEnabled(true);
            loadSamplesFileChooser.setFileFilter(new FileFilter() {
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
            loadSamplesFileChooser.setAcceptAllFileFilterUsed(false);
        }
        try {
            loadSamplesFileChooser.setCurrentDirectory(new File(ZPREF_lastDir.getValue()));
        } catch (Exception e) {
        }
    }

    public static void loadSamples(final ContextEditableSample beginSample) throws ResourceUnavailableException {
        packageGUIQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                if (!beginSample.getDeviceContext().isSmdiCoupled())
                    throw new CommandFailedException("Cannot perform sample load - the device is not SMDI coupled.");
                File[] files = null;
                assertLoadSamplesChooser();
                int retval = loadSamplesFileChooser.showDialog(ZoeosFrame.getInstance(), "Load");
                ((SampleAuditionPanel) loadSamplesFileChooser.getAccessory()).stopClip();
                if (retval == JFileChooser.APPROVE_OPTION) {
                    files = loadSamplesFileChooser.getSelectedFiles();
                    ZPREF_lastDir.putValue(files[0].getParentFile().getAbsolutePath());
                }
                final File[] f_files = files;

                try {
                    loadSampleFiles(f_files, beginSample);
                } catch (Exception e) {
                    e.printStackTrace();
                    UserMessaging.showError("Sample load failed");
                }
            }
        }, "loadSamples").post();
    }

    public static void loadSampleFiles(final File[] files, final ContextEditableSample beginSample) throws ResourceUnavailableException {
        packageWorkerQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                if (files != null) {
                    if (files.length == 1 && beginSample.isEmpty()) {
                        int mc = ZUtilities.getFileCountForPattern(files, AudioUtilities.sampleIndexPattern);
                        boolean strip = false;
                        if (mc > 0)
                            strip = UserMessaging.askYesNo("The sample file name appears to have a sample index appendage. Strip?");

                        ProgressCallbackTree prog = new ProgressCallbackTree("Sample upload", true) {
                            public String finalizeString(String s) {
                                return beginSample.getSampleContext().getDeviceContext().makeDeviceProgressTitle(s);
                            }
                        };
                        try {
                            SampleContextMacros.loadSamplesToContext(files, new Integer[]{beginSample.getIndex()}, beginSample.getSampleContext(), prog, strip);
                        } finally {
                            prog.updateProgress(1);
                            // following commented out as I don't think it is neccessary under most conditions
                            // usually the sample must have been visible in the context anyway to perform the load at that position
                            // of course we miss the scenario where a sample sub-menu was accessed from a preset editor

                            //if ( LoadSamplePackageDialog.ZPREF_addLoadedSamplesToContextFilter.getValue())
                            //  if ( UserMessaging.askYesNo("Add loaded sample to sample filter?"))
                            //    beginSample.getDeviceContext().getViewManager().addSamplesToSampleContextFilter(new Integer[]{beginSample.getSampleNumber()}).start();
                        }
                    } else {
                        LoadContextSamplesDialog dlg = new LoadContextSamplesDialog(beginSample, files);
                        dlg.setVisible(true);
                        Integer[] indexes = dlg.getTargetIndexes();
                        if (indexes != null) {
                            ProgressCallbackTree prog = new ProgressCallbackTree("Sample upload", true) {
                                public String finalizeString(String s) {
                                    return beginSample.getSampleContext().getDeviceContext().makeDeviceProgressTitle(s);
                                }
                            };
                            try {
                                SampleContextMacros.loadSamplesToContext(files, indexes, beginSample.getSampleContext(), prog, LoadContextSamplesZMTC.stripSampleAppendages.getValue());
                                if (LoadSamplePackageDialog.ZPREF_addLoadedSamplesToContextFilter.getValue())
                                    beginSample.getDeviceContext().getViewManager().addSamplesToSampleContextFilter(indexes).post();
                            } catch (Exception e) {
                                UserMessaging.showError(e.getMessage());
                            } finally {
                                prog.updateProgress(1);
                            }
                        }
                    }
                }
            }
        }, "loadSampleFiles").post();
    }
}
