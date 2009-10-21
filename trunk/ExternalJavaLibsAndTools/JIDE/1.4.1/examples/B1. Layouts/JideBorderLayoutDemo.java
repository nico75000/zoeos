/*
 * @(#)JideBorderLayoutDemo.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.swing.JideBorderLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Demoed Component: {@link JideBorderLayout}
 * <br>
 * Required jar files: jide-common.jar
 * <br>
 * Required L&F: any L&F
 */
public class JideBorderLayoutDemo {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Demo of JideBorderlayout");
        frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel panel1 = createJideBorderLayoutPanel();
        JPanel panel2 = createBorderLayoutPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(panel1);
        tabbedPane.add(panel2);

        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel createJideBorderLayoutPanel() {
        JPanel panel = new JPanel(new JideBorderLayout(6, 10));
        panel.setName("JideBorderLayout");
        JButton button = new JButton("NORTH");
        panel.add(button, JideBorderLayout.BEFORE_FIRST_LINE);
        button = new JButton("SOUTH");
        panel.add(button, JideBorderLayout.AFTER_LAST_LINE);
        button = new JButton("WEST");
        panel.add(button, JideBorderLayout.BEFORE_LINE_BEGINS);
        button = new JButton("EAST");
        panel.add(button, JideBorderLayout.AFTER_LINE_ENDS);
        button = new JButton("CENTER");
        button.setPreferredSize(new Dimension(200, 100));
        panel.add(button, JideBorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private static JPanel createBorderLayoutPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 10));
        panel.setName("BorderLayout");
        JButton button = new JButton("NORTH");
        panel.add(button, JideBorderLayout.BEFORE_FIRST_LINE);
        button = new JButton("SOUTH");
        panel.add(button, JideBorderLayout.AFTER_LAST_LINE);
        button = new JButton("WEST");
        panel.add(button, JideBorderLayout.BEFORE_LINE_BEGINS);
        button = new JButton("EAST");
        panel.add(button, JideBorderLayout.AFTER_LINE_ENDS);
        button = new JButton("CENTER");
        button.setPreferredSize(new Dimension(200, 100));
        panel.add(button, JideBorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }
}
