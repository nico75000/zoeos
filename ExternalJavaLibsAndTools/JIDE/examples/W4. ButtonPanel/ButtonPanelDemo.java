/*
 * @(#)ButtonPanelDemo.java	Dec 7, 2002
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.MultilineLabel;
import com.jidesoft.combobox.ListComboBox;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.utils.SystemInfo;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Demoed Component: {@link ButtonPanel}
 * <br>
 * Required jar files: jide-common.jar, jide-dialogs.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class ButtonPanelDemo {
    private static JSplitPane _pane;
    private static JFrame _frame;
    private static JTextField _orderTextField;
    private static JTextField _oppositeOrderTextField;
    private static JTextField _grpGapTextField;
    private static JTextField _btnGapTextField;
    private static JTextField _minBtnWidthTextField;

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

        _frame = new JFrame("Demo of ButtonPanel");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.setSizeContraint(ButtonPanel.NO_LESS_THAN);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton button = new JButton("Preview");
        buttonPanel.add(button, ButtonPanel.OTHER_BUTTON);
        button = new JButton("Save As PDF...");
        buttonPanel.add(button, ButtonPanel.OTHER_BUTTON);
        button = new JButton("Fax...");
        buttonPanel.add(button, ButtonPanel.OTHER_BUTTON);
        button = new JButton("Cancel");
        buttonPanel.add(button, ButtonPanel.CANCEL_BUTTON);
        button = new JButton("Print");
        buttonPanel.add(button, ButtonPanel.AFFIRMATIVE_BUTTON);
        button = new JButton("Help");
        buttonPanel.add(button, ButtonPanel.HELP_BUTTON);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("This area is for the content of the dialog", JLabel.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.gray)));
        panel.add(label, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.AFTER_LAST_LINE);

        JPanel controlPanel = createControlPanel(buttonPanel);

        _pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, controlPanel);
        _frame.setContentPane(_pane);
        _frame.pack();
        _frame.setSize(1000, 600);
        _pane.setDividerLocation(780);
        _frame.setVisible(true);
    }

    static JPanel createControlPanel(final ButtonPanel buttonPanel) {
        JPanel panel = new JPanel();
        panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS));

        panel.add(new JLabel("LookAndFeel:"));
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        Object[] lnf = new Object[]{
            "Windows L&F (on Windows only)", "Metal L&F", "Aqua L&F (on MacOSX only)"};
        final JComboBox lnfComboBox = new JComboBox(lnf);
        if(SystemInfo.isWindows()) {
            lnfComboBox.setSelectedIndex(0);
        }
        else if(SystemInfo.isMacOSX()) {
            lnfComboBox.setSelectedIndex(2);
        }
        else {
            lnfComboBox.setSelectedIndex(1);
        }
        lnfComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int value = lnfComboBox.getSelectedIndex();
                    try {
                        switch (value) {
                            case 0:
                                if(SystemInfo.isWindows()) {
                                    UIManager.setLookAndFeel(LookAndFeelFactory.create(LookAndFeelFactory.WINDOWS_LNF));
                                }
                                else {
                                    UIManager.setLookAndFeel(LookAndFeelFactory.create(LookAndFeelFactory.METAL_LNF));
                                }
                                break;
                            case 1:
                                UIManager.setLookAndFeel(LookAndFeelFactory.create(LookAndFeelFactory.METAL_LNF));
                                break;
                            case 2:
                                if(SystemInfo.isMacOSX()) {
                                    UIManager.setLookAndFeel(LookAndFeelFactory.create(LookAndFeelFactory.AQUA_LNF));
                                }
                                else {
                                    UIManager.setLookAndFeel(LookAndFeelFactory.create(LookAndFeelFactory.METAL_LNF));
                                }
                                break;
                        }
                    } catch (UnsupportedLookAndFeelException e1) {
                    }
                    SwingUtilities.updateComponentTreeUI(_frame);
                    _orderTextField.setText(UIManager.getString("ButtonPanel.order"));
                    _oppositeOrderTextField.setText(UIManager.getString("ButtonPanel.oppositeOrder"));
                    _grpGapTextField.setText("" + UIManager.getInt("ButtonPanel.groupGap"));
                    _btnGapTextField.setText("" + UIManager.getInt("ButtonPanel.buttonGap"));
                    _minBtnWidthTextField.setText("" + UIManager.getInt("ButtonPanel.minButtonWidth"));
                    doLayout(buttonPanel);
                }
            }
        });
        panel.add(lnfComboBox);
        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        panel.add(new JLabel("Alignment:"));
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        Object[] alignments = new Object[]{
            "SwingConstants.CENTER", "SwingConstants.TOP", "SwingConstants.LEFT", "SwingConstants.BOTTOM", "SwingConstants.RIGHT"};
        final JComboBox comboBox = new JComboBox(alignments);
        comboBox.setSelectedIndex(buttonPanel.getAlignment());
        comboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selected = comboBox.getSelectedIndex();
                    if (selected == SwingConstants.TOP || selected == SwingConstants.BOTTOM) {
                        JPanel panel = (JPanel) buttonPanel.getParent();
                        panel.add(buttonPanel, BorderLayout.AFTER_LINE_ENDS);
                        buttonPanel.setAlignment(selected);
                        doLayout(buttonPanel);
                    } else {
                        JPanel panel = (JPanel) buttonPanel.getParent();
                        panel.add(buttonPanel, BorderLayout.AFTER_LAST_LINE);
                        buttonPanel.setAlignment(selected);
                        doLayout(buttonPanel);
                    }
                }
            }
        });
        panel.add(comboBox);

        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        panel.add(new JLabel("Button Order(*):"));
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _orderTextField = new JTextField(buttonPanel.getButtonOrder());
        panel.add(_orderTextField);
        _orderTextField.setColumns(6);

        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        panel.add(new JLabel("Opposite Button Order(*):"));
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _oppositeOrderTextField = new JTextField(buttonPanel.getOppositeButtonOrder());
        panel.add(_oppositeOrderTextField);
        _oppositeOrderTextField.setColumns(6);

        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        panel.add(new JLabel("Gap between Button Groups(*):"));
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _grpGapTextField = new JTextField("" + buttonPanel.getGroupGap());
        panel.add(_grpGapTextField);
        _grpGapTextField.setColumns(4);

        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        panel.add(new JLabel("Gap between Buttons(*):"));
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _btnGapTextField = new JTextField("" + buttonPanel.getButtonGap());
        panel.add(_btnGapTextField);
        _btnGapTextField.setColumns(4);

        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        panel.add(new JLabel("Minumum Button Width(*):"));
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        _minBtnWidthTextField = new JTextField("" + buttonPanel.getMinButtonWidth());
        panel.add(_minBtnWidthTextField);
        _minBtnWidthTextField.setColumns(4);

        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        panel.add(new JLabel("Size Constraint:"));
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        final JRadioButton sameSize = new JRadioButton("Same Size");
        panel.add(sameSize);

        final JRadioButton noLessThan = new JRadioButton("No Less Than");
        panel.add(noLessThan);

        ButtonGroup size = new ButtonGroup();
        size.add(sameSize);
        size.add(noLessThan);

        sameSize.setSelected(buttonPanel.getSizeContraint() == ButtonPanel.SAME_SIZE);
        noLessThan.setSelected(buttonPanel.getSizeContraint() == ButtonPanel.NO_LESS_THAN);

        sameSize.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (sameSize.isSelected()) {
                    buttonPanel.setSizeContraint(ButtonPanel.SAME_SIZE);
                    doLayout(buttonPanel);
                } else {
                    buttonPanel.setSizeContraint(ButtonPanel.NO_LESS_THAN);
                    doLayout(buttonPanel);
                }
            }
        });

        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

        panel.add(Box.createGlue(), JideBoxLayout.VARY);

        final JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = _orderTextField.getText();
                buttonPanel.setButtonOrder(text);
                String text2 = _oppositeOrderTextField.getText();
                buttonPanel.setOppositeButtonOrder(text2);
                try {
                    String text3 = _grpGapTextField.getText();
                    buttonPanel.setGroupGap(Integer.parseInt(text3));
                } catch (NumberFormatException e1) {
                }
                try {
                    String text4 = _btnGapTextField.getText();
                    buttonPanel.setButtonGap(Integer.parseInt(text4));
                } catch (NumberFormatException e1) {
                }
                doLayout(buttonPanel);
            }
        });
        panel.add(new MultilineLabel("Press refresh to see the change to fields with \"*\""));
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        panel.add(refresh);

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(6, 6));
        JLabel header = new JLabel("Options");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13));
        header.setOpaque(true);
        header.setBackground(new Color(0, 0, 128));
        header.setForeground(Color.WHITE);
        header.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.lightGray, Color.lightGray, Color.gray),
                        BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        header.setPreferredSize(new Dimension(100, 30));
        topPanel.add(header, BorderLayout.BEFORE_FIRST_LINE);
        topPanel.add(panel, BorderLayout.CENTER);

        topPanel.setMinimumSize(new Dimension(0, 0));
        return topPanel;
    }

    private static void doLayout(final ButtonPanel buttonPanel) {
        JPanel panel = (JPanel) buttonPanel.getParent();
        buttonPanel.invalidate();
        panel.doLayout();
        buttonPanel.doLayout();
        buttonPanel.repaint();
        panel.repaint();
    }
}
