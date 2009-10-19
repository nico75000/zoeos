package com.pcmsolutions.device.EMU.E4.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 06-Jun-2003
 * Time: 22:51:09
 * To change this template use Options | File Templates.
 */
public interface Hideable {
    public JButton getHideButton();

    public Component getComponent();

    public String getShowButtonText();
}
