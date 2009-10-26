package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTableModel;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 20-Jul-2003
 * Time: 19:39:39
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextTableModel extends AbstractRowHeaderedAndSectionedTableModel {
    public final static ContextFilter allPassFilter = new ContextFilter() {
        public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
            return true;
        }

        public String getFilterName() {
            return "All";
        }
    };

    protected ContextFilter contextFilter = allPassFilter;

    public interface ContextFilter {
        public boolean filter(Integer index, String name, boolean wasFilteredPreviously);

        public String getFilterName();
    }


    public void setContextFilter(ContextFilter cf) {
        if (cf == null)
            this.contextFilter = allPassFilter;
        else
            this.contextFilter = cf;
    }

    public ContextFilter getContextFilter() {
        return contextFilter;
    }
}
