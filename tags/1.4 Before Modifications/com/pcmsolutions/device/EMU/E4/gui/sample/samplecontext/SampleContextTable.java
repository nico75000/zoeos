package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.gui.table.DragAndDropTable;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTable;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleException;
import com.pcmsolutions.device.EMU.E4.selections.ContextSampleSelection;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.DisabledTransferHandler;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 21-May-2003
 * Time: 01:19:26
 * To change this template use Options | File Templates.
 */
public class SampleContextTable extends AbstractContextTable<SampleContext, ReadableSample> {
    private static SampleContextTransferHandler scth = new SampleContextTransferHandler();

    public SampleContextTable(SampleContext sc) {
        super(sc, new SampleContextTableModel(sc), scth, null, /*new RowHeaderTableCellRenderer(UIColors.getMultimodeRowHeaderBG(), UIColors.getMultimodeRowHeaderFG()),*/ "Sample>");
       // this.sampleContext = sc;
        //this.setShowGrid(false);
        //this.setTableHeader(null);
        //setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //setDefaultRenderer(ContextReadablePreset.class, new PresetContextTableCellRenderer(UIColors.getPresetContextTableBG(), UIColors.getPresetContextTableFG(), new Color(255, 255, 255, 150), new Color(242, 81, 103, 250), 0.6));
        //setRowHeight(rowHeight);
        setDropChecker(defaultDropGridChecker);
        setMaximumSize(getPreferredSize());
        this.getRowHeader().setSelectionModel(this.getSelectionModel());
    }

    public ContextSampleSelection getSelection() {
        Object[] sobjs = this.getSelObjects();
        ArrayList samples = new ArrayList();
        for (int i = 0, j = sobjs.length; i < j; i++)
            if (sobjs[i] instanceof ReadableSample)
                samples.add(((ReadableSample) sobjs[i]));

        return new ContextSampleSelection(getContext().getDeviceContext(), (ReadableSample[]) samples.toArray(new ReadableSample[samples.size()]), getContext());
    }

    protected DragAndDropTable generateRowHeaderTable() {
        DragAndDropTable t = new DragAndDropTable(popupName, null, null) {
            public void zDispose() {
            }

            protected Component[] getCustomMenuItems() {
                return customRowHeaderMenuItems;
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final Object o = getModel().getValueAt(this.rowAtPoint(e.getPoint()), 1);
                    if (o instanceof ReadableSample)
                        try {
                            ((ReadableSample) o).audition().post(new Callback() {
                                public void result(Exception e, boolean wasCancelled) {
                                    if (e != null&& !wasCancelled)
                                        UserMessaging.flashWarning(null, e.getMessage());
                                }
                            });
                        } catch (ResourceUnavailableException e1) {
                            UserMessaging.flashWarning(null, e1.getMessage());
                        }
                } else
                    super.mouseClicked(e);
            }
        };
        t.setTransferHandler(DisabledTransferHandler.getInstance());
        return t;
    }


    public Integer selectSamplesByRegex(String regexStr, boolean fullMatch, boolean useDisplayName, boolean newSelection) {
        if (regexStr == null)
            return null;
        getSelectionModel().setValueIsAdjusting(true);
        try {
            Pattern p = Pattern.compile(regexStr);
            SampleContextTableModel sctm = (SampleContextTableModel) getModel();
            Matcher m;
            if (newSelection)
                this.clearSelection();
            String name;
            Integer firstSelectedSample = null;
            for (int i = 0, j = sctm.getRowCount(); i < j; i++) {
                ReadableSample sample = (ReadableSample) sctm.getValueAt(i, 1);
                try {
                    if (useDisplayName)
                        name = sample.getDisplayName();
                    else
                        name = sample.getName();
                } catch (SampleException e) {
                    continue;
                } catch (EmptyException e) {
                    name = DeviceContext.EMPTY_SAMPLE;
                }
                m = p.matcher(name);
                boolean res = false;
                if (fullMatch)
                    res = m.matches();
                else
                    res = m.find();
                if (res) {
                    if (firstSelectedSample == null)
                        firstSelectedSample = sample.getIndex();
                    this.addRowSelectionInterval(i, i);
                    this.addColumnSelectionInterval(0, 0);
                }
            }
            return firstSelectedSample;
        } finally {
            getSelectionModel().setValueIsAdjusting(false);
        }
    }

    public void selectAllSamplesExcluded(Set samples) {
        getSelectionModel().setValueIsAdjusting(true);
        try {
            SampleContextTableModel sctm = (SampleContextTableModel) getModel();
            this.selectAll();
            Integer s;
            for (Iterator i = samples.iterator(); i.hasNext();) {
                s = (Integer) i.next();
                int row = sctm.getRowForIndex(s);
                if (row != -1)
                    this.getSelectionModel().removeSelectionInterval(row, row);
            }
        } finally {
            getSelectionModel().setValueIsAdjusting(false);
        }
    }

    public String getTableTitle() {
        return " ";
    }

    public void zDispose() {
       super.zDispose();
    }
}
