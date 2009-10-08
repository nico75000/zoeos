package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinPopupMenu;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinValueProfile;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.preferences.ZIntPref;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 08:51:55
 * To change this template use Options | File Templates.
 */
public class LinkTableModel extends AbstractPresetTableModel implements ZDisposable, ChangeListener {
    protected static final Map id2col = new Hashtable();

    protected List readablePresets;
    protected int numLinks;

    private PresetListener gpl;
    private Integer[] gpi;

    public LinkTableModel(ReadablePreset p, DeviceParameterContext dpc) throws ZDeviceNotRunningException {
        super(p, dpc.getLinkContext());
        try {
            readablePresets = p.getDeviceContext().getDefaultPresetContext().getDatabasePresets();
        } catch (NoSuchContextException e1) {
            readablePresets = new ArrayList();
            e1.printStackTrace();
        }
        init();

        WinValueProfile.ZPREF_keyWinDisplayMode.addChangeListener(this);
        WinValueProfile.ZPREF_velWinDisplayMode.addChangeListener(this);


        try {
            gpl = new PresetListenerAdapter() {
                public void presetNameChanged(PresetNameChangeEvent ev) {
                    updatePresetCells(ev.getPreset());
                }

                public void presetInitialized(PresetInitializeEvent ev) {
                    updatePresetCells(ev.getPreset());
                }

                public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev) {
                    updatePresetCells(ev.getPreset());
                }

                public void presetRefreshed(PresetRefreshEvent ev) {
                    updatePresetCells(ev.getPreset());
                }
            };
            Set s = preset.getPresetContext().getDatabaseIndexes();
            gpi = (Integer[]) s.toArray(new Integer[s.size()]);
            preset.getPresetContext().addPresetListener(gpl, gpi);
        } catch (NoSuchContextException e) {
            e.printStackTrace();
        }
    }

    public void updatePresetCells(Integer preset) {
        Integer col = ((Integer) id2col.get(IntPool.get(23)));
        if (col != null)
            for (int i = 0, n = this.getRowCount(); i < n; i++) {
                Object v = getValueAt(i, col.intValue());
                try {
                    if (v instanceof ReadableParameterModel && ((ReadableParameterModel) v).getValue().equals(preset))
                        LinkTableModel.this.fireTableCellUpdated(i, col.intValue());
                } catch (ParameterUnavailableException e) {
                }
            }
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof ZIntPref)
            LinkTableModel.this.fireTableDataChanged();
    }

    public void zDispose() {
        if (gpi != null && gpl != null)
            preset.getPresetContext().removePresetListener(gpl, gpi);

        super.zDispose();
        WinValueProfile.ZPREF_keyWinDisplayMode.removeChangeListener(this);
        WinValueProfile.ZPREF_velWinDisplayMode.removeChangeListener(this);
        readablePresets.clear();
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", DEF_COL_WIDTH, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[parameterObjects.size()];
        String title;
        GeneralParameterDescriptor pd;
        int id;
        ArrayList arrSectionData = new ArrayList();
        int sectionIndex = 0;
        int colWidthCount = 0;

        id2col.clear();

        for (int i = 0, n = parameterObjects.size(); i < n; i++) {
            pd = (GeneralParameterDescriptor) parameterObjects.get(i);
            title = getColNameFromRefString(pd.getReferenceString(), 1);
            id = pd.getId().intValue();

            id2col.put(IntPool.get(id), IntPool.get(i + 1));

            if (id == 23)
                columnData[i] = new ColumnData(title, DEF_COL_WIDTH * 4, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
            else if (id == 28 || id == 30) // alphanumeric key position and keyWin
                columnData[i] = new ColumnData(title, DEF_COL_WIDTH + 5, JLabel.LEFT, sectionIndex, ReadableParameterModel.class, WinTableCellRenderer.CANONICAL_RENDERERS[(id - 28) % 4]);
            else if (id >= 28 && id <= 35) // keyWin, velWin
                columnData[i] = new ColumnData(title, DEF_COL_WIDTH, JLabel.LEFT, sectionIndex, ReadableParameterModel.class, WinTableCellRenderer.CANONICAL_RENDERERS[(id - 28) % 4]);
            else if (id == 251)
                columnData[i] = new ColumnData(title, (DEF_COL_WIDTH * 3) / 2, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);
            else
                columnData[i] = new ColumnData(title, DEF_COL_WIDTH, JLabel.LEFT, sectionIndex, ReadableParameterModel.class);

            colWidthCount += columnData[i].width;

            if (id == 27) {
                arrSectionData.add(new SectionData(
                        UIColors.getTableFirstSectionBG(),
                        UIColors.getTableFirstSectionFG(),
                        colWidthCount,
                        "MAIN"));
                sectionIndex++;
                colWidthCount = 0;
            } else if (id == 31) {
                arrSectionData.add(new SectionData(
                        UIColors.getTableSecondSectionBG(),
                        UIColors.getTableSecondSectionFG(),
                        colWidthCount,
                        "KEY WIN", new MouseAdapter() {
                            public void mouseReleased(MouseEvent e) {
                                if (e.isPopupTrigger())
                                    new WinPopupMenu(WinValueProfile.ZPREF_keyWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                            }

                            public void mousePressed(MouseEvent e) {
                                if (e.isPopupTrigger())
                                    new WinPopupMenu(WinValueProfile.ZPREF_keyWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                            }

                            public void mouseClicked(MouseEvent e) {
                                if (e.isPopupTrigger())
                                    new WinPopupMenu(WinValueProfile.ZPREF_keyWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                else if (e.getClickCount() >= 2) {
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

            } else if (id == 35) {
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
                                if (e.isPopupTrigger())
                                    new WinPopupMenu(WinValueProfile.ZPREF_velWinDisplayMode).getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                                else if (e.getClickCount() >= 2) {
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
            } else if (preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.getValue() && id == 266) {
                arrSectionData.add(new SectionData(
                        UIColors.getTableFourthSectionBG(),
                        UIColors.getTableFourthSectionFG(),
                        colWidthCount,
                        "MIDI FILTERS"));
                sectionIndex++;
                colWidthCount = 0;
            }
        }
        sectionData = new SectionData[arrSectionData.size()];
        arrSectionData.toArray(sectionData);
    }

    protected void buildDefaultParameterData(ParameterContext vc) {
/*        E4_LINK_PRESET,            id = 23 (17h,00h)     min =   0;  max = 999(1255)
        E4_LINK_VOLUME,            id = 24 (18h,00h)     min = -96;  max = +10
        E4_LINK_PAN,               id = 25 (19h,00h)     min = -64;  max = +63
        E4_LINK_TRANSPOSE,         id = 26 (1Ah,00h)     min = -24;  max = +24
        E4_LINK_FINE_TUNE,         id = 27 (1Bh,00h)     min = -64;  max = +64

        E4_LINK_KEY_LOW,           id = 28 (1Ch,00h)     min = 0;  max = 127  (C-2 -> G8)
        E4_LINK_KEY_LOWFADE,       id = 29 (1Dh,00h)     min = 0;  max = 127
        E4_LINK_KEY_HIGH,          id = 30 (1Eh,00h)     min = 0;  max = 127  (C-2 -> G8)
        E4_LINK_KEY_HIGHFADE,      id = 31 (1Fh,00h)     min = 0;  max = 127

        E4_LINK_VEL_LOW,           id = 32 (20h,00h)     min = 0;  max = 127
        E4_LINK_VEL_LOWFADE,       id = 33 (21h,00h)     min = 0;  max = 127
        E4_LINK_VEL_HIGH,          id = 34 (22h,00h)     min = 0;  max = 127
        E4_LINK_VEL_HIGHFADE,      id = 35 (23h,00h)     min = 0;  max = 127

 E4_LINK_INTERNAL_EXTERNAL, id = 251 (7Bh,01h)   min = 0;  max =  16;  default = 0;
 E4_LINK_FILTER_PITCH,      id = 252 (7Ch,01h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_MOD,        id = 253 (7Dh,01h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_PRESSURE,   id = 254 (7Eh,01h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_PEDAL,      id = 255 (7Fh,01h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_CTRL_A,     id = 256 (00h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_CTRL_B,     id = 257 (01h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_CTRL_C,     id = 258 (02h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_CTRL_D,     id = 259 (03h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_CTRL_E,     id = 260 (04h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_CTRL_F,     id = 261 (05h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_CTRL_G,     id = 262 (06h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_CTRL_H,     id = 263 (07h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_SWITCH_1,   id = 264 (08h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_SWITCH_2,   id = 265 (09h,02h)   min = 0;  max =   1;  default = 0;
 E4_LINK_FILTER_THUMB,      id = 266 (0Ah,02h)   min = 0;  max =   1;  default = 0;

    0 = filter off
    1 = filter on

  */
        GeneralParameterDescriptor pd;
        try {
            pd = vc.getParameterDescriptor(IntPool.get(23));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(24));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(25));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(26));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(27));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(28));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(29));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(30));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(31));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(32));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(33));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(34));
            parameterObjects.add(pd);

            pd = vc.getParameterDescriptor(IntPool.get(35));
            parameterObjects.add(pd);

            if (preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.getValue()) {
                pd = vc.getParameterDescriptor(IntPool.get(251));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(252));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(253));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(254));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(255));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(256));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(257));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(258));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(259));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(260));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(261));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(262));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(263));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(264));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(265));
                parameterObjects.add(pd);

                pd = vc.getParameterDescriptor(IntPool.get(266));
                parameterObjects.add(pd);
            }
        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        }
    }

    protected static class LinkPresetReadableParameterModel implements ParameterModelWrapper, ReadableParameterModel, IconAndTipCarrier {
        protected ReadableParameterModel pm;
        protected PresetContext defPC;

        public LinkPresetReadableParameterModel(ReadableParameterModel pm, PresetContext pc) {
            this.pm = pm;
            defPC = pc;
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
            Integer pn = IntPool.get(0);
            try {
                pn = pm.getValue();
                return new AggRemoteName(pn, defPC.getPresetName(pm.getValue())).toString();
            } catch (ParameterUnavailableException e) {
                //e.printStackTrace();
            } catch (NoSuchPresetException e) {
            } catch (PresetEmptyException e) {
                return new AggRemoteName(pn, DeviceContext.EMPTY_PRESET).toString();
            }
            return pn.toString();
        }

        public Icon getIcon() {
            try {
                return defPC.getReadablePreset(pm.getValue()).getIcon();
            } catch (NoSuchPresetException e) {
            } catch (ParameterUnavailableException e) {
            }
            return null;
        }

        public String getToolTipText() {
            try {
                return defPC.getReadablePreset(pm.getValue()).getToolTipText();
            } catch (NoSuchPresetException e) {
            } catch (ParameterUnavailableException e) {
            }
            return "";
        }

        public Object[] getWrappedObjects() {
            return new Object[]{pm};
        }
    }

    protected void doRefresh() {
        // TODO!! re-enable this?
        //ignorePresetInitialize = true;
        numLinks = 0;
        try {
            int nl = 0;
            nl = preset.numLinks();
            for (int i = 0, n = nl; i < n; i++) {
                newLink();
                numLinks++;
            }
        } catch (NoSuchPresetException e) {
            handleNoSuchPresetException();
        } catch (PresetEmptyException e) {
            handlePresetEmptyException();
        } finally {
            //ignorePresetInitialize = false;
        }
    }

    protected void doPreRefresh() {
    }

    protected void doPostRefresh() {
    }

    public void linkAdded(final LinkAddEvent ev) {
        final int num = ev.getNumberOfLinks();
        for (int n = 0; n < num; n++) {
            newLink();
            fireTableRowsInserted(numLinks - 1, numLinks - 1); // -1 because newLink incremented numLinks
            numLinks++;
        }
    }

    public void linkRemoved(final LinkRemoveEvent ev) {
        ((Link) tableRowObjects.remove(ev.getLink().intValue())).zDispose();
        remapLinks();
        numLinks--;
        fireTableRowsDeleted(ev.getLink().intValue(), ev.getLink().intValue());
    }

    public void linkChanged(final LinkChangeEvent ev) {
        int ind = ev.getLink().intValue();
        if (ind >= 0 && ind < tableRowObjects.size())
            ((LinkTableModel.Link) tableRowObjects.get(ind)).updateParameters(ev.getParameters());
    }

    private void remapLinks() {
        for (int i = 0, n = tableRowObjects.size(); i < n; i++)
            ((LinkTableModel.Link) tableRowObjects.get(i)).getLink().setLinkNumber(IntPool.get(i));
    }

    protected void newLink() {
        new Link(preset.getReadableLink(IntPool.get(numLinks))).init();
    }

    protected class Link implements ZDisposable, ColumnValueProvider, ReadablePreset.ReadableLink, IconAndTipCarrier {
        protected ReadablePreset.ReadableLink link;
        protected ArrayList linkColumnObjects = new ArrayList();

        public Link(ReadablePreset.ReadableLink l) {
            this.link = l;
        }

        public void init() {
            createColumnObjects();
            tableRowObjects.add(this);
        }

        public void updateParameters(Integer[] parameters) {
            final int thisIndex = tableRowObjects.indexOf(this);
            boolean keyWinUpdated = false;
            boolean velWinUpdated = false;
            for (int i = 0, n = parameters.length; i < n; i++) {
                if (ParameterUtilities.isKeyWinId(parameters[i].intValue())) {
                    if (!keyWinUpdated) {
                        keyWinUpdated = true;
                        for (int j = 0; j < ID.linkKeyWin.length; j++)
                            updateId(thisIndex, ID.linkKeyWin[j]);
                    } else
                        continue;
                } else if (ParameterUtilities.isVelWinId(parameters[i].intValue())) {
                    if (!velWinUpdated) {
                        velWinUpdated = true;
                        for (int j = 0; j < ID.linkVelWin.length; j++)
                            updateId(thisIndex, ID.linkVelWin[j]);
                    } else
                        continue;
                }                    //    for (int j = 2, o = zoneColumnObjects.size(); j < o; j++) {
                else
                    updateId(thisIndex, parameters[i]);


                //for (int j = 1, o = linkColumnObjects.size(); j < o; j++) {
                //  if (parameters[i].equals(((ReadableParameterModel) linkColumnObjects.get(j)).getParameterDescriptor().getId())) {
                //    LinkTableModel.this.fireTableCellUpdated(thisIndex, j);
                //  break;
                // }
                //}

            }
        }

        private void updateId(int row, Integer id) {
            Integer col = ((Integer) id2col.get(id));
            if (col != null) {
                LinkTableModel.this.fireTableCellUpdated(row, col.intValue());
            }
        }

        public String toString() {
            return "L" + IntPool.get(link.getLinkNumber().intValue() + 1);
        }

        protected void createColumnObjects() {
            ReadableParameterModel pm;
            linkColumnObjects.add(this);
            for (int i = 0, n = parameterObjects.size(); i < n; i++) {
                try {
                    pm = getAppropiateParameterModelInterface(i);
                    pm.setShowUnits(false);
                    linkColumnObjects.add(pm);
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                    linkColumnObjects.add("");
                }
            }
        }

        protected ReadableParameterModel getAppropiateParameterModelInterface(int i) throws IllegalParameterIdException {
            ReadableParameterModel pm = link.getParameterModel(((GeneralParameterDescriptor) parameterObjects.get(i)).getId());
            if (((GeneralParameterDescriptor) parameterObjects.get(i)).getId().equals(IntPool.get(23)))
                return new LinkPresetReadableParameterModel(pm, preset.getPresetContext());
            pm.setTipShowingOwner(true);
            return pm;
        }

        public Object getValueAt(int col) {
            return (linkColumnObjects.size() > col ? linkColumnObjects.get(col) : "");
        }

        public ReadablePreset.ReadableLink getLink() {
            return link;
        }

        // called by refresh
        public void zDispose() {
            // to avoid infinite recursion - remember "this" is at index 0
            linkColumnObjects.remove(0);
            ZUtilities.zDisposeCollection(linkColumnObjects);
        }

        public PresetContext getPresetContext() {
            return ((ContextEditablePreset.EditableLink) link).getPresetContext();
        }

        public Integer[] getLinkParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException {
            return link.getLinkParams(ids);
        }

        public Integer getLinkNumber() {
            return link.getLinkNumber();
        }

        public void setLinkNumber(Integer link) {
            this.link.setLinkNumber(link);
        }

        public IsolatedPreset.IsolatedLink getIsolated() throws PresetEmptyException, NoSuchPresetException, NoSuchLinkException {
            return link.getIsolated();
        }

        public ReadablePreset getPreset() {
            return link.getPreset();
        }

        public Integer getPresetNumber() {
            return preset.getPresetNumber();
        }

        public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException {
            return link.getParameterModel(id);
        }

        public int compareTo(Object o) {
            return link.compareTo(o);
        }

        public ZCommand[] getZCommands() {
            //return cmdProviderHelper.getCommandObjects(this);
            return new ZCommand[0];
        }

        public Icon getIcon() {
            return null;
        }

        public String getToolTipText() {
            return "Link " + IntPool.get(link.getLinkNumber().intValue() + 1);

        }
    }
}
