package com.pcmsolutions.gui.zcommand;

import javax.swing.*;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 05-Sep-2004
 * Time: 13:42:09
 */
public interface ZCommandField <C extends JComponent, T> {
    C getComponent();

    String getLabelText();

    T getValue();
}
