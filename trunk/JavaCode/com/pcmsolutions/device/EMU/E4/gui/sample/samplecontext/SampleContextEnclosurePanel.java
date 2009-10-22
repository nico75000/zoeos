package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.ViewMessaging;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTableModel;
import com.pcmsolutions.device.EMU.E4.gui.device.DefaultDeviceEnclosurePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext.PresetContextEnclosurePanel;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.remote.Remotable;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.gui.FrameMenuBarProvider;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZCommandFactory;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.desktop.ViewMessageReceiver;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.Linkable;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;
import com.pcmsolutions.util.IntegerUseMap;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;


public class SampleContextEnclosurePanel extends DefaultDeviceEnclosurePanel implements ZDisposable, FrameMenuBarProvider, ListSelectionListener, Linkable, ViewMessageReceiver {
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

    private PresetContextEnclosurePanel presetContextEnclosurePanel;

    private Timer freeMemTimer;
    private static int freeMemInterval = 20000;

    private final Action sampleDefrag = new AbstractAction("Defragment Sample Memory") {
        public void actionPerformed(ActionEvent e) {
            if (sampleContext != null)
                try {
                    sampleContext.getDeviceContext().sampleMemoryDefrag(true).post();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
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
        updateStatus();
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

        final String firstEmpty = "First empty";
        jmi = new JMenuItem(new AbstractAction(firstEmpty) {
            public void actionPerformed(ActionEvent e) {
                try {
                    Integer empty = scep.getSampleContext().firstEmpty();
                    if (empty != null) {
                        if (!scep.getSampleContextTable().showingAllIndexes(new Integer[]{empty}))
                            ((SampleContextTableModel) scep.getSampleContextTable().getModel()).addIndexesToCurrentContextFilter(new Integer[]{empty}, firstEmpty);
                        scrollToSample(empty, true);
                    } else
                        UserMessaging.showInfo("No empty sample found");
                } catch (DeviceException e1) {
                    e1.printStackTrace();
                }
            }
        });
        jmi.setMnemonic(KeyEvent.VK_E);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Using regex on name") {
            public void actionPerformed(ActionEvent e) {
                String regex = (String) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Regular expression?", "Select by regex", JOptionPane.QUESTION_MESSAGE, null, null, null);
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
        jmi.setMnemonic(KeyEvent.VK_R);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Using regex on indexed name") {
            public void actionPerformed(ActionEvent e) {
                String regex = (String) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Regular expression?", "Select by regex", JOptionPane.QUESTION_MESSAGE, null, null, null);
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
        jmi.setMnemonic(KeyEvent.VK_X);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Referenced by current preset selection") {
            public void actionPerformed(ActionEvent e) {
                final ReadablePreset[] rps = PresetContextMacros.extractReadablePresets(presetContextEnclosurePanel.getPresetContextTable().getSelObjects());
                try {
                    if (!PresetContextMacros.confirmInitializationOfPresets(rps))
                        return;
                    device.getQueues().generalQ().getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
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
                                        if (!scep.getSampleContextTable().showingAllIndexes(smpls.getIntegers())) {
                                            ((SampleContextTableModel) scep.getSampleContextTable().getModel()).addIndexesToCurrentContextFilter(smpls.getIntegers(), firstEmpty);
                                        }
                                        Integer[] smpls2 = smpls.getIntegers();
                                        scep.getSampleContextTable().clearSelection();
                                        scep.getSampleContextTable().addIndexesToSelection(smpls2);
                                    }
                                });

                            } catch (PresetException e1) {
                                UserMessaging.showOperationFailed("Failed");
                            }
                        }
                    }, "sampleReferencing").post();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        jmi.setMnemonic(KeyEvent.VK_P);
        locateMenu.add(jmi);

        /*   jmi = new JMenuItem(new AbstractAction("Not referenced by user presets") {
               public void actionPerformed(ActionEvent e) {
                   new ZDataModifyThread("Not referenced by user presets") {
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
                                                       // if (((SampleContextTableModel) scep.getSampleContextTable().getName()).getContextFilter() != SampleContextTableModel.allPassFilter)
                                                           if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Some of the selected samples will not be visible under the current filter. Remove filter before performing selection?", "Remove Sample Filter", JOptionPane.YES_NO_OPTION) == 0) {
                                                               ((SampleContextTableModel) scep.getSampleContextTable().getName()).setContextFilter(null);
                                                               ((SampleContextTableModel) scep.getSampleContextTable().getName()).refresh(false);
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
                   new ZDataModifyThread("Not referenced by all presets") {
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
                                                       //if (((SampleContextTableModel) scep.getSampleContextTable().getName()).getContextFilter() != SampleContextTableModel.allPassFilter)
                                                           if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Some of the selected samples will not be visible under the current filter. Remove filter before performing selection?", "Remove Sample Filter", JOptionPane.YES_NO_OPTION) == 0) {
                                                               ((SampleContextTableModel) scep.getSampleContextTable().getName()).setContextFilter(null);
                                                               ((SampleContextTableModel) scep.getSampleContextTable().getName()).refresh(false);
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
                scep.getSampleContextTable().selectAll();
            }
        });
        jmi.setMnemonic(KeyEvent.VK_A);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Invert") {
            public void actionPerformed(ActionEvent e) {
                scep.getSampleContextTable().invertSelection();
            }
        });
        jmi.setMnemonic(KeyEvent.VK_V);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Clear") {
            public void actionPerformed(ActionEvent e) {
                scep.getSampleContextTable().clearSelection();
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
                //((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                updateStatus();
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
                //((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                updateStatus();
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
                // ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                updateStatus();
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
                // ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                updateStatus();
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
                //   ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                updateStatus();
            }
        });
        jmi.setMnemonic(KeyEvent.VK_R);
        filterMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Invert") {
            public void actionPerformed(ActionEvent e) {
                ((SampleContextTableModel) scep.getSampleContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously)
                            return false;
                        return true;
                    }

                    public String getFilterName() {
                        return "Invert current filter";
                    }

                });
                // ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                updateStatus();
            }
        });
        jmi.setMnemonic(KeyEvent.VK_I);
        filterMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Selected") {
            public void actionPerformed(ActionEvent e) {
                Object[] sobjs = scep.getSampleContextTable().getSelObjects();
                if (sobjs.length < 1) {
                    UserMessaging.showInfo("No samples selected");
                    return;
                }
                final HashMap map_sobjs = new HashMap();
                for (int i = 0, j = sobjs.length; i < j; i++)
                    map_sobjs.put(((ReadableSample) sobjs[i]).getIndex(), null);

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
                // ((SampleContextTableModel) scep.getSampleContextTable().getModel()).refresh(false);
                updateStatus();
            }
        });
        jmi.setMnemonic(KeyEvent.VK_S);
        filterMenu.add(jmi);

        final JMenu freeMem = new JMenu("");
        freeMem.setEnabled(false);
        freeMemTimer = new Timer(freeMemInterval, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sampleContext.getDeviceContext().getQueues().generalQ().getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
                            try {
                                final Remotable.SampleMemory sm = sampleContext.getDeviceContext().getSampleMemory();
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        freeMem.setText((sm.getSampleFreeMemory().intValue() * 10) + " Kb");
                                    }
                                });
                            } catch (Exception e1) {
                            }
                        }
                    }, "Sample memory monitor").post();
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                }
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
        int row = sctm.getRowForIndex(sample);
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
        if (!e.getValueIsAdjusting())
            handleSelectionChange();
    }

    private void handleSelectionChange() {
        updateStatus();
        processMenu.removeAll();
        JMenu m = ZCommandFactory.getMenu(scep.getSampleContextTable().getSelObjects(), "Process");
        Component[] comps = m.getMenuComponents();
        for (int i = 0, j = comps.length; i < j; i++)
            processMenu.add(comps[i]);

        finalizeProcessMenu();
    }

    private void finalizeProcessMenu() {
        if (processMenu.getMenuComponentCount() != 0)
            processMenu.addSeparator();

        processMenu.add(sampleDefrag);
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

    public boolean isFrameMenuBarAvailable() {
        return true;
    }

    public JMenuBar getFrameMenuBar() {
        return menuBar;
    }

    public void linkTo(Object o) throws Linkable.InvalidLinkException {
        if (o instanceof PresetContextEnclosurePanel)
            presetContextEnclosurePanel = (PresetContextEnclosurePanel) o;
        else
            throw new Linkable.InvalidLinkException("SampleContextEnclosurePanel cannot link to " + o.getClass());
    }

    public void receiveMessage(String msg) {
        if (msg.startsWith(ViewMessaging.MSG_ADD_SAMPLES_TO_SAMPLE_CONTEXT_FILTER)) {
            String[] intFields = ViewMessaging.extractFieldsFromMessage(msg);
            Integer[] samples = new Integer[intFields.length];
            for (int i = 0; i < intFields.length; i++)
                samples[i] = IntPool.get(Integer.parseInt(intFields[i]));
            SampleContextTableModel sctm = (SampleContextTableModel) scep.getSampleContextTable().getModel();
            sctm.addIndexesToCurrentContextFilter(samples, ViewMessaging.MSG_ADD_SAMPLES_TO_SAMPLE_CONTEXT_FILTER);
            //sctm.refresh(false);
        }
    }

    public boolean testCondition(String condition) {
        return false;
    }


    void updateStatus() {
        setStatus(scep.getSampleContextTable().getSelectedRowCount() + " of " + scep.getSampleContextTable().getRowCount() + " samples selected");
    }
}
