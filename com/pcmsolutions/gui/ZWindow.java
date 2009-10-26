package com.pcmsolutions.gui;

import com.pcmsolutions.util.ScreenUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 25-Apr-2003
 * Time: 02:21:45
 * To change this template use Options | File Templates.
 */
public class ZWindow extends JWindow {
    private Component centreAboutComponent;

    public ZWindow(Frame owner, Component centreAboutComponent) throws HeadlessException {
        super(owner);
        this.centreAboutComponent = centreAboutComponent;
    }

    public ZWindow(Frame owner) throws HeadlessException {
        this(owner, owner);
    }

    public void show() {
        center();
        super.show();
    }

    public void setVisible(boolean b) {
        if (b)
            center();
        super.setVisible(b);
    }

    public void center() {
        Component c;
        if (centreAboutComponent != null)
            c = centreAboutComponent;
        else
            c = this.getOwner();

        Rectangle cRect = c.getBounds();
        Component parent = c.getParent();
        if (parent == null)
            parent = c;
        Point tp = SwingUtilities.convertPoint(parent, cRect.getLocation(), getOwner());
        Point loc = ScreenUtilities.centreRect(new Rectangle(tp, cRect.getSize()), getBounds());
        setLocation(loc);
    }
}
