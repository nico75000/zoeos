package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.gui.device.PropertiesPanel;
import com.pcmsolutions.gui.desktop.ViewInstance;
import com.pcmsolutions.gui.desktop.ZDesktopManager;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.ZoeosPreferences;
import com.pcmsolutions.system.paths.DesktopName;
import com.pcmsolutions.system.paths.ViewPath;

import javax.swing.*;

/**
 * User: paulmeehan
 * Date: 15-Feb-2004
 * Time: 11:04:26
 */
public class SystemViewFactory {
    public static ViewInstance providePropertiesView() {
        final DesktopName name = new DesktopName(PropertiesPanel.class, SystemPathFactory.providePropertiesPath());
        final ViewPath vp = new ViewPath(ZDesktopManager.dockPROPERTIES, name);
        return new ViewInstance() {
            public JComponent getView() throws ComponentGenerationException {
                try {
                    PropertiesPanel pp = new PropertiesPanel(ZoeosPreferences.getPropertyList(), Zoeos.getInstance());
                    return pp;
                } catch (Exception e) {
                    throw new ComponentGenerationException(e.getMessage());
                }
            }

            public DesktopName getDesktopName() {
                return name;
            }

            public ViewPath getViewPath() {
                return vp;
            }
        };
    }
}
