/*
 * @(#)JideBoxLayoutDemo.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.icons.JideIconsFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Demoed Component: {@link JideBoxLayout}
 * <br>
 * Required jar files: jide-common.jar
 * <br>
 * Required L&F: any L&F
 */
public class JideBoxLayoutDemo {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Demo of JideBoxLayout");
        frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(6, 6));

        // create a panel with JideBoxLayout
        JPanel panel = new JPanel();
        panel.setLayout(new JideBoxLayout(panel, 0, 6));

        JButton button = new JButton("FIX");
        button.setPreferredSize(new Dimension(60, 60));
        panel.add(button, JideBoxLayout.FIX);

        button = new JButton("FLEX");
        button.setPreferredSize(new Dimension(120, 60));
        panel.add(button, JideBoxLayout.FLEXIBLE);

        button = new JButton("VARY");
        button.setPreferredSize(new Dimension(120, 60));
        panel.add(button, JideBoxLayout.VARY);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(new JLabel("Resize the frame to see how each components are resized"), BorderLayout.BEFORE_FIRST_LINE);

        frame.pack();
        frame.setVisible(true);

    }
}
