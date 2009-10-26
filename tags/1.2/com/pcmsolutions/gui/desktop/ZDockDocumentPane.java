package com.pcmsolutions.gui.desktop;

import com.jidesoft.document.DocumentComponentEvent;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 20-Jan-2004
 * Time: 05:04:49
 */
public class ZDockDocumentPane extends ZDocumentPane {

    protected ZDockableFrame dockableFrame;

    public ZDockDocumentPane(ZDockableFrame dockableFrame) {
        this.dockableFrame = dockableFrame;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public ZDockableFrame getDockableFrame() {
        return dockableFrame;
    }

    public boolean isMenuBarAvailable() {
        return false;
    }

    public JMenuBar getMenuBar() {
        return null;
    }

    public void documentComponentDeactivated(DocumentComponentEvent event) {
        super.documentComponentDeactivated(event);
         if (event.getDocumentComponent() instanceof ZDocumentComponent) {
            ZDocumentComponent zdc = (ZDocumentComponent) event.getDocumentComponent();
            try {
                if (zdc.getDesktopElement().isMenuBarAvailable())
                    ((ZDockDocumentPane) zdc.getDocumentPane()).getDockableFrame().setJMenuBar(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void documentComponentActivated(DocumentComponentEvent event) {
        super.documentComponentActivated(event);
        if (event.getDocumentComponent() instanceof ZDocumentComponent) {
            ZDocumentComponent zdc = (ZDocumentComponent) event.getDocumentComponent();
            try {
                if (zdc.getDesktopElement().isMenuBarAvailable())
                    ((ZDockDocumentPane) zdc.getDocumentPane()).getDockableFrame().setJMenuBar(zdc.getDesktopElement().getMenuBar());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
