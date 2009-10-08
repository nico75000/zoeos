// TileGridAction.java
// An action that tiles all internal frames when requested.
//
package com.pcmsolutions.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class TileHorizontalAction extends AbstractAction {
    private JDesktopPane desk; // the desktop to work with

    public TileHorizontalAction(JDesktopPane desk) {
        super("Tile Horizontal");
        this.desk = desk;
    }

    public void actionPerformed(ActionEvent ev) {
        DesktopUtils.tileVertical(desk);

        /*
        // How many frames do we have?
        JInternalFrame[] allframes = desk.getAllDefaultLayeredFrames();
        int count = allframes.elementCount;
        if (count == 0) return;


        // Define some initial values for size & location.
        Dimension size = desk.getSize();

        int w = size.width / count;
        int h = size.height;
        int x = 0;
        int y = 0;

        // Iterate over the frames, deiconifying any iconified frames and then
        // relocating & resizing each.
        for (int i = 0; i < count; i++) {
            JInternalFrame f = allframes[i];

            if (!f.isClosed() && f.isIcon()) {
                try {
                    f.setIcon(false);
                } catch (PropertyVetoException ignored) {
                }
            }

            desk.getzDesktopManager().resizeFrame(f, x, y, w, h);
            x += w;
        } */
    }
}

