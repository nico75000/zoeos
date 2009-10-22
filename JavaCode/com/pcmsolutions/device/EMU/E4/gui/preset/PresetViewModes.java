package com.pcmsolutions.device.EMU.E4.gui.preset;

/**
 * User: paulmeehan
 * Date: 21-May-2004
 * Time: 13:05:50
 */
public interface PresetViewModes {
    int VOICE_MODE_MAIN = 0x01;
    int VOICE_MODE_KEY_WIN = 0x02;
    int VOICE_MODE_VEL_WIN = 0x04;
    int VOICE_MODE_RT_WIN = 0x08;
    int VOICE_MODE_USER = 0x10;
    int VOICE_MODE_ALL = VOICE_MODE_MAIN | VOICE_MODE_KEY_WIN | VOICE_MODE_VEL_WIN | VOICE_MODE_RT_WIN | VOICE_MODE_USER;
    int VOICE_MODE_ALL_BUT_USER = VOICE_MODE_MAIN | VOICE_MODE_KEY_WIN | VOICE_MODE_VEL_WIN | VOICE_MODE_RT_WIN;

    int LINK_MODE_MAIN = 0;
    int LINK_MODE_WIN = 1;
    int LINK_MODE_MAIN_WIN = 2;

}
