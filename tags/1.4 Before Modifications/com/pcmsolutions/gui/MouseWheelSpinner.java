package com.pcmsolutions.gui;

import javax.swing.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Oct-2003
 * Time: 21:26:40
 * To change this template use Options | File Templates.
 */
public class MouseWheelSpinner extends JSpinner implements MouseWheelListener {

    public MouseWheelSpinner() {
        addMouseWheelListener(this);
    }

    public MouseWheelSpinner(SpinnerModel model) {
        super(model);
        addMouseWheelListener(this);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int r = e.getWheelRotation();
        Object next;
        if (r == 0)
            return;
        if (r < 0)
            while (r++ != 0) {
                next = (MouseWheelBehaviour.upPolarity ? getNextValue() : getPreviousValue());
                if (next != null)
                    setValue(next);
            }
        else
            while (r-- != 0) {
                next = (MouseWheelBehaviour.upPolarity ? getPreviousValue() : getNextValue());
                if (next != null)
                    setValue(next);
            }
    }
}