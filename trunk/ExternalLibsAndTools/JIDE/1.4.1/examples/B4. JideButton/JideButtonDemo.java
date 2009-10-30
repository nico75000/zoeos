/*
 * @(#)CollapsiblePaneDemo.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideSwingUtilities;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Demoed Component: {@link JideButton}
 * <br>
 * Required jar files: jide-common.jar, jide-components.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class JideButtonDemo extends JFrame {

    private static JideButtonDemo _frame;
    private static JideButton[] _buttons;

    public JideButtonDemo(String title) throws HeadlessException {
        super(title);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LookAndFeelFactory.installJideExtension();
        }
        catch (ClassNotFoundException e) {
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (UnsupportedLookAndFeelException e) {
        }

        _frame = new JideButtonDemo("Demo of JideButton");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(6, 1));
        _buttons = new JideButton[6];
        _buttons[0] = createHyperlinkButton("Rename this file", ButtonsIconsFactory.getImageIcon(ButtonsIconsFactory.Buttons.RENAME));
        _buttons[1] = createHyperlinkButton("Move this file", ButtonsIconsFactory.getImageIcon(ButtonsIconsFactory.Buttons.MOVE));
        _buttons[2] = createHyperlinkButton("Copy this file", ButtonsIconsFactory.getImageIcon(ButtonsIconsFactory.Buttons.COPY));
        _buttons[3] = createHyperlinkButton("Publish this file", ButtonsIconsFactory.getImageIcon(ButtonsIconsFactory.Buttons.PUBLISH));
        _buttons[4] = createHyperlinkButton("Email this file", ButtonsIconsFactory.getImageIcon(ButtonsIconsFactory.Buttons.EMAIL));
        _buttons[5] = createHyperlinkButton("Delete this file", ButtonsIconsFactory.getImageIcon(ButtonsIconsFactory.Buttons.DELET));
        for (int i = 0; i < _buttons.length; i++) {
            JideButton button = _buttons[i];
            panel.add(button);
        }

        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(createOptionsPanel(), BorderLayout.AFTER_LINE_ENDS);
        JPanel topPanel = JideSwingUtilities.createTopPanel(panel);
        topPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        _frame.getContentPane().add(topPanel, BorderLayout.CENTER);

        _frame.setBounds(10, 10, 300, 500);

        _frame.setVisible(true);
    }

    private static JPanel createOptionsPanel() {
        JLabel header = new JLabel("Options");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13));
        header.setOpaque(true);
        header.setBackground(new Color(0, 0, 128));
        header.setForeground(Color.WHITE);
        header.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.lightGray, Color.lightGray, Color.gray),
                        BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        header.setPreferredSize(new Dimension(100, 30));
        final JRadioButton style1 = new JRadioButton("Toolbar Style");
        final JRadioButton style2 = new JRadioButton("Toolbox Style");
        final JRadioButton style3 = new JRadioButton("Flat Style");
        final JRadioButton style4 = new JRadioButton("Hyperlink Style");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(style1);
        buttonGroup.add(style2);
        buttonGroup.add(style3);
        buttonGroup.add(style4);
        JPanel switchPanel = new JPanel(new GridLayout(4, 1, 3, 3));
        switchPanel.add(style1);
        switchPanel.add(style2);
        switchPanel.add(style3);
        switchPanel.add(style4);
        style1.setSelected(true);

        style1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (style1.isSelected()) {
                    for (int i = 0; i < _buttons.length; i++) {
                        JideButton button = _buttons[i];
                        button.setButtonStyle(JideButton.TOOLBAR_STYLE);
                    }
                }
            }
        });
        style2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (style2.isSelected()) {
                    for (int i = 0; i < _buttons.length; i++) {
                        JideButton button = _buttons[i];
                        button.setButtonStyle(JideButton.TOOLBOX_STYLE);
                    }
                }
            }
        });
        style3.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (style3.isSelected()) {
                    for (int i = 0; i < _buttons.length; i++) {
                        JideButton button = _buttons[i];
                        button.setButtonStyle(JideButton.FLAT_STYLE);
                    }
                }
            }
        });
        style4.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (style4.isSelected()) {
                    for (int i = 0; i < _buttons.length; i++) {
                        JideButton button = _buttons[i];
                        button.setButtonStyle(JideButton.HYPERLINK_STYLE);
                    }
                }
            }
        });
        switchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panel = new JPanel();
        panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS));
        panel.add(header);
        panel.add(switchPanel);
        panel.add(Box.createGlue(), JideBoxLayout.VARY);
        return panel;
    }

    static JideButton createHyperlinkButton(String name, Icon icon) {
        final JideButton button = new JideButton(name, icon);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        return button;
    }
}
