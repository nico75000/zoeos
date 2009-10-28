package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializationStatusChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetNameChangeEvent;
import com.pcmsolutions.device.EMU.E4.gui.*;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterSelectorDialog;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.PresetNamePostfixer;
import com.pcmsolutions.device.EMU.E4.gui.preset.PresetViewModes;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.gui.table.PopupTable;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetListenerAdapter;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.gui.GriddedPanel;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZCommandFactory;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.desktop.SessionableComponent;
import com.pcmsolutions.system.Indexable;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.util.ClassUtility;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 12:00:34
 * To change this template use Options | File Templates.
 */
public class PresetPanel extends GriddedPanel implements MouseListener, /*ComponentListener,*/ PresetNamePostfixer, ZDisposable, ChangeListener, TitleProvider, Indexable, EnclosureNorthenComponentProvider, SessionableComponent {
    protected ReadablePreset preset;
    protected HideablePanel voicePanel;
    protected HideablePanel linkPanel;
    protected JPanel globalPanel;
    protected VoiceOverviewTable voiceOverviewTable;
    protected LinkTable linkTable;
    protected PresetParameterTable[] globalTables;

    private Impl_TableExclusiveSelectionContext tableExclusiveSelectionContext = new Impl_TableExclusiveSelectionContext();

    protected AbstractAction refreshPreset;

    public ReadablePreset getPreset() {
        return preset;
    }

    // contextual menu bar stuff
    protected static final String selectionContext = "selection";
    private EnclosureMenuBar encMenuBar;
    private ZCommandFactory.ZCommandPresentationContext activeSelectionPresentationContext;
    private ZCommandFactory.ZCommandPresentationContext defaultSelectionPresentationContext;
    ZCommandFactory.ZCommandPresentationContext voiceCommandPresentationContext;
    ZCommandFactory.ZCommandPresentationContext zoneCommandPresentationContext;

    final boolean incVoices;
    final boolean incLinks;
    final boolean incGlobal;

    private int voiceTableMode;

    public int getVoiceTableMode() {
        return voiceTableMode;
    }

    public Impl_TableExclusiveSelectionContext getTableExclusiveSelectionContext() {
        return tableExclusiveSelectionContext;
    }

    public PresetPanel(ReadablePreset p, boolean incVoices, boolean incLinks, boolean incGlobal, int voiceTableMode) throws DeviceException {
        this.preset = p;
        this.voiceTableMode = voiceTableMode;
        voiceCommandPresentationContext = ZCommandFactory.getToolbarPresentationContext(ReadablePreset.ReadableVoice.cmdProviderHelper.getSupportedMarkers());
        zoneCommandPresentationContext = ZCommandFactory.getToolbarPresentationContext(ReadablePreset.ReadableVoice.ReadableZone.cmdProviderHelper.getSupportedMarkers());

          getTableExclusiveSelectionContext().setSelectionAction(new Impl_TableExclusiveSelectionContext.SelectionAction() {
            public void newSelection(PopupTable t) {
                if (t != null) {
                    ZCommandFactory.ZCommandPresentationContext nextContext = null;
                    Object[] selObjs = t.getSelObjects();
                    selObjs = ZUtilities.getRealObjects(ZCommandFactory.extractZCommandProviders(selObjs));

                     if (ClassUtility.areAllInstanceOf(selObjs, ReadablePreset.ReadableVoice.class)) {
                        nextContext = voiceCommandPresentationContext;
                    } else if (ClassUtility.areAllInstanceOf(selObjs, ReadablePreset.ReadableVoice.ReadableZone.class)) {
                        nextContext = zoneCommandPresentationContext;
                    }
                    if (nextContext == null) {
                            getDefaultSelectionPresentationContext().disableContext();
                        adjustSelectionPresentationContext(getDefaultSelectionPresentationContext());
                    } else {
                        nextContext.setTargets(selObjs);
                        adjustSelectionPresentationContext(nextContext);
                    }

                } else {
                        getDefaultSelectionPresentationContext().disableContext();
                    adjustSelectionPresentationContext(getDefaultSelectionPresentationContext());
                }
            }

            public void clearedSelection(PopupTable t) {
            }
        });
        refreshPreset = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    preset.refresh();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };
        refreshPreset.putValue("tip", "Refresh preset");

        this.incVoices = incVoices;
        this.incLinks = incLinks;
        this.incGlobal = incGlobal;

        this.setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy());

        updateGlobalPanel();
        updateVoicePanel();
        updateLinkPanel();
        updateComponents();

        addMouseListener(this);
        if (incLinks)
            preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.addChangeListener(this);
        if (incVoices && (voiceTableMode & PresetViewModes.VOICE_MODE_USER) != 0)
            preset.getDeviceContext().getDevicePreferences().ZPREF_voiceTableUserIds.addChangeListener(this);
        preset.addListener(pla);
    }

    public ZCommandFactory.ZCommandPresentationContext getActiveSelectionPresentationContext() {
        return activeSelectionPresentationContext;
    }

    public void setActiveSelectionPresentationContext(ZCommandFactory.ZCommandPresentationContext activeSelectionPresentationContext) {
        this.activeSelectionPresentationContext = activeSelectionPresentationContext;
    }

    public ZCommandFactory.ZCommandPresentationContext getDefaultSelectionPresentationContext() {
        return defaultSelectionPresentationContext;
    }

    public void setDefaultSelectionPresentationContext(ZCommandFactory.ZCommandPresentationContext defaultSelectionPresentationContext) {
        this.defaultSelectionPresentationContext = defaultSelectionPresentationContext;
    }

    protected PresetListenerAdapter pla = new PresetListenerAdapter() {
        public void presetInitializationStatusChanged(final PresetInitializationStatusChangedEvent ev) {
            if (ev.getIndex().equals(preset.getIndex())) {
                if (ev.getStatus() == 0.0)
                    tplh.fireTitleProviderDataChanged();
            }
        }

        public void presetRefreshed(final PresetInitializeEvent ev) {
            if (ev.getIndex().equals(preset.getIndex()))
                tplh.fireTitleProviderDataChanged();

        }

        public void presetNameChanged(final PresetNameChangeEvent ev) {
            if (ev.getIndex().equals(preset.getIndex()))
                tplh.fireTitleProviderDataChanged();
        }
    };

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    protected final void updateLinkPanel() throws DeviceException {
        if (incLinks) {
            if (linkPanel != null) {
                // linkPanel.removeComponentListener(this);
                linkPanel.zDispose();
            }
            makeLinkPanel();
            // linkPanel.addComponentListener(this);
        }
    }

    protected void makeLinkPanel() throws DeviceException {
        RowHeaderedAndSectionedTablePanel lp;
        linkTable = new LinkTable(preset, PresetViewModes.LINK_MODE_MAIN_WIN);
        linkTable.setCustomAction(new AbstractAction("Hide/Show Filter Section") {
            public void actionPerformed(ActionEvent e) {
                //try {
                preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.putValue(!preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.getValue());
                /* updateLinkPanel();
                 updateComponents();
                 revalidate();
                 repaint();
                 */
                // } catch (ZDeviceNotRunningException e1) {
                //   e1.printStackTrace();
                // }
            }
        });
        tableExclusiveSelectionContext.addTableToContext(linkTable);
        lp = new RowHeaderedAndSectionedTablePanel().init(linkTable, "SHOW LINKS", UIColors.getTableBorder(), refreshPreset);
        //lp.getRowHeaderedTable().getRowHeader().addFocusListener(FocusAlerter.getInstance());
        linkPanel = new HideablePanel(lp, false) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
    }

    protected final void updateVoicePanel() throws DeviceException {
        if (incVoices) {
            java.util.List expansionMemory = null;
            if (voicePanel != null) {
                if (voiceOverviewTable != null)
                    expansionMemory = ((VoiceOverviewTableModel) voiceOverviewTable.getModel()).getExpansionMemory();
                //  voicePanel.removeComponentListener(this);
                voicePanel.zDispose();
            }
            makeVoicePanel(expansionMemory);
            //   voicePanel.addComponentListener(this);
        }
    }


    /*
    private void loadVoiceIdList(List userIds) throws ZDeviceNotRunningException, IllegalParameterIdException {
        ParameterContext vc = preset.getDeviceParameterContext().getVoiceContext();
        Set ids = vc.getIds();
        final List dispList = new ArrayList();
        for (Iterator i = ids.iterator(); i.hasNext();) {
            Object next = i.next();
            int idv = ((Integer) next).intValue();
            //if ((idv < 37 || idv > 56) && idv != 85 && idv != 86)
            if (idv != 37 && idv != 38)
                dispList.add(next);
        }

        for (int i = 0, j = dispList.size(); i < j; i++)
            dispList.set(i, vc.getParameterDescriptor((Integer) dispList.get(i)));
        vidList.setModel(new ListModel() {
            public int getSize() {
                return dispList.size();
            }

            public Object getElementAt(int index) {
                return dispList.get(index);
            }

            public void addListDataListener(ListDataListener l) {
            }

            public void removeListDataListener(ListDataListener l) {
            }
        });
        vidList.clearSelection();
        for (int i = 0, j = dispList.size(); i < j; i++)
            if (userIds.contains(dispList.get(i)))
                vidList.addSelectionInterval(i, i);
    }

    public static JList vidList;
    public static JDialog vidDialog;

    static {
        vidList = new JList();
        vidList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        vidList.setBackground(UIColors.getDefaultBG());
        vidList.setForeground(UIColors.getDefaultFG());
        vidDialog = new ZDialog(ZoeosFrame.getInstance(), "User Parameters", true);
        Box b = new Box(BoxLayout.Y_AXIS);
        Box bb = new Box(BoxLayout.X_AXIS);

        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object[] sv = vidList.getSelectedValues();
                ArrayList customIds = new ArrayList();
                for (int i = 0, j = sv.length; i < j; i++)
                    customIds.add(((GeneralParameterDescriptor) sv[i]).getId());
                VoiceOverviewTableModel.setUserIdList((Integer[]) customIds.toArray(new Integer[customIds.size()]));
                vidDialog.setVisible(false);
            }
        });
        JButton clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                vidList.clearSelection();
            }
        });
        JButton cbCancelled = new JButton("Cancel");
        cbCancelled.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                vidDialog.dispose();
            }
        });
        bb.add(ok);
        bb.add(cbCancelled);
        bb.add(clear);

        JScrollPane sp = new JScrollPane(vidList);
        sp.setPreferredSize(new Dimension(200, 200));
        b.add(sp);
        b.add(bb);
        vidDialog.setContentPane(b);
        vidDialog.pack();
        vidDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }
    */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

    protected void makeVoicePanel(java.util.List expansionMemory) throws DeviceException {
        RowHeaderedAndSectionedTablePanel vp;

        voiceOverviewTable = new VoiceOverviewTable(preset, voiceTableMode);
        if (expansionMemory != null)
            ((VoiceOverviewTableModel) voiceOverviewTable.getModel()).setExpansionMemory(expansionMemory);

        setVoiceOverviewTableCustomAction(voiceOverviewTable);
        tableExclusiveSelectionContext.addTableToContext(voiceOverviewTable);

        vp = new RowHeaderedAndSectionedTablePanel().init(voiceOverviewTable, "SHOW VOICES", UIColors.getTableBorder(), refreshPreset);
        //vp.getRowHeaderedTable().getRowHeader().addFocusListener(FocusAlerter.getInstance());

        voicePanel = new HideablePanel(vp, false) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

        };
    }

    protected void setVoiceOverviewTableCustomAction(VoiceOverviewTable vot) {
        if ((voiceTableMode & PresetViewModes.VOICE_MODE_USER) != 0)
            vot.setCustomAction(new AbstractAction("Configure preset user table") {
                public void actionPerformed(ActionEvent e) {
                    ParameterSelectorDialog dlg = null;
                    try {
                        List pdl = preset.getDeviceParameterContext().getVoiceContext().getAllParameterDescriptors();
                        GeneralParameterDescriptor[] pds = (GeneralParameterDescriptor[]) pdl.toArray(new GeneralParameterDescriptor[pdl.size()]);
                        dlg = new ParameterSelectorDialog(ZoeosFrame.getInstance(), "Configure preset user table", pds, ParameterUtilities.userTableGroupedParameters, ParameterUtilities.userTableIgnoreIds, VoiceOverviewTableModel.getUserIdList());
                        dlg.setVisible(true);
                        Integer[] ids = dlg.getSelectedIds();
                        if (dlg.isApplied() && ids != null)
                            VoiceOverviewTableModel.setUserIdList(ids);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
    }

    protected final void updateGlobalPanel() throws DeviceException {
        if (incGlobal) {
            if (globalPanel != null) {
                //  globalPanel.removeComponentListener(this);
                Component[] comps = globalPanel.getComponents();
                for (int i = 0, j = comps.length; i < j; i++)
                    if (comps[i] instanceof ZDisposable)
                        ((ZDisposable) comps[i]).zDispose();
            }
            makeGlobalPanel();
            //  globalPanel.addComponentListener(this);
        }
    }

    protected void makeGlobalPanel() throws DeviceException {
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
        for (int i = 0, n = cats.size(); i < n; i++) {
            List ids = ppc.getIdsForCategory((String) cats.get(i));
            ArrayList models = new ArrayList();
            for (int j = 0, k = ids.size(); j < k; j++)
                try {
                    models.add(preset.getParameterModel((Integer) ids.get(j)));
                } catch (ParameterException e) {
                    e.printStackTrace();
                }
            ReadableParameterModel[] pms = new ReadableParameterModel[models.size()];
            models.toArray(pms);

            globalTables[i] = new PresetParameterTable(preset, cats.get(i).toString(), pms, cats.get(i).toString().toUpperCase());
            tableExclusiveSelectionContext.addTableToContext(globalTables[i]);
            globalPanel.add(new HideablePanel(new RowHeaderedAndSectionedTablePanel().init(globalTables[i], "SHOW " + cats.get(i).toString().toUpperCase(), UIColors.getTableBorder(), refreshPreset, false), false) {
                public Color getBackground() {
                    return UIColors.getDefaultBG();
                }
            });
        }
    }

    protected final void updateComponents() {
        this.removeAll();
        GridBagLayout gridbag = new GridBagLayout();
        this.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();

        if (incVoices) {
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.weighty = 1.0;
            gridbag.setConstraints(voicePanel, c);
            add(voicePanel);
        }
        if (incLinks) {
            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.weighty = 1.0;
            gridbag.setConstraints(linkPanel, c);
            add(linkPanel);
        }
        if (incGlobal) {
            c.gridx = 0;
            c.gridy = 2;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.weighty = 1.0;
            gridbag.setConstraints(globalPanel, c);
            add(globalPanel);
        }
        revalidate();
        repaint();
    }

    public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() == 2) {
            try {
                preset.audition().post(new Callback() {
                    public void result(Exception e, boolean wasCancelled) {
                        if (e != null && !wasCancelled)
                            UserMessaging.flashWarning(null, e.getMessage());
                    }
                });
            } catch (ResourceUnavailableException e1) {
                UserMessaging.flashWarning(null, e1.getMessage());
            }
        } else
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
            sels = new Object[]{preset.getMostCapableNonContextEditablePreset()};
            ZCommandFactory.showPopup("Preset >", this, sels, e);
            return true;
        }
        return false;
    }

    /*
    public void componentResized(ComponentEvent e) {
         updateComponents();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }
     */
    public String getPostfix() {
        return "[vw]";
    }

    public void zDispose() {
        removeAll();

        // voicePanel.removeComponentListener(this);
        if (voicePanel != null) {
            voicePanel.zDispose();
            voicePanel = null;
            voiceOverviewTable = null;
        }

        // linkPanel.removeComponentListener(this);
        if (linkPanel != null) {
            linkPanel.zDispose();
            linkPanel = null;
            linkTable = null;
        }

        if (globalPanel != null) {
            Component[] comps = globalPanel.getComponents();
            for (int i = 0, j = comps.length; i < j; i++)
                if (comps[i] instanceof ZDisposable)
                    ((ZDisposable) comps[i]).zDispose();
            globalPanel = null;
            globalTables = null;
        }
        // globalPanel.removeComponentListener(this);

        tplh.clearListeners();
        preset.removeListener(pla);
        preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.removeChangeListener(this);
        preset.getDeviceContext().getDevicePreferences().ZPREF_voiceTableUserIds.removeChangeListener(this);
        removeMouseListener(this);
        tableExclusiveSelectionContext.zDispose();
        tableExclusiveSelectionContext = null;
        tplh = null;
        pla = null;
        preset = null;
        refreshPreset = null;
        encMenuBar.zDispose();
        encMenuBar = null;
    }

    public String getTitle() {
        try {
            return preset.getDisplayName();
        } catch (PresetException e) {
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
        return preset.getIndex();
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == preset.getDeviceContext().getDevicePreferences().ZPREF_voiceTableUserIds) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        updateVoicePanel();
                        updateComponents();
                        // revalidate();
                        // repaint();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (e.getSource() == preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        updateLinkPanel();
                        updateComponents();
                        //  revalidate();
                        //  repaint();
                    } catch (DeviceException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
    }

    public boolean isEnclosureNorthenComponentAvailable() {
        return true;
    }

    protected EnclosureMenuBar getEncMenuBar() {
        return encMenuBar;
    }

    public final Component getEnclosureNorthenComponent() {
        if (encMenuBar == null) {
            encMenuBar = new EnclosureMenuBar();
            addMenuContexts();
        }
        return encMenuBar.getjMenuBar();
    }

    protected void addMenuContexts() {
        getEncMenuBar().addStaticMenuContext(ZCommandFactory.getMenu(new Object[]{preset}, "Preset"), "PRESET_MENU");
        ZCommandFactory.ZCommandPresentationContext c = ZCommandFactory.getTargetedButtonPresentationContext(new Object[]{preset.getMostCapableNonContextEditablePreset()});
        c.setTargets(new Object[]{preset});
        getEncMenuBar().addStaticMenuContext(c.getComponents(), "PRESET_BUTTONS");
        setActiveSelectionPresentationContext(ZCommandFactory.nullContext);
        setDefaultSelectionPresentationContext(ZCommandFactory.nullContext);
        getEncMenuBar().addDynamicMenuContext(getActiveSelectionPresentationContext().getComponents(), selectionContext);
    }

    protected void adjustSelectionPresentationContext(ZCommandFactory.ZCommandPresentationContext zpc) {
        if (zpc == null || getActiveSelectionPresentationContext() == null)
            throw new IllegalArgumentException();

        if (zpc != getActiveSelectionPresentationContext()) {
            getEncMenuBar().changeDynamicMenuContext(zpc.getComponents(), selectionContext);
            setActiveSelectionPresentationContext(zpc);
        }
    }

    private static final String voicesHiddenTagId = PresetPanel.class.toString() + "VOICESHIDDEN";
    private static final String linksHiddenTagId = PresetPanel.class.toString() + "LINKSHIDDEN";
    private static final String voiceExpansionMemoryTagId = PresetPanel.class.toString() + "EXPANSIONMEMORY";

    public String retrieveComponentSession() {
        StringBuffer sb = new StringBuffer();
        if (voicePanel != null)
            sb.append(ZUtilities.makeTaggedField(voicesHiddenTagId, String.valueOf(voicePanel.isHidden())));
        if (linkPanel != null)
            sb.append(ZUtilities.makeTaggedField(linksHiddenTagId, String.valueOf(linkPanel.isHidden())));
        if (voiceOverviewTable != null) {
            List<Boolean> expansionMemory = ((VoiceOverviewTableModel) voiceOverviewTable.getModel()).getExpansionMemory();
            StringBuffer mem = new StringBuffer();
            for (Iterator<Boolean> i = expansionMemory.iterator(); i.hasNext();)
                mem.append(i.next() + ",");
            sb.append(ZUtilities.makeTaggedField(voiceExpansionMemoryTagId, mem.toString()));
        }
        return sb.toString();
    }

    public void restoreComponentSession(String sessStr) {
        if (sessStr != null && !sessStr.equals("")) {
            try {
                String field;
                if (voicePanel != null) {
                    field = ZUtilities.extractTaggedField(sessStr, voicesHiddenTagId);
                    if (field != null && voicePanel != null)
                        voicePanel.setHidden(Boolean.parseBoolean(field));
                }
                if (linkPanel != null) {
                    field = ZUtilities.extractTaggedField(sessStr, linksHiddenTagId);
                    if (field != null)
                        linkPanel.setHidden(Boolean.parseBoolean(field));
                }
                if (voiceOverviewTable != null) {
                    field = ZUtilities.extractTaggedField(sessStr, voiceExpansionMemoryTagId);
                    if (field != null) {
                        List<Boolean> mem = new ArrayList<Boolean>();
                        StringTokenizer tok = new StringTokenizer(field, ",");
                        while (tok.hasMoreTokens())
                            mem.add(Boolean.valueOf(tok.nextToken()));
                        if (mem.size() > 0)
                            ((VoiceOverviewTableModel) voiceOverviewTable.getModel()).setExpansionMemory(mem);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
