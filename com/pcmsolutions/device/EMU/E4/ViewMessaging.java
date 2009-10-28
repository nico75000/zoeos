package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.IntPool;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * User: paulmeehan
 * Date: 17-May-2004
 * Time: 11:40:56
 */
public class ViewMessaging {
    public static final String MSG_BROADCAST_CLOSE_EMPTY = "close_broadcast_empty";
    public static final String MSG_CLOSE_PRESET_EMPTY = "close_preset_empty";
    public static final String MSG_CLOSE_PRESET_USER = "close_preset_user";
    public static final String MSG_CLOSE_PRESET_FLASH = "close_preset_flash";
    public static final String MSG_CLOSE_VOICE_EMPTY = "close_voice_empty";
    public static final String MSG_CLOSE_VOICE = "voice_close";

    public static final String CONDITION_IS_OPEN_PRESET = "is_open_preset";

    public static final String MSG_PRESET_CONTEXT_PREFIX = "preset_context";
    public static final String MSG_ADD_PRESETS_TO_PRESET_CONTEXT_FILTER = MSG_PRESET_CONTEXT_PREFIX + "add_presets_to_filter";
    public static final String MSG_SELECT_OPEN_PRESETS_IN_PRESET_CONTEXT = MSG_PRESET_CONTEXT_PREFIX + "select_open_presets";

    public static final String MSG_SAMPLE_CONTEXT_PREFIX = "sample_context";
    public static final String MSG_ADD_SAMPLES_TO_SAMPLE_CONTEXT_FILTER = MSG_SAMPLE_CONTEXT_PREFIX + "add_samples_to_filter";

    public static final String MSG_FIELD_SEPARATOR = ";";

    public static String applyFieldsToMessage(String msg, Object[] fields) {
        StringBuffer s = new StringBuffer(msg);
        for (int i = 0; i < fields.length; i++) {
            s.append(MSG_FIELD_SEPARATOR);
            s.append(fields[i]);
        }
        return s.toString();
    }

    public static String[] extractFieldsFromMessage(String msg) {
        StringTokenizer t = new StringTokenizer(msg, MSG_FIELD_SEPARATOR);
        ArrayList fields = new ArrayList();
        int f = 0;
        while (t.hasMoreTokens()) {
            if (f++ == 0)
                t.nextToken();
            else
                fields.add(t.nextToken());
        }
        return (String[]) fields.toArray(new String[fields.size()]);
    }

    public static Integer[] extractIntegersFromMessage(String msg) {
        String[] intFields = ViewMessaging.extractFieldsFromMessage(msg);
        Integer[] presets = new Integer[intFields.length];
        try {
            for (int i = 0; i < intFields.length; i++)
                presets[i] = IntPool.get(Integer.parseInt(intFields[i]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return presets;
    }
}
