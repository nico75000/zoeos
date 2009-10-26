package com.pcmsolutions.device.EMU.E4.gui.preset;

import com.pcmsolutions.system.preferences.ZIntPref;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 18-Dec-2003
 * Time: 07:25:07
 * To change this template use Options | File Templates.
 */
public class WinPopupMenu extends JMenu {
    private ZIntPref pref;

    public WinPopupMenu(ZIntPref pref) {
        this.pref = pref;
        final ZIntPref f_pref = pref;
        JRadioButtonMenuItem r1 = new JRadioButtonMenuItem(new AbstractAction("Alpha-numeric") {
            public void actionPerformed(ActionEvent e) {
                f_pref.putValue(WinValueProfile.MODE_DISPLAY_TEXT);
            }
        });
        JRadioButtonMenuItem r2 = new JRadioButtonMenuItem(new AbstractAction("Graph") {
            public void actionPerformed(ActionEvent e) {
                f_pref.putValue(WinValueProfile.MODE_DISPLAY_GRAPH);
            }
        });
        JRadioButtonMenuItem r3 = new JRadioButtonMenuItem(new AbstractAction("Alpha-numeric over graph") {
            public void actionPerformed(ActionEvent e) {
                f_pref.putValue(WinValueProfile.MODE_DISPLAY_TEXT_AND_GRAPH);
            }
        });
        ButtonGroup bg = new ButtonGroup();

        bg.add(r1);
        bg.add(r2);
        bg.add(r3);

        add(r1);
        add(r2);
        add(r3);

        if (pref.getValue() == WinValueProfile.MODE_DISPLAY_TEXT)
            r1.setSelected(true);
        else if (pref.getValue() == WinValueProfile.MODE_DISPLAY_TEXT_AND_GRAPH)
            r3.setSelected(true);
        else if (pref.getValue() == WinValueProfile.MODE_DISPLAY_GRAPH)
            r2.setSelected(true);
    }
}
