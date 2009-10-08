package com.pcmsolutions.gui.desktop;

import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.gui.MenuBarProvider;
import com.pcmsolutions.system.Expirable;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.paths.DesktopName;
import com.pcmsolutions.system.paths.ViewPath;

import javax.swing.*;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 10-Oct-2003
 * Time: 20:16:06
 * To change this template use Options | File Templates.
 */
public interface DesktopElement extends TitleProvider, ZDisposable, MenuBarProvider, Expirable, Serializable, Comparable {

    // should be specific to describing what this component does - e.g  "DefaultPresetEditor:43"
    // never displayed to the user - used internally by desktop manager to provide singleton documents
    public DesktopName getName();

    public ViewPath getViewPath();

    // the component to be displayed
    public JComponent getComponent() throws ComponentGenerationException;

    public boolean isComponentGenerated();

    public DesktopNodeDescriptor getNodalDescriptor();

    // obvious
    public ActivityContext getActivityContext();

    // obvious
    public boolean isFloatable();

    public void setSessionString(String ss);

    public String getSessionString();

    public DesktopElement getCopy();
}
