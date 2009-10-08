package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.DevicePreferences;
import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinPopupMenu;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinValueProfile;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.VoiceSwitchIcon;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.preferences.ZIntPref;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.prefs.Preferences;

public class VoiceOverviewTableModel extends AbstractPresetTableModel implements ZDisposable, ChangeListener {
    protected static final Map id2col = new Hashtable();
    protected ReadablePreset preset;
    protected ArrayList zoneParameterDescriptors;

    private Integer[] customIds = getUserIdList();
    private int firstCustomCol = 1;

    private SampleListener gsl;
    private Integer[] gsi;

    {
        zoneParameterDescriptors = new ArrayList();
    }

    public static Integer[] getUserIdList() {
        ArrayList customIds = new ArrayList();
        Preferences prefs = Preferences.userNodeForPackage(VoiceOverviewTableModel.class);

        String ids = DevicePreferences.ZPREF_voiceTableUserIds.getValue();
        StringTokenizer t = new StringTokenizer(ids, Zoeos.preferenceFieldSeperator);

        while (t.hasMoreTokens())
            customIds.add(IntPool.get(Integer.parseInt(t.nextToken())));

        return (Integer[]) customIds.toArray(new Integer[customIds.size()]);
    }

    public static void setUserIdList(Integer[] userIds) {
        String s = "";
        for (int i = 0,j = userIds.length; i < j; i++)
            s += userIds[i].toString() + Zoeos.preferenceFieldSeperator;
        DevicePreferences.ZPREF_voiceTableUserIds.putValue(s);
    }

    protected int numVoices;
    protected ArrayList expansionMemory;
    protected ParameterContext vc;

    private final static VoiceSwitchIcon onIcon = new VoiceSwitchIcon(10, 10, Color.DARK_GRAY, Color.LIGHT_GRAY, VoiceSwitchIcon.EXPANDED);
    private final static VoiceSwitchIcon offIcon = new VoiceSwitchIcon(10, 10, Color.DARK_GRAY, Color.LIGHT_GRAY, VoiceSwitchIcon.CONTRACTED);
    private final static VoiceSwitchIcon disabledIcon = new VoiceSwitchIcon(10, 10, Color.LIGHT_GRAY, Color.white, VoiceSwitchIcon.DISABLED);

    public VoiceOverviewTableModel(ReadablePreset p, DeviceParameterContext dpc) {
        super(p, dpc.getVoiceContext());
        vc = dpc.getVoiceContext();
        this.preset = p;
        init();
        WinValueProfile.ZPREF_keyWinDisplayMode.addChangeListener(this);
        WinValueProfile.ZPREF_velWinDisplayMode.addChangeListener(this);
        WinValueProfile.ZPREF_rtWinDisplayMode.addChangeListener(this);

        try {
            gsl = new SampleListenerAdapter() {
                public void sampleInitializationStatusChanged(SampleInitializationStatusChangedEvent ev) {
                    updateSampleCells(ev.getSample());
                }

                public void sampleNameChanged(SampleNameChangeEvent ev) {
                    updateSampleCells(ev.getSample());
                }

                public void sampleRefreshed(SampleRefreshEvent ev) {
                    updateSampleCells(ev.getSample());
                }

                public void sampleInitialized(SampleInitializeEvent ev) {
                    updateSampleCells(ev.getSample());
                }
            };
            Set s = preset.getDeviceContext().getDefaultSampleContext().getDatabaseIndexes();
            gsi = (Integer[]) s.toArray(new Integer[s.size()]);
            preset.getDeviceContext().getDefaultSampleContext().addSampleListener(gsl, gsi);
        } catch (NoSuchContextException e) {
            e.printStackTrace();
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        }
    }

    public void updateSampleCells(Integer sample) {
        Integer col = ((Integer) id2col.get(IntPool.get(38)));
        if (col != null)
            for (int i = 0, n = this.getRowCount(); i < n; i++) {
                Object v = getValueAt(i, col.intValue());
                try {
                    if (v instanceof ReadableParameterModel && ((ReadableParameterModel) v).getValue().equals(sample))
                        VoiceOverviewTableModel.this.fireTableCellUpdated(i, col.intValue());
                } catch (ParameterUnavailableException e) {
                }
            }
    }

    public void zDispose() {
        if (gsi != null && gsl != null)
            try {
                preset.getDeviceContext().getDefaultSampleContext().removeSampleListener(gsl, gsi);
            } catch (ZDeviceNotRunningException e) {
                e.printStackTrace();
            }

        super.zDispose();
        WinValueProfile.ZPREF_keyWinDisplayMode.removeChangeListener(this);
        WinValueProfile.ZPREF_velWinDisplayMode.removeChangeListener(this);
        WinValueProfile.ZPREF_rtWinDisplayMode.removeChangeListener(this);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof ZIntPref)
            VoiceOverviewTableModel.this.fireTableDataChanged();
    }

    public void setExpansionMemory(java.util.List mem) {
        expansionMemory = new ArrayList(mem);
        refresh(false);
    }

    protected java.util.List getExpansionMemory() {
        return (java.util.List) expansionMemory.clone();
    }

// returns a Boolean for each row, true if it's a voice and false if it's a zone
    public Boolean[] getRowState() {
        Boolean[] st = new Boolean[tableRowObjects.size()];

        for (int i = 0,j = tableRowObjects.size(); i < j; i++)
            if (((ColumnValueProvider) tableRowObjects.get(i)).getValueAt(0) instanceof ReadablePreset.ReadableVoice)
                st[i] = Boolean.TRUE;
            else
                st[i] = Boolean.FALSE;

        return st;
    }

    public boolean isRowAZone(int row) {
        if (row >= 0 && row < tableRowObjects.size())
            if (((ColumnValueProvider) tableRowObjects.get(row)).getValueAt(0) instanceof ReadablePreset.ReadableVoice.ReadableZone)
                return true;
        return false;
    }

    public boolean isRowMultisampleVoice(int row) {
        if (row >= 0 && row < tableRowObjects.size()) {
            Object obj = ((ColumnValueProvider) tableRowObjects.get(row)).getValueAt(0);
            try {
                if (obj instanceof ReadablePreset.ReadableVoice && ((ReadablePreset.ReadableVoice) obj).numZones() > 0)
                    return true;
            } catch (PresetEmptyException e) {
                handlePresetEmptyException();
            } catch (NoSuchVoiceException e) {
                e.printStackTrace();
            } catch (NoSuchPresetException e) {
                handleNoSuchPresetException();
            }
        }
        return false;
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", DEF_COL_WIDTH, JLabel.LEFT, 0, Object.class, VoiceOverviewRowHeaderTableCellRenderer.INSTANCE, null);
        columnData = new ColumnData[parameterObjects.size()];
        firstCustomCol = parameterObjects.size() - customIds.length;

        int id;
        ArrayList arrSectionData = new ArrayList();
        int sectionIndex = 0;
        int colWidthCount = 0;

        id2col.clear();

        for (int i = 0, n = parameterObjects.size(); i < n; i++) {
            id = generateColumnDataInstance(i, sectionIndex);

            id2col.put(IntPool.get(id), IntPool.get(i + 1));

            colWidthCount += columnData[i].width;

            if (id == 44) {
                arrSectionData.add(new SectionData(
                        UIColors.getTableFirstSectionBG(),
                        UIColors.getTableFirstSectionFG(),
                        colWidthCount,
                        "MAIN"));
                sectionIndex++;
                colWidthCount = 0;
            } else if (id == 48) {
                arrSectionData.add(new SectionData(
                        UIColors.getTableSecondSectionBG(),
                        UIColors.getTableSecondSectionFG(),
                        colWidthCount,
                        "KEY WIN", new MouseAdapter() {
                            public void mouseReleased(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    new WinPopupMenu(WinValueProfile.ZPREF_keyWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                }
                            }

                            public void mousePressed(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    new WinPopupMenu(WinValueProfile.ZPREF_keyWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                }
                            }

                            public void mouseClicked(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    new WinPopupMenu(WinValueProfile.ZPREF_keyWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                } else if (e.getClickCount() >= 2) {
                                    try {
                                        WinValueProfile.ZPREF_keyWinDisplayMode.putValue((WinValueProfile.ZPREF_keyWinDisplayMode.getValue() + 1) % 3);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }));
                sectionIndex++;
                colWidthCount = 0;

            } else if (id == 52) {
                arrSectionData.add(new SectionData(
                        UIColors.getTableThirdSectionBG(),
                        UIColors.getTableThirdSectionFG(),
                        colWidthCount,
                        "VELOCITY WIN", new MouseAdapter() {
                            public void mouseReleased(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    new WinPopupMenu(WinValueProfile.ZPREF_velWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                }
                            }

                            public void mousePressed(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    new WinPopupMenu(WinValueProfile.ZPREF_velWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                }
                            }

                            public void mouseClicked(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    new WinPopupMenu(WinValueProfile.ZPREF_velWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                } else if (e.getClickCount() >= 2) {
                                    try {
                                        WinValueProfile.ZPREF_velWinDisplayMode.putValue((WinValueProfile.ZPREF_velWinDisplayMode.getValue() + 1) % 3);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }));
                sectionIndex++;
                colWidthCount = 0;
            } else if (id == 56) {
                arrSectionData.add(new SectionData(
                        UIColors.getTableFourthSectionBG(),
                        UIColors.getTableFourthSectionFG(),
                        colWidthCount,
                        "REALTIME WIN", new MouseAdapter() {
                            public void mouseReleased(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    new WinPopupMenu(WinValueProfile.ZPREF_rtWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                }
                            }

                            public void mousePressed(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    new WinPopupMenu(WinValueProfile.ZPREF_rtWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                }
                            }

                            public void mouseClicked(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    new WinPopupMenu(WinValueProfile.ZPREF_rtWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                } else if (e.getClickCount() >= 2) {
                                    try {
                                        WinValueProfile.ZPREF_rtWinDisplayMode.putValue((WinValueProfile.ZPREF_rtWinDisplayMode.getValue() + 1) % 3);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }));
                sectionIndex++;
                colWidthCount = 0;
            } else if (customIds.length > 0 && id == (customIds[customIds.length - 1]).intValue()) {
                arrSectionData.add(new SectionData(
                        UIColors.getTableFifthSectionBG(),
                        UIColors.getTableFifthSectionFG(),
                        colWidthCount,
                        "USER"));
                sectionIndex++;
                colWidthCount = 0;
            }
        }
        sectionData = new SectionData[arrSectionData.size()];
        arrSectionData.toArray(sectionData);
    }

    protected int generateColumnDataInstance(int i, int sectionIndex) {
        GeneralParameterDescriptor pd;
        String title;
        int id;
        pd = (GeneralParameterDescriptor) parameterObjects.get(i);
        id = pd.getId().intValue();
        if (!(id < 57))
            title = getColNameFromRefString(pd.getReferenceString(), 2);
        else
            title = getColNameFromRefString(pd.getReferenceString(), 1);

        if (id == 38) // sample
            columnData[i] = new ColumnData(title, DEF_COL_WIDTH * 4, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        else if (id == 44) // alphanumeric key position
            columnData[i] = new ColumnData(title, DEF_COL_WIDTH + 5, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        else if (id == 45 || id == 47) // alphanumeric key position and keyWin
            columnData[i] = new ColumnData(title, DEF_COL_WIDTH + 5, JLabel.LEFT, sectionIndex, ReadableParameterModel.class, WinTableCellRenderer.CANONICAL_RENDERERS[(id - 45) % 4]);
        else if (id >= 45 && id <= 56) // keyWin, velWin, rtWin
            columnData[i] = new ColumnData(title, DEF_COL_WIDTH, JLabel.LEFT, sectionIndex, ReadableParameterModel.class, WinTableCellRenderer.CANONICAL_RENDERERS[(id - 45) % 4]);
        else if (id < 57) // one of the defaults
            columnData[i] = new ColumnData(title, DEF_COL_WIDTH, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        else  // user id
            columnData[i] = new ColumnData(title, DEF_COL_WIDTH + 20, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        return id;
    }

    protected void buildDefaultParameterData(ParameterContext vc) {
        parameterObjects.clear();
        zoneParameterDescriptors.clear();

/*E4_GEN_GROUP_NUM,          id = 37 (25h,00h)     min =   1;  max =  32
E4_GEN_SAMPLE,             id = 38 (26h,00h)     min =   0;  max = 999(2999)

E4_GEN_VOLUME,             id = 39 (27h,00h)     min = -96;  max = +10
E4_GEN_PAN,                id = 40 (28h,00h)     min = -64;  max = +63
E4_GEN_CTUNE,              id = 41 (29h,00h)     min = -72;  max = +24  (Link only)
E4_GEN_FTUNE,              id = 42 (2Ah,00h)     min = -64;  max = +64
E4_GEN_XPOSE,              id = 43 (2Bh,00h)     min = -24;  max = +24  (Link only)
E4_GEN_ORIG_KEY,           id = 44 (2Ch,00h)     min =   0;  max = 127  (60 = C3, Sample only)

E4_GEN_KEY_LOW,            id = 45 (2Dh,00h)     min = 0;  max = 127  (C-2 -> G8)
E4_GEN_KEY_LOWFADE,        id = 46 (2Eh,00h)     min = 0;  max = 127
E4_GEN_KEY_HIGH,           id = 47 (2Fh,00h)     min = 0;  max = 127  (C-2 -> G8)
E4_GEN_KEY_HIGHFADE,       id = 48 (30h,00h)     min = 0;  max = 127

E4_GEN_VEL_LOW,            id = 49 (31h,00h)     min = 0;  max = 127
E4_GEN_VEL_LOWFADE,        id = 50 (32h,00h)     min = 0;  max = 127
E4_GEN_VEL_HIGH,           id = 51 (33h,00h)     min = 0;  max = 127
E4_GEN_VEL_HIGHFADE,       id = 52 (34h,00h)     min = 0;  max = 127

E4_GEN_RT_LOW,             id = 53 (35h,00h)     min = 0;  max = 127  (Link only)
E4_GEN_RT_LOWFADE,         id = 54 (36h,00h)     min = 0;  max = 127  (Link only)
E4_GEN_RT_HIGH,            id = 55 (37h,00h)     min = 0;  max = 127  (Link only)
E4_GEN_RT_HIGHFADE,        id = 56 (38h,00h)     min = 0;  max = 127  (Link only)
*/
        GeneralParameterDescriptor pd;
        try {
            pd = vc.getParameterDescriptor(IntPool.get(37));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(null);

            pd = vc.getParameterDescriptor(ID.sample);
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(39));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(40));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(41));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(null);

            pd = vc.getParameterDescriptor(IntPool.get(42));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(43));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(null);

            pd = vc.getParameterDescriptor(IntPool.get(44));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(45));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(46));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(47));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(48));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(49));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(50));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(51));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(52));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(53));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(null);

            pd = vc.getParameterDescriptor(IntPool.get(54));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(null);

            pd = vc.getParameterDescriptor(IntPool.get(55));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(null);

            pd = vc.getParameterDescriptor(IntPool.get(56));
            parameterObjects.add(pd);
            zoneParameterDescriptors.add(null);

            for (int i = 0,j = customIds.length; i < j; i++) {
                pd = vc.getParameterDescriptor(customIds[i]);
                parameterObjects.add(pd);
                zoneParameterDescriptors.add(null);
            }
/*
            pd = vc.getParameterDescriptor(IntPool.get(57));
            parameterObjects.addDesktopElement(pd);
            zoneParameterDescriptors.addDesktopElement(null);

            pd = vc.getParameterDescriptor(IntPool.get(58));
            parameterObjects.addDesktopElement(pd);
            zoneParameterDescriptors.addDesktopElement(null);

            pd = vc.getParameterDescriptor(IntPool.get(61));
            parameterObjects.addDesktopElement(pd);
            zoneParameterDescriptors.addDesktopElement(null);
            pd = vc.getParameterDescriptor(IntPool.get(72));
            parameterObjects.addDesktopElement(pd);
            zoneParameterDescriptors.addDesktopElement(null);
            lastCustomId = 72;
            */

        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        }
    }

    protected static class VoiceSampleReadableParameterModel implements ParameterModelWrapper, ReadableParameterModel, IconAndTipCarrier {
        protected ReadableParameterModel pm;
        protected SampleContext sc;

        public VoiceSampleReadableParameterModel(ReadableParameterModel pm, SampleContext sc) {
            this.pm = pm;
            this.sc = sc;
            pm.setTipShowingOwner(true);
        }

        public Integer getValue() throws ParameterUnavailableException {
            return pm.getValue();
        }

        public void setTipShowingOwner(boolean tipShowsOwner) {
            pm.setTipShowingOwner(tipShowsOwner);
        }

        public boolean isTipShowingOwner() {
            return pm.isTipShowingOwner();
        }

        public String getValueString() throws ParameterUnavailableException {
            return pm.getValueString();
        }

        public String getValueUnitlessString() throws ParameterUnavailableException {
            return pm.getValueUnitlessString();
        }

        public GeneralParameterDescriptor getParameterDescriptor() {
            return pm.getParameterDescriptor();
        }

        public void addChangeListener(ChangeListener cl) {
            pm.addChangeListener(cl);
        }

        public void removeChangeListener(ChangeListener cl) {
            pm.removeChangeListener(cl);
        }

        public void setShowUnits(boolean showUnits) {
            pm.setShowUnits(showUnits);
        }

        public boolean getShowUnits() {
            return pm.getShowUnits();
        }

        public void zDispose() {
            pm.zDispose();
        }

        public ZCommand[] getZCommands() {
            return pm.getZCommands();
        }

        public String toString() {
            Integer sn = IntPool.get(0);
            try {
                sn = pm.getValue();
                if (sn.intValue() >= 0)
                    return new AggRemoteName(sn, sc.getSampleName(pm.getValue())).toString();
            } catch (ParameterUnavailableException e) {
                //e.printStackTrace();
            } catch (NoSuchSampleException e) {
                e.printStackTrace();
            } catch (SampleEmptyException e) {
                return new AggRemoteName(sn, DeviceContext.EMPTY_SAMPLE).toString();
            }
            switch (sn.intValue()) {
                case -1:
                    return "Sampling ADC";
                case -2:
                    return "AES/EBU Input";
                case -3:
                    return "External ADC 1";
                case -4:
                    return "External ADC 2";
                case -5:
                    return "ADAT chan 1-2";
                case -6:
                    return "ADAT chan 3-4";
                case -7:
                    return "ADAT chan 5-6";
                case -8:
                    return "ADAT chan 7-8";
                default:
                    return sn.toString();
            }
        }

        public Icon getIcon() {
            try {
                return sc.getReadableSample(pm.getValue()).getIcon();
            } catch (ParameterUnavailableException e) {
            } catch (NoSuchSampleException e) {
            }
            return null;
        }

        public String getToolTipText() {
            try {
                Integer val = pm.getValue();
                if (val.intValue() >= 0)
                    return sc.getReadableSample(pm.getValue()).getToolTipText();
                else
                    return null;
            } catch (ParameterUnavailableException e) {
                //e.printStackTrace();
            } catch (NoSuchSampleException e) {
                e.printStackTrace();
            }
            return "";
        }

        public Object[] getWrappedObjects() {
            return new Object[]{pm};
        }
    }

    protected void doRefresh() {
        ArrayList oldExpansionMemory = expansionMemory;
        expansionMemory = new ArrayList();
        numVoices = 0;
        boolean iezbd = preset.getDeviceContext().getDevicePreferences().ZPREF_expandingZonesByDefault.getValue();
        int memSize;
        if (oldExpansionMemory != null)
            memSize = oldExpansionMemory.size();
        else
            memSize = 0;
        try {
            int nv = preset.numVoices();
            for (int i = 0, n = nv; i < n; i++)
                if (i < memSize)
                    newVoice(((Boolean) oldExpansionMemory.get(i)).booleanValue());
                else
                    newVoice(iezbd);
        } catch (NoSuchPresetException e) {
            handleNoSuchPresetException();
        } catch (PresetEmptyException e) {
            handlePresetEmptyException();
        } finally {
//ignorePresetInitialize = false;
            if (oldExpansionMemory != null)
                oldExpansionMemory.clear();
        }
    }

    protected void doPreRefresh() {
        // TODO!! re-enable this   ??
        // ignorePresetInitialize = true;
    }

    protected void doPostRefresh() {
    }

    private Voice[] getVoices() {
        ArrayList voices = new ArrayList();
        Object ro;
        for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
            ro = tableRowObjects.get(i);
            if (ro instanceof Voice)
                voices.add(ro);
        }
        return (Voice[]) voices.toArray(new Voice[voices.size()]);
    }

    public void toggleAll() {
        Voice[] voices = getVoices();
        for (int i = 0, n = voices.length; i < n; i++)
            voices[i].toggle();
    }

    public void expandAll() {
        Voice[] voices = getVoices();
        for (int i = 0, n = voices.length; i < n; i++)
            voices[i].setExpanded(true);
    }

    public void contractAll() {
        Voice[] voices = getVoices();
        for (int i = 0, n = voices.length; i < n; i++)
            voices[i].setExpanded(false);
    }

    private Voice findVoice(Integer voiceIndex) {
        Object ro;
        for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
            ro = tableRowObjects.get(i);
            if (ro instanceof Voice) {
                if (((Voice) ro).getVoice().getVoiceNumber().equals(voiceIndex))
                    return (Voice) ro;
            }
        }
        return null;
    }

    public void voiceAdded(final VoiceAddEvent ev) {
        int num = ev.getNumberOfVoices();
        for (int n = 0; n < num; n++)
            newVoice(true);
    }

    public void voiceRemoved(final VoiceRemoveEvent ev) {
        Voice v = findVoice(ev.getVoice());
        if (v != null) {
            v.disposeVoice();
        } else
            refresh(false);
    }

    public void voiceChanged(final VoiceChangeEvent ev) {
        if (ev.getPreset().equals(preset.getPresetNumber())) {
            for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
                if (tableRowObjects.get(i) instanceof Voice && ((Voice) tableRowObjects.get(i)).getVoice().getVoiceNumber().equals(ev.getVoice())) {
                    ((Voice) tableRowObjects.get(i)).updateParameters(ev.getParameters());
                }
            }
        }
    }

    private void remapVoices() {
        Object ro;
        int cvi = 0;
        for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
            ro = tableRowObjects.get(i);
            if (ro instanceof Voice) {
                ((Voice) ro).getVoice().setVoiceNumber(IntPool.get(cvi++));
            }
        }
    }

    public void zoneAdded(final ZoneAddEvent ev) {
        Voice v = findVoice(ev.getVoice());
        if (v != null) {
            for (int i = 0, n = ev.getNumberOfZones(); i < n; i++)
                v.addZone();
        } else  // out of sync somehow
            refresh(false);
    }

    public void zoneRemoved(final ZoneRemoveEvent ev) {
        Voice v = findVoice(ev.getVoice());
        if (v != null) {
            v.removeZone(ev.getZone());
        } else {
            refresh(false);
        }
    }

    public void zoneChanged(final ZoneChangeEvent ev) {
        if (ev.getPreset().equals(preset.getPresetNumber())) {
            for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
                if (tableRowObjects.get(i) instanceof Voice.Zone && ((Voice.Zone) tableRowObjects.get(i)).getVoice().getVoiceNumber().equals(ev.getVoice()) && ((Voice.Zone) tableRowObjects.get(i)).getZone().getZoneNumber().equals(ev.getZone())) {
                    ((Voice.Zone) tableRowObjects.get(i)).updateParameters(ev.getParameters());
                }
            }
        }
    }

    protected void newVoice(boolean expanded) {
        new Voice(getVoiceInterface(numVoices)).init(expanded);
    }

    protected ReadablePreset.ReadableVoice getVoiceInterface(int index) {
        return preset.getReadableVoice(IntPool.get(index));
    }

    protected class Voice implements ZDisposable, ColumnValueProvider, Switchable, IconAndTipCarrier, ZCommandProvider, ReadablePreset.ReadableVoice, ObjectProxy {
        protected ReadablePreset.ReadableVoice voice;
        protected ArrayList voiceColumnObjects = new ArrayList();
        protected ArrayList voiceRowObjects = new ArrayList();
        protected boolean expanded = true;
        protected int numZones = 0;

        public Voice(ReadablePreset.ReadableVoice v) {
            this.voice = v;
        }

        public void init(boolean expanded) {
            this.expanded = expanded;
            expansionMemory.add(new Boolean(expanded));
            createColumnObjects();
            createRowObjects();
            numVoices++;
        }

        public int compareTo(Object o) {
            return voice.compareTo(o);
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expand) {
            if (this.expanded != expand) {
                this.expanded = expand;
                expansionMemory.set(voice.getVoiceNumber().intValue(), new Boolean(expand));
                if (expand)
                    syncZones();
                else {
                    removeAllZones();
                    syncZones();
                }
            }
        }

        public void updateParameters(Integer[] parameters) {
            boolean filterChanged = false;

            boolean keyWinUpdated = false;
            boolean velWinUpdated = false;
            boolean rtWinUpdated = false;

            final int thisIndex = tableRowObjects.indexOf(this);
            int cidv;
            for (int i = 0, n = parameters.length; i < n; i++) {
                if (parameters[i].intValue() == 82) {
                    filterChanged = true;
                    updateId(thisIndex, parameters[i]);
                } else if (ParameterUtilities.isKeyWinId(parameters[i].intValue())) {
                    if (!keyWinUpdated) {
                        keyWinUpdated = true;
                        for (int j = 0; j < ID.voiceKeyWin.length; j++)
                            updateId(thisIndex, ID.voiceKeyWin[j]);
                    } else
                        continue;
                } else if (ParameterUtilities.isVelWinId(parameters[i].intValue())) {
                    if (!velWinUpdated) {
                        velWinUpdated = true;
                        for (int j = 0; j < ID.voiceVelWin.length; j++)
                            updateId(thisIndex, ID.voiceVelWin[j]);
                    } else
                        continue;
                } else if (ParameterUtilities.isRTWinId(parameters[i].intValue())) {
                    if (!rtWinUpdated) {
                        rtWinUpdated = true;
                        for (int j = 0; j < ID.voiceRTWin.length; j++)
                            updateId(thisIndex, ID.voiceRTWin[j]);
                    } else
                        continue;
                } else
                    updateId(thisIndex, parameters[i]);
            }
            if (filterChanged)
                for (int j = firstCustomCol, o = voiceColumnObjects.size(); j < o; j++) {
                    cidv = ((ReadableParameterModel) voiceColumnObjects.get(j)).getParameterDescriptor().getId().intValue();
                    if ((cidv > 82 && cidv < 93)) {
                        VoiceOverviewTableModel.this.fireTableCellUpdated(thisIndex, j);
                    }
                }
        }

        private void updateId(int row, Integer id) {
            Integer col = ((Integer) id2col.get(id));
            if (col != null) {
                VoiceOverviewTableModel.this.fireTableCellUpdated(row, col.intValue());
            }
        }

        public ReadablePreset.ReadableVoice getVoice() {
            return voice;
        }

        protected void setVoice(ReadablePreset.ReadableVoice voice) {
            this.voice = voice;
            createColumnObjects();
        }

        protected boolean hasZones() {
            if (numZones > 0)
                return true;
            else
                return false;
        }

        protected void disposeVoice() {
            final int index = tableRowObjects.indexOf(this);
            final int rr = voiceRowObjects.size();
            expansionMemory.remove(voice.getVoiceNumber().intValue());
            tableRowObjects.removeAll(voiceRowObjects);
            voiceRowObjects.remove(0);
            ZUtilities.zDisposeCollection(voiceRowObjects);
            disposeColumnObjects();
            voiceRowObjects.clear();
            numVoices--;
            remapVoices();
            fireTableRowsDeleted(index, index + rr - 1);
        }

        protected void addZone() {
            if (expanded) {
                int index = tableRowObjects.indexOf(this);
                int numRowObjects = voiceRowObjects.size();
                tableRowObjects.removeAll(voiceRowObjects);
                voiceRowObjects.add(newZone());
                tableRowObjects.addAll(index, voiceRowObjects);
                numZones++;
                fireTableCellUpdated(index, 0);
                fireTableRowsInserted(index + numRowObjects, index + numRowObjects);
            } else
                numZones++;
            // fireTableCellUpdated(tableRowObjects.indexOf(this), 0);
            int index = tableRowObjects.indexOf(this);
            fireTableRowsUpdated(index, index);
        }

        protected void removeZone(Integer zone) {
            numZones--;
            if (expanded) {
                Zone z = (Zone) voiceRowObjects.remove(zone.intValue() + 1);
                final int index = tableRowObjects.indexOf(z);
                tableRowObjects.remove(z);
                remapZones();
                z.zDispose();
                fireTableRowsDeleted(index, index);
            }
            fireTableCellUpdated(tableRowObjects.indexOf(this), 0);
        }

        private void removeAllZones() {
            for (int i = voiceRowObjects.size() - 1, n = 0; i > n; i--) {
                Zone z = (Zone) voiceRowObjects.remove(i);
                final int index = tableRowObjects.indexOf(z);
                tableRowObjects.remove(z);
                z.zDispose();
                fireTableRowsDeleted(index, index);
            }
        }

        private void remapZones() {
            for (int i = 1, n = voiceRowObjects.size(); i < n; i++)
                ((Zone) voiceRowObjects.get(i)).getZone().setZoneNumber(IntPool.get(i - 1));
        }

        protected void createRowObjects() {
            voiceRowObjects.add(this);
            tableRowObjects.addAll(voiceRowObjects);
            int rowIndex = tableRowObjects.size() - 1;
            fireTableRowsInserted(rowIndex, rowIndex);
            syncZones();
        }

        private void syncZones() {
            numZones = 0;
            try {
                for (int i = 0, n = preset.numZones(voice.getVoiceNumber()); i < n; i++)
                    addZone();
            } catch (NoSuchPresetException e) {
                handleNoSuchPresetException();
            } catch (PresetEmptyException e) {
                handlePresetEmptyException();
            } catch (NoSuchVoiceException e) {
                e.printStackTrace();
            }
            //e.printStackTrace();
            //refresh(false);
            //}
        }

        protected Zone newZone() {
            return new Zone(voice.getReadableZone(IntPool.get(numZones)));
        }

        protected void disposeColumnObjects() {
            if (voiceColumnObjects.size() > 0) {
                voiceColumnObjects.remove(0);
                ZUtilities.zDisposeCollection(voiceColumnObjects);
                voiceColumnObjects.clear();
            }
        }

        protected void createColumnObjects() {
            disposeColumnObjects();
            ReadableParameterModel pm;
            voiceColumnObjects.add(this);
            for (int i = 0, n = parameterObjects.size(); i < n; i++) {
                try {
                    pm = getAppropiateParameterModelInterface(i);
                    //if (pm.getParameterDescriptor().getId().intValue() > 56)
                    //  pm.setShowUnits(true);
                    // else
                    pm.setShowUnits(false);
                    voiceColumnObjects.add(pm);
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                    voiceColumnObjects.add("");
                }
            }
        }

        protected ReadableParameterModel getAppropiateParameterModelInterface(int i) throws IllegalParameterIdException {
            ReadableParameterModel pm = voice.getParameterModel(((GeneralParameterDescriptor) parameterObjects.get(i)).getId());
            if (((GeneralParameterDescriptor) parameterObjects.get(i)).getId().equals(ID.sample))
                try {
                    return new VoiceSampleReadableParameterModel(pm, preset.getDeviceContext().getDefaultSampleContext());
                } catch (ZDeviceNotRunningException e) {
                    e.printStackTrace();
                }
            pm.setTipShowingOwner(true);
            return pm;
        }

        public Object getValueAt(int col) {
            try {
                if (col == 2 && hasZones()) // sample number
                    return DeviceContext.MULTISAMPLE + "[" + this.numZones() + "]";
                else if (col == 8 && hasZones()) // orig key
                    return "";
                return (voiceColumnObjects.size() > col ? voiceColumnObjects.get(col) : "");
            } catch (PresetEmptyException e) {
            } catch (NoSuchVoiceException e) {
            } catch (NoSuchPresetException e) {
            }
            return "";
        }

        public boolean getState() {
            return expanded;
        }

        public void toggle() {
            setExpanded(!expanded);
        }

        public String toString() {
            return "V" + IntPool.get(voice.getVoiceNumber().intValue() + 1);
        }

        public Icon getIcon() {
            try {
                if (!hasZones())
                    return disabledIcon;
                else if (!expanded)
                    return offIcon;
                else
                    return onIcon;
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        public String getToolTipText() {
            return "Voice " + IntPool.get(voice.getVoiceNumber().intValue() + 1) + "  (" + numZones + " zones)";
        }

        // called by refresh
        public void zDispose() {
            disposeColumnObjects();
            voiceRowObjects.clear();
            /*if (voiceRowObjects.size() > 0) {
                voiceRowObjects.remove(0);
                ZUtilities.zDisposeCollection(voiceRowObjects);
                voiceRowObjects.clear();
            } */
            /*disposeColumnObjects();
            voiceRowObjects.remove(0);
            ZUtilities.zDisposeCollection(voiceRowObjects);
            voiceRowObjects.clear();
            */
        }

        public ZCommand[] getZCommands() {
            return cmdProviderHelper.getCommandObjects(this);
        }

        public ReadablePreset getPreset() {
            return voice.getPreset();
        }

        public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer zone) throws NoSuchZoneException, NoSuchPresetException, PresetEmptyException, NoSuchVoiceException {
            return voice.getIsolatedZone(zone);
        }

        public Integer[] getVoiceParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException {
            return voice.getVoiceParams(ids);
        }

        public Integer getVoiceNumber() {
            return voice.getVoiceNumber();
        }

        public void setVoiceNumber(Integer voice) {
            this.voice.setVoiceNumber(voice);
        }

        public void performOpenAction() {
            this.voice.performOpenAction();
        }

        public Integer getPresetNumber() {
            return voice.getPresetNumber();
        }

        public IsolatedPreset.IsolatedVoice getIsolated() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException {
            return voice.getIsolated();
        }

        public ReadablePreset.ReadableVoice.ReadableZone getReadableZone(Integer zone) {
            return voice.getReadableZone(zone);
        }

        public int numZones() throws PresetEmptyException, NoSuchVoiceException, NoSuchPresetException {
            return voice.numZones();
        }

        public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException {
            return voice.getParameterModel(id);
        }

        public Integer[] getVoiceIndexesInGroup() throws PresetEmptyException, NoSuchContextException, NoSuchVoiceException, NoSuchPresetException {
            return voice.getVoiceIndexesInGroup();
        }

        public Object getRealObject() {
            return voice;
        }

        protected class Zone implements ZDisposable, ColumnValueProvider, IconAndTipCarrier, ReadablePreset.ReadableVoice.ReadableZone {
            protected ReadablePreset.ReadableVoice.ReadableZone zone;
            protected ArrayList zoneColumnObjects = new ArrayList();

            public Zone(ReadablePreset.ReadableVoice.ReadableZone z) {
                this.zone = z;
                createColumnObjects();
            }

            public void updateParameters(Integer[] parameters) {
                final int thisIndex = tableRowObjects.indexOf(this);
                boolean keyWinUpdated = false;
                boolean velWinUpdated = false;
                for (int i = 0, n = parameters.length; i < n; i++) {
                    if (ParameterUtilities.isKeyWinId(parameters[i].intValue())) {
                        if (!keyWinUpdated) {
                            keyWinUpdated = true;
                            for (int j = 0; j < ID.voiceKeyWin.length; j++)
                                updateId(thisIndex, ID.voiceKeyWin[j]);
                        } else
                            continue;
                    } else if (ParameterUtilities.isVelWinId(parameters[i].intValue())) {
                        if (!velWinUpdated) {
                            velWinUpdated = true;
                            for (int j = 0; j < ID.voiceVelWin.length; j++)
                                updateId(thisIndex, ID.voiceVelWin[j]);
                        } else
                            continue;
                    }
                    //    for (int j = 2, o = zoneColumnObjects.size(); j < o; j++) {
                    //       if (zoneColumnObjects.get(j) instanceof ReadableParameterModel && parameters[i].equals(((ReadableParameterModel) zoneColumnObjects.get(j)).getParameterDescriptor().getId())) {
                    //         VoiceOverviewTableModel.this.fireTableCellUpdated(thisIndex, j);
                    //       break;
                    //  }
                    //}
                    else
                        updateId(thisIndex, parameters[i]);
                }
            }

            public int compareTo(Object o) {
                return zone.compareTo(o);
            }

            public ReadablePreset.ReadableVoice.ReadableZone getZone() {
                return zone;
            }

            public Integer[] getZoneParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchVoiceException, NoSuchZoneException {
                return zone.getZoneParams(ids);
            }

            public Integer getVoiceNumber() {
                return zone.getVoiceNumber();
            }

            public ReadablePreset.ReadableVoice getVoice() {
                return voice;
            }

            public Integer getPresetNumber() {
                return zone.getPresetNumber();
            }

            public Integer getZoneNumber() {
                return zone.getZoneNumber();
            }

            public ReadablePreset getPreset() {
                return zone.getPreset();
            }

            public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolated() throws PresetEmptyException, NoSuchZoneException, NoSuchVoiceException, NoSuchPresetException {
                return zone.getIsolated();
            }

            public void setZoneNumber(Integer zone) {
                this.zone.setZoneNumber(zone);
            }

            public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException {
                return zone.getParameterModel(id);
            }

            protected void createColumnObjects() {
                ReadableParameterModel pm;
                GeneralParameterDescriptor pd;
                zoneColumnObjects.add(this);
                for (int i = 0, n = zoneParameterDescriptors.size(); i < n; i++) {
                    pd = (GeneralParameterDescriptor) zoneParameterDescriptors.get(i);
                    try {
                        if (pd == null)
                            zoneColumnObjects.add("");
                        else {
                            pm = getAppropiateParameterModelInterface(pd);
                            pm.setShowUnits(false);
                            zoneColumnObjects.add(pm);
                        }
                    } catch (IllegalParameterIdException e) {
                        e.printStackTrace();
                        zoneColumnObjects.add("");
                    }
                }
            }

            public String toString() {
                return "";
            }

            protected ReadableParameterModel getAppropiateParameterModelInterface(GeneralParameterDescriptor pd) throws IllegalParameterIdException {
                ReadableParameterModel pm = zone.getParameterModel(pd.getId());
                if (pd.getId().equals(ID.sample))
                    try {
                        return new VoiceSampleReadableParameterModel(pm, preset.getDeviceContext().getDefaultSampleContext());
                    } catch (ZDeviceNotRunningException e) {
                        e.printStackTrace();
                    }
                pm.setTipShowingOwner(true);
                return pm;
            }

            public Object getValueAt(int col) {
                if (zoneColumnObjects.size() > col)
                    return zoneColumnObjects.get(col);
                return "";
            }

            // called by refresh
            public void zDispose() {
                // to avoid infinite recursion - remember "this" is at index 0
                if (zoneColumnObjects.size() > 0) {
                    zoneColumnObjects.remove(0);
                    ZUtilities.zDisposeCollection(zoneColumnObjects);
                    zoneColumnObjects.clear();
                }
            }

            public Icon getIcon() {
                return null;
            }

            public String getToolTipText() {
                return "Zone " + IntPool.get(zone.getZoneNumber().intValue() + 1);
            }
        }
    }

}
