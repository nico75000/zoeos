package com.pcmsolutions.gui;

import javax.swing.*;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 27-Mar-2004
 * Time: 19:30:09
 */
public class ZJPanel extends JPanel {
    public ZJPanel() {
    }

    public ZJPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public ZJPanel(LayoutManager layout) {
        super(layout);
    }

    public ZJPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }
    /*
    protected void paintComponent(Graphics g) {
       // super.paintComponent(g);    //To change body of overridden methods use File | Settings | File Templates.

        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, getBackground(), 0, getHeight(),getBackground().brighter(), false);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

    }
    */
}
