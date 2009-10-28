package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.DeviceContext;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 18-Aug-2003
 * Time: 03:29:06
 * To change this template use Options | File Templates.
 */
public class LinkParameterSelectionCollection extends AbstractE4Selection {
    protected LinkParameterSelection[] selections;
    protected int category;

    public LinkParameterSelectionCollection(DeviceContext d, LinkParameterSelection[] selections, int category) {
        super(d);
        if (selections.length < 1 || selections[0] == null)
            throw new IllegalArgumentException("LinkParameterSelectionCollection needs at leas one valid element");
        this.selections = selections;
        this.category = category;
    }

    public Integer[] getIds() {
        return selections[0].getIds();
    }

    public int getCategory() {
        return category;
    }

    public int getCount() {
        return selections.length;
    }

    public LinkParameterSelection[] getSelections() {
        return (LinkParameterSelection[]) selections.clone();
    }
}
