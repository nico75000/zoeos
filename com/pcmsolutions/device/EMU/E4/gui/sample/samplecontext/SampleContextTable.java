package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.device.EMU.E4.selections.ContextSampleSelection;

import java.awt.*;
import java.util.ArrayList;
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
public class SampleContextTable extends AbstractRowHeaderedAndSectionedTable {
    protected SampleContext sampleContext;

    private static SampleContextTransferHandler scth = new SampleContextTransferHandler();

    public SampleContextTable(SampleContext sc) {
        super(new SampleContextTableModel(sc), scth, null, /*new RowHeaderTableCellRenderer(UIColors.getMultimodeRowHeaderBG(), UIColors.getMultimodeRowHeaderFG()),*/ "Sample>");
        this.sampleContext = sc;
        //this.setShowGrid(false);
        //this.setTableHeader(null);
        //setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //setDefaultRenderer(ContextReadablePreset.class, new PresetContextTableCellRenderer(UIColors.getPresetContextTableBG(), UIColors.getPresetContextTableFG(), new Color(255, 255, 255, 150), new Color(242, 81, 103, 250), 0.6));
        //setRowHeight(rowHeight);
        setDropChecker(defaultDropGridChecker);
        setMaximumSize(getPreferredSize());
    }

    public ContextSampleSelection getSelection() {
        Object[] sobjs = this.getSelObjects();
        ArrayList samples = new ArrayList();
        for (int i = 0,j = sobjs.length; i < j; i++)
            if (sobjs[i] instanceof ReadableSample)
                samples.add(((ReadableSample) sobjs[i]));

        return new ContextSampleSelection(sampleContext.getDeviceContext(), (ReadableSample[]) samples.toArray(new ReadableSample[samples.size()]), sampleContext);
    }

    public Integer selectSamplesByRegex(String regexStr, boolean fullMatch, boolean useDisplayName, boolean newSelection) {
        if (regexStr == null)
            return null;
        Pattern p = Pattern.compile(regexStr);
        SampleContextTableModel sctm = (SampleContextTableModel) getModel();
        Matcher m;
        if (newSelection)
            this.clearSelection();
        String name;
        Integer firstSelectedSample = null;
        for (int i = 0,j = sctm.getRowCount(); i < j; i++) {
            ReadableSample sample = (ReadableSample) sctm.getValueAt(i, 1);
            try {
                if (useDisplayName)
                    name = sample.getSampleDisplayName();
                else
                    name = sample.getSampleName();
            } catch (NoSuchSampleException e) {
                continue;
            } catch (SampleEmptyException e) {
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
                    firstSelectedSample = sample.getSampleNumber();
                this.addRowSelectionInterval(i, i);
                this.addColumnSelectionInterval(0, 0);
            }
        }
        return firstSelectedSample;
    }

    // inclusive (will ignore indexes that are not available)
    public void addSampleToSelection(Integer sample) {
        SampleContextTableModel sctm = (SampleContextTableModel) getModel();
        int row = sctm.getRowForSample(sample);
        if (row != -1) {
            addRowSelectionInterval(row, row);
            addColumnSelectionInterval(0, 0);
            //this.getSelectionModel().addSelectionInterval(row, row);
        }
    }

    public void addSamplesToSelection(Integer[] samples) {
        for (int i = 0; i < samples.length; i++)
            addSampleToSelection(samples[i]);
    }

    public void selectAllSamplesExcluded(Set samples) {
        SampleContextTableModel sctm = (SampleContextTableModel) getModel();
        this.selectAll();
        Integer s;
        for (Iterator i = samples.iterator(); i.hasNext();) {
            s = (Integer) i.next();
            int row = sctm.getRowForSample(s);
            if (row != -1)
                this.getSelectionModel().removeSelectionInterval(row, row);
        }
    }

    public boolean showingAllSamples(Integer[] samples) {
        SampleContextTableModel sctm = (SampleContextTableModel) getModel();
        for (int i = 0; i < samples.length; i++)
            if (samples[i].intValue() > 0 && sctm.getRowForSample(samples[i]) == -1)
                return false;
        return true;
    }

    public void invertSelection() {
        int[] selRows = this.getSelectedRows();
        this.selectAll();
        for (int i = 0; i < selRows.length; i++)
            this.removeRowSelectionInterval(selRows[i], selRows[i]);
    }

    // may return -1
    public int getRowForSample(Integer sample) {
        SampleContextTableModel sctm = (SampleContextTableModel) getModel();
        return sctm.getRowForSample(sample);
    }

    // will do nothing if index does not available
    public void scrollToSample(Integer sample) {
        SampleContextTableModel sctm = (SampleContextTableModel) getModel();
        int row = sctm.getRowForSample(sample);
        Rectangle cellRect = this.getCellRect(row, 0, true);
        this.scrollRectToVisible(cellRect);
    }

    public String getTableTitle() {
        return " ";
    }

    public void zDispose() {
        sampleContext = null;
    }
}
