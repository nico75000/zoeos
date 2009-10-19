package com.pcmsolutions.device.EMU.E4.gui.colors;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 18-Apr-2003
 * Time: 00:21:48
 * To change this template use Options | File Templates.
 */
public interface PresetColorContext extends ColorContext {
    public Color getPendingPresetIconColor();

    public Color getNamedPresetIconColor();

    public Color getFlashPresetIconColor();

    public Color getInitializedPresetIconColor();

    public LinkTableColorContext getLinkTableContext();

    public VoiceOverviewTableColorContext getVoiceOverviewTableContext();
}
