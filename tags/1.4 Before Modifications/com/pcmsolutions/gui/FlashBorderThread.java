package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.system.threads.Impl_ZThread;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 15-Nov-2003
 * Time: 05:51:52
 * To change this template use Options | File Templates.
 */
public class FlashBorderThread extends Impl_ZThread {
    private long timeout = 75;
    private Color borderColor = UIColors.applyAlpha(Color.red, 100);
    private int borderThickness = 3;
    private JComponent c;

    public FlashBorderThread(JComponent c) {
        super("Flash border");
        this.c = c;
    }

    public FlashBorderThread(JComponent c, long timeout, Color borderColor, int borderThickness) {
        super("Flash border");
        this.timeout = timeout;
        this.borderColor = borderColor;
        this.borderThickness = borderThickness;
        this.c = c;
    }

    public void runBody() {
        final Border b = c.getBorder();
        c.setBorder(new CompoundBorder(new LineBorder(borderColor, borderThickness, true), b));
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    if (c != null) {
                        c.setBorder(b);
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
