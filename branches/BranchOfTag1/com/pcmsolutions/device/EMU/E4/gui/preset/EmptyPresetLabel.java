package com.pcmsolutions.device.EMU.E4.gui.preset;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-Apr-2003
 * Time: 20:33:54
 * To change this template use Options | File Templates.
 */
public class EmptyPresetLabel extends JLabel {
    public EmptyPresetLabel(Color fg) {
        this(fg, true);
    }

    public EmptyPresetLabel(Color fg, boolean opaque) {
        this.setForeground(fg);
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.setAlignmentY(Component.TOP_ALIGNMENT);
        this.setText("Empty Preset");
        this.setFont(new Font("MONOSPACED", Font.ITALIC, 16));
        setOpaque(opaque);
    }
}
