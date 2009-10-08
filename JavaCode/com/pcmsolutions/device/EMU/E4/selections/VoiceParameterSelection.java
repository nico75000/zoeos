package com.pcmsolutions.device.EMU.E4.selections;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.parameter.ParameterUtilities;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import java.util.HashSet;
import java.util.Set;

public class VoiceParameterSelection extends AbstractE4Selection {
    protected Integer[] ids;
    protected Integer[] vals;
    protected int category;
    protected ReadablePreset.ReadableVoice voice;

    public static final int VOICE_GENERAL = VOICE_BASE + 0;
    public static final int VOICE_AMPLIFIER = VOICE_BASE + 1;
    public static final int VOICE_AMPLIFIER_ENVELOPE = VOICE_BASE + 2;
    public static final int VOICE_AUX_ENVELOPE = VOICE_BASE + 3;
    public static final int VOICE_CORDS = VOICE_BASE + 4;
    public static final int VOICE_FILTER = VOICE_BASE + 5;
    public static final int VOICE_FILTER_ENVELOPE = VOICE_BASE + 6;
    public static final int VOICE_LFO1 = VOICE_BASE + 7;
    public static final int VOICE_LFO2 = VOICE_BASE + 8;
    public static final int VOICE_TUNING = VOICE_BASE + 9;
    public static final int VOICE_TUNING_MODIFIERS = VOICE_BASE + 10;
    public static final int VOICE_TUNING_SETUP = VOICE_BASE + 11;

    public static final int VOICE_KEY_WIN = VOICE_BASE + 12;
    public static final int VOICE_VEL_WIN = VOICE_BASE + 13;
    public static final int VOICE_RT_WIN = VOICE_BASE + 14;
    public static final int VOICE_MAIN = VOICE_BASE + 15;

    public static final int VOICE_KEY_AND_VEL_WIN = VOICE_BASE + 16;
    public static final int VOICE_MAIN_SAMPLE_ZONE_COMMON = VOICE_BASE + 17;

    public static int voiceCategoryStringToEnum(String c) {
        if (c.equals(ParameterCategories.VOICE_AMPLIFIER))
            return VOICE_AMPLIFIER;
        if (c.equals(ParameterCategories.VOICE_AMPLIFIER_ENVELOPE))
            return VOICE_AMPLIFIER_ENVELOPE;
        if (c.equals(ParameterCategories.VOICE_AUX_ENVELOPE))
            return VOICE_AUX_ENVELOPE;
        if (c.equals(ParameterCategories.VOICE_CORDS))
            return VOICE_CORDS;
        if (c.equals(ParameterCategories.VOICE_FILTER))
            return VOICE_FILTER;
        if (c.equals(ParameterCategories.VOICE_FILTER_ENVELOPE))
            return VOICE_FILTER_ENVELOPE;
        if (c.equals(ParameterCategories.VOICE_LFO1))
            return VOICE_LFO1;
        if (c.equals(ParameterCategories.VOICE_LFO2))
            return VOICE_LFO2;
        if (c.equals(ParameterCategories.VOICE_TUNING))
            return VOICE_TUNING;
        if (c.equals(ParameterCategories.VOICE_TUNING_MODIFIERS))
            return VOICE_TUNING_MODIFIERS;
        if (c.equals(ParameterCategories.VOICE_TUNING_SETUP))
            return VOICE_TUNING_SETUP;
        if (c.equals(ParameterCategories.VOICE_MAIN))
            return VOICE_MAIN;
        if (c.equals(ParameterCategories.VOICE_KEY_WIN))
            return VOICE_KEY_WIN;
        if (c.equals(ParameterCategories.VOICE_VEL_WIN))
            return VOICE_VEL_WIN;
        if (c.equals(ParameterCategories.VOICE_RT_WIN))
            return VOICE_RT_WIN;
        return VOICE_GENERAL;
    }

    public static String voiceCategoryEnumToString(int c) {
        if (c == VOICE_AMPLIFIER)
            return ParameterCategories.VOICE_AMPLIFIER;
        if (c == VOICE_AMPLIFIER_ENVELOPE)
            return ParameterCategories.VOICE_AMPLIFIER_ENVELOPE;
        if (c == VOICE_AUX_ENVELOPE)
            return ParameterCategories.VOICE_AUX_ENVELOPE;
        if (c == VOICE_CORDS)
            return ParameterCategories.VOICE_CORDS;
        if (c == VOICE_FILTER)
            return ParameterCategories.VOICE_FILTER;
        if (c == VOICE_FILTER_ENVELOPE)
            return ParameterCategories.VOICE_FILTER_ENVELOPE;
        if (c == VOICE_LFO1)
            return ParameterCategories.VOICE_LFO1;
        if (c == VOICE_LFO2)
            return ParameterCategories.VOICE_LFO2;
        if (c == VOICE_TUNING)
            return ParameterCategories.VOICE_TUNING;
        if (c == VOICE_TUNING_MODIFIERS)
            return ParameterCategories.VOICE_TUNING_MODIFIERS;
        if (c == VOICE_TUNING_SETUP)
            return ParameterCategories.VOICE_TUNING_SETUP;
        if (c == VOICE_KEY_WIN)
            return ParameterCategories.VOICE_KEY_WIN;
        if (c == VOICE_VEL_WIN)
            return ParameterCategories.VOICE_VEL_WIN;
        if (c == VOICE_RT_WIN)
            return ParameterCategories.VOICE_RT_WIN;
        if (c == VOICE_MAIN)
            return ParameterCategories.VOICE_MAIN;
        return "General";
    }

    public VoiceParameterSelection(ReadablePreset.ReadableVoice voice, Integer[] ids, Integer[] vals) throws ZDeviceNotRunningException, IllegalParameterIdException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException {
        this(voice, ids, vals, determineVoiceParameterSelectionCategory(voice.getPreset().getDeviceContext().getDeviceParameterContext(), ids));
    }

    public VoiceParameterSelection(ReadablePreset.ReadableVoice voice, Integer[] ids, Integer[] vals, int category) throws ZDeviceNotRunningException, IllegalParameterIdException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException {
        super(voice.getPreset().getDeviceContext());
        //this.ids = new Integer[ids.length];
        //this.vals = new Integer[ids.length];
        this.voice = voice;
        this.category = category;
        this.ids = (Integer[]) ids.clone();
        this.vals = (Integer[]) vals.clone();
    }

    public VoiceParameterSelection(ReadablePreset.ReadableVoice voice, Integer[] ids, int category) throws ZDeviceNotRunningException, IllegalParameterIdException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException {
        this(voice, ids, voice.getVoiceParams(ids), category);
    }

    public VoiceParameterSelection(ReadablePreset.ReadableVoice voice, Integer[] ids) throws ZDeviceNotRunningException, IllegalParameterIdException, PresetEmptyException, NoSuchPresetException, NoSuchVoiceException {
        this(voice, ids, voice.getVoiceParams(ids), determineVoiceParameterSelectionCategory(voice.getPreset().getDeviceContext().getDeviceParameterContext(), ids));
    }

    public static int determineVoiceParameterSelectionCategory(DeviceParameterContext dpc, Integer[] ids) {
        Set cats = new HashSet();
        for (int i = 0,j = ids.length; i < j; i++) {
            try {
                cats.add(IntPool.get(voiceCategoryStringToEnum(dpc.getParameterDescriptor(ids[i]).getCategory())));
            } catch (IllegalParameterIdException e) {
                e.printStackTrace();
            }
        }
        Integer[] cats2 = (Integer[]) cats.toArray(new Integer[cats.size()]);
        if (cats2.length == 1)
            return cats2[0].intValue();
        return VOICE_GENERAL;
    }

    public VoiceParameterSelection(VoiceParameterSelection vps) {
        super(vps.getSrcDevice());
        ids = (Integer[]) vps.ids.clone();
        vals = (Integer[]) vps.vals.clone();
        voice = vps.voice;
        category = vps.category;
    }

    public int getCategory() {
        return category;
    }

    public boolean containsOnlySampleZoneIds() throws ZDeviceNotRunningException {
        return ParameterUtilities.containsOnlySampleZoneIds(this);
    }

    public ReadablePreset.ReadableVoice getVoice() {
        return voice;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void render(ContextEditablePreset.EditableVoice[] voices) {
      /*  for (int v = 0,x = voices.length; v < x; v++)
            for (int i = 0, n = ids.length; i < n; i++) {
                try {
                    voices[v].setVoicesParam(ids[i], vals[i]);
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                } catch (ParameterValueOutOfRangeException e) {
                    e.printStackTrace();
                } catch (NoSuchPresetException e) {
                    e.printStackTrace();
                } catch (PresetEmptyException e) {
                    e.printStackTrace();
                } catch (NoSuchVoiceException e) {
                    e.printStackTrace();
                }
            }*/
        try {
            PresetContextMacros.setContextVoicesParam(voices,ids,vals );
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        } catch (NoSuchContextException e) {
            e.printStackTrace();
        }
    }

    public void render(ContextEditablePreset.EditableVoice.EditableZone[] zones) {
        int iv;
        for (int z = 0,x = zones.length; z < x; z++)
            for (int i = 0, n = ids.length; i < n; i++) {
                iv = ids[i].intValue();
                if (((iv >= 38 && iv <= 40) || iv == 42 || (iv >= 44 && iv <= 52)))
                    try {
                        zones[z].setZonesParam(ids[i], vals[i]);
                    } catch (IllegalParameterIdException e) {
                        e.printStackTrace();
                    } catch (ParameterValueOutOfRangeException e) {
                        e.printStackTrace();
                    } catch (NoSuchPresetException e) {
                        e.printStackTrace();
                    } catch (PresetEmptyException e) {
                        e.printStackTrace();
                    } catch (NoSuchVoiceException e) {
                        e.printStackTrace();
                    } catch (NoSuchZoneException e) {
                        e.printStackTrace();
                    }
            }
    }

    public boolean containsOnlyKeyAndVelWinIds() {
        return ParameterUtilities.containsOnlyVoiceKeyAndVelWinIds(ids);
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
