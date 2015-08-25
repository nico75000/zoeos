package com.pcmsolutions.device.EMU.E4.zcommands.icons;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * User: paulmeehan
 * Date: 05-May-2004
 * Time: 14:53:07
 */
public class SamplePackageIcon extends PackageIcon implements Icon{
    SamplePackageIcon(boolean open){
        super(open, UIColors.getTableFirstSectionBG(), new Color(200,100,100), new Color(150,75,75));
    }     
}
