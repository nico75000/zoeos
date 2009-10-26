package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.Remotable;
import com.pcmsolutions.device.EMU.E4.RemoteUnreachableException;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTableModel;
import com.pcmsolutions.device.EMU.E4.gui.device.DefaultDeviceEnclosurePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext.PresetContextEnclosurePanel;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.gui.MenuBarProvider;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZCommandInvocationHelper;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.Linkable;
import com.pcmsolutions.system.threads.ZDBModifyThread;
import com.pcmsolutions.system.threads.ZDefaultThread;
import com.pcmsolutions.util.IntegerUseMap;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;


public class SampleContextEnclosurePanel extends DefaultDeviceEnclosurePanel implements ZDisposable, MenuBarProvider, ListSelectionListener, Linkable {
    protected DeviceContext device;
    private SampleContextEditorPanel scep;

    private JMenuBar menuBar;
    private JMenu processMenu;
    private JMenu locateMenu;
    private JMenu filterMenu;
    private JLabel statusLabel = new JLabel(" ");
    private SampleContext sampleContext;
    private static final String statusPrefix = " .. ";
    private static final String filterPostfix = " [ filter on ]";
    private static final String statusInfix = " / ";

    private boolean supressSelectionChange = false;

    private PresetContextEnclosurePanel presetContextEnclosurePanel;

    private Timer freeMemTimer;
    private static int freeMemInterval = 10000;

    private final Action sampleDefrag = new AbstractAction("Defragment Sample Memory") {
        public void actionPerformed(ActionEvent e) {
            if (sampleContext != null)
                new ZDBModifyThread("Defragment Sample Memory") {
                    public void run() {
                        try {
                            sampleContext.getDeviceContext().sampleMemoryDefrag(true);
                        } catch (ZDeviceNotRunningException e1) {
                            e1.printStackTrace();
                        } catch (RemoteUnreachableException e1) {
                            e1.printStackTrace();
                        }
                    }
                }.start();
        }
    };

    /*   public PresetContextEnclosurePanel getPresetContextEnclosurePanel() {
           return presetContextEnclosurePanel;
       }

       public void setPresetContextEnclosurePanel(PresetContextEnclosurePanel presetContextEnclosurePanel) {
           this.presetContextEnclosurePanel = presetContextEnclosurePanel;
       }
      */
    public void init(DeviceContext device) throws Exception {
        this.device = device;
        scep = new SampleContextEditorPanel(sampleContext = device.getDefaultSampleContext());
        super.init(device, scep);
        setupMenu();
        scep.getSampleContextTable().getSelectionModel().addListSelectionListener(this);
        setStatus(scep.getSampleContextTable().getSelectedRowCount() + statusInfix + scep.getSampleContextTable().getRowCount() + " samples selected");
    }

    protected void buildRunningPanel() {
        super.buildRunningPanel();
        runningPanel.add(statusLabel, BorderLayout.NORTH);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    public void setStatus(String status) {
        if (status == null)
            status = " ";
        if (((SampleContextTableModel) scep.getSampleContextTable().getModel()).getContextFilter() != SampleContextTableModel.allPassFilter)
            this.statusLabel.setText(statusPrefix + status + filterPostfix);
        else
            this.statusLabel.setText(statusPrefix + status);
    }

    public String getStatus() {
        return statusLabel.getText();
    }

    public SampleContext getSampleContext() {
        return sampleContext;
    }

    private void setupMenu() {
        // PROCESS MENU
        processMenu = new JMenu("Process");
        processMenu.setMnemonic(KeyEvent.VK_P);
        finalizeProcessMenu();

        // SELECT MENU
        locateMenu = new JMenu("Select");
        locateMenu.setMnemonic(KeyEvent.VK_S);

        JMenuItem jmi;
        jmi = new JMenuItem(new AbstractAction("Index (goto)") {
            public void actionPerformed(ActionEvent e) {
                String input = (String) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Sample Index?", "Goto Sample Index", JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (input != null)
                    try {
                        int si = Integer.parseInt(input);
                        scrollToSample(IntPool.get(si), true);
                    } catch (NumberFormatException e1) {
                        UserMessaging.showInfo("Not found");
                        return;
                    }
            }
        });
        jmi.setMnemonic(KeyEvent.VK_I);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("First Empty") {
            public void actionPerformed(ActionEvent e) {
                try {
                    Integer empty = scep.getSampleContext().firstEmptySampleInContext();
                    if (scrollToSample(empty, true))
                        return;
                    else if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "The first empty sample is not visible under the current filter. Remove filter and select?", "Remove Sample Filter", JOptionPane.YES_NO_OPTION) == 0) {
                        ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(null);
                        ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                        scrollToSample(empty, true);
                        return;
                    }

                } catch (NoSuchContextException e1) {
                    e1.printStackTrace();
                } catch (NoSuchSampleException e1) {
                    e1.printStackTrace();
                }
                UserMessaging.showInfo("No empty sample found");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_E);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Using Regex on name") {
            public void actionPerformed(ActionEvent e) {
                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        String regex = (String) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Regular Expression?", "Select by Regular Expression on Sample Name", JOptionPane.QUESTION_MESSAGE, null, null, null);
                        if (regex == null)
                            return;
                        Integer first = scep.getSampleContextTable().selectSamplesByRegex(regex, false, false, true);
                        if (first != null)
                            scrollToSample(first, false);
                        else {
                            UserMessaging.showInfo("Nothing to select");
                            return;
                        }
                    }
                });
            }
        });
        jmi.setMnemonic(KeyEvent.VK_R);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Using Regex on indexed name") {
            public void actionPerformed(ActionEvent e) {
                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        String regex = (String) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Regular Expression?", "Select by Regular Expression on Indexed Sample Name", JOptionPane.QUESTION_MESSAGE, null, null, null);
                        if (regex == null)
                            return;
                        Integer first = scep.getSampleContextTable().selectSamplesByRegex(regex, false, true, true);
                        if (first != null)
                            scrollToSample(first, false);
                        else {
                            UserMessaging.showInfo("Nothing to select");
                            return;
                        }
                    }
                });
            }
        });
        jmi.setMnemonic(KeyEvent.VK_X);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Referenced by current preset selection") {
            public void actionPerformed(ActionEvent e) {
                final ReadablePreset[] rps = PresetContextMacros.extractReadablePresets(presetContextEnclosurePanel.getPresetContextTable().getSelObjects());
                try {
                    if (!PresetContextMacros.confirmInitializationOfPresets(rps))
                        return;
                    new ZDefaultThread("Operation: Referenced by current preset selection") {
                        public void run() {
                            try {
                                final IntegerUseMap smpls = PresetContextMacros.getPresetSampleUsage(rps);
                                if (smpls.size() == 0 || (smpls.size() == 1 && smpls.getIntegers()[0].intValue() == 0)) {
                                    UserMessaging.showInfo("Nothing to select");
                                    return;
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        if (!UserMessaging.askYesNo("Referenced samples determined. Select now?"))
                                            return;
                                        if (!scep.getSampleContextTable().showingAllSamples(smpls.getIntegers())) {
                                            if (UserMessaging.askYesNo("Some of the selected samples will not be visible under the current filter. Remove filter before performing selection?")) {
                                                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(null);
                                                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                                            }
                                        }
                                        runWithSelectionHandlingSupressed(new Runnable() {
                                            public void run() {
                                                Integer[] smpls2 = smpls.getIntegers();
                                                scep.getSampleContextTable().clearSelection();
                                                scep.getSampleContextTable().addSamplesToSelection(smpls2);
                                            }
                                        });
                                    }
                                });

                            } catch (NoSuchPresetException e1) {
                                UserMessaging.showOperationFailed("Preset not found");
                            }
                        }
                    }.start();
                } catch (NoSuchPresetException e1) {
                    UserMessaging.showOperationFailed("Preset not found");
                }
            }
        });
        jmi.setMnemonic(KeyEvent.VK_P);
        locateMenu.add(jmi);

        /*   jmi = new JMenuItem(new AbstractAction("Not referenced by user presets") {
               public void actionPerformed(ActionEvent e) {
                   new ZDBModifyThread("Not referenced by user presets") {
                       public void run() {
                           try {
                               int ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "This will require all relevant presets to be initialized. Proceed?", "Select Unreferenced Samples", JOptionPane.YES_NO_OPTION);
                               if (ok == 0) {
                                   final IntegerUseMap useMap = sampleContext.getDeviceContext().getDefaultPresetContext().getSampleIndexesInUseForUserPresets();
                                   SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           runWithSelectionHandlingSupressed(new Runnable() {
                                               public void run() {
                                                   int ok;
                                                   ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Unrefereneced samples determined. Select now?", "Select Unreferenced Samples", JOptionPane.YES_NO_OPTION);
                                                   if (ok == 0) {
                                                       if (!scep.getSampleContextTable().showingAllSamples((Integer[]) useMap.getUsedIntegerSet().toArray(new Integer[useMap.size()])))
                                                       // if (((SampleContextTableModel) scep.getSampleContextTable().getModel()).getContextFilter() != SampleContextTableModel.allPassFilter)
                                                           if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Some of the selected samples will not be visible under the current filter. Remove filter before performing selection?", "Remove Sample Filter", JOptionPane.YES_NO_OPTION) == 0) {
                                                               ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(null);
                                                               ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                                                           }
                                                       scep.getSampleContextTable().selectAllSamplesExcluded(useMap.getUsedIntegerSet());
                                                   }
                                               }
                                           });
                                       }
                                   });
                               }
                           } catch (ZDeviceNotRunningException e1) {
                               e1.printStackTrace();
                           } catch (NoSuchContextException e1) {
                               e1.printStackTrace();
                           }
                       }
                   }.stateStart();
               }
           });
           jmi.setMnemonic(KeyEvent.VK_U);
           locateMenu.addDesktopElement(jmi);

           jmi = new JMenuItem(new AbstractAction("Not referenced by all presets") {
               public void actionPerformed(ActionEvent e) {
                   new ZDBModifyThread("Not referenced by all presets") {
                       public void run() {
                           try {
                               int ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "This will require all relevant presets to be initialized. Proceed?", "Select Unreferenced Samples", JOptionPane.YES_NO_OPTION);
                               if (ok == 0) {
                                   final IntegerUseMap useMap = sampleContext.getDeviceContext().getDefaultPresetContext().getSampleIndexesInUseForAllPresets();
                                   SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           runWithSelectionHandlingSupressed(new Runnable() {
                                               public void run() {
                                                   int ok;
                                                   ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Unrefereneced samples determined. Select now?", "Select Unreferenced Samples", JOptionPane.YES_NO_OPTION);
                                                   if (ok == 0) {
                                                       if (!scep.getSampleContextTable().showingAllSamples((Integer[]) useMap.getUsedIntegerSet().toArray(new Integer[useMap.size()])))
                                                       //if (((SampleContextTableModel) scep.getSampleContextTable().getModel()).getContextFilter() != SampleContextTableModel.allPassFilter)
                                                           if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Some of the selected samples will not be visible under the current filter. Remove filter before performing selection?", "Remove Sample Filter", JOptionPane.YES_NO_OPTION) == 0) {
                                                               ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(null);
                                                               ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                                                           }
                                                       scep.getSampleContextTable().selectAllSamplesExcluded(useMap.getUsedIntegerSet());
                                                   }
                                               }
                                           });
                                       }
                                   });
                               }
                           } catch (ZDeviceNotRunningException e1) {
                               e1.printStackTrace();
                           } catch (NoSuchContextException e1) {
                               e1.printStackTrace();
                           }
                       }
                   }.stateStart();
               }
           });
           jmi.setMnemonic(KeyEvent.VK_X);
           locateMenu.addDesktopElement(jmi);
           */
        jmi = new JMenuItem(new AbstractAction("All") {
            public void actionPerformed(ActionEvent e) {
                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        scep.getSampleContextTable().selectAll();
                    }
                });
            }
        });
        jmi.setMnemonic(KeyEvent.VK_A);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Invert") {
            public void actionPerformed(ActionEvent e) {
                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        scep.getSampleContextTable().invertSelection();
                    }
                });
            }
        });
        jmi.setMnemonic(KeyEvent.VK_V);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Clear") {
            public void actionPerformed(ActionEvent e) {
                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        scep.getSampleContextTable().clearSelection();
                    }
                });
            }
        });
        jmi.setMnemonic(KeyEvent.VK_C);
        locateMenu.add(jmi);

        // FILTER MENU
        filterMenu = new JMenu("Filter");
        filterMenu.setMnemonic(KeyEvent.VK_F);

        jmi = new JMenuItem(new AbstractAction("All (reset)") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(null);
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                setStatus(scep.getSampleContextTable().getSelectedRowCount() + " of " + scep.getSampleContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_A);
        jmi.setSelected(true);
        filterMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Non-Empty") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (!wasFilteredPreviously || name != null && name.trim().equals(DeviceContext.EMPTY_SAMPLE))
                            return false;
                        return true;
                    }

                    public String getFilterName() {
                        return "Non-Empty Samples";
                    }

                });
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                setStatus(scep.getSampleContextTable().getSelectedRowCount() + " of " + scep.getSampleContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_N);
        filterMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Empty") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && name != null && name.trim().equals(DeviceContext.EMPTY_SAMPLE))
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "Empty Samples";
                    }

                });
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                setStatus(scep.getSampleContextTable().getSelectedRowCount() + " of " + scep.getSampleContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_E);
        filterMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("User") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && index.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "User Samples";
                    }

                });
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                setStatus(scep.getSampleContextTable().getSelectedRowCount() + " of " + scep.getSampleContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_U);
        filterMenu.add(jmi);


        jmi = new JMenuItem(new AbstractAction("ROM") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && index.intValue() > DeviceContext.MAX_USER_SAMPLE)
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "ROM Samples";
                    }

                });
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                setStatus(scep.getSampleContextTable().getSelectedRowCount() + " of " + scep.getSampleContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_R);
        filterMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Selected") {
            public void actionPerformed(ActionEvent e) {
                Object[] sobjs = scep.getSampleContextTable().getSelObjects();
                if (sobjs.length < 1) {
                    UserMessaging.showInfo("No samples selected");
                    return;
                }
                final HashMap map_sobjs = new HashMap();
                for (int i = 0,j = sobjs.length; i < j; i++)
                    map_sobjs.put(((ReadableSample) sobjs[i]).getSampleNumber(), null);

                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && map_sobjs.containsKey(index))
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "Selected Samples";
                    }
                });
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                setStatus(scep.getSampleContextTable().getSelectedRowCount() + " of " + scep.getSampleContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_S);
        filterMenu.add(jmi);

        final JMenu freeMem = new JMenu("");
        freeMem.setEnabled(false);
        freeMemTimer = new Timer(freeMemInterval, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Sample Memory Monitor") {
                    public void run() {
                        try {
                            final Remotable.SampleMemory sm = sampleContext.getDeviceContext().getSampleMemory();
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    freeMem.setText((sm.getSampleFreeMemory().intValue() * 10) + " Kb");
                                }
                            });

                        } catch (RemoteUnreachableException e1) {
                            System.out.println("timer - Remote Unreachable");
                        } catch (ZDeviceNotRunningException e1) {
                            System.out.println("timer - device not running");
                        }
                    }
                }.start();
            }
        });
        freeMemTimer.setCoalesce(true);
        freeMemTimer.start();

        // FINALIZE MENU SETUP
        menuBar = new JMenuBar();
        menuBar.add(processMenu);
        menuBar.add(locateMenu);
        menuBar.add(filterMenu);
        menuBar.add(freeMem);
    }

    public boolean scrollToSample(Integer sample, boolean select) {
        SampleContextTableModel sctm = ((SampleContextTableModel) scep.getSampleContextTable().getModel());
        int row = sctm.getRowForSample(sample);
        if (row != -1) {
            Rectangle cellRect = scep.getSampleContextTable().getCellRect(row, 0, true);
            scep.scrollRectToVisible(cellRect);
            if (select) {
                scep.getSampleContextTable().setRowSelectionInterval(row, row);
                scep.getSampleContextTable().setColumnSelectionInterval(0, 0);
            }
            return true;
        }
        return false;
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!supressSelectionChange)
            handleSelectionChange();
    }

    private void handleSelectionChange() {
        setStatus(scep.getSampleContextTable().getSelectedRowCount() + statusInfix + scep.getSampleContextTable().getRowCount() + " samples selected");
        processMenu.removeAll();
        JMenu m = ZCommandInvocationHelper.getMenu(scep.getSampleContextTable().getSelObjects(), null, null, "Process");
        Component[] comps = m.getMenuComponents();
        for (int i = 0,j = comps.length; i < j; i++)
            processMenu.add(comps[i]);

        finalizeProcessMenu();
    }

    private void finalizeProcessMenu() {
        if (processMenu.getMenuComponentCount() != 0)
            processMenu.addSeparator();

        processMenu.add(sampleDefrag);
    }

    private void runWithSelectionHandlingSupressed(Runnable r) {
        supressSelectionChange = true;
        try {
            r.run();
        } finally {
            supressSelectionChange = false;
            handleSelectionChange();
        }
    }

    private void runOnEDTWithSelectionHandlingSupressed(final Runnable r) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                supressSelectionChange = true;
                try {
                    r.run();
                } finally {
                    supressSelectionChange = false;
                    handleSelectionChange();
                }
            }
        });
    }

    public SampleContextTable getSampleContextTable() {
        return scep.getSampleContextTable();
    }

    public void zDispose() {
        scep.getSampleContextTable().getSelectionModel().removeListSelectionListener(this);
        super.zDispose();
        freeMemTimer.stop();
        device = null;
        scep = null;
        presetContextEnclosurePanel = null;
        sampleContext = null;
    }

    public boolean isMenuBarAvailable() {
        return true;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public void linkTo(Object o) throws Linkable.InvalidLinkException {
        if (o instanceof PresetContextEnclosurePanel)
            presetContextEnclosurePanel = (PresetContextEnclosurePanel) o;
        else
            throw new Linkable.InvalidLinkException("SampleContextEnclosurePanel cannot link to " + o.getClass());
    }
}
