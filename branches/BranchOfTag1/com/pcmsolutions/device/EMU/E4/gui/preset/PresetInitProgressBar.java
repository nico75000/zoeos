package com.pcmsolutions.device.EMU.E4.gui.preset;

import com.pcmsolutions.gui.ZWindow;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 18-Jun-2003
 * Time: 16:47:01
 * To change this template use Options | File Templates.
 */
public class PresetInitProgressBar extends ZWindow implements ZDisposable {
    private JProgressBar bar = new JProgressBar(0, 100);

    private static final String REFERSH_PREFIX = "Refreshing: ";

    public PresetInitProgressBar(Frame owner, String presetName) throws HeadlessException {
        this(owner, null, presetName);
    }

    public PresetInitProgressBar(Frame owner, Component centreAboutComponent, String str) throws HeadlessException {
        super(owner, centreAboutComponent);
        getContentPane().add(bar);
        bar.setString(REFERSH_PREFIX + str);
        bar.setStringPainted(true);
        pack();
    }


    public void setPresetName(String str) {
        bar.setString(REFERSH_PREFIX + str);
    }

    public JProgressBar getBar() {
        return bar;
    }

    public void zDispose() {
        dispose();
    }
}
