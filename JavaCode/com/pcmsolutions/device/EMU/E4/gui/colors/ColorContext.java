package com.pcmsolutions.device.EMU.E4.gui.colors;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 17-Apr-2003
 * Time: 18:36:15
 * To change this template use Options | File Templates.
 */
public interface ColorContext {
    // POPUP
    public Color getPopupBGColor();

    public Color getPopupFGColor();

    // BACKGROUND
    public Color getBGColor();

    public Color getFGColor();

    // SELECTION
    public Color getSelectionFGColor();

    public Color getSelectionBGColor();

    public Color getTitleBGColor();

    public Color getTitleFGColor();
}
