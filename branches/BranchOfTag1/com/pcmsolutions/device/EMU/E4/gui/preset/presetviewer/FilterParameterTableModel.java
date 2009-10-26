package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.parameter.FilterParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterUnavailableException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.system.IntPool;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Jul-2003
 * Time: 21:09:51
 * To change this template use Options | File Templates.
 */
public class FilterParameterTableModel extends VoiceParameterTableModel implements ChangeListener {
    protected ReadableParameterModel filterTypeModel;
    //public static final String INACTIVE_FILTER_FIELD = ZUtilities.makeExactLengthString(".", 50, '.', true);
    public static final String INACTIVE_FILTER_FIELD = " ";

    public FilterParameterTableModel(ReadableParameterModel[] parameterModels) {
        super(parameterModels);
    }

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        rowHeaderColumnData.width += 30;
        columnData[0].width += 50;
        sectionData[0].sectionWidth = columnData[0].width;
    }

    public void stateChanged(ChangeEvent e) {
        super.stateChanged(e);
        if (((ReadableParameterModel) e.getSource()).getParameterDescriptor() == filterTypeModel.getParameterDescriptor()) {
            //refresh(false);
            this.fireTableRowsUpdated(0, getRowCount() - 1);
        }
    }

    public void zDispose() {
        super.zDispose();
        if (filterTypeModel != null)
            filterTypeModel.removeChangeListener(this);
    }

    protected void doRefresh() {
        if (filterTypeModel != null)
            filterTypeModel.removeChangeListener(this);
        filterTypeModel = null;

        for (int i = 0,j = parameterModels.length; i < j; i++)
            if (parameterModels[i].getParameterDescriptor().getId().equals(IntPool.get(82))) {
                filterTypeModel = parameterModels[i];
                filterTypeModel.addChangeListener(this); // do we need this anymore, now that filter ParameterModels also listen for filter changes??
                break;
            }
        if (filterTypeModel == null)
            throw new IllegalArgumentException("No ParameterModel for filter type present");

        for (int i = 0, n = parameterModels.length; i < n; i++) {
            if (parameterModels[i].getParameterDescriptor() instanceof FilterParameterDescriptor) {
                FilterParameterDescriptor fpd = (FilterParameterDescriptor) parameterModels[i].getParameterDescriptor();
                try {
                    fpd.setFilterType(filterTypeModel.getValue());
                    //if (!fpd.isCurrentlyActive())
                    //  continue;
                } catch (ParameterUnavailableException e) {
                    //  e.printStackTrace();
                }
            }
            final int j = i;
            parameterModels[j].setShowUnits(true);
            tableRowObjects.add(new ColumnValueProvider() {
                private ReadableParameterModel pm = parameterModels[j];

                public Object getValueAt(int col) {
                    if (pm.getParameterDescriptor() instanceof FilterParameterDescriptor && !((FilterParameterDescriptor) pm.getParameterDescriptor()).isCurrentlyActive())
                        return INACTIVE_FILTER_FIELD;

                    if (col == 0)
                        return pm.getParameterDescriptor().getPresentationString();
                    else if (col == 1)
                        return pm;
                    return "";
                }

                public boolean equals(Object o) {
                    return pm.getParameterDescriptor().getId().equals(o);
                }

                public void zDispose() {
                    pm = null;
                }
            });
        }
    }
}
