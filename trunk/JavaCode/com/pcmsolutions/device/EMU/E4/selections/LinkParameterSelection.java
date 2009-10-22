package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.EmptyException;

public class LinkParameterSelection extends AbstractE4Selection {
    private Integer[] ids;
    private Integer[] vals;
    private int category;

    public static final int LINK_GENERAL = LINK_BASE + 0;
    public static final int LINK_MAIN = LINK_BASE + 1;
    public static final int LINK_KEYWIN = LINK_BASE + 2;
    public static final int LINK_VELWIN = LINK_BASE + 3;
    public static final int LINK_MIDIFILTERS = LINK_BASE + 4;

    public static int convertLinkCategoryString(String c) {
        if (c.equals(ParameterCategories.LINK_MAIN))
            return LINK_MAIN;

        if (c.equals(ParameterCategories.LINK_KEYWIN))
            return LINK_KEYWIN;

        if (c.equals(ParameterCategories.LINK_VELWIN))
            return LINK_VELWIN;

        if (c.equals(ParameterCategories.LINK_MIDIFILTERS))
            return LINK_MIDIFILTERS;

        return LINK_GENERAL;

    }

    public LinkParameterSelection(ReadablePreset.ReadableLink l, Integer[] ids, int category) throws ParameterException, PresetException, EmptyException {
        super(l.getPreset().getDeviceContext());
        this.ids = new Integer[ids.length];
        this.vals = new Integer[ids.length];
        this.category = category;
        this.ids = (Integer[]) ids.clone();
        vals = l.getLinkParams(ids);
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public boolean containsOnlyKeyAndVelWinIds() {
        return ParameterUtilities.containsOnlyLinkKeyAndVelWinIds(ids);
    }

    public void render(ContextEditablePreset.EditableLink l) {
        for (int i = 0, n = ids.length; i < n; i++) {
            try {
                l.setLinkParam(ids[i], vals[i]);
            }catch (PresetException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer[] getIds() {
        return (Integer[]) ids.clone();
    }

    public Integer[] getVals() {
        return (Integer[]) vals.clone();
    }

    public boolean containsId(Integer id) {
        for (int i = 0,j = ids.length; i < j; i++)
            if (ids[i].equals(id))
                return true;
        return false;
    }
}
