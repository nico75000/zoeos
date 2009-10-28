package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope;

import com.pcmsolutions.system.ZDisposable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 29-Jun-2003
 * Time: 20:25:02
 * To change this template use Options | File Templates.
 */
public interface RatesEnvelopeModel extends ZDisposable {

    public void refresh();

    public void addRatesEnvelopeListener(RatesEnvelopeListener rel);

    public void removeRatesEnvelopeListener(RatesEnvelopeListener rel);

    public float getBaseLevel();

    public void setBaseLevel(float baseLevel);

    public int getAtk1Level();

    public void setAtk1Level(int atk1Level);

    public int getAtk1Rate();

    public void setAtk1Rate(int atk1Rate);

    public int getAtk2Level();

    public void setAtk2Level(int atk2Level);

    public int getAtk2Rate();

    public void setAtk2Rate(int atk2Rate);

    public int getDec1Level();

    public void setDec1Level(int dec1Level);

    public int getDec1Rate();

    public void setDec1Rate(int dec1Rate);

    public int getDec2Level();

    public void setDec2Level(int dec2Level);

    public int getDec2Rate();

    public void setDec2Rate(int dec2Rate);

    public int getMaxLevel();

    public void setMaxLevel(int maxLevel);

    public int getMinLevel();

    public void setMinLevel(int minLevel);

    public int getRls1Level();

    public void setRls1Level(int rls1Level);

    public int getRls1Rate();

    public void setRls1Rate(int rls1Rate);

    public int getRls2Level();

    public void setRls2Level(int rls2Level);

    public int getRls2Rate();

    public void setRls2Rate(int rls2Rate);

    public int getMaxRate();

    public void setMaxRate(int maxRate);
}
