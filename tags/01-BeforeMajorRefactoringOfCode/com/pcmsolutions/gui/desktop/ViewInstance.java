package com.pcmsolutions.gui.desktop;

import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.system.paths.DesktopName;
import com.pcmsolutions.system.paths.ViewPath;

import javax.swing.*;
import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 15-Feb-2004
 * Time: 11:06:48
 */
public interface ViewInstance extends Serializable {
    JComponent getView() throws ComponentGenerationException;

    DesktopName getDesktopName();

    ViewPath getViewPath();
}