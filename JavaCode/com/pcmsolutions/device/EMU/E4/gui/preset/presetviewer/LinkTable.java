package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinValueProfile;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinValueProfileProvider;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.selections.LinkParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.LinkParameterSelectionCollection;
import com.pcmsolutions.device.EMU.E4.selections.LinkSelection;
import com.pcmsolutions.gui.ZCommandInvocationHelper;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.util.ClassUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class LinkTable extends AbstractRowHeaderedAndSectionedTable implements WinValueProfileProvider, ZDisposable {//, LinkParameterSelectionCollectionProvider {
    protected ReadablePreset preset;
    protected static LinkTableTransferHandler ltth = new LinkTableTransferHandler();
    protected Action customAction;

    public LinkTable(ReadablePreset p) throws ZDeviceNotRunningException {
        this(new LinkTableModel(p, p.getDeviceContext().getDeviceParameterContext()));
    }

    public static interface LinkSelectionProvider {
        public LinkSelection getSelection();
    }

    // public static interface LinkParameterSelectionCollectionProvider {
    //   public LinkParameterSelectionCollection getSelection();
    //  }
    public void setCustomAction(Action customAction) {
        this.customAction = customAction;
    }

    protected class LinkTableRowHeader extends DragAndDropTable implements LinkSelectionProvider {
        public LinkTableRowHeader(String popupName, Color popupBG, Color popupFG) {
            super(popupName, popupBG, popupFG);
        }

        public LinkTableRowHeader(TransferHandler t, String popupName, Color popupBG, Color popupFG) {
            super(t, popupName, popupBG, popupFG);
        }

        public void zDispose() {
        }

        public LinkSelection getSelection() {
            int[] selRows = this.getSelectedRows();
            ReadablePreset.ReadableLink[] readLinks = new ReadablePreset.ReadableLink[selRows.length];
            for (int i = 0,j = selRows.length; i < j; i++)
                readLinks[i] = (ReadablePreset.ReadableLink) getValueAt(selRows[i], 0);
            return new LinkSelection(preset.getDeviceContext(), readLinks);
        }
    };

    protected DragAndDropTable generateRowHeaderTable() {
        LinkTableRowHeader t = new LinkTableRowHeader(ltth, popupName, null, null) {
            public void zDispose() {
            }

            protected JMenuItem[] getCustomMenuItems() {
                return customRowHeaderMenuItems;
            }

            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (!e.isConsumed() && e.getClickCount() == 2)
                    this.clearSelection();
            }
        };
        return t;
    }

    public LinkTable(LinkTableModel ltm) throws ZDeviceNotRunningException {
        super(ltm, null, null /*new RowHeaderTableCellRenderer(UIColors.getVoiceOverViewTableRowHeaderSectionBG(), UIColors.getVoiceOverViewTableRowHeaderSectionFG())*/, "Link >");
        this.preset = ltm.getPreset().getMostCapableNonContextEditablePresetDowngrade();
        setDragEnabled(true);
        this.setTransferHandler(ltth);
        getRowHeader().setFocusable(true);
    }

    protected ReadablePreset convertPassThroughPreset(ReadablePreset preset) {
        return preset.getMostCapableNonContextEditablePresetDowngrade();
    }

    protected JMenuItem[] getCustomMenuItems() {
        JMenuItem smi = null;
        Object[] selObjs = this.getSelObjects();
        try {
            PresetContext pc = preset.getPresetContext();
            if (ClassUtility.areAllInstanceOf(selObjs, ReadableParameterModel.class)) {
                ReadableParameterModel[] models = (ReadableParameterModel[]) Arrays.asList(selObjs).toArray(new ReadableParameterModel[selObjs.length]);
                if (ParameterModelUtilities.areAllOfId(models, 23)) {
                    Set s = ParameterModelUtilities.getValueSet(models);
                    ReadablePreset[] presets = new ReadablePreset[s.size()];
                    int si = 0;
                    for (Iterator i = s.iterator(); i.hasNext();)
                        presets[si++] = convertPassThroughPreset(pc.getReadablePreset((Integer) i.next()));
                    String mstr;
                    if (presets.length == 1)
                        mstr = presets[0].getPresetDisplayName();
                    else
                        mstr = "Selected presets";
                    smi = ZCommandInvocationHelper.getMenu(presets, null, null, mstr);
                }
            }
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        } catch (ParameterUnavailableException e) {
            e.printStackTrace();
        }
        try {
            ArrayList menuItems = new ArrayList();
            menuItems.add(ZCommandInvocationHelper.getMenu(new Object[]{convertPassThroughPreset(preset)}, null/*UIColors.getCustomPopupFG()*/, null/*UIColors.getCustomPopupBG()*/, preset.getPresetDisplayName()));
            if (smi != null)
                menuItems.add(smi);
            if (customAction != null)
                menuItems.add(new JMenuItem(customAction));
            return (JMenuItem[]) menuItems.toArray(new JMenuItem[menuItems.size()]);
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTableTitle() {
        return "LINKS";
    }

    protected LinkParameterSelection getRowSelection(int row) throws ZDeviceNotRunningException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, NoSuchPresetException {

        if (row >= 0 && row < getRowCount()) {
            int[] selCols = getSelectedColumns();
            Object val;
            ArrayList idList = new ArrayList();
            for (int i = 0,j = selCols.length; i < j; i++) {
                val = getValueAt(row, selCols[i]);
                idList.add(((ReadableParameterModel) val).getParameterDescriptor().getId());
            }
            return new LinkParameterSelection((ReadablePreset.ReadableLink) getModel().getValueAt(row, 0), (Integer[]) idList.toArray(new Integer[idList.size()]), LinkParameterSelection.LINK_GENERAL);
        }
        return null;
    }

    public LinkParameterSelectionCollection getSelection() {
        int[] selRows;
        selRows = getSelectedRows();

        ArrayList sels = new ArrayList();
        for (int r = 0,rl = selRows.length; r < rl; r++)
            try {
                sels.add(getRowSelection(selRows[r]));
            } catch (ZDeviceNotRunningException e) {
                e.printStackTrace();
            } catch (PresetEmptyException e) {
                e.printStackTrace();
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            } catch (NoSuchLinkException e) {
                e.printStackTrace();
            } catch (NoSuchPresetException e) {
                e.printStackTrace();
            }

        return new LinkParameterSelectionCollection(preset.getDeviceContext(), (LinkParameterSelection[]) sels.toArray(new LinkParameterSelection[sels.size()]), LinkParameterSelection.LINK_GENERAL);
    }

    public WinValueProfile getWinValues(int row, int col) {
        int type;
        Integer[] values;
        try {
            LinkTableModel tm = (LinkTableModel) getModel();
            Object ro = tm.getValueAt(row, 0);
            if (ro instanceof ReadablePreset.ReadableLink) {
                ReadablePreset.ReadableLink link = (ReadablePreset.ReadableLink) ro;
                Object co = tm.getValueAt(row, col + 1);
                if (co instanceof ReadableParameterModel) {
                    ReadableParameterModel pm = (ReadableParameterModel) co;
                    int id = pm.getParameterDescriptor().getId().intValue();
                    if (ParameterUtilities.isKeyWinId(id)) {
                        values = link.getLinkParams(ID.linkKeyWin);
                        type = WinValueProfile.KEY_WIN;
                    } else if (ParameterUtilities.isVelWinId(id)) {
                        values = link.getLinkParams(ID.linkVelWin);
                        type = WinValueProfile.VEL_WIN;
                    } else
                        return null;
                } else
                    return null;
            } else
                return null;
            final Integer[] f_values = values;
            final int f_type = type;
            return new WinValueProfile() {
                public int getLow() {
                    return f_values[0].intValue();
                }

                public int getLowFade() {
                    return f_values[1].intValue();
                }

                public int getHigh() {
                    return f_values[2].intValue();
                }

                public int getHighFade() {
                    return f_values[3].intValue();
                }

                public boolean isChildWindow() {
                    return false;
                }

                public int getType() {
                    return f_type;
                }
            };
        } catch (NoSuchPresetException e) {
        } catch (PresetEmptyException e) {
        } catch (IllegalParameterIdException e) {
        } catch (NoSuchLinkException e) {
        }
        return null;
    }
}
