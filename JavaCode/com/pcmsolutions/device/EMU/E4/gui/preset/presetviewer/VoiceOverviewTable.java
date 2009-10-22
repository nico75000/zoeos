package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinValueProfile;
import com.pcmsolutions.device.EMU.E4.gui.preset.WinValueProfileProvider;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelectionCollection;
import com.pcmsolutions.device.EMU.E4.selections.VoiceSelection;
import com.pcmsolutions.device.EMU.E4.selections.ZoneSelection;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.ZCommandFactory;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.util.ClassUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class VoiceOverviewTable extends AbstractRowHeaderedAndSectionedTable implements WinValueProfileProvider, RowHeaderedAndSectionedTable, ZDisposable, DropTargetListener {
    protected ReadablePreset preset;
    protected static VoiceOverviewTableTransferHandler votth = new VoiceOverviewTableTransferHandler();

    protected Action customAction;

    public interface VoiceAndZoneSelectionProvider {
        public static final int NOTHING_AVAILABLE = 0;
        public static final int VOICES_AVAILABLE = 1;
        public static final int ZONES_AVAILABLE = 2;

        public int whatIsAvailable();

        public VoiceSelection getVoiceSelection();

        public ZoneSelection getZoneSelection();

    }

    protected class VoiceOverviewTableRowHeader extends DragAndDropTable implements VoiceOverviewTable.VoiceAndZoneSelectionProvider {
        public VoiceOverviewTableRowHeader(String popupName, Color popupBG, Color popupFG) {
            super(popupName, popupBG, popupFG);
        }

        public VoiceOverviewTableRowHeader(TransferHandler t, String popupName, Color popupBG, Color popupFG) {
            super(t, popupName, popupBG, popupFG);
        }

        public void zDispose() {
        }

        public Object[] getSelObjects() {
            int[] selRows = this.getSelectedRows();
            int[] selCols = new int[]{0};  // only one column

            if (selRows != null && selCols != null) {
                int selRowCount = selRows.length;
                int selColCount = selCols.length;
                ArrayList selObjects = new ArrayList();
                for (int n = 0; n < selRowCount; n++)
                    for (int i = 0; i < selColCount; i++)
                        selObjects.add(this.getValueAt(selRows[n], selCols[i]));
                return selObjects.toArray();
            }
            return new Object[0];
        }

        public int whatIsAvailable() {
            int nv = 0;
            int nz = 0;
            int[] selRows = this.getSelectedRows();
            Object valueAt;
            for (int i = 0,j = selRows.length; i < j; i++) {
                valueAt = getValueAt(selRows[i], 0);
                if (valueAt instanceof ReadablePreset.ReadableVoice)
                    nv++;
                else if (valueAt instanceof ReadablePreset.ReadableVoice.ReadableZone)
                    nz++;
            }
            if (nv > 0 && nz == 0)
                return VOICES_AVAILABLE;
            else if (nz > 0 && nv == 0)
                return ZONES_AVAILABLE;

            return NOTHING_AVAILABLE;
        }

        public VoiceSelection getVoiceSelection() {
            if (whatIsAvailable() != VOICES_AVAILABLE)
                return null;

            int[] selRows = this.getSelectedRows();

            ReadablePreset.ReadableVoice[] readVoices = new ReadablePreset.ReadableVoice[selRows.length];

            for (int i = 0,j = selRows.length; i < j; i++)
                readVoices[i] = (ReadablePreset.ReadableVoice) getValueAt(selRows[i], 0);

            return new VoiceSelection(preset.getDeviceContext(), readVoices);
        }

        public ZoneSelection getZoneSelection() {
            if (whatIsAvailable() != ZONES_AVAILABLE)
                return null;

            int[] selRows = this.getSelectedRows();

            ReadablePreset.ReadableVoice.ReadableZone[] readZones = new ReadablePreset.ReadableVoice.ReadableZone[selRows.length];

            for (int i = 0,j = selRows.length; i < j; i++)
                readZones[i] = (ReadablePreset.ReadableVoice.ReadableZone) getValueAt(selRows[i], 0);

            return new ZoneSelection(preset.getDeviceContext(), readZones);
        }
    };

    protected DragAndDropTable generateRowHeaderTable() {
        VoiceOverviewTableRowHeader t = new VoiceOverviewTableRowHeader(votth, popupName, null, null) {
            public void zDispose() {
            }

            protected Component[] getCustomMenuItems() {
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

    public void setCustomAction(Action customAction) {
        this.customAction = customAction;
    }

    protected final Action expandAllAction = new AbstractAction("Expand all") {
        public void actionPerformed(ActionEvent e) {
            ((VoiceOverviewTableModel) getModel()).expandAll();
        }
    };
    protected final Action contractAllAction = new AbstractAction("Contract all") {
        public void actionPerformed(ActionEvent e) {
            ((VoiceOverviewTableModel) getModel()).contractAll();
        }
    };
    protected final Action toggleAllAction = new AbstractAction("Toggle all") {
        public void actionPerformed(ActionEvent e) {
            ((VoiceOverviewTableModel) getModel()).toggleAll();
        }
    };
    protected final JMenuItem[] customRowHeaderMenuItems = new JMenuItem[]{
        new JMenuItem(expandAllAction),
        new JMenuItem(contractAllAction),
        new JMenuItem(toggleAllAction)
    };

    public VoiceOverviewTable(ReadablePreset p, int mode) throws DeviceException {
        this(new VoiceOverviewTableModel(p, p.getDeviceContext().getDeviceParameterContext(), mode));
    }

    public VoiceOverviewTable(VoiceOverviewTableModel tm) {
        super(tm, null, null/*, new VoiceOverviewRowHeaderTableCellRenderer()*/, "Voice >");
        this.preset = tm.getPreset().getMostCapableNonContextEditablePreset();
        this.setTransferHandler(votth);
        getRowHeader().addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {                   
                    int row = getRowHeader().rowAtPoint(e.getPoint());
                    Object val = getRowHeader().getValueAt(row, 0);
                    if (val != null)
                        if (preset.getDeviceContext().getDevicePreferences().ZPREF_voiceDoubleClickEdits.getValue()) {
                            if (val instanceof ReadablePreset.ReadableVoice)
                                ((ReadablePreset.ReadableVoice) val).performOpenAction();
                        } else if (val instanceof Switchable)
                            ((Switchable) val).toggle();
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = rowAtPoint(e.getPoint());
                    int col = columnAtPoint(e.getPoint());
                    Object val = getValueAt(row, col);
                    if (val != null && val.toString().indexOf(DeviceContext.MULTISAMPLE) != -1)
                        if (preset.getDeviceContext().getDevicePreferences().ZPREF_voiceDoubleClickEdits.getValue()) {
                            val = getRowHeader().getValueAt(row, 0);
                            if (val instanceof Switchable) {
                                ((Switchable) val).toggle();
                                VoiceOverviewTable.this.clearSelection();
                            }
                        }
                }
            }
        });
        getRowHeader().setFocusable(true);
        this.setCustomRowHeaderMenuItems(customRowHeaderMenuItems);
    }

    protected ReadableSample convertPassThroughSample(ReadableSample sample) {
        return sample.getMostCapableNonContextEditableSampleDowngrade();
    }

    protected ReadablePreset convertPassThroughPreset(ReadablePreset preset) {
        return preset.getMostCapableNonContextEditablePreset();
    }

    protected Component[] getCustomMenuItems() {
        JMenuItem smi = null;
        Object[] selObjs = ZUtilities.eliminateInstances(this.getSelObjects(), String.class);
        try {
            SampleContext sc = preset.getDeviceContext().getDefaultSampleContext();
            if (ClassUtility.areAllInstanceOf(selObjs, ReadableParameterModel.class)) {
                ReadableParameterModel[] models = (ReadableParameterModel[]) Arrays.asList(selObjs).toArray(new ReadableParameterModel[selObjs.length]);
                if (ParameterModelUtilities.areAllOfId(models, 38) && !ParameterModelUtilities.areAllOfValue(models, 0)) {
                    Set s = ParameterModelUtilities.getValueSet(models);
                    ReadableSample[] samples = new ReadableSample[s.size()];
                    int si = 0;
                    for (Iterator i = s.iterator(); i.hasNext();)
                        samples[si++] = convertPassThroughSample(sc.getReadableSample((Integer) i.next()));
                    String mstr;
                    if (samples.length == 1)
                        mstr = samples[0].getDisplayName();
                    else
                        mstr = "Selected samples";
                    smi = ZCommandFactory.getMenu(samples, mstr);
                }
            }
        } catch (DeviceException e) {
            e.printStackTrace();
        } catch (ParameterException e) {
            e.printStackTrace();
        } 
        try {
            ArrayList menuItems = new ArrayList();
            menuItems.add(ZCommandFactory.getMenu(new Object[]{convertPassThroughPreset(preset)}, preset.getDisplayName()));
            if (smi != null)
                menuItems.add(smi);
            if (customAction != null)
                menuItems.add(new JMenuItem(customAction));
            return (JMenuItem[]) menuItems.toArray(new JMenuItem[menuItems.size()]);
        } catch (PresetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTableTitle() {
        return "VOICES";
    }

    protected VoiceParameterSelection getRowSelection(int row) throws EmptyException, ParameterException, PresetException, DeviceException{
        if (row >= 0 && row < getRowCount()) {
            int[] selCols = getSelectedColumns();
            Object val;
            ArrayList idList = new ArrayList();
            for (int i = 0,j = selCols.length; i < j; i++) {
                val = getValueAt(row, selCols[i]);
                //if (!(val instanceof ReadableParameterModel))
                //  throw new IllegalArgumentException("Cannot include empty cells in a selection");
                if (val instanceof ReadableParameterModel)
                    idList.add(((ReadableParameterModel) val).getParameterDescriptor().getId());
            }
            Object obj = getModel().getValueAt(row, 0);

            Integer[] ids = (Integer[]) idList.toArray(new Integer[idList.size()]);
            if (obj instanceof ReadablePreset.ReadableVoice)
                return new VoiceParameterSelection((ReadablePreset.ReadableVoice) obj, ids);
            else if (obj instanceof ReadablePreset.ReadableVoice.ReadableZone)
                return new VoiceParameterSelection(((ReadablePreset.ReadableVoice.ReadableZone) obj).getVoice(), ids, ((ReadablePreset.ReadableVoice.ReadableZone) obj).getZoneParams(ids));
            else
                throw new IllegalStateException("column 0 is not a voice nor a zone");
        }
        return null;
    }

    public VoiceParameterSelectionCollection getSelection() {
        int[] selRows;
        selRows = getSelectedRows();

        ArrayList sels = new ArrayList();
        for (int r = 0,rl = selRows.length; r < rl; r++)
            try {
                sels.add(getRowSelection(selRows[r]));
            } catch (Exception e) {
                e.printStackTrace();
            }

        return new VoiceParameterSelectionCollection(preset.getDeviceContext(), (VoiceParameterSelection[]) sels.toArray(new VoiceParameterSelection[sels.size()]), VoiceParameterSelection.VOICE_GENERAL);
    }

    public boolean selectedRowsIncludeZones() {
        int[] selRows = getSelectedRows();
        Boolean[] st = ((VoiceOverviewTableModel) getModel()).getRowState();

        for (int r = 0,k = selRows.length; r < k; r++)
            if (st[selRows[r]].equals(Boolean.FALSE))
                return true;
        return false;
    }

    public boolean hasValidVoiceParameterSelectionCollection() {
        if (selectedRowsIncludeZones()) {
            int[] selCols = getSelectedColumns();
            int[] selRows = getSelectedRows();
            for (int r = 0,k = selRows.length; r < k; r++) {
                for (int c = 0,j = selCols.length; c < j; c++) {
                    if (!(getValueAt(selRows[r], selCols[c]) instanceof ReadableParameterModel))
                        return false;
                }
            }
        }
        return true;
    }

    public WinValueProfile getWinValues(int row, int col) {
        Integer[] values;
        int type;
        try {
            VoiceOverviewTableModel tm = (VoiceOverviewTableModel) getModel();
            final Object ro = tm.getValueAt(row, 0);
            if (ro instanceof ReadablePreset.ReadableVoice) {
                ReadablePreset.ReadableVoice voice = (ReadablePreset.ReadableVoice) ro;
                Object co = tm.getValueAt(row, col + 1);
                if (co instanceof ReadableParameterModel) {
                    ReadableParameterModel pm = (ReadableParameterModel) co;
                    int id = pm.getParameterDescriptor().getId().intValue();
                    if (ParameterUtilities.isKeyWinId(id)) {
                        values = voice.getVoiceParams(ID.voiceKeyWin);
                        type = WinValueProfile.KEY_WIN;
                    } else if (ParameterUtilities.isVelWinId(id)) {
                        values = voice.getVoiceParams(ID.voiceVelWin);
                        type = WinValueProfile.VEL_WIN;
                    } else if (ParameterUtilities.isRTWinId(id)) {
                        values = voice.getVoiceParams(ID.voiceRTWin);
                        type = WinValueProfile.RT_WIN;
                    } else
                        return null;
                } else
                    return null;
            } else if (ro instanceof ReadablePreset.ReadableVoice.ReadableZone) {
                ReadablePreset.ReadableVoice.ReadableZone zone = (ReadablePreset.ReadableVoice.ReadableZone) ro;
                Object co = tm.getValueAt(row, col + 1);
                if (co instanceof ReadableParameterModel) {
                    ReadableParameterModel pm = (ReadableParameterModel) co;
                    int id = pm.getParameterDescriptor().getId().intValue();
                    if (ParameterUtilities.isKeyWinId(id)) {
                        values = zone.getZoneParams(ID.voiceKeyWin);
                        type = WinValueProfile.KEY_WIN;
                    } else if (ParameterUtilities.isVelWinId(id)) {
                        values = zone.getZoneParams(ID.voiceVelWin);
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
                    return ro instanceof ReadablePreset.ReadableVoice.ReadableZone;
                }

                public int getType() {
                    return f_type;
                }
            };
        } catch (EmptyException e) {
        } catch (IllegalParameterIdException e) {
        } catch (ParameterException e) {
        } catch (PresetException e) {
        }
        return null;
    }
}
