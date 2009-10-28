package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.Win;

import java.util.Vector;

public class DefaultRangeWindowModel implements RangeWindowModel {
    protected int atk1Rate = 0;
    protected int atk1Level = 0;
    protected int atk2Rate = 0;
    protected int atk2Level = 100;

    protected int dec1Rate = 0;
    protected int dec1Level = 99;
    protected int dec2Rate = 0;
    protected int dec2Level = 100;

    protected int rls1Rate = 20;
    protected int rls1Level = 0;
    protected int rls2Rate = 0;
    protected int rls2Level = 0;

    protected float baseLevel;

    protected int maxLevel = 100;
    protected int minLevel = -100;
    protected int maxRate = 127;

    protected Vector listeners = new Vector();

    public DefaultRangeWindowModel() {
        baseLevel = minLevel + (maxLevel - minLevel) / 2;
    }

    protected void update() {
        baseLevel = minLevel + (maxLevel - minLevel) / 2;
        fireModelChanged();
    }

    protected void fireModelChanged() {
        for (int i = 0,j = listeners.size(); i < j; i++)
            try {
                ((RangeWindowListener) listeners.get(i)).rangeWindowChanged(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void refresh() {
    }

    public void addRangeWindowListener(RangeWindowListener rel) {
        listeners.add(rel);
    }

    public void removeRangeWindowListener(RangeWindowListener rel) {
        listeners.remove(rel);
    }

    public float getBaseLevel() {
        return baseLevel;
    }

    public void setBaseLevel(float baseLevel) {
        this.baseLevel = baseLevel;
    }

    public int getAtk1Level() {
        return atk1Level;
    }

    public void setAtk1Level(int atk1Level) {
        this.atk1Level = atk1Level;
        update();

    }

    public int getAtk1Rate() {
        return atk1Rate;
    }

    public void setAtk1Rate(int atk1Rate) {
        this.atk1Rate = atk1Rate;
        update();
    }

    public int getAtk2Level() {
        return atk2Level;
    }

    public void setAtk2Level(int atk2Level) {
        this.atk2Level = atk2Level;
        update();
    }

    public int getAtk2Rate() {
        return atk2Rate;
    }

    public void setAtk2Rate(int atk2Rate) {
        this.atk2Rate = atk2Rate;
        update();
    }

    public int getDec1Level() {
        return dec1Level;
    }

    public void setDec1Level(int dec1Level) {
        this.dec1Level = dec1Level;
        update();
    }

    public int getDec1Rate() {
        return dec1Rate;
    }

    public void setDec1Rate(int dec1Rate) {
        this.dec1Rate = dec1Rate;
        update();
    }

    public int getDec2Level() {
        return dec2Level;
    }

    public void setDec2Level(int dec2Level) {
        this.dec2Level = dec2Level;
        update();
    }

    public int getDec2Rate() {
        return dec2Rate;
    }

    public void setDec2Rate(int dec2Rate) {
        this.dec2Rate = dec2Rate;
        update();
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        update();
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
        update();
    }

    public int getRls1Level() {
        return rls1Level;
    }

    public void setRls1Level(int rls1Level) {
        this.rls1Level = rls1Level;
        update();
    }

    public int getRls1Rate() {
        return rls1Rate;
    }

    public void setRls1Rate(int rls1Rate) {
        this.rls1Rate = rls1Rate;
        update();
    }

    public int getRls2Level() {
        return rls2Level;
    }

    public void setRls2Level(int rls2Level) {
        this.rls2Level = rls2Level;
        update();
    }

    public int getRls2Rate() {
        return rls2Rate;
    }

    public void setRls2Rate(int rls2Rate) {
        this.rls2Rate = rls2Rate;
        update();
    }

    public int getMaxRate() {
        return maxRate;
    }

    public void setMaxRate(int maxRate) {
        this.maxRate = maxRate;
        update();
    }

    public void zDispose() {
        listeners.clear();
        listeners = null;
    }
}
