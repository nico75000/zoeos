package com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext;

import com.pcmsolutions.device.EMU.E4.*;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTableModel;
import com.pcmsolutions.device.EMU.E4.gui.device.DefaultDeviceEnclosurePanel;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextEnclosurePanel;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PresetContextEnclosurePanel extends DefaultDeviceEnclosurePanel implements ZDisposable, MenuBarProvider, ListSelectionListener, Linkable {
    protected DeviceContext device;
    private PresetContextEditorPanel pcep;

    private JMenuBar menuBar;
    private JMenu processMenu;
    private JMenu locateMenu;
    private JMenu filterMenu;
    private JLabel statusLabel = new JLabel(" ");
    private static final String statusPrefix = " .. ";
    private static final String filterPostfix = " [ filter on ]";
    private static final String statusInfix = " / ";
    private PresetContext presetContext;
    private SampleContextEnclosurePanel sampleContextEnclosurePanel;
    private boolean supressSelectionChange = false;

    private Timer freeMemTimer;
    private static int freeMemInterval = 10000;

    public void init(DeviceContext device) throws Exception {
        this.device = device;
        pcep = new PresetContextEditorPanel(presetContext = device.getDefaultPresetContext());
        super.init(device, pcep);
        setupMenu();
        pcep.getPresetContextTable().getSelectionModel().addListSelectionListener(this);
        setStatus(pcep.getPresetContextTable().getSelectedRowCount() + statusInfix + pcep.getPresetContextTable().getRowCount() + " presets selected");
    }

    protected void buildRunningPanel() {
        super.buildRunningPanel();
        runningPanel.add(statusLabel, BorderLayout.NORTH);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    public PresetContextTable getPresetContextTable() {
        return pcep.getPresetContextTable();
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!supressSelectionChange)
            handleSelectionChange();
    }

    private void finalizeProcessMenu() {
        if (processMenu.getMenuComponentCount() == 0)
            processMenu.setEnabled(false);
        else
            processMenu.setEnabled(true);
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

    private void handleSelectionChange() {
        setStatus(pcep.getPresetContextTable().getSelectedRowCount() + statusInfix + pcep.getPresetContextTable().getRowCount() + " presets selected");
        processMenu.removeAll();
        JMenu m = ZCommandInvocationHelper.getMenu(pcep.getPresetContextTable().getSelObjects(), null, null, "Process");
        Component[] comps = m.getMenuComponents();
        for (int i = 0,j = comps.length; i < j; i++)
            processMenu.add(comps[i]);

        finalizeProcessMenu();
    }

    public void setStatus(String status) {
        if (status == null)
            status = " ";
        if (((PresetContextTableModel) pcep.getPresetContextTable().getModel()).getContextFilter() != PresetContextTableModel.allPassFilter)
            this.statusLabel.setText(statusPrefix + status + filterPostfix);
        else
            this.statusLabel.setText(statusPrefix + status);
    }

    public String getStatus() {
        return statusLabel.getText();
    }

    public boolean scrollToPreset(Integer preset, boolean select) {
        PresetContextTableModel pctm = ((PresetContextTableModel) pcep.getPresetContextTable().getModel());
        int row = pctm.getRowForPreset(preset);
        if (row != -1) {
            Rectangle cellRect = pcep.getPresetContextTable().getCellRect(row, 0, true);
            pcep.scrollRectToVisible(cellRect);
            if (select) {
                pcep.getPresetContextTable().setRowSelectionInterval(row, row);
                pcep.getPresetContextTable().setColumnSelectionInterval(0, 0);
            }
            return true;
        }
        return false;
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
                String input = (String) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Preset Index?", "Goto Preset Index", JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (input != null)
                    try {
                        int pi = Integer.parseInt(input);
                        scrollToPreset(IntPool.get(pi), true);
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
                    Integer empty = pcep.getPresetContext().firstEmptyPresetInContext();
                    if (scrollToPreset(empty, true))
                        return;
                    else {
                        if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "The first empty preset is not visible under the current filter. Remove filter and select?", "Remove Preset Filter", JOptionPane.YES_NO_OPTION) == 0) {
                            ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(null);
                            ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                            scrollToPreset(empty, true);
                            return;
                        }
                    }

                } catch (NoSuchContextException e1) {
                    e1.printStackTrace();
                } catch (NoSuchPresetException e1) {
                    e1.printStackTrace();
                }
                UserMessaging.showInfo("No empty preset found");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_E);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Using Regex on name") {
            public void actionPerformed(ActionEvent e) {
                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        String regex = (String) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Regular Expression?", "Select by Regular Expression on Preset Name", JOptionPane.QUESTION_MESSAGE, null, null, null);
                        if (regex == null)
                            return;
                        Integer first = pcep.getPresetContextTable().selectPresetsByRegex(regex, false, false, true);
                        if (first != null)
                            scrollToPreset(first, false);
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
                        String regex = (String) JOptionPane.showInputDialog(ZoeosFrame.getInstance(), "Regular Expression?", "Select by Regular Expression on Indexed Preset Name", JOptionPane.QUESTION_MESSAGE, null, null, null);
                        if (regex == null)
                            return;
                        Integer first = pcep.getPresetContextTable().selectPresetsByRegex(regex, false, true, true);
                        if (first != null)
                            scrollToPreset(first, false);
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

        class PresetReferencingRunnable implements Runnable {
            private ReadablePreset[] currPresets;
            private ReadableSample[] currSamples;

            public PresetReferencingRunnable(ReadablePreset[] currPresets, ReadableSample[] currSamples) {
                this.currPresets = currPresets;
                this.currSamples = currSamples;
            }

            public void run() {
                try {
                    if (!PresetContextMacros.confirmInitializationOfPresets(currPresets))
                        return;
                    new ZDefaultThread("Operation: Presets referencing current sample selection") {
                        public void run() {
                            try {
                                final ReadablePreset[] filtPresets = PresetContextMacros.filterPresetsReferencingSamples(currPresets, currSamples);
                                if (filtPresets.length == 0) {
                                    UserMessaging.showInfo("Nothing to select");
                                    return;
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        if (!UserMessaging.askYesNo("Referencing presets determined. Select now?"))
                                            return;
                                        if (!pcep.getPresetContextTable().showingAllPresets(PresetContextMacros.extractPresetIndexes(filtPresets))) {
                                            if (UserMessaging.askYesNo("Some of the selected presets will not be visible under the current filter. Remove filter before performing selection?")) {
                                                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(null);
                                                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                                            }
                                        }
                                        runWithSelectionHandlingSupressed(new Runnable() {
                                            public void run() {
                                                Integer[] fp = PresetContextMacros.extractPresetIndexes(filtPresets);
                                                pcep.getPresetContextTable().clearSelection();
                                                pcep.getPresetContextTable().addPresetsToSelection(fp);
                                            }
                                        });
                                    }
                                });
                            } catch (NoSuchPresetException e1) {
                                UserMessaging.showOperationFailed("Preset not found");
                                return;
                            }
                        }
                    }.start();
                } catch (NoSuchPresetException e1) {
                    UserMessaging.showOperationCancelled("Preset not found");
                }
            }
        }

        jmi = new JMenuItem(new AbstractAction("Referencing current sample selection") {
            public void actionPerformed(ActionEvent e) {
                final ReadableSample[] currSamples = SampleContextMacros.extractReadableSamples(sampleContextEnclosurePanel.getSampleContextTable().getSelObjects());
                try {
                    List cp = pcep.getPresetContextTable().getPresetContext().getContextPresets();
                    final ReadablePreset[] currPresets = (ReadablePreset[]) cp.toArray(new ReadablePreset[cp.size()]);
                    new PresetReferencingRunnable(currPresets, currSamples).run();
                } catch (NoSuchContextException e1) {
                    UserMessaging.showOperationFailed("No such context");
                }
            }
        });
        jmi.setMnemonic(KeyEvent.VK_S);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("In current selection referencing current sample selection") {
            public void actionPerformed(ActionEvent e) {
                final ReadableSample[] currSamples = SampleContextMacros.extractReadableSamples(sampleContextEnclosurePanel.getSampleContextTable().getSelObjects());
                final ReadablePreset[] currPresets = PresetContextMacros.extractReadablePresets(pcep.getPresetContextTable().getSelObjects());
                new PresetReferencingRunnable(currPresets, currSamples).run();
            }
        });
        jmi.setMnemonic(KeyEvent.VK_L);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("All") {
            public void actionPerformed(ActionEvent e) {
                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        pcep.getPresetContextTable().selectAll();
                    }
                });
            }
        });
        jmi.setMnemonic(KeyEvent.VK_A);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Clear") {
            public void actionPerformed(ActionEvent e) {
                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        pcep.getPresetContextTable().clearSelection();
                    }
                });
            }
        });
        jmi.setMnemonic(KeyEvent.VK_C);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Invert") {
            public void actionPerformed(ActionEvent e) {
                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        pcep.getPresetContextTable().invertSelection();
                    }
                });
            }
        });
        jmi.setMnemonic(KeyEvent.VK_V);
        locateMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Deep") {
            public void actionPerformed(ActionEvent e) {

                runWithSelectionHandlingSupressed(new Runnable() {
                    public void run() {
                        Object[] sobjs = pcep.getPresetContextTable().getSelObjects();
                        Set totSet = new HashSet();
                        for (int i = 0; i < sobjs.length; i++)
                            if (sobjs[i] instanceof ReadablePreset)
                                try {
                                    totSet.addAll(((ReadablePreset) sobjs[i]).getPresetSet());
                                } catch (NoSuchPresetException e1) {
                                } catch (PresetEmptyException e1) {
                                }
                        if (!pcep.getPresetContextTable().showingAllPresets((Integer[]) totSet.toArray(new Integer[totSet.size()])))
                            if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Some of the selected presets will not be visible under the current filter. Remove filter before performing selection?", "Remove Preset Filter", JOptionPane.YES_NO_OPTION) == 0) {
                                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(null);
                                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                            }
                        pcep.getPresetContextTable().clearSelection();
                        pcep.getPresetContextTable().addPresetsToSelection((Integer[]) totSet.toArray(new Integer[totSet.size()]));
                    }
                });
            }
        });
        jmi.setMnemonic(KeyEvent.VK_D);
        locateMenu.add(jmi);

        // FILTER MENU
        filterMenu = new JMenu("Filter");
        filterMenu.setMnemonic(KeyEvent.VK_F);

        jmi = new JMenuItem(new AbstractAction("All (reset)") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(null);
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                setStatus(pcep.getPresetContextTable().getSelectedRowCount() + " of " + pcep.getPresetContextTable().getRowCount() + " presets selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_A);
        jmi.setSelected(true);
        filterMenu.add(jmi);


        jmi = new JMenuItem(new AbstractAction("Non-Empty") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (!wasFilteredPreviously || name != null && name.trim().equals(DeviceContext.EMPTY_PRESET))
                            return false;
                        return true;
                    }

                    public String getFilterName() {
                        return "Non-Empty Presets";
                    }

                });
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                setStatus(pcep.getPresetContextTable().getSelectedRowCount() + " of " + pcep.getPresetContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_N);
        filterMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Empty") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && name != null && name.trim().equals(DeviceContext.EMPTY_PRESET))
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "Empty Presets";
                    }

                });
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                setStatus(pcep.getPresetContextTable().getSelectedRowCount() + " of " + pcep.getPresetContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_E);
        filterMenu.add(jmi);

        jmi = new JMenuItem(new AbstractAction("Cached") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        try {
                            if (wasFilteredPreviously && (presetContext.isPresetInitialized(index) && !presetContext.isPresetEmpty(index)))
                                return true;
                        } catch (NoSuchPresetException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchContextException e1) {
                            e1.printStackTrace();
                        }
                        return false;
                    }

                    public String getFilterName() {
                        return "Initialized Presets";
                    }

                });
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                setStatus(pcep.getPresetContextTable().getSelectedRowCount() + " of " + pcep.getPresetContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_C);
        filterMenu.add(jmi);


        jmi = new JMenuItem(new AbstractAction("User") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && index.intValue() <= DeviceContext.MAX_USER_PRESET)
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "User Presets";
                    }

                });
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                setStatus(pcep.getPresetContextTable().getSelectedRowCount() + " of " + pcep.getPresetContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_U);
        //bg.addDesktopElement(jmi);
        filterMenu.add(jmi);


        jmi = new JMenuItem(new AbstractAction("Flash") {
            public void actionPerformed(ActionEvent e) {
                // set to default filter ( all preset filter)
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && index.intValue() > DeviceContext.MAX_USER_PRESET)
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "Flash Presets";
                    }

                });
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                setStatus(pcep.getPresetContextTable().getSelectedRowCount() + " of " + pcep.getPresetContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_F);
        filterMenu.add(jmi);


        jmi = new JMenuItem(new AbstractAction("Selected") {
            public void actionPerformed(ActionEvent e) {
                //((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                Object[] sobjs = pcep.getPresetContextTable().getSelObjects();
                if (sobjs.length < 1) {
                    UserMessaging.showInfo("No samples selected");
                    pcep.getPresetContextTable().grabFocus();
                    return;
                }

                final HashMap map_sobjs = new HashMap();
                for (int i = 0,j = sobjs.length; i < j; i++)
                    map_sobjs.put(((ReadablePreset) sobjs[i]).getPresetNumber(), null);

                // set to default filter ( all preset filter)
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && map_sobjs.containsKey(index))
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "Selected Presets";
                    }

                });
                ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                setStatus(pcep.getPresetContextTable().getSelectedRowCount() + " of " + pcep.getPresetContextTable().getRowCount() + " samples selected");
            }
        });
        jmi.setMnemonic(KeyEvent.VK_S);
        filterMenu.add(jmi);


        PresetClassManager.PresetClassProfile[] profs = PresetClassManager.getAllProfilesWithNonNullPrefix();
        for (int i = 0,j = profs.length; i < j; i++) {
            final String name = profs[i].getName();
            final String prefix = profs[i].getPrefix();
            jmi = new JCheckBoxMenuItem(new AbstractAction(profs[i].getName()) {
                public void actionPerformed(ActionEvent e) {
                    ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                        public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                            if (name.substring(0, prefix.length()).equals(prefix))
                                return true;
                            return false;
                        }

                        public String getFilterName() {
                            return name;
                        }

                    });
                    ((PresetContextTableModel) pcep.getPresetContextTable().getModel()).refresh(false);
                    setStatus(pcep.getPresetContextTable().getSelectedRowCount() + " of " + pcep.getPresetContextTable().getRowCount() + " samples selected");
                }
            });
            // assuming format of prefix is X_
            jmi.setMnemonic(prefix.charAt(0));
            //  bg.addDesktopElement(jmi);
            filterMenu.add(jmi);
        }

        final JMenu freeMem = new JMenu("");
        freeMem.setEnabled(false);
        freeMemTimer = new Timer(freeMemInterval, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Preset Memory Monitor") {
                    public void run() {
                        try {
                            final Remotable.PresetMemory pm = presetContext.getDeviceContext().getPresetMemory();
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    freeMem.setText((pm.getPresetFreeMemory().intValue() + " Kb"));
                                }
                            });
                        } catch (RemoteUnreachableException e1) {
                        } catch (ZDeviceNotRunningException e1) {
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

    public void zDispose() {
        pcep.getPresetContextTable().getSelectionModel().removeListSelectionListener(this);
        super.zDispose();
        freeMemTimer.stop();
        device = null;
        presetContext = null;
        pcep = null;
        sampleContextEnclosurePanel = null;
    }

    public boolean isMenuBarAvailable() {
        return true;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }
    /*
    public SampleContextEnclosurePanel getSampleContextEnclosurePanel() {
        return sampleContextEnclosurePanel;
    }

    public void setSampleContextEnclosurePanel(SampleContextEnclosurePanel sampleContextEnclosurePanel) {
        this.sampleContextEnclosurePanel = sampleContextEnclosurePanel;
    }
    */
    public void linkTo(Object o) throws Linkable.InvalidLinkException {
        if (o instanceof SampleContextEnclosurePanel)
            sampleContextEnclosurePanel = (SampleContextEnclosurePanel) o;
        else
            throw new Linkable.InvalidLinkException("PresetContextEnclosurePanel cannot link to " + o.getClass());
    }
}
