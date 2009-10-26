package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.RemoteObjectStates;
import com.pcmsolutions.device.EMU.E4.events.PresetInitializationStatusChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.PresetInitializeEvent;
import com.pcmsolutions.device.EMU.E4.events.PresetNameChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.PresetRefreshEvent;
import com.pcmsolutions.device.EMU.E4.gui.HideablePanel;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListenerHelper;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.PresetNamePostfixer;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetListenerAdapter;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.gui.*;
import com.pcmsolutions.system.Indexable;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 12:00:34
 * To change this template use Options | File Templates.
 */
public class DefaultPresetViewerPanel extends GriddedPanel implements MouseListener, ComponentListener, PresetNamePostfixer, ZDisposable, ChangeListener, TitleProvider, Indexable {
    protected ReadablePreset preset;
    protected HideablePanel voicePanel;
    protected HideablePanel linkPanel;
    protected JPanel globalPanel;

    protected VoiceOverviewTable voiceOverviewTable;
    protected LinkTable linkTable;
    protected PresetParameterTable[] globalTables;

    protected AbstractAction rp;

    public ReadablePreset getPreset() {
        return preset;
    }

    public DefaultPresetViewerPanel(ReadablePreset p) throws ZDeviceNotRunningException {
        this.preset = p;
        rp = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Preset") {
                    public void run() {
                        try {
                            preset.refreshPreset();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }.start();
            }
        };
        rp.putValue("tip", "Refresh Preset");

        this.setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy());

        /*this.setFocusTraversalPolicy(new FocusTraversalPolicy() {
            public Component getComponentAfter(Container focusCycleRoot,
                                               Component aComponent) {
                if (aComponent == voiceOverviewTable)
                    return linkTable;

                if (aComponent == linkTable)
                    return globalTables[0];

                for (int i = 0; i < globalTables.length; i++) {
                    if (aComponent == globalTables[i])
                        if (i != globalTables.length - 1)
                            return globalTables[i + 1];
                        else
                            return voiceOverviewTable;
                }
                return null;
            }

            public Component getComponentBefore(Container focusCycleRoot,
                                                Component aComponent) {
                if (aComponent == voiceOverviewTable)
                    return globalTables[globalTables.length - 1];

                if (aComponent == linkTable)
                    return voiceOverviewTable;

                for (int i = 0; i < globalTables.length; i++) {
                    if (aComponent == globalTables[i])
                        if (i != 0)
                            return globalTables[i - 1];
                        else
                            return linkTable;
                }

                return null;
            }

            public Component getFirstComponent(Container focusCycleRoot) {
                return voiceOverviewTable;
            }

            public Component getLastComponent(Container focusCycleRoot) {
                return globalTables[globalTables.length - 1];
            }

            public Component getDefaultComponent(Container focusCycleRoot) {
                return voiceOverviewTable;
            }
        });
          */

        updateGlobalPanel();
        updateVoicePanel();
        updateLinkPanel();
        addComponents();

        addMouseListener(this);
        preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.addChangeListener(this);
        preset.getDeviceContext().getDevicePreferences().ZPREF_voiceTableUserIds.addChangeListener(this);
        preset.addPresetListener(pla);
    }

    protected PresetListenerAdapter pla = new PresetListenerAdapter() {
        public void presetInitialized(final PresetInitializeEvent ev) {
            if (ev.getPreset().equals(preset.getPresetNumber()))
                tplh.fireTitleProviderDataChanged();
        }

        public void presetInitializationStatusChanged(final PresetInitializationStatusChangedEvent ev) {
            if (ev.getPreset().equals(preset.getPresetNumber())) {
                if (ev.getStatus() == 0.0 || ev.getStatus() == RemoteObjectStates.STATUS_INITIALIZED)
                    tplh.fireTitleProviderDataChanged();
            }
        }

        public void presetRefreshed(final PresetRefreshEvent ev) {
            if (ev.getPreset().equals(preset.getPresetNumber()))
                tplh.fireTitleProviderDataChanged();

        }

        public void presetNameChanged(final PresetNameChangeEvent ev) {
            if (ev.getPreset().equals(preset.getPresetNumber()))
                tplh.fireTitleProviderDataChanged();
        }
    };

/*    protected boolean started = false;
    public void stateStart() {
        if ( !started){
            try {
                updateGlobalPanel();
                updateVoicePanel();
                updateLinkPanel();
                addComponents();
                started = true;
            } catch (ZDeviceNotRunningException e) {
                e.printStackTrace();
            }
        }
    }
  */
    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    protected void updateLinkPanel() throws ZDeviceNotRunningException {
        if (linkPanel != null) {
            linkPanel.removeComponentListener(this);
            linkPanel.zDispose();
        }
        makeLinkPanel();
        linkPanel.addComponentListener(this);
    }

    protected void makeLinkPanel() throws ZDeviceNotRunningException {
        RowHeaderedAndSectionedTablePanel lp;
        linkTable = new LinkTable(preset);
        linkTable.setCustomAction(new AbstractAction("Hide/Show Filter Section") {
            public void actionPerformed(ActionEvent e) {
                //try {
                preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.putValue(!preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.getValue());
                /* updateLinkPanel();
                 addComponents();
                 revalidate();
                 repaint();
                 */
                // } catch (ZDeviceNotRunningException e1) {
                //   e1.printStackTrace();
                // }
            }
        });
        lp = new RowHeaderedAndSectionedTablePanel().init(linkTable, "SHOW LINKS", UIColors.getTableBorder(), rp);
        //lp.getRowHeaderedTable().getRowHeader().addFocusListener(FocusAlerter.getInstance());
        linkPanel = new HideablePanel(lp, false) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

        };
    }

    protected void updateVoicePanel() throws ZDeviceNotRunningException {
        java.util.List expansionMemory = null;
        if (voicePanel != null) {
            if (voiceOverviewTable != null)
                expansionMemory = ((VoiceOverviewTableModel) voiceOverviewTable.getModel()).getExpansionMemory();
            voicePanel.removeComponentListener(this);
            voicePanel.zDispose();
        }
        makeVoicePanel(expansionMemory);
        voicePanel.addComponentListener(this);
    }

    private JList makeVoiceIdSelectionList(List userIds) throws ZDeviceNotRunningException, IllegalParameterIdException {
        ParameterContext vc = preset.getDeviceParameterContext().getVoiceContext();
        Set ids = vc.getIds();
        List dispList = new ArrayList();
        for (Iterator i = ids.iterator(); i.hasNext();) {
            Object next = i.next();
            int idv = ((Integer) next).intValue();
            if ((idv < 37 || idv > 56) && idv != 85 && idv != 86)
                dispList.add(next);
        }

        for (int i = 0,j = dispList.size(); i < j; i++)
            dispList.set(i, vc.getParameterDescriptor((Integer) dispList.get(i)));

        JList vidsl = new JList(dispList.toArray());
        for (int i = 0,j = dispList.size(); i < j; i++)
            if (userIds.contains(dispList.get(i)))
                vidsl.addSelectionInterval(i, i);

        vidsl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        return vidsl;
    }

    protected void makeVoicePanel(java.util.List expansionMemory) throws ZDeviceNotRunningException {
        RowHeaderedAndSectionedTablePanel vp;
        voiceOverviewTable = new VoiceOverviewTable(preset);
        if (expansionMemory != null)
            ((VoiceOverviewTableModel) voiceOverviewTable.getModel()).setExpansionMemory(expansionMemory);

        setVoiceOverviewTableCustomAction(voiceOverviewTable, rp);

        vp = new RowHeaderedAndSectionedTablePanel().init(voiceOverviewTable, "SHOW VOICES", UIColors.getTableBorder(), rp);
        //vp.getRowHeaderedTable().getRowHeader().addFocusListener(FocusAlerter.getInstance());

        voicePanel = new HideablePanel(vp, false) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

        };
    }

    protected void setVoiceOverviewTableCustomAction(VoiceOverviewTable vot, final AbstractAction rp) {
        vot.setCustomAction(new AbstractAction("Change table USER Parameters") {
            public void actionPerformed(ActionEvent e) {
                try {
                    final JList list = makeVoiceIdSelectionList(Arrays.asList(VoiceOverviewTableModel.getUserIdList()));
                    list.setBackground(UIColors.getDefaultBG());
                    list.setForeground(UIColors.getDefaultFG());
                    final JDialog j = new ZDialog(ZoeosFrame.getInstance(), "Change table USER Parameters", true);
                    Box b = new Box(BoxLayout.Y_AXIS);
                    Box bb = new Box(BoxLayout.X_AXIS);

                    JButton ok = new JButton("OK");
                    ok.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            //try {
                            Object[] sv = list.getSelectedValues();
                            ArrayList customIds = new ArrayList();
                            for (int i = 0,j = sv.length; i < j; i++)
                                customIds.add(((GeneralParameterDescriptor) sv[i]).getId());
                            VoiceOverviewTableModel.setUserIdList((Integer[]) customIds.toArray(new Integer[customIds.size()]));
                            /*updateVoicePanel();
                            addComponents();
                            revalidate();
                            repaint();
                            */
                            // } catch (ZDeviceNotRunningException e1) {
                            //   e1.printStackTrace();
                            //  }
                            j.dispose();
                        }
                    });
                    JButton clear = new JButton("Clear");
                    clear.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            list.clearSelection();
                        }
                    });
                    JButton cancel = new JButton("Cancel");
                    cancel.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            j.dispose();
                        }
                    });
                    bb.add(ok);
                    bb.add(cancel);
                    bb.add(clear);

                    JScrollPane sp = new JScrollPane(list);
                    sp.setPreferredSize(new Dimension(200, 200));
                    b.add(sp);
                    b.add(bb);
                    j.getContentPane().add(b);
                    j.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    j.pack();
                    j.show();
                } catch (ZDeviceNotRunningException e1) {
                    e1.printStackTrace();
                } catch (IllegalParameterIdException e1) {
                    e1.printStackTrace();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void updateGlobalPanel() throws ZDeviceNotRunningException {
        if (globalPanel != null) {
            globalPanel.removeComponentListener(this);
            Component[] comps = globalPanel.getComponents();
            for (int i = 0, j = comps.length; i < j; i++)
                if (comps[i] instanceof ZDisposable)
                    ((ZDisposable) comps[i]).zDispose();
        }
        makeGlobalPanel();
        globalPanel.addComponentListener(this);
    }

    protected void makeGlobalPanel() throws ZDeviceNotRunningException {
        ParameterContext ppc = preset.getDeviceParameterContext().getPresetContext();
        List cats = ppc.getCategories();
        globalTables = new PresetParameterTable[cats.size()];
        Collections.sort(cats);
        globalPanel = new JPanel() {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

        };
        globalPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        for (int i = 0,n = cats.size(); i < n; i++) {
            List ids = ppc.getIdsForCategory((String) cats.get(i));
            ArrayList models = new ArrayList();
            for (int j = 0,k = ids.size(); j < k; j++)
                try {
                    models.add(preset.getParameterModel((Integer) ids.get(j)));
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                }
            ReadableParameterModel[] pms = new ReadableParameterModel[models.size()];
            models.toArray(pms);

            globalTables[i] = new PresetParameterTable(preset, cats.get(i).toString(), pms, cats.get(i).toString().toUpperCase());
            globalPanel.add(new HideablePanel(new RowHeaderedAndSectionedTablePanel().init(globalTables[i], "SHOW " + cats.get(i).toString().toUpperCase(), UIColors.getTableBorder(), rp, false), false) {
                public Color getBackground() {
                    return UIColors.getDefaultBG();
                }

            });
        }
    }

    protected void addComponents() {
        this.removeAll();

        GridBagLayout gridbag = new GridBagLayout();
        this.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(voicePanel, c);
        add(voicePanel);

        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(linkPanel, c);
        add(linkPanel);

        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(globalPanel, c);
        add(globalPanel);

        //this.removeAll();
        //this.addAnchoredComponent(voicePanel, 0, 0, GridBagConstraints.NORTHWEST);
        //this.addAnchoredComponent(linkPanel, 1, 0, GridBagConstraints.NORTHWEST);
        //this.addAnchoredComponent(globalPanel, 2, 0, GridBagConstraints.NORTHWEST);
    }

    public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        checkPopup(e);
    }

    public void mouseEntered(java.awt.event.MouseEvent e) {
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
    }

    public void mousePressed(java.awt.event.MouseEvent e) {
        checkPopup(e);
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
        checkPopup(e);
    }

    public boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Object[] sels;
            sels = new Object[]{preset.getMostCapableNonContextEditablePresetDowngrade()};
            ZCommandInvocationHelper.showPopup("Preset >", this, sels, e, null);
            return true;
        }
        return false;
    }

    public void componentResized(ComponentEvent e) {
        //removeAll();
        addComponents();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public String getPostfix() {
        return "[vw]";
    }

    public void zDispose() {
        removeAll();

        voicePanel.removeComponentListener(this);
        voicePanel.zDispose();

        linkPanel.removeComponentListener(this);
        linkPanel.zDispose();

        Component[] comps = globalPanel.getComponents();
        for (int i = 0, j = comps.length; i < j; i++)
            if (comps[i] instanceof ZDisposable)
                ((ZDisposable) comps[i]).zDispose();
        globalPanel.removeComponentListener(this);

        tplh.clearListeners();
        preset.removePresetListener(pla);
        preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.removeChangeListener(this);
        preset.getDeviceContext().getDevicePreferences().ZPREF_voiceTableUserIds.removeChangeListener(this);
        removeMouseListener(this);

        tplh = null;
        pla = null;
        preset = null;
        voicePanel = null;
        linkPanel = null;
        globalPanel = null;
        voiceOverviewTable = null;
        linkTable = null;
        globalTables = null;
        rp = null;
    }

    /* public void preferenceChange(PreferenceChangeEvent evt) {
         if (evt.getNode().equals(Preferences.userNodeForPackage(VoiceOverviewTableModel.class)) && evt.getKey().equals(VoiceOverviewTableModel.PREF_userIds)) {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     try {
                         updateVoicePanel();
                         addComponents();
                         revalidate();
                         repaint();
                     } catch (ZDeviceNotRunningException e) {
                         e.printStackTrace();
                     }
                 }
             });
         } else if (evt.getNode().equals(Preferences.userNodeForPackage(LinkTableModel.class)) && evt.getKey().equals(LinkTableModel.PREF_showFiltersSection)) {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     try {
                         updateLinkPanel();
                         addComponents();
                         revalidate();
                         repaint();
                     } catch (ZDeviceNotRunningException e) {
                         e.printStackTrace();
                     }
                 }
             });
         }
     }
      */
    public String getTitle() {
        try {
            return preset.getPresetDisplayName();
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public String getReducedTitle() {
        return getTitle();
    }

    protected TitleProviderListenerHelper tplh = new TitleProviderListenerHelper(this);

    public void addTitleProviderListener(TitleProviderListener tpl) {
        tplh.addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        tplh.removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return preset.getIcon();
    }

    public Integer getIndex() {
        return preset.getPresetNumber();
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == preset.getDeviceContext().getDevicePreferences().ZPREF_voiceTableUserIds) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        updateVoicePanel();
                        addComponents();
                        revalidate();
                        repaint();
                    } catch (ZDeviceNotRunningException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (e.getSource() == preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        updateLinkPanel();
                        addComponents();
                        revalidate();
                        repaint();
                    } catch (ZDeviceNotRunningException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
