package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 01-Dec-2003
 * Time: 09:20:57
 * To change this template use Options | File Templates.
 */
public interface ID {
    final Integer preset = IntPool.get(23);
    final Integer lfo1Shape = IntPool.get(106);
    final Integer lfo2Shape = IntPool.get(111);

    final Integer sample = IntPool.get(38);
    final Integer group = IntPool.get(37);
    final Integer origKey = IntPool.get(44);
    final Integer keyLow = IntPool.get(45);
    final Integer keyHigh = IntPool.get(47);

    final Integer[] voiceKeyWin = ZUtilities.fillIncrementally(new Integer[4], 45);
    final Integer[] voiceVelWin = ZUtilities.fillIncrementally(new Integer[4], 49);
    final Integer[] voiceRTWin = ZUtilities.fillIncrementally(new Integer[4], 53);

    final Integer[] linkKeyWin = ZUtilities.fillIncrementally(new Integer[4], 28);
    final Integer[] linkVelWin = ZUtilities.fillIncrementally(new Integer[4], 32);
}
