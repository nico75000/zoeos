package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.events.sample.*;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTableModel;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.database.ContextListener;
import com.pcmsolutions.device.EMU.database.Context;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.events.context.ContextRemovalEvent;
import com.pcmsolutions.device.EMU.database.events.context.ContextAdditionEvent;
import com.pcmsolutions.device.EMU.database.events.context.ContextReleaseEvent;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.IntPool;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SampleContextTableModel extends AbstractContextTableModel<SampleContext, ReadableSample> implements SampleListener {
    public SampleContextTableModel(SampleContext sc) {
        init(sc);
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 45, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[1];
        columnData[0] = new ColumnData("", 155, JLabel.LEFT, 0, ContextReadableSample.class, new SampleContextTableCellRenderer(), null);
        sectionData = new SectionData[]{new SectionData(UIColors.getTableFirstSectionBG(), UIColors.getTableFirstSectionHeaderBG(), UIColors.getTableFirstSectionFG(), 155, "")};
    }

    protected void doPreRefresh() {
        removeSampleListeners();
    }

    protected void doPostRefresh() {
    }


    protected void xdoRefresh() {
        try {
            //final List sic = filterSampleList(sc.getContextSamples());
            final List<ContextReadableSample> sic = getContext().getContextSamples();
            final DecimalFormat df = new DecimalFormat("0000");
            int count = 0;
            for (int i = 0, n = sic.size(); i < n; i++) {
                final ContextReadableSample s = sic.get(i);
                if (s.getIndex().intValue() == 0)
                    continue;
                try {
                    if (!contextFilter.filter(s.getIndex(), s.getString(), prevIndexes.contains(s.getIndex())))
                        continue;
                } catch (SampleException e) {
                    continue;
                } /*catch (EmptyException e) {
                    if (!contextFilter.filter(s.getIndex(), DeviceContext.EMPTY_SAMPLE, prevSampleIndexes.containsKey(s.getIndex())))
                        continue;
                } */
                s.setToStringFormatExtended(false);
                indexes.put(s.getIndex(), IntPool.get(count++));
                tableRowObjects.add(new ColumnValueProvider() {
                    private ReadableSample sample = s;

                    public Object getValueAt(int col) {
                        if (col == 0)
                            return "S " + df.format(sample.getIndex());
                        else if (col == 1)
                            return sample;
                        return "";
                    }

                    public void zDispose() {
                    }

                    public boolean equals(Object obj) {
                        if (obj instanceof Integer && obj.equals(sample.getIndex()))
                            return true;
                        return false;
                    }
                });
                s.addListener(this);
            }
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    public boolean acceptElement(ReadableSample readableSample) {
        return readableSample.getIndex().intValue() != 0;
    }

    protected String getContextPrefix() {
        return "S ";
    }

    protected void finalizeRefreshedElement(ReadableSample readableSample) throws DeviceException {
        readableSample.addListener(this);
    }

    private void removeSampleListeners() {
        Integer[] samples = new Integer[tableRowObjects.size()];
        for (int i = 0, n = tableRowObjects.size(); i < n; i++)
            samples[i] = ((ReadableSample) ((ColumnValueProvider) tableRowObjects.get(i)).getValueAt(1)).getIndex();
        getContext().removeContentListener(this, samples);
    }

    public void sampleInitialized(SampleInitializeEvent ev) {
        updateIndex(ev.getIndex());
    }

    public void sampleRefreshed(SampleRefreshEvent ev) {
        updateIndex(ev.getIndex());
    }

    public void sampleChanged(SampleChangeEvent ev) {
    }

    public void sampleNameChanged(SampleNameChangeEvent ev) {
        updateIndex(ev.getIndex());
    }

    public void sampleInitializationStatusChanged(SampleInitializationStatusChangedEvent ev) {
        updateIndex(ev.getIndex());
    }
}
