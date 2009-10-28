package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.IntPool;

import java.util.ArrayList;
import java.util.Map;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 23:51:34
 */
class Impl_DynamicGeneralParameterDescriptor extends AbstractParameterDescriptor {
    protected Map<String, Integer> value4StrMap;
    protected String[] valueStrings;
    protected String[] valueTips;

    public Impl_DynamicGeneralParameterDescriptor() {
    }

    public void init(Integer id, MinMaxDefault mmd, int loc) {
        MinMaxDefault defMMD = ParameterTables.id2mmd[id.intValue()];
        if (!ParameterTables.compatibleMMDs(mmd, defMMD)) {
            ArrayList<String> valStrs = new ArrayList<String>();
            ArrayList<String> tipStrs = new ArrayList<String>();

            if (ParameterTables.unspecifiedMMD(defMMD)) {
                for (int i = mmd.getMin().intValue(), j = mmd.getMax().intValue(); i <= j; i++)
                    valStrs.add(Integer.toString(i));
                valueStrings = valStrs.toArray(new String[valStrs.size()]);
                value4StrMap = ParameterTables.createValueForStringMap();
                for (int i = 0, j = valueStrings.length; i < j; i++)
                    value4StrMap.put(valueStrings[i], IntPool.get(mmd.getMin().intValue() + i));

                for (int i = mmd.getMin().intValue(), j = mmd.getMax().intValue(); i <= j; i++)
                    tipStrs.add(Integer.toString(i));
                valueTips = valStrs.toArray(new String[tipStrs.size()]);

            } else {
                // fill out below
                int below = defMMD.getMin().intValue() - mmd.getMin().intValue();
                if (below > 0) {
                    for (int i = 0; i < below; i++)
                        valStrs.add(Integer.toString(mmd.getMin().intValue() + i));
                }
                // fill out common
                int clow = (defMMD.getMin().intValue() > mmd.getMin().intValue() ? defMMD.getMin().intValue() : mmd.getMin().intValue());
                int chigh = (defMMD.getMax().intValue() < mmd.getMax().intValue() ? defMMD.getMax().intValue() : mmd.getMax().intValue());
                for (int i = clow; i <= chigh; i++)
                    valStrs.add(ParameterTables.id2valueStrings[id.intValue()][i - defMMD.getMin().intValue()]);

                // fill out above
                int above = mmd.getMax().intValue() - defMMD.getMax().intValue();
                if (above > 0) {
                    for (int i = 0; i < below; i++)
                        valStrs.add(Integer.toString(mmd.getMin().intValue() + i));
                }

                valueStrings = valStrs.toArray(new String[valStrs.size()]);

                value4StrMap = ParameterTables.createValueForStringMap();
                for (int i = 0, j = valueStrings.length; i < j; i++)
                    value4StrMap.put(valueStrings[i], IntPool.get(mmd.getMin().intValue() + i));

                // fill out below
                if (below > 0) {
                    for (int i = 0; i < below; i++)
                        tipStrs.add(Integer.toString(mmd.getMin().intValue() + i));
                }
                // fill out common
                for (int i = clow; i <= chigh; i++)
                    tipStrs.add(ParameterTables.id2valueStrings[id.intValue()][i - defMMD.getMin().intValue()]);

                // fill out above
                if (above > 0) {
                    for (int i = 0; i < below; i++)
                        tipStrs.add(Integer.toString(mmd.getMin().intValue() + i));
                }

                valueTips = valStrs.toArray(new String[tipStrs.size()]);
            }
        }
        super.init(id, mmd, loc);
    }

    protected String[] getValueStrings() {
        return valueStrings;
    }

    protected String[] getValueTips() {
        return valueTips;
    }

    protected Map<String, Integer> getString2ValueMap() {
        return value4StrMap;
    }
}
