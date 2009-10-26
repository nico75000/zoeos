package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTableModel;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext.PresetContextTableCellRenderer;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.*;
import com.pcmsolutions.system.IntPool;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 25-Jun-2003
 * Time: 01:40:38
 * To change this template use Options | File Templates.
 */
public class SampleContextTableModel extends AbstractContextTableModel implements SampleContextListener, SampleListener {
    private SampleContext sc;
    private HashMap sampleIndexes = new HashMap();
    private HashMap prevSampleIndexes;

    public SampleContextTableModel(SampleContext sc) {
        this.sc = sc;
        init();
        sc.addSampleContextListener(this);
    }

    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 45, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[1];
        columnData[0] = new ColumnData("", 155, JLabel.LEFT, 0, ContextReadableSample.class, new PresetContextTableCellRenderer(), null);
        sectionData = new SectionData[]{new SectionData(UIColors.getTableFirstSectionBG(), UIColors.getTableFirstSectionFG(), 155, "")};
    }

    protected void doPreRefresh() {
        removeSampleListeners();
        prevSampleIndexes = (HashMap) sampleIndexes.clone();
        sampleIndexes.clear();
    }

    protected void doPostRefresh() {
    }

    /* protected List filterSampleList(List sic) {
        ReadableSample s;
        ArrayList outSic = new ArrayList(sic.size());
        for (int i = 0, n = sic.size(); i < n; i++) {
            s = (ReadableSample) sic.get(i);
            try {
                if (!contextFilter.filter(s.getSampleNumber(), s.getSampleName(), prevSampleIndexes.containsKey(s.getSampleNumber())))
                    continue;
                outSic.addDesktopElement(s);
            } catch (NoSuchSampleException e) {
                continue;
            } catch (SampleEmptyException e) {
                if (!contextFilter.filter(s.getSampleNumber(), DeviceContext.EMPTY_SAMPLE, prevSampleIndexes.containsKey(s.getSampleNumber())))
                    continue;
            }
        }
        if (outSic.size() == 0) {
            JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Filter cancelled. It yields no samples.", "No Effect", JOptionPane.ERROR_MESSAGE);
            return sic;
        }
        return outSic;
    }*/

    protected void doRefresh() {
        try {
            //final List sic = filterSampleList(sc.getContextSamples());
            final List sic = sc.getContextSamples();
            final DecimalFormat df = new DecimalFormat("0000");
            int count = 0;
            for (int i = 0, n = sic.size(); i < n; i++) {
                final ReadableSample s = (ReadableSample) sic.get(i);
                if (s.getSampleNumber().intValue() == 0)
                    continue;
                try {
                    if (!contextFilter.filter(s.getSampleNumber(), s.getSampleName(), prevSampleIndexes.containsKey(s.getSampleNumber())))
                        continue;
                } catch (NoSuchSampleException e) {
                    continue;
                } catch (SampleEmptyException e) {
                    if (!contextFilter.filter(s.getSampleNumber(), DeviceContext.EMPTY_SAMPLE, prevSampleIndexes.containsKey(s.getSampleNumber())))
                        continue;
                }
                s.setToStringFormatExtended(false);
                sampleIndexes.put(s.getSampleNumber(), IntPool.get(count++));
                tableRowObjects.add(new ColumnValueProvider() {
                    private ReadableSample sample = s;

                    public Object getValueAt(int col) {
                        if (col == 0)
                            return "S " + df.format(sample.getSampleNumber());
                        else if (col == 1)
                            return sample;
                        return "";
                    }

                    public void zDispose() {
                    }

                    public boolean equals(Object obj) {
                        if (obj instanceof Integer && obj.equals(sample.getSampleNumber()))
                            return true;
                        return false;
                    }
                });
                s.addSampleListener(this);
            }
        } catch (NoSuchContextException e) {
        }
    }

    public int getRowForSample(Integer sample) {
        Integer row = (Integer) sampleIndexes.get(sample);
        if (row != null)
            return row.intValue();
        return -1;
    }

    private void removeSampleListeners() {
        Integer[] samples = new Integer[tableRowObjects.size()];
        for (int i = 0, n = tableRowObjects.size(); i < n; i++)
            samples[i] = ((ReadableSample) ((ColumnValueProvider) tableRowObjects.get(i)).getValueAt(1)).getSampleNumber();
        sc.removeSampleListener(this, samples);
    }

    public void samplesRemovedFromContext(SampleContext pc, Integer[] samples) {
        refresh(false);
    }

    public void samplesAddedToContext(SampleContext pc, Integer[] samples) {
        refresh(false);
    }

    public void contextReleased(SampleContext pc) {
        refresh(false);
    }

    private void updateSample(final Integer sample) {
        Integer index = (Integer) sampleIndexes.get(sample);
        if (index != null)
            this.fireTableCellUpdated(index.intValue(), 1);
    }

    public void sampleInitialized(SampleInitializeEvent ev) {
        updateSample(ev.getSample());
    }

    public void sampleRefreshed(SampleRefreshEvent ev) {
        updateSample(ev.getSample());
    }

    public void sampleChanged(SampleChangeEvent ev) {
    }

    public void sampleNameChanged(SampleNameChangeEvent ev) {
        updateSample(ev.getSample());
    }

    public void sampleInitializationStatusChanged(SampleInitializationStatusChangedEvent ev) {
        updateSample(ev.getSample());
    }
}
