package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTableModel;
import com.pcmsolutions.device.EMU.database.Context;
import com.pcmsolutions.device.EMU.database.ContextElement;
import com.pcmsolutions.device.EMU.database.ContextListener;
import com.pcmsolutions.device.EMU.database.events.context.ContextAdditionEvent;
import com.pcmsolutions.device.EMU.database.events.context.ContextReleaseEvent;
import com.pcmsolutions.device.EMU.database.events.context.ContextRemovalEvent;
import com.pcmsolutions.system.IntPool;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 20-Jul-2003
 * Time: 19:39:39
 * To change this template use Options | File Templates.
 */
public abstract class AbstractContextTableModel <C extends Context, CI extends ContextElement> extends AbstractRowHeaderedAndSectionedTableModel implements ContextListener {
    public final static ContextFilter allPassFilter = new ContextFilter() {
        public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
            return true;
        }

        public String getFilterName() {
            return "Allpass";
        }
    };
    protected ContextFilter contextFilter = allPassFilter;
    private C context;
    final protected Map<Integer, Integer> indexes = new HashMap<Integer, Integer>();
    final protected Set<Integer> prevIndexes = new HashSet<Integer>();

    protected AbstractContextTableModel() {
    }

    protected void init(C context) {
        this.context = context;
        context.addContextListener(this);
        super.init();
    }

    public C getContext() {
        return context;
    }

    public interface ContextFilter {
        public boolean filter(Integer index, String name, boolean wasFilteredPreviously);

        public String getFilterName();
    }

    public static ContextFilter getAggegrateContextFilter(final ContextFilter filt1, final ContextFilter filt2) {
        return new ContextFilter() {
            public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                return filt1.filter(index, name, wasFilteredPreviously) || filt2.filter(index, name, wasFilteredPreviously);
            }

            public String getFilterName() {
                return filt1.getFilterName() + " + " + filt2.getFilterName();
            }
        };
    }

    abstract protected boolean acceptElement(CI elem);

    protected final void doRefresh() {
        prevIndexes.clear();
        prevIndexes.addAll(indexes.keySet());
        indexes.clear();
        try {
            final List eic = getContext().getContextElements();
            final DecimalFormat df = new DecimalFormat("0000");
            int count = 0;
            for (int i = 0, n = eic.size(); i < n; i++) {
                final CI elem = (CI) eic.get(i);
                if (!acceptElement(elem))
                    continue;
                try {
                    if (!contextFilter.filter(elem.getIndex(), elem.getString(), prevIndexes.contains(elem.getIndex())))
                        continue;
                } catch (Exception e) {
                    continue;
                } /*catch (EmptyException e) {
                    if (!contextFilter.filter(elem.getPresetNumber(), DeviceContext.EMPTY_PRESET, prevPresetIndexes.containsKey(elem.getPresetNumber())))
                        continue;
                } */
                elem.setToStringFormatExtended(false);
                indexes.put(elem.getIndex(), IntPool.get(count++));
                tableRowObjects.add(new ColumnValueProvider() {
                    private ContextElement element = elem;

                    public Object getValueAt(int col) {
                        if (col == 0)
                            return getContextPrefix() + df.format(element.getIndex());
                        else if (col == 1)
                            return element;
                        return "";
                    }

                    public void zDispose() {
                        element = null;
                    }

                    public boolean equals(Object obj) {
                        if (obj instanceof Integer && obj.equals(element.getIndex()))
                            return true;
                        return false;
                    }
                });
                finalizeRefreshedElement(elem);
            }
        } catch (DeviceException e) {
        }
    }

    abstract protected String getContextPrefix();

    abstract protected void finalizeRefreshedElement(CI element) throws DeviceException;

    public int getRowForIndex(Integer index) {
        Integer row = indexes.get(index);
        if (row != null)
            return row.intValue();
        return -1;
    }

    protected void updateIndex(final Integer index) {
        Integer row = indexes.get(index);
        if (row != null)
            this.fireTableCellUpdated(row.intValue(), 1);
    }

    public void addIndexesToCurrentContextFilter(Integer[] indexes, final String reason) {
        final List<Integer> indexList = Arrays.asList(indexes);
        this.setContextFilter(getAggegrateContextFilter(getContextFilter(), new ContextFilter() {
            public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                return indexList.contains(index);
            }

            public String getFilterName() {
                return getContextFilter().getFilterName() + " + " + reason;
            }
        }));
    }

    public final void setContextFilter(ContextFilter cf) {
        if (cf == null)
            this.contextFilter = allPassFilter;
        else
            this.contextFilter = cf;
        refresh(false);
    }

    public ContextFilter getContextFilter() {
        return contextFilter;
    }

    public void removalFromContext(ContextRemovalEvent ev) {
        refresh(false);
    }

    public void additionToContext(ContextAdditionEvent ev) {
        refresh(false);
    }

    public void contextReleased(ContextReleaseEvent ev) {
        context.removeContextListener(this);
        refresh(false);
    }
}
