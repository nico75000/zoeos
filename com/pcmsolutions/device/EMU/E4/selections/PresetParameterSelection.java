package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.ZDeviceNotRunningException;

public class PresetParameterSelection extends AbstractE4Selection {
    private Integer[] ids;
    private Integer[] vals;
    private int category;

    public static final int PRESET_FX_A = PRESET_BASE + 0;
    public static final int PRESET_FX_B = PRESET_BASE + 1;
    public static final int PRESET_GLOBAL = PRESET_BASE + 2;
    public static final int PRESET_GENERAL = PRESET_BASE + 3;

    public static int convertPresetCategoryString(String c) {
        if (c.equals(ParameterCategories.PRESET_FX_A))
            return PRESET_FX_A;
        if (c.equals(ParameterCategories.PRESET_FX_B))
            return PRESET_FX_B;
        if (c.equals(ParameterCategories.PRESET_GLOBALS))
            return PRESET_GLOBAL;
        return PRESET_GENERAL;
    }

    public PresetParameterSelection(ReadablePreset p, Integer[] ids, int category) throws ZDeviceNotRunningException, IllegalParameterIdException, PresetEmptyException, NoSuchPresetException {
        super(p.getDeviceContext());
        this.ids = new Integer[ids.length];
        this.vals = new Integer[ids.length];
        this.category = category;
        this.ids = (Integer[]) ids.clone();
        vals = p.getPresetParams(ids);
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void render(ContextEditablePreset p) {
        try {
            p.setPresetParams(ids, vals);
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        } catch (PresetEmptyException e) {
            e.printStackTrace();
        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        } catch (ParameterValueOutOfRangeException e) {
            e.printStackTrace();
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
