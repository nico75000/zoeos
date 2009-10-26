package com.pcmsolutions.device.EMU.E4.gui.parameter;

import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTableModel;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.system.IntPool;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Jul-2003
 * Time: 23:24:23
 * To change this template use Options | File Templates.
 */
public abstract class AbstractParameterModelTableModel extends AbstractRowHeaderedAndSectionedTableModel implements ChangeListener {
    protected ReadableParameterModel[] parameterModels;
    protected HashMap pmIndexes = new HashMap();

    public AbstractParameterModelTableModel(ReadableParameterModel[] parameterModels) {
        this.parameterModels = parameterModels;
        for (int i = 0, j = parameterModels.length; i < j; i++) {
            parameterModels[i].addChangeListener(this);
            pmIndexes.put(parameterModels[i], IntPool.get(i));
        }
        init();
    }

    public ReadableParameterModel[] getParameterModels() {
        return (ReadableParameterModel[]) parameterModels.clone();
    }

    // -1 for not present, otherwise row index
    public int rowIndexOfId(Integer id) {
        for (int i = 0,j = parameterModels.length; i < j; i++)
            if (parameterModels[i].getParameterDescriptor().getId().equals(id))
                return i;
        return -1;
    }

    public Integer getIdForRow(int row) {
        if (row >= 0 && row < parameterModels.length)
            return parameterModels[row].getParameterDescriptor().getId();
        return null;
    }

    public boolean containsAllIds(Integer[] ids) {
        List checkIds = Arrays.asList(ids);

        for (int i = 0,j = parameterModels.length; i < j; i++)
            checkIds.remove(parameterModels[i].getParameterDescriptor().getId());

        if (checkIds.size() == 0)
            return true;

        return false;
    }

    protected final int getIndexOfParameterModel(Object pm) {
        Integer i = (Integer) pmIndexes.get(pm);
        if (i != null)
            return i.intValue();
        return -1;
    }

    protected abstract void updateParameterModelAtIndex(int pmIndex);

    public void zDispose() {
        super.zDispose();
        for (int i = 0, j = parameterModels.length; i < j; i++) {
            parameterModels[i].zDispose();
            //parameterModels[i].removeChangeListener(this);
        }
        parameterModels = null;
        pmIndexes.clear();
        pmIndexes = null;
    }

    public void stateChanged(ChangeEvent e) {
        int i = getIndexOfParameterModel(e.getSource());
        if (i != -1)
            updateParameterModelAtIndex(i);
    }
}
