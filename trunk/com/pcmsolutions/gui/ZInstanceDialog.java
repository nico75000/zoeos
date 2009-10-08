package com.pcmsolutions.gui;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-May-2003
 * Time: 10:48:32
 * To change this template use Options | File Templates.
 */
public class ZInstanceDialog extends ZDialog {
    {
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                dispose();
            }
        });

    }

    public ZInstanceDialog(Frame owner) throws HeadlessException {
        super(owner);
    }

    public ZInstanceDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
    }

    public ZInstanceDialog(Frame owner, String title) throws HeadlessException {
        super(owner, title);
    }

    public ZInstanceDialog(Frame owner, String title, boolean modal) throws HeadlessException {
        super(owner, title, modal);
    }
}
