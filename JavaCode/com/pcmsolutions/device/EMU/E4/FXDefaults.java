package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.system.IntPool;

/**
 * User: paulmeehan
 * Date: 07-Jan-2005
 * Time: 16:31:04
 */
public class FXDefaults {
    private static int[] FXA_Decay = new int[]{0, 40, 44, 48, 56, 56, 56, 56, 36, 24, 24, 24, 24, 48, 48, 48, 48, 48, 40, 37, 60, 30, 45, 48, 72, 80, 64, 32, 0, 0, 0, 60, 60, 64, 48, 80, 64, 60, 60, 40, 48, 52, 40, 32, 56};
    private static int[] FXA_HFDamping = new int[]{0, 96, 64, 96, 64, 80, 64, 120, 120, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 120, 120, 120, 120, 0, 0, 0, 96, 96, 0, 96, 64, 120, 120, 96, 96, 0, 120, 8, 104, 104, 112, 112, 80, 56, 32};
    private static int[] FXB_Feedback = new int[]{0, 0, 4, 8, 16, 64, 0, 0, 88, 64, 64, 104, 72, 16, 112, 16, 48, 64, 32, 32, 32, 32, 16, 24, 24, 32, 32, 0, 100, 70, 90, 20, 0};
    private static int[] FXB_LFORate = new int[]{0, 3, 11, 4, 11, 2, 0, 0, 3, 1, 6, 5, 2, 24, 1, 4, 24, 9, 0, 0, 0, 0, 9, 24, 3, 0, 0, 70, 0, 1, 6, 4, 0};
    private static int[] FXB_DelayTime = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 250, 300, 400, 400, 200, 120, 250, 500, 500, 0, 0, 0, 0, 0, 0};

    public static Integer getFXA_Decay(Integer effect) {
        return IntPool.get(FXA_Decay[effect.intValue()]);
    }

    public static Integer getFXA_HFDamping(Integer effect) {
        return IntPool.get(FXA_HFDamping[effect.intValue()]);
    }

    public static Integer getFXB_Feedback(Integer effect) {
        return IntPool.get(FXB_Feedback[effect.intValue()]);
    }

    public static Integer getFXB_LFORate(Integer effect) {
        return IntPool.get(FXB_LFORate[effect.intValue()]);
    }

    public static Integer getFXB_DelayTime(Integer effect) {
        return IntPool.get(FXB_DelayTime[effect.intValue()] / 5);
    }
}
