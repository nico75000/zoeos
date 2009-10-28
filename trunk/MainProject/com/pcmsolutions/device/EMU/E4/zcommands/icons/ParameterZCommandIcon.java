package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 03-May-2004
 * Time: 18:35:53
 */
abstract public class ParameterZCommandIcon implements Icon {
    protected static final float width = 12;
    protected static final float height = 12;
    protected static final float inset = 6;
    protected static final Color c1 = UIColors.applyAlpha(Color.lightGray, UIColors.tableAlpha);
    protected static final Color c2 = UIColors.applyAlpha(Color.blue, UIColors.tableAlpha);

    ParameterZCommandIcon(){

    }
    public int getIconWidth() {
        return (int)(width + inset * 2);
    }

    public int getIconHeight() {
        return (int)(height + inset * 2);
    }
}
