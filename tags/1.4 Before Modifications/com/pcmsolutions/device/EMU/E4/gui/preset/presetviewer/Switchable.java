package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-Jun-2003
 * Time: 17:49:21
 * To change this template use Options | File Templates.
 */
public interface Switchable {
    public boolean getState();

    public void toggle();
}
