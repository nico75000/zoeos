package com.pcmsolutions.device.EMU.E4.gui.parameter;

import com.pcmsolutions.device.EMU.E4.gui.preset.CheckedSelectionListDialog;
import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.system.ZUtilities;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * User: paulmeehan
 * Date: 22-May-2004
 * Time: 20:09:24
 */
public class ParameterSelectorDialog extends CheckedSelectionListDialog {
    public interface ParameterSet {
        Integer[] getIds();

        boolean isSelected();
    }

    private interface ParameterSetCheckable extends ParameterSet, Checkable {
    };

    public ParameterSet[] getParameterSets() {
        return (ParameterSet[]) getCheckables();
    }

    public Integer[] getSelectedIds() {
        ParameterSet[] psets = getParameterSets();
        if (psets != null) {
            Integer[] ids = new Integer[0];
            for (int i = 0; i < psets.length; i++)
                if (psets[i].isSelected())
                    ids = ZUtilities.joinIntegerArrays(ids, psets[i].getIds());
            return ids;
        }
        return null;
    }

    public ParameterSelectorDialog(Frame owner, String title, GeneralParameterDescriptor[] pds, ParameterUtilities.ParameterGroup[] groups, Integer[] ignoreSet, Integer[] initiallySelected) throws HeadlessException {
        super(owner);
        this.setTitle(title);
        this.setCheckables(makeParameterSetCheckables(pds, groups, ignoreSet, initiallySelected));
    }

    static ParameterUtilities.ParameterGroup getGroupForId(ParameterUtilities.ParameterGroup[] groups, Integer id) {
        for (int i = 0; i < groups.length; i++)
            if (groups[i].containsId(id))
                return groups[i];
        return null;
    }

    static ParameterSetCheckable[] makeParameterSetCheckables(final GeneralParameterDescriptor[] pds, ParameterUtilities.ParameterGroup[] groups, Integer[] ignoreSet, Integer[] initiallySelected) {
        final List il = Arrays.asList(ignoreSet);
        final List is = Arrays.asList(initiallySelected);
        final ArrayList checkables = new ArrayList();
        final Map group2PSC = new HashMap();
        for (int i = 0; i < pds.length; i++) {
            final int f_i = i;
            if (il.contains(pds[i].getId()))
                continue;
            final ParameterUtilities.ParameterGroup pg = getGroupForId(groups, pds[i].getId());
            if (pg != null) {
                if (group2PSC.containsKey(pg))
                    continue;
                else {
                    ParameterSetCheckable psc = new ParameterSetCheckable() {
                        boolean checked = is.contains(pds[f_i].getId());

                        public Integer[] getIds() {
                            return pg.getIds();
                        }

                        public boolean isSelected() {
                            return isChecked();
                        }

                        public void setChecked(boolean s) {
                            checked = s;
                        }

                        public boolean isChecked() {
                            return checked;
                        }

                        public String toString() {
                            return pg.toString();
                        }
                    };
                    checkables.add(psc);
                    group2PSC.put(pg, psc);
                }
            } else
                checkables.add(new ParameterSetCheckable() {
                    boolean checked = is.contains(pds[f_i].getId());

                    public Integer[] getIds() {
                        return new Integer[]{pds[f_i].getId()};
                    }

                    public boolean isSelected() {
                        return isChecked();
                    }

                    public void setChecked(boolean s) {
                        checked = s;
                    }

                    public boolean isChecked() {
                        return checked;
                    }

                    public String toString() {
                        return pds[f_i].toString();
                    }
                });
        }
        return (ParameterSetCheckable[]) checkables.toArray(new ParameterSetCheckable[checkables.size()]);
    };
}
