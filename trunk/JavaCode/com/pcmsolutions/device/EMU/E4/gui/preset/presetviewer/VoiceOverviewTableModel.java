package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.DevicePreferences;
import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleInitializationStatusChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleInitializeEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleNameChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.sample.SampleRefreshEvent;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.PresetViewModes;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinPopupMenu;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinValueProfile;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.VoiceSwitchIcon;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleListener;
import com.pcmsolutions.device.EMU.E4.sample.SampleListenerAdapter;
import com.pcmsolutions.device.EMU.E4.sample.VisibleSampleIndexProvider;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.gui.EmptyIcon;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.preferences.ZIntPref;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class VoiceOverviewTableModel extends AbstractPresetTableModel implements ZDisposable, ChangeListener, VisibleSampleIndexProvider {
    protected final Map<Integer, Integer> id2col = new Hashtable<Integer, Integer>();
    protected ReadablePreset preset;
    protected ArrayList zoneParameterDescriptors;

    private Integer[] customIds = getUserIdList();
    private int firstCustomCol = 1;

    private SampleListener gsl;
    private Integer[] gsi;

    protected int origKeyCol = -1;
    protected int sampleCol = 2; // always 2 under current scheme

    {
        zoneParameterDescriptors = new ArrayList();
    }

    public static Integer[] getUserIdList() {
        ArrayList customIds = new ArrayList();
        String ids = DevicePreferences.ZPREF_voiceTableUserIds.getValue();
        StringTokenizer t = new StringTokenizer(ids, Zoeos.preferenceFieldSeperator);

        while (t.hasMoreTokens())
            customIds.add(IntPool.get(Integer.parseInt(t.nextToken())));

        return (Integer[]) customIds.toArray(new Integer[customIds.size()]);
    }

    public static void setUserIdList(Integer[] userIds) {
        String s = "";
        for (int i = 0, j = userIds.length; i < j; i++)
            s += userIds[i].toString() + Zoeos.preferenceFieldSeperator;
        DevicePreferences.ZPREF_voiceTableUserIds.putValue(s);
    }

    protected int numVoices;
    protected ArrayList<Boolean> expansionMemory;
    protected ParameterContext vc;

    Timer emptySampleRefreshTimer;

    private final static VoiceSwitchIcon onIcon = new VoiceSwitchIcon(10, 10, Color.DARK_GRAY, Color.LIGHT_GRAY, VoiceSwitchIcon.EXPANDED);
    private final static VoiceSwitchIcon offIcon = new VoiceSwitchIcon(10, 10, Color.DARK_GRAY, Color.LIGHT_GRAY, VoiceSwitchIcon.CONTRACTED);
    private final static VoiceSwitchIcon disabledIcon = new VoiceSwitchIcon(10, 10, Color.LIGHT_GRAY, Color.white, VoiceSwitchIcon.DISABLED);
    private final static EmptyIcon emptyIcon = new EmptyIcon(10, 10);

    int mode;
    private static int emptySampleRefreshInterval = 10000;

    public boolean includesUserParameters() {
        return (mode & PresetViewModes.VOICE_MODE_USER) != 0;
    }

    public VoiceOverviewTableModel(ReadablePreset p, DeviceParameterContext dpc, int mode) {
        super(p, dpc.getVoiceContext());
        this.mode = mode;
        vc = dpc.getVoiceContext();
        this.preset = p;
        init();
        WinValueProfile.ZPREF_keyWinDisplayMode.addChangeListener(this);
        WinValueProfile.ZPREF_velWinDisplayMode.addChangeListener(this);
        WinValueProfile.ZPREF_rtWinDisplayMode.addChangeListener(this);

        try {
            gsl = new SampleListenerAdapter() {
                public void sampleInitializationStatusChanged(SampleInitializationStatusChangedEvent ev) {
                    updateSampleCells(ev.getIndex());
                }

                public void sampleNameChanged(SampleNameChangeEvent ev) {
                    updateSampleCells(ev.getIndex());
                }

                public void sampleRefreshed(SampleRefreshEvent ev) {
                    updateSampleCells(ev.getIndex());
                }

                public void sampleInitialized(SampleInitializeEvent ev) {
                    updateSampleCells(ev.getIndex());
                }
            };
            Set s = preset.getDeviceContext().getDefaultSampleContext().getDatabaseIndexes();
            gsi = (Integer[]) s.toArray(new Integer[s.size()]);
            preset.getDeviceContext().getDefaultSampleContext().addContentListener(gsl, gsi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        emptySampleRefreshTimer = new Timer(emptySampleRefreshInterval, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Integer[] samples = getSampleIndexes();
                SampleContext sc = preset.getPresetContext().getRootSampleContext();
                for (int i = 0; i < samples.length; i++)
                    try {
                        if (samples[i].intValue() > 0 && samples[i].intValue() < DeviceContext.BASE_ROM_SAMPLE)
                            sc.refreshIfEmpty(samples[i]).post();
                    } catch (Exception e1) {
                    }
            }
        });
        emptySampleRefreshTimer.setCoalesce(true);
        emptySampleRefreshTimer.start();
    }

    public void updateSampleCells(Integer sample) {
        Integer col = ((Integer) id2col.get(IntPool.get(38)));
        if (col != null)
            for (int i = 0, n = this.getRowCount(); i < n; i++) {
                Object v = getValueAt(i, col.intValue());
                try {
                    if (v instanceof ReadableParameterModel && ((ReadableParameterModel) v).getValue().equals(sample))
                        VoiceOverviewTableModel.this.fireTableCellUpdated(i, col.intValue());
                } catch (ParameterException e) {
                }
            }
    }

    public void zDispose() {
        if (gsi != null && gsl != null)
            try {
                preset.getDeviceContext().getDefaultSampleContext().removeContentListener(gsl, gsi);
            } catch (DeviceException e) {
            }

        super.zDispose();
        WinValueProfile.ZPREF_keyWinDisplayMode.removeChangeListener(this);
        WinValueProfile.ZPREF_velWinDisplayMode.removeChangeListener(this);
        WinValueProfile.ZPREF_rtWinDisplayMode.removeChangeListener(this);
        if (emptySampleRefreshTimer != null)
            emptySampleRefreshTimer.stop();
        emptySampleRefreshTimer = null;
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof ZIntPref)
            VoiceOverviewTableModel.this.fireTableDataChanged();
    }

    public void setExpansionMemory(java.util.List<Boolean> mem) {
        expansionMemory = new ArrayList<Boolean>(mem);
        refresh(false);
    }

    protected java.util.List<Boolean> getExpansionMemory() {
        return (java.util.List<Boolean>) expansionMemory.clone();
    }

// returns a Boolean for each row, true if it's a voice and false if it's a zone
    public Boolean[] getRowState() {
        Boolean[] st = new Boolean[tableRowObjects.size()];

        for (int i = 0, j = tableRowObjects.size(); i < j; i++)
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
            } catch (EmptyException e) {
                handleEmptyException();
            } catch (PresetException e) {
                handlePresetException();
            }
        }
        return false;
    }

    private static final String SECTION_MAIN = "MAIN";
    private static final String SECTION_KEYWIN = "KEY WIN";
    private static final String SECTION_VELWIN = "VELOCITY WIN";
    private static final String SECTION_RTWIN = "REALTIME WIN";
    private static final String SECTION_USER = "USER";

    SectionData createSection(String section, int colWidthCount) {
        if (section.equals(SECTION_MAIN)) {
            return new SectionData(UIColors.getTableFirstSectionBG(),
                    UIColors.getTableFirstSectionHeaderBG(),
                    UIColors.getTableFirstSectionFG(),
                    colWidthCount,
                    "MAIN");
        } else if (section.equals(SECTION_KEYWIN)) {
            return new SectionData(UIColors.getTableSecondSectionBG(),
                    UIColors.getTableSecondSectionHeaderBG(),
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
                    });
        } else if (section.equals(SECTION_VELWIN)) {
            return new SectionData(UIColors.getTableThirdSectionBG(),
                    UIColors.getTableThirdSectionHeaderBG(),
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
                    });
        } else if (section.equals(SECTION_RTWIN)) {
            return new SectionData(UIColors.getTableFourthSectionBG(),
                    UIColors.getTableFourthSectionHeaderBG(),
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
                    });
        } else if (section.equals(SECTION_USER)) {
            return new SectionData(UIColors.getTableFifthSectionBG(),
                    UIColors.getTableFifthSectionHeaderBG(),
                    UIColors.getTableFifthSectionFG(),
                    colWidthCount,
                    "USER");
        } else
            throw new IllegalArgumentException("unsupported section");
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", DEF_COL_WIDTH + 2, JLabel.LEFT, 0, Object.class, VoiceOverviewRowHeaderTableCellRenderer.INSTANCE, null);
        columnData = new ColumnData[parameterObjects.size()];
        firstCustomCol = parameterObjects.size() - customIds.length;

        int id;
        ArrayList arrSectionData = new ArrayList();
        int sectionIndex = 0;
        int colWidthCount = 0;
        String currSection = null;
        id2col.clear();

        for (int i = 0, n = parameterObjects.size(); i < n; i++) {
            id = ((GeneralParameterDescriptor) parameterObjects.get(i)).getId().intValue();
            id2col.put(IntPool.get(id), IntPool.get(i + 1));
            if (ParameterUtilities.isKeyWinId(id)) {
                if (currSection != null && !currSection.equals(SECTION_KEYWIN)) {
                    arrSectionData.add(createSection(currSection, colWidthCount));
                    sectionIndex++;
                    colWidthCount = 0;
                }
                currSection = SECTION_KEYWIN;
            } else if (ParameterUtilities.isVelWinId(id)) {
                if (currSection != null && !currSection.equals(SECTION_VELWIN)) {
                    arrSectionData.add(createSection(currSection, colWidthCount));
                    sectionIndex++;
                    colWidthCount = 0;
                }
                currSection = SECTION_VELWIN;
            } else if (ParameterUtilities.isRTWinId(id)) {
                if (currSection != null && !currSection.equals(SECTION_RTWIN)) {
                    arrSectionData.add(createSection(currSection, colWidthCount));
                    sectionIndex++;
                    colWidthCount = 0;
                }
                currSection = SECTION_RTWIN;
            } else if (ParameterUtilities.isMainId(id)) {
                if (currSection != null && !currSection.equals(SECTION_MAIN)) {
                    arrSectionData.add(createSection(currSection, colWidthCount));
                    sectionIndex++;
                    colWidthCount = 0;
                }
                currSection = SECTION_MAIN;
            } else if (ParameterUtilities.isVoiceOverviewUserId(id)) {
                if (currSection != null && !currSection.equals(SECTION_USER)) {
                    arrSectionData.add(createSection(currSection, colWidthCount));
                    sectionIndex++;
                    colWidthCount = 0;
                }
                currSection = SECTION_USER;
            }
            generateColumnDataInstance(i, sectionIndex);
            colWidthCount += columnData[i].width;
        }
        arrSectionData.add(createSection(currSection, colWidthCount));
        sectionData = new SectionData[arrSectionData.size()];
        arrSectionData.toArray(sectionData);
        if (id2col.containsKey(ID.origKey))
            origKeyCol = id2col.get(ID.origKey).intValue();
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

        if (id == 37) // group
            columnData[i] = new ColumnData(title, (int) (DEF_COL_WIDTH * 0.8), JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        else if (id == 38) // sample
            columnData[i] = new ColumnData(title, (int) (DEF_COL_WIDTH * 3.45), JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        else if (id >= 41 && id <= 43) // tune, ftune, xpose
            columnData[i] = new ColumnData(title, (int) (DEF_COL_WIDTH * 0.8), JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        else if (id == 44) // alphanumeric key position
            columnData[i] = new ColumnData(title, DEF_COL_WIDTH + 5, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        //else if (id == 45 || id == 47) // alphanumeric key position and keyWin
        //    columnData[i] = new ColumnData(title, DEF_COL_WIDTH + 5, JLabel.LEFT, sectionIndex, ReadableParameterModel.class, WinTableCellRenderer.CANONICAL_RENDERERS[(id - 45) % 4]);
        else if (id >= 45 && id <= 56) // keyWin, velWin, rtWin
            columnData[i] = new ColumnData(title, DEF_COL_WIDTH, JLabel.LEFT, sectionIndex, ReadableParameterModel.class, WinTableCellRenderer.CANONICAL_RENDERERS[(id - 45) % 4]);
        else if (id < 57) // one of the defaults
            columnData[i] = new ColumnData(title, DEF_COL_WIDTH, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        else { // user id
            if (id == 82)
                columnData[i] = new ColumnData(title, DEF_COL_WIDTH + 77, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
            else
                columnData[i] = new ColumnData(title, DEF_COL_WIDTH + 17, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
        }

        /*
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
            */
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
            if ((mode & PresetViewModes.VOICE_MODE_MAIN) != 0) {

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
            }
            if ((mode & PresetViewModes.VOICE_MODE_KEY_WIN) != 0) {

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
            }
            if ((mode & PresetViewModes.VOICE_MODE_VEL_WIN) != 0) {
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
            }
            if ((mode & PresetViewModes.VOICE_MODE_RT_WIN) != 0) {

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
            }
            if ((mode & PresetViewModes.VOICE_MODE_USER) != 0) {
                for (int i = 0, j = customIds.length; i < j; i++) {
                    pd = vc.getParameterDescriptor(customIds[i]);
                    parameterObjects.add(pd);
                    zoneParameterDescriptors.add(ParameterUtilities.isZoneId(customIds[i].intValue()) ? pd : null);
                }
            }
        } catch (IllegalParameterIdException e) {
            SystemErrors.internal(e);
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

        public Integer getValue() throws ParameterException {
            return pm.getValue();
        }

        public void setTipShowingOwner(boolean tipShowsOwner) {
            pm.setTipShowingOwner(tipShowsOwner);
        }

        public boolean isTipShowingOwner() {
            return pm.isTipShowingOwner();
        }

        public String getValueString() throws ParameterException {
            return pm.getValueString();
        }

        public String getValueUnitlessString() throws ParameterException {
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

        public ZCommand[] getZCommands(Class markerClass) {
            return pm.getZCommands(markerClass);
        }

        // most capable/super first
        public Class[] getZCommandMarkers() {
            return pm.getZCommandMarkers();
        }

        public String toString() {
            Integer sn = IntPool.get(0);
            try {
                sn = pm.getValue();
                if (sn.intValue() >= 0)
                    return new ContextLocation(sn, sc.getName(pm.getValue())).toString();
            } catch (DeviceException e) {
            } catch (EmptyException e) {
                return new ContextLocation(sn, DeviceContext.EMPTY_SAMPLE).toString();
            } catch (ParameterException e) {
            } catch (ContentUnavailableException e) {
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
            } catch (DeviceException e) {
            } catch (ParameterException e) {
                e.printStackTrace();
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
            } catch (DeviceException e) {
            } catch (ParameterException e) {
            }
            return "";
        }

        public Object[] getWrappedObjects() {
            return new Object[]{pm};
        }
    }

    protected void doRefresh
            () {
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
        } catch (EmptyException e) {
            handleEmptyException();
        } catch (PresetException e) {
            handlePresetException();
        } finally {
//ignorePresetInitialize = false;
            if (oldExpansionMemory != null)
                oldExpansionMemory.clear();
        }
    }

    protected void doPreRefresh
            () {
        // TODO!! re-enable this   ??
        // ignorePresetInitialize = true;
    }

    protected void doPostRefresh
            () {
    }

    private Voice[] getVoices
            () {
        ArrayList voices = new ArrayList();
        Object ro;
        for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
            ro = tableRowObjects.get(i);
            if (ro instanceof Voice)
                voices.add(ro);
        }
        return (Voice[]) voices.toArray(new Voice[voices.size()]);
    }

    public void toggleAll
            () {
        Voice[] voices = getVoices();
        for (int i = 0, n = voices.length; i < n; i++)
            voices[i].toggle();
    }

    public void expandAll
            () {
        Voice[] voices = getVoices();
        for (int i = 0, n = voices.length; i < n; i++)
            voices[i].setExpanded(true);
    }

    public void contractAll
            () {
        Voice[] voices = getVoices();
        for (int i = 0, n = voices.length; i < n; i++)
            voices[i].setExpanded(false);
    }

    private Voice findVoice
            (Integer
            voiceIndex) {
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

    public void voiceAdded
            (final VoiceAddEvent ev) {
        newVoice(true);
    }

    public void voiceRemoved
            (final VoiceRemoveEvent ev) {
        Voice v = findVoice(ev.getVoice());
        if (v != null) {
            v.disposeVoice();
        } else
            refresh(false);
    }

    public void voiceChanged
            (final VoiceChangeEvent ev) {
        if (ev.getIndex().equals(preset.getIndex())) {
            for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
                if (tableRowObjects.get(i) instanceof Voice && ((Voice) tableRowObjects.get(i)).getVoice().getVoiceNumber().equals(ev.getVoice())) {
                    ((Voice) tableRowObjects.get(i)).updateParameters(ev.getIds());
                }
            }
        }
    }

    private void remapVoices
            () {
        Object ro;
        int cvi = 0;
        for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
            ro = tableRowObjects.get(i);
            if (ro instanceof Voice) {
                ((Voice) ro).getVoice().setVoiceNumber(IntPool.get(cvi++));
            }
        }
    }

    public void zoneAdded
            (final ZoneAddEvent ev) {
        Voice v = findVoice(ev.getVoice());
        if (v != null) {
            v.addZone();
        } else  // out of sync somehow
            refresh(false);
    }

    public void zoneRemoved
            (final ZoneRemoveEvent ev) {
        Voice v = findVoice(ev.getVoice());
        if (v != null) {
            v.removeZone(ev.getZone());
        } else {
            refresh(false);
        }
    }

    public void zoneChanged
            (final ZoneChangeEvent ev) {
        if (ev.getIndex().equals(preset.getIndex())) {
            for (int i = 0, n = tableRowObjects.size(); i < n; i++) {
                if (tableRowObjects.get(i) instanceof Voice.Zone && ((Voice.Zone) tableRowObjects.get(i)).getVoice().getVoiceNumber().equals(ev.getVoice()) && ((Voice.Zone) tableRowObjects.get(i)).getZone().getZoneNumber().equals(ev.getZone())) {
                    ((Voice.Zone) tableRowObjects.get(i)).updateParameters(ev.getIds());
                }
            }
        }
    }

    protected void newVoice
            (boolean expanded) {
        new Voice(getVoiceInterface(numVoices)).init(expanded);
    }

    protected ReadablePreset.ReadableVoice getVoiceInterface
            (int index) {
        return preset.getReadableVoice(IntPool.get(index));
    }

    public Integer[] getSampleIndexes() {
        ArrayList samples = new ArrayList();
        Integer col = ((Integer) id2col.get(IntPool.get(38)));
        if (col != null)
            for (int i = 0, n = this.getRowCount(); i < n; i++) {
                Object v = getValueAt(i, col.intValue());
                try {
                    if (v instanceof ReadableParameterModel) {
                        Integer s = ((ReadableParameterModel) v).getValue();
                        if (!samples.contains(s))
                            samples.add(s);
                    }
                } catch (ParameterUnavailableException e) {
                } catch (ParameterException e) {
                }
            }
        return (Integer[]) samples.toArray(new Integer[samples.size()]);
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
            } catch (EmptyException e) {
                handleEmptyException();
            } catch (PresetException e) {
                handlePresetException();
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
                    if (pm.getParameterDescriptor().getId().equals(ID.sample)) {
                        try {
                            if (pm.getValue().intValue() > 0 && pm.getValue().intValue() < DeviceContext.BASE_ROM_SAMPLE)
                                preset.getPresetContext().getRootSampleContext().refreshIfEmpty(pm.getValue()).post();
                        } catch (ResourceUnavailableException e) {
                        }
                    }
                } catch (ParameterException e) {
                    e.printStackTrace();
                    voiceColumnObjects.add("");
                }
            }
        }

        protected ReadableParameterModel getAppropiateParameterModelInterface(int i) throws ParameterException {
            ReadableParameterModel pm = voice.getParameterModel(((GeneralParameterDescriptor) parameterObjects.get(i)).getId());
            if (((GeneralParameterDescriptor) parameterObjects.get(i)).getId().equals(ID.sample))
                try {
                    return new VoiceSampleReadableParameterModel(pm, preset.getDeviceContext().getDefaultSampleContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            pm.setTipShowingOwner(true);
            return pm;
        }

        public Object getValueAt(int col) {
            try {
                if (col == sampleCol && hasZones()) // sample number
                    return new IconAndTipCarrier() {
                        public Icon getIcon() {
                            return emptyIcon;
                        }

                        public String getToolTipText() {
                            return null;
                        }

                        public String toString() {
                            try {
                                return DeviceContext.MULTISAMPLE + "[" + Voice.this.numZones() + "]";
                            } catch (Exception e) {

                            }
                            return DeviceContext.MULTISAMPLE;
                        }
                    };
                else if (origKeyCol == col && hasZones()) // orig key
                    return "";
                else if (voiceColumnObjects.size() > col)
                    return voiceColumnObjects.get(col);
            } catch (Exception e) {
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

        public ZCommand[] getZCommands(Class markerClass) {
            return ReadablePreset.ReadableVoice.cmdProviderHelper.getCommandObjects(markerClass, this);
        }

        public Class[] getZCommandMarkers() {
            return ReadablePreset.ReadableVoice.cmdProviderHelper.getSupportedMarkers();
        }

        public ReadablePreset getPreset() {
            return voice.getPreset();
        }

        public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer zone) throws PresetException, EmptyException {
            return voice.getIsolatedZone(zone);
        }

        public Integer[] getVoiceParams(Integer[] ids) throws ParameterException, PresetException, EmptyException {
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

        public IsolatedPreset.IsolatedVoice getIsolated() throws PresetException, EmptyException {
            return voice.getIsolated();
        }

        public ReadablePreset.ReadableVoice.ReadableZone getReadableZone(Integer zone) {
            return voice.getReadableZone(zone);
        }

        public int numZones() throws PresetException, EmptyException {
            return voice.numZones();
        }

        public ReadableParameterModel getParameterModel(Integer id) throws ParameterException {
            return voice.getParameterModel(id);
        }

        public Integer[] getVoiceIndexesInGroup() throws PresetException, EmptyException {
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

            public Integer[] getZoneParams(Integer[] ids) throws EmptyException, ParameterException, PresetException {
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

            public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolated() throws EmptyException, PresetException {
                return zone.getIsolated();
            }

            public void setZoneNumber(Integer zone) {
                this.zone.setZoneNumber(zone);
            }

            public ReadableParameterModel getParameterModel(Integer id) throws ParameterException {
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
                            if (pm.getParameterDescriptor().getId().equals(ID.sample)) {
                                try {
                                    if (pm.getValue().intValue() > 0 && pm.getValue().intValue() < DeviceContext.BASE_ROM_SAMPLE)
                                        preset.getPresetContext().getRootSampleContext().refreshIfEmpty(pm.getValue());
                                } catch (Exception e) {
                                }
                            }
                        }
                    } catch (ParameterException e) {
                        e.printStackTrace();
                        zoneColumnObjects.add("");
                    }
                }
            }

            public String toString() {
                return "";
            }

            protected ReadableParameterModel getAppropiateParameterModelInterface(GeneralParameterDescriptor pd) throws ParameterException {
                ReadableParameterModel pm = zone.getParameterModel(pd.getId());
                if (pd.getId().equals(ID.sample))
                    try {
                        return new VoiceSampleReadableParameterModel(pm, preset.getDeviceContext().getDefaultSampleContext());
                    } catch (DeviceException e) {
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
