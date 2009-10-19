/*
 * ParameterTables.java
 *
 * Created on February 18, 2003, 5:21 PM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.NoteUtilities;

import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * @author  pmeehan
 */
class ParameterTables implements ParameterCategories {

    private static final int ID_LOAD = 250;
    public static final String COLLABORATION_VENV = "AmpEnv";
    public static final String COLLABORATION_FENV = "FilterEnv";
    public static final String COLLABORATION_AENV = "AuxEnv";
    private static final String CATEGORY_GENERAL = "General";
    private static final String CATEGORY_UNRECOGNIZED_ID = "Unrecognized Ids";

    private static final String PREFIX_RAW_VALUE = "(Raw)";
    private static final String PREFIX_RAW_ID = "Id:";

    protected static final Map defStringForValueMap;
    protected static final Map defValueForStringMap;

    private static final Map noteValueStrings;

    static {
        noteValueStrings = NoteUtilities.getNoteValueStringsMap();
    }

    // all id tables
    protected static final LinkedHashMap cs2id = new LinkedHashMap();  // category(string)->ids (ArrayList), use LinkedHashMap guarantees insertion order
    protected static final HashMap id2cs = new HashMap(ID_LOAD);       // id -> category(string)
    protected static final HashMap co2id = new HashMap(ID_LOAD);       // collaboration(string)->ids (ArrayList)
    protected static final HashMap id2co = new HashMap(ID_LOAD);       // id -> collaboration(string)
    protected static final HashMap id2rs = new HashMap(ID_LOAD);       // id -> reference string
    protected static final HashMap rs2id = new HashMap(ID_LOAD);       // reference string -> id
    protected static final HashMap id2mmd = new HashMap(ID_LOAD);      // id -> MinMaxDefault

    // general id tables
    protected static final HashMap id2ps = new HashMap(ID_LOAD);       // id -> presentation string
    protected static final HashMap id2us = new HashMap(ID_LOAD);       // id -> units string
    protected static final HashMap id2v2ps = new HashMap(ID_LOAD);     // id -> (TreeMap)value -> presentation string
    protected static final HashMap id2v2ts = new HashMap(ID_LOAD);     // id -> (HashMap)value -> tip string
    protected static final HashMap id2v2i = new HashMap(ID_LOAD);      // id -> (HashMap)value -> image
    protected static final HashMap id2useSpinner = new HashMap(ID_LOAD);      // id -> Boolean

    // filter (sub)id tables
    protected static final int numFilterTypes = 21;
    // presentation strings for Filter subIds
    //format: filter type(Map) -> filter sub id(Map) -> value presentation string(String)
    protected static final HashMap filterParam_ps = new HashMap();
    // units for Filter subIds
    // format: filter type(Map) -> filter sub id(Map) -> value units presentation string(String)
    protected static final HashMap filterParam_u = new HashMap();
    // Filter value presentation strings
    // format: filter type(Map) -> filter sub id(Map) -> value presentation string(String[])
    protected static final HashMap filterValue_ps = new HashMap();
    //Filter Table 1:    fil_freq (input, 20000, 1002);     /* input=0..255 */
    protected static final String[] filterValueTable1 = new String[256];
    //Filter Table 2:  fil_freq (input, 18000, 1003);       /* input=0..255 */
    protected static final String[] filterValueTable2 = new String[256];
    //Filter Table 3:    fil_freq (input, 10000, 1006);     /*input=0..255 */
    protected static final String[] filterValueTable3a = new String[256];
    //Filter Table 3:    fil_freq (input, 10000, 1009);     /*input=0..255 */
    protected static final String[] filterValueTable3b = new String[256];
    //Filter Table 4:    cnv_morph_gain (input);            /* input=0..127 */
    protected static final String[] filterValueTable4 = new String[128];
    //Filter Table 5:    cnv_morph_freq (2*input);          /* input=0..127 */
    protected static final String[] filterValueTable5 = new String[128];

    static {
        System.out.println("PARAMETER TABLES LOADING...");
        defStringForValueMap = createStringForValueMap();
        defValueForStringMap = createStringForValueMap();
        Integer iv;
        for (int i = -256, n = 10001; i < n; i++) {
            iv = IntPool.get(i);
            defStringForValueMap.put(iv, iv.toString());
            defValueForStringMap.put(iv.toString(), iv);
        }


        Map v2ps;
        Map v2i;

        buildFilterTables();

        //  E4_PRESET_TRANSPOSE,       id = 0 (00h,00h)      min = -24;  max = +24  (semitones)
        load(0, "preset_xpose", "Transpose", null, null, null, " semitones", PRESET_GLOBALS, null, true, -24, 24, 0);

        //  E4_PRESET_VOLUME,          id = 1 (01h,00h)      min = -96;  max = +10  (dB)
        load(1, "preset_vol", "Volume", null, null, null, " dB", PRESET_GLOBALS, null, true, -96, 10, 0);

        // E4_PRESET_CTRL_A,          id = 2 (02h,00h)      min = -1;   max = 127  (-1 = off)
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "off");
        load(2, "preset_ctrla", "Init Ctrl A", v2ps, null, null, null, PRESET_GLOBALS, null, true, -1, 127, -1);

        // E4_PRESET_CTRL_B,          id = 3 (03h,00h)      min = -1;   max = 127  (-1 = off)
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "off");
        load(3, "preset_ctrlb", "Init Ctrl B", v2ps, null, null, null, PRESET_GLOBALS, null, true, -1, 127, -1);

        // E4_PRESET_CTRL_C,          id = 4 (04h,00h)      min = -1;   max = 127  (-1 = off)
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "off");
        load(4, "preset_ctrlc", "Init Ctrl C", v2ps, null, null, null, PRESET_GLOBALS, null, true, -1, 127, -1);

        // E4_PRESET_CTRL_D,          id = 5 (05h,00h)      min = -1;   max = 127  (-1 = off)
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "off");
        load(5, "preset_ctrld", "Init Ctrl D", v2ps, null, null, null, PRESET_GLOBALS, null, true, -1, 127, -1);

        Map v2ps_fxa = createStringForValueMap();
        v2ps_fxa.put(IntPool.get(0), "Master Effect A");
        v2ps_fxa.put(IntPool.get(1), "Room 1");
        v2ps_fxa.put(IntPool.get(2), "Room 2");
        v2ps_fxa.put(IntPool.get(3), "Room 3");
        v2ps_fxa.put(IntPool.get(4), "Hall 1");
        v2ps_fxa.put(IntPool.get(5), "Hall 2");
        v2ps_fxa.put(IntPool.get(6), "Plate");
        v2ps_fxa.put(IntPool.get(7), "Delay");
        v2ps_fxa.put(IntPool.get(8), "Panning Delay");
        v2ps_fxa.put(IntPool.get(9), "Multitap 1");
        v2ps_fxa.put(IntPool.get(10), "Multitap Pan");
        v2ps_fxa.put(IntPool.get(11), "3 Tap");
        v2ps_fxa.put(IntPool.get(12), "3 Tap Pan");
        v2ps_fxa.put(IntPool.get(13), "Soft Room");
        v2ps_fxa.put(IntPool.get(14), "Warm Room");
        v2ps_fxa.put(IntPool.get(15), "Perfect Room");
        v2ps_fxa.put(IntPool.get(16), "Tiled Room");
        v2ps_fxa.put(IntPool.get(17), "Hard Plate");
        v2ps_fxa.put(IntPool.get(18), "Warm Hall");
        v2ps_fxa.put(IntPool.get(19), "Spacious Hall");
        v2ps_fxa.put(IntPool.get(20), "Bright Hall");
        v2ps_fxa.put(IntPool.get(21), "Bright Hall Pan");
        v2ps_fxa.put(IntPool.get(22), "Bright Plate");
        v2ps_fxa.put(IntPool.get(23), "BBall Court");
        v2ps_fxa.put(IntPool.get(24), "Gymnasium");
        v2ps_fxa.put(IntPool.get(25), "Cavern");
        v2ps_fxa.put(IntPool.get(26), "Concert 9");
        v2ps_fxa.put(IntPool.get(27), "Concert 10 Pan");
        v2ps_fxa.put(IntPool.get(28), "Reverse Gate");
        v2ps_fxa.put(IntPool.get(29), "Gate 2");
        v2ps_fxa.put(IntPool.get(30), "Gate Pan");
        v2ps_fxa.put(IntPool.get(31), "Concert 11");
        v2ps_fxa.put(IntPool.get(32), "MediumConcert");
        v2ps_fxa.put(IntPool.get(33), "Large Concert");
        v2ps_fxa.put(IntPool.get(34), "Lg Concert Pan");
        v2ps_fxa.put(IntPool.get(35), "Canyon");
        v2ps_fxa.put(IntPool.get(36), "DelayVerb 1");
        v2ps_fxa.put(IntPool.get(37), "DelayVerb 2");
        v2ps_fxa.put(IntPool.get(38), "DelayVerb 3");
        v2ps_fxa.put(IntPool.get(39), "DelayVerb4Pan");
        v2ps_fxa.put(IntPool.get(40), "DelayVerb5Pan");
        v2ps_fxa.put(IntPool.get(41), "DelayVerb 6");
        v2ps_fxa.put(IntPool.get(42), "DelayVerb 7");
        v2ps_fxa.put(IntPool.get(43), "DelayVerb 8");
        v2ps_fxa.put(IntPool.get(44), "DelayVerb 9");

        // E4_PRESET_FX_A_ALGORITHM,  id = 6 (06h,00h)      min = 0;  max =  44 (see below)
        load(6, "preset_fxa", "Effect A", v2ps_fxa, null, null, null, PRESET_FX_A, null, false, 0, 44, 0);

        // E4_PRESET_FX_A_PARM_0,     id = 7 (07h,00h)      min = 0;  max =  90
        load(7, "preset_fx_decay", "Decay", null, null, null, null, PRESET_FX_A, null, true, 0, 90, 40);

        // E4_PRESET_FX_A_PARM_1,     id = 8 (08h,00h)      min = 0;  max = 127
        load(8, "preset_fx_hfdamp", "Damping", null, null, null, null, PRESET_FX_A, null, true, 0, 127, 90);

        // E4_PRESET_FX_A_PARM_2,     id = 9 (09h,00h)      min = 0;  max = 127
        load(9, "preset_fx_b2a", "FxB->FxA", null, null, null, null, PRESET_FX_A, null, true, 0, 127, 0);

        // E4_PRESET_FX_A_AMT_0,      id = 10 (0Ah,00h)     min = 0;  max = 100
        load(10, "preset_fx_amt0", "Main Send", null, null, null, "%", PRESET_FX_A, null, true, 0, 100, 0);

        // E4_PRESET_FX_A_AMT_1,      id = 11 (0Bh,00h)     min = 0;  max = 100
        load(11, "preset_fx_amt1", "Sub1 Send", null, null, null, "%", PRESET_FX_A, null, true, 0, 100, 0);

        // E4_PRESET_FX_A_AMT_2,      id = 12 (0Ch,00h)     min = 0;  max = 100
        load(12, "preset_fx_amt2", "Sub2 Send", null, null, null, "%", PRESET_FX_A, null, true, 0, 100, 0);

        // E4_PRESET_FX_A_AMT_3,      id = 13 (0Dh,00h)     min = 0;  max = 100
        load(13, "preset_fx_amt3", "Sub3 Send", null, null, null, "%", PRESET_FX_A, null, true, 0, 100, 0);

        // E4_PRESET_FX_B_ALGORITHM,  id = 14 (0Eh,00h)     min = 0;  max =  27 (see below)
        Map v2ps_fxb = createStringForValueMap();
        v2ps_fxb.put(IntPool.get(0), "Master Effect B");
        v2ps_fxb.put(IntPool.get(1), "Chorus 1");
        v2ps_fxb.put(IntPool.get(2), "Chorus 2");
        v2ps_fxb.put(IntPool.get(3), "Chorus 3");
        v2ps_fxb.put(IntPool.get(4), "Chorus 4");
        v2ps_fxb.put(IntPool.get(5), "Chorus 5");
        v2ps_fxb.put(IntPool.get(6), "Doubling");
        v2ps_fxb.put(IntPool.get(7), "Slapback");
        v2ps_fxb.put(IntPool.get(8), "Flange 1");
        v2ps_fxb.put(IntPool.get(9), "Flange 2");
        v2ps_fxb.put(IntPool.get(10), "Flange 3");
        v2ps_fxb.put(IntPool.get(11), "Flange 4");
        v2ps_fxb.put(IntPool.get(12), "Flange 5");
        v2ps_fxb.put(IntPool.get(13), "Flange 6");
        v2ps_fxb.put(IntPool.get(14), "Flange 7");
        v2ps_fxb.put(IntPool.get(15), "Big Chorus");
        v2ps_fxb.put(IntPool.get(16), "Symphonic");
        v2ps_fxb.put(IntPool.get(17), "Ensemble");
        v2ps_fxb.put(IntPool.get(18), "Delay");
        v2ps_fxb.put(IntPool.get(19), "Delay Stereo");
        v2ps_fxb.put(IntPool.get(20), "Delay Stereo 2");
        v2ps_fxb.put(IntPool.get(21), "Panning Delay");
        v2ps_fxb.put(IntPool.get(22), "Delay Chorus");
        v2ps_fxb.put(IntPool.get(23), "Pan Dly Chorus 1");
        v2ps_fxb.put(IntPool.get(24), "Pan Dly Chorus 2");
        v2ps_fxb.put(IntPool.get(25), "DualTap 1/3");
        v2ps_fxb.put(IntPool.get(26), "DualTap 1/4");
        v2ps_fxb.put(IntPool.get(27), "Vibrato");
        v2ps_fxb.put(IntPool.get(28), "Distortion 1");
        v2ps_fxb.put(IntPool.get(29), "Distortion 2");
        v2ps_fxb.put(IntPool.get(30), "Distorted Flange");
        v2ps_fxb.put(IntPool.get(31), "Distorted Chorus");
        v2ps_fxb.put(IntPool.get(32), "Distorted Double");
        load(14, "preset_fxb", "Effect B", v2ps_fxb, null, null, null, PRESET_FX_B, null, false, 0, 32, 0);

        // E4_PRESET_FX_B_PARM_0,     id = 15 (0Fh,00h)     min = 0;  max = 127
        load(15, "preset_fxb_feedback", "Feedback", null, null, null, null, PRESET_FX_B, null, true, 0, 127, 0);

        // E4_PRESET_FX_B_PARM_1,     id = 16 (10h,00h)     min = 0;  max = 127
        load(16, "preset_fxb_lforate", "LFO Rate", null, null, null, null, PRESET_FX_B, null, true, 0, 127, 3);

        // E4_PRESET_FX_B_PARM_2,     id = 17 (11h,00h)     min = 0;  max = 127
        v2ps = createStringForValueMap();
        for (int i = 0; i < 128; i++)
            v2ps.put(IntPool.get(i), Integer.toString(i * 5));
        load(17, "preset_fxb_delay", "Delay Time", v2ps, null, null, " ms", PRESET_FX_B, null, true, 0, 127, 0);

        // E4_PRESET_FX_B_AMT_0,      id = 18 (12h,00h)     min = 0;  max = 100
        load(18, "preset_fxb_amt0", "Main Send", null, null, null, null, PRESET_FX_B, null, true, 0, 100, 0);

        // E4_PRESET_FX_B_AMT_1,      id = 19 (13h,00h)     min = 0;  max = 100
        load(19, "preset_fxb_amt1", "Sub1 Send", null, null, null, null, PRESET_FX_B, null, true, 0, 100, 0);

        // E4_PRESET_FX_B_AMT_2,      id = 20 (14h,00h)     min = 0;  max = 100
        load(20, "preset_fxb_amt2", "Sub2 Send", null, null, null, null, PRESET_FX_B, null, true, 0, 100, 0);

        // E4_PRESET_FX_B_AMT_3,      id = 21 (15h,00h)     min = 0;  max = 100
        load(21, "preset_fxb_amt3", "Sub3 Send", null, null, null, null, PRESET_FX_B, null, true, 0, 100, 0);

        // REQUIRES MMD CHECK
        // E4_LINK_PRESET,            id = 23 (17h,00h)     min =   0;  max = 999(1255)
        load(23, "link_preset", "Preset", null, null, null, null, LINK_MAIN, null, false);

        // E4_LINK_VOLUME,            id = 24 (18h,00h)     min = -96;  max = +10
        load(24, "link_vol", "Volume", null, null, null, null, LINK_MAIN, null, true, -96, 10, 0);

        // E4_LINK_PAN,               id = 25 (19h,00h)     min = -64;  max = +63
        load(25, "link_pan", "Pan", null, null, null, null, LINK_MAIN, null, true, -64, 63, 0);

        // E4_LINK_TRANSPOSE,         id = 26 (1Ah,00h)     min = -24;  max = +24
        load(26, "link_xpose", "Xpose", null, null, null, null, LINK_MAIN, null, true, -24, 24, 0);

        // E4_LINK_FINE_TUNE,         id = 27 (1Bh,00h)     min = -64;  max = +64
        load(27, "link_ftune", "ftune", null, null, null, null, LINK_MAIN, null, true, -64, 64, 0);

        // E4_LINK_KEY_LOW,           id = 28 (1Ch,00h)     min = 0;  max = 127  (C-2 -> G8)
        load(28, "link_key_low", "Low Key", noteValueStrings, null, null, null, LINK_KEYWIN, null, true, 0, 127, 0);

        // E4_LINK_KEY_LOWFADE,       id = 29 (1Dh,00h)     min = 0;  max = 127
        load(29, "link_key_low_fade", "Fade Low Key", null, null, null, null, LINK_KEYWIN, null, true, 0, 127, 0);

        // E4_LINK_KEY_HIGH,          id = 30 (1Eh,00h)     min = 0;  max = 127  (C-2 -> G8)
        load(30, "link_key_high", "High Key", noteValueStrings, null, null, null, LINK_KEYWIN, null, true, 0, 127, 127);

        // E4_LINK_KEY_HIGHFADE,      id = 31 (1Fh,00h)     min = 0;  max = 127
        load(31, "link_key_high_fade", "Fade High Key", null, null, null, null, LINK_KEYWIN, null, true, 0, 127, 0);

        // E4_LINK_VEL_LOW,           id = 32 (20h,00h)     min = 0;  max = 127
        load(32, "link_vel_low", "Low Vel", null, null, null, null, LINK_VELWIN, null, true, 0, 127, 0);

        // E4_LINK_VEL_LOWFADE,       id = 33 (21h,00h)     min = 0;  max = 127
        load(33, "link_vel_low_fade", "Fade Low Vel", null, null, null, null, LINK_VELWIN, null, true, 0, 127, 0);

        // E4_LINK_VEL_HIGH,          id = 34 (22h,00h)     min = 0;  max = 127
        load(34, "link_vel_high", "High Vel", null, null, null, null, LINK_VELWIN, null, true, 0, 127, 127);

        // E4_LINK_VEL_HIGHFADE,      id = 35 (23h,00h)     min = 0;  max = 127
        load(35, "link_vel_high_fade", "Fade High Vel", null, null, null, null, LINK_VELWIN, null, true, 0, 127, 0);

        // E4_GEN_GROUP_NUM,          id = 37 (25h,00h)     min =   1;  max =  32
        load(37, "gen_group", "Group", null, null, null, null, VOICE_MAIN, null, true, 1, 32, 1);

        // REQUIRES MMD CHECK
        // E4_GEN_SAMPLE,             id = 38 (26h,00h)     min =   0;  max = 999(2999)
        load(38, "gen_sample", "Sample", null, null, null, null, VOICE_MAIN, null, false);

        // E4_GEN_VOLUME,             id = 39 (27h,00h)     min = -96;  max = +10
        load(39, "gen_vol", "Volume", null, null, null, " db", VOICE_AMPLIFIER, null, true, -96, 10, 0);

        // E4_GEN_PAN,                id = 40 (28h,00h)     min = -64;  max = +63
        load(40, "gen_pan", "Pan", null, null, null, null, VOICE_AMPLIFIER, null, true, -64, 63, 0);

        // E4_GEN_CTUNE,              id = 41 (29h,00h)     min = -72;  max = +24  (Voice only)
        load(41, "gen_tune", "Coarse Tune", null, null, null, " semitones", VOICE_TUNING, null, true, -72, 24, 0);

        // E4_GEN_FTUNE,              id = 42 (2Ah,00h)     min = -64;  max = +64
        load(42, "gen_ftune", "Fine Tune", null, null, null, " semitones / 64", VOICE_TUNING, null, true, -64, 64, 0);

        // E4_GEN_XPOSE,              id = 43 (2Bh,00h)     min = -24;  max = +24  (Voice only)
        load(43, "gen_xpose", "Key Xpose", null, null, null, " semitones", VOICE_TUNING, null, true, -24, 24, 0);

        // E4_GEN_ORIG_KEY,           id = 44 (2Ch,00h)     min =   0;  max = 127  (60 = C3, Sample only)
        load(44, "gen_origkey", "Original Key", noteValueStrings, null, null, null, VOICE_VEL_WIN, null, false, 0, 127, 72);

        // E4_GEN_KEY_LOW,            id = 45 (2Dh,00h)     min = 0;  max = 127  (C-2 -> G8)
        load(45, "gen_key_low", "Low Key", noteValueStrings, null, null, null, VOICE_KEY_WIN, null, true, 0, 127, 0);

        // E4_GEN_KEY_LOWFADE,        id = 46 (2Eh,00h)     min = 0;  max = 127
        load(46, "gen_key_low_fade", "Low Fade", null, null, null, null, VOICE_KEY_WIN, null, true, 0, 127, 0);

        // E4_GEN_KEY_HIGH,           id = 47 (2Fh,00h)     min = 0;  max = 127  (C-2 -> G8)
        load(47, "gen_key_high", "High Key", noteValueStrings, null, null, null, VOICE_KEY_WIN, null, true, 0, 127, 127);

        // E4_GEN_KEY_HIGHFADE,       id = 48 (30h,00h)     min = 0;  max = 127
        load(48, "gen_key_high_fade", "High Fade", null, null, null, null, VOICE_KEY_WIN, null, true, 0, 127, 0);

        // E4_GEN_VEL_LOW,            id = 49 (31h,00h)     min = 0;  max = 127
        load(49, "gen_vel_low", "High Vel", null, null, null, null, VOICE_VEL_WIN, null, true, 0, 127, 0);

        // E4_GEN_VEL_LOWFADE,        id = 50 (32h,00h)     min = 0;  max = 127
        load(50, "gen_vel_low_fade", "Low Fade", null, null, null, null, VOICE_VEL_WIN, null, true, 0, 127, 0);

        // E4_GEN_VEL_HIGH,           id = 51 (33h,00h)     min = 0;  max = 127
        load(51, "gen_vel_high", "High Vel", null, null, null, null, VOICE_VEL_WIN, null, true, 0, 127, 127);

        // E4_GEN_VEL_HIGHFADE,       id = 52 (34h,00h)     min = 0;  max = 127
        load(52, "gen_vel_high_fade", "High Fade", null, null, null, null, VOICE_VEL_WIN, null, true, 0, 127, 0);

        // E4_GEN_RT_LOW,             id = 53 (35h,00h)     min = 0;  max = 127  (VoiceObject only)
        load(53, "gen_rt_low", "Low RT", null, null, null, null, VOICE_RT_WIN, null, true, 0, 127, 0);

        // E4_GEN_RT_LOWFADE,         id = 54 (36h,00h)     min = 0;  max = 127  (VoiceObject only)
        load(54, "gen_rt_low_fade", "Low Fade", null, null, null, null, VOICE_RT_WIN, null, true, 0, 127, 0);

        // E4_GEN_RT_HIGH,            id = 55 (37h,00h)     min = 0;  max = 127  (VoiceObject only)
        load(55, "gen_rt_high", "High RT", null, null, null, null, VOICE_RT_WIN, null, true, 0, 127, 127);

        // E4_GEN_RT_HIGHFADE,        id = 56 (38h,00h)     min = 0;  max = 127  (VoiceObject only)
        load(56, "gen_rt_high_fade", "High Fade", null, null, null, null, VOICE_RT_WIN, null, true, 0, 127, 0);

        // E4_VOICE_NON_TRANSPOSE,    id = 57 (39h,00h)     min = 0;  max = 1
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "off");
        v2ps.put(IntPool.get(1), "on");
        load(57, "voice_nxpose", "Non-Xpose", v2ps, null, null, null, VOICE_TUNING, null, true, 0, 1, 0);

        // E4_VOICE_CHORUS_AMOUNT,    id = 58 (3Ah,00h)     min =    0;  max = 100 (%)
        load(58, "voice_chorus_amt", "Chrs Amt", null, null, null, "%", VOICE_TUNING_MODIFIERS, null, true, 0, 100, 0);

        // E4_VOICE_CHORUS_WIDTH,     id = 59 (3Bh,00h)     min = -128;  max =   0
        /*  Displayed Value:
            int pct = ((val + 128) * 100) / 128;
            sprintf(buf,"%3d%%", pct);
         */
        v2ps = createStringForValueMap();
        for (int i = -128; i <= 0; i++)
            v2ps.put(IntPool.get(i), Integer.toString(((i + 128) * 100) / 128));
        load(59, "voice_chorus_width", "Chrs Width", v2ps, null, null, "%", VOICE_TUNING_MODIFIERS, null, true, -128, 0, 0);


        // E4_VOICE_CHORUS_X,         id = 60 (3Ch,00h)     min =  -32;  max = +32 ( ms)
        /*
         Chorus initial ITD(Inter-Aural Time Delay):
        Adjusts the delay of the left and right sounds.
        Positive numbers delay the left channel more.
        Negative numbers delay the right channel more.
         */
        v2ps = createStringForValueMap();
        double[] vals = new double[]{
            0.000,
            0.045, 0.090, 0.136, 0.181, 0.226, 0.272, 0.317, 0.362,
            0.408, 0.453, 0.498, 0.544, 0.589, 0.634, 0.680, 0.725,
            0.770, 0.816, 0.861, 0.907, 0.952, 0.997, 1.043, 1.088,
            1.133, 1.179, 1.224, 1.269, 1.315, 1.360, 1.405, 1.451
        };
        for (int i = 32; i > 0; i--)
            v2ps.put(IntPool.get(-i), Double.toString(-vals[i]));

        for (int i = 0; i < 33; i++)
            v2ps.put(IntPool.get(i), Double.toString(vals[i]));

        load(60, "voice_chorus_itd", "Chrs InitITD", v2ps, null, null, " ms", VOICE_TUNING_MODIFIERS, null, true, -32, 32, 0);

        // E4_VOICE_DELAY,            id = 61 (3Dh,00h)     min = 0;  max = 10000  (ms)
        load(61, "voice_delay", "Delay", null, null, null, " ms", VOICE_TUNING_MODIFIERS, null, true, 0, 10000, 0);

        // E4_VOICE_START_OFFSET,     id = 62 (3Eh,00h)     min = 0;  max =   127
        load(62, "voice_start_offset", "Start Offset", null, null, null, null, VOICE_TUNING_MODIFIERS, null, true, 0, 127, 0);

        // E4_VOICE_GLIDE_RATE,       id = 63 (3Fh,00h)     min = 0;  max = 127 (sec/oct)
        v2ps = createStringForValueMap();
        int[] evunits1 = new int[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 2, 2, 2, 2,
            2, 2, 2, 3, 3, 3, 3, 3,
            4, 4, 4, 4, 5, 5, 5, 5,
            6, 6, 7, 7, 7, 8, 8, 9,
            9, 10, 11, 11, 12, 13, 13, 14,
            15, 16, 17, 18, 19, 20, 22, 23,
            24, 26, 28, 30, 32, 34, 36, 38,
            41, 44, 47, 51, 55, 59, 64, 70,
            76, 83, 91, 100, 112, 125, 142, 163
        };

        int[] evunits2 = new int[]{
            00, 01, 02, 03, 04, 05, 06, 07,
            8, 9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22, 23,
            25, 26, 28, 29, 32, 34, 36, 38,
            41, 43, 46, 49, 52, 55, 58, 62,
            65, 70, 74, 79, 83, 88, 93, 98,
            04, 10, 17, 24, 31, 39, 47, 56,
            65, 74, 84, 95, 06, 18, 31, 44,
            59, 73, 89, 06, 23, 42, 62, 82,
            04, 28, 52, 78, 05, 34, 64, 97,
            32, 67, 06, 46, 90, 35, 83, 34,
            87, 45, 06, 70, 38, 11, 88, 70,
            56, 49, 48, 53, 65, 85, 13, 50,
            97, 54, 24, 06, 02, 15, 44, 93,
            64, 60, 84, 41, 34, 70, 56, 03,
            22, 28, 40, 87, 9, 65, 36, 69
        };
        int msec;
        DecimalFormat dc1, dc2;
        for (int i = 0; i < 128; i++) {
            msec = (evunits1[i] * 1000 + evunits2[i] * 10) / 5;
            dc1 = new DecimalFormat("00");
            dc2 = new DecimalFormat("000");
            v2ps.put(IntPool.get(i), dc1.format(msec / 1000) + "." + dc2.format(msec % 1000));
        }
        load(63, "voice_glide_rate", "Glide Rate", v2ps, null, null, " s/oct", VOICE_TUNING_SETUP, null, true, 0, 127, 0);


        // E4_VOICE_GLIDE_CURVE,      id = 64 (40h,00h)     min = 0;  max = 8
        // linear -> exponential;  0 = linear, 8 = most exponential curve
        load(64, "voice_glide_curve", "Glide Curve", null, null, null, null, VOICE_TUNING_SETUP, null, true, 0, 8, 0);

        // E4_VOICE_SOLO,             id = 65 (41h,00h)     min = 0;  max = 8
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Off");
        v2ps.put(IntPool.get(1), "Multiple Trigger");
        v2ps.put(IntPool.get(2), "Meoldy (last)");
        v2ps.put(IntPool.get(3), "Melody (low)");
        v2ps.put(IntPool.get(4), "Melody (high)");
        v2ps.put(IntPool.get(5), "Synth (last)");
        v2ps.put(IntPool.get(6), "Synth (low)");
        v2ps.put(IntPool.get(7), "Synth (high)");
        v2ps.put(IntPool.get(8), "Fingered Glide");
        load(65, "voice_solo", "Solo Mode", v2ps, null, null, null, VOICE_TUNING_SETUP, null, true, 0, 8, 0);

        // E4_VOICE_ASSIGN_GROUP,     id = 66 (42h,00h)     min = 0;  max = 23
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Poly All");
        v2ps.put(IntPool.get(1), "Poly16 A");
        v2ps.put(IntPool.get(2), "Poly16 B");
        v2ps.put(IntPool.get(3), "Poly 8 A");
        v2ps.put(IntPool.get(4), "Poly 8 B");
        v2ps.put(IntPool.get(5), "Poly 8 C");
        v2ps.put(IntPool.get(6), "Poly 8 D");
        v2ps.put(IntPool.get(7), "Poly 4 A");
        v2ps.put(IntPool.get(8), "Poly 4 B");
        v2ps.put(IntPool.get(9), "Poly 4 C");
        v2ps.put(IntPool.get(10), "Poly 4 D");
        v2ps.put(IntPool.get(11), "Poly 2 A");
        v2ps.put(IntPool.get(12), "Poly 2 B");
        v2ps.put(IntPool.get(13), "Poly 2 C");
        v2ps.put(IntPool.get(14), "Poly 2 D");
        v2ps.put(IntPool.get(15), "Mono A");
        v2ps.put(IntPool.get(16), "Mono B");
        v2ps.put(IntPool.get(17), "Mono C");
        v2ps.put(IntPool.get(18), "Mono D");
        v2ps.put(IntPool.get(19), "Mono E");
        v2ps.put(IntPool.get(20), "Mono F");
        v2ps.put(IntPool.get(21), "Mono G");
        v2ps.put(IntPool.get(22), "Mono H");
        v2ps.put(IntPool.get(23), "Mono I");
        load(66, "voice_assign_group", "Assign Grp", v2ps, null, null, null, VOICE_TUNING_SETUP, null, true, 0, 23, 0);

        // E4_VOICE_LATCHMODE,        id = 67 (43h,00h)     min =    0;  max =   1
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "off");
        v2ps.put(IntPool.get(1), "on");
        load(67, "voice_latch_mode", "Latch Mode", v2ps, null, null, null, VOICE_TUNING_SETUP, null, true, 0, 1, 0);

        // E4_VOICE_VOLENV_DEPTH,     id = 68 (44h,00h)     min = 0;  max = 16
        // Amp Env Depth:    -96dB to -48dB by 3's
        v2ps = createStringForValueMap();
        for (int i = 0; i < 17; i++)
            v2ps.put(IntPool.get(i), Integer.toString(-96 + i * 3));
        load(68, "voice_volenv_depth", "Env Depth", v2ps, null, null, "dB", VOICE_AMPLIFIER, null, true, 0, 16, 0);

        // REQUIRES MMD CHECK
        // E4_VOICE_SUBMIX,           id = 69 (45h,00h)     min = -1;  max =   3
        Map v2ps_submix = createStringForValueMap();
        v2ps_submix.put(IntPool.get(-2), "mixer");
        v2ps_submix.put(IntPool.get(-1), "voice");
        v2ps_submix.put(IntPool.get(0), "Main");
        v2ps_submix.put(IntPool.get(1), "Bus 1");
        v2ps_submix.put(IntPool.get(2), "Bus 2");
        v2ps_submix.put(IntPool.get(3), "Bus 3");
        v2ps_submix.put(IntPool.get(4), "Bus 4");
        v2ps_submix.put(IntPool.get(5), "Bus 5");
        v2ps_submix.put(IntPool.get(6), "Bus 6");
        v2ps_submix.put(IntPool.get(7), "Bus 7");
        v2ps_submix.put(IntPool.get(8), "Bus 8");
        v2ps_submix.put(IntPool.get(9), "Bus 9");
        v2ps_submix.put(IntPool.get(10), "Bus 10");
        v2ps_submix.put(IntPool.get(11), "Bus 11");
        v2ps_submix.put(IntPool.get(12), "Bus 12");
        v2ps_submix.put(IntPool.get(13), "GFX 1");
        v2ps_submix.put(IntPool.get(14), "GFX 2");
        load(69, "voice_submix", "Submix", v2ps_submix, null, null, null, VOICE_AMPLIFIER, null, true);

        // E4_VOICE_VENV_SEG0_RATE,   id = 70 (46h,00h)     min = 0;  max = 127 (Atk1 Rate)
        load(70, "voice_vatk1_rate", "Rate", null, null, null, null, VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 127, 0);

        // E4_VOICE_VENV_SEG0_TGTLVL, id = 71 (47h,00h)     min = 0;  max = 100 (Atk1 Level%)
        load(71, "voice_vatk1_level", "Level", null, null, null, "%", VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 100, 0);

        // E4_VOICE_VENV_SEG1_RATE,   id = 72 (48h,00h)     min = 0;  max = 127 (Dcy1 Rate)
        load(72, "voice_vatk2_rate", "Rate", null, null, null, null, VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 127, 0);

        // E4_VOICE_VENV_SEG1_TGTLVL, id = 73 (49h,00h)     min = 0;  max = 100 (Dcy1 Level%)
        load(73, "voice_vatk2_level", "Level", null, null, null, "%", VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 100, 100);

        // E4_VOICE_VENV_SEG2_RATE,   id = 74 (4Ah,00h)     min = 0;  max = 127 (Rls1 Rate)
        load(74, "voice_vdec1_rate", "Rate", null, null, null, null, VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 127, 0);

        // E4_VOICE_VENV_SEG2_TGTLVL, id = 75 (4Bh,00h)     min = 0;  max = 100 (Rls1 Level%)
        load(75, "voice_vdec1_level", "Level", null, null, null, "%", VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 100, 99);

        // E4_VOICE_VENV_SEG3_RATE,   id = 76 (4Ch,00h)     min = 0;  max = 127 (Atk2 Rate)
        load(76, "voice_vdec2_rate", "Rate", null, null, null, null, VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 127, 0);

        // E4_VOICE_VENV_SEG3_TGTLVL, id = 77 (4Dh,00h)     min = 0;  max = 100 (Atk2 Level%)
        load(77, "voice_vdec2_level", "Level", null, null, null, "%", VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 100, 100);

        // E4_VOICE_VENV_SEG4_RATE,   id = 78 (4Eh,00h)     min = 0;  max = 127 (Dcy2 Rate)
        load(78, "voice_vrls1_rate", "Rate", null, null, null, null, VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 127, 20);

        // E4_VOICE_VENV_SEG4_TGTLVL, id = 79 (4Fh,00h)     min = 0;  max = 100 (Dcy2 Level%)
        load(79, "voice_vrls1_level", "Level", null, null, null, "%", VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 100, 0);

        // E4_VOICE_VENV_SEG5_RATE,   id = 80 (50h,00h)     min = 0;  max = 127 (Rls2 Rate)
        load(80, "voice_vrls2_rate", "Rate", null, null, null, null, VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 127, 0);

        // E4_VOICE_VENV_SEG5_TGTLVL, id = 81 (51h,00h)     min = 0;  max = 100 (Rls2 Level%)
        load(81, "voice_vrls2_level", "Level", null, null, null, "%", VOICE_AMPLIFIER_ENVELOPE, COLLABORATION_VENV, true, 0, 100, 0);

        // REQUIRES MMD CHECK
        // E4_VOICE_FTYPE,            id = 82 (52h,00h)     min = 0;  max = variable
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "2 Pole Low-pass");
        v2ps.put(IntPool.get(1), "4 Pole Low-pass");
        v2ps.put(IntPool.get(2), "6 Pole Low-pass");
        v2ps.put(IntPool.get(3), "2nd Order High-pass");
        v2ps.put(IntPool.get(4), "4th Order High-pass");
        v2ps.put(IntPool.get(5), "2nd Order Band-pass");
        v2ps.put(IntPool.get(6), "4th Order Band-pass");
        v2ps.put(IntPool.get(7), "Contrary Band-pass");
        v2ps.put(IntPool.get(8), "Swept EQ 1 octave");
        v2ps.put(IntPool.get(9), "Swept EQ 2->1 octave");
        v2ps.put(IntPool.get(10), "Swept EQ 3->1 octave");
        v2ps.put(IntPool.get(11), "Phaser 1");
        v2ps.put(IntPool.get(12), "Phaser 2");
        v2ps.put(IntPool.get(13), "Bat-Phaser");
        v2ps.put(IntPool.get(14), "Flanger Lite");
        v2ps.put(IntPool.get(15), "Vocal Ah-Ay-Ee");
        v2ps.put(IntPool.get(16), "Vocal Oo-Ah");
        v2ps.put(IntPool.get(17), "Dual EQ Morph");
        v2ps.put(IntPool.get(18), "2EQ + Lowpass Morph");
        v2ps.put(IntPool.get(19), "2EQMorph + Expression");
        v2ps.put(IntPool.get(20), "Peak/Shelf Morph");
        load(82, "voice_ftype", "Filter", v2ps, null, null, null, VOICE_FILTER, null, false);

        //
        // NOTE!! the majority of filter sub ids 83-92 handling is done in the special filter tables
        // we just make sure reference and id mappings are available in main tables here
        //
        // E4_VOICE_FMORPH,           id = 83      min = 0;  max = 255
        load(83, "voice_fmorph", null, null, null, null, null, VOICE_FILTER, null, true, 0, 255, 255);

        // E4_VOICE_FKEY_XFORM,       id = 84      min = 0;  max = 127
        load(84, "voice_xform", null, null, null, null, null, VOICE_FILTER, null, true, 0, 127, 0);

        // E4_VOICE_FILT_GEN_PARM1,   id = 85      min = 0;  max = 127
        load(85, "voice_fgen1", null, null, null, null, null, VOICE_FILTER_RESERVED, null, true, 0, 127, 0);

        // E4_VOICE_FILT_GEN_PARM2,   id = 86      min = 0;  max = 127
        load(86, "voice_fgen2", null, null, null, null, null, VOICE_FILTER_RESERVED, null, true, 0, 127, 0);

        // E4_VOICE_FILT_GEN_PARM3,   id = 87      min = 0;  max = 127
        load(87, "voice_fgen3", null, null, null, null, null, VOICE_FILTER, null, true, 0, 127, 0);

        // E4_VOICE_FILT_GEN_PARM4,   id = 88      min = 0;  max = 127
        load(88, "voice_fgen4", null, null, null, null, null, VOICE_FILTER, null, true, 0, 127, 0);

        // E4_VOICE_FILT_GEN_PARM5,   id = 89      min = 0;  max = 127
        load(89, "voice_fgen5", null, null, null, null, null, VOICE_FILTER, null, true, 0, 127, 0);

        // E4_VOICE_FILT_GEN_PARM6,   id = 90      min = 0;  max = 127
        load(90, "voice_fgen6", null, null, null, null, null, VOICE_FILTER, null, true, 0, 127, 0);

        // E4_VOICE_FILT_GEN_PARM7,   id = 91      min = 0;  max = 127
        load(91, "voice_fgen7", null, null, null, null, null, VOICE_FILTER, null, true, 0, 127, 0);

        // E4_VOICE_FILT_GEN_PARM8,   id = 92      min = 0;  max = 127
        load(92, "voice_fgen8", null, null, null, null, null, VOICE_FILTER, null, true, 0, 127, 0);

        // E4_VOICE_FENV_SEG0_RATE,   id = 93 (5Dh,00h)     min = 0;  max = 127 (Atk1 Rate)
        load(93, "voice_fatk1_rate", "Rate", null, null, null, null, VOICE_FILTER_ENVELOPE, "FilterEnv", true, 0, 127, 0);

        // E4_VOICE_FENV_SEG0_TGTLVL, id = 94 (5Eh,00h)     min = 0;  max = 100 (Atk1 Level%)
        load(94, "voice_fatk1_level", "Level", null, null, null, "%", VOICE_FILTER_ENVELOPE, "FilterEnv", true, -100, 100, 0);

        // E4_VOICE_FENV_SEG1_RATE,   id = 95 (5Fh,00h)     min = 0;  max = 127 (Dcy1 Rate)
        load(95, "voice_fatk2_rate", "Rate", null, null, null, null, VOICE_FILTER_ENVELOPE, "FilterEnv", true, 0, 127, 0);

        // E4_VOICE_FENV_SEG1_TGTLVL, id = 96 (60h,00h)     min = 0;  max = 100 (Dcy1 Level%)
        load(96, "voice_fatk2_level", "Level", null, null, null, "%", VOICE_FILTER_ENVELOPE, "FilterEnv", true, -100, 100, 100);

        // E4_VOICE_FENV_SEG2_RATE,   id = 97 (61h,00h)     min = 0;  max = 127 (Rls1 Rate)
        load(97, "voice_fdec1_rate", "Rate", null, null, null, null, VOICE_FILTER_ENVELOPE, "FilterEnv", true, 0, 127, 0);

        // E4_VOICE_FENV_SEG2_TGTLVL, id = 98 (62h,00h)     min = 0;  max = 100 (Rls1 Level%)
        load(98, "voice_fdec1_level", "Level", null, null, null, "%", VOICE_FILTER_ENVELOPE, "FilterEnv", true, -100, 100, 99);

        // E4_VOICE_FENV_SEG3_RATE,   id = 99 (63h,00h)     min = 0;  max = 127 (Atk2 Rate)
        load(99, "voice_fdec2_rate", "Rate", null, null, null, null, VOICE_FILTER_ENVELOPE, "FilterEnv", true, 0, 127, 0);

        // E4_VOICE_FENV_SEG3_TGTLVL, id = 100 (64h,00h)    min = 0;  max = 100 (Atk2 Level%)
        load(100, "voice_dec2_level", "Level", null, null, null, "%", VOICE_FILTER_ENVELOPE, "FilterEnv", true, -100, 100, 100);

        // E4_VOICE_FENV_SEG4_RATE,   id = 101 (65h,00h)    min = 0;  max = 127 (Dcy2 Rate)
        load(101, "voice_frls1_rate", "Rate", null, null, null, null, VOICE_FILTER_ENVELOPE, "FilterEnv", true, 0, 127, 20);

        // E4_VOICE_FENV_SEG4_TGTLVL, id = 102 (66h,00h)    min = 0;  max = 100 (Dcy2 Level%)
        load(102, "voice_frls1_level", "Level", null, null, null, "%", VOICE_FILTER_ENVELOPE, "FilterEnv", true, -100, 100, 0);

        // E4_VOICE_FENV_SEG5_RATE,   id = 103 (67h,00h)    min = 0;  max = 127 (Rls2 Rate)
        load(103, "voice_frls2_rate", "Rate", null, null, null, null, VOICE_FILTER_ENVELOPE, "FilterEnv", true, 0, 127, 0);

        // E4_VOICE_FENV_SEG5_TGTLVL, id = 104 (68h,00h)    min = 0;  max = 100 (Rls2 Level%)
        load(104, "voice_frls2_level", "Level", null, null, null, "%", VOICE_FILTER_ENVELOPE, "FilterEnv", true, -100, 100, 0);

        // E4_VOICE_LFO_RATE,         id = 105 (69h,00h)    min = 0;  max = 127
        int lfounits1[] = new int[]
        {
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 3, 3, 3,
            3, 3, 3, 3, 3, 3, 3, 4,
            4, 4, 4, 4, 4, 4, 4, 4,
            5, 5, 5, 5, 5, 5, 5, 6,
            6, 6, 6, 6, 6, 7, 7, 7,
            7, 7, 7, 8, 8, 8, 8, 8,
            9, 9, 9, 9, 10, 10, 10, 10,
            10, 11, 11, 11, 11, 12, 12, 12,
            13, 13, 13, 13, 14, 14, 14, 15,
            15, 15, 16, 16, 17, 17, 17, 18,
        };
        int lfounits2[] = new int[]
        {
            8, 11, 15, 18, 21, 25, 28, 32,
            35, 39, 42, 46, 50, 54, 58, 63,
            67, 71, 76, 80, 85, 90, 94, 99,
            04, 10, 15, 20, 25, 31, 37, 42,
            48, 54, 60, 67, 73, 79, 86, 93,
            00, 07, 14, 21, 29, 36, 44, 52,
            60, 68, 77, 85, 94, 03, 12, 21,
            31, 40, 50, 60, 70, 81, 91, 02,
            13, 25, 36, 48, 60, 72, 84, 97,
            10, 23, 37, 51, 65, 79, 94, 8,
            24, 39, 55, 71, 88, 04, 21, 39,
            57, 75, 93, 12, 32, 51, 71, 92,
            13, 34, 56, 78, 00, 23, 47, 71,
            95, 20, 46, 71, 98, 25, 52, 80,
            9, 38, 68, 99, 30, 61, 93, 26,
            60, 94, 29, 65, 01, 38, 76, 14,
        };
        v2ps = createStringForValueMap();
        for (int i = 0; i < 128; i++) {
            dc1 = new DecimalFormat("#0");
            dc2 = new DecimalFormat("00");
            v2ps.put(IntPool.get(i), dc1.format(lfounits1[i]) + "." + dc2.format(lfounits2[i]));
        }
        load(105, "voice_lfo1_rate", "LFO Rate", v2ps, null, null, null, VOICE_LFO1, null, true, 0, 127, 63);

        // REQUIRES MMD CHECK
        // E4_VOICE_LFO_SHAPE,        id = 106 (6Ah,00h)    min = 0;  max =   7
        Map v2ps_lfoshape = createStringForValueMap();
        v2ps_lfoshape.put(IntPool.get(-1), "Random");
        v2ps_lfoshape.put(IntPool.get(0), "Triangle");
        v2ps_lfoshape.put(IntPool.get(1), "Sine");
        v2ps_lfoshape.put(IntPool.get(2), "Sawtooth");
        v2ps_lfoshape.put(IntPool.get(3), "Square");
        v2ps_lfoshape.put(IntPool.get(4), "33% Pulse");
        v2ps_lfoshape.put(IntPool.get(5), "25% Pulse");
        v2ps_lfoshape.put(IntPool.get(6), "16% Pulse");
        v2ps_lfoshape.put(IntPool.get(7), "12% Pulse");
        v2ps_lfoshape.put(IntPool.get(8), "pat:Octaves");
        v2ps_lfoshape.put(IntPool.get(9), "pat:Fifth + Oct");
        v2ps_lfoshape.put(IntPool.get(10), "pat:Sus4 trip");
        v2ps_lfoshape.put(IntPool.get(11), "pat:Neener");
        v2ps_lfoshape.put(IntPool.get(12), "Sine 1,2");
        v2ps_lfoshape.put(IntPool.get(13), "Sine 1,3,5");
        v2ps_lfoshape.put(IntPool.get(14), "Sine + Noise");
        v2ps_lfoshape.put(IntPool.get(15), "Hemi-Quaver");
        load(106, "voice_lfo1_shape", "Shape", v2ps_lfoshape, null, null, null, VOICE_LFO1, null, true, 0, 15, 0);
        // E4_VOICE_LFO_DELAY,        id = 107 (6Bh,00h)    min = 0;  max = 127
        load(107, "voice_lfo1_delay", "Delay", null, null, null, null, VOICE_LFO1, null, true, 0, 127, 0);
        // E4_VOICE_LFO_VAR,          id = 108 (6Ch,00h)    min = 0;  max = 100 (%)
        load(108, "voice_lfo1_var", "Variation", null, null, null, "%", VOICE_LFO1, null, true, 0, 100, 0);
        // E4_VOICE_LFO_SYNC,         id = 109 (6Dh,00h)    min = 0;  max =   1
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Key Sync");
        v2ps.put(IntPool.get(1), "Free-Run");
        load(109, "voice_lfo1_sync", "Sync", v2ps, null, null, null, VOICE_LFO1, null, true, 0, 1, 0);

        // E4_VOICE_LFO2_RATE,        id = 110 (6Eh,00h)    min = 0;  max = 127
        v2ps = createStringForValueMap();

        for (int i = 0; i < 128; i++) {
            dc1 = new DecimalFormat("#0");
            dc2 = new DecimalFormat("00");
            v2ps.put(IntPool.get(i), dc1.format(lfounits1[i]) + "." + dc2.format(lfounits2[i]));
        }
        load(110, "voice_lfo2_rate", "LFO Rate", v2ps, null, null, null, VOICE_LFO2, null, true, 0, 127, 63);

        // E4_VOICE_LFO2_SHAPE,       id = 111 (6Fh,00h)    min = 0;  max =   7 (as above)
        load(111, "voice_lfo2_shape", "Shape", v2ps_lfoshape, null, null, null, VOICE_LFO2, null, true, 0, 15, 0);
        // E4_VOICE_LFO2_DELAY,       id = 112 (70h,00h)    min = 0;  max = 127
        load(112, "voice_lfo2_delay", "Delay", null, null, null, null, VOICE_LFO2, null, true, 0, 127, 0);
        // E4_VOICE_LFO2_VAR,         id = 113 (71h,00h)    min = 0;  max = 100 (%)
        load(113, "voice_lfo2_var", "Variation", null, null, null, "%", VOICE_LFO2, null, true, 0, 100, 0);
        // E4_VOICE_LFO2_SYNC,        id = 114 (72h,00h)    min = 0;  max =   1 (as above)
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Key Sync");
        v2ps.put(IntPool.get(1), "Free-Run");
        load(114, "voice_lfo2_sync", "Sync", v2ps, null, null, null, VOICE_LFO2, null, true, 0, 1, 0);
        // E4_VOICE_LFO2_OP0_PARM,    id = 115 (73h,00h)    min = 0;  max = 10  (Lag 0: )
        load(115, "voice_lfo2_lag0", "Lag0", null, null, null, null, VOICE_LFO2, null, true, 0, 10, 0);
        // E4_VOICE_LFO2_OP1_PARM,    id = 116 (74h,00h)    min = 0;  max = 10  (Lag 1: )
        load(116, "voice_lfo2_lag1", "Lag1", null, null, null, null, VOICE_LFO2, null, true, 0, 10, 0);

        // E4_VOICE_AENV_SEG0_RATE,   id = 117 (75h,00h)    min = 0;  max = 127 (Atk1 Rate)
        load(117, "voice_aatk1_rate", "Rate", null, null, null, null, VOICE_AUX_ENVELOPE, "AuxEnv", true, 0, 127, 0);
        // E4_VOICE_AENV_SEG0_TGTLVL, id = 118 (76h,00h)    min = 0;  max = 100 (Atk1 Level%)
        load(118, "voice_aatk1_level", "Level", null, null, null, "%", VOICE_AUX_ENVELOPE, "AuxEnv", true, -100, 100, 0);
        // E4_VOICE_AENV_SEG1_RATE,   id = 119 (77h,00h)    min = 0;  max = 127 (Dcy1 Rate)
        load(119, "voice_aatk2_rate", "Rate", null, null, null, null, VOICE_AUX_ENVELOPE, "AuxEnv", true, 0, 127, 0);
        // E4_VOICE_AENV_SEG1_TGTLVL, id = 120 (78h,00h)    min = 0;  max = 100 (Dcy1 Level%)
        load(120, "voice_aatk2_level", "Level", null, null, null, "%", VOICE_AUX_ENVELOPE, "AuxEnv", true, -100, 100, 100);
        // E4_VOICE_AENV_SEG2_RATE,   id = 121 (79h,00h)    min = 0;  max = 127 (Rls1 Rate)
        load(121, "voice_adec1_rate", "Rate", null, null, null, null, VOICE_AUX_ENVELOPE, "AuxEnv", true, 0, 127, 0);
        // E4_VOICE_AENV_SEG2_TGTLVL, id = 122 (7Ah,00h)    min = 0;  max = 100 (Rls1 Level%)
        load(122, "voice_adec1_level", "Level", null, null, null, "%", VOICE_AUX_ENVELOPE, "AuxEnv", true, -100, 100, 99);
        // E4_VOICE_AENV_SEG3_RATE,   id = 123 (7Bh,00h)    min = 0;  max = 127 (Atk2 Rate)
        load(123, "voice_adec2_rate", "Rate", null, null, null, null, VOICE_AUX_ENVELOPE, "AuxEnv", true, 0, 127, 0);
        // E4_VOICE_AENV_SEG3_TGTLVL, id = 124 (7Ch,00h)    min = 0;  max = 100 (Atk2 Level%)
        load(124, "voice_adec2_level", "Level", null, null, null, "%", VOICE_AUX_ENVELOPE, "AuxEnv", true, -100, 100, 100);
        // E4_VOICE_AENV_SEG4_RATE,   id = 125 (7Dh,00h)    min = 0;  max = 127 (Dcy2 Rate)
        load(125, "voice_arls1_rate", "Rate", null, null, null, null, VOICE_AUX_ENVELOPE, "AuxEnv", true, 0, 127, 20);
        // E4_VOICE_AENV_SEG4_TGTLVL, id = 126 (7Eh,00h)    min = 0;  max = 100 (Dcy2 Leel%)
        load(126, "voice_arls1_level", "Level", null, null, null, "%", VOICE_AUX_ENVELOPE, "AuxEnv", true, -100, 100, 0);
        // E4_VOICE_AENV_SEG5_RATE,   id = 127 (7Fh,00h)    min = 0;  max = 127 (Rls2 Rate)
        load(127, "voice_arls2_rate", "Rate", null, null, null, null, VOICE_AUX_ENVELOPE, "AuxEnv", true, 0, 127, 0);
        // E4_VOICE_AENV_SEG5_TGTLVL, id = 128 (00h,01h)    min = 0;  max = 100 (Rls2 Level%)
        load(128, "voice_arls2_rate", "Level", null, null, null, "%", VOICE_AUX_ENVELOPE, "AuxEnv", true, -100, 100, 0);

        Map v2ps_source = createStringForValueMap();
        Map v2ps_dest = createStringForValueMap();
        Map v2ts_source = createStringForValueMap();
        Map v2ts_dest = createStringForValueMap();

        // CORD SOURCE PARAMETERS
        v2ps_source.put(IntPool.get(0), "Off");

        v2ps_source.put(IntPool.get(4), "XfdRnd");
        v2ts_source.put(IntPool.get(4), "Crossfade Random");

        v2ps_source.put(IntPool.get(8), "Key+");
        v2ts_source.put(IntPool.get(8), "Key 0..127");

        v2ps_source.put(IntPool.get(9), "Key~");
        v2ts_source.put(IntPool.get(9), "Key -64..+63");

        v2ps_source.put(IntPool.get(10), "Vel+");
        v2ts_source.put(IntPool.get(10), "Velocity 0..127");

        v2ps_source.put(IntPool.get(11), "Vel~");
        v2ts_source.put(IntPool.get(11), "Velocity -64..+63");

        v2ps_source.put(IntPool.get(12), "Vel<");
        v2ts_source.put(IntPool.get(12), "Velocity -127..0");

        v2ps_source.put(IntPool.get(13), "RlsVel");
        v2ts_source.put(IntPool.get(13), "Release Velocity");

        v2ps_source.put(IntPool.get(14), "Gate");

        v2ps_source.put(IntPool.get(16), "PitWl");
        v2ts_source.put(IntPool.get(16), "Pitch Wheel");

        v2ps_source.put(IntPool.get(17), "ModWl");
        v2ts_source.put(IntPool.get(17), "Modulation Wheel");

        v2ps_source.put(IntPool.get(18), "Press");
        v2ts_source.put(IntPool.get(18), "Pressure (AfterTouch)");

        v2ps_source.put(IntPool.get(19), "Pedal");
        v2ts_source.put(IntPool.get(19), "Pedal");

        v2ps_source.put(IntPool.get(20), "MidiA");
        v2ts_source.put(IntPool.get(20), "Midi A Controller");

        v2ps_source.put(IntPool.get(21), "MidiB");
        v2ts_source.put(IntPool.get(21), "Midi B Controller");

        v2ps_source.put(IntPool.get(22), "FtSw1");
        v2ts_source.put(IntPool.get(22), "Foot Switch 1");

        v2ps_source.put(IntPool.get(23), "FtSw2");
        v2ts_source.put(IntPool.get(23), "Foot Switch 2");

        v2ps_source.put(IntPool.get(24), "Ft1FF");
        v2ts_source.put(IntPool.get(24), "Flip-Flop Foot Switch 1");

        v2ps_source.put(IntPool.get(25), "Ft2FF");
        v2ts_source.put(IntPool.get(25), "Flip-Flop Foot Switch 2");

        v2ps_source.put(IntPool.get(26), "MidiVl");
        v2ts_source.put(IntPool.get(26), "Midi Volume Controller 7");

        v2ps_source.put(IntPool.get(27), "MidiPan");
        v2ts_source.put(IntPool.get(27), "Midi Pan Controller 10");

        v2ps_source.put(IntPool.get(32), "MidiC");
        v2ts_source.put(IntPool.get(32), "Midi C Controller");

        v2ps_source.put(IntPool.get(33), "MidiD");
        v2ts_source.put(IntPool.get(33), "Midi D Controller");

        v2ps_source.put(IntPool.get(34), "MidiE");
        v2ts_source.put(IntPool.get(34), "Midi E Controller");

        v2ps_source.put(IntPool.get(35), "MidiF");
        v2ts_source.put(IntPool.get(35), "Midi F Controller");

        v2ps_source.put(IntPool.get(36), "MidiG");
        v2ts_source.put(IntPool.get(36), "Midi G Controller");

        v2ps_source.put(IntPool.get(37), "MidiH");
        v2ts_source.put(IntPool.get(37), "Midi H Controller");

        v2ps_source.put(IntPool.get(38), "Thumb");
        v2ts_source.put(IntPool.get(38), "Thumby Controller");

        v2ps_source.put(IntPool.get(39), "ThumbFF");
        v2ts_source.put(IntPool.get(39), "Thumby Flip-Flop Controller");


        v2ps_source.put(IntPool.get(40), "MidiI");
        v2ts_source.put(IntPool.get(40), "Midi I Controller");
        v2ps_source.put(IntPool.get(41), "MidiJ");
        v2ts_source.put(IntPool.get(41), "Midi J Controller");
        v2ps_source.put(IntPool.get(42), "MidiK");
        v2ts_source.put(IntPool.get(42), "Midi K Controller");
        v2ps_source.put(IntPool.get(43), "MidiL");
        v2ts_source.put(IntPool.get(43), "Midi L Controller");

        v2ps_source.put(IntPool.get(48), "KeyGld");
        v2ts_source.put(IntPool.get(48), "Key Glide");

        v2ps_source.put(IntPool.get(72), "VEnv+");
        v2ts_source.put(IntPool.get(72), "Volume Envelope 0..127");

        v2ps_source.put(IntPool.get(73), "VEnv~");
        v2ts_source.put(IntPool.get(73), "Volume Envelope -64..+63");

        v2ps_source.put(IntPool.get(74), "VEnv<");
        v2ts_source.put(IntPool.get(74), "Volume Envelope -127..0");

        v2ps_source.put(IntPool.get(80), "FEnv+");
        v2ts_source.put(IntPool.get(80), "Filter Envelope 0..127");

        v2ps_source.put(IntPool.get(81), "FEnv~");
        v2ts_source.put(IntPool.get(81), "Filter Envelope -64..+63");

        v2ps_source.put(IntPool.get(82), "FEnv<");
        v2ts_source.put(IntPool.get(82), "Filter Envelope -127..0");

        v2ps_source.put(IntPool.get(88), "AEnv+");
        v2ts_source.put(IntPool.get(88), "Aux Envelope 0..127");

        v2ps_source.put(IntPool.get(89), "AEnv~");
        v2ts_source.put(IntPool.get(89), "Aux Envelope -64..+63");

        v2ps_source.put(IntPool.get(90), "AEnv<");
        v2ts_source.put(IntPool.get(90), "Aux Envelope -127..0");

        v2ps_source.put(IntPool.get(96), "Lfo1~");
        v2ts_source.put(IntPool.get(96), "Lfo1 Centered");

        v2ps_source.put(IntPool.get(97), "Lfo1+");
        v2ts_source.put(IntPool.get(97), "Lfo1 Positive");

        v2ps_source.put(IntPool.get(98), "White");
        v2ts_source.put(IntPool.get(98), "White Noise");

        v2ps_source.put(IntPool.get(99), "Pink");
        v2ts_source.put(IntPool.get(99), "Pink Noise");

        v2ps_source.put(IntPool.get(100), "kRand1");
        v2ts_source.put(IntPool.get(100), "kRandom1");

        v2ps_source.put(IntPool.get(101), "kRand2");
        v2ts_source.put(IntPool.get(101), "kRandom2");

        v2ps_source.put(IntPool.get(104), "Lfo2~");
        v2ts_source.put(IntPool.get(104), "Lfo2 Centered");

        v2ps_source.put(IntPool.get(105), "Lfo2+");
        v2ts_source.put(IntPool.get(105), "Lfo2 Positive");

        v2ps_source.put(IntPool.get(106), "Lag0in");
        v2ts_source.put(IntPool.get(106), "Lag0 input (summing amp out)");

        v2ps_source.put(IntPool.get(107), "Lag0");
        v2ts_source.put(IntPool.get(107), "Lag0");

        v2ps_source.put(IntPool.get(108), "Lag1in");
        v2ts_source.put(IntPool.get(108), "Lag1 input (summing amp out)");

        v2ps_source.put(IntPool.get(109), "Lag1");
        v2ts_source.put(IntPool.get(109), "Lag1");

        v2ps_source.put(IntPool.get(144), "CkDwhl");
        v2ts_source.put(IntPool.get(144), "Clock Double Whole Note");

        v2ps_source.put(IntPool.get(145), "CkWhle");
        v2ts_source.put(IntPool.get(145), "Clock Whole Note");

        v2ps_source.put(IntPool.get(146), "CkHalf");
        v2ts_source.put(IntPool.get(146), "Clock Half Note");

        v2ps_source.put(IntPool.get(147), "CkQtr");
        v2ts_source.put(IntPool.get(147), "Clock Quater Note");

        v2ps_source.put(IntPool.get(148), "Ck8th");
        v2ts_source.put(IntPool.get(148), "Clock Eight Note");

        v2ps_source.put(IntPool.get(149), "Ck16th");
        v2ts_source.put(IntPool.get(149), "Clock Sixteenth Note");

        v2ps_source.put(IntPool.get(160), "DC");
        v2ts_source.put(IntPool.get(160), "DC Offset");

        v2ps_source.put(IntPool.get(161), "Sum");
        v2ts_source.put(IntPool.get(161), "Summing Amp");

        v2ps_source.put(IntPool.get(162), "Switch");
        v2ts_source.put(IntPool.get(162), "Switch");

        v2ps_source.put(IntPool.get(163), "Abs");
        v2ts_source.put(IntPool.get(163), "Absolute Value");

        v2ps_source.put(IntPool.get(164), "Diode");
        v2ts_source.put(IntPool.get(164), "Diode");

        v2ps_source.put(IntPool.get(165), "Flip-Flop");
        v2ts_source.put(IntPool.get(165), "Flip-Flop");

        v2ps_source.put(IntPool.get(166), "Quant");
        v2ts_source.put(IntPool.get(166), "Quantizer");

        v2ps_source.put(IntPool.get(167), "Gain 4X");
        v2ts_source.put(IntPool.get(167), "Gain 4X");

        // CORD DEST PARAMETERS
        v2ps_dest.put(IntPool.get(0), "Off");

        v2ps_dest.put(IntPool.get(8), "KeySust");
        v2ts_dest.put(IntPool.get(8), "Key Sustain");

        v2ps_dest.put(IntPool.get(47), "FinePtch");
        v2ts_dest.put(IntPool.get(47), "Fine Pitch");

        v2ps_dest.put(IntPool.get(48), "Pitch");

        v2ps_dest.put(IntPool.get(49), "Glide");
        v2ts_dest.put(IntPool.get(49), "Keyboard Glide");

        v2ps_dest.put(IntPool.get(50), "ChrsAmt");
        v2ts_dest.put(IntPool.get(50), "Chorus Amount");

        v2ps_dest.put(IntPool.get(51), "'ChrsITD");
        v2ts_dest.put(IntPool.get(51), "Chorus Position ITD");

        v2ps_dest.put(IntPool.get(52), "'SStart");
        v2ts_dest.put(IntPool.get(52), "SampleObject Start");

        v2ps_dest.put(IntPool.get(53), "SLoop");
        v2ts_dest.put(IntPool.get(53), "SampleObject Loop");

        v2ps_dest.put(IntPool.get(54), "SRetrig");
        v2ts_dest.put(IntPool.get(54), "SampleObject Retrigger");

        v2ps_dest.put(IntPool.get(56), "FilFreq");
        v2ts_dest.put(IntPool.get(56), "Filter Frequency");

        v2ps_dest.put(IntPool.get(57), "'FilRes");
        v2ts_dest.put(IntPool.get(57), "Filter Resonance");

        v2ps_dest.put(IntPool.get(64), "AmpVol");
        v2ts_dest.put(IntPool.get(64), "Amplifier Volume");

        v2ps_dest.put(IntPool.get(65), "AmpPan");
        v2ts_dest.put(IntPool.get(65), "Amplifier Pan");

        v2ps_dest.put(IntPool.get(66), "AmpXfd");
        v2ts_dest.put(IntPool.get(66), "Amplifier Crossfade");

        v2ps_dest.put(IntPool.get(72), "VEnvRts");
        v2ts_dest.put(IntPool.get(72), "Volume Envelope Rates");

        v2ps_dest.put(IntPool.get(73), "VEnvAtk");
        v2ts_dest.put(IntPool.get(73), "Volume Envelope Attack");

        v2ps_dest.put(IntPool.get(74), "VEnvDcy");
        v2ts_dest.put(IntPool.get(74), "Volume Envelope Decay");

        v2ps_dest.put(IntPool.get(75), "VEnvRls");
        v2ts_dest.put(IntPool.get(75), "Volume Envelope Release");

        v2ps_dest.put(IntPool.get(80), "FEnvRts");
        v2ts_dest.put(IntPool.get(80), "Filter Enveople Rates");

        v2ps_dest.put(IntPool.get(81), "FEnvAtk");
        v2ts_dest.put(IntPool.get(81), "Filter Enveople Attack");

        v2ps_dest.put(IntPool.get(82), "FEnvDcy");
        v2ts_dest.put(IntPool.get(82), "Filter Enveople Decay");

        v2ps_dest.put(IntPool.get(83), "FEnvRls");
        v2ts_dest.put(IntPool.get(83), "Filter Enveople Release");

        v2ps_dest.put(IntPool.get(86), "FEnvTrig");
        v2ts_dest.put(IntPool.get(86), "Filter Enveople Trigger/Retrigger");

        v2ps_dest.put(IntPool.get(88), "AEnvRts");
        v2ts_dest.put(IntPool.get(88), "Aux Envelope Rates");

        v2ps_dest.put(IntPool.get(89), "AEnvAtk");
        v2ts_dest.put(IntPool.get(89), "Aux Envelope Attack");

        v2ps_dest.put(IntPool.get(90), "AEnvDcy");
        v2ts_dest.put(IntPool.get(90), "Aux Envelope Decay");

        v2ps_dest.put(IntPool.get(91), "AEnvRls");
        v2ts_dest.put(IntPool.get(91), "Aux Envelope Release");

        v2ps_dest.put(IntPool.get(94), "AEnvTrig");
        v2ts_dest.put(IntPool.get(94), "Aux Envelope Trigger/Retrigger");

        v2ps_dest.put(IntPool.get(96), "Lfo1Rt");
        v2ts_dest.put(IntPool.get(96), "Lfo 1 Rate");

        v2ps_dest.put(IntPool.get(97), "Lfo1Trig");
        v2ts_dest.put(IntPool.get(97), "Lfo 1 Trigger/Retrigger");

        v2ps_dest.put(IntPool.get(104), "Lfo2Rt");
        v2ts_dest.put(IntPool.get(104), "Lfo 2 Rate");

        v2ps_dest.put(IntPool.get(105), "Lfo2Trig");
        v2ts_dest.put(IntPool.get(105), "Lfo 2 Trigger/Retrigger");

        v2ps_dest.put(IntPool.get(106), "Lag0in");
        v2ts_dest.put(IntPool.get(106), "Lag 0 input");

        v2ps_dest.put(IntPool.get(108), "Lag1in");
        v2ts_dest.put(IntPool.get(108), "Lag 1 input");

        v2ps_dest.put(IntPool.get(161), "Sum");
        v2ts_dest.put(IntPool.get(161), "Summing Amp");

        v2ps_dest.put(IntPool.get(162), "Switch");

        v2ps_dest.put(IntPool.get(163), "Abs");
        v2ts_dest.put(IntPool.get(163), "Absolute Value");

        v2ps_dest.put(IntPool.get(164), "Diode");

        v2ps_dest.put(IntPool.get(165), "FlipFlop");

        v2ps_dest.put(IntPool.get(166), "Quant");
        v2ts_dest.put(IntPool.get(166), "Quantize");

        v2ps_dest.put(IntPool.get(167), "Gain 4X");

        v2ps_dest.put(IntPool.get(168), "C00Amt");
        v2ts_dest.put(IntPool.get(168), "Cord 0 Amount");

        v2ps_dest.put(IntPool.get(169), "C01Amt");
        v2ts_dest.put(IntPool.get(169), "Cord 1 Amount");

        v2ps_dest.put(IntPool.get(170), "C02Amt");
        v2ts_dest.put(IntPool.get(170), "Cord 2 Amount");

        v2ps_dest.put(IntPool.get(171), "C03Amt");
        v2ts_dest.put(IntPool.get(171), "Cord 3 Amount");

        v2ps_dest.put(IntPool.get(172), "C04Amt");
        v2ts_dest.put(IntPool.get(172), "Cord 4 Amount");

        v2ps_dest.put(IntPool.get(173), "C05Amt");
        v2ts_dest.put(IntPool.get(173), "Cord 5 Amount");

        v2ps_dest.put(IntPool.get(174), "C06Amt");
        v2ts_dest.put(IntPool.get(174), "Cord 6 Amount");

        v2ps_dest.put(IntPool.get(175), "C07Amt");
        v2ts_dest.put(IntPool.get(175), "Cord 7 Amount");

        v2ps_dest.put(IntPool.get(176), "C08Amt");
        v2ts_dest.put(IntPool.get(176), "Cord 8 Amount");

        v2ps_dest.put(IntPool.get(177), "C09Amt");
        v2ts_dest.put(IntPool.get(177), "Cord 9 Amount");

        v2ps_dest.put(IntPool.get(178), "C10Amt");
        v2ts_dest.put(IntPool.get(178), "Cord 10 Amount");

        v2ps_dest.put(IntPool.get(179), "C11Amt");
        v2ts_dest.put(IntPool.get(179), "Cord 11 Amount");

        v2ps_dest.put(IntPool.get(180), "C12Amt");
        v2ts_dest.put(IntPool.get(180), "Cord 12 Amount");

        v2ps_dest.put(IntPool.get(181), "C13Amt");
        v2ts_dest.put(IntPool.get(181), "Cord 13 Amount");

        v2ps_dest.put(IntPool.get(182), "C14Amt");
        v2ts_dest.put(IntPool.get(182), "Cord 14 Amount");

        v2ps_dest.put(IntPool.get(183), "C15Amt");
        v2ts_dest.put(IntPool.get(183), "Cord 15 Amount");

        v2ps_dest.put(IntPool.get(184), "C16Amt");
        v2ts_dest.put(IntPool.get(184), "Cord 16 Amount");

        v2ps_dest.put(IntPool.get(185), "C17Amt");
        v2ts_dest.put(IntPool.get(185), "Cord 17 Amount");

        /*
        // E4_VOICE_CORD0_SRC,        id = 129 (01h,01h)    min =    0;  max =  255
        load(129, "voice_cord0_src", "Source", v2ps_source, v2ts_source, null, null, "Cord0", null, true, 0, 255, 12);
        // E4_VOICE_CORD0_DST,        id = 130 (02h,01h)    min =    0;  max =  255
        load(130, "voice_cord0_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord0", null, true, 0, 255, 64);
        // E4_VOICE_CORD0_AMT,        id = 131 (03h,01h)    min = -100;  max = +100
        load(131, "voice_cord0_amt", "Amt", null, null, null, "%", "Cord0", null, true, -100, 100, 24);
        // E4_VOICE_CORD1_SRC,        id = 132 (04h,01h)    min =    0;  max =  255
        load(132, "voice_cord1_src", "Source", v2ps_source, v2ts_source, null, null, "Cord1", null, true, 0, 255, 16);
        // E4_VOICE_CORD1_DST,        id = 133 (05h,01h)    min =    0;  max =  255
        load(133, "voice_cord1_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord1", null, true, 0, 255, 48);
        // E4_VOICE_CORD1_AMT,        id = 134 (06h,01h)    min = -100;  max = +100
        load(134, "voice_cord1_amt", "Amt", null, null, null, "%", "Cord1", null, true, -100, 100, 6);
        // E4_VOICE_CORD2_SRC,        id = 135 (07h,01h)    min =    0;  max =  255
        load(135, "voice_cord2_src", "Source", v2ps_source, v2ts_source, null, null, "Cord2", null, true, 0, 255, 96);
        // E4_VOICE_CORD2_DST,        id = 136 (08h,01h)    min =    0;  max =  255
        load(136, "voice_cord2_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord2", null, true, 0, 255, 48);
        // E4_VOICE_CORD2_AMT,        id = 137 (09h,01h)    min = -100;  max = +100
        load(137, "voice_cord2_amt", "Amt", null, null, null, "%", "Cord2", null, true, -100, 100, 0);
        // E4_VOICE_CORD3_SRC,        id = 138 (0Ah,01h)    min =    0;  max =  255
        load(138, "voice_cord3_src", "Source", v2ps_source, v2ts_source, null, null, "Cord3", null, true, 0, 255, 17);
        // E4_VOICE_CORD3_DST,        id = 139 (0Bh,01h)    min =    0;  max =  255
        load(139, "voice_cord3_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord3", null, true, 0, 255, 170);
        // E4_VOICE_CORD3_AMT,        id = 140 (0Ch,01h)    min = -100;  max = +100
        load(140, "voice_cord3_amt", "Amt", null, null, null, "%", "Cord3", null, true, -100, 100, 13);
        // E4_VOICE_CORD4_SRC,        id = 141 (0Dh,01h)    min =    0;  max =  255
        load(141, "voice_cord4_src", "Source", v2ps_source, v2ts_source, null, null, "Cord4", null, true, 0, 255, 12);
        // E4_VOICE_CORD4_DST,        id = 142 (0Eh,01h)    min =    0;  max =  255
        load(142, "voice_cord4_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord4", null, true, 0, 255, 56);
        // E4_VOICE_CORD4_AMT,        id = 143 (0Fh,01h)    min = -100;  max = +100
        load(143, "voice_cord4_amt", "Amt", null, null, null, "%", "Cord4", null, true, -100, 100, 0);
        // E4_VOICE_CORD5_SRC,        id = 144 (10h,01h)    min =    0;  max =  255
        load(144, "voice_cord5_src", "Source", v2ps_source, v2ts_source, null, null, "Cord5", null, true, 0, 255, 80);
        // E4_VOICE_CORD5_DST,        id = 145 (11h,01h)    min =    0;  max =  255
        load(145, "voice_cord5_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord5", null, true, 0, 255, 56);
        // E4_VOICE_CORD5_AMT,        id = 146 (12h,01h)    min = -100;  max = +100
        load(146, "voice_cord5_amt", "Amt", null, null, null, "%", "Cord5", null, true, -100, 100, 0);
        // E4_VOICE_CORD6_SRC,        id = 147 (13h,01h)    min =    0;  max =  255
        load(147, "voice_cord6_src", "Source", v2ps_source, v2ts_source, null, null, "Cord6", null, true, 0, 255, 8);
        // E4_VOICE_CORD6_DST,        id = 148 (14h,01h)    min =    0;  max =  255
        load(148, "voice_cord6_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord6", null, true, 0, 255, 56);
        // E4_VOICE_CORD6_AMT,        id = 149 (15h,01h)    min = -100;  max = +100
        load(149, "voice_cord6_amt", "Amt", null, null, null, "%", "Cord6", null, true, -100, 100, 0);
        // E4_VOICE_CORD7_SRC,        id = 150 (16h,01h)    min =    0;  max =  255
        load(150, "voice_cord7_src", "Source", v2ps_source, v2ts_source, null, null, "Cord7", null, true, 0, 255, 22);
        // E4_VOICE_CORD7_DST,        id = 151 (17h,01h)    min =    0;  max =  255
        load(151, "voice_cord7_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord7", null, true, 0, 255, 8);
        // E4_VOICE_CORD7_AMT,        id = 152 (18h,01h)    min = -100;  max = +100
        load(152, "voice_cord7_amt", "Amt", null, null, null, "%", "Cord7", null, true, -100, 100, 100);
        // E4_VOICE_CORD8_SRC,        id = 153 (19h,01h)    min =    0;  max =  255
        load(153, "voice_cord8_src", "Source", v2ps_source, v2ts_source, null, null, "Cord8", null, true, 0, 255, 0);
        // E4_VOICE_CORD8_DST,        id = 154 (1Ah,01h)    min =    0;  max =  255
        load(154, "voice_cord8_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord8", null, true, 0, 255, 0);
        // E4_VOICE_CORD8_AMT,        id = 155 (1Bh,01h)    min = -100;  max = +100
        load(155, "voice_cord8_amt", "Amt", null, null, null, "%", "Cord8", null, true, -100, 100, 0);
        // E4_VOICE_CORD9_SRC,        id = 156 (1Ch,01h)    min =    0;  max =  255
        load(156, "voice_cord9_src", "Source", v2ps_source, v2ts_source, null, null, "Cord9", null, true, 0, 255, 0);
        // E4_VOICE_CORD9_DST,        id = 157 (1Dh,01h)    min =    0;  max =  255
        load(157, "voice_cord9_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord9", null, true, 0, 255, 0);
        // E4_VOICE_CORD9_AMT,        id = 158 (1Eh,01h)    min = -100;  max = +100
        load(158, "voice_cord9_amt", "Amt", null, null, null, "%", "Cord9", null, true, -100, 100, 0);
        // E4_VOICE_CORD10_SRC,       id = 159 (1Fh,01h)    min =    0;  max =  255
        load(159, "voice_cord10_src", "Source", v2ps_source, v2ts_source, null, null, "Cord10", null, true, 0, 255, 0);
        // E4_VOICE_CORD10_DST,       id = 160 (20h,01h)    min =    0;  max =  255
        load(160, "voice_cord10_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord10", null, true, 0, 255, 0);
        // E4_VOICE_CORD10_AMT,       id = 161 (21h,01h)    min = -100;  max = +100
        load(161, "voice_cord10_amt", "Amt", null, null, null, "%", "Cord10", null, true, -100, 100, 0);
        // E4_VOICE_CORD11_SRC,       id = 162 (22h,01h)    min =    0;  max =  255
        load(162, "voice_cord11_src", "Source", v2ps_source, v2ts_source, null, null, "Cord11", null, true, 0, 255, 0);
        // E4_VOICE_CORD11_DST,       id = 163 (23h,01h)    min =    0;  max =  255
        load(163, "voice_cord11_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord11", null, true, 0, 255, 0);
        // E4_VOICE_CORD11_AMT,       id = 164 (24h,01h)    min = -100;  max = +100
        load(164, "voice_cord11_amt", "Amt", null, null, null, "%", "Cord11", null, true, -100, 100, 0);
        // E4_VOICE_CORD12_SRC,       id = 165 (25h,01h)    min =    0;  max =  255
        load(165, "voice_cord12_src", "Source", v2ps_source, v2ts_source, null, null, "Cord12", null, true, 0, 255, 0);
        // E4_VOICE_CORD12_DST,       id = 166 (26h,01h)    min =    0;  max =  255
        load(166, "voice_cord12_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord12", null, true, 0, 255, 0);
        // E4_VOICE_CORD12_AMT,       id = 167 (27h,01h)    min = -100;  max = +100
        load(167, "voice_cord12_amt", "Amt", null, null, null, "%", "Cord12", null, true, -100, 100, 0);
        // E4_VOICE_CORD13_SRC,       id = 168 (28h,01h)    min =    0;  max =  255
        load(168, "voice_cord13_src", "Source", v2ps_source, v2ts_source, null, null, "Cord13", null, true, 0, 255, 0);
        // E4_VOICE_CORD13_DST,       id = 169 (29h,01h)    min =    0;  max =  255
        load(169, "voice_cord13_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord13", null, true, 0, 255, 0);
        // E4_VOICE_CORD13_AMT,       id = 170 (2Ah,01h)    min = -100;  max = +100
        load(170, "voice_cord13_amt", "Amt", null, null, null, "%", "Cord13", null, true, -100, 100, 0);
        // E4_VOICE_CORD14_SRC,       id = 171 (2Bh,01h)    min =    0;  max =  255
        load(171, "voice_cord14_src", "Source", v2ps_source, v2ts_source, null, null, "Cord14", null, true, 0, 255, 0);
        // E4_VOICE_CORD14_DST,       id = 172 (2Ch,01h)    min =    0;  max =  255
        load(172, "voice_cord14_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord14", null, true, 0, 255, 0);
        // E4_VOICE_CORD14_AMT,       id = 173 (2Dh,01h)    min = -100;  max = +100
        load(173, "voice_cord14_amt", "Amt", null, null, null, "%", "Cord14", null, true, -100, 100, 0);
        // E4_VOICE_CORD15_SRC,       id = 174 (2Eh,01h)    min =    0;  max =  255
        load(174, "voice_cord15_src", "Source", v2ps_source, v2ts_source, null, null, "Cord15", null, true, 0, 255, 0);
        // E4_VOICE_CORD15_DST,       id = 175 (2Fh,01h)    min =    0;  max =  255
        load(175, "voice_cord15_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord15", null, true, 0, 255, 0);
        // E4_VOICE_CORD15_AMT,       id = 176 (30h,01h)    min = -100;  max = +100
        load(176, "voice_cord15_amt", "Amt", null, null, null, "%", "Cord15", null, true, -100, 100, 0);
        // E4_VOICE_CORD16_SRC,       id = 177 (31h,01h)    min =    0;  max =  255
        load(177, "voice_cord16_src", "Source", v2ps_source, v2ts_source, null, null, "Cord16", null, true, 0, 255, 0);
        // E4_VOICE_CORD16_DST,       id = 178 (32h,01h)    min =    0;  max =  255
        load(178, "voice_cord16_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord16", null, true, 0, 255, 0);
        // E4_VOICE_CORD16_AMT,       id = 179 (33h,01h)    min = -100;  max = +100
        load(179, "voice_cord16_amt", "Amt", null, null, null, "%", "Cord16", null, true, -100, 100, 0);
        // E4_VOICE_CORD17_SRC,       id = 180 (34h,01h)    min =    0;  max =  255
        load(180, "voice_cord17_src", "Source", v2ps_source, v2ts_source, null, null, "Cord17", null, true, 0, 255, 0);
        // E4_VOICE_CORD17_DST,       id = 181 (35h,01h)    min =    0;  max =  255
        load(181, "voice_cord17_dst", "Dest", v2ps_dest, v2ts_dest, null, null, "Cord17", null, true, 0, 255, 0);
        // E4_VOICE_CORD17_AMT,       id = 182 (36h,01h)    min = -100;  max = +100
        load(182, "voice_cord17_amt", "Amt", null, null, null, "%", "Cord17", null, true, -100, 100, 0);
        */

        // E4_VOICE_CORD0_SRC,        id = 129 (01h,01h)    min =    0;  max =  255
        load(129, "voice_cord0_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 12);
        // E4_VOICE_CORD0_DST,        id = 130 (02h,01h)    min =    0;  max =  255
        load(130, "voice_cord0_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 64);
        // E4_VOICE_CORD0_AMT,        id = 131 (03h,01h)    min = -100;  max = +100
        load(131, "voice_cord0_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 24);
        // E4_VOICE_CORD1_SRC,        id = 132 (04h,01h)    min =    0;  max =  255
        load(132, "voice_cord1_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 16);
        // E4_VOICE_CORD1_DST,        id = 133 (05h,01h)    min =    0;  max =  255
        load(133, "voice_cord1_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 48);
        // E4_VOICE_CORD1_AMT,        id = 134 (06h,01h)    min = -100;  max = +100
        load(134, "voice_cord1_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 6);
        // E4_VOICE_CORD2_SRC,        id = 135 (07h,01h)    min =    0;  max =  255
        load(135, "voice_cord2_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 96);
        // E4_VOICE_CORD2_DST,        id = 136 (08h,01h)    min =    0;  max =  255
        load(136, "voice_cord2_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 48);
        // E4_VOICE_CORD2_AMT,        id = 137 (09h,01h)    min = -100;  max = +100
        load(137, "voice_cord2_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD3_SRC,        id = 138 (0Ah,01h)    min =    0;  max =  255
        load(138, "voice_cord3_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 17);
        // E4_VOICE_CORD3_DST,        id = 139 (0Bh,01h)    min =    0;  max =  255
        load(139, "voice_cord3_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 170);
        // E4_VOICE_CORD3_AMT,        id = 140 (0Ch,01h)    min = -100;  max = +100
        load(140, "voice_cord3_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 13);
        // E4_VOICE_CORD4_SRC,        id = 141 (0Dh,01h)    min =    0;  max =  255
        load(141, "voice_cord4_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 12);
        // E4_VOICE_CORD4_DST,        id = 142 (0Eh,01h)    min =    0;  max =  255
        load(142, "voice_cord4_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 56);
        // E4_VOICE_CORD4_AMT,        id = 143 (0Fh,01h)    min = -100;  max = +100
        load(143, "voice_cord4_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD5_SRC,        id = 144 (10h,01h)    min =    0;  max =  255
        load(144, "voice_cord5_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 80);
        // E4_VOICE_CORD5_DST,        id = 145 (11h,01h)    min =    0;  max =  255
        load(145, "voice_cord5_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 56);
        // E4_VOICE_CORD5_AMT,        id = 146 (12h,01h)    min = -100;  max = +100
        load(146, "voice_cord5_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD6_SRC,        id = 147 (13h,01h)    min =    0;  max =  255
        load(147, "voice_cord6_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 8);
        // E4_VOICE_CORD6_DST,        id = 148 (14h,01h)    min =    0;  max =  255
        load(148, "voice_cord6_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 56);
        // E4_VOICE_CORD6_AMT,        id = 149 (15h,01h)    min = -100;  max = +100
        load(149, "voice_cord6_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD7_SRC,        id = 150 (16h,01h)    min =    0;  max =  255
        load(150, "voice_cord7_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 22);
        // E4_VOICE_CORD7_DST,        id = 151 (17h,01h)    min =    0;  max =  255
        load(151, "voice_cord7_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 8);
        // E4_VOICE_CORD7_AMT,        id = 152 (18h,01h)    min = -100;  max = +100
        load(152, "voice_cord7_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 100);
        // E4_VOICE_CORD8_SRC,        id = 153 (19h,01h)    min =    0;  max =  255
        load(153, "voice_cord8_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD8_DST,        id = 154 (1Ah,01h)    min =    0;  max =  255
        load(154, "voice_cord8_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD8_AMT,        id = 155 (1Bh,01h)    min = -100;  max = +100
        load(155, "voice_cord8_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD9_SRC,        id = 156 (1Ch,01h)    min =    0;  max =  255
        load(156, "voice_cord9_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD9_DST,        id = 157 (1Dh,01h)    min =    0;  max =  255
        load(157, "voice_cord9_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD9_AMT,        id = 158 (1Eh,01h)    min = -100;  max = +100
        load(158, "voice_cord9_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD10_SRC,       id = 159 (1Fh,01h)    min =    0;  max =  255
        load(159, "voice_cord10_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD10_DST,       id = 160 (20h,01h)    min =    0;  max =  255
        load(160, "voice_cord10_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD10_AMT,       id = 161 (21h,01h)    min = -100;  max = +100
        load(161, "voice_cord10_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD11_SRC,       id = 162 (22h,01h)    min =    0;  max =  255
        load(162, "voice_cord11_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD11_DST,       id = 163 (23h,01h)    min =    0;  max =  255
        load(163, "voice_cord11_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD11_AMT,       id = 164 (24h,01h)    min = -100;  max = +100
        load(164, "voice_cord11_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD12_SRC,       id = 165 (25h,01h)    min =    0;  max =  255
        load(165, "voice_cord12_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD12_DST,       id = 166 (26h,01h)    min =    0;  max =  255
        load(166, "voice_cord12_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD12_AMT,       id = 167 (27h,01h)    min = -100;  max = +100
        load(167, "voice_cord12_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD13_SRC,       id = 168 (28h,01h)    min =    0;  max =  255
        load(168, "voice_cord13_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD13_DST,       id = 169 (29h,01h)    min =    0;  max =  255
        load(169, "voice_cord13_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD13_AMT,       id = 170 (2Ah,01h)    min = -100;  max = +100
        load(170, "voice_cord13_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD14_SRC,       id = 171 (2Bh,01h)    min =    0;  max =  255
        load(171, "voice_cord14_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD14_DST,       id = 172 (2Ch,01h)    min =    0;  max =  255
        load(172, "voice_cord14_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD14_AMT,       id = 173 (2Dh,01h)    min = -100;  max = +100
        load(173, "voice_cord14_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD15_SRC,       id = 174 (2Eh,01h)    min =    0;  max =  255
        load(174, "voice_cord15_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD15_DST,       id = 175 (2Fh,01h)    min =    0;  max =  255
        load(175, "voice_cord15_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD15_AMT,       id = 176 (30h,01h)    min = -100;  max = +100
        load(176, "voice_cord15_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD16_SRC,       id = 177 (31h,01h)    min =    0;  max =  255
        load(177, "voice_cord16_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD16_DST,       id = 178 (32h,01h)    min =    0;  max =  255
        load(178, "voice_cord16_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD16_AMT,       id = 179 (33h,01h)    min = -100;  max = +100
        load(179, "voice_cord16_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);
        // E4_VOICE_CORD17_SRC,       id = 180 (34h,01h)    min =    0;  max =  255
        load(180, "voice_cord17_src", "Source", v2ps_source, v2ts_source, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD17_DST,       id = 181 (35h,01h)    min =    0;  max =  255
        load(181, "voice_cord17_dst", "Dest", v2ps_dest, v2ts_dest, null, null, VOICE_CORDS, null, false, 0, 255, 0);
        // E4_VOICE_CORD17_AMT,       id = 182 (36h,01h)    min = -100;  max = +100
        load(182, "voice_cord17_amt", "Amt", null, null, null, "%", VOICE_CORDS, null, true, -100, 100, 0);

        // MASTER_TUNING_OFFSET,         id = 183 (37h,01h)    min = -64;  max = +64
        v2ps = createStringForValueMap();
        double[] mto_v2ps = new double[]{0.0, 1.2, 3.5, 4.7, 6.0, 7.2, 9.5, 10.7,
                                         12.0, 14.2, 15.5, 17.7, 18.0, 20.2, 21.5, 23.7,
                                         25.0, 26.2, 28.5, 29.7, 31.0, 32.2, 34.5, 35.7, 37.0,
                                         39.2, 40.5, 42.7, 43.0, 45.2, 46.5, 48.7, 50.0,
                                         51.2, 53.5, 54.7, 56.0, 57.2, 59.5, 60.7, 62.0,
                                         64.2, 65.5, 67.7, 68.0, 70.2, 71.5, 73.7, 75.0,
                                         76.2, 78.5, 79.7, 81.0, 82.2, 84.5, 85.7, 87.0, 89.2,
                                         90.5, 92.7, 93.0, 95.2, 96.5, 98.7, 100.0};
        for (int i = 0; i < 65; i++)
            v2ps.put(IntPool.get(i), Double.toString(mto_v2ps[i]));

        for (int i = 64; i > 0; i--)
            v2ps.put(IntPool.get(-i), Double.toString(-mto_v2ps[i]));

        load(183, "master_tuning_offset", "Tuning Offset", v2ps, null, null, null, MASTER_TUNING, null, false, -64, 64, 0);

        // MASTER_TRANSPOSE,             id = 184 (38h,01h)    min = -12;  max = +12
        v2ps = createStringForValueMap();
        String[] mt_v2ps = new String[]{"Off", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C"};

        for (int i = 0; i <= 12; i++)
            v2ps.put(IntPool.get(i), mt_v2ps[i]);
        for (int i = 12; i > 0; i--)
            v2ps.put(IntPool.get(-i), "-" + mt_v2ps[i]);

        load(184, "master_xpose", "Transpose", v2ps, null, null, null, MASTER_TUNING, null, false, -12, 12, 0);

        // MASTER_HEADROOM,              id = 185 (39h,01h)    min = 0;  max = 15
        load(185, "master_headroom", "Headroom", null, null, null, null, MASTER_IO, null, true, 0, 15, 4);

        // MASTER_HCHIP_BOOST,           id = 186 (3Ah,01h)    min = 0;  max = 1
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "+0");
        v2ps.put(IntPool.get(1), "+12");
        load(186, "master_boost", "Output Boost", v2ps, null, null, "dB", MASTER_IO, null, false, 0, 1, 0);

        // REQUIRES MMD CHECK
        // MASTER_OUTPUT_FORMAT,         id = 187 (3Bh,01h)    min = 0;  max = 2
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "analog");
        v2ps.put(IntPool.get(1), "AES Pro");
        v2ps.put(IntPool.get(2), "S/PDIF");
        load(187, "master_output_format", "Output Format", v2ps, null, null, null, MASTER_IO, null, false);

        // MASTER_OUTPUT_CLOCK,          id = 188 (3Ch,01h)    min = 0;  max = 1
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "44.1");
        v2ps.put(IntPool.get(1), "48");
        load(188, "master_output_clock", "Output Clock", v2ps, null, null, "KHz", MASTER_IO, null, false, 0, 1, 0);

        // REQUIRES MMD CHECK
        // MASTER_AES_BOOST,             id = 189 (3Dh,01h)    min = 0;  max = 1
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Off");
        v2ps.put(IntPool.get(1), "On");
        load(189, "master_aes_boost", "AES Boost", v2ps, null, null, null, MASTER_IO, null, false, 0, 1, 0);

        // MASTER_SCSI_ID,               id = 190 (3Eh,01h)    min = 0;  max = 7
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "none");
        v2ps.put(IntPool.get(0), "ID 0");
        v2ps.put(IntPool.get(1), "ID 1");
        v2ps.put(IntPool.get(2), "ID 2");
        v2ps.put(IntPool.get(3), "ID 3");
        v2ps.put(IntPool.get(4), "ID 4");
        v2ps.put(IntPool.get(5), "ID 5");
        v2ps.put(IntPool.get(6), "ID 6");
        v2ps.put(IntPool.get(7), "ID 7");
        load(190, "master_scsi_id", "SCSI Id", v2ps, null, null, null, MASTER_SCSI_DISK, null, false, 0, 7, 0);

        // MASTER_SCSI_TERM,             id = 191 (3Fh,01h)    min = 0;  max = 1
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Off");
        v2ps.put(IntPool.get(1), "On");
        load(191, "master_scsi_term", "SCSI Termination", v2ps, null, null, null, MASTER_SCSI_DISK, null, false, 0, 1, 0);

        // MASTER_USING_MAC,             id = 192 (40h,01h)    min = -1;  max = 7
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "none");
        v2ps.put(IntPool.get(0), "ID 0");
        v2ps.put(IntPool.get(1), "ID 1");
        v2ps.put(IntPool.get(2), "ID 2");
        v2ps.put(IntPool.get(3), "ID 3");
        v2ps.put(IntPool.get(4), "ID 4");
        v2ps.put(IntPool.get(5), "ID 5");
        v2ps.put(IntPool.get(6), "ID 6");
        v2ps.put(IntPool.get(7), "ID 7 (Mac)");
        load(192, "master_avoid_scsi_id", "Avoid Host on ID", v2ps, null, null, null, MASTER_SCSI_DISK, null, false, -1, 7, -1);

        // MASTER_COMBINE_LR,            id = 193 (41h,01h)    min = 0;  max = 1
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "On");
        v2ps.put(IntPool.get(1), "Off");
        load(193, "master_combine_lr", "Comb L/R to stereo", v2ps, null, null, null, MASTER_IMPORT_OPTIONS, null, false, 0, 1, 0);

        // MASTER_AKAI_LOOP_ADJ,         id = 194 (42h,01h)    min = 0;  max = 1
        load(194, "master_akai_loop_adj", "Adj Akai/Ensoniq F/Loops", v2ps, null, null, null, MASTER_IMPORT_OPTIONS, null, false, 0, 1, 0);

        // MASTER_AKAI_SAMPLER_ID,       id = 195 (43h,01h)    min = -1;  max = 7
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "none");
        v2ps.put(IntPool.get(0), "ID 0");
        v2ps.put(IntPool.get(1), "ID 1");
        v2ps.put(IntPool.get(2), "ID 2");
        v2ps.put(IntPool.get(3), "ID 3");
        v2ps.put(IntPool.get(4), "ID 4");
        v2ps.put(IntPool.get(5), "ID 5");
        v2ps.put(IntPool.get(6), "ID 6");
        v2ps.put(IntPool.get(7), "ID 7");
        load(195, "master_foreign_sampler_id", "Foreign/Sampler ID", v2ps, null, null, null, MASTER_IMPORT_OPTIONS, null, false, -1, 7, -1);

        // MIDIGLO_BASIC_CHANNEL,        id = 198 (46h,01h)    min = 0;  max = 15(31 if has MIDI expansion card)
        v2ps = createStringForValueMap();
        for (int i = 0; i < 16; i++)
            v2ps.put(IntPool.get(i), Integer.toString(i + 1));

        load(198, "midiglobal_basic_ch", "Basic Channel", v2ps, null, null, null, MASTER_MIDI_MODE, null, false, 0, 15, 0);

        // MIDIGLO_MIDI_MODE,            id = 199 (47h,01h)    min = 0;  max = 2
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Omni");
        v2ps.put(IntPool.get(1), "Poly");
        v2ps.put(IntPool.get(2), "Multi");
        load(199, "midiglobal_midi_mode", "Midi Mode", v2ps, null, null, null, MASTER_MIDI_MODE, null, false, 0, 2, 2);

        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "Off");
        for (int i = 0; i < 32; i++)
            v2ps.put(IntPool.get(i), "cc" + Integer.toString(i));
        v2ps.put(IntPool.get(32), "ptwheel");
        v2ps.put(IntPool.get(33), "chnpress");

        // MIDIGLO_PITCH_CONTROL,        id = 201 (49h,01h)    min = -1;  max = 33
        load(201, "midiglobal_pitch_control", "Pitch Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false, -1, 33, 32);
        // MIDIGLO_MOD_CONTROL,          id = 202 (4Ah,01h)    min = -1;  max = 33
        load(202, "midiglobal_mod_control", "Modulation Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false, -1, 33, 1);
        // MIDIGLO_PRESSURE_CONTROL,     id = 203 (4Bh,01h)    min = -1;  max = 33
        load(203, "midiglobal_pressure_control", "Pressure Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false, -1, 33, 33);
        // MIDIGLO_PEDAL_CONTROL,        id = 204 (4Ch,01h)    min = -1;  max = 33
        load(204, "midiglobal_pedal_control", "Pedal Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false, -1, 33, 3);

        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "Off");
        for (int i = 0; i < 33; i++)
            v2ps.put(IntPool.get(i), Integer.toString(i + 64));

        // MIDIGLO_SWITCH_1_CONTROL,     id = 205 (4Dh,01h)    min = -1;  max = 33
        load(205, "midiglobal_switch1_control", "Switch1 Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false, -1, 33, 0);
        // MIDIGLO_SWITCH_2_CONTROL,     id = 206 (4Eh,01h)    min = -1;  max = 33
        load(206, "midiglobal_switch2_control", "Switch2 Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false, -1, 33, 1);
        // MIDIGLO_THUMB_CONTROL,        id = 207 (4Fh,01h)    min = -1;  max = 33
        load(207, "midiglobal_switch3_control", "Switch3 Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false, -1, 33, 2);

        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "Off");
        for (int i = 0; i < 32; i++)
            v2ps.put(IntPool.get(i), "cc" + Integer.toString(i));
        v2ps.put(IntPool.get(32), "ptwheel");
        v2ps.put(IntPool.get(33), "chnpress");

        // REQUIRES MMD CHECK
        // MIDIGLO_MIDI_A_CONTROL,       id = 208 (50h,01h)    min = -1;  max = 33
        load(208, "midiglobal_midia_control", "MidiA Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false);
        // REQUIRES MMD CHECK
        // MIDIGLO_MIDI_B_CONTROL,       id = 209 (51h,01h)    min = -1;  max = 33
        load(209, "midiglobal_midib_control", "MidiB Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false);
        // REQUIRES MMD CHECK
        // MIDIGLO_MIDI_C_CONTROL,       id = 210 (52h,01h)    min = -1;  max = 33
        load(210, "midiglobal_midic_control", "MidiC Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false);
        // REQUIRES MMD CHECK
        // MIDIGLO_MIDI_D_CONTROL,       id = 211 (53h,01h)    min = -1;  max = 33
        load(211, "midiglobal_midid_control", "MidiD Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false);
        // REQUIRES MMD CHECK
        // MIDIGLO_MIDI_E_CONTROL,       id = 212 (54h,01h)    min = -1;  max = 33
        load(212, "midiglobal_midie_control", "MidiE Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false);
        // REQUIRES MMD CHECK
        // MIDIGLO_MIDI_F_CONTROL,       id = 213 (55h,01h)    min = -1;  max = 33
        load(213, "midiglobal_midif_control", "MidiF Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false);
        // REQUIRES MMD CHECK
        // MIDIGLO_MIDI_G_CONTROL,       id = 214 (56h,01h)    min = -1;  max = 33
        load(214, "midiglobal_midig_control", "MidiG Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false);
        // REQUIRES MMD CHECK
        // MIDIGLO_MIDI_H_CONTROL,       id = 215 (57h,01h)    min = -1;  max = 33
        load(215, "midiglobal_midih_control", "MidiH Control", v2ps, null, null, null, MASTER_MIDI_CONTROLLERS, null, false);

        // REQUIRES MMD CHECK
        // MIDIGLO_VEL_CURVE,            id = 216 (58h,01h)    min = 0;  max = 13
        // TODO!! handle imageForValueMap for this parameter2
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "linear");
        for (int i = 1; i < 14; i++)
            v2ps.put(IntPool.get(i), Integer.toString(i));
        load(216, "midiglobal_vel_curve", "Velocity Curve", v2ps, null, null, null, MASTER_MIDI_PREFERENCES, null, false);

        // MIDIGLO_VOLUME_SENSITIVITY,   id = 217 (59h,01h)    min = 0;  max = 31
        load(217, "midiglobal_vel_sensitivity", "Ctrl#7(Vol) Sensitivity", null, null, null, null, MASTER_MIDI_PREFERENCES, null, false, 0, 31, 31);

        // MIDIGLO_CTRL7_CURVE,          id = 218 (5Ah,01h)    min = 0;  max = 2
        // TODO!! handle imageForValueMap for this parameter2
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "linear");
        v2ps.put(IntPool.get(1), "squared");
        v2ps.put(IntPool.get(2), "logarithmic");
        load(218, "midiglobal_vol_curve", "Ctrl#7(Vol) Curve", v2ps, null, null, null, MASTER_MIDI_PREFERENCES, null, false, 0, 2, 0);

        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "off");
        v2ps.put(IntPool.get(1), "on");

        // MIDIGLO_PEDAL_OVERRIDE,       id = 219 (5Bh,01h)    min = 0;  max = 1
        load(219, "midiglobal_pedal_override", "Pedal Override", v2ps, null, null, null, MASTER_MIDI_PREFERENCES, null, false, 0, 1, 0);
        // MIDIGLO_RCV_PROGRAM_CHANGE,   id = 220 (5Ch,01h)    min = 0;  max = 1
        load(220, "midiglobal_rcv_prg_change", "Recv Program Change", v2ps, null, null, null, MASTER_MIDI_PREFERENCES, null, false, 0, 1, 1);
        // MIDIGLO_SEND_PROGRAM_CHANGE,  id = 221 (5Dh,01h)    min = 0;  max = 1
        load(221, "midiglobal_send_prg_change", "Send Program Change", v2ps, null, null, null, MASTER_MIDI_PREFERENCES, null, false, 0, 1, 1);

        // MIDIGLO_MAGIC_PRESET,         id = 222 (5Eh,01h)    min = 0;  max = 128
        DecimalFormat df = new DecimalFormat("000");
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "off");
        for (int i = 1; i < 129; i++)
            v2ps.put(IntPool.get(i), "P" + df.format(i - 1));
        load(222, "midiglobal_magic_preset", "Magic Preset", v2ps, null, null, null, MASTER_MIDI_PREFERENCES, null, false, 0, 128, 0);

        // NOTE: ids 223-227 used for selection of preset, group, voice, link or zone

        // MASTER_FX_A_ALGORITHM,     id = 228 (64h,01h)   min = 0;  max =  44;  default = 14;
        // NOTE: v2ps_fxa already created previously for preset effect ids
        load(228, "master_fxa", "Effect A", v2ps_fxa, null, null, null, MASTER_FX_A, null, false, 1, 44, 14);
        // MASTER_FX_A_PARM_0,        id = 229 (65h,01h)   min = 0;  max =  90;  default = 54;
        load(229, "master_fxa_decay", "Decay", null, null, null, null, MASTER_FX_A, null, true, 0, 90, 54);
        // MASTER_FX_A_PARM_1,        id = 230 (66h,01h)   min = 0;  max = 127;  default = 64;
        load(230, "master_fxa_hfdamp", "Damping", null, null, null, null, MASTER_FX_A, null, true, 0, 127, 64);
        // MASTER_FX_A_PARM_2,        id = 231 (67h,01h)   min = 0;  max = 127;  default = 0;
        load(231, "master_fxa_b2a", "FxB->FxA", null, null, null, null, MASTER_FX_A, null, true, 0, 127, 0);
        // MASTER_FX_A_AMT_0,         id = 232 (68h,01h)   min = 0;  max = 100;  default = 10;
        load(232, "master_fxa_amt0", "Main Send", null, null, null, "%", MASTER_FX_A, null, true, 0, 100, 0);
        // MASTER_FX_A_AMT_1,         id = 233 (69h,01h)   min = 0;  max = 100;  default = 20;
        load(233, "master_fxa_amt1", "Sub1 Send", null, null, null, "%", MASTER_FX_A, null, true, 0, 100, 20);
        // MASTER_FX_A_AMT_2,         id = 234 (6Ah,01h)   min = 0;  max = 100;  default = 30;
        load(234, "master_fxa_amt2", "Sub2 Send", null, null, null, "%", MASTER_FX_A, null, true, 0, 100, 30);
        // MASTER_FX_A_AMT_3,         id = 235 (6Bh,01h)   min = 0;  max = 100;  default = 40;
        load(235, "master_fxa_amt3", "Sub3 Send", null, null, null, "%", MASTER_FX_A, null, true, 0, 100, 40);


        // MASTER_FX_B_ALGORITHM,     id = 236 (6Ch,01h)   min = 0;  max =  27;  default = 1;
        // NOTE: v2ps_fxb already created previously for preset effect ids
        load(236, "master_fxb", "Effect B", v2ps_fxb, null, null, null, MASTER_FX_B, null, false, 1, 32, 1);
        // MASTER_FX_B_PARM_0,        id = 237 (6Dh,01h)   min = 0;  max = 127;  default = 0;
        load(237, "master_fxb_feedback", "Feedback", null, null, null, null, MASTER_FX_B, null, true, 0, 127, 0);
        // MASTER_FX_B_PARM_1,        id = 238 (6Eh,01h)   min = 0;  max = 127;  default = 3;
        load(238, "master_fxb_lforate", "LFO Rate", null, null, null, null, MASTER_FX_B, null, true, 0, 127, 3);
        // MASTER_FX_B_PARM_2,        id = 239 (6Fh,01h)   min = 0;  max = 127;  default = 0;
        v2ps = createStringForValueMap();
        for (int i = 0; i < 128; i++)
            v2ps.put(IntPool.get(i), Integer.toString(i * 5));
        load(239, "master_fxb_delay", "Delay", v2ps, null, null, " ms", MASTER_FX_B, null, true, 0, 127, 0);
        // MASTER_FX_B_AMT_0,         id = 240 (70h,01h)   min = 0;  max = 100;  default = 10;
        load(240, "master_fxb_amt0", "Main Send", null, null, null, "%", MASTER_FX_B, null, true, 0, 100, 10);
        // MASTER_FX_B_AMT_1,         id = 241 (71h,01h)   min = 0;  max = 100;  default = 15;
        load(241, "master_fxb_amt1", "Sub1 Send", null, null, null, "%", MASTER_FX_B, null, true, 0, 100, 15);
        // MASTER_FX_B_AMT_2,         id = 242 (72h,01h)   min = 0;  max = 100;  default = 30;
        load(242, "master_fxb_amt2", "Sub2 Send", null, null, null, "%", MASTER_FX_B, null, true, 0, 100, 30);
        // MASTER_FX_B_AMT_3,         id = 243 (73h,01h)   min = 0;  max = 100;  default = 0;
        load(243, "master_fxb_amt3", "Sub3 Send", null, null, null, "%", MASTER_FX_B, null, true, 0, 100, 0);

        // MASTER_FX_BYPASS,          id = 244 (74h,01h)   min =  0;  max =  1;  default =  0;
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Off");
        v2ps.put(IntPool.get(1), "On");
        load(244, "master_fx_bypass", "Effect Bypass", v2ps, null, null, null, MASTER_FX_SETUP, null, true, 0, 1, 0);

        // MASTER_FX_MM_CTRL_CHANNEL, id = 245 (75h,01h)   min = -1;  max = 15;  default = -1;
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(-1), "Master Settings");
        for (int i = 0; i < 16; i++)
            v2ps.put(IntPool.get(i), "Preset on ch " + Integer.toString(i + 1));
        load(245, "master_fx_mm_channel", "Multimode FX Control", v2ps, null, null, null, MASTER_FX_SETUP, null, false, -1, 15, -1);

        // NOTE - this may not be suitable for display??
        // it is similar to the selection ids??
        // MULTIMODE_CHANNEL,         id = 246 (76h,01h)   min =   1;  max = 16(32);     default =   1;
        //load( 246, "multimode_channel", "Multimode Channel", null, null, null );

        // REQUIRES MMD CHECK
        // MULTIMODE_PRESET,          id = 247 (77h,01h)   min =  -1;  max = 999(1255);  default =  -1;
        load(247, "multimode_preset", "Multimode Preset", null, null, null, null, MULTIMODE, null, false);

        // MULTIMODE_VOLUME,          id = 248 (78h,01h)   min =   0;  max =  127;       default = 127;
        load(248, "multimode_vol", "Multimode Volume", null, null, null, null, MULTIMODE, null, true, 0, 127, 127);

        // MULTIMODE_PAN   ,          id = 249 (79h,01h)   min = -64;  max =  +63;       default =   0;
        load(249, "multimode_pan", "Multimode Pan", null, null, null, null, MULTIMODE, null, true, -64, 63, 0);

        // REQUIRES MMD CHECK
        // MULTIMODE_SUBMIX,          id = 250 (7Ah,01h)   min =  -1;  max = 3(7);       default =  -1;
        load(250, "multimode_submix", "Multimode Submix", v2ps_submix, null, null, null, MULTIMODE, null, false);

        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Internal");
        df = new DecimalFormat("00");
        for (int i = 1; i < 17; i++)
            v2ps.put(IntPool.get(i), "Ext/c" + df.format(i));
        // E4_LINK_INTERNAL_EXTERNAL, id = 251 (7Bh,01h)   min = 0;  max =  16;  default = 0;
        load(251, "link_intext", "Type/Chnl", v2ps, null, null, null, LINK_MIDIFILTERS, null, false, 0, 16, 0);

        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Off");
        v2ps.put(IntPool.get(1), "On");
        // E4_LINK_FILTER_PITCH,      id = 252 (7Ch,01h)   min = 0;  max =   1;  default = 0;
        load(252, "link_filter_pitch", "Pitch", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_MOD,        id = 253 (7Dh,01h)   min = 0;  max =   1;  default = 0;
        load(253, "link_filter_mod", "Modulation", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_PRESSURE,   id = 254 (7Eh,01h)   min = 0;  max =   1;  default = 0;
        load(254, "link_filter_pressure", "Pressure", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_PEDAL,      id = 255 (7Fh,01h)   min = 0;  max =   1;  default = 0;
        load(255, "link_filter_pedal", "Pedal", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_CTRL_A,     id = 256 (00h,02h)   min = 0;  max =   1;  default = 0;
        load(256, "link_filter_ctrla", "CtrlA", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_CTRL_B,     id = 257 (01h,02h)   min = 0;  max =   1;  default = 0;
        load(257, "link_filter_ctrlb", "CtrlB", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_CTRL_C,     id = 258 (02h,02h)   min = 0;  max =   1;  default = 0;
        load(258, "link_filter_ctrlc", "CtrlC", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_CTRL_D,     id = 259 (03h,02h)   min = 0;  max =   1;  default = 0;
        load(259, "link_filter_ctrld", "CtrlD", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_CTRL_E,     id = 260 (04h,02h)   min = 0;  max =   1;  default = 0;
        load(260, "link_filter_ctrle", "CtrlE", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_CTRL_F,     id = 261 (05h,02h)   min = 0;  max =   1;  default = 0;
        load(261, "link_filter_ctrlf", "CtrlF", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_CTRL_G,     id = 262 (06h,02h)   min = 0;  max =   1;  default = 0;
        load(262, "link_filter_ctrlg", "CtrlG", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_CTRL_H,     id = 263 (07h,02h)   min = 0;  max =   1;  default = 0;
        load(263, "link_filter_ctrlh", "CtrlH", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_SWITCH_1,   id = 264 (08h,02h)   min = 0;  max =   1;  default = 0;
        load(264, "link_filter_switch1", "FootSwitch2", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_SWITCH_2,   id = 265 (09h,02h)   min = 0;  max =   1;  default = 0;
        load(265, "link_filter_switch2", "FootSwitch1", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);
        // E4_LINK_FILTER_THUMB,      id = 266 (0Ah,02h)   min = 0;  max =   1;  default = 0;
        load(266, "link_filter_thumb", "Thumb", v2ps, null, null, null, LINK_MIDIFILTERS, null, true, 0, 1, 0);

        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Internal");
        v2ps.put(IntPool.get(1), "BNC");
        v2ps.put(IntPool.get(2), "AES");
        v2ps.put(IntPool.get(3), "ADAT");
        // REQUIRES MMD CHECK
        //MASTER_WORD_CLOCK_IN,          id = 267 (0Bh,02h)    min = 0;  max = 4
        load(267, "master_wc_in", "Word Clock In", v2ps, null, null, null, MASTER_IO, null, false);

        v2ps = createStringForValueMap();
        double inc = 359.30 / 512;
        df = new DecimalFormat("###.00");
        for (int i = 0; i < 512; i++)
            v2ps.put(IntPool.get(i), df.format(i * inc));

        /** ULTRA ONLY PARAMETERS *************************************************/

        // MASTER_WORD_CLOCK_PHASE_IN,    id = 268 (0Ch,02h)    min = 0;  max = 511
        load(268, "master_wc_phase_in", "Word Clock Phase In", v2ps, null, null, "deg", MASTER_IO, null, false, 0, 511, 0);
        // MASTER_WORD_CLOCK_PHASE_OUT,   id = 269 (0Dh,02h)    min = 0;  max = 511
        load(269, "master_wc_phase_out", "Word Clock Phase Out", v2ps, null, null, "deg", MASTER_IO, null, false, 0, 511, 0);

        // MASTER_OUTPUT_DITHER,          id = 270 (0Eh,02h)    min = 0;  max = 1
        v2ps = createStringForValueMap();
        v2ps.put(IntPool.get(0), "Off");
        v2ps.put(IntPool.get(1), "On");
        load(270, "master_output_dither", "Output Dither", v2ps, null, null, null, MASTER_IO, null, false, 0, 1, 0);

        /** ULTRA ONLY PARAMETERS *************************************************/

        // REQUIRES MMD CHECK
        // MASTER_AUDITION_KEY,           id = 271 (0Fh,02h)    min = 0;  max = 127
        // TODO!! make note names for this parameter2
        load(271, "master_audition_key", "Audition Key", null, null, null, null, MASTER_TUNING, null, false);
        System.out.println("PARAMETER TABLES LOADED.");
    }

    protected static Map createStringForValueMap() {
        // TreeMap sorts keys ( ensures parameter2 values(the keys) are ordered )
        return new HashMap();
    }
    /*private Map createStringForValueMap(int loadFact) {
        // TreeMap sorts keys ( ensures parameter2 values(the keys) are ordered )
        return new TreeMap();
    } */

    protected static Map createValueForStringMap() {
        // TreeMap sorts keys ( ensures parameter2 values(the keys) are ordered )
        return new HashMap();
    }

    public static Map getCollaborations() {
        TreeMap retMap = new TreeMap();
        Iterator i = co2id.keySet().iterator();
        Object o;
        ArrayList a;
        for (; i.hasNext();) {
            o = i.next();
            a = ((ArrayList) co2id.get(o));
            retMap.put(o, a.clone());
        }
        return retMap;
    }

    public static Map getCategories() {
        TreeMap retMap = new TreeMap();
        Iterator i = cs2id.keySet().iterator();
        Object o;
        ArrayList a;
        for (; i.hasNext();) {
            o = i.next();
            a = ((ArrayList) cs2id.get(o));
            retMap.put(o, a.clone());
        }
        return retMap;
    }

    public static  boolean isFilterSubId(Integer id) {
        int idv = id.intValue();

        if ((idv >= 83 && idv <= 92)) {
            return true;
        }

        return false;
    }

    public static boolean paramExists(Integer id) {
        return id2ps.containsKey(id);
    }

    public static boolean paramExists(String ref) {
        return rs2id.containsKey(ref);
    }

    public static GeneralParameterDescriptor generateParameterDescriptor(Integer id, MinMaxDefault mmd, int loc) {

        if (isFilterSubId(id)) {
            Impl_FilterParameterDescriptor pd = new Impl_FilterParameterDescriptor();
            pd.init(id, mmd, loc);
            return pd;
        } else {
            if (!id2ps.containsKey(id)) {
                // no harcoded data for this id, so just make some defaults
                int maxv = mmd.getMax().intValue();
                Map vmap = createStringForValueMap();
                id2v2ps.put(id, vmap);
                for (int n = mmd.getMin().intValue(); n <= maxv; n++)
                    vmap.put(IntPool.get(n), makeDefaultValueString(n));

                load(id.intValue(), makeDefaultReferenceString(id), makeDefaultPresentationString(id), vmap, null, null, null, CATEGORY_UNRECOGNIZED_ID, null, false);
            }

            Impl_GeneralParameterDescriptor pd = new Impl_GeneralParameterDescriptor();
            pd.init(id, mmd, loc);
            return pd;
        }
    }

    public static GeneralParameterDescriptor generateParameterDescriptor(Integer id, int loc) throws ParameterRequiresMMDException {
        // make sure we have MMD info first
        if (!id2mmd.containsKey(id))
            throw new ParameterRequiresMMDException(id, "");
        MinMaxDefault mmd = (MinMaxDefault) id2mmd.get(id);

        if (isFilterSubId(id)) {
            Impl_FilterParameterDescriptor pd = new Impl_FilterParameterDescriptor();
            pd.init(id, mmd, loc);
            return pd;
        } else {
            // if no harcoded data for this id, just make some defaults
            if (!id2ps.containsKey(id)) {
                int maxv = mmd.getMax().intValue();
                Map vmap = createStringForValueMap();
                id2v2ps.put(id, vmap);
                for (int n = mmd.getMin().intValue(); n <= maxv; n++)
                    vmap.put(IntPool.get(n), makeDefaultValueString(n));

                load(id.intValue(), makeDefaultReferenceString(id), makeDefaultPresentationString(id), vmap, null, null, null, CATEGORY_UNRECOGNIZED_ID, null, false);
            }

            Impl_GeneralParameterDescriptor pd = new Impl_GeneralParameterDescriptor();
            pd.init(id, mmd, loc);
            return pd;
        }
    }

    private static String makeDefaultReferenceString(Integer id) {
        return id.toString();
    }

    private static String makeDefaultPresentationString(Integer id) {
        return PREFIX_RAW_ID + id.toString();
    }

    private static String makeDefaultValueString(int val) {
        return /*PREFIX_RAW_VALUE +*/ Integer.toString(val);
    }

    private static String[] makeIntegerStringArray(int low, int high) {
        int upper = high - low;
        String[] retArr = new String[upper + 1];

        for (int i = 0; i <= upper; i++)
            retArr[i] = Integer.toString(i + low);
        return retArr;
    }

    private static String safeFetchStringForValue(String[] vals, int min, int val) {
        int index = val - min;
        if (index < 0 || index >= vals.length)
            return Integer.toString(val);
        return vals[val];
    }

    private static void buildFilterTables() {
        /*
              E4_VOICE_FTYPE,            id = 82 (52h,00h)     min = 0;  max = variable
              E4_VOICE_FMORPH,           id = 83 (53h,00h)     min = 0;  max = 255
              E4_VOICE_FKEY_XFORM,       id = 84 (54h,00h)     min = 0;  max = 127

              E4_VOICE_FILT_GEN_PARM1,   id = 85 (55h,00h)     min = 0;  max = 255 (reserved for future expansion)
              E4_VOICE_FILT_GEN_PARM2,   id = 86 (56h,00h)     min = 0;  max = 255 (reserved for future expansion)
              E4_VOICE_FILT_GEN_PARM3,   id = 87 (57h,00h)     min = 0;  max = 255
              E4_VOICE_FILT_GEN_PARM4,   id = 88 (58h,00h)     min = 0;  max = 255
              E4_VOICE_FILT_GEN_PARM5,   id = 89 (59h,00h)     min = 0;  max = 255
              E4_VOICE_FILT_GEN_PARM6,   id = 90 (5Ah,00h)     min = 0;  max = 255
              E4_VOICE_FILT_GEN_PARM7,   id = 91 (5Bh,00h)     min = 0;  max = 255
              E4_VOICE_FILT_GEN_PARM8,   id = 92 (5Ch,00h)     min = 0;  max = 255
      */

        // presentation strings for Filter subIds
        // format: filter type(Map) -> filter sub id(Map) -> presentation string(String)
        // private Map filterParam_ps = new HashMap();

        // unit strings for Filter subIds
        // format: filter type(Map) -> filter sub id(Map) -> units string(String)
        // private Map filterParam_u = new HashMap();

        Integer id;
        for (int i = 0; i < numFilterTypes; i++) {
            filterParam_ps.put(IntPool.get(i), new HashMap());
            filterParam_u.put(IntPool.get(i), new HashMap());
        }

        // build filter morph names table ( id 83 )
        id = IntPool.get(83);
        for (int i = 0; i < 15; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "Frequency");
        for (int i = 15; i < 18; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "Morph");

        ((Map) filterParam_ps.get(IntPool.get(18))).put(id, "Fc/Morph");
        ((Map) filterParam_ps.get(IntPool.get(19))).put(id, "Morph");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "Morph");

        // build filter xform names table ( id 84 )
        id = IntPool.get(84);
        for (int i = 0; i < 8; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "Q");
        for (int i = 8; i < 11; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "Gain");
        for (int i = 11; i < 15; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "Resonance");
        for (int i = 15; i < 17; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "Body Size");
        ((Map) filterParam_ps.get(IntPool.get(17))).put(id, "Gain");
        ((Map) filterParam_ps.get(IntPool.get(18))).put(id, "LPF Q");
        ((Map) filterParam_ps.get(IntPool.get(19))).put(id, "Expression");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "Peak");

        // build filter filterGenParam1_ps table (id 85) ( CURRENTLY RESERVED )
        /*id = IntPool.get(85);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "EQ 1 Low");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "Low Morph Frame Freq");

        // build filter filterGenParam2_ps table (id 86) ( CURRENTLY RESERVED )
        id = IntPool.get(86);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "EQ 1 Low");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "Low Morph Frame Freq");
         */

        // build filter filterGenParam3_ps table (id 87)
        id = IntPool.get(87);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "EQ 1 Low");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "Low Morph Freq");

        // build filter filterGenParam4_ps table (id 88)
        id = IntPool.get(88);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "EQ 1 High");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "Low Morph Shelf");


        // build filter filterGenParam5_ps table (id 89)
        id = IntPool.get(89);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "EQ 1 Gain");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "Low Morph Peak");


        // build filter filterGenParam6_ps table  (id 90)
        id = IntPool.get(90);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "EQ 2 Low");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "High Morph Freq");

        // build filter filterGenParam7_ps table (id 91)
        id = IntPool.get(91);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "EQ 2 High");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "High Morph Shelf");

        // build filter filterGenParam8_ps table (id 92)
        id = IntPool.get(92);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_ps.get(IntPool.get(i))).put(id, "EQ 2 Gain");
        ((Map) filterParam_ps.get(IntPool.get(20))).put(id, "High Morph Peak");


        // UNITS

        // build filter morph names units table ( id 83 )
        id = IntPool.get(83);
        for (int i = 0; i < 11; i++)
            ((Map) filterParam_u.get(IntPool.get(i))).put(id, " Hz");


        // build filter xform names units table ( id 84 )
        id = IntPool.get(84);
        ((Map) filterParam_u.get(IntPool.get(8))).put(id, " dB");
        ((Map) filterParam_u.get(IntPool.get(9))).put(id, " dB");
        ((Map) filterParam_u.get(IntPool.get(10))).put(id, " dB");
        ((Map) filterParam_u.get(IntPool.get(17))).put(id, " dB");
        ((Map) filterParam_u.get(IntPool.get(20))).put(id, " dB");

        // build filter filterGenParam3_u table (id 87)
        id = IntPool.get(87);
        for (int i = 17; i < 21; i++)
            ((Map) filterParam_u.get(IntPool.get(i))).put(id, " Hz");
        //((Map) filterParam_u.get(IntPool.get(20))).put(id, "Hz");

        // build filter filterGenParam4_u table  (id 88)
        id = IntPool.get(88);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_u.get(IntPool.get(i))).put(id, " Hz");
        //((Map) filterParam_u.get(IntPool.get(20))).put(id, null);

        // build filter filterGenParam5_u table (id 89)
        id = IntPool.get(89);
        for (int i = 17; i < 21; i++)
            ((Map) filterParam_u.get(IntPool.get(i))).put(id, " dB");
        //((Map) filterParam_u.get(IntPool.get(20))).put(id, "dB");

        // build filter filterGenParam6_u table (id 90)
        id = IntPool.get(90);
        for (int i = 17; i < 21; i++)
            ((Map) filterParam_u.get(IntPool.get(i))).put(id, " Hz");
        //((Map) filterParam_u.get(IntPool.get(20))).put(id, "Hz");

        // build filter filterGenParam7_u table  id(91)
        id = IntPool.get(91);
        for (int i = 17; i < 20; i++)
            ((Map) filterParam_u.get(IntPool.get(i))).put(id, " Hz");
        //((Map) filterParam_u.get(IntPool.get(20))).put(id, null);

        // build filter filterGenParam8_u table (id 92)
        id = IntPool.get(92);
        for (int i = 17; i < 21; i++)
            ((Map) filterParam_u.get(IntPool.get(i))).put(id, " dB");
        //((Map) filterParam_u.get(IntPool.get(20))).put(id, "dB");


        // build filter value table 1
        for (int i = 0; i < 256; i++)
            filterValueTable1[i] = String.valueOf(fil_freq(i, 20000, 1002));

        // build filter value table 2
        for (int i = 0; i < 256; i++)
            filterValueTable2[i] = String.valueOf(fil_freq(i, 18000, 1003));

        // build filter value table 3a
        for (int i = 0; i < 256; i++)
            filterValueTable3a[i] = String.valueOf(fil_freq(i, 10000, 1006));

        // build filter value table 3b
        for (int i = 0; i < 256; i++)
            filterValueTable3b[i] = String.valueOf(fil_freq(i, 10000, 1005));

        // build filter value table 4
        for (int i = 0; i < 128; i++)
            filterValueTable4[i] = cnv_morph_gain(i);

        // build filter value table 5
        for (int i = 0; i < 128; i++)
            filterValueTable5[i] = cnv_morph_freq(2 * i);

        // build filter value presentation strings
        // format: filter type(Map) -> filter sub id(Map) -> value presentation string(String[])
        Map subIds;
        String[] isArr128 = makeIntegerStringArray(0, 127);
        String[] isArr256 = makeIntegerStringArray(0, 255);
        String[] isArr128_zero_centered = makeIntegerStringArray(-64, 63);

        //   2 Pole Low-pass
        //   4 Pole Low-pass
        //   6 Pole Low-pass
        subIds = new HashMap();
        subIds.put(IntPool.get(83), filterValueTable1);
        subIds.put(IntPool.get(84), isArr128);
        filterValue_ps.put(IntPool.get(0), subIds);
        filterValue_ps.put(IntPool.get(1), subIds);
        filterValue_ps.put(IntPool.get(2), subIds);

        // 2nd Order High-pass
        // 4th Order High-pass
        subIds = new HashMap();
        subIds.put(IntPool.get(83), filterValueTable2);
        subIds.put(IntPool.get(84), isArr128);
        filterValue_ps.put(IntPool.get(3), subIds);
        filterValue_ps.put(IntPool.get(4), subIds);

        // 2nd Order Band-pass
        // 4th Order Band-pass
        // Contrary Band-pass
        subIds = new HashMap();
        subIds.put(IntPool.get(83), filterValueTable3b);
        subIds.put(IntPool.get(84), isArr128);
        filterValue_ps.put(IntPool.get(5), subIds);
        filterValue_ps.put(IntPool.get(6), subIds);
        filterValue_ps.put(IntPool.get(7), subIds);

        // Swept EQ 1 octave
        // Swept EQ 2->1 oct
        // Swept EQ 3->1 oct
        subIds = new HashMap();
        subIds.put(IntPool.get(83), filterValueTable3a);
        subIds.put(IntPool.get(84), filterValueTable4);
        filterValue_ps.put(IntPool.get(8), subIds);
        filterValue_ps.put(IntPool.get(9), subIds);
        filterValue_ps.put(IntPool.get(10), subIds);

        // Phaser 1
        // Phaser 2
        // Bat-Phaser
        // Flanger Lite
        // Vocal Ah-Ay-Ee
        // Vocal Oo-Ah
        subIds = new HashMap();
        subIds.put(IntPool.get(83), isArr256);
        subIds.put(IntPool.get(84), isArr128);
        filterValue_ps.put(IntPool.get(11), subIds);
        filterValue_ps.put(IntPool.get(12), subIds);
        filterValue_ps.put(IntPool.get(13), subIds);
        filterValue_ps.put(IntPool.get(14), subIds);
        filterValue_ps.put(IntPool.get(15), subIds);
        filterValue_ps.put(IntPool.get(16), subIds);

        // Dual EQ Morph:
        subIds = new HashMap();
        subIds.put(IntPool.get(83), isArr256);
        subIds.put(IntPool.get(84), filterValueTable4);
        subIds.put(IntPool.get(87), filterValueTable5);
        subIds.put(IntPool.get(88), filterValueTable5);
        subIds.put(IntPool.get(89), filterValueTable4);
        subIds.put(IntPool.get(90), filterValueTable5);
        subIds.put(IntPool.get(91), filterValueTable5);
        subIds.put(IntPool.get(92), filterValueTable4);
        filterValue_ps.put(IntPool.get(17), subIds);

        // 2EQ+Lowpass Morph:
        // 2EQMorph+Exprssn:
        subIds = new HashMap();
        subIds.put(IntPool.get(83), isArr256);
        subIds.put(IntPool.get(84), isArr128);
        subIds.put(IntPool.get(87), filterValueTable5);
        subIds.put(IntPool.get(88), filterValueTable5);
        subIds.put(IntPool.get(89), filterValueTable4);
        subIds.put(IntPool.get(90), filterValueTable5);
        subIds.put(IntPool.get(91), filterValueTable5);
        subIds.put(IntPool.get(92), filterValueTable4);
        filterValue_ps.put(IntPool.get(18), subIds);
        filterValue_ps.put(IntPool.get(19), subIds);

        // Peak/Shelf Morph
        subIds = new HashMap();
        subIds.put(IntPool.get(83), isArr256);
        subIds.put(IntPool.get(84), filterValueTable4);
        subIds.put(IntPool.get(87), filterValueTable5);
        subIds.put(IntPool.get(88), isArr128_zero_centered);
        subIds.put(IntPool.get(89), filterValueTable4);
        subIds.put(IntPool.get(90), filterValueTable5);
        subIds.put(IntPool.get(91), isArr128_zero_centered);
        subIds.put(IntPool.get(92), filterValueTable4);
        filterValue_ps.put(IntPool.get(20), subIds);
    }

    private static void load(int id, String rs, String ps, Map v2ps, Map v2ts, Map v2i, String us, String cs, String co, boolean useSpinner, int min, int max, int def) {
        final Integer ido = IntPool.get(id);
        final Integer mino = IntPool.get(min);
        final Integer maxo = IntPool.get(max);
        final Integer defo = IntPool.get(def);
        MinMaxDefault mmd = new MinMaxDefault() {
            public int id = getID().intValue();
            public int min = getMin().intValue();
            public int max = getMax().intValue();
            public int def = getDefault().intValue();

            public boolean equals(Object obj) {
                if (obj instanceof MinMaxDefault) {
                    MinMaxDefault mmd = (MinMaxDefault) obj;
                    return (mmd.getID().equals(getID()) && mmd.getMin().equals(getMin()) && mmd.getMax().equals(getMax()) && mmd.getDefault().equals(getDefault()));
                }
                return false;
            }

            public Integer getID() {
                return ido;
            }

            public Integer getMin() {
                return mino;
            }

            public Integer getMax() {
                return maxo;
            }

            public Integer getDefault() {
                return defo;
            }
        };
        load(id, rs, ps, v2ps, v2ts, v2i, us, cs, co, useSpinner, mmd);
    }

    private static void load(int id, String rs, String ps, Map v2ps, Map v2ts, Map v2i, String us, String cs, String co, boolean useSpinner) {
        load(id, rs, ps, v2ps, v2ts, v2i, us, cs, co, useSpinner, null);
    }

    private static void load(int id, String rs, String ps, Map v2ps, Map v2ts, Map v2i, String us, String cs, String co, boolean useSpinner, MinMaxDefault mmd) {
        Integer ido = IntPool.get(id);

        // reference string
        if (rs != null) {
            id2rs.put(ido, rs);
            rs2id.put(rs, ido);
        }

        // presentation string
        if (ps != null)
            id2ps.put(ido, ps);

        // value presentation strings
        if (v2ps != null)
            id2v2ps.put(ido, v2ps);

        // value tip strings
        if (v2ts != null)
            id2v2ts.put(ido, v2ts);

        // value imageForValueMap
        if (v2i != null)
            id2v2i.put(ido, v2i);

        // unit string
        if (us != null)
            id2us.put(ido, us);

        java.util.List lst = null;

        // category string
        if (cs == null)
            cs = CATEGORY_GENERAL;

        id2cs.put(ido, cs);
        lst = (List) cs2id.get(cs);
        if (lst == null) {
            lst = new ArrayList();
            cs2id.put(cs, lst);
        }
        lst.add(ido);

        // collaboration string
        if (co != null) {
            id2co.put(ido, co);
            lst = (List) co2id.get(co);
            if (lst == null) {
                lst = new ArrayList();
                co2id.put(co, lst);
            }
            lst.add(ido);
        }

        // min, max, default information
        if (mmd != null)
            id2mmd.put(ido, mmd);

        id2useSpinner.put(ido, new Boolean(useSpinner));
    }

    private static int fil_freq(int input, int maxfreq, int mul) {
        int f = maxfreq;
        input = 255 - input;
        while (input-- > 0) {
            f *= mul;
            f /= 1024;
        }
        return f;
    }

    private static String cnv_morph_freq(int input) {
        DecimalFormat df = new DecimalFormat("####0");
        return df.format(fil_freq(input, 10000, 1006));   /*1009*/
    }
/* in=0..127 out=-24..+24    (32in ==> 12out) */
    /*void cnv_morph_gain (int input, char *buf)
    {
        int gain10x = -240 + ((input * 120) / 32);
        int gain_i  = gain10x / 10;
        int gain_f  = abs (gain10x % 10);
        sprintf (buf, "%s%d.%1ddB",
                 gain10x >= 0 ? "+" : "-",
                 abs (gain_i),
                 gain_f);
    } */

    private static String cnv_morph_gain(int input) {
        int gain10x = -240 + ((input * 120) / 32);
        int gain_i = gain10x / 10;
        int gain_f = Math.abs(gain10x % 10);
        DecimalFormat df = new DecimalFormat("##0");
        return (gain10x >= 0 ? "+" : "-") + (df.format(Math.abs(gain_i)) + "." + Integer.toString(gain_f));
    }

}