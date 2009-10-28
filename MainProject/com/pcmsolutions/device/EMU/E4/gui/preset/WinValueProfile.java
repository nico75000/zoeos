package com.pcmsolutions.device.EMU.E4.gui.preset;

import com.pcmsolutions.system.preferences.Impl_ZIntPref;
import com.pcmsolutions.system.preferences.ZIntPref;

import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 09-Dec-2003
 * Time: 17:52:39
 * To change this template use Options | File Templates.
 */
public interface WinValueProfile {

    public static final int WIN_POS_LOW = 0;
    public static final int WIN_POS_LOW_FADE = 1;
    public static final int WIN_POS_HIGH = 2;
    public static final int WIN_POS_HIGH_FADE = 3;

    public static final int MODE_DISPLAY_TEXT = 0;
    public static final int MODE_DISPLAY_GRAPH = 1;
    public static final int MODE_DISPLAY_TEXT_AND_GRAPH = 2;

    public static final int MODE_GRAPH_MERGE = 0;       // show merged graph similar to front panel display
    public static final int MODE_GRAPH_LOW = 1;         // show just the low fade graph
    public static final int MODE_GRAPH_HIGH = 2;        // show just the high fade graph
    public static final int MODE_GRAPH_OVERLAY = 3;     // show the low and high graph overlayed

    public static final int KEY_WIN = 0;
    public static final int VEL_WIN = 1;
    public static final int RT_WIN = 2;

    public static ZIntPref ZPREF_keyWinDisplayMode = new Impl_ZIntPref(Preferences.userNodeForPackage(WinValueProfile.class), "keyWinTableCellRendererDisplayMode", MODE_DISPLAY_GRAPH);
    public static ZIntPref ZPREF_velWinDisplayMode = new Impl_ZIntPref(Preferences.userNodeForPackage(WinValueProfile.class), "velWinTableCellRendererDisplayMode", MODE_DISPLAY_GRAPH);
    public static ZIntPref ZPREF_rtWinDisplayMode = new Impl_ZIntPref(Preferences.userNodeForPackage(WinValueProfile.class), "rtWinTableCellRendererDisplayMode", MODE_DISPLAY_GRAPH);

    public static ZIntPref ZPREF_graphMode = new Impl_ZIntPref(Preferences.userNodeForPackage(WinValueProfile.class), "winTableCellRendererGraphMode", MODE_GRAPH_MERGE);

    public int getLow();

    public int getLowFade();

    public int getHigh();

    public int getHighFade();

    public boolean isChildWindow();

    //public ZIntPref getDisplayZPref();

    // key, vel, rt
    public int getType();

    //public Shape getLowWindow();

    //public Shape getHighWindow();
}
