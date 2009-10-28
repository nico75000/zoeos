package com.pcmsolutions.system;

/**
 * User: paulmeehan
 * Date: 19-Dec-2004
 * Time: 08:19:26
 */
public class TimingUtils {
    public static double getDMsForBeats(double beats) {
        return (60000 * beats) / ZoeosPreferences.ZPREF_systemTempo.getValue();
    }

    public static int getIMsForBeats(double beats) {
        return (int) Math.round(getDMsForBeats(beats));
    }    

    public static double constrainDelay(double delay) {
        return Math.max(Math.min(delay, 10000), 0);
    }

    public static int constrainDelay(int delay) {
        return Math.max(Math.min(delay, 10000), 0);
    }

    public static double parseMsField_double(String input) throws NumberFormatException {
        int iob = input.indexOf("b");
        if (iob == -1) {
            iob = input.indexOf("B");
            if (iob == -1)
                return Double.parseDouble(input);
            else
            // 4/4
                return getDMsForBeats(4 * Double.parseDouble(input.substring(0, iob)));
        } else
            return getDMsForBeats(Double.parseDouble(input.substring(0, iob)));
    }

    public static int parseMsField_int(String input) {
        return (int) Math.round(parseMsField_double(input));
    }
}