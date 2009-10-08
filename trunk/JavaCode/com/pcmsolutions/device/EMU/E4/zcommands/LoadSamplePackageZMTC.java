package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.packaging.PackageHeaderInfoPane;
import com.pcmsolutions.device.EMU.E4.gui.packaging.SamplePackageHeaderInfoPane;
import com.pcmsolutions.device.EMU.E4.packaging.PackageFactory;
import com.pcmsolutions.device.EMU.E4.packaging.SamplePackage;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedSample;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.ContextEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.preferences.Impl_ZStringPref;
import com.pcmsolutions.system.preferences.ZStringPref;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class LoadSamplePackageZMTC extends AbstractContextEditableSampleZMTCommand {
    private static JFileChooser fc;
    private static final Preferences prefs = Preferences.userNodeForPackage(LoadSamplePackageZMTC.class);
    public static final ZStringPref ZPREF_lastDir = new Impl_ZStringPref(prefs, "lastLoadSamplePackageDir", Zoeos.getHomeDir().getAbsolutePath());

    private static void assertChooser() {
        if (fc == null) {
            fc = new JFileChooser();
            final PackageHeaderInfoPane phip = new SamplePackageHeaderInfoPane(null);

            JPanel p = new JPanel();
            p.add(new JScrollPane(phip));
            p.setBorder(new TitledBorder("Package Header"));

            fc.setAccessory(p);
            fc.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                        SamplePackage pkg = null;
                        File sf = fc.getSelectedFile();
                        if (sf != null && !sf.isDirectory())
                            try {
                                pkg = PackageFactory.extractSamplePackage(fc.getSelectedFile());
                                phip.setHeader(pkg.getHeader());
                            } catch (CommandFailedException e) {
                                phip.setText("error");
                            }
                        else
                            phip.setHeader(null);
                    } else {
                        phip.setHeader(null);
                    }
                }
            });
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
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

    public LoadSamplePackageZMTC() {
        init("Load" + ZUtilities.DOT_POSTFIX, "Load a sample package", null, null);
    }

    public String getMenuPathString() {
        return ";Packaging";
    }

    protected ContextEditableSample sample;

    public int getMaxNumTargets() {
        return 1;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        sample = getTargets()[0];
        File f = null;

        synchronized (this.getClass()) {
            assertChooser();
            int retval = fc.showDialog(ZoeosFrame.getInstance(), "Load");
            if (retval == JFileChooser.APPROVE_OPTION) {
                f = fc.getSelectedFile();
                ZPREF_lastDir.putValue(f.getParentFile().getAbsolutePath());
            }
        }

        if (f != null)
            loadSamplePackage(PackageFactory.extractSamplePackage(f), f.getParent(), sample);
    }

    private static void updateIndexMap(Map index2IndexMap, Map file2IndexMap, Integer[] newIndexes, File[] files) {
        for (int i = 0; i < files.length; i++) {
            Integer ind = (Integer) file2IndexMap.get(files[i]);
            if (ind == null)
                throw new IllegalArgumentException("missing index mapping");
            index2IndexMap.put(ind, newIndexes[i]);
        }
    }

    // returns Integer -> Integer
    // returns null if op cancelled
    public static Map loadSamplePackage(SamplePackage pkg, String location, ContextEditableSample sample) throws CommandFailedException {
        DecimalFormat df = new DecimalFormat("0000");

        Map index2IndexMap = new HashMap();
        IsolatedSample[] samples = pkg.getSamples();
        File[] files = SampleContextMacros.extractFiles(samples);
        Integer[] indexes = SampleContextMacros.extractIndexes(samples);
        ZUtilities.prefixPath(files, location); // will ignore nulls

        // initialize index2IndexMap
        for (int i = 0; i < files.length; i++)
            index2IndexMap.put(indexes[i], indexes[i]);

        // some files may be null (typically in the case of ROM samples )
        int nonNullFiles = files.length - ZUtilities.numNulls(files);

        Map file2IndexMap = new TreeMap();
        for (int i = 0; i < samples.length; i++)
            if (files[i] != null)
                file2IndexMap.put(files[i], indexes[i]);

        File[] nnFiles = (File[]) file2IndexMap.keySet().toArray(new File[file2IndexMap.keySet().size()]);

        if (sample.getSampleNumber().intValue() + nnFiles.length > DeviceContext.BASE_ROM_SAMPLE)
            throw new CommandFailedException("Not enough user locations on or after S" + df.format(sample.getSampleNumber()) + " to load package");

        String opt1;
        String opt2;
        try {
            if (SampleContextMacros.areSampleIndexesEmpty(sample.getSampleContext(), sample.getSampleNumber(), nonNullFiles)) {
                indexes = ZUtilities.fillIncrementally(new Integer[nonNullFiles], sample.getSampleNumber().intValue());
                SampleContextMacros.loadSamplesToContext(nnFiles, indexes, sample.getSampleContext(), true, true);
                updateIndexMap(index2IndexMap, file2IndexMap, indexes, nnFiles);
                return index2IndexMap;
            }
        } catch (NoSuchSampleException e) {
            throw new CommandFailedException("No such sample");
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("Problem with sample context");
        }
        if (nonNullFiles > 1) {
            opt1 = "At locations " + df.format(sample.getSampleNumber()) + " - " + df.format(sample.getSampleNumber().intValue() + nonNullFiles - 1);
            opt2 = "At first " + nonNullFiles + " empty locations searching from " + df.format(sample.getSampleNumber());
        } else {
            opt1 = "At location " + df.format(sample.getSampleNumber());
            opt2 = "At first empty location searching from " + df.format(sample.getSampleNumber());
        }
        int res = JOptionPane.showOptionDialog(ZoeosFrame.getInstance(), (nonNullFiles == 1 ? "Load 1 sample" : "Load " + nonNullFiles + " samples "), "Load Samples", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{opt1, opt2}, opt1);
        if (res == JOptionPane.CLOSED_OPTION)
            return null;
        if (res == 0) {
            int si = sample.getSampleNumber().intValue();
            indexes = new Integer[nonNullFiles];
            for (int i = 0; i < indexes.length; i++)
                indexes[i] = IntPool.get(si + i);

            SampleContextMacros.loadSamplesToContext(nnFiles, indexes, sample.getSampleContext(), true, true);
            updateIndexMap(index2IndexMap, file2IndexMap, indexes, nnFiles);
        } else if (res == 1) {
            try {
                List emptyList = sample.getSampleContext().findEmptySamplesInContext(nonNullFiles, sample.getSampleNumber(), IntPool.get(DeviceContext.MAX_USER_SAMPLE));

                if (emptyList.size() < nonNullFiles)
                    throw new CommandFailedException("Could not find " + nonNullFiles + " empty user sample locations.");

                indexes = (Integer[]) emptyList.toArray(new Integer[emptyList.size()]);
                SampleContextMacros.loadSamplesToContext(nnFiles, indexes, sample.getSampleContext(), true, true);
                updateIndexMap(index2IndexMap, file2IndexMap, indexes, nnFiles);

            } catch (NoSuchContextException e) {
                throw new CommandFailedException("Problem with sample context");
            }
        }
        return index2IndexMap;
    }

    /*   public static void loadFilesToContext(final File[] files, ContextEditableSample sample) throws CommandFailedException {
           if (files.length > 0) {
               DecimalFormat df = new DecimalFormat("0000");

               if (sample.getSampleNumber().intValue() + files.length > DeviceContext.MAX_USER_SAMPLE) {
                   JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Not enough room beyond location " + df.format(sample.getSampleNumber()) + " to load " + files.length + " samples", "Problem", JOptionPane.ERROR_MESSAGE);
                   return;
               }
               //int mc = ZUtilities.getFileCountForPattern(files, AudioUtilities.sampleIndexPattern);
               SampleContext sc = sample.getSampleContext();
               String opt1;
               String opt2;
               try {
                   if (SampleContextMacros.areSampleIndexesEmpty(sc, sample.getSampleNumber(), files.length)) {
                       SampleContextMacros.loadSamplesToContext(files, ZUtilities.fillIncrementally(new Integer[files.length], sample.getSampleNumber().intValue()), sample.getSampleContext(), true, true);
                       return;
                   }
               } catch (NoSuchSampleException e) {
                   throw new CommandFailedException("No such sample");
               } catch (NoSuchContextException e) {
                   throw new CommandFailedException("Problem with sample context");
               }
               if (files.length > 1) {
                   opt1 = "At locations " + df.format(sample.getSampleNumber()) + " - " + df.format(sample.getSampleNumber().intValue() + files.length - 1);
                   opt2 = "At first " + files.length + " empty locations searching from " + df.format(sample.getSampleNumber());
               } else {
                   opt1 = "At location " + df.format(sample.getSampleNumber());
                   opt2 = "At first empty location searching from " + df.format(sample.getSampleNumber());
               }
               int res = JOptionPane.showOptionDialog(ZoeosFrame.getInstance(), (files.length == 1 ? "Load 1 sample" : "Load " + files.length + " samples "), "Load Samples", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{opt1, opt2}, opt1);
               if (res == 0) {
                   int si = sample.getSampleNumber().intValue();
                   Integer[] indexes = new Integer[files.length];
                   for (int i = 0; i < indexes.length; i++) {
                       indexes[i] = IntPool.get(si + i);
                   }
                   SampleContextMacros.loadSamplesToContext(files, indexes, sample.getSampleContext(), true, true);
               } else if (res == 1) {
                   try {
                       List emptyList = sc.findEmptySamplesInContext(files.length, sample.getSampleNumber());
                       if (emptyList.size() < files.length) {
                           JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Could not find " + files.length + " empty sample locations. Operation aborted.", "Problem", JOptionPane.ERROR_MESSAGE);
                           return;
                       }
                       SampleContextMacros.loadSamplesToContext(files, (Integer[]) emptyList.toArray(new Integer[emptyList.size()]), sample.getSampleContext(), true, true);
                   } catch (NoSuchContextException e) {
                       throw new CommandFailedException("Problem with sample context");
                   }
               }
           }
       } */
}

