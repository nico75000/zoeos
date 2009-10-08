package com.pcmsolutions.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Mar-2003
 * Time: 23:00:21
 * To change this template use Options | File Templates.
 */
public class ZDeviceManagerDialog extends ZDialog implements HierarchyListener {
    public ZDeviceManagerDialog(Frame ownerFrame, boolean modal) throws HeadlessException {
        super(ownerFrame, "Device Manager", modal);
        JButton hideButt = new JButton(new AbstractAction("Hide") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        hideButt.setAlignmentX(Component.LEFT_ALIGNMENT);
        getRootPane().setDefaultButton(hideButt);

        JPanel buttPanel = new JPanel();
        buttPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttPanel.add(hideButt);
        //buttPanel.setOpaque(false);

        JPanel displayPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                /*int w = getWidth();
                int h = getHeight();
                Graphics2D g2d = ((Graphics2D) g);
                GradientPaint gp = new GradientPaint(0, 0, Color.white, 0, h, Color.LIGHT_GRAY, false);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.setColor(Zoeos.logoColor);
                g2d.setFont(Zoeos.logoFont);
                g2d.drawString(Zoeos.logoStr, w / 2, h / 2);
                */
                super.paintComponent(g);
            }

        };
        //displayPanel.setOpaque(false);

        displayPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        ZDeviceManagerPanel dmp = new ZDeviceManagerPanel(this);
        displayPanel.add(dmp);
        displayPanel.add(buttPanel);
        dmp.addHierarchyListener(this);
        this.setContentPane(displayPanel);
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        // ZUtilities.applyHideButton(this, false, false);
        pack();
    }

    public void hierarchyChanged(HierarchyEvent e) {
        pack();
    }

}
