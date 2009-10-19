package com.pcmsolutions.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 15-Nov-2003
 * Time: 06:55:41
 * To change this template use Options | File Templates.
 */
public class FocusAlerter implements FocusListener {
    private static final FocusAlerter INSTANCE = new FocusAlerter();

    public static FocusAlerter getInstance() {
        return INSTANCE;
    }

    public void focusGained(FocusEvent e) {
        //  if (!e.isTemporary() && e.getSource() instanceof JComponent)
        //    new FlashBorderThread((JComponent) e.getSource()).stateStart();
    }

    public void focusLost(FocusEvent e) {
    }
}
