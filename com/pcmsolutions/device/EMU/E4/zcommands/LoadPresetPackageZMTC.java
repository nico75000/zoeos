package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.packaging.PresetPackageHeaderInfoPane;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.packaging.PresetPackage;
import com.pcmsolutions.device.EMU.E4.packaging.SamplePackage;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZStringPref;
import com.pcmsolutions.util.IntegerUseMap;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 29-Sep-2003
 * Time: 13:53:18
 * To change this template use Options | File Templates.
 */
public class LoadPresetPackageZMTC extends AbstractContextEditablePresetZMTCommand implements E4ContextEditablePresetZCommandMarker {
    protected static final Preferences pref = Preferences.userNodeForPackage(LoadPresetPackageZMTC.class);
    private static JFileChooser fc;
    public static final ZStringPref ZPREF_lastDir = new Impl_ZStringPref(pref, "lastLoadPresetPackageDir", Zoeos.getHomeDir().getAbsolutePath());

    private ContextEditablePreset preset;

    private synchronized static void assertChooser() {
        if (fc == null) {
            fc = new JFileChooser();
            final PresetPackageHeaderInfoPane phip = new PresetPackageHeaderInfoPane(null);

            JPanel p = new JPanel();
            p.add(new JScrollPane(phip));
            p.setBorder(new TitledBorder("Package Header"));

            fc.setAccessory(p);
            fc.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                        PresetPackage pkg = null;
                        File sf = fc.getSelectedFile();

                        if (sf != null && !sf.isDirectory())
                            try {
                                pkg = PackageFactory.extractPresetPackage(fc.getSelectedFile());
                                if (pkg != null) {
                                    phip.setHeader(pkg.getHeader());
                                    if (pkg.getSamplePackage() != null)
                                        phip.setSampleHeader(pkg.getSamplePackage().getHeader());
                                    else
                                        phip.setSampleHeader(null);
                                } else
                                    phip.setHeader(null);
                            } catch (CommandFailedException e) {
                                phip.setText("error");
                            }
                        else
                            phip.setHeader(null);
                    } else {
                        //if (evt.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                        phip.setHeader(null);
                    }
                }
            });

            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
        }
        try {
            fc.setCurrentDirectory(new File(ZPREF_lastDir.getValue()));
        } catch (Exception e) {
        }
    }

    public LoadPresetPackageZMTC() {
        init("Load" + ZUtilities.DOT_POSTFIX, "Load a preset package", null, null);
    }

    public String getMenuPathString() {
        return ";Packaging";
    }


    //private static int romSampleMode = PresetContext.ROM_LEAVE_UNTOUCHED;

    /*
    public static final int SAMPLE_ZERO_ALL = 0;
    public static final int SAMPLE_ZERO_UNMATCHED = 1;
    public static final int SAMPLE_LEAVE_UNTOUCHED = 2;
    */

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        preset = getTargets()[0];
        File f = null;
        synchronized (this.getClass()) {
            assertChooser();
            int retval = fc.showDialog(ZoeosFrame.getInstance(), "Load");
            if (retval == JFileChooser.APPROVE_OPTION) {
                f = fc.getSelectedFile();
                ZPREF_lastDir.putValue(f.getParentFile().getAbsolutePath());
            }
        }
        if (f != null) {
            ppkg = PackageFactory.extractPresetPackage(f);
            spkg = ppkg.getSamplePackage();
            //int sampleMode = SAMPLE_LEAVE_UNTOUCHED;
            Map index2IndexMap = null;
            try {
                if (spkg == null) {
                    IntegerUseMap um = PresetContextMacros.getSampleUsage(ppkg.getPresets());
                    if (um.size() > 0) {
                        String msg = (um.size() == 1 ? "This package references 1 sample, but does not include a sample package." : "This package references " + um.size() + " samples, but does not include a sample package.");
                        String[] options = (um.size() == 1 ? new String[]{"Zero", "Leave untouched"} : new String[]{"Zero all", "Leave untouched"});
                        int res = UserMessaging.askOptions("Sample references", msg, options);
                        if (res == JOptionPane.CANCEL_OPTION)
                            return;
                        if (res == 0)
                            index2IndexMap = new HashMap(); // non-null and absence of any entries should force all sample references to zero
                    }
                } else {
                    Integer index = IntPool.get(1);
                    if (spkg.getHeader().getPhysicalSampleCount() > 0)
                        try {
                            ReadableSample[] samples = SampleContextMacros.getContextUserSamples(preset.getDeviceContext().getDefaultSampleContext());
                            //int res = UserMessaging.askOptions("Select sample position to load package samples at", "Sample", samples);
                            ReadableSample s = (ReadableSample) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Sample?", "Select sample position to load the package samples to", JOptionPane.QUESTION_MESSAGE, null, samples, samples[0]);
                            if (s == null)
                                return;
                            index = s.getSampleNumber();
                        } catch (HeadlessException e) {
                            throw new CommandFailedException("internal error");
                        } catch (NoSuchContextException e) {
                            throw new CommandFailedException("problem with sample context");
                        } catch (ZDeviceNotRunningException e) {
                            throw new CommandFailedException("device not running");
                        }
                    index2IndexMap = LoadSamplePackageZMTC.loadSamplePackage(spkg, f.getParent(), preset.getDeviceContext().getDefaultSampleContext().getEditableSample(index));
                    if (index2IndexMap == null)
                        return;
                    IsolatedSample[] samples = spkg.getSamples();
                    IsolatedSample[] umSamples = SampleContextMacros.unmatchedRomLocations(samples, preset.getDeviceContext().getDefaultSampleContext());
                    if (umSamples.length > 0) {
                        int res = UserMessaging.askOptions("ROM sample references", umSamples.length + " of " + samples.length + " ROM " + (umSamples.length == 1 ? "sample reference does not match up" : "sample references do not match"), new String[]{"Zero all", "Zero unmatched", "Leave untouched"});
                        if (res == JOptionPane.CLOSED_OPTION)
                            return;
                        switch (res) {
                            case 0:
                                //romSampleMode = SAMPLE_ZERO_ALL;
                                Integer key;
                                for (Iterator i = index2IndexMap.keySet().iterator(); i.hasNext();) {
                                    key = (Integer) i.next();
                                    if (key.intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                                        index2IndexMap.put(key, IntPool.get(0));
                                }
                                break;
                            case 1:
                                //romSampleMode = SAMPLE_ZERO_UNMATCHED;
                                for (int i = 0; i < umSamples.length; i++)
                                    index2IndexMap.put(umSamples[i].getOriginalIndex(), IntPool.get(0));
                                break;
                            case 2:
                            default:
                                //romSampleMode = SAMPLE_LEAVE_UNTOUCHED;
                                break;
                        }
                    }
                }
                loadPresetPackage(ppkg, preset, index2IndexMap);
            } catch (NoSuchSampleException e) {
                throw new CommandFailedException("no such sample");
            } catch (ZDeviceNotRunningException e) {
                throw new CommandFailedException("device not running");
            }
        }
    }

    public static void loadPresetPackage(PresetPackage pkg, ContextEditablePreset preset, Map sampleIndexMap) throws CommandFailedException {
        DecimalFormat df = new DecimalFormat("0000");
        IsolatedPreset[] presets = pkg.getPresets();
        String opt1;
        String opt2;
        Integer[] indexes;
        if (preset.getPresetNumber().intValue() + presets.length > DeviceContext.BASE_FLASH_PRESET)
            throw new CommandFailedException("Not enough user locations on or after P" + df.format(preset.getPresetNumber()) + " to load package");

        try {
            if (PresetContextMacros.arePresetIndexesEmpty(preset.getPresetContext(), preset.getPresetNumber(), presets.length)) {
                indexes = ZUtilities.fillIncrementally(new Integer[presets.length], preset.getPresetNumber().intValue());
                PresetContextMacros.loadPresetsToContext(presets, indexes, preset.getPresetContext(), sampleIndexMap, true);
                return;
            }
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("Problem with preset context");
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("No such preset");
        }
        if (presets.length > 1) {
            opt1 = "At user locations " + df.format(preset.getPresetNumber()) + " - " + df.format(preset.getPresetNumber().intValue() + presets.length - 1);
            opt2 = "At first " + presets.length + " empty user locations searching from " + df.format(preset.getPresetNumber());
        } else {
            opt1 = "At location " + df.format(preset.getPresetNumber());
            opt2 = "At first empty user location searching from " + df.format(preset.getPresetNumber());
        }
        int res = UserMessaging.askOptions("Load Presets", (presets.length == 1 ? "Load 1 preset" : "Load " + presets.length + " presets "), new Object[]{opt1, opt2}, opt1);
        if (res == JOptionPane.CLOSED_OPTION)
            return;
        if (res == 0) {
            int si = preset.getPresetNumber().intValue();
            indexes = new Integer[presets.length];
            for (int i = 0; i < indexes.length; i++)
                indexes[i] = IntPool.get(si + i);

            PresetContextMacros.loadPresetsToContext(presets, indexes, preset.getPresetContext(), sampleIndexMap, true);
        } else if (res == 1) {
            try {
                List emptyList = preset.getPresetContext().findEmptyPresetsInContext(IntPool.get(presets.length), preset.getPresetNumber(), IntPool.get(DeviceContext.MAX_USER_PRESET));

                if (emptyList.size() < presets.length)
                    throw new CommandFailedException("Could not find " + presets.length + " empty user preset locations.");

                indexes = (Integer[]) emptyList.toArray(new Integer[emptyList.size()]);
                PresetContextMacros.loadPresetsToContext(presets, indexes, preset.getPresetContext(), sampleIndexMap, true);

            } catch (NoSuchContextException e) {
                throw new CommandFailedException("Problem with preset context");
            }
        }
    }

    private PresetPackage ppkg = null;
    private SamplePackage spkg = null;

    public int getMaxNumTargets() {
        return 1;
    }
}
