/*
 * @(#)BorderDemo.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.swing.*;

import javax.swing.*;
import java.awt.*;

/**
 * Demoed Component: {@link PartialEtchedBorder}, {@link PartialLineBorder}, {@link JideTitledBorder}.
 * <br>
 * Required jar files: jide-common.jar
 * <br>
 * Required L&F: any L&F
 */
public class BorderDemo {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Demo of Additional Borders");
        frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS, 10));

        JTextArea textField = new JTextArea();
        JPanel border = new JPanel(new BorderLayout());
        border.setPreferredSize(new Dimension(100, 100));
        border.add(new JScrollPane(textField), BorderLayout.CENTER);
        border.setBorder(new JideTitledBorder(new PartialEtchedBorder(PartialEtchedBorder.LOWERED, PartialSide.NORTH), "PartialEtchedBorder"));

        JTextArea textField2 = new JTextArea();
        JPanel border2 = new JPanel(new BorderLayout());
        border2.setPreferredSize(new Dimension(100, 100));
        border2.add(new JScrollPane(textField2), BorderLayout.CENTER);
        border2.setBorder(new JideTitledBorder(new PartialLineBorder(Color.darkGray, 1, PartialSide.NORTH), "PartialLineBorder"));

        JTextArea textField3 = new JTextArea();
        JPanel border3 = new JPanel(new BorderLayout());
        border3.setPreferredSize(new Dimension(100, 100));
        border3.add(new JScrollPane(textField3), BorderLayout.CENTER);
        border3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
                new PartialLineBorder(Color.gray, 1, true), "Rounded Corners Border"), BorderFactory.createEmptyBorder(0, 6, 4, 6)));

        panel.add(border, JideBoxLayout.FLEXIBLE);
        panel.add(Box.createVerticalStrut(12));
        panel.add(border2, JideBoxLayout.FLEXIBLE);
        panel.add(Box.createVerticalStrut(12));
        panel.add(border3, JideBoxLayout.FLEXIBLE);
        panel.add(Box.createGlue(), JideBoxLayout.VARY);

        panel.setPreferredSize(new Dimension(500, 400));
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}
