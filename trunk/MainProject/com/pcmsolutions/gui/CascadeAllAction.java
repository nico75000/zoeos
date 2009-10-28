// TileGridAction.java
// An action that tiles all internal frames when requested.
//
package com.pcmsolutions.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CascadeAllAction extends AbstractAction {
    private JDesktopPane desk; // the desktop to work with

    public CascadeAllAction(JDesktopPane desk) {
        super("Cascade All");
        this.desk = desk;
    }

    public void actionPerformed(ActionEvent ev) {
        DesktopUtils.cascadeAll(desk);
    }
}
