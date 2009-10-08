package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class HideablePanel extends JPanel implements ActionListener, ZDisposable {
    private JButton showButton;
    private Hideable hideableComp;
    private JButton hideButton;
    private String showButtonText;

    public HideablePanel(Hideable c, boolean initiallyHidden) throws ZDeviceNotRunningException {
        this.hideableComp = c;
        this.hideButton = c.getHideButton();
        this.showButtonText = c.getShowButtonText();

        // setup look and layout
        this.setLayout(new BorderLayout());

        // listen for hide action
        hideButton.addActionListener(this);

        // create show button
        showButton = new JButton(showButtonText);
        // listen for show action
        showButton.addActionListener(this);

        // customize show button
        showButton.setMaximumSize(showButton.getPreferredSize());

        this.setBackground(UIColors.getDefaultBG());

        // TODO !! decide on this
        com.incors.plaf.alloy.AlloyCommonUtilities.set3DBackground(showButton, UIColors.getUtilityButtonBG());
        //showButton.setBackground(UIColors.getUtilityButtonBG());

        // addDesktopElement component to panel
        if (initiallyHidden)
            add(showButton);
        else
            add(hideableComp.getComponent());
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == hideButton) {
            remove(hideableComp.getComponent());
            add(showButton);
        } else if (e.getSource() == showButton) {
            add(hideableComp.getComponent());
            remove(showButton);
        }
        revalidate();
        repaint();
    }

    public Hideable getHideableComp() {
        return hideableComp;
    }

    public void zDispose() {
        if (hideableComp instanceof ZDisposable)
            ((ZDisposable) hideableComp).zDispose();
        hideableComp = null;
    }
}

