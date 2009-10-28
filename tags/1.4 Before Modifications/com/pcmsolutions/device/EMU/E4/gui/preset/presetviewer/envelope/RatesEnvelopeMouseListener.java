package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope;

import com.pcmsolutions.device.EMU.E4.DevicePreferences;
import com.pcmsolutions.system.preferences.ZBoolPref;
import com.pcmsolutions.system.preferences.ZEnumPref;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * User: paulmeehan
 * Date: 19-Feb-2004
 * Time: 17:58:42
 */
public class RatesEnvelopeMouseListener implements MouseListener {
    private ZBoolPref fillPref;
    private ZEnumPref modePref;
    private RatesEnvelope env;

    public RatesEnvelopeMouseListener(RatesEnvelope env, ZBoolPref fillPref, ZEnumPref modePref) {
        this.fillPref = fillPref;
        this.modePref = modePref;
        this.env = env;
    }

    public void mouseClicked(MouseEvent e) {
        checkPopup(e);
    }

    public void mousePressed(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    protected boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JPopupMenu p = new JPopupMenu();
            boolean addFixed = false, addScaled = false;
            String s = modePref.getValue();
            if (s.equals(DevicePreferences.ENVELOPE_MODE_FIXED)) {
                addFixed = false;
                addScaled = true;
            } else if (s.equals(DevicePreferences.ENVELOPE_MODE_SCALED)) {
                addFixed = true;
                addScaled = false;
            }

            if (addScaled)
                p.add(new AbstractAction("Scale") {
                    public void actionPerformed(ActionEvent e) {
                        modePref.putValue(DevicePreferences.ENVELOPE_MODE_SCALED);
                    }
                });
            if (addFixed)
                p.add(new AbstractAction("Fixed") {
                    public void actionPerformed(ActionEvent e) {
                        modePref.putValue(DevicePreferences.ENVELOPE_MODE_FIXED);
                    }
                });

            p.add(new AbstractAction("Toggle Fill") {
                public void actionPerformed(ActionEvent e) {
                    fillPref.toggleValue();
                }
            });
            p.show(env, e.getX(), e.getY());
        }

        return true;
    }
}
