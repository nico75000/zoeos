/*
 * @(#)BannerPanelDemo.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * Demoed Component: {@link BannerPanel}
 * <br>
 * Required jar files: jide-common.jar, jide-dialogs.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class BannerPanelDemo {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Demo of BannerPanel");
        frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS, 10));

        BannerPanel headerPanel0 = new BannerPanel("This is also a BannerPanel", null);
        headerPanel0.setFont(new Font("Tahoma", Font.PLAIN, 11));
        headerPanel0.setBackground(new Color(0, 0, 128));
        headerPanel0.setForeground(Color.WHITE);
        headerPanel0.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.lightGray, Color.lightGray, Color.gray));

        BannerPanel headerPanel1 = new BannerPanel("This is a BannerPanel", "BannerPanel is very useful to display a title, a description and an icon. It can be used in dialog to show some help information or display a product logo in a nice way.",
                JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32));
        headerPanel1.setFont(new Font("Tahoma", Font.PLAIN, 11));
        headerPanel1.setBackground(Color.WHITE);
        headerPanel1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        BannerPanel headerPanel2 = new BannerPanel("This is a BannerPanel", "BannerPanel is very useful to display a title, a description and an icon. It can be used in dialog to show some help information or display a product logo in a nice way.",
                JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32));
        headerPanel2.setFont(new Font("Tahoma", Font.PLAIN, 11));
        headerPanel2.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        headerPanel2.setBackgroundPaint(new GradientPaint(0, 0, new Color(0, 0, 128), 500, 0, Color.WHITE));

        panel.add(headerPanel1, JideBoxLayout.FLEXIBLE);
        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);
        panel.add(headerPanel2, JideBoxLayout.FLEXIBLE);
        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);
        panel.add(headerPanel0, JideBoxLayout.FLEXIBLE);
        panel.add(Box.createGlue(), JideBoxLayout.VARY);

        panel.setPreferredSize(new Dimension(500, 300));
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.getContentPane().doLayout();
    }
}
