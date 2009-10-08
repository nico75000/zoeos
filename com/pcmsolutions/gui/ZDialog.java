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
public class ZDialog extends JDialog {
    private boolean centeredDone = false;

    public ZDialog(Frame owner) throws HeadlessException {
        super(owner);
    }

    public ZDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
    }

    public ZDialog(Frame owner, String title) throws HeadlessException {
        super(owner, title);
    }

    public ZDialog(Frame owner, String title, boolean modal) throws HeadlessException {
        super(owner, title, modal);
    }

    public void show() {
        if (!centeredDone) {
            center(this.getOwner());
            centeredDone = true;
        }
        super.show();
    }

    public void center(Component c) {
        if (c != null)
            setLocation(ScreenUtilities.centreRect(getOwner().getBounds(), getBounds()));
        else
            setLocation(ScreenUtilities.centreRect(ZoeosFrame.getInstance().getBounds(), getBounds()));
    }
}
